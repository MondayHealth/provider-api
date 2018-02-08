package health.monday.models;

class Address
{
	private final String formatted;

	private final double lat;

	private final double lng;

	Address(final String raw)
	{
		String[] tokens = raw.split("\\|");
		formatted = tokens[0];
		lng = Double.parseDouble(tokens[1]);
		lat = Double.parseDouble(tokens[2]);
	}

	Address(final String formattedName, double latitude, double longitude)
	{
		formatted = formattedName;
		lat = latitude;
		lng = longitude;
	}
}
