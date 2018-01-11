package health.monday.servlets.providers;

import health.monday.BaseServletHandler;
import health.monday.exceptions.InvalidCertificateException;
import health.monday.servlets.BaseHTTPServlet;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@WebServlet(name = "ListProviders", urlPatterns = {"/providers/list"})
public class ListProvidersServlet extends BaseHTTPServlet
{
	private class Handler extends BaseServletHandler
	{
		Handler(HttpServletRequest req, HttpServletResponse resp)
				throws InvalidCertificateException
		{
			super(req, resp);
		}

		public void get() throws IOException, SQLException
		{
			initDBConnection();
			Statement s = connection.createStatement();
			ResultSet r =
					s.executeQuery("SELECT COUNT(*) FROM monday.provider");

			if (!r.next())
			{
				failure(0);
				return;
			}

			success(r.getInt(1));
		}
	}

	@Override
	protected BaseServletHandler getNewHandler(HttpServletRequest request,
											   HttpServletResponse response)
			throws InvalidCertificateException
	{
		return new Handler(request, response);
	}
}
