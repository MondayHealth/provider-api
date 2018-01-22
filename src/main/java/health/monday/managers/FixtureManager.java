package health.monday.managers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FixtureManager
{
	private static final FixtureManager instance = new FixtureManager();

	private static final Logger logger = LogManager.getLogger();

	private static final String PAYOR_NAMES_QUERY =
			"SELECT	id, name FROM monday.payor";

	private static final String CRED_NAMES_QUERY =
			"SELECT id, acronym FROM monday.credential";

	private static final String SPECIALTY_NAMES_QUERY =
			"SELECT id, name FROM monday.specialty";

	private Map<Integer, String> payors = new HashMap<>();

	private Map<Integer, String> credentials = new HashMap<>();

	private Map<Integer, String> specialties = new HashMap<>();

	private FixtureManager()
	{

	}

	public synchronized void reloadFixtures() throws SQLException
	{
		try (final Connection conn = DatabaseManager.connection())
		{
			PreparedStatement s = conn.prepareStatement(PAYOR_NAMES_QUERY);
			ResultSet r = s.executeQuery();
			while (r.next())
			{
				payors.put(r.getInt(1), r.getString(2));
			}

			s = conn.prepareStatement(CRED_NAMES_QUERY);
			r = s.executeQuery();
			while (r.next())
			{
				credentials.put(r.getInt(1), r.getString(2));
			}

			s = conn.prepareStatement(SPECIALTY_NAMES_QUERY);
			r = s.executeQuery();
			while (r.next())
			{
				specialties.put(r.getInt(1), r.getString(2));
			}
		}

		logger.info("Reloaded.");
	}

	public void initialize()
	{
		try
		{
			reloadFixtures();
		}
		catch (SQLException e)
		{
			throw new RuntimeException("Failed: " + e.getMessage());
		}
	}

	public void destroy()
	{
		logger.info("Destroying");
		payors = new HashMap<>();
		credentials = new HashMap<>();
		specialties = new HashMap<>();
	}

	public static FixtureManager getInstance()
	{
		return instance;
	}

	public Map<Integer, String> getPayors()
	{
		return Collections.unmodifiableMap(payors);
	}

	public Map<Integer, String> getCredentials()
	{
		return Collections.unmodifiableMap(credentials);
	}

	public Map<Integer, String> getSpecialties()
	{
		return Collections.unmodifiableMap(specialties);
	}
}
