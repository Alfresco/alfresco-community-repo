<#--                              -->
<#-- CMIS Extensions for Document -->
<#--                              -->

<#macro document node>
<link rel="cmis-allowableactions" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/permissions"/>
<link rel="cmis-relationships" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/associations"/>
<link rel="cmis-parents" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/parents"/>
<link rel="cmis-allversions" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/versions"/>
<link rel="cmis-stream" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/content" type="${node.mimetype}"/>
<cmis:properties>
  <cmis:objectId>${node.nodeRef}</cmis:objectId>
  <cmis:baseType>document</cmis:baseType>
  <cmis:createdBy>${node.properties.creator}</cmis:createdBy>
  <cmis:creationDate>${xmldate(node.properties.created)}</cmis:creationDate>
  <cmis:lastModifiedBy>${node.properties.modifier}</cmis:lastModifiedBy>
  <cmis:lastModificationDate>${xmldate(node.properties.modified)}</cmis:lastModificationDate>
  <cmis:contentStreamLength>${node.properties.content.size}</cmis:contentStreamLength>
  <cmis:contentStreamMimetype>${node.properties.content.mimetype}</cmis:contentStreamMimetype>
  <cmis:contentStreamName>${node.name}</cmis:contentStreamName>
  <cmis:contentStreamUri>${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/content</cmis:contentStreamUri>
</cmis:properties>
</#macro>

<#--                              -->
<#-- CMIS Extensions for Version  -->
<#--                              -->

<#macro version node version>
<link rel="cmis-allowableactions" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/permissions"/>
<link rel="cmis-relationships" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/associations"/>
<link rel="cmis-parents" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/parents"/>
<link rel="cmis-allversions" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/versions"/>
<link rel="cmis-stream" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/content" type="${node.mimetype}"/>
<cmis:properties>
  <cmis:objectId>${node.nodeRef}</cmis:objectId>
  <cmis:baseType>document</cmis:baseType>
  <cmis:createdBy>${node.properties.creator}</cmis:createdBy>
  <cmis:creationDate>${xmldate(node.properties.created)}</cmis:creationDate>
  <cmis:lastModifiedBy>${node.properties.modifier}</cmis:lastModifiedBy>
  <cmis:lastModificationDate>${xmldate(node.properties.modified)}</cmis:lastModificationDate>
  <cmis:contentStreamLength>${node.properties.content.size}</cmis:contentStreamLength>
  <cmis:contentStreamMimetype>${node.properties.content.mimetype}</cmis:contentStreamMimetype>
  <cmis:contentStreamName>${node.name}</cmis:contentStreamName>
  <cmis:contentStreamUri>${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/content</cmis:contentStreamUri>
</cmis:properties>
</#macro>

<#--                            -->
<#-- CMIS Extensions for Folder -->
<#--                            -->

<#macro folder node>
<link rel="cmis-allowableactions" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/permissions"/>
<link rel="cmis-relationships" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/associations"/>
<link rel="cmis-parent" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/parent"/>
<link rel="cmis-children" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/children"/>
<link rel="cmis-descendants" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/descendants"/>
<cmis:properties>
  <cmis:objectId>${node.nodeRef}</cmis:objectId>
  <cmis:baseType>folder</cmis:baseType>
  <cmis:createdBy>${node.properties.creator}</cmis:createdBy>
  <cmis:creationDate>${xmldate(node.properties.created)}</cmis:creationDate>
  <cmis:lastModifiedBy>${node.properties.modifier}</cmis:lastModifiedBy>
  <cmis:lastModificationDate>${xmldate(node.properties.modified)}</cmis:lastModificationDate>
  <cmis:name>${node.name}</cmis:name>
  <cmis:parent>${node.parent.nodeRef}</cmis:parent>
</cmis:properties>
</#macro>