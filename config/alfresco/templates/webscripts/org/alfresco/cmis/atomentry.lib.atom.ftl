[#ftl]

[#--                         --]
[#-- ATOM Entry for Document --]
[#--                         --]

[#macro document node]
<author><name>${node.properties.creator}</name></author>
<content type="${node.mimetype}" src="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/content"/>
<id>urn:uuid:${node.id}</id>
<link rel="self" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}"/>
<link rel="enclosure" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/content" type="${node.mimetype}"/>
<link rel="edit" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}"/>
<link rel="edit-media" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/content" type="${node.mimetype}"/>
[@documentCMISLinks node=node/]
<published>${xmldate(node.properties.created)}</published>
<summary>${node.properties.description!node.properties.title!cropContent(node, 50)}</summary>
<title>${node.name}</title>
<updated>${xmldate(node.properties.modified)}</updated>
[@documentCMISProps node=node/]
<app:edited>${xmldate(node.properties.modified)}</app:edited>
<alf:icon>${absurl(url.context)}${node.icon16}</alf:icon>
[/#macro]

[#macro documentCMISLinks node]
<link rel="cmis-allowableactions" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/permissions"/>
<link rel="cmis-relationships" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/associations"/>
<link rel="cmis-parents" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/parents"/>
<link rel="cmis-allversions" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/versions"/>
<link rel="cmis-stream" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/content" type="${node.mimetype}"/>
[/#macro]

[#macro documentCMISProps node]
<cmis:properties>
  <cmis:propertyDateTime cmis:name="creationDate">${xmldate(node.properties.created)}</cmis:propertyDateTime>
  <cmis:propertyDateTime cmis:name="lastModificationDate">${xmldate(node.properties.modified)}</cmis:propertyDateTime>
  <cmis:propertyID cmis:name="objectId">${node.nodeRef}</cmis:propertyID>
  <cmis:propertyInteger cmis:name="contentStreamLength">${node.properties.content.size}</cmis:propertyInteger>
  <cmis:propertyString cmis:name="name">${node.name}</cmis:propertyString>
  <cmis:propertyString cmis:name="baseType">document</cmis:propertyString>
  <cmis:propertyString cmis:name="objectType">document</cmis:propertyString>  [#-- TODO --]
  <cmis:propertyString cmis:name="createdBy">${node.properties.creator}</cmis:propertyString>
  <cmis:propertyString cmis:name="lastModifiedBy">${node.properties.modifier}</cmis:propertyString>
  <cmis:propertyString cmis:name="contentStreamMimetype">${node.properties.content.mimetype}</cmis:propertyString>  
  <cmis:propertyString cmis:name="contentStreamName">${node.name}</cmis:propertyString>
  <cmis:propertyURI cmis:name="contentStreamURI">${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/content</cmis:propertyURI>
</cmis:properties>
[/#macro]


[#--                        --]
[#-- ATOM Entry for Version --]
[#--                        --]

[#macro version node version]
<author><name>${node.properties.creator}</name></author>
<content type="${node.mimetype}" src="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/content"/>
<id>urn:uuid:${node.id}</id>
<link rel="self" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}"/>
<link rel="enclosure" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/content" type="${node.mimetype}"/>
[@documentCMISLinks node=node/]
<published>${xmldate(node.properties.created)}</published>
<summary>${node.properties.description!node.properties.title!cropContent(node.properties.content, 50)}</summary>
<title>${node.name}</title>
<updated>${xmldate(node.properties.modified)}</updated>
[@documentCMISProps node=node/]
<app:edited>${xmldate(node.properties.modified)}</app:edited>
<alf:icon>${absurl(url.context)}${node.icon16}</alf:icon>
[/#macro]


[#--                                     --]
[#-- ATOM Entry for Private Working Copy --]
[#--                                     --]

[#macro pwc node]
<author><name>${node.properties.creator}</name></author>
<content type="${node.mimetype}" src="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/content"/>
<id>urn:uuid:${node.id}</id>
<link rel="self" href="${absurl(url.serviceContext)}/api/pwc/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}"/>
<link rel="enclosure" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/content" type="${node.mimetype}"/>
<link rel="edit" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}"/>
<link rel="edit-media" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/content" type="${node.mimetype}"/>
[@documentCMISLinks node=node/]
<published>${xmldate(node.properties.created)}</published>
<summary>${node.properties.description!node.properties.title!cropContent(node.properties.content, 50)}</summary>
<title>${node.name}</title>
<updated>${xmldate(node.properties.modified)}</updated>
[@documentCMISProps node=node/]
<app:edited>${xmldate(node.properties.modified)}</app:edited>
[#-- TODO: the edit link refers to the updatable node resource, allowing updates on PWCs without checkin --]
<alf:icon>${absurl(url.context)}${node.icon16}</alf:icon>
[/#macro]


[#--                       --]
[#-- ATOM Entry for Folder --]
[#--                       --]

[#macro folder node]
<author><name>${node.properties.creator}</name></author>
<content>${node.id}</content>  [#-- TODO --]
<id>urn:uuid:${node.id}</id>
<link rel="self" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}"/>
<link rel="edit" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}"/>
[@folderCMISLinks node=node/]
<published>${xmldate(node.properties.created)}</published>
<summary>${node.properties.description!node.properties.title}</summary>  [#-- TODO --]
<title>${node.name}</title>
<updated>${xmldate(node.properties.modified)}</updated>
[@folderCMISProps node=node/]
<app:edited>${xmldate(node.properties.modified)}</app:edited>
<alf:icon>${absurl(url.context)}${node.icon16}</alf:icon>
[/#macro]

[#macro folderCMISLinks node]
<link rel="cmis-allowableactions" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/permissions"/>
<link rel="cmis-relationships" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/associations"/>
<link rel="cmis-parent" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/parent"/>
<link rel="cmis-children" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/children"/>
<link rel="cmis-descendants" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/descendants"/>
[/#macro]

[#macro folderCMISProps node]
<cmis:properties>
  <cmis:propertyDateTime cmis:name="creationDate">${xmldate(node.properties.created)}</cmis:propertyDateTime>
  <cmis:propertyDateTime cmis:name="lastModificationDate">${xmldate(node.properties.modified)}</cmis:propertyDateTime>
  <cmis:propertyID cmis:name="objectId">${node.nodeRef}</cmis:propertyID>
  <cmis:propertyID cmis:name="parent">${node.parent.nodeRef}</cmis:propertyID>
  <cmis:propertyString cmis:name="name">${node.name}</cmis:propertyString>
  <cmis:propertyString cmis:name="objectType">document</cmis:propertyString>  [#-- TODO --]
  <cmis:propertyString cmis:name="baseType">folder</cmis:propertyString>
  <cmis:propertyString cmis:name="createdBy">${node.properties.creator}</cmis:propertyString>
  <cmis:propertyString cmis:name="lastModifiedBy">${node.properties.modifier}</cmis:propertyString>
</cmis:properties>
[/#macro]



[#-- Helper to render Alfresco content type to Atom content type --]
[#macro contenttype type][#if type == "text/html"]text[#elseif type == "text/xhtml"]xhtml[#elseif type == "text/plain"]text<#else>${type}[/#if][/#macro]
