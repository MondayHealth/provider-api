SELECT provider_id
FROM monday.credential cred
  JOIN monday.providers_credentials cs ON cred.id = cs.credential_id
WHERE cred.id = ?
