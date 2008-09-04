<#--                         -->
<#-- ATOM Entry for Document -->
<#--                         -->

<#macro document node>
<#-- ATOM syndication -->
<author><name>${node.properties.creator}</name></author>
<content type="${node.mimetype}" src="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/content"/>
<id>urn:uuid:${node.id}</id>
<published>${xmldate(node.properties.created)}</published>
<summary>${node.properties.description!node.properties.title!cropContent(node, 50)}</summary>
<title>${node.name}</title>
<updated>${xmldate(node.properties.modified)}</updated>
<link rel="self" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}" type="entry"/>
<link rel="enclosure" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/content" type="${node.mimetype}"/>
<#-- ATOM Publishing Protocol -->
<app:edited>${xmldate(node.properties.modified)}</app:edited>
<link rel="edit" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}" type="entry"/>
<link rel="edit-media" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/content" type="${node.mimetype}"/>
<#-- Alfresco props -->
<alf:icon>${absurl(url.context)}${node.icon16}</alf:icon>
<#-- CMIS Schema -->
<link rel="cmis-allowableactions" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/permissions" type="entry"/>
<link rel="cmis-relationships" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/associations" type="feed"/>
<link rel="cmis-parents" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/parents" type="feed"/>
<link rel="cmis-allversions" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/versions" type="feed"/>
<link rel="cmis-stream" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/content" type="${node.mimetype}"/>
<cmis:object>
  <cmis:object_id>${node.nodeRef}</cmis:object_id>
  <cmis:baseType>document</cmis:baseType>
  <cmis:createdBy>${node.properties.creator}</cmis:createdBy>
  <cmis:creationDate>${xmldate(node.properties.created)}</cmis:creationDate>
  <cmis:lastModifiedBy>${node.properties.modifier}</cmis:lastModifiedBy>
  <cmis:lastModificationDate>${xmldate(node.properties.modified)}</cmis:lastModificationDate>
  <cmis:contentStreamLength>${node.properties.content.size}</cmis:contentStreamLength>
  <cmis:contentStreamMimetype>${node.properties.content.mimetype}</cmis:contentStreamMimetype>
  <cmis:contentStreamName>${node.name}</cmis:contentStreamName>
  <cmis:contentStreamUri>${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/content</cmis:contentStreamUri>
</cmis:object>
<#-- CMIS LINKS -->
</#macro>

<#--                       -->
<#-- ATOM Entry for Folder -->
<#--                       -->

<#macro folder node>
<#-- ATOM syndication -->
<author><name>${node.properties.creator}</name></author>
<content>${node.id}</content>  <#-- TODO -->
<id>urn:uuid:${node.id}</id>
<published>${xmldate(node.properties.created)}</published>
<summary>${node.properties.description!node.properties.title}</summary>  <#-- TODO -->
<title>${node.name}</title>
<updated>${xmldate(node.properties.modified)}</updated>
<link rel="self" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}" type="entry"/>
<#-- ATOM Publishing Protocol -->
<app:edited>${xmldate(node.properties.modified)}</app:edited>
<link rel="edit" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}" type="entry"/>
<#-- Alfresco props -->
<alf:icon>${absurl(url.context)}${node.icon16}</alf:icon>
<#-- CMIS Schema -->
<link rel="cmis-allowableactions" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/permissions" type="entry"/>
<link rel="cmis-relationships" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/associations" type="feed"/>
<link rel="cmis-parent" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/parent" type="feed"/>
<link rel="cmis-children" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/children" type="feed"/>
<link rel="cmis-descendants" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/descendants" type="feed"/>
<cmis:object>
  <cmis:object_id>${node.nodeRef}</cmis:object_id>
  <cmis:baseType>folder</cmis:baseType>
  <cmis:createdBy>${node.properties.creator}</cmis:createdBy>
  <cmis:creationDate>${xmldate(node.properties.created)}</cmis:creationDate>
  <cmis:lastModifiedBy>${node.properties.modifier}</cmis:lastModifiedBy>
  <cmis:lastModificationDate>${xmldate(node.properties.modified)}</cmis:lastModificationDate>
  <cmis:name>${node.name}</cmis:name>
  <cmis:parent>${node.parent.nodeRef}</cmis:parent>
  <#-- In certain cases, if depth is specified children may appear Here. If there are children, the cmis:object tag will be nested For the children of this item -->
</cmis:object>
</#macro>


<#-- Helper to render Alfresco content type to Atom content type -->
<#macro contenttype type><#if type == "text/html">text<#elseif type == "text/xhtml">xhtml<#elseif type == "text/plain">text<#else>${type}</#if></#macro>
