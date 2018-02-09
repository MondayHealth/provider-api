SELECT
  pro.id,
  pro.first_name,
  pro.last_name,
  pro.website_url,
  pro.gender,
  pro.minimum_fee,
  pro.maximum_fee,
  pro.sliding_scale,
  pro.accepting_new_patients,
  pro.free_consultation,
  ARRAY(SELECT provider_id
        FROM monday.providers_modalities
        WHERE provider_id = pro.id)             AS modalities,
  ARRAY(SELECT degree_id
        FROM monday.providers_degrees
        WHERE provider_id = pro.id)             AS degrees,
  ARRAY(SELECT pc.credential_id
        FROM monday.providers_credentials pc
        WHERE pc.provider_id = pro.id)          AS credentials,
  ARRAY(SELECT ps.specialty_id
        FROM monday.providers_specialties ps
        WHERE ps.provider_id = pro.id)          AS specialties,
  ARRAY(SELECT body
        FROM monday.providers_orientations p_ori
          JOIN monday.orientation ori ON p_ori.orientation_id = ori.id
        WHERE provider_id = pro.id)             AS orientations,
  ARRAY(SELECT
          addy.formatted || '|' || st_x(addy.point) || '|' || st_y(addy.point)
        FROM monday.providers_addresses pa
          JOIN monday.address addy ON pa.address_id = addy.id
        WHERE pa.provider_id = pro.id AND addy.formatted IS NOT
                                          NULL) AS addresses
FROM monday.provider pro