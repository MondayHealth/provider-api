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

public class FixtureManager
{
	private static final FixtureManager instance = new FixtureManager();

	private static final Logger logger = LogManager.getLogger();

	private static final String CRED_NAMES_QUERY =
			"SELECT id, acronym FROM monday.credential";

	private static final String DEGREES_QUERY =
			"SELECT id, acronym FROM monday.degree";

	private static final String LANGUAGES_QUERY =
			"SELECT id, name FROM monday.language";

	private static final String PAYOR_NAMES_QUERY =
			"SELECT	id, name FROM monday.payor";

	private static final String LICENSOR_NAMES_QUERY =
			"SELECT	id, name FROM monday.licensor";

	private static final String MODALITIES_QUERY =
			"SELECT id, name FROM monday.modality";

	private static final String SPECIALTY_NAMES_QUERY =
			"SELECT id, name FROM monday.specialty";

	private static final String DIRECTORY_NAMES_QUERY =
			"SELECT id, name FROM monday.directory";

	private static final String PLAN_NAMES_QUERY =
			"SELECT id, name FROM monday.plan";

	private static final String PAYMENT_TYPE_QUERY =
			"SELECT id, payment_type FROM monday.payment_method";

	private Map<Integer, String> payors = new HashMap<>();

	private Map<Integer, String> credentials = new HashMap<>();

	private Map<Integer, String> specialties = new HashMap<>();

	private Map<Integer, String> degrees = new HashMap<>();

	private Map<Integer, String> languages = new HashMap<>();

	private Map<Integer, String> modalities = new HashMap<>();

	private Map<Integer, String> paymentTypes = new HashMap<>();

	private Map<Integer, String> directories = new HashMap<>();

	private Map<Integer, String> plans = new HashMap<>();

	private Map<Integer, String> licensors = new HashMap<>();

	private FixtureManager()
	{

	}

	private Map<Integer, String> mapForQuery(final Connection connection,
											 final String query)
			throws SQLException
	{
		final HashMap<Integer, String> ret = new HashMap<>();
		PreparedStatement s = connection.prepareStatement(query);
		ResultSet r = s.executeQuery();
		while (r.next())
		{
			ret.put(r.getInt(1), r.getString(2));
		}

		return Collections.unmodifiableMap(ret);
	}

	private synchronized void reloadFixtures() throws SQLException
	{
		try (final Connection conn = DatabaseManager.connection())
		{
			payors = mapForQuery(conn, PAYOR_NAMES_QUERY);
			credentials = mapForQuery(conn, CRED_NAMES_QUERY);
			degrees = mapForQuery(conn, DEGREES_QUERY);
			languages = mapForQuery(conn, LANGUAGES_QUERY);
			modalities = mapForQuery(conn, MODALITIES_QUERY);
			specialties = mapForQuery(conn, SPECIALTY_NAMES_QUERY);
			paymentTypes = mapForQuery(conn, PAYMENT_TYPE_QUERY);
			directories = mapForQuery(conn, DIRECTORY_NAMES_QUERY);
			plans = mapForQuery(conn, PLAN_NAMES_QUERY);
			licensors = mapForQuery(conn, LICENSOR_NAMES_QUERY);
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

	public static FixtureManager getInstance()
	{
		return instance;
	}

	@SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType")
	public Map<Integer, String> getFixtureByName(final String name)
	{
		switch (name.toLowerCase())
		{
			case "credentials":
				return credentials;
			case "payors":
				return payors;
			case "paymenttypes":
				return paymentTypes;
			case "modalities":
				return modalities;
			case "languages":
				return languages;
			case "degrees":
				return degrees;
			case "specialties":
				return specialties;
			case "directories":
				return directories;
			case "plans":
				return plans;
			case "licensors":
				return licensors;
			default:
				throw new IllegalArgumentException("Unknown fixture: " + name);
		}
	}
}
