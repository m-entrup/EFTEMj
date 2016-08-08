# Diverse Notzizen zu EFTEMj

## Umgang mit Branches

Für Releases habe ich bisher Branches mit der vollständigen Versionsnummer angelegt (z.B. 1.3.0). Beginnend mit Version 1.5.0 werde ich für den Branch nur noch den Namen v1.5 verwenden. Die volle Versionsnummer werde ich in den Tags verwenden, welche Github für das releases Feature nutzt.
Der master Branch wird nach einem Release eines neuen minor Branch (z.B. 1.4.0) auf die nächst höhere minor Version gesetzt (1.4.0-SNAPSHOT -> 1.5.0-SNAPSHOT). Versionen ohne SNAPSHOT sind generell nur für die release Branches vorgesehen. Verbesserungen an den bestehenden Plugins werden als patch Releases in die release Branches eingepflegt. Neue Plugins werden nicht zu einem release Branch hinzugefügt, sondern in separaten Branches, die auf dem aktuellen master Branch aufbauen.

## Curvefitting

### ImageJ

Der CurveFitter von ImageJ nutzt die Simplex-Methode, um Funktionen zu fitten. Es gibt diverse vorgegebene Funktionen und man kann einen String eingeben, um eigene Funktionen zu fitten. Die vorgegebenen Funktionen eignen sich auch für mein Elemental-Mapping Plugin. Eigene Funktionen einzugeben ist jedoch keine gute Idee. Kann der eingegebene String nicht richtig ausgewertet werden, so wird eine Fehlermeldung ausgegeben. Da ich für jeden Pixel eine neue Instanz von CurveFitter erstellen muss, erscheint für jeden Pixel eine Fehlermeldung. Es bleibt einem nur die Möglichkeit ImageJ mit dem Taskmanager zu beenden, wenn man nicht alle Fehlermeldungen (über 1 Million bei einem Bild mit 1024 x 1024 Pixeln) per Hand wegklicken möchte.

Die Klasse Fitter ist das GUI zu CurveFitter.

## Konfigurationsdateien

Als Kandidaten für den Zugriff auf Konfigurationsdateien habe ich zwei Frameworks gefunden. Direkt verwerfen musste ich **[Owner]** von Aeonbits. Dieses Framework kann nur Konfigurationsdateien lesen, jedoch fehlt eine Funktion zum schreiben von Konfigurationsdateien. **[Configuration]** bietet hingegen eine Funktion zum schreiben von Konfigurationsdateien. Das Framework ist Teil von Apache Commons. Sehr gut gefällt mir die Funktion, das man beliebig viele Konfigurationsdateien zu einer Konfiguration vereinen kann. Beim Laden der Konfigurationsdateien wird durch die Reihenfolge festgelegt, wie sich Einträge gegenseitig überschreiben. Für EFTEMj konnte ich so eine Konfiguration mit drei Ebenen erstellen, bei der erst die allgemeine Konfiguration (von EFTEMj-lib) geladen wird, dann die Konfiguration aus dem eintsprechenden Modul (z.B. EFTEMj-ESI) und zuletzt die Konfigurationsdateien des Benutzers (``EFTEMj_config.xml``).

[Owner]: http://owner.aeonbits.org
[Configuration]: https://commons.apache.org/proper/commons-configuration/
