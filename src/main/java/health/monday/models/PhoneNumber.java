package health.monday.models;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class PhoneNumber
{
	final String number;

	final int directory;

	@NotNull
	private static PhoneNumber mapper(final String raw)
	{
		final String[] tokens = raw.split("\\|");
		return new PhoneNumber(tokens[0], Integer.parseInt(tokens[1]));
	}

	public static PhoneNumber[] getNumbers(final String[] rawResults)
	{
		return Arrays.stream(rawResults)
				.map(PhoneNumber::mapper)
				.toArray(PhoneNumber[]::new);
	}

	private PhoneNumber(final String num, final int directoryID)
	{
		number = num;
		directory = directoryID;
	}
}
