# GitHub to Slack Commit Tracker

A Spring Boot backend service that tracks GitHub pushes, stores commit history in H2, and notifies a Slack channel.

## Tech Stack
- Java 11+
- Spring Boot 2.7
- H2 In-Memory Database with JPA/Hibernate
- Slack Incoming Webhooks

## Setup

### 1. Configure Slack Webhook
1. Go to https://api.slack.com/apps → Create New App → From Scratch
2. Enable **Incoming Webhooks** → Add New Webhook to Workspace
3. Copy the webhook URL and paste it in `src/main/resources/application.properties`:
   ```
   slack.webhook.url=https://hooks.slack.com/services/YOUR/WEBHOOK/URL
   ```

### 2. Run the Application
```bash
./mvnw spring-boot:run
```
The app starts on `http://localhost:8080`.

### 3. Expose Localhost (for GitHub Webhook)
Use [ngrok](https://ngrok.com/) to expose your local server:
```bash
ngrok http 8080
```
Copy the HTTPS URL (e.g., `https://abc123.ngrok-free.app`).

### 4. Configure GitHub Webhook
1. Go to your GitHub repo → Settings → Webhooks → Add webhook
2. **Payload URL:** `https://abc123.ngrok-free.app/api/webhook/github`
3. **Content type:** `application/json`
4. **Events:** Select "Just the push event"
5. Click **Add webhook**

### 5. Test
Push code to your GitHub repo. You should see:
- Data saved in H2 database
- Slack notification in your channel

### 6. Verify in H2 Console
Open `http://localhost:8080/h2-console` with:
- JDBC URL: `jdbc:h2:mem:commitdb`
- Username: `sa`
- Password: *(leave empty)*

**SQL to verify author-commit relationship:**
```sql
SELECT a.id AS author_id, a.name, a.email, a.username, a.pushed_at,
       c.id AS commit_id, c.commit_id AS sha, c.message, c.timestamp
FROM authors a
JOIN commits c ON a.id = c.author_id
ORDER BY a.id, c.id;
```

## Architecture
```
GitHub Push → Webhook Controller → WebhookService → Save to H2 DB
                                                   → SlackService → Slack Channel
```

## Entity Relationship
```
Author (1) ──── (N) Commit
  - id              - id
  - name            - commit_id (SHA)
  - email           - message
  - username         - url
  - pushed_at       - timestamp
                    - author_id (FK)
```
// test

some change
test change
// test
