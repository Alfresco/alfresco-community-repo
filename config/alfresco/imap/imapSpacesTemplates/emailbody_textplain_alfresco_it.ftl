------------------------------------------------------------------------------
Nome documento:   ${document.name}
------------------------------------------------------------------------------

   <#if document.properties.title?exists>
Titolo:   ${document.properties.title}
   <#else>
Titolo:         NESSUNO
   </#if>
   <#if document.properties.description?exists>
Descrizione:   ${document.properties.description}
   <#else>
Descrizione:   NESSUNA
   </#if>
Autore:   ${document.properties.creator}
Data di creazione:   ${document.properties.created?datetime}
Modificatore:  ${document.properties.modifier}
Data di modifica:  ${document.properties.modified?datetime}
Dimensioni:      ${document.size / 1024} Kb


COLLEGAMENTI DEL CONTENUTO

URL del contenuto:      ${document.shareUrl}

