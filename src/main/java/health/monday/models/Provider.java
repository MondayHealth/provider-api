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

	private final String gender;

	private final int minFee;

	private final int maxFee;

	private final boolean freeConsultation;

	private final boolean acceptingNewPatients;

	private final boolean slidingScale;

	private final Integer[] modalities;

	private final Integer[] degrees;

	private final Integer[] credentials;

	private final Integer[] specialties;

	private final Address[] addresses;

	private final String[] orientations;

	public Provider(final ResultSet r) throws SQLException
	{
		id = r.getLong("id");
		firstName = r.getString("first_name");
		lastName = r.getString("last_name");
		websiteURL = r.getString("website_url");
		minFee = r.getInt("minimum_fee");
		maxFee = r.getInt("maximum_fee");
		freeConsultation = r.getBoolean("free_consultation");
		acceptingNewPatients = r.getBoolean("accepting_new_patients");
		slidingScale = r.getBoolean("sliding_scale");
		gender = r.getString("gender");

		Array a = r.getArray("credentials");
		credentials = (Integer[]) a.getArray();

		a = r.getArray("modalities");
		modalities = (Integer[]) a.getArray();
		a = r.getArray("specialties");
		specialties = (Integer[]) a.getArray();
		a = r.getArray("orientations");
		orientations = (String[]) a.getArray();
		a = r.getArray("degrees");
		degrees = (Integer[]) a.getArray();

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

			addresses[idx++] = new Address(addy);
		}
	}
}
