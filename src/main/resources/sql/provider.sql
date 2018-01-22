SELECT
  pro.id,
  pro.first_name,
  pro.last_name,
  pro.website_url,
  ARRAY(SELECT pc.credential_id
        FROM monday.providers_credentials pc
        WHERE pc.provider_id = pro.id) AS credentials,
  ARRAY(SELECT ps.specialty_id
        FROM monday.providers_specialties ps
        WHERE ps.provider_id = pro.id) AS specialties
FROM monday.provider pro