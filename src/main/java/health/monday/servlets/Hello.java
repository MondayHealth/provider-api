package health.monday.servlets;

import com.google.gson.Gson;
import health.monday.exceptions.InvalidCertificateException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "HelloServlet", urlPatterns = {"/hello"}, loadOnStartup = 1)
public class Hello extends HttpServlet
{
	private String userEmail = null;

	private String userName = null;

	private class Return
	{
		Return(final Map<String, String> res)
		{
			success = true;
			result = Collections.unmodifiableMap(res);
		}

		boolean success;
		Map<String, String> result;
	}

	private void getUser(final HttpServletRequest request)
			throws InvalidCertificateException
	{
		final String cert = request.getHeader("x-ssl-s-dn");
		final String[] tokens = cert.split(",");
		final String[] emailTokens = tokens[0].split("=");
		final String[] commonNameTokens = tokens[1].split("=");

		if (emailTokens.length != 2 || !emailTokens[0].equals("emailAddress"))
		{
			throw new InvalidCertificateException(cert);
		}

		userEmail = emailTokens[1];

		if (commonNameTokens.length != 2 || !commonNameTokens[0].equals("CN"))
		{
			throw new InvalidCertificateException(cert);
		}

		userName = commonNameTokens[1];
	}

	protected void doGet(HttpServletRequest request,
						 HttpServletResponse response)
			throws IOException, ServletException
	{
		getUser(request);
		final Map<String, String> res = new HashMap<>();
		res.put("user", this.userName);
		res.put("email", this.userEmail);
		final Gson g = new Gson();
		response.getWriter().print(g.toJson(new Return(res)));
	}
}
