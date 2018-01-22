package health.monday.servlets.providers;

import health.monday.exceptions.InvalidCertificateException;
import health.monday.managers.DatabaseManager;
import health.monday.models.Provider;
import health.monday.servlets.BaseHTTPServlet;
import health.monday.servlets.BaseServletHandler;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import static org.jooq.impl.DSL.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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

		private final String queryPrefix =
				"SELECT DISTINCT p.id, p.first_name, p.last_name, p" +
						".website_url, array_agg(pc.credential_id) AS " +
						"credentials " +
						"FROM monday.provider p " +
						"JOIN monday.providers_credentials pc ON p.id = pc" +
						".provider_id ";

		private final String filterByPayorJoins =
				"JOIN monday.providers_plans pp ON p.id = pp.provider_id " +
						"JOIN monday.plan plan ON pp.plan_id = plan.id ";

		private final String filterByPayorWhere = "WHERE plan.payor_id = ? ";

		private final String querySuffix = "GROUP BY p.id " +
				"ORDER BY p.last_name ASC " +
				"LIMIT ? OFFSET ?";

		public void get() throws IOException, SQLException, ServletException
		{
			final int count = requireInt("count");
			final int offset = requireInt("offset");
			final int payor = intParameter("payor", 0);
			final Provider[] result = new Provider[count];

			String query = queryPrefix;

			if (payor > 0)
			{
				query += filterByPayorJoins;
				query += filterByPayorWhere;
			}

			query += querySuffix;

			final DSLContext create = DSL.using(SQLDialect.POSTGRES_9_5);
			final String q = create.select(field("provider.id")).getSQL();

			try (final Connection conn = DatabaseManager.connection())
			{

				int idx = 1;
				PreparedStatement s = conn.prepareStatement(query);

				if (payor > 0)
				{
					s.setInt(idx++, payor);
				}

				s.setInt(idx++, count);
				s.setInt(idx, offset);
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
