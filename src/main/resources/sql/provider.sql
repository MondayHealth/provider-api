SELECT
  pro.id,
  pro.first_name,
  pro.last_name,
  pro.website_url,
  ARRAY(SELECT pc.credential_id
        FROM monday.providers_credentials pc
        WHERE pc.provider_id = pro.id)                            AS credentials,
  ARRAY(SELECT ps.specialty_id
        FROM monday.providers_specialties ps
        WHERE ps.provider_id =
              pro.id)                                             AS specialties,
  ARRAY(SELECT
          addy.formatted || '|' || st_x(addy.point) || '|' || st_y(addy.point)
        FROM monday.providers_addresses pa
          JOIN monday.address addy ON pa.address_id = addy.id
        WHERE pa.provider_id = pro.id AND addy.formatted <> NULL) AS addresses
FROM monday.provider pro