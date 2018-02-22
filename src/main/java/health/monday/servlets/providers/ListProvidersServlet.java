package health.monday.servlets.providers;

import health.monday.exceptions.InvalidCertificateException;
import health.monday.managers.DatabaseManager;
import health.monday.models.Provider;
import health.monday.servlets.BaseHTTPServlet;
import health.monday.servlets.BaseServletHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
	private static final Logger logger = LogManager.getLogger();

	private static final String providerQuery;

	private static final String providerByPayorQuery;

	private static final String providerBySpecialtyQuery;

	private static final String providerByCoordinate;

	private static final String providerByLanguage;

	private static final String providerByModality;

	private static final double MILE_IN_METERS = 1609.34;

	private static final double MIN_RADIUS_METERS = 500;

	private static final double MAX_RADIUS_METERS = MILE_IN_METERS * 50;

	static
	{
		providerQuery = loadQuery("provider");
		providerByPayorQuery = loadQuery("provider-by-payor");
		providerBySpecialtyQuery = loadQuery("provider-by-specialty");
		providerByCoordinate = loadQuery("provider-by-coord");
		providerByLanguage = loadQuery("provider-by-language");
		providerByModality = loadQuery("provider-by-modality");
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
			final int count = requireInt("count");
			final int offset = requireInt("offset");
			final int payor = intParameter("payor", 0);
			final int specialty = intParameter("specialty", 0);
			final int modality = intParameter("modality", 0);
			final String feeRange = stringParameter("feeRange", null);
			final int gender = intParameter("gender", 0);
			final int language = intParameter("language", 0);
			final Double lat = doubleOrNullParameter("lat");
			final Double lng = doubleOrNullParameter("lng");
			final Double radius = doubleParameter("radius", MILE_IN_METERS);
			final Provider[] result = new Provider[count];

			if (lat == null ^ lng == null)
			{
				throw new SQLException("Invalid lat/lng pair.");
			}

			if (radius > MAX_RADIUS_METERS)
			{
				throw new SQLException("Radius too large: " + radius);
			}

			if (radius < MIN_RADIUS_METERS)
			{
				throw new SQLException("Radius too small: " + radius);
			}

			String query = providerQuery;

			int whereClauses = 0;

			if (payor > 0)
			{
				query += " WHERE pro.id IN (";
				query += providerByPayorQuery;
				query += ") ";
				whereClauses += 1;
			}

			if (specialty > 0)
			{
				query += whereClauses > 0 ? " AND " : " WHERE ";
				query += "pro.id IN (";
				query += providerBySpecialtyQuery;
				query += ") ";
				whereClauses += 1;
			}

			if (lat != null)
			{
				query += whereClauses > 0 ? " AND " : " WHERE ";
				query += "pro.id IN (";
				query += providerByCoordinate;
				query += ") ";
				whereClauses += 1;
			}

			if (feeRange != null)
			{
				query += whereClauses > 0 ? " AND " : " WHERE ";
				query += "pro.minimum_fee >= ? and pro.maximum_fee <= ? ";
				whereClauses += 1;
			}

			if (gender > 0)
			{
				if (gender > 2)
				{
					throw new SQLException("Invalid gender index " + gender);
				}

				query += whereClauses > 0 ? " AND " : " WHERE ";
				query += "pro.gender = ?";
				whereClauses += 1;
			}

			if (language > 0)
			{
				query += whereClauses > 0 ? " AND " : " WHERE ";
				query += "pro.id IN (" + providerByLanguage + ") ";
				whereClauses += 1;
			}

			if (modality > 0)
			{
				query += whereClauses > 0 ? " AND " : " WHERE ";
				query += "pro.id IN (" + providerByModality + ") ";
				whereClauses += 1;
			}

			query += " ORDER BY pro.last_name ASC LIMIT ? OFFSET ? ";

			try (final Connection conn = DatabaseManager.connection())
			{

				int idx = 1;
				PreparedStatement s = conn.prepareStatement(query);

				if (payor > 0)
				{
					s.setInt(idx++, payor);
				}

				if (specialty > 0)
				{
					s.setInt(idx++, specialty);
				}

				if (lat != null)
				{
					s.setDouble(idx++, lng);
					s.setDouble(idx++, lat);
					s.setDouble(idx++, radius);
				}

				if (feeRange != null)
				{
					final String[] tokens = feeRange.split(",");
					s.setInt(idx++, Integer.parseInt(tokens[0]));
					s.setInt(idx++, Integer.parseInt(tokens[1]));
				}

				if (gender > 0)
				{
					s.setString(idx++, gender == 1 ? "F" : "M");
				}

				if (language > 0)
				{
					s.setInt(idx++, language);
				}

				if (modality > 0)
				{
					s.setInt(idx++, modality);
				}

				s.setInt(idx++, count);
				s.setInt(idx, offset);

				logger.debug(s);

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
