# Dokumentation

## Überblick

![Architektur](Architektur.drawio.png)

*Architektur*

Die Enterprise Application Integration Komponente (EAI) bietet eine Webschnittstelle, bei deren Aufruf im Hintergrund die vorliegenden Daten aus dem angegebenen S3-Bucket abgeholt und als verarbeitet gekennzeichnet werden können.

### Sequenzdiagramm für die S3-Integration

![Sequenzdiagramm](Sequenz.drawio.png)

*Sequenzdiagramm*


Github-Repo:  https://github.com/it-at-m/mobidam-s3-eai


## Technisches Setup

### Openapi
Um neue Openapi Java Source Dateien zu erstellen kann das Maven Profil _generate-openapi_ verwendet werden.
Das Profil erzeugt die Openapi Java Source Dateien im Maven _target_ Ordner.
Änderungen und neue Features können in die Klassen im Package _de.muenchen.mobidam.rest_ kopiert werden.

Die Openapi Quelle man mit dem [Swagger Editor](https://editor.swagger.io) bearbeitet werden.

### Konfiguration

Zur Konfiguration der Credentials der Buckets dient das Property ***mobidam.s3.bucket-credential-config***.
Dieses ist als Map gestaltet und enthält für jeden benannten Bucket die Namen von Umgebungsvariablen für Access-Key und Secret-Key:
```
x-itmkm82k:
  access-key-env-var: MOBIDAM_BUCKET1_ACCESS_KEY
  secret-key-env-var: MOBIDAM_BUCKET1_SECRET_KEY
int-mdasc-mdasdev:
  access-key-env-var: MOBIDAM_BUCKET2_ACCESS_KEY
  secret-key-env-var: MOBIDAM_BUCKET2_SECRET_KEY
```
Die Umgebungsvariablen müssen entsprechend in der Laufzeitumgebung bereitgestellt werden:
```
MOBIDAM_BUCKET1_ACCESS_KEY=<my-access-key1>
MOBIDAM_BUCKET1_SECRET_KEY=<my-secret-key1>
MOBIDAM_BUCKET2_ACCESS_KEY=<my-access-key2>
MOBIDAM_BUCKET2_SECRET_KEY=<my-secret-key2>
```