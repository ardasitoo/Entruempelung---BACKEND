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

## E-Mail-Benachrichtigungen mit Resend

Nach dem Speichern einer Kundenanfrage sendet das Backend die Benachrichtigung
über HTTPS an `https://api.resend.com/emails`. Die Formularroute bleibt gleich.
Antworten auf die Benachrichtigung gehen an die E-Mail-Adresse des Kunden.
Ein Versandfehler verhindert nicht das Speichern; es gibt derzeit keine automatische Wiederholung.

Im Render-Backend unter Environment setzen:

```text
EMAIL_NOTIFICATIONS_ENABLED=true
RESEND_API_KEY=<geheimer Resend API-Key>
EMAIL_NOTIFICATIONS_TO=<Empfängeradresse>
EMAIL_NOTIFICATIONS_FROM=NEXRUM <anfragen@deine-verifizierte-domain.de>
```

Den API-Key nur als geheime Umgebungsvariable hinterlegen, niemals in GitHub
oder im Frontend. Die bisherigen `SPRING_MAIL_*`-Variablen werden nicht mehr benötigt.
Anschließend das Backend neu deployen.

Für einen ersten Test ohne eigene Domain ist `NEXRUM <onboarding@resend.dev>`
als Absender möglich. Der Empfänger muss dann die Adresse des Resend-Kontos sein.
Für beliebige Empfänger eine eigene Domain in Resend verifizieren. Eine fremde
Domain wie `web.de` oder `gmail.com` kann nicht als eigene Absenderdomain dienen.

Geschützter Versandtest (curl fragt nach dem Admin-Passwort):

```bash
curl -i -X POST https://entruempelung-backend-1.onrender.com/api/admin/test-email -u DEIN_ADMIN
```

HTTP 204 bedeutet, dass Resend den Versand angenommen hat, nicht dass die Mail
bereits zugestellt ist. Den Zustellstatus im Resend-Dashboard prüfen.
Bei Fehlern Backend-Logs und Resend-Dashboard prüfen (API-Key, Absenderdomain,
Testempfänger-Beschränkung). Keine Zugangsdaten oder Kundeninhalte werden geloggt.
Wenn `EMAIL_NOTIFICATIONS_ENABLED=false` bleibt, werden Anfragen nur gespeichert.

Dokumentation: https://resend.com/docs/api-reference/emails/send-email
Testdomain: https://resend.com/docs/knowledge-base/403-error-resend-dev-domain

## Auftragsstatus im Admin-Bereich

`PATCH /api/customer-requests/{id}/status` ist nur mit Admin-Zugang erlaubt.
Beispiel: `{"status":"CONTACTED"}`. Erlaubte Werte: `NEW`, `CONTACTED`,
`OFFER_SENT`, `ACCEPTED`, `REJECTED`, `COMPLETED`. Andere Kundenfelder werden
über diese Route nicht geändert. Ungültige Statuswerte liefern 400,
unbekannte Anfragen 404 und fehlende/falsche Zugangsdaten 401.

Die bestehende H2-Datenbank läuft im Arbeitsspeicher: Anfragen und Statusänderungen
überstehen keinen Backend-Neustart. Für den dauerhaften Firmenbetrieb muss eine
persistente Datenbank eingerichtet werden.

## Dauerhafte PostgreSQL-Datenbank (Render)

Der Standardmodus bleibt H2 im Arbeitsspeicher für die lokale Entwicklung.
Erst mit `SPRING_PROFILES_ACTIVE=postgres` wird ausschließlich die externe Datenbank
verwendet. Im PostgreSQL-Profil werden keine Demo-Anfragen angelegt und die H2-Konsole
ist deaktiviert. Bei fehlender Verbindung schlägt der Start fehl, anstatt unbemerkt
auf eine leere H2-Datenbank zurückzufallen.

1. Bestehende benötigte H2-Anfragen vor dem nächsten Backend-Neustart sichern.
   E-Mail-Benachrichtigungen enthalten Kundenangaben, aber nicht die späteren Statusänderungen.
2. Render: New → Postgres. Datenbank in derselben Region wie das Backend anlegen.
   Für dauerhaften Betrieb einen bezahlten Plan wählen: Free läuft nach 30 Tagen ab.
   Preis inklusive Speicher vor Erstellung prüfen.
3. Im Backend unter Environment folgende Werte aus den Datenbank-Verbindungsdetails setzen:

```text
SPRING_PROFILES_ACTIVE=postgres
DB_HOST=<interner Hostname, ohne Protokoll>
DB_PORT=5432
DB_NAME=<Database>
DB_USERNAME=<Username>
DB_PASSWORD=<Passwort, geheim>
```

Nicht die `postgresql://...`-URL als DB_HOST eintragen. Die Anwendung baut daraus
eine JDBC-Verbindung mit erforderlicher TLS-Verschlüsselung. Bereits vorhandene
`SPRING_DATASOURCE_*`-Umgebungsvariablen auf Konflikte prüfen und entfernen.
Alle Werte gemeinsam setzen und danach das Backend deployen. Die Datenbank muss
bereits verfügbar sein. Die Tabellen werden beim ersten Start angelegt, vorhandene
Daten bei weiteren Starts beibehalten (`ddl-auto=update`).

4. Über das Formular eine eindeutig benannte Testanfrage erstellen und im Admin-Bereich
   ihren Status ändern. Backend manuell neu starten. Danach erneut anmelden und
   kontrollieren, dass sowohl Anfrage als auch Status unverändert vorhanden sind.
   Erst dann ist die produktive Persistenz überprüft.

Alte H2-Einträge werden nicht automatisch übertragen. Bereits verlorene Einträge
lassen sich nicht aus H2 wiederherstellen. Bestehende E-Mails können manuell neu
erfasst werden. Backups sind zusätzlich zur dauerhaften Speicherung notwendig;
Render bietet für bezahlte Datenbanken Wiederherstellungsfunktionen.

Quellen: https://render.com/docs/postgresql-creating-connecting
https://render.com/docs/free
