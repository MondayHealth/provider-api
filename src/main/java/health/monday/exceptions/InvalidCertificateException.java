package health.monday.exceptions;

import javax.servlet.ServletException;

public class InvalidCertificateException extends ServletException
{
	public InvalidCertificateException(final String s_dn)
	{
		super("Invalid Certificate: " + s_dn);
	}
}
