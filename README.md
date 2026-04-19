# Aurum Backend

Backend service for Aurum, a SIP-based portfolio tracking application.

## Features
- JWT-based authentication (signup/login)
- Create and manage SIP investments
- Portfolio summary (invested, current value, profit/loss, duration)
- SIP calculations using historical NAV data (mfapi.in)
- AI-based investment insights

## Tech Stack
- Java, Spring Boot
- Spring Security (JWT)
- JPA / Hibernate
- REST APIs
- External API: https://api.mfapi.in
- Deployed on Render

## API Endpoints
Auth:
POST /api/v1/auth/signup  
POST /api/v1/auth/login  

SIP:
POST /api/v1/sip  
GET /api/v1/sip  
GET /api/v1/sip/summary  
GET /api/v1/sip/ai-insights  

## Run Locally
git clone https://github.com/your-username/aurum-backend.git  
cd aurum-backend  

Configure database in application.properties:
spring.datasource.url=YOUR_DB_URL  
spring.datasource.username=YOUR_DB_USERNAME  
spring.datasource.password=YOUR_DB_PASSWORD  

Run:
./mvnw spring-boot:run

## Notes
- Portfolio values are calculated using SIP logic (monthly investments + NAV history)
- Each SIP is calculated individually and then aggregated

## License
MIT
