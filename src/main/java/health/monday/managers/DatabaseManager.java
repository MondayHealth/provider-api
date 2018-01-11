package health.monday.managers;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseManager
{
	private static DatabaseManager instance = new DatabaseManager();

	private static final Logger logger = LogManager.getLogger();

	static private final HikariConfig config;

	private HikariDataSource dataSource = null;

	static
	{
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

		config = new HikariConfig(properties);
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

		dataSource = new HikariDataSource(config);
	}

	public void destroy()
	{
		logger.info("Destroying.");
	}

	private DatabaseManager()
	{
	}

	private HikariDataSource getDataSource()
	{
		return dataSource;
	}

	public static Connection connection() throws SQLException
	{
		return instance.getDataSource().getConnection();
	}
}
