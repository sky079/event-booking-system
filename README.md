# Event Booking System

A backend REST API for an Event Booking System built with Java and Spring Boot.

The system supports two roles:

* **Organizer** — create, update, cancel and manage events
* **Customer** — browse events and book tickets

The application includes JWT authentication, role-based authorization, PostgreSQL persistence, asynchronous email processing using JobRunr, real email notifications using Resend, Docker support, Swagger/OpenAPI documentation, and k6 load testing.

---

## 1. Tech Stack

* Java 17
* Spring Boot
* Spring Web
* Spring Data JPA / Hibernate
* Spring Security
* JWT Authentication
* PostgreSQL
* Maven
* JobRunr
* Resend
* Docker
* k6
* Swagger / OpenAPI
* JUnit / Mockito
* Git / GitHub

---

## 2. Architecture

```text
Client
  |
  v
Spring Boot REST API
  |
  +-------------------+
  |                   |
  v                   v
PostgreSQL          JobRunr
                      |
                      v
                    Resend
                      |
                      v
                  Email Service
```

The application follows a layered architecture:

```text
Controller
    |
    v
Service
    |
    v
Repository
    |
    v
PostgreSQL
```

Spring Security and JWT are used for authentication and role-based authorization.

---

## 3. User Roles

### Organizer

Organizers can:

* Create events
* Update their own events
* Cancel their own events
* View bookings for their events
* View event statistics

### Customer

Customers can:

* View upcoming events
* View event details
* Book tickets
* View their bookings

---

# 4. Authentication

Authentication uses JWT.

### Register

```http
POST /api/auth/register
```

Example:

```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123",
  "role": "CUSTOMER"
}
```

### Login

```http
POST /api/auth/login
```

Example:

```json
{
  "email": "john@example.com",
  "password": "password123"
}
```

The API returns a JWT token.

Protected APIs require:

```http
Authorization: Bearer <JWT_TOKEN>
```

---

# 5. API Endpoints

## Authentication

| Method | Endpoint             | Access |
| ------ | -------------------- | ------ |
| POST   | `/api/auth/register` | Public |
| POST   | `/api/auth/login`    | Public |

## Events

| Method | Endpoint                    | Access    |
| ------ | --------------------------- | --------- |
| POST   | `/api/events`               | ORGANIZER |
| GET    | `/api/events`               | Public    |
| GET    | `/api/events/{id}`          | Public    |
| PUT    | `/api/events/{id}`          | ORGANIZER |
| DELETE | `/api/events/{id}`          | ORGANIZER |
| GET    | `/api/events/{id}/bookings` | ORGANIZER |
| GET    | `/api/events/{id}/stats`    | ORGANIZER |

## Bookings

| Method | Endpoint                         | Access   |
| ------ | -------------------------------- | -------- |
| POST   | `/api/events/{eventId}/bookings` | CUSTOMER |
| GET    | `/api/bookings`                  | CUSTOMER |
| GET    | `/api/bookings/{id}`             | CUSTOMER |

---

# 6. Event Creation

Example:

```http
POST /api/events
Authorization: Bearer <ORGANIZER_TOKEN>
Content-Type: application/json
```

```json
{
  "name": "Java Backend Workshop",
  "description": "Spring Boot backend development workshop",
  "location": "Bangalore",
  "startTime": "2027-01-01T10:00:00Z",
  "endTime": "2027-01-01T12:00:00Z",
  "totalTickets": 1000
}
```

When an event is created:

```text
availableTickets = totalTickets
```

---

# 7. Booking

Example:

```http
POST /api/events/{eventId}/bookings
Authorization: Bearer <CUSTOMER_TOKEN>
Content-Type: application/json
```

```json
{
  "quantity": 2
}
```

If sufficient tickets are available, the booking is confirmed.

If insufficient tickets are available:

```http
409 Conflict
```

is returned.

---

# 8. Concurrency and Ticket Reservation

Ticket availability is the main concurrency-sensitive part of the system.

Two implementations were created.

## V1

The initial implementation follows a read-modify-write approach:

```text
Read event
    |
Check available tickets
    |
Decrease available tickets
    |
Save event
    |
Create booking
```

## V2

The optimized implementation performs the ticket deduction atomically at the database level:

```sql
UPDATE events
SET available_tickets = available_tickets - :quantity
WHERE id = :eventId
  AND available_tickets >= :quantity
```

The number of affected rows is checked:

```text
updated == 1
    |
    +-- Booking allowed

updated == 0
    |
    +-- Insufficient tickets
```

This moves the inventory check and deduction into a single database operation.

---

# 9. Background Email Processing

Email processing is handled asynchronously using JobRunr.

## Booking Confirmation

After a booking transaction successfully commits:

```text
Booking API
    |
    v
Database Transaction
    |
    v
Transaction Commit
    |
    v
JobRunr Job
    |
    v
Resend
    |
    v
Customer Email
```

The booking API does not wait for the external email provider to finish processing the email.

## Event Update Notification

When an organizer updates an event:

1. The event is updated.
2. The database transaction commits.
3. A JobRunr background job is enqueued.
4. Customers with confirmed bookings are identified.
5. Event update emails are sent.

---

# 10. JobRunr

JobRunr is used for persistent background jobs.

It is backed by PostgreSQL and processes email jobs asynchronously.

The JobRunr dashboard is available locally at:

```text
http://localhost:8000
```

---

# 11. Idempotency

Booking requests support an idempotency key.

The booking table contains a unique constraint on:

```text
(customer_id, idempotency_key)
```

This helps prevent duplicate bookings when the same customer retries a request with the same idempotency key.

Example:

```http
Idempotency-Key: booking-request-123
```

---

# 12. Database Design

## Users

```text
users
----------------
id
name
email
password
role
created_at
```

## Events

```text
events
----------------
id
organizer_id
name
description
location
start_time
end_time
total_tickets
available_tickets
status
created_at
updated_at
```

## Bookings

```text
bookings
----------------
id
event_id
customer_id
quantity
status
idempotency_key
created_at
```

Indexes are used on frequently queried booking fields such as:

```text
event_id
customer_id
```

---

# 13. Validation

The API validates incoming requests using Jakarta Bean Validation.

Examples:

* Event name cannot be blank.
* Event name has a maximum length.
* Ticket quantity must be positive.
* Total tickets must be within the configured range.
* Event start time must be in the future.
* End time must be after start time.

Invalid requests return appropriate HTTP 4xx responses.

---

# 14. Authorization

Role-based authorization is implemented using Spring Security.

```text
ORGANIZER
    |
    +-- Create Event
    +-- Update Event
    +-- Cancel Event
    +-- View Event Bookings
    +-- View Event Statistics

CUSTOMER
    |
    +-- View Events
    +-- Book Tickets
    +-- View Own Bookings
```

An organizer can only modify events owned by that organizer.

---

# 15. API Documentation

Swagger/OpenAPI documentation is available when the application is running:

```text
http://localhost:8080/swagger-ui/index.html
```

Swagger UI provides:

* API endpoint documentation
* Request/response models
* Authentication
* Interactive API testing

---

# 16. Running Locally

## Prerequisites

* Java 17
* Maven
* PostgreSQL
* Git

Optional:

* Docker
* k6

## Clone

```bash
git clone <YOUR_GITHUB_REPOSITORY_URL>
cd event-booking-system
```

## Environment Variables

Example:

```text
DATABASE_URL=jdbc:postgresql://localhost:5432/event_booking
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=your_password

JWT_SECRET=your_jwt_secret

RESEND_API_KEY=your_resend_api_key
RESEND_FROM_EMAIL=your_verified_sender

APP_BOOKING_MODE=v1
```

Do not commit secrets to Git.

## Start the Application

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

The API will be available at:

```text
http://localhost:8080
```

---

# 17. Docker

Build the application:

```bash
./mvnw clean package -DskipTests
```

Build the Docker image:

```bash
docker build -t event-booking-system .
```

Run:

```bash
docker run -p 8080:8080 event-booking-system
```

---

# 18. Load Testing

k6 was used to test the booking API under increasing concurrency.

The test sends authenticated booking requests to:

```text
POST /api/events/{eventId}/bookings
```

Example:

```powershell
$env:BASE_URL="http://localhost:8080"
$env:TOKEN="<CUSTOMER_JWT>"
$env:EVENT_ID="<EVENT_ID>"

k6 run .\k6\booking-load-test.js
```

The workload uses staged increases in virtual users.

---

# 19. V1 Performance Results

V1 was tested with a maximum of **500 virtual users**.

### Results

| Metric          |           V1 |
| --------------- | -----------: |
| Maximum VUs     |          500 |
| Total Requests  |       10,382 |
| Throughput      | 415.14 req/s |
| HTTP Error Rate |           0% |
| Average Latency |    429.69 ms |
| Median Latency  |    292.29 ms |
| P90 Latency     |       1.01 s |
| P95 Latency     |       1.11 s |
| Maximum Latency |       1.62 s |

All **10,382 requests** completed successfully.

The test reached a maximum of 496 active VUs during execution, with 500 configured as the maximum.

These results represent the specific local test environment and workload used during testing.

---

# 20. V2 Performance Results

V2 was tested with a maximum of **200 virtual users**.

### Results

| Metric          |          V2 |
| --------------- | ----------: |
| Maximum VUs     |         200 |
| Total Requests  |         798 |
| Throughput      | 14.51 req/s |
| HTTP Error Rate |          0% |
| Average Latency |   125.11 ms |
| Median Latency  |    33.34 ms |
| P90 Latency     |   304.94 ms |
| P95 Latency     |   531.23 ms |
| Maximum Latency |      2.78 s |

All **798 requests** completed successfully.

The test reached 200 maximum configured VUs.

---

# 21. V1 vs V2

The main difference between the two implementations is the ticket reservation strategy.

### V1

```text
Read
  ↓
Check
  ↓
Modify
  ↓
Save
```

### V2

```text
Atomic Database UPDATE
          ↓
Check affected rows
          ↓
Create booking
```

The V2 implementation performs the ticket availability check and deduction atomically:

```sql
UPDATE events
SET available_tickets = available_tickets - :quantity
WHERE id = :eventId
  AND available_tickets >= :quantity
```

### Observed Load-Test Results

The tests produced the following measurements:

| Metric          |           V1 |          V2 |
| --------------- | -----------: | ----------: |
| Maximum VUs     |          500 |         200 |
| Requests        |       10,382 |         798 |
| Throughput      | 415.14 req/s | 14.51 req/s |
| Error Rate      |           0% |          0% |
| Average Latency |    429.69 ms |   125.11 ms |
| Median Latency  |    292.29 ms |    33.34 ms |
| P95             |       1.11 s |   531.23 ms |

The two tests used different maximum VU levels, so throughput should **not** be interpreted as a direct V1-versus-V2 comparison.

The V2 test showed lower latency under its 200-VU workload. A direct performance comparison requires both versions to be tested with identical k6 stages, duration, event capacity, database configuration and environment.

---

# 22. Concurrency Correctness Test

Performance testing and concurrency correctness are evaluated separately.

For example:

```text
Event capacity: 100 tickets
Concurrent booking attempts: 200
Quantity per request: 1
```

After the test, the database can be checked:

```sql
SELECT available_tickets
FROM events
WHERE id = <EVENT_ID>;
```

And:

```sql
SELECT COALESCE(SUM(quantity), 0)
FROM bookings
WHERE event_id = <EVENT_ID>
  AND status = 'CONFIRMED';
```

The confirmed booking quantity should not exceed the original event capacity.

This verifies that concurrent requests respect the available ticket inventory.

---

# 23. Error Handling

The application uses centralized API error handling.

Common responses:

| Status | Meaning               |
| ------ | --------------------- |
| 400    | Bad Request           |
| 401    | Unauthorized          |
| 403    | Forbidden             |
| 404    | Not Found             |
| 409    | Conflict              |
| 500    | Internal Server Error |

Examples:

* Invalid request → `400`
* Missing/invalid JWT → `401`
* Insufficient role → `403`
* Event not found → `404`
* Insufficient tickets → `409`

---

# 24. Design Decisions

### PostgreSQL

PostgreSQL provides transactional persistence for events and bookings.

### Spring Data JPA

JPA/Hibernate simplifies database access while allowing custom queries for concurrency-sensitive operations.

### JWT

JWT provides stateless authentication for the REST APIs.

### JobRunr

JobRunr provides persistent asynchronous background processing without requiring additional infrastructure such as Kafka or Redis.

### Resend

Resend is used for transactional booking confirmation and event update emails.

### Atomic Ticket Update

V2 moves the ticket deduction into an atomic database operation so that ticket availability is checked and reduced as one database operation.

---

# 25. Trade-offs

The implementation intentionally uses a relatively simple architecture:

```text
Spring Boot
PostgreSQL
JobRunr
Resend
```

Instead of introducing additional distributed infrastructure such as:

```text
Kafka
Redis
Kubernetes
Microservices
```

This keeps the system easier to develop and deploy while still demonstrating:

* Authentication
* Authorization
* Transactions
* Concurrency handling
* Asynchronous processing
* Email notifications
* Load testing
* Performance optimization

---

# 26. Future Improvements

Possible production improvements include:

* Redis caching
* Kafka-based event notifications
* Dedicated email workers
* Distributed rate limiting
* Database connection-pool tuning
* Database read replicas
* Horizontal scaling
* Distributed tracing
* Prometheus/Grafana metrics
* Circuit breakers for external services
* More comprehensive integration and concurrency testing

---

# 27. Project Structure

```text
src/main/java/com/sumit/eventbooking
│
├── common
│   └── ApiException
│
├── config
│   └── SecurityConfig
│
├── controller
│   ├── AuthController
│   ├── EventController
│   └── BookingController
│
├── entity
│   ├── event
│   └── booking
│
├── jobs
│   ├── BookingEmailJob
│   └── EventUpdateEmailJob
│
├── model
│   ├── event
│   ├── booking
│   └── user
│
├── repository
│   ├── UserRepository
│   ├── EventRepository
│   └── BookingRepository
│
├── security
│   ├── JwtAuthenticationFilter
│   └── JwtService
│
└── service
    ├── BookingService
    ├── BookingServiceV1
    ├── BookingServiceV2
    ├── EventService
    └── EmailService
```

---

# 28. Demo Flow

The application can be demonstrated in the following order:

```text
1. Register Organizer
2. Login Organizer
3. Create Event
4. Register Customer
5. Login Customer
6. Browse Events
7. Book Tickets
8. Verify Booking
9. Verify Booking Confirmation Email
10. Update Event as Organizer
11. Verify JobRunr Job
12. Verify Event Update Email
13. Run V1 Load Test
14. Run V2 Load Test
15. Compare Results
```

---

# 29. Deployment

The application can be deployed as a Dockerized Spring Boot service.

Deployment architecture:

```text
                Client
                   |
                   v
          Spring Boot API
             /         \
            /           \
           v             v
     PostgreSQL        JobRunr
                         |
                         v
                       Resend
```

Production secrets should be configured through environment variables provided by the deployment platform.

---

# 30. Conclusion

The Event Booking System demonstrates a Spring Boot backend with:

* JWT authentication
* Role-based authorization
* PostgreSQL persistence
* Event and booking management
* Idempotent booking support
* Asynchronous email processing
* Real email notifications
* Concurrency-aware ticket reservation
* Swagger/OpenAPI documentation
* Docker support
* k6 load testing
* V1 and V2 booking implementations

The primary V2 optimization moves ticket availability checking and deduction into an atomic database operation, addressing the concurrency-sensitive part of the booking flow.
