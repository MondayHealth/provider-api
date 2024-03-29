package health.monday.servlets;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import health.monday.exceptions.InvalidCertificateException;
import health.monday.exceptions.InvalidParameterException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ResourceBundle;

abstract public class BaseServletHandler
{
	private final String emailAddress;

	private final String userName;

	private final HttpServletRequest request;

	private final HttpServletResponse response;

	private static final Gson serializer = new Gson();

	private static final String LSTRING_FILE =
			"javax.servlet.http.LocalStrings";

	private static ResourceBundle lStrings =
			ResourceBundle.getBundle(LSTRING_FILE);

	String getEmailAddress()
	{
		return emailAddress;
	}

	String getUserName()
	{
		return userName;
	}

	private class Response
	{
		@SuppressWarnings("unused")
		final private boolean success;

		@SuppressWarnings("unused")
		final private Object result;

		Response(boolean s, Object o)
		{
			success = s;
			result = o;
		}
	}

	private class FailureResponse
	{
		final private boolean success = false;

		final private int code;

		FailureResponse(final int c)
		{
			code = c;
		}
	}

	private String[] auth() throws InvalidCertificateException
	{
		final String cert = request.getHeader("x-ssl-s-dn");

		if (cert == null)
		{
			throw new InvalidCertificateException("(null)");
		}

		final String[] tokens = cert.split(",");
		final String[] emailTokens = tokens[0].split("=");
		final String[] commonNameTokens = tokens[1].split("=");

		if (emailTokens.length != 2 || !emailTokens[0].equals("emailAddress"))
		{
			throw new InvalidCertificateException(cert);
		}

		final String[] ret = new String[2];
		ret[0] = emailTokens[1];

		if (commonNameTokens.length != 2 || !commonNameTokens[0].equals("CN"))
		{
			throw new InvalidCertificateException(cert);
		}

		ret[1] = commonNameTokens[1];

		return ret;
	}

	protected BaseServletHandler(final HttpServletRequest req,
								 final HttpServletResponse resp)
			throws InvalidCertificateException
	{
		request = req;
		response = resp;

		final String[] tokens = auth();
		emailAddress = tokens[0];
		userName = tokens[1];

		response.setCharacterEncoding("utf-8");
		response.setContentType("application/json");
	}

	private void responseNotAllowed() throws IOException
	{
		final String protocol = request.getProtocol();
		final String msg = lStrings.getString("http.method_get_not_supported");
		if (protocol.endsWith("1.1"))
		{
			response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, msg);
		}
		else
		{
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, msg);
		}
	}

	public void get() throws IOException, ServletException, SQLException
	{
		responseNotAllowed();
	}

	protected void success() throws IOException
	{
		final JsonObject obj = new JsonObject();
		obj.addProperty("success", true);
		respond(obj);
	}

	protected void failure(final int code) throws IOException
	{
		respond(new FailureResponse(code));
	}

	protected void success(final Object result) throws IOException
	{
		respond(new Response(true, result));
	}

	private void respond(final Object result) throws IOException
	{
		response.getWriter().print(serializer.toJson(result));
	}

	private String requireParameter(final String name)
			throws InvalidParameterException
	{
		final String ret = request.getParameter(name);
		if (ret == null)
		{
			throw new InvalidParameterException(name, "required");
		}
		return ret;
	}

	protected String[] getPathComponents()
	{
		return request.getPathInfo().split("/");
	}

	protected int requireInt(final String name) throws
			InvalidParameterException
	{
		return Integer.parseInt(requireParameter(name));
	}

	protected String stringParameter(final String name,
									 final String defaultValue)
	{
		final String ret = request.getParameter(name);
		return ret == null ? defaultValue : ret;
	}

	protected int intParameter(final String name, final int defaultValue)
	{
		final String ret = request.getParameter(name);
		return ret == null ? defaultValue : Integer.parseInt(ret);
	}

	/**
	 * @param name The name of the parameter
	 *
	 * @return {@code true} IFF the value of the parameter is either "true" or
	 * 		an int not equal to zero, otherwise return {@code false}.
	 */
	protected boolean boolParameter(final String name)
	{
		final String ret = request.getParameter(name);

		if (ret == null)
		{
			return false;
		}

		if (ret.toLowerCase().equals("true"))
		{
			return true;
		}

		try
		{
			return Integer.parseInt(ret) != 0;
		}
		catch (NumberFormatException ignored)
		{
		}

		return false;
	}

	protected double doubleParameter(final String name,
									 final double defaultValue)
	{
		final String ret = request.getParameter(name);
		return ret == null ? defaultValue : Double.parseDouble(ret);
	}

	protected Double doubleOrNullParameter(final String name)
	{
		final String ret = request.getParameter(name);
		return ret == null ? null : Double.parseDouble(ret);
	}
}
