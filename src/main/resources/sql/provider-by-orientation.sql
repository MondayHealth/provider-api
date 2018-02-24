SELECT po.provider_id
FROM monday.providers_orientations po
  JOIN monday.orientation o ON po.orientation_id = o.id
WHERE o.tsv @@ ? :: TSQUERY
