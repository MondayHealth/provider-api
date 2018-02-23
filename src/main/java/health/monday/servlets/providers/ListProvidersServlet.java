package health.monday.servlets.providers;

import health.monday.exceptions.InvalidCertificateException;
import health.monday.exceptions.InvalidParameterException;
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

	private static class Response
	{
		@SuppressWarnings("unused")
		private final Provider[] records;

		@SuppressWarnings("unused")
		private final long total;

		Response(final Provider[] rec, final long t)
		{
			records = rec;
			total = t;
		}
	}

	private static class Handler extends BaseServletHandler
	{
		private final int count;

		private final int offset;

		private final int payor;

		private final int specialty;

		private final int modality;

		private final String feeRange;

		private final boolean contact;

		private final int gender;

		private final int language;

		private final Double lat;

		private final Double lng;

		private final Double radius;

		private final String queryConstraints;

		Handler(HttpServletRequest req, HttpServletResponse resp)
				throws InvalidParameterException, InvalidCertificateException
		{
			super(req, resp);

			// Required for all calls
			count = requireInt("count");
			offset = requireInt("offset");

			// Generally optional, but may have semantics
			payor = intParameter("payor", 0);
			specialty = intParameter("specialty", 0);
			modality = intParameter("modality", 0);
			feeRange = stringParameter("feeRange", null);
			contact = boolParameter("contact");
			gender = intParameter("gender", 0);
			language = intParameter("language", 0);
			lat = doubleOrNullParameter("lat");
			lng = doubleOrNullParameter("lng");
			radius = doubleParameter("radius", MILE_IN_METERS);

			checkParameters();

			queryConstraints = getQueryConstraints();
		}

		private String getQueryConstraints()
		{
			String query = "";

			int whereClauses = 0;

			if (payor > 0)
			{
				query += " WHERE pro.id IN (" + providerByPayorQuery + ") ";
				whereClauses += 1;
			}

			if (specialty > 0)
			{
				query += whereClauses > 0 ? " AND " : " WHERE ";
				query += "pro.id IN (" + providerBySpecialtyQuery + ") ";
				whereClauses += 1;
			}

			if (lat != null)
			{
				query += whereClauses > 0 ? " AND " : " WHERE ";
				query += "pro.id IN (" + providerByCoordinate + ") ";
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

			if (contact)
			{
				query += whereClauses > 0 ? " AND " : " WHERE ";
				query += "(pro.id IN ";
				query += "(SELECT provider_id FROM monday.providers_phones) ";
				query += "OR email IS NOT NULL) ";
				whereClauses += 1;
			}

			return query;
		}

		private void checkParameters() throws InvalidParameterException
		{
			if (lat == null ^ lng == null)
			{
				throw new InvalidParameterException("lat/lng", "pairing");
			}

			if (radius > MAX_RADIUS_METERS)
			{
				throw new InvalidParameterException("radius", "too large");
			}

			if (radius < MIN_RADIUS_METERS)
			{
				throw new InvalidParameterException("radius", "too small");
			}

			if (gender > 2 || gender < 0)
			{
				throw new InvalidParameterException("gender", "value: " +
						gender);
			}
		}

		private int setStatementValues(final PreparedStatement s)
				throws SQLException
		{
			int idx = 1;

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

			return idx;
		}

		public void get() throws IOException, SQLException
		{
			final Provider[] result = new Provider[count];

			final String query = providerQuery +
					queryConstraints +
					" ORDER BY pro.last_name ASC LIMIT ? OFFSET ? ";

			final String countQuery =
					"SELECT COUNT(pro.id) FROM monday.provider pro " +
							queryConstraints;

			final long resultCount;

			try (final Connection conn = DatabaseManager.connection())
			{
				PreparedStatement c = conn.prepareStatement(countQuery);
				setStatementValues(c);
				ResultSet r = c.executeQuery();
				if (!r.next())
				{
					throw new SQLException("No count results!");
				}
				resultCount = r.getInt(1);

				PreparedStatement s = conn.prepareStatement(query);
				int idx = setStatementValues(s);
				s.setInt(idx++, count);
				s.setInt(idx, offset);
				logger.debug(s);
				r = s.executeQuery();
				int i = 0;
				while (r.next())
				{
					result[i++] = new Provider(r);
				}
			}

			success(new Response(result, resultCount));
		}
	}

	@Override
	protected BaseServletHandler getNewHandler(HttpServletRequest request,
											   HttpServletResponse response)
			throws ServletException
	{
		return new Handler(request, response);
	}
}
