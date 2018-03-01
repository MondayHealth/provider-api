package health.monday.models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Plan
{
	public final int id;

	public final int payorId;

	public final String name;

	public final String url;

	private static final String query = "SELECT * FROM monday.plan";

	public static Map<Integer, Plan> loadAll(final Connection connection)
			throws SQLException
	{
		final HashMap<Integer, Plan> ret = new HashMap<>();
		PreparedStatement s = connection.prepareStatement(query);
		ResultSet r = s.executeQuery();
		while (r.next())
		{
			Plan p = new Plan(r);
			ret.put(p.id, p);
		}

		return Collections.unmodifiableMap(ret);
	}

	private Plan(final ResultSet r) throws SQLException
	{
		id = r.getInt("id");
		payorId = r.getInt("payor_id");
		name = r.getString("name");
		url = r.getString("url");
	}
}
