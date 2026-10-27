# Support Ticket Tracker

REST API for a small support ticket system. Users are customers or agents. Customers create tickets, and every ticket follows a fixed status flow.

Built with Java 21, Spring Boot 4.1.1, Spring Data JPA, MySQL and Maven. H2 is used only for tests.

## Running the project

You need Java 21 and MySQL running on `localhost:3306`. The database (`support_ticket_db`) and the tables are created automatically on the first start.

The DB password is read from the `DB_PASSWORD` environment variable, so it isn't stored in the code. The username is `root` by default (change it with `DB_USERNAME`), and the URL can be changed with `DB_URL`.

```bash
git clone https://github.com/<your-username>/support-ticket-tracker.git
cd support-ticket-tracker
export DB_PASSWORD=your_password
./mvnw spring-boot:run
```

On PowerShell use `$env:DB_PASSWORD="your_password"` and `.\mvnw spring-boot:run`. In IntelliJ, add `DB_PASSWORD=your_password` under Run > Edit Configurations > Environment variables and run `SupportTicketTrackerApplication`.

The app runs on http://localhost:8080 and Swagger UI is at http://localhost:8080/swagger-ui/index.html.

To run the tests (they use H2, so MySQL isn't needed):

```bash
./mvnw test
```

## API

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/users` | Create a user |
| GET | `/api/users`, `/api/users/{id}` | List users, get one user |
| PUT | `/api/users/{id}` | Update a user |
| DELETE | `/api/users/{id}` | Delete a user (409 if the user still has tickets) |
| GET | `/api/users/{id}/tickets` | All tickets of one user |
| POST | `/api/tickets` | Create a ticket (always starts as OPEN) |
| GET | `/api/tickets` | List tickets with filter, search and pagination |
| GET | `/api/tickets/{id}` | Get one ticket with the creator's name and email |
| PUT | `/api/tickets/{id}` | Update title, description and priority |
| PATCH | `/api/tickets/{id}/status` | Change the status |
| DELETE | `/api/tickets/{id}` | Delete a ticket |

`GET /api/tickets` accepts `status`, `priority`, `search` (on title, case-insensitive), `page`, `size` and `sort`. All of them are optional and can be used together. By default it returns 10 per page, newest first.

Status flow:

- OPEN -> IN_PROGRESS or CLOSED
- IN_PROGRESS -> RESOLVED
- RESOLVED -> CLOSED or IN_PROGRESS (reopen)
- CLOSED is final

Any other change returns 400.

## Example requests (Postman)

Create a customer: `POST http://localhost:8080/api/users`

```json
{ "name": "Rahim Uddin", "email": "rahim@example.com", "role": "CUSTOMER" }
```

Create a ticket: `POST http://localhost:8080/api/tickets`

```json
{ "title": "Cannot login on mobile", "description": "Login button does nothing", "priority": "HIGH", "createdById": 1 }
```

The response comes with `"status": "OPEN"` and the creator's details:

```json
{
  "id": 1,
  "title": "Cannot login on mobile",
  "description": "Login button does nothing",
  "status": "OPEN",
  "priority": "HIGH",
  "createdBy": { "id": 1, "name": "Rahim Uddin", "email": "rahim@example.com", "role": "CUSTOMER" },
  "createdAt": "2026-10-19T15:10:42.512",
  "updatedAt": "2026-10-19T15:10:42.512"
}
```

Filter, search and paginate in one request:

`GET http://localhost:8080/api/tickets?status=OPEN&priority=HIGH&search=login&page=0&size=10&sort=createdAt,desc`

Change the status: `PATCH http://localhost:8080/api/tickets/1/status`

```json
{ "status": "IN_PROGRESS" }
```

All errors come back in the same format. For example, moving a CLOSED ticket back to OPEN:

```json
{
  "timestamp": "2026-10-19T15:14:05.233",
  "status": 400,
  "error": "Bad Request",
  "message": "Cannot change status from CLOSED to OPEN",
  "path": "/api/tickets/1/status"
}
```

## A few decisions

- Controllers return DTOs, not entities. A ticket knows its creator (`@ManyToOne`), but a user has no list of tickets, so there is no infinite JSON loop.
- The status rules are in the `TicketStatus` enum. The service only calls `canChangeTo()`, so the rules live in one place.
- The POST and PUT request classes don't have a `status` field. A status sent there is ignored, new tickets are always OPEN, and only the PATCH endpoint can change it.
- Filtering uses Spring Data Query by Example with `Pageable`, so there are no hand-written queries. The max page size is 100.
- Emails are saved in lowercase, so the same email with different letter case counts as a duplicate (409).
- Only customers can create tickets, and an agent who tries gets a 400. A customer who already has tickets can't be changed to an agent (409), otherwise those tickets would end up with an agent as creator.
- There is no model package, and the enums have their own package, as my supervisor asked.

## Frontend (Angular)

The Angular app is in the `frontend` folder. It needs Node.js (LTS) and Angular CLI.

```bash
cd frontend
npm install
npm start
```

It runs on http://localhost:4200. The dev server uses `proxy.conf.json` to forward `/api` requests to the backend on `localhost:8080`, so the backend must be running first.

Pages: ticket list (filter, search, pagination), create/edit ticket, ticket details with status change buttons (only valid next statuses are shown), and a users page.