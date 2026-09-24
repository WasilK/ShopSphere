# ShopSphere Architecture

## High-Level Architecture

```mermaid
flowchart TB
    Client[Client / Swagger UI / Frontend]

    subgraph Render["Production — Render"]
        Security[Spring Security<br/>JWT Authentication + RBAC]
        Controllers[REST Controllers]
        Services[Service Layer]
        Repositories[Spring Data JPA]
    end

    DB[(Supabase PostgreSQL)]
    Redis[(Upstash Redis)]
    Cloudinary[Cloudinary]

    Client --> Security
    Security --> Controllers
    Controllers --> Services
    Services --> Repositories
    Repositories --> DB
    Services --> Redis
    Services --> Cloudinary
```

## Application Layers

```text
HTTP Request
     │
     ▼
┌───────────────────────────┐
│ Security Filter Chain     │
│ JWT Authentication        │
│ Role-Based Authorization  │
└─────────────┬─────────────┘
              ▼
┌───────────────────────────┐
│ Controllers               │
│ HTTP + DTO boundary       │
└─────────────┬─────────────┘
              ▼
┌───────────────────────────┐
│ Services                  │
│ Business logic            │
│ Transactions              │
│ Idempotency               │
│ Inventory coordination    │
└─────────────┬─────────────┘
              ▼
┌───────────────────────────┐
│ Repositories              │
│ Spring Data JPA           │
└─────────────┬─────────────┘
              ▼
       PostgreSQL
```

## Infrastructure

```text
                    ┌──────────────────┐
                    │      Render      │
                    │  Spring Boot API │
                    └───────┬──────────┘
                            │
              ┌─────────────┼─────────────┐
              │             │             │
              ▼             ▼             ▼
       ┌────────────┐ ┌────────────┐ ┌────────────┐
       │  Supabase  │ │   Upstash  │ │ Cloudinary │
       │ PostgreSQL │ │    Redis   │ │   Images   │
       └────────────┘ └────────────┘ └────────────┘
```

## Checkout Flow

```mermaid
sequenceDiagram
    participant Client
    participant Checkout
    participant Idempotency
    participant Inventory
    participant DB

    Client->>Checkout: POST /checkout/me/create
    Checkout->>Idempotency: Check Idempotency-Key

    alt Existing COMPLETED request
        Idempotency-->>Checkout: Existing result
        Checkout-->>Client: Existing order
    else New request
        Checkout->>Idempotency: Mark PROCESSING
        Checkout->>DB: Begin transaction
        Checkout->>Inventory: Acquire inventory lock
        Inventory-->>Checkout: Current stock
        Checkout->>Inventory: Validate + decrement
        Checkout->>DB: Persist order
        Checkout->>Idempotency: Mark COMPLETED
        Checkout-->>Client: Order response
    end
```

## Product Read Path

```text
GET /products/{id}
       │
       ▼
ProductService
       │
       ▼
Redis cache?
   ┌───┴───┐
   │       │
  HIT     MISS
   │       │
   │       ▼
   │   PostgreSQL
   │       │
   │       ▼
   │     Redis
   │       │
   └───┬───┘
       ▼
ProductResponse
```

## JWT Revocation

```text
Login
  │
  ▼
JWT generated
  │
  └── jti = unique token ID

Logout
  │
  ▼
Redis
jwt:blacklist:{jti}
  │
  ▼
Future request
  │
  ▼
JwtAuthenticationFilter
  │
  ├── token valid?
  ├── user enabled?
  └── jti blacklisted?
        │
        ├── yes → reject
        └── no  → authenticate
```

## Data Consistency

### Optimistic locking

Inventory contains:

```java
@Version
private Long version;
```

This lets JPA detect conflicting updates to the same inventory row.

### Pessimistic locking

Critical inventory access uses a database write lock so competing transactions cannot simultaneously modify the same inventory row.

### Idempotency

Checkout requests carry an idempotency key and are tracked through processing states so retries can be distinguished from genuinely new checkout operations.

## Deployment Profiles

```text
Local Docker
    │
    └── application-docker.properties
          ├── PostgreSQL → postgres:5432
          └── Redis      → redis:6379 (no TLS)

Production
    │
    └── application-prod.properties
          ├── PostgreSQL → Supabase
          └── Redis      → Upstash (TLS)
```
