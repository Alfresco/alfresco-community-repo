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

Content folder:   ${contextUrl}/navigate/browse${document.displayPath}
Content URL:      ${contextUrl}${document.url}
Download URL:     ${contextUrl}${document.downloadUrl}
WebDAV URL:       ${contextUrl}${document.webdavUrl}

START WORKFLOW

{It is not possible to create a customizible workflow in txt format!
 It is possible to create static links to IMAP Workflow Handler webscript,
 But, in this case all parameters must be hardcoded in the link.
 See http://localhost:8080/alfresco/service/description/org/alfresco/imap/start-workflow.get
 for usage information.}  