# API and UI integration map

The browser calls the API Gateway on port `4004`. It does not call internal service ports. Gateway JWT validation asks auth-service to introspect the token and replaces any client-supplied identity headers with verified identity claims.

## Implemented user flows

| Flow | Gateway endpoints | Service | Access |
|---|---|---|---|
| Register/login/profile/logout | `/auth/register`, `/auth/login`, `/auth/me`, `/auth/logout` | auth-service | Register/login public; profile/logout token protected |
| Public doctor directory | `GET /api/care/doctors`, `/api/care/doctors/{id}`, `/api/care/doctors/availability/upcoming` | care-service | Public GET; upcoming endpoint returns open future slots (up to 14 days) |
| Appointments | `/api/care/appointments*` | care-service | Patient-owned create/list/cancel; admin list |
| Lab catalogue/bookings/results | `/api/care/lab-tests*`, `/api/care/lab-bookings*` | care-service | Public catalogue GET; booking patient-owned; results/admin listing role protected |
| Patient account and records | `/api/patients/me*`, `/api/patients/medical-history`, `/api/patients/notifications` | patient-service | Own profile, history, notifications; admin list/management |
| Pharmacy inventory/prescriptions/orders | `/api/pharmacy/**` | pharmacy-service | Catalogue GET; prescriptions patient scoped; orders owned or staff managed |
| Invoices | `/api/billing/invoices*`, `/api/billing/payment-options` | billing-service | Patient-owned invoice list/detail/download; staff operations restricted |
| AI admin overview | `/api/ai/**` | ai-service | Admin/SUPER_ADMIN role required |
| Staff accounts | `/api/admin/staff` | auth-service | Admin creates department logins; passwords are hashed and roles are assigned by the server |
| Department workspaces | `/api/pharmacy/**`, `/api/care/**` | pharmacy-service, care-service | PHARMACIST, PHARMACY_REVIEWER, LAB_TECH, LAB_REVIEWER, DOCTOR, and DOCTOR_REVIEWER role checks |

## Operational configuration

- Appointment, lab, and pharmacy order creation request an invoice from billing-service. Set the same strong `BILLING_INTERNAL_SERVICE_TOKEN` in billing-service, care-service, and pharmacy-service. Infrastructure now passes this optional environment variable to all three. If it is unset or mismatched, those workflows return an error when invoice creation is attempted.
- Billing has persistent REST invoices and PDF download. `GET /api/billing/payment-options` reports whether an online provider is enabled. No real payment checkout/callback is implemented until a provider and credentials are configured. The UI therefore disables online payment when the provider reports disabled; invoice creation must not be treated as payment settlement.
- Care service uses H2 by default for local development and PostgreSQL when configured. Production deployment needs its DB environment values and migrations/data setup.
- Gateway CORS uses `FRONTEND_ORIGIN`; set it to the deployed frontend origin.

## Known integration limits

- Invoice creation calls billing synchronously from care/pharmacy service transactions. It is not a distributed transaction; production should add retries/outbox reconciliation before relying on it for financial settlement.
- Pharmacy orders have an `AWAITING_PAYMENT` state, but with payment disabled they remain pending. Do not mark an order paid manually without a verified provider callback or controlled staff workflow.
- Medical-history and notifications support patient reads and administrator writes; care and pharmacy events are not yet automatically published as patient notifications.
- Pharmacy now publishes inventory changes to the `pharmacy-inventory-events` Kafka topic. The AI service records a Pharmacy Department low-stock alert when an item crosses its configured reorder point or becomes out of stock. This is a transparent threshold rule, not a trained forecast model; training requires retained demand history and labeled stockout/replenishment outcomes.
- Local demo monitoring is available through admin-only `GET /api/ai/demo-monitoring`. With `AI_DEMO_MONITORING_ENABLED=true`, the AI service generates synthetic readings every five seconds and a synthetic staff alert after about 30 seconds. It is in-memory demonstration data only: not connected to medical equipment, not clinically validated, and not delivered by SMS/push. It does not detect real patient deterioration and must not be used for patient care. Disable it outside a demo with `AI_DEMO_MONITORING_ENABLED=false`.
- The admin dashboard surfaces operational APIs and requires an `ADMIN` or `SUPER_ADMIN` identity. Seeded/admin credentials should be rotated for a real deployment.
- Local Compose seeds five fictional doctors across cardiology, dermatology, pediatrics, neurology, and orthopedics, with sample slots for the next two days when `DEMO_DOCTORS_ENABLED=true`. They are marked in their profile bios as fictional demo data and are never added by default outside Compose.
- Local Compose seeds five fictional lab tests across hematology, biochemistry, endocrinology, and pathology when `DEMO_LAB_TESTS_ENABLED=true`. Their prices and fasting instructions are sample display data. Availability is generated in 30-minute increments from 08:00 to 16:30 and excludes booked slots. These entries are for UI/API demonstration only, not real lab services or clinical use; disable with `DEMO_LAB_TESTS_ENABLED=false`.
- Department account creation is administrator-only. New medicine/test submissions remain out of public catalogues until a different staff account with the matching reviewer role approves them. Lab result summaries and manual patient-to-doctor assignments also require a different staff account to review. Doctors are linked to a doctor profile and see that profile's appointments plus approved manual assignments. Role checks are enforced in the service APIs; hiding a screen is not the security boundary.
