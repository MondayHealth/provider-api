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
import java.util.Arrays;
import java.util.Calendar;
import java.util.stream.Collectors;

@WebServlet(name = "ListProviders", urlPatterns = {"/providers/list"})
public class ListProvidersServlet extends BaseHTTPServlet
{
	private static final Logger logger = LogManager.getLogger();

	private static final String providerQuery;

	private static final String providerByNameQuery;

	private static final String providerByPayorQuery;

	private static final String providerByPlanQuery;

	private static final String providerBySpecialtyQuery;

	private static final String providerByDegreeQuery;

	private static final String providerByCredentialQuery;

	private static final String providerByCoordinate;

	private static final String providerByLanguage;

	private static final String providerByModality;

	private static final String providerByGroup;

	private static final String providerByOrientation;

	private static final double MILE_IN_METERS = 1609.34;

	private static final double MIN_RADIUS_METERS = 500;

	private static final double MAX_RADIUS_METERS = MILE_IN_METERS * 50;

	static
	{
		providerQuery = loadQuery("provider");
		providerByNameQuery = loadQuery("provider-by-name");
		providerByPayorQuery = loadQuery("provider-by-payor");
		providerByPlanQuery = loadQuery("provider-by-plan");
		providerBySpecialtyQuery = loadQuery("provider-by-specialty");
		providerByDegreeQuery = loadQuery("provider-by-degree");
		providerByCredentialQuery = loadQuery("provider-by-credential");
		providerByCoordinate = loadQuery("provider-by-coord");
		providerByLanguage = loadQuery("provider-by-language");
		providerByModality = loadQuery("provider-by-modality");
		providerByGroup = loadQuery("provider-by-group");
		providerByOrientation = loadQuery("provider-by-orientation");
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

		private final int plan;

		private final Integer[] specialties;

		private final Integer[] degrees;

		private final Integer[] credentials;

		private final int modality;

		private final String feeRange;

		private final boolean contact;

		private final boolean freeConsult;

		private final boolean hasWebsite;

		private final int gender;

		private final int language;

		private final Double lat;

		private final Double lng;

		private final Double radius;

		private final int practiceAge;

		private final String[] keywords;

		private final String queryConstraints;

		private final String name;

		private int whereClauses = 0;

		Handler(HttpServletRequest req, HttpServletResponse resp)
				throws InvalidParameterException, InvalidCertificateException
		{
			super(req, resp);

			// Required for all calls
			count = requireInt("count");
			offset = requireInt("offset");

			// Generally optional, but may have semantics
			payor = intParameter("payor", 0);
			plan = intParameter("plan", 0);
			modality = intParameter("modality", 0);
			feeRange = stringParameter("feeRange", null);
			contact = boolParameter("contact");
			hasWebsite = boolParameter("website");
			freeConsult = boolParameter("freeConsult");
			gender = intParameter("gender", 0);
			language = intParameter("language", 0);
			lat = doubleOrNullParameter("lat");
			lng = doubleOrNullParameter("lng");
			radius = doubleParameter("radius", MILE_IN_METERS);
			practiceAge = intParameter("practiceAge", 0);
			name = stringParameter("name", null);

			String raw = stringParameter("keywords", null);
			keywords = raw != null ? raw.split(" ") : new String[0];

			specialties = getMultiParameter("specialties");
			degrees = getMultiParameter("degrees");
			credentials = getMultiParameter("credentials");

			checkParameters();

			queryConstraints = getQueryConstraints();
		}

		private Integer[] getMultiParameter(final String name)
		{
			final String raw = stringParameter(name, null);

			final Integer[] ret;

			if (raw == null)
			{
				ret = new Integer[0];
			}
			else
			{
				final String[] raws = raw.split(",");
				ret = new Integer[raws.length];
				for (int i = 0; i < raws.length; i++)
				{
					if (!raws[i].isEmpty())
					{
						ret[i] = Integer.parseInt(raws[i]);
					}
				}
			}

			return ret;
		}

		private String addIntListQuery(final Integer[] list,
									   final String singleElementQuery,
									   final String singular,
									   final String plural)
		{
			String query = "";

			if (list.length == 1)
			{
				query += whereClauses > 0 ? " AND " : " WHERE ";
				query += "pro.id IN (" + singleElementQuery + ") ";
				whereClauses += 1;
			}
			else if (list.length > 0)
			{
				query += whereClauses > 0 ? " AND " : " WHERE ";
				query += "pro.id IN ( SELECT provider_id FROM " +
						"monday.providers_" +
						plural +
						"WHERE " +
						singular +
						"_id IN (";

				query += Arrays.stream(list)
						.map(Object::toString)
						.collect(Collectors.joining(","));

				query += ") GROUP BY provider_id HAVING count(provider_id) =" +
						list.length +
						") ";

				whereClauses += 1;
			}

			return query;
		}

		private String getQueryConstraints()
		{
			String query = "";

			if (payor > 0)
			{
				query += " WHERE pro.id IN (" + providerByPayorQuery + ") ";
				whereClauses += 1;
			}

			query +=
					addIntListQuery(specialties, providerBySpecialtyQuery,
							"specialty", "specialties");

			query +=
					addIntListQuery(degrees, providerByDegreeQuery, "degree",
							"degrees");

			query +=
					addIntListQuery(credentials, providerByCredentialQuery,
							"credential", "credentials");

			if (lat != null)
			{
				query += whereClauses > 0 ? " AND " : " WHERE ";
				query += "pro.id IN (" + providerByCoordinate + ") ";
				whereClauses += 1;
			}

			if (feeRange != null)
			{
				query += whereClauses > 0 ? " AND " : " WHERE ";
				query += "pro.minimum_fee <= ? and pro.maximum_fee <= ? ";
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

			if (freeConsult)
			{
				query += whereClauses > 0 ? " AND " : " WHERE ";
				query += "free_consultation = true ";
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

			if (practiceAge > 0)
			{
				final int year = Calendar.getInstance().get(Calendar.YEAR);
				final int targetYear = year - practiceAge;
				query += whereClauses > 0 ? " AND " : " WHERE ";
				query += "began_practice <= " + targetYear;
				whereClauses += 1;
			}

			if (plan > 0)
			{
				query += whereClauses > 0 ? " AND " : " WHERE ";
				query += "pro.id IN (" + providerByPlanQuery + ") ";
				whereClauses += 1;
			}

			if (hasWebsite)
			{
				query += whereClauses > 0 ? " AND " : " WHERE ";
				query += "website_url IS NOT NULL ";
				whereClauses += 1;
			}

			if (name != null)
			{
				query += whereClauses > 0 ? " AND " : " WHERE ";
				query += "first_name ILIKE ? OR last_name ILIKE ? ";
				whereClauses += 1;
			}

			if (keywords.length > 0)
			{
				query += whereClauses > 0 ? " AND " : " WHERE ";
				query += "pro.id IN (" + providerByGroup + ") ";
				query += "OR pro.id IN (" + providerByOrientation + ") ";
				//noinspection UnusedAssignment
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

			if (plan > 0 && payor > 0)
			{
				throw new IllegalStateException("Cant have plan and payor.");
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

			if (practiceAge < 0 || practiceAge > 999)
			{
				throw new InvalidParameterException("practiceAge", "value:" +
						practiceAge);
			}

			for (final Integer specialty : specialties)
			{
				if (specialty < 1 || specialty > 9999)
				{
					throw new InvalidParameterException("specialty",
							"invalid int: " +
									specialty);
				}
			}
		}

		private int setStatementValues(final PreparedStatement s, int start)
				throws SQLException
		{
			int idx = start;

			if (payor > 0)
			{
				s.setInt(idx++, payor);
			}

			if (specialties.length == 1)
			{
				s.setInt(idx++, specialties[0]);
			}

			if (degrees.length == 1)
			{
				s.setInt(idx++, degrees[0]);
			}

			if (credentials.length == 1)
			{
				s.setInt(idx++, credentials[0]);
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

			if (plan > 0)
			{
				s.setInt(idx++, plan);
			}

			if (name != null)
			{
				final String fuzzy = "%" + name + "%";
				s.setString(idx++, fuzzy);
				s.setString(idx++, fuzzy);
			}

			if (keywords.length > 0)
			{
				final String tsquery = String.join(":* & ", keywords) + ":*";
				s.setString(idx++, tsquery);
				s.setString(idx++, tsquery);
			}

			return idx;
		}

		private int getCount(final Connection conn) throws SQLException
		{
			final String query =
					"SELECT COUNT(pro.id) FROM monday.provider pro " +
							queryConstraints;

			final PreparedStatement c = conn.prepareStatement(query);
			setStatementValues(c, 1);

			ResultSet r = c.executeQuery();
			if (!r.next())
			{
				throw new SQLException("No count results!");
			}
			return r.getInt(1);
		}

		private Provider[] getProviders(final Connection conn,
										final long resultCount)
				throws SQLException
		{
			final String query;

			//noinspection IfMayBeConditional
			if (name == null)
			{
				query = providerQuery +
						queryConstraints +
						" ORDER BY pro.last_name ASC LIMIT ? OFFSET ? ";
			}
			else
			{
				query = providerByNameQuery +
						queryConstraints +
						" ORDER BY lsim DESC, fsim DESC, pro.last_name ASC " +
						"LIMIT ? OFFSET ?";
			}

			final PreparedStatement s = conn.prepareStatement(query);

			int idx = 1;

			if (name != null)
			{
				s.setString(idx++, name);
				s.setString(idx++, name);
			}

			idx = setStatementValues(s, idx);

			s.setInt(idx++, count);
			s.setInt(idx, offset);

			logger.debug(s);

			final long computed = Math.min(resultCount, count);
			final Provider[] result = new Provider[Math.toIntExact(computed)];

			ResultSet r = s.executeQuery();

			int i = 0;
			while (r.next())
			{
				result[i++] = new Provider(r);
			}

			return result;
		}

		public void get() throws IOException, SQLException
		{
			final Provider[] result;

			final long resultCount;

			try (final Connection conn = DatabaseManager.connection())
			{
				resultCount = getCount(conn);
				result = getProviders(conn, resultCount);
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
