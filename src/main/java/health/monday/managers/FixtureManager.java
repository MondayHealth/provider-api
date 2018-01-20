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
			"SELECT id, ACRONYM FROM monday.credential";

	private Map<Integer, String> payors = new HashMap<>();

	private Map<Integer, String> credentials = new HashMap<>();

	private FixtureManager()
	{

	}

	public synchronized void reloadFixtures() throws SQLException
	{
		final HashMap<Integer, String> newPayorNames = new HashMap<>();
		final HashMap<Integer, String> newCredNames = new HashMap<>();

		try (final Connection conn = DatabaseManager.connection())
		{
			PreparedStatement s = conn.prepareStatement(PAYOR_NAMES_QUERY);
			ResultSet r = s.executeQuery();
			while (r.next())
			{
				newPayorNames.put(r.getInt(1), r.getString(2));
			}

			s = conn.prepareStatement(CRED_NAMES_QUERY);
			r = s.executeQuery();
			while (r.next())
			{
				newCredNames.put(r.getInt(1), r.getString(2));
			}
		}

		payors = new HashMap<>(newPayorNames);
		credentials = new HashMap<>(newCredNames);

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
		payors = new ConcurrentHashMap<>();
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
}
