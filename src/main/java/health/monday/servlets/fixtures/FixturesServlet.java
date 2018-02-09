package health.monday.servlets.fixtures;

import health.monday.exceptions.InvalidCertificateException;
import health.monday.managers.FixtureManager;
import health.monday.servlets.BaseHTTPServlet;
import health.monday.servlets.BaseServletHandler;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "Fixtures", urlPatterns = {"/fixtures/*"})
public class FixturesServlet extends BaseHTTPServlet
{
	private class Handler extends BaseServletHandler
	{
		Handler(HttpServletRequest req, HttpServletResponse resp)
				throws InvalidCertificateException
		{
			super(req, resp);
		}

		public void get() throws IOException
		{
			final String[] tokens = getPathComponents();
			success(FixtureManager.getInstance().getFixtureByName(tokens[1]));
		}
	}

	@Override
	protected BaseServletHandler getNewHandler(HttpServletRequest request,
											   HttpServletResponse response)
			throws InvalidCertificateException
	{
		return new Handler(request, response);
	}
}
