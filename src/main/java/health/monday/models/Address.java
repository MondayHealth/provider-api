package health.monday.models;

class Address
{
	private final String formatted;

	private final double lat;

	private final double lng;

	Address(final String formattedName, double latitude, double longitude)
	{
		formatted = formattedName;
		lat = latitude;
		lng = longitude;
	}
}
