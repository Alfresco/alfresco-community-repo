------------------------------------------------------------------------------
Document name:   ${document.name}
------------------------------------------------------------------------------

   <#if document.properties.title?exists>
Title:   ${document.properties.title}
   <#else>
Title:         NONE
   </#if>
   <#if document.properties.description?exists>
Description:   ${document.properties.description}
   <#else>
Description:   NONE
   </#if>
Creator:   ${document.properties.creator}
Created:   ${document.properties.created?datetime}
Modifier:  ${document.properties.modifier}
Modified:  ${document.properties.modified?datetime}
Size:      ${document.size / 1024} Kb


CONTENT LINKS

Content folder:   ${shareContextUrl}/page/site/${parentPathFromSites}
Content URL:      ${shareContextUrl}/proxy/alfresco/api/node/content/${document.storeType}/${document.storeId}/${document.id}/${document.name}
Download URL:     ${shareContextUrl}/proxy/alfresco/api/node/content/${document.storeType}/${document.storeId}/${document.id}/${document.name}?a=true
