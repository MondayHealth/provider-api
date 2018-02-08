package health.monday.models;

public class License
{
	private final int licensor;

	private final int number;

	private final int secondaryNumber;

	License(final String raw)
	{
		final String[] tokens = raw.split("\\|");
		licensor = Integer.parseInt(tokens[0]);
		number = Integer.parseInt(tokens[1]);
		secondaryNumber = tokens.length > 2 ? Integer.parseInt(tokens[2]) : -1;
	}
}
