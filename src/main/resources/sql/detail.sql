SELECT
  *,
  ARRAY(SELECT payment_method_id
        FROM monday.providers_payment_methods
        WHERE provider_id = pro.id)             AS payment_methods,
  ARRAY(SELECT language_id
        FROM monday.providers_languages
        WHERE provider_id = pro.id)             AS languages,
  ARRAY(SELECT provider_id
        FROM monday.providers_modalities
        WHERE provider_id = pro.id)             AS modalities,
  ARRAY(SELECT degree_id
        FROM monday.providers_degrees
        WHERE provider_id = pro.id)             AS degrees,
  ARRAY(SELECT p_dirs.directory_id
        FROM monday.providers_directories p_dirs
        WHERE p_dirs.provider_id = pro.id)      AS directories,
  ARRAY(SELECT pc.credential_id
        FROM monday.providers_credentials pc
        WHERE pc.provider_id = pro.id)          AS credentials,
  ARRAY(SELECT ps.specialty_id
        FROM monday.providers_specialties ps
        WHERE ps.provider_id = pro.id)          AS specialties,
  ARRAY(SELECT pp.plan_id
        FROM monday.providers_plans pp
        WHERE pp.provider_id = pro.id)          AS plans,
  ARRAY(SELECT body
        FROM monday.providers_orientations p_ori
          JOIN monday.orientation ori ON p_ori.orientation_id = ori.id
        WHERE provider_id = pro.id)             AS orientations,
  ARRAY(SELECT apc.body
        FROM monday.providers_acceptedpayorcomments p_apc
          JOIN monday.acceptedpayorcomment apc
            ON p_apc.acceptedpayorcomment_id = apc.id
        WHERE provider_id = pro.id)             AS apc,
  ARRAY(SELECT
          addy.formatted || '|' || st_x(addy.point) || '|' || st_y(addy.point)
          || '|' || coalesce(addy.directory_id, -1)
        FROM monday.providers_addresses pa
          JOIN monday.address addy ON pa.address_id = addy.id
        WHERE pa.provider_id = pro.id AND addy.formatted IS NOT
                                          NULL) AS addresses,
  ARRAY(SELECT licensor_id || '|' || number ||
               CASE WHEN secondary_number IS NOT NULL
                 THEN '|' || license.secondary_number
               ELSE '|'
               END
        FROM monday.license
        WHERE licensee_id = pro.id)             AS licenses,

  ARRAY(SELECT phone.npa || '-' || phone.nxx || '-' || phone.xxxx ||
               CASE WHEN phone.extension IS NOT NULL
                 THEN ' ext. ' || phone.extension
               ELSE ''
               END
               || '|' ||
               CASE WHEN phone.directory IS NOT NULL
                 THEN phone.directory
               ELSE -1
               END
        FROM monday.providers_phones p_phones
          JOIN monday.phone phone ON p_phones.phone_id = phone.id
        WHERE p_phones.provider_id = pro.id)    AS phones
FROM monday.provider pro
WHERE pro.id = ?
