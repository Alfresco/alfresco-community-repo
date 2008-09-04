<#import "/org/alfresco/cmis/cmis.lib.atom.ftl" as cmisLib/>

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
<link rel="self" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}"/>
<link rel="enclosure" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/content" type="${node.mimetype}"/>
<#-- ATOM Publishing Protocol -->
<app:edited>${xmldate(node.properties.modified)}</app:edited>
<link rel="edit" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}"/>
<link rel="edit-media" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/content" type="${node.mimetype}"/>
<#-- Alfresco props -->
<alf:icon>${absurl(url.context)}${node.icon16}</alf:icon>
</#macro>

<#--                                     -->
<#-- ATOM Entry for Version              -->
<#--                                     -->

<#macro version node version>
<#-- ATOM syndication -->
${version.label}  ${cropContent(node.properties.content, 50)}
<author><name>${node.properties.creator}</name></author>
<content type="${node.mimetype}" src="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/content"/>
<id>urn:uuid:${node.id}</id>
<published>${xmldate(node.properties.created)}</published>
<summary>${node.properties.description!node.properties.title!cropContent(node.properties.content, 50)}</summary>
<title>${node.name}</title>
<updated>${xmldate(node.properties.modified)}</updated>
<link rel="self" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}"/>
<link rel="enclosure" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/content" type="${node.mimetype}"/>
<#-- ATOM Publishing Protocol -->
<app:edited>${xmldate(node.properties.modified)}</app:edited>
<#-- Alfresco props -->
<alf:icon>${absurl(url.context)}${node.icon16}</alf:icon>
</#macro>

<#--                                     -->
<#-- ATOM Entry for Private Working Copy -->
<#--                                     -->

<#macro pwc node>
<#-- ATOM syndication -->
<author><name>${node.properties.creator}</name></author>
<content type="${node.mimetype}" src="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/content"/>
<id>urn:uuid:${node.id}</id>
<published>${xmldate(node.properties.created)}</published>
<summary>${node.properties.description!node.properties.title!cropContent(node.properties.content, 50)}</summary>
<title>${node.name}</title>
<updated>${xmldate(node.properties.modified)}</updated>
<link rel="self" href="${absurl(url.serviceContext)}/api/pwc/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}"/>
<link rel="enclosure" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/content" type="${node.mimetype}"/>
<#-- ATOM Publishing Protocol -->
<app:edited>${xmldate(node.properties.modified)}</app:edited>
<#-- TODO: the edit link refers to the updatable node resource, allowing updates on PWCs without checkin -->
<link rel="edit" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}"/>
<link rel="edit-media" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/content" type="${node.mimetype}"/>
<#-- Alfresco props -->
<alf:icon>${absurl(url.context)}${node.icon16}</alf:icon>
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
<link rel="self" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}"/>
<#-- ATOM Publishing Protocol -->
<app:edited>${xmldate(node.properties.modified)}</app:edited>
<link rel="edit" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}"/>
<#-- Alfresco props -->
<alf:icon>${absurl(url.context)}${node.icon16}</alf:icon>
</#macro>


<#-- Helper to render Alfresco content type to Atom content type -->
<#macro contenttype type><#if type == "text/html">text<#elseif type == "text/xhtml">xhtml<#elseif type == "text/plain">text<#else>${type}</#if></#macro>
