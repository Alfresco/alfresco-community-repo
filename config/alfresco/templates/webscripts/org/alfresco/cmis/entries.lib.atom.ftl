<#macro document node>
<#-- ATOM props -->
<title>${node.name}</title>
<id>urn:uuid:${node.id}</id>
<updated>${xmldate(node.properties.modified)}</updated>
<published>${xmldate(node.properties.created)}</published>
<summary>${node.properties.description!""}</summary>
<author><name>${node.properties.creator}</name></author> 
<#-- Alfresco props -->
<alf:noderef>${node.nodeRef}</alf:noderef>
<alf:icon>${absurl(url.context)}${node.icon16}</alf:icon>
<#-- CMIS Schema -->
<cmis:object>
  <cmis:object_id>${node.id}</cmis:object_id>
  <cmis:baseType>document</cmis:baseType>
  <cmis:uri>http://example.org/atom03</cmis:uri>  <#-- TODO: -->
  <cmis:objectType>document</cmis:objectType>  <#-- TODO: -->
  <cmis:createdBy>Al Brown</cmis:createdBy>  <#-- TODO: -->
  <cmis:creationDate>2003-12-13T18:30:02Z</cmis:creationDate>  <#-- TODO: -->
  <cmis:lastModifiedBy>Al Brown</cmis:lastModifiedBy>  <#-- TODO: -->
  <cmis:lastModificationDate>2003-12-13T18:30:02Z</cmis:lastModificationDate>  <#-- TODO: -->
  <cmis:isCurrentVersion>true</cmis:isCurrentVersion>  <#-- TODO: -->
  <cmis:isCheckedOut>false</cmis:isCheckedOut>  <#-- TODO: -->
  <cmis:contentStreamLength>534</cmis:contentStreamLength>  <#-- TODO: -->
  <cmis:contentStreamMimetype>application/msword</cmis:contentStreamMimetype>  <#-- TODO: -->
  <cmis:contentStreamName>myDocument.doc</cmis:contentStreamName>  <#-- TODO: -->
  <cmis:contentStreamUri>http://example.org/media/atom03</cmis:contentStreamUri>  <#-- TODO: -->
  <cmis:keyword cmis:index="0">XML</cmis:keyword>  <#-- TODO: -->
  <cmis:keyword cmis:index="1">standards</cmis:keyword>  <#-- TODO: -->
</cmis:object>
<#-- TODO: ATOM LINKS -->
<link rel="self" href="${absurl(url.full)}"/>
<link rel="alternate" href="${absurl(url.serviceContext)}${node.url}"/> <#-- TODO: -->
<#-- CMIS LINKS -->
<link rel="cmis-parents" href="http://example.org/atom03?getdocumentparents"/> <#-- TODO: -->
<link rel="cmis-allowableactions" href="http://example.org/atom03?getactions"/> <#-- TODO: -->
<link rel="cmis-allversions" href="http://example.org/atom03?deleteallversion"/> <#-- TODO: -->
<link rel="cmis-relationships" href="http://example.org/atom03?getrelationships"/> <#-- TODO: -->
<link rel="cmis-type" href="http://example.org/type1"/> <#-- TODO: -->
<link rel="cmis-stream" href="http://example.org/media/atom03"/> <#-- TODO: -->
</#macro>

<#macro folder node>
<#-- ATOM props -->
<title>${node.name}</title>
<link rel="alternate" href="${absurl(url.serviceContext)}${node.url}"/>
<id>urn:uuid:${node.id}</id>
<updated>${xmldate(node.properties.modified)}</updated>
<published>${xmldate(node.properties.created)}</published>
<summary>${node.properties.description!""}</summary>
<author><name>${node.properties.creator!""}</name></author> 
<#-- Alfresco props -->
<alf:noderef>${node.nodeRef}</alf:noderef>
<alf:icon>${absurl(url.context)}${node.icon16}</alf:icon>
<#-- CMIS Schema -->
<cmis:object>
  <cmis:object_id>${node.id}</cmis:object_id>
  <cmis:baseType>folder</cmis:baseType>
  <cmis:uri>http://example.org/folders/myfolder</cmis:uri>  <#-- TODO: -->
  <cmis:objectType>folder</cmis:objectType>  <#-- TODO: -->
  <cmis:baseType>folder</cmis:baseType>  <#-- TODO: -->
  <cmis:createdBy>Al Brown</cmis:createdBy>  <#-- TODO: -->
  <cmis:creationDate>2003-12-13T18:30:02Z</cmis:creationDate>  <#-- TODO: -->
  <cmis:lastModifiedBy>Al Brown</cmis:lastModifiedBy>  <#-- TODO: -->
  <cmis:lastModificationDate> 2003-12-13T18:30:02Z</cmis:lastModificationDate>  <#-- TODO: -->
  <cmis:name>Atom-powered folders</cmis:name>  <#-- TODO: -->
  <cmis:parent>folderIdp1</cmis:parent>   <#-- TODO: -->
  <#-- In certain cases, if depth is specified children may appear Here. If there are children, the cmis:object tag will be nested For the children of this item -->
</cmis:object>
<#-- TODO: ATOM LINKS -->
<link rel="self" href="${absurl(url.full)}"/>
<#-- CMIS LINKS -->
<link rel="cmis-parent" href="http://example.org/folders/myfolder?getfolderparent"/>  <#-- TODO: -->
<link rel="cmis-allowableactions" href="http://example.org/folders/myfolder?getactions"/>  <#-- TODO: -->
<link rel="cmis-relationships" href="http://example.org/folders/myfolder?getrelationships"/>  <#-- TODO: -->
<link rel="cmis-children" href="http://example.org/folders/myfolder?getchildren"/>  <#-- TODO: -->
<link rel="cmis-descendants" href="http://example.org/folders/myfolder?getdescendants"/>  <#-- TODO: -->
<link rel="cmis-type" href="http://example.org/type1"/>  <#-- TODO: -->
</#macro>