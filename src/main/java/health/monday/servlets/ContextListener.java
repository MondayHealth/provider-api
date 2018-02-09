package health.monday.servlets;

import health.monday.managers.DatabaseManager;
import health.monday.managers.FixtureManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class ContextListener implements ServletContextListener
{
	private static final Logger logger = LogManager.getLogger();

	@Override
	public void contextInitialized(ServletContextEvent contextEvent)
	{
		DatabaseManager.getInstance().initialize();
		FixtureManager.getInstance().initialize();
	}

	@Override
	public void contextDestroyed(ServletContextEvent contextEvent)
	{
		DatabaseManager.getInstance().destroy();
	}
}
