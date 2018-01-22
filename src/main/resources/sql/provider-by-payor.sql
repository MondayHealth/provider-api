SELECT pp.provider_id
FROM monday.providers_plans pp
  JOIN monday.plan plan ON pp.plan_id = plan.id
WHERE plan.payor_id = ?