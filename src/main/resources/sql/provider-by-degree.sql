SELECT provider_id
FROM monday.degree deg
  JOIN monday.providers_degrees ds ON deg.id = ds.degree_id
WHERE deg.id = ?
