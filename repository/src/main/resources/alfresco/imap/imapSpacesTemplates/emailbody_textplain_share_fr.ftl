------------------------------------------------------------------------------
Nom du document :   ${document.name?html}
------------------------------------------------------------------------------

   <#if document.properties.title?exists>
Titre:   ${document.properties.title}
   <#else>
Titre :         AUCUN
   </#if>
   <#if document.properties.description?exists>
Description :   ${document.properties.description?html}
   <#else>
Description :   AUCUNE
   </#if>
Créateur :      ${document.properties.creator?html}
Créé :          ${document.properties.created?datetime}
Modificateur :  ${document.properties.modifier?html}
Modifié :       ${document.properties.modified?datetime}
Taille :        ${document.size / 1024} Ko


LIENS DU CONTENU

Dossier du contenu :     ${contentFolderUrl?html}
URL du contenu :         ${shareContextUrl}/proxy/alfresco/api/node/content/${document.storeType}/${document.storeId}/${document.id}/${document.name?html}
URL de téléchargement :  ${shareContextUrl}/proxy/alfresco/api/node/content/${document.storeType}/${document.storeId}/${document.id}/${document.name?html}?a=true
