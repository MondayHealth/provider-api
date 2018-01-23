SELECT provider_id
FROM monday.specialty spec
  JOIN monday.providers_specialties ps ON spec.id = ps.specialty_id
WHERE spec.id = ?