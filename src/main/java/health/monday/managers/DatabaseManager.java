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
	private static DatabaseManager instance = new DatabaseManager();

	private static final Logger logger = LogManager.getLogger();

	static private final Properties properties;

	static private final String url;

	static
	{
		final EnvironmentManager em = EnvironmentManager.getInstance();
		properties = new Properties();
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

		url = properties.getProperty("url");
	}

	public static DatabaseManager getInstance()
	{
		return instance;
	}

	public void initialize()
	{
		logger.info("Initializing.");

		try
		{
			Class.forName("org.postgresql.Driver");
		}
		catch (ClassNotFoundException e)
		{
			throw new RuntimeException("Could not load the psql driver.");
		}
	}

	public void destroy()
	{
		logger.info("Destroying.");
	}

	private DatabaseManager()
	{
	}

	public Connection newConnection() throws SQLException
	{
		return DriverManager.getConnection(url, properties);
	}
}
