# Dokumentation

## Überblick

![Architektur](Architektur.drawio.png)

*Architektur*

Die Enterprise Application Integration Komponente (EAI) bietet eine Webschnittstelle, bei deren Aufruf im Hintergrund die vorliegenden Daten aus dem angegebenen S3-Bucket abgeholt und als verarbeitet gekennzeichnet werden können.

### Sequenzdiagramm für die S3-Integration

![Sequenzdiagramm](Sequenz.drawio.png)

*Sequenzdiagramm*


Github-Repo:  https://github.com/it-at-m/mobidam-s3-eai

## API

| **API**            | **Beschreibung**                                              | **Parameter**                 |
|--------------------|---------------------------------------------------------------|-------------------------------|
| /api/filesInFolder | Auflisten aller Objekte im Bucket mit / ohne bestimmtem Path-Prefix | bucketName<br/>path           | 
| /api/presignedUrl  | Erstellen einer vorsignierten Url für ein Objekt im Bucket    | bucketName<br/>objectName<br/>path |
| /api/archive       | Archivieren eines Objektes im Bucket                          | bucketName<br/>objectName |

## Technisches Setup
## S3
Unser LHM S3 ist eine Implementierung von [StorageGrid](https://docs.netapp.com/us-en/storagegrid-family/).

Für das Projekt existiert ein S3 Tenant dem auf Antrag neue Buckets hinzugefügt werden können.
Jeder Bucket ist fachlich einer Schnittstelle zugeordnet.

Innerhalb des Buckets können Dateien mit einem 'Prefix' geordnet werden. Z. Bsp. 
- /Pfad1/Pfad1/Datei1
- /Pfad1/Pfad1/Datei2
- /Pfad1/Pfad2/Datei1
- ...

Bei einem 'Prefix' handelt es sich nicht um eine Pfad, sondern er ist besser als eine 'vorangestellte' Erweiterung des Dateinamens zu verstehen.

Es wird erwartet das Dateien immer mit einem 'Prefix' in einen S3 Bucket importiert werden. 
  
## Openapi
Um neue Openapi Java Source Dateien zu erstellen kann das Maven Profil _generate-openapi_ verwendet werden (mvn clean compile -P generate-openapi).
Das Profil erzeugt die Openapi Java Source Dateien im Maven _target_ Ordner.
Änderungen und neue Features können in die Klassen im Package _de.muenchen.mobidam.rest_ kopiert werden.

Die Openapi Quelle kann mit dem [Swagger Editor](https://editor.swagger.io) angezeigt und bearbeitet werden.
- Dazu den Swagger Editor mit dem Link im Browser starten.
- Die Openapi YAML aus [GitHub ...main/src/main/resources/openapi_rest_s3...yaml ](https://github.com/it-at-m/mobidam-s3-eai/blob/main/src/main/resources/) downloaden.
- Die Openapi YAML im Browser mit dem Swagger Editor über die Menü Punkte *File/Import file* öffnen.

## Security
Wird die EAI im Security Modus gestartet, muss der Aufrufer der REST Schnittstelle ein gültigen OAuth 2.0 Token mitliefern, sonst wird die Anfrage mit dem HTTP Status Code 401 "Unauthorized" abgelehnt.
Das gilt auch für einen abgelaufenen Token.

Zu Testzwecken kann ein Token bsp.weise mit curl vom SSO Provider bezogen werden :

curl \
-d "client_id=[client_id]" \
-d "client_secret=[client_secret]" \
-d "grant_type=client_credentials" \
"https://..."

# REST Schnittstelle
Mit dem [Swagger Editor](https://editor.swagger.io) kann die komplette [Openapi REST Beschreibung](https://github.com/it-at-m/mobidam-s3-eai/blob/sprint/src/main/resources/openapi_rest_s3_v1.yaml) angezeigt werden.
Der Workflow für den Import von Dateien in FME sieht folgende Schritt vor:
- Anzeigen von Inhalten eines S3 Buckets.
- Erzeugen eines Download Links für eine Datei.
- Nach dem Herunterladen und dem Import in FME verschieben der Datei in Archiv innerhalb des Buckets. Für alle Buckets ist 'archiv' ein festgelegter Prefix der in allen Buckets gleich ist. Beim Verschieben in das Archiv wird dem Objektnamen bestehend aus Prefix/Objektname das Prefix 'archiv' vorangestellt (s.u).
- Mit dem Verschiebn in das Bucket Archiv wird zusätzlich ein Datenbankeintrag mit einer Verfallsdauer der Datei geschrieben.
- Löschen der Datei aus dem S3 nach dem Ablauf der Verfalldauer.

## Anzeigen nicht verarbeiteter Dateien
Mit der Rest Ressource GET '.../filesInFolder?bucketName=bucket1&path=...[&archived=false]' können die Dateien mit einem bestimmten Prefix selektiert werden.

## Download von Dateien aus dem S3
Mit der REST Ressource '/presignedUrl' läßt sich ein zeitlich begrenzter Download Link für eine Datei aus einem S3 Bucket erstellen.
Z.Bsp. : GET '.../presignedUrl?bucketName=bucket1&objectName=Pfad1/Pfad2/Dateiname' 

## Archivieren und Anzeigen von bereits verarbeiteten Dateien
### Archivieren
Mit der Rest Ressource '/archive' lassen sich bereits in FME importierte Dateien in einen vordefinierten 'archiv' Pfad verschieben, damit sie nicht noch einmal verarbeitet werden.
Technisch ist das in S3 eine Erweiterung des Datei Pfads und Namens im S3 Bucket.
Z.Bsp. : PUT '.../archive?bucketName=bucket1&objectName=Pfad1/Pfad2/Datei' wird innerhalb des 'bucket1' verschoben nach 'archive/Pfad1/Pfad2/Datei'.

Für alle archivierten Dateien wird mit dem Verschieben in das '/archive' ein Datenbankeintrag mit einem Löschdatum erstellt.
Anhand des Datenbankeintrags werden die archivierten Dateien nach einer Frist automatisch aus dem S3 Bucket wieder gelöscht.

### Anzeigen
Mit der Rest Ressource GET '.../filesInFolder?bucketName=bucket1&path=...&archived=true' können die archivierten Dateien eines Pfades angezeigt werden.

### Konfiguration

Zur Konfiguration der Credentials der Buckets dient das Property ***de.muenchen.mobidam.common.s3.bucket-credential-configs***.
Dieses ist als Map gestaltet und enthält die default Tenant-Credentials. Nach Bedarf können der Access-Key und Secret-Key für die einzelnen Buckets separat konfiguriert werden:
```
de.muenchen.mobidam:
  common:
    s3:
      bucket-credential-configs:
        tenant-default:
          access-key-env-var: MOBIDAM_ACCESS_KEY
          secret-key-env-var: MOBIDAM_SECRET_KEY
        int-mdasc-mdasdev:
          access-key-env-var: MOBIDAM_BUCKET2_ACCESS_KEY
          secret-key-env-var: MOBIDAM_BUCKET2_SECRET_KEY

```
Die Umgebungsvariablen müssen entsprechend in der Laufzeitumgebung bereitgestellt werden:
```
MOBIDAM_ACCESS_KEY=<my-access-key1>
MOBIDAM_SECRET_KEY=<my-secret-key1>
MOBIDAM_BUCKET2_ACCESS_KEY=<my-access-key2>
MOBIDAM_BUCKET2_SECRET_KEY=<my-secret-key2>
```

# Archivierte Dateien aufräumen
Alle im S3 archivierten Dateien werden nach einer Ablauffrist automatisch über ihren Datenbankeintrag selektiert und wieder gelöscht. 

application.yaml:
```
de.muenchen.mobidam:
  archive:
    expiration-months: 1
```

Die Löschzeitpunkte können konfiguriert werden.
application.yaml:
```
camel:
  route:
    delete-archive: quartz://mobidam/archiveCleanUp?cron=0+30+2+?+*+*

```

## S3 EAI Datenbank
Datenbankeintrag enthält Angaben zum
- Bucket
- Path/Objektname (z.Bsp. archive/Pfad1/Pfad2/Datei )
- Erstelldatum des Eintrags
- Ablaufdatum des Eintrags.

application.yaml:
```
spring:
  datasource:
    url: ...
    username: ...
    password: ...
    driver-class-name: ...

```

## Grafana
Die Mobilithek Grafana Dashboards für die Umgebungen _mobidam-dev_ und _mobidam_ sind  über den 'CAP Grafana Overview' der jeweiligen Umgebungen erreichbar. Die URLs der 'Overviews' finden sich im LHM-CAP-Wiki.
Bislang sind nur für Umgebungen _mobidam-dev_ und _mobidam_ Grafana-Operatoren eingerichtet und Dashboards verfügbar:

- _<mobidam-grafana-url>/Dashboards/<kubernetes-namespace>/mobidam-mobilithek-eai-<environment>-cap_

Von der mobidam-mobilitheks-eai werden verschiedene auf Probleme hinweisende Metriken für die Dashboards zur Auswertung zur Verfügung gestellt:
- Kennwerte die Verarbeitung von Camel Exchanges betreffen.
- Kennwerte die auf Probleme bei einzelnen Schnittstellen hinweisen.
- Kennwerte die die Verarbeitung von Downloads erfassen.

Jede Dashboard Visualisierung stellt Erklärungen zu ihren Inhalten zur Verfügung.

Das Dashboard ist gemäß der Anleitung im LHM-CAP-Wiki als Kubernetes Dashboard Manifest gesichert. **Das hat zwei Auswirkungen
für die Arbeit mit dem Grafana Dashboard zur Folge**:

- Änderungen die man im Grafana Dashboard vornimmt werden durch das Kubernetes Manifeste automatisch wieder überschrieben.
  Um das zu vermeiden über den Dashboard Edit Modus erst eine Kopie des Dashboard erstellen und die Änderungen darin vornehmen.
- Alle Änderungen die in der Kopie des Grafana Dashboard gemacht werden, müssen im Kubernetes Manifest widergespiegelt werden.
  Dazu das _Grafana -> Settings -> JSON Model_ im Kubernetes Dashboard Manifest per Copy-Paste aktualisieren.
  Anschließend werden durch Kubernetes alle Manifest Änderungen wieder mit dem Grafana Dashboard aktualisiert und alle Umgebungen sind wieder up-to-date.

Um Dashboard Aktualisierungen von K auf P zu bringen, die _JSON Model_ Änderungen in den Kubernetes Dashboards Manifeste der Namespaces synchronisieren.
Die Grafana Instanzen auf K und P sind eigenständig. Daher kann die Grafana Dashboard-UID beibehalten werden.
Bei Problemen mit der Dashboard-UID diese einfach neu Vergeben.
Ist bei der Aktualisierung des Grafana Dashboards aus dem Kubenetes Manifest heraus keine Dashboard-UID vorhanden, wird diese automatisch neu generiert.
Die UID kann im Zweifel im Kubernetes Dashboard Manifest also gelöscht werden.

Alle von der mobidam-mobilithek-eai zur Verfügung gestellten Metriken können im Pod-Terminal mit _curl localhost:8080/actuator/prometheus_ angezeigt werden.
Das ist zum Beispiel hilfreich um eine Überblick über alle Metrik Identifier mit ihren Werten zum Abfragezeitunkt zu bekommen.
Einige Schnittstelle spezifische Metriken werden dynamisch erzeugt und sind erst sichtbar wenn die Schnittstelle aktiviert und erfolgreich beendet wurde.
Wenn im Dashboard dazu trotzdem Werte angezeigt werden die in der der curl-Abfrage nicht sichtbar sind, liegt das daran das die Dashboard Übersichten Pod übergreifend ihre Werte ermitteln können.
So sind auch Abweichungen bei den Werten zwischen Pod-Abfrage und Grafana Dashboard zu erklären
