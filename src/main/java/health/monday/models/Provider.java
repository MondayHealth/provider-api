package health.monday.models;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Provider
{
	private final long id;

	private final String firstName;

	private final String lastName;

	private final String websiteURL;

	private final Integer[] credentials;

	private final Integer[] specialties;

	private final Address[] addresses;

	public Provider(final ResultSet r) throws SQLException
	{
		id = r.getLong("id");
		firstName = r.getString("first_name");
		lastName = r.getString("last_name");
		websiteURL = r.getString("website_url");

		Array a = r.getArray("credentials");
		credentials = (Integer[]) a.getArray();

		a = r.getArray("specialties");
		specialties = (Integer[]) a.getArray();

		a = r.getArray("addresses");
		String[] addys = (String[]) a.getArray();
		addresses = new Address[addys.length];
		int idx = 0;
		for (final String addy : addys)
		{
			if (addy == null || addy.isEmpty())
			{
				continue;
			}

			String[] tokens = addy.split("\\|");
			final double lng = Double.parseDouble(tokens[1]);
			final double lat = Double.parseDouble(tokens[2]);
			addresses[idx++] = new Address(tokens[0], lat, lng);
		}
	}
}
