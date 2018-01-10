package health.monday.managers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseManager
{
	@org.jetbrains.annotations.Nullable
	private static DatabaseManager instance;

	private static final Logger logger = LogManager.getLogger();

	private final Properties properties;

	private final String url;

	public static DatabaseManager getInstance()
	{
		return instance;
	}

	public static void initialize()
	{
		if (instance != null)
		{
			throw new IllegalStateException("Attempt to double initialize.");
		}

		logger.info("Initializing.");

		try
		{
			Class.forName("org.postgresql.Driver");
		}
		catch (ClassNotFoundException e)
		{
			throw new RuntimeException("Could not load the psql driver.");
		}

		final EnvironmentManager em = EnvironmentManager.getInstance();
		final Properties properties = new Properties();
		final String fileName =
				String.format("/properties/%s.psql.properties", em
						.getEnvironment()
						.toString()
						.toLowerCase());

		try
		{
			properties.load(DatabaseManager.class.getResourceAsStream
					(fileName));
		}
		catch (IOException e)
		{
			throw new RuntimeException(String.format("ioe on %s : %s ",
					fileName, e
					.getMessage()));
		}

		final DatabaseManager inst = new DatabaseManager(properties);

		instance = inst;
	}

	public static void destroy()
	{
		logger.info("Destroying.");
		instance = null;
	}

	private DatabaseManager(final Properties props)
	{
		properties = props;
		url = properties.getProperty("url");
	}

	public Connection newConnection() throws SQLException
	{
		final Connection ret = DriverManager.getConnection(url, properties);
		return ret;
	}



}
