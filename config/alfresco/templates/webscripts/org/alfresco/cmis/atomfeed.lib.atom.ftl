[#ftl]

[#--                     --]
[#-- ATOM Feed (generic) --]
[#--                     --]

[#macro generic id title author="System"]
<author><name>${author}</name></author>
<generator version="${server.version}">Alfresco (${server.edition})</generator>
<icon>${absurl(url.context)}/images/logo/AlfrescoLogo16.ico</icon>
<id>${id}</id>
<link rel="self" href="${absurl(encodeuri(url.full))?xml}"/>
[#nested]  [#-- NOTE: Custom links --]
<title>${title}</title> 
<updated>${xmldate(date)}</updated>
[/#macro]


[#--                    --]
[#-- ATOM Feed for Node --]
[#--                    --]

[#macro node node]
<author><name>${node.properties.creator!""}</name></author> 
<generator version="${server.version}">Alfresco (${server.edition})</generator>
<icon>${absurl(url.context)}/images/logo/AlfrescoLogo16.ico</icon>
<id>urn:uuid:${node.id}</id>
<link rel="self" href="${absurl(encodeuri(url.full))?xml}"/>
<link rel="cmis-source" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}"/>
[#nested]  [#-- NOTE: Custom links --]
<title>${node.name}</title>
<updated>${xmldate(node.properties.modified)}</updated>
[/#macro]


[#--             --]
[#-- CMIS Paging --]
[#--             --]

[#macro hasMore more]
[#if more?is_string && more = "true"]
  <cmis:hasMoreItems>true</cmis:hasMoreItems>
[#elseif more?is_string && more = "false"]
  <cmis:hasMoreItems>false</cmis:hasMoreItems>
[#else]  
  <cmis:hasMoreItems>${more.hasNextPage?string}</cmis:hasMoreItems>
[/#if]
[/#macro]
