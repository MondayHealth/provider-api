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

	public Provider(final ResultSet r) throws SQLException
	{
		id = r.getLong("id");
		firstName = r.getString("first_name");
		lastName = r.getString("last_name");
		websiteURL = r.getString("website_url");

		final Array a = r.getArray("credentials");

		credentials = (Integer[]) a.getArray();
	}
}
