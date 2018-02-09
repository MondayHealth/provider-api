package health.monday.models;

import org.postgresql.util.PGobject;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ProviderDetail
{
	private final long id;

	private final String firstName;

	private final String lastName;

	private final String middleName;

	private final int minFee;

	private final int maxFee;

	private final boolean freeConsultation;

	private final boolean acceptingNewPatients;

	private final boolean slidingScale;

	private final int beganPractice;

	private final String websiteURL;

	private final String school;

	private final String gender;

	private final int yearGraduated;

	private final String[] ageRanges;

	private final String[] ageGroups;

	private final Integer[] credentials;

	private final Integer[] specialties;

	private final Address[] addresses;

	private final Integer[] paymentMethods;

	private final Integer[] languages;

	private final Integer[] modalities;

	private final Integer[] degrees;

	private final Integer[] directories;

	private final Integer[] plans;

	private final String[] orientations;

	private final String[] acceptedPayorComments;

	private final License[] licenses;

	public ProviderDetail(final ResultSet r) throws SQLException
	{
		id = r.getLong("id");
		firstName = r.getString("first_name");
		lastName = r.getString("last_name");
		websiteURL = r.getString("website_url");
		middleName = r.getString("middle_name");
		minFee = r.getInt("minimum_fee");
		maxFee = r.getInt("maximum_fee");
		freeConsultation = r.getBoolean("free_consultation");
		acceptingNewPatients = r.getBoolean("accepting_new_patients");
		slidingScale = r.getBoolean("sliding_scale");
		beganPractice = r.getInt("began_practice");
		school = r.getString("school");
		yearGraduated = r.getInt("year_graduated");
		gender = r.getString("gender");

		Array a = r.getArray("credentials");
		credentials = (Integer[]) a.getArray();

		a = r.getArray("age_groups");
		ageGroups = a != null ? (String[]) a.getArray() : new String[0];

		a = r.getArray("age_ranges");
		if (a != null)
		{
			final Object[] pgo = (Object[]) a.getArray();
			ageRanges = new String[pgo.length];
			for (int i = 0; i < pgo.length; i++)
			{
				ageRanges[i] = ((PGobject) pgo[i]).getValue();
			}
		} else {
			ageRanges = new String[0];
		}

		a = r.getArray("specialties");
		specialties = (Integer[]) a.getArray();

		a = r.getArray("payment_methods");
		paymentMethods = (Integer[]) a.getArray();

		a = r.getArray("modalities");
		modalities = (Integer[]) a.getArray();

		a = r.getArray("languages");
		languages = (Integer[]) a.getArray();

		a = r.getArray("directories");
		directories = (Integer[]) a.getArray();

		a = r.getArray("degrees");
		degrees = (Integer[]) a.getArray();

		a = r.getArray("plans");
		plans = (Integer[]) a.getArray();

		a = r.getArray("orientations");
		orientations = (String[]) a.getArray();

		a = r.getArray("apc");
		acceptedPayorComments = (String[]) a.getArray();

		a = r.getArray("addresses");
		String[] addys = (String[]) a.getArray();
		addresses = new Address[addys.length];
		for (int i = 0; i < addys.length; i++)
		{
			String addy = addys[i];
			if (addy == null || addy.isEmpty())
			{
				continue;
			}
			addresses[i] = new Address(addy);
		}

		final String[] rawLicenses =
				(String[]) r.getArray("licenses").getArray();
		licenses = new License[rawLicenses.length];
		for (int i = 0; i < rawLicenses.length; i++)
		{
			licenses[i] = new License(rawLicenses[i]);
		}
	}
}
