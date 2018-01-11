package health.monday.managers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

	static
	{
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
}
