package health.monday.servlets;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import health.monday.exceptions.InvalidCertificateException;

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
		final private boolean success;

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

	protected BaseServletHandler(final HttpServletRequest req,
								 final HttpServletResponse resp)
			throws InvalidCertificateException
	{
		request = req;
		response = resp;

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

		emailAddress = emailTokens[1];

		if (commonNameTokens.length != 2 || !commonNameTokens[0].equals("CN"))
		{
			throw new InvalidCertificateException(cert);
		}

		userName = commonNameTokens[1];
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

	protected String requireParameter(final String name) throws
			ServletException
	{
		final String ret = request.getParameter(name);
		if (ret == null)
		{
			throw new ServletException("Missing required parameter: " + name);
		}
		return ret;
	}

	protected int requireInt(final String name) throws ServletException
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
}
