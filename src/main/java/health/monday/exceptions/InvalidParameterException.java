package health.monday.exceptions;

import javax.servlet.ServletException;

public class InvalidParameterException extends ServletException
{
	public InvalidParameterException(final String paramName, final String reason)
	{
		super("Parameter '" + paramName + "' invalid: " + reason);
	}
}
