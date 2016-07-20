# Diverse Notzizen zu EFTEMj

## Curvefitting

### ImageJ

Der CurveFitter von ImageJ nutzt die Simplex-Methode, um Funktionen zu fitten. Es gibt diverse vorgegebene Funktionen und man kann einen String eingeben, um eigene Funktionen zu fitten. Die vorgegebenen Funktionen eignen sich auch für mein Elemental-Mapping Plugin. Eigene Funktionen einzugeben ist jedoch keine gute Idee. Kann der eingegebene String nicht richtig ausgewertet werden, so wird eine Fehlermeldung ausgegeben. Da ich für jeden Pixel eine neue Instanz von CurveFitter erstellen muss, erscheint für jeden Pixel eine Fehlermeldung. Es bleibt einem nur die Möglichkeit ImageJ mit dem Taskmanager zu beenden, wenn man nicht alle Fehlermeldungen (über 1 Million bei einem Bild mit 1024 x 1024 Pixeln) per Hand wegklicken möchte.

Die Klasse Fitter ist das GUI zu CurveFitter.

## Konfigurationsdateien

Als Kandidaten für den Zugriff auf Konfigurationsdateien habe ich zwei Frameworks gefunden. Direkt verwerfen musste ich **[Owner]** von Aeonbits. Dieses Framework kann nur Konfigurationsdateien lesen, jedoch fehlt eine Funktion zum schreiben von Konfigurationsdateien. **[Configuration]** bietet hingegen eine Funktion zum schreiben von Konfigurationsdateien. Das Framework ist Teil von Apache Commons. Sehr gut gefällt mir die Funktion, das man beliebig viele Konfigurationsdateien zu einer Konfiguration vereinen kann. Beim Laden der Konfigurationsdateien wird durch die Reihenfolge festgelegt, wie sich Einträge gegenseitig überschreiben. Für EFTEMj konnte ich so eine Konfiguration mit drei Ebenen erstellen, bei der erst die allgemeine Konfiguration (von EFTEMj-lib) geladen wird, dann die Konfiguration aus dem eintsprechenden Modul (z.B. EFTEMj-ESI) und zuletzt die Konfigurationsdateien des Benutzers (``EFTEMj_config.xml``).

[Owner]: http://owner.aeonbits.org
[Configuration]: https://commons.apache.org/proper/commons-configuration/
