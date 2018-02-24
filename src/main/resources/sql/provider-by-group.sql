SELECT pg.provider_id
FROM monday.providers_groups pg
  JOIN monday."group" g ON pg.group_id = g.id
WHERE g.tsv @@ ? :: TSQUERY
