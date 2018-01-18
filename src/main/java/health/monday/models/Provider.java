package health.monday.models;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Provider
{
	private final long id;

	private final String firstName;

	private final String lastName;

	public Provider(final ResultSet r) throws SQLException
	{
		id = r.getLong("id");
		firstName = r.getString("first_name");
		lastName = r.getString("last_name");
	}
}
