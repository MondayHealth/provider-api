SELECT pa.provider_id
FROM monday.address
  JOIN monday.providers_addresses pa ON address.id = pa.address_id
WHERE st_distance_sphere(point, st_makepoint(?, ?)) <= ?