------------------------------------------------------------------------------
Dokumentname:   ${document.name}
------------------------------------------------------------------------------

   <#if document.properties.title?exists>
Titel:   ${document.properties.title}
   <#else>
Titel:         KEINER
   </#if>
   <#if document.properties.description?exists>
Beschreibung:   ${document.properties.description}
   <#else>
Beschreibung:   KEINE
   </#if>
Ersteller:   ${document.properties.creator}
Erstellt am:   ${document.properties.created?datetime}
Bearbeiter:  ${document.properties.modifier}
Bearbeitet am:  ${document.properties.modified?datetime}
Größe:      ${document.size / 1024} Kb


LINKS ZUM INHALT

URL zum Inhalt:   ${document.shareUrl}

