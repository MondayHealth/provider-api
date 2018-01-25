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
import java.io.InputStream;
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

	private static final double MILE_IN_METERS = 1609.34;

	private static final double MIN_RADIUS_METERS = 500;

	private static final double MAX_RADIUS_METERS = MILE_IN_METERS * 50;

	private static String convertStreamToString(java.io.InputStream is)
	{
		java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}

	static private String loadFile(final String path)
	{
		try (InputStream r = DatabaseManager.class.getResourceAsStream(path))
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

	static
	{
		providerQuery = loadFile("/sql/provider.sql");
		providerByPayorQuery = loadFile("/sql/provider-by-payor.sql");
		providerBySpecialtyQuery = loadFile("/sql/provider-by-specialty.sql");
		providerByCoordinate = loadFile("/sql/provider-by-coord.sql");
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

			if (payor > 0)
			{
				query += " WHERE pro.id IN (";
				query += providerByPayorQuery;
				query += ") ";
			}

			if (specialty > 0)
			{
				query += payor > 0 ? " AND " : " WHERE ";
				query += "pro.id IN (";
				query += providerBySpecialtyQuery;
				query += ") ";
			}

			if (lat != null)
			{
				query += (payor > 0 || specialty > 0) ? " AND " : " WHERE ";
				query += "pro.id IN (";
				query += providerByCoordinate;
				query += ") ";
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
					s.setDouble(idx++, lat);
					s.setDouble(idx++, lng);
					s.setDouble(idx++, radius);
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
