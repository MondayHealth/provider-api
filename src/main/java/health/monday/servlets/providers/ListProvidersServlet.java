package health.monday.servlets.providers;

import health.monday.exceptions.InvalidCertificateException;
import health.monday.managers.DatabaseManager;
import health.monday.models.Provider;
import health.monday.servlets.BaseHTTPServlet;
import health.monday.servlets.BaseServletHandler;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;

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

		private final String query = "SELECT id, first_name, last_name " +
				"FROM monday.provider " +
				"ORDER BY last_name DESC " +
				"LIMIT ? OFFSET ?";

		public void get() throws IOException, SQLException
		{
			final int count = 1000;
			final Provider[] result = new Provider[count];
			final int offset = 10000;
			try (final Connection conn = DatabaseManager.connection())
			{
				PreparedStatement s = conn.prepareStatement(query);
				s.setInt(1, count);
				s.setInt(2, offset);
				ResultSet r = s.executeQuery();

				int i = 0;
				while (r.next())
				{
					result[i++] = new Provider(r);
				}
			}

			success(result);
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
