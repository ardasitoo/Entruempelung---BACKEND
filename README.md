# Entruempelung---BACKEND

Backend der Webanwendung für ein Entrümpelungsunternehmen.

## Projektbeschreibung

Ziel des Projekts ist die Entwicklung einer Webanwendung für ein Entrümpelungsunternehmen. 
Kunden sollen sich über angebotene Leistungen informieren und über die Website eine Anfrage für eine Entrümpelung stellen können.

Das Backend verarbeitet die Anfragen der Website und stellt die benötigten Daten über eine REST-API bereit.

## Geplante Funktionen

- Kundenanfragen erfassen
- Kundenanfragen verwalten
- Entrümpelungsleistungen verwalten
- Termine und Aufträge verwalten
- Anbindung an das Frontend über eine REST-API
- Speicherung der Daten in einer Datenbank

## Technologien

- Java
- Spring Boot
- REST API
- Datenbankanbindung (wird später ergänzt)

## Projektstatus

Das Projekt befindet sich aktuell in der Entwicklung.

### Aktueller Stand

- [x] GitHub-Repository erstellt
- [x] Spring-Boot-Projekt einrichten
- [x] Erste Entity erstellen
- [x] Erste GET-Route erstellen
- [x] Beispieldaten über die REST-API ausgeben
- [ ] Datenbank anbinden
- [x] Frontend und Backend verbinden

## REST API

```text
GET /api/customer-requests
```

Diese Route gibt eine Liste von Beispiel-Kundenanfragen zurueck.

## Render Deployment

Service-Typ:

```text
Web Service
```

Build Command:

```bash
mvn clean package -DskipTests
```

Start Command:

```bash
java -jar target/entruempelung-backend-0.0.1-SNAPSHOT.jar
```

Environment Variable fuer CORS:

```text
APP_CORS_ALLOWED_ORIGINS=https://dein-frontend.onrender.com
```

Lokal ist standardmaessig `http://localhost:5173` erlaubt.

Admin-Zugang fuer geschuetzte Anfrage-Liste:

```text
ADMIN_USERNAME=dein-admin-name
ADMIN_PASSWORD=dein-sicheres-passwort
```

Die Route `GET /api/customer-requests` ist geschuetzt und kann nur mit diesen
Admin-Zugangsdaten aufgerufen werden. `POST /api/customer-requests` bleibt fuer
das Kontaktformular oeffentlich erreichbar.

Optionale E-Mail-Benachrichtigung:

```text
EMAIL_NOTIFICATIONS_ENABLED=true
EMAIL_NOTIFICATIONS_TO=deine-email@example.com
EMAIL_NOTIFICATIONS_FROM=noreply@deine-domain.de
SPRING_MAIL_HOST=smtp.example.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=smtp-benutzer
SPRING_MAIL_PASSWORD=smtp-passwort
SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH=true
SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE=true
```

Wenn `EMAIL_NOTIFICATIONS_ENABLED=false` bleibt, werden Anfragen nur gespeichert
und keine E-Mails versendet.
