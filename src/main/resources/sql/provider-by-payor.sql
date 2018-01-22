SELECT DISTINCT
  pro.id,
  pro.first_name,
  pro.last_name,
  pro.website_url,
  array_agg(pro_creds.credential_id) AS credentials
FROM monday.provider pro
  JOIN monday.providers_credentials pro_creds ON pro.id = pro_creds.provider_id
  JOIN monday.providers_plans pp ON pro.id = pp.provider_id
  INNER JOIN monday.plan plan ON pp.plan_id = plan.id
WHERE plan.payor_id = ?
GROUP BY pro.id
ORDER BY pro.last_name ASC
LIMIT ?
OFFSET ?
