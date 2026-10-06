# Public demo deployment (single VPS)

The quickest self-hosted demo path is one Linux VPS running the existing Docker Compose stack, with Caddy as the only public web entry point. The backend gateway and PostgreSQL stay bound to loopback; the public frontend proxies API requests inside Docker. Caddy obtains and renews HTTPS certificates when DNS and ports 80/443 are ready.

## Requirements

- A Linux VPS with Docker Engine and Compose plugin, enough memory for PostgreSQL, Kafka, and the Spring services, and a public IP.
- A domain or subdomain with an A record pointed at the VPS.
- Firewall inbound access limited to SSH plus TCP 80/443. Do not expose the Docker daemon, PostgreSQL, Kafka, or service ports publicly.

## Prepare the host

1. Clone/copy this repository onto the VPS.
2. Copy `deploy/public-demo.env.example` to the repository root as `.env`. Replace every `REPLACE_...` value with unique random secrets, set `PUBLIC_DEMO_DOMAIN` and `FRONTEND_ORIGIN` to the real domain, and use a private initial administrator password of 24–72 UTF-8 bytes. Never commit or post `.env`.
3. Ensure DNS resolves to the VPS and ports 80/443 reach it. Caddy's automatic HTTPS requires a real public hostname and external reachability on those ports.
4. Start the separate public-demo Compose project:

```bash
docker compose -f compose.yaml -f compose.public-demo.yaml --profile public-demo --env-file .env up --build -d
```

5. Check startup with `docker compose -f compose.yaml -f compose.public-demo.yaml --profile public-demo --env-file .env ps` and inspect errors using `docker compose -f compose.yaml -f compose.public-demo.yaml --profile public-demo --env-file .env logs -f caddy api-gateway auth-service`.

The public demo uses fictional catalogues and a private, bcrypt-hashed initial administrator account. SQL seed scripts are disabled; the bootstrap account is created only if there is no existing administrator. Keep any created user information fictional and reset the demo database regularly. Online payments and synthetic patient monitoring are disabled by default. This environment still uses Hibernate schema update to create demo databases; it is not the repository's migration-backed production profile and must never contain real patient data.

The VPS hostname/domain, account credentials, data-retention notice, and reset schedule must be supplied by the project owner before launch. Back up or remove the public-demo database deliberately; the volume is separate from the local Compose project.
