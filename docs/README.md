# Technisches Setup
## Openapi
Um neue Openapi Java Source Dateien zu erstellen kann das Maven Profil _generate-openapi_ verwendet werden (mvn clean compile -P generate-openapi).
Das Profil erzeugt die Openapi Java Source Dateien im Maven _target_ Ordner.
Änderungen und neue Features können in die Klassen im Package _de.muenchen.mobidam.rest_ kopiert werden.

Die Openapi Quelle kann mit dem [Swagger Editor](https://editor.swagger.io) bearbeitet werden.