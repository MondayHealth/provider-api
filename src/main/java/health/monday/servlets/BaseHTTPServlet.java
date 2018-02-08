package health.monday.servlets;

import health.monday.exceptions.InvalidCertificateException;
import health.monday.managers.DatabaseManager;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

public abstract class BaseHTTPServlet extends HttpServlet
{
	protected void doGet(HttpServletRequest request,
						 HttpServletResponse response)
			throws IOException, ServletException
	{
		final BaseServletHandler handler = getNewHandler(request, response);

		try
		{
			handler.get();
		}
		catch (SQLException e)
		{
			final String msg =
					String.format("SQL Exception: %s", e.getMessage());
			throw new ServletException(msg, e.getCause());
		}
	}

	private static final String queryPath = "/sql/";

	private static String convertStreamToString(java.io.InputStream is)
	{
		java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}

	static protected String loadQuery(final String path)
	{
		final String p = queryPath + path + ".sql";
		try (InputStream r = DatabaseManager.class.getResourceAsStream(p))
		{
			if (r == null)
			{
				throw new RuntimeException("Couldn't find file " + path);
			}
			return convertStreamToString(r);
		}
		catch (IOException e)
		{
			throw new RuntimeException(String.format("ioe on %s : %s ", path,
					e.getMessage()));
		}
	}

	protected abstract BaseServletHandler getNewHandler(final
														HttpServletRequest
																request,
														final
														HttpServletResponse
																response)
			throws InvalidCertificateException;
}
