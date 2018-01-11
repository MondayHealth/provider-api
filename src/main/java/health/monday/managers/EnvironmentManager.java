package health.monday.managers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

public class EnvironmentManager
{
	enum Environment
	{
		LOCAL, PRODUCTION
	}

	private static final EnvironmentManager instance;

	private static final Environment environment;

	private static final Logger logger = LogManager.getLogger();

	private static final String hostName;

	private static Environment detectEnvironment()
	{
		final String ENV_KEY = "ENVIRONMENT";

		Map<String, String> env = System.getenv();

		if (!env.containsKey(ENV_KEY))
		{
			return Environment.PRODUCTION;
		}

		final String raw = env.get(ENV_KEY);

		if (raw == null || raw.isEmpty())
		{
			logger.info("No environment variable found.");
			return Environment.PRODUCTION;
		}

		switch (raw)
		{
			case "local":
				return Environment.LOCAL;
			default:
				logger.warn(String.format("Invalid environment: '%s'", raw));
				return Environment.PRODUCTION;
		}
	}

	private static String detectHostname()
	{
		String name = null;

		try
		{
			name = InetAddress.getLocalHost().getHostName();
		}
		catch (UnknownHostException e)
		{
			logger.error("Could not determine hostname for localhost: " +
					e.getMessage());
		}

		if (name == null)
		{
			return "UNKNOWN";
		}

		return name;
	}

	static
	{
		hostName = detectHostname();
		environment = detectEnvironment();
		logger.info("Detected environment: " + environment);
		instance = new EnvironmentManager();
	}

	private EnvironmentManager()
	{

	}

	public static EnvironmentManager getInstance()
	{
		return instance;
	}

	public final Environment getEnvironment()
	{
		return environment;
	}

	public final String getHostName()
	{
		return hostName;
	}
}
