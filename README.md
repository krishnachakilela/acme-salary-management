# ACME Employee Salary Management

Local HR system for searching, updating, and analyzing compensation records for ~10,000 ACME employees without spreadsheet sprawl.

## Context

- Version: 1.0.0
- Stack: Java 21, Spring Boot 3.3, PostgreSQL 16, Angular 20, Docker Compose

## Prerequisites

- Docker Desktop 4+ (Compose v2)
- Optional local tooling: JDK 21, Maven 3.9+, Node.js 20+

## Quick Start

```bash
cp .env.example .env
docker compose up --build
```

Open http://localhost:9080 and sign in with credentials from `.env` (`SEED_HR_EMAIL` / `SEED_HR_PASSWORD`).

API health: http://localhost:8081/api/v1/health

## Architecture

```mermaid
flowchart LR
  Browser --> UI[nginx Angular]
  UI -->|/api proxy| API[Spring Boot]
  API --> DB[(PostgreSQL)]
```

Layers (DDD): Presentation → Application → Domain → Infrastructure.

## Configuration

Copy `.env.example` to `.env`. Do not commit secrets. Key variables:

- `DB_PASSWORD` — PostgreSQL password
- `JWT_SECRET` — HS256 signing secret (min 32 chars)
- `SEED_ON_START` — set by Compose to seed 10k employees
- `SEED_HR_EMAIL` / `SEED_HR_PASSWORD` — demo HR login

## Local UI (optional)

With the API already running on port 8081:

```bash
cd frontend && npm install && npm start
```

Open http://localhost:4200. `proxy.conf.json` forwards `/api` to `http://127.0.0.1:8081`.

## Common Issues

- Port 9080 or 8081 already bound: stop the conflicting process or change ports in `docker-compose.yml`.
- First boot is slow while Maven/npm build and seed run; wait until API logs show seed completion.
- Login fails after rebuild with a fresh volume: confirm `.env` password matches the seeded user.
- If something else already uses host port 8080, this Compose stack serves the UI on **9080**.
- Local `ng serve` login 404 on `/api/...`: ensure the API is on 8081 and restart `npm start` so `proxy.conf.json` is loaded.

## Smoke check

```bash
./scripts/smoke.sh
```


Sample Screens

Login Screen 

<img width="3456" height="1452" alt="image" src="https://github.com/user-attachments/assets/01b6e5cd-6818-4ee7-aad4-222849e8642f" />

Employee List 

<img width="3456" height="1878" alt="image" src="https://github.com/user-attachments/assets/5117e2de-9e09-49c8-969e-e0768499914e" />

Add Employee Screen with validation 

<img width="3456" height="1654" alt="image" src="https://github.com/user-attachments/assets/67f09ad3-1bdd-4ca5-a649-158d38a97afb" />

Save Employee 

<img width="3430" height="1352" alt="image" src="https://github.com/user-attachments/assets/37909dc4-6ebe-44f5-80da-823591869a7c" />

Add Salary Range

<img width="3434" height="1282" alt="image" src="https://github.com/user-attachments/assets/a2693522-4674-41b2-863e-1544125b6e6f" />

Mark employee as IN-Active 

<img width="3440" height="1500" alt="image" src="https://github.com/user-attachments/assets/e7d1a609-cb8b-4a25-986d-fdb5a9de4891" />

IN-ACTIVE Employee screen where salary addition is not allowed 

<img width="3456" height="1446" alt="image" src="https://github.com/user-attachments/assets/21441b3e-5006-41b7-a847-8f33a0dd026a" />

Re-Active employee screen 

<img width="3456" height="1720" alt="image" src="https://github.com/user-attachments/assets/548910f1-3723-4091-9904-eaeaa0140478" />

Analytics Dashboard by currency , country & department

<img width="3442" height="1924" alt="image" src="https://github.com/user-attachments/assets/f15bdfc8-1e98-4ee7-84f7-243e2e63d69f" />

Salary Distribution bands 

<img width="2982" height="598" alt="image" src="https://github.com/user-attachments/assets/b7b51429-6f5e-453d-b3b3-c4703b22551a" />









