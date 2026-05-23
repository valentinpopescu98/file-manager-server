# file-manager-server

Spring Boot backend for a cloud file manager deployed on AWS. Handles authentication, file storage on S3, metadata persistence on RDS, and email notifications via SES. Containerized with Docker and deployed on EC2.

**Companion project:** [file-manager-client](https://github.com/valentinpopescu98/file-manager-client) — React frontend

---

## Features

### Authentication
- **JWT authentication** — stateless, 1-hour token expiry, roles embedded in claims
- **OAuth2 / Google login** — Spring Security OAuth2 client with custom success handler that issues a JWT on redirect
- **BCrypt password hashing**
- **Role-based access control** — `USER` role for standard endpoints, `ADMIN` role for publisher email verification

### File Management
- **Upload** — async upload via `CompletableFuture` on a dedicated thread pool; returns an `uploadId` immediately so the client can poll for status
- **Upload status polling** — `ConcurrentHashMap<uploadId, UploadStatus>` (PROCESSING / DONE / ERROR) for non-blocking status checks
- **Download** — streamed response via `StreamingResponseBody` (8KB buffer) to avoid loading large files into memory
- **Delete** — removes from S3 and RDS atomically
- **List** — paginated, sortable, filterable file listing using JPA `Specification` (filter by name, description, uploader email, date range)

### AWS Integration
- **S3** — upload, download, delete, presigned URL generation (1-hour expiry)
- **SES** — async email notifications on upload, download, and delete events via `@Async`
- **RDS (PostgreSQL)** — file metadata persistence (name, description, uploader email, S3 key, URL, timestamp)

### Infrastructure
- **Docker** — multi-stage build (Maven build → Alpine JRE runtime image)
- **Docker Compose** — single service with external network, log volume mount, prod profile
- **Spring profiles** — separate `dev` / `prod` configs for IP, logging, and DB connection
- **`build-and-run.sh`** — SSH into EC2, clone repo, build and start container
- **`stop-and-cleanup.sh`** — stop and remove container + image

---

## API

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/login` | Public | Email/password login, returns JWT |
| POST | `/api/register` | Public | Register new user |
| GET | `/oauth2/authorization/google` | Public | Initiate Google OAuth2 login |
| GET | `/api` | JWT | List files (paginated, filtered, sorted) |
| GET | `/api/download?s3Key=` | JWT | Stream file download |
| POST | `/api/upload` | JWT | Start async file upload, returns uploadId |
| GET | `/api/upload/status?uploadId=` | JWT | Poll upload status |
| DELETE | `/api/delete?s3Key=` | JWT | Delete file from S3 and DB |
| POST | `/api/verify-email` | ADMIN | Verify SES publisher email |

### List files query params

| Param | Default | Description |
|---|---|---|
| `page` | 1 | Page number |
| `limit` | 20 | Items per page |
| `sortBy` | `name` | Column to sort by |
| `sortOrder` | `asc` | `asc` or `desc` |
| `filterName` | — | Partial match on file name |
| `filterDescription` | — | Partial match on description |
| `filterUploaderEmail` | — | Partial match on uploader email |
| `filterUploadedAtBefore` | — | ISO date upper bound |
| `filterUploadedAtAfter` | — | ISO date lower bound |

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.4 |
| Security | Spring Security, JWT (jjwt), OAuth2 |
| ORM | Spring Data JPA / Hibernate |
| Database | PostgreSQL (AWS RDS) |
| Storage | AWS S3 (SDK v2) |
| Email | AWS SES (SDK v2) |
| Async | `CompletableFuture`, `@Async`, Spring `AsyncConfig` |
| Container | Docker, Docker Compose |
| Server | EC2 (Ubuntu) |

---

## How to run

1. Create RDS PostgreSQL database + S3 bucket + EC2 instance; have a GitHub private key ready
2. `scp -i ~/.ssh/file-manager-key.pem ~/.ssh/id_rsa user@host:~/.ssh/`
3. `ssh -i ~/.ssh/file-manager-key.pem user@host`
4. `chmod 600 ~/.ssh/id_rsa`
5. `git clone git@github.com:valentinpopescu98/file-manager-server.git ~/file-manager-server/`
6. `~/file-manager-server/build-and-run.sh`

---
- `file-manager-key.pem` = EC2 private key (`~/.ssh/`)
- `id_rsa` = GitHub private key (`~/.ssh/`)
- `user` = EC2 user (e.g. `ubuntu`)
- `host` = EC2 instance public IP
