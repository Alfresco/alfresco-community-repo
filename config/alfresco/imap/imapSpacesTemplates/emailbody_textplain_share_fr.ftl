------------------------------------------------------------------------------
Nom du document :   ${document.name}
------------------------------------------------------------------------------

   <#if document.properties.title?exists>
Titre:   ${document.properties.title}
   <#else>
Titre:         AUCUN
   </#if>
   <#if document.properties.description?exists>
Description:   ${document.properties.description}
   <#else>
Description:   AUCUN
   </#if>
Créateur:      ${document.properties.creator}
Créé:          ${document.properties.created?datetime}
Modificateur:  ${document.properties.modifier}
Modifié:       ${document.properties.modified?datetime}
Taille:        ${document.size / 1024} Kb


Liens de contenu

Dossier du contenu :   ${shareContextUrl}/page/site/${parentPathFromSites}
URL de contenu :      ${shareContextUrl}/proxy/alfresco/api/node/content/${document.storeType}/${document.storeId}/${document.id}/${document.name}
Adresse de téléchargement :     ${shareContextUrl}/proxy/alfresco/api/node/content/${document.storeType}/${document.storeId}/${document.id}/${document.name}?a=true