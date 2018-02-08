package health.monday.servlets.providers;

import health.monday.exceptions.InvalidCertificateException;
import health.monday.managers.DatabaseManager;
import health.monday.models.ProviderDetail;
import health.monday.servlets.BaseHTTPServlet;
import health.monday.servlets.BaseServletHandler;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet(name = "ProviderDetail", urlPatterns = {"/providers/detail/*"})
public class ProviderDetailServlet extends BaseHTTPServlet
{
	private static final String query;

	static
	{
		query = loadQuery("detail");
	}

	private class Handler extends BaseServletHandler
	{
		Handler(HttpServletRequest req, HttpServletResponse resp)
				throws InvalidCertificateException
		{
			super(req, resp);
		}

		public void get() throws IOException, SQLException, ServletException
		{
			final String[] tokens = getPathComponents();
			final int id = Integer.parseInt(tokens[1]);

			ProviderDetail result;

			try (final Connection c = DatabaseManager.connection())
			{
				final PreparedStatement s = c.prepareStatement(query);
				s.setInt(1, id);

				final ResultSet r = s.executeQuery();

				if (!r.next())
				{
					throw new ServletException("No results for id " + id);
				}

				result = new ProviderDetail(r);
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
