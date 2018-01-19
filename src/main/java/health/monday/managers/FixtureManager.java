package health.monday.managers;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FixtureManager
{
	private static final FixtureManager instance = new FixtureManager();

	private static final Logger logger = LogManager.getLogger();

	private static final String PAYOR_NAMES_QUERY =
			"SELECT	id, name FROM monday.payor";

	private ConcurrentHashMap<Integer, String> payors =
			new ConcurrentHashMap<>();

	private FixtureManager()
	{

	}

	public synchronized void reloadFixtures() throws SQLException
	{
		final HashMap<Integer, String> newPayorNames =
				new HashMap<>();

		try (final Connection conn = DatabaseManager.connection())
		{
			PreparedStatement s = conn.prepareStatement(PAYOR_NAMES_QUERY);
			ResultSet r = s.executeQuery();
			while (r.next())
			{
				newPayorNames.put(r.getInt(1), r.getString(2));
			}
		}

		payors = new ConcurrentHashMap<>(newPayorNames);

		logger.info("Reloaded.");
	}

	public void initialize()
	{
		try {
			reloadFixtures();
		} catch (SQLException e) {
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
		return payors;
	}
}
