package health.monday.models;

class Address
{
	private final String formatted;

	private final double lat;

	private final double lng;

	private final int directoryID;

	Address(final String raw)
	{
		String[] tokens = raw.split("\\|");
		formatted = tokens[0];
		lng = Double.parseDouble(tokens[1]);
		lat = Double.parseDouble(tokens[2]);
		directoryID = Integer.parseInt(tokens[3]);
	}

	Address(final String formattedName,
			double latitude,
			double longitude,
			int directory)
	{
		formatted = formattedName;
		lat = latitude;
		lng = longitude;
		directoryID = directory;
	}
}
