SELECT pa.provider_id
FROM monday.address addy
  JOIN monday.providers_addresses pa ON addy.id = pa.address_id
WHERE st_distance_sphere(addy.point, st_setsrid(st_makepoint(?, ?), 4326)) <= ?