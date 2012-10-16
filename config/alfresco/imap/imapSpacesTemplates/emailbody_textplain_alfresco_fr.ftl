------------------------------------------------------------------------------
Nom du document :   ${document.name}
------------------------------------------------------------------------------

   <#if document.properties.title?exists>
Titre :   ${document.properties.title}
   <#else>
Titre :         AUCUN
   </#if>
   <#if document.properties.description?exists>
Description :   ${document.properties.description}
   <#else>
Description :   AUCUN
   </#if>
Créateur :   ${document.properties.creator}
Créé :   ${document.properties.created?datetime}
Modificateur :  ${document.properties.modifier}
Modifié :  ${document.properties.modified?datetime}
Taille :      ${document.size / 1024} Ko


LIENS DE CONTENU

URL de contenu :      ${document.shareUrl}

