package health.monday.servlets;

import health.monday.exceptions.InvalidCertificateException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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

	protected abstract BaseServletHandler getNewHandler(final
														HttpServletRequest
																request,
														final
														HttpServletResponse
																response)
			throws InvalidCertificateException;
}
