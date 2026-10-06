# Production readiness

This repository is currently a local demonstration. The `prod` Spring profile adds safer defaults but does not make the system ready for real patient data or clinical use.

## Guardrails now in the repository

- Compose publishes the gateway and PostgreSQL on loopback only.
- Production gateway CORS requires an explicit `FRONTEND_ORIGIN`; service destinations must be supplied through environment variables.
- Every production service requires explicit PostgreSQL URL, username, and password settings, so missing database configuration cannot silently fall back to H2.
- Production gateway omits routes to interactive API documentation.
- Service `prod` profiles disable Swagger/OpenAPI, H2 consoles, SQL seed scripts, demo monitoring/catalogue data, verbose SQL, and detailed server error responses.
- Hibernate uses `validate` instead of mutating the production schema. Database migrations must exist and run before production startup.

## Required before a real deployment

1. Add versioned database migrations for every service and exercise backup/restore on production-like data.
2. Define a secure, one-time first-administrator provisioning procedure; local seed accounts are disabled by production SQL-init settings.
3. Deploy behind HTTPS and a managed ingress; inject unique secrets from a secret manager and rotate local/demo credentials. Restrict databases and service ports to private networks.
4. Add login and API rate limits, account lockout or equivalent abuse controls, session/token revocation retention, and security/audit events for access to patient records.
5. Add automated unit, integration, authorization, and end-to-end tests; add dependency and container vulnerability scanning to CI.
6. Add metrics, health/readiness checks, centralized redacted logs, alerting, recovery procedures, and capacity/load testing.
7. Configure and verify real payment callbacks and patient notification delivery; reconcile billing asynchronously and idempotently. These integrations are not implemented today.
8. Complete a privacy/security review for the deployment jurisdiction, retention and deletion policies, consent, incident response, and staff access procedures.
9. Keep AI functions advisory and disabled for clinical decisions until a real model/data source, validation, human oversight, monitoring, and applicable regulatory review are in place. Current chatbot responses use rules; monitoring is synthetic demo data.

## Activating the profile

Set `SPRING_PROFILES_ACTIVE=prod` for each deployed Spring service. Configure `AUTH_SERVICE_URL`, `CARE_SERVICE_URL`, `BILLING_SERVICE_URL`, `PATIENT_SERVICE_URL`, `PHARMACY_SERVICE_URL`, `AI_SERVICE_URL`, and `FRONTEND_ORIGIN` on the gateway. Supply database connection values and strong service-specific credentials through the deployment platform. Do not use the local `.env.example` values in a deployed environment.
