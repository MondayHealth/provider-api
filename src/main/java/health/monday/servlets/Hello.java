package health.monday.servlets;

import health.monday.exceptions.InvalidCertificateException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;

@WebServlet(name = "HelloServlet", urlPatterns = {"/hello"})
public class Hello extends BaseHTTPServlet
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
			final HashMap<String, String> result = new HashMap<>();
			result.put("name", userName);
			result.put("email", emailAddress);
			success(result);
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
