# AutoMinutes

Aplicație de management al ședințelor cu procesare AI a transcrierilor (Spring Boot 4 + Angular).

## Tech stack

- Backend: Spring Boot 4, Java 17, Gradle
- DB: H2 (dev/test) sau PostgreSQL (rulare reală), migrații cu Flyway
- AI: Ollama local (`llama3.2`) prin `LlmClient`
- Frontend: Angular

## Rulare rapidă (H2, fără setup)

Nu ai nevoie de nimic instalat separat — H2 e in-memory și pornește automat.

./gradlew bootRun


Aplicația pornește pe `http://localhost:8080`, cu schema creată automat de Flyway și câteva date de test.

## Rulare cu PostgreSQL

### 1. Instalează PostgreSQL

Descarcă de pe [postgresql.org](https://www.postgresql.org/download/) (vine cu pgAdmin inclus). Reține portul și parola userului `postgres` setate la instalare.

### 2. Creează baza de date

În pgAdmin: click dreapta pe **Databases** → **Create** → **Database**, nume `autominutes`.

Sau din `psql`:
```sql
CREATE DATABASE autominutes;
```

### 3. Configurează variabilele de mediu

Copiază `.env.example` → `.env` și completează cu valorile tale reale:

DB_URL=jdbc:postgresql://localhost:5432/autominutes
DB_USERNAME=postgres
DB_PASSWORD=parola_ta


`.env` e doar pentru referință — Spring Boot nu îl citește automat. Trebuie să setezi efectiv variabilele:

- **IntelliJ**: Run → Edit Configurations → `BackendApplication` → Modify options → bifează *Environment variables* → adaugă cele 3 de mai sus, plus în câmpul *Active profiles* pui `postgres`.
- **Terminal**:

export DB_URL=jdbc:postgresql://localhost:5432/autominutes
export DB_USERNAME=postgres
export DB_PASSWORD=parola_ta
./gradlew bootRun --args='--spring.profiles.active=postgres'


### 4. Verifică

La pornire, log-urile ar trebui să arate Flyway rulând migrațiile pe `jdbc:postgresql://...` (nu `h2:mem`). Tabelele apar în pgAdmin sub `autominutes → Schemas → public → Tables`.

## Testare

./gradlew test


Testele folosesc profilul default (H2), independent de setup-ul PostgreSQL de mai sus.

## Securitate

Nu pune niciodată credențiale reale în `application.properties` sau `application-postgres.properties` — acestea sunt commitate în git. Folosește `.env` (gitignored) sau environment variables pentru orice valoare sensibilă.