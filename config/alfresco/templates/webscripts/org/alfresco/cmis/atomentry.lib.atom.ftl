[#ftl]

[#--                         --]
[#-- ATOM Entry for Document --]
[#--                         --]

[#macro document node propfilter="*"]
<author><name>${node.properties.creator!""}</name></author>
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
[@documentCMISProps node propfilter/]
[#-- TODO: custom ns  <app:edited>${xmldate(node.properties.modified)}</app:edited> --]
[#-- TODO: custom ns  <alf:icon>${absurl(url.context)}${node.icon16}</alf:icon> --]
[/#macro]

[#macro documentCMISLinks node]
<link rel="cmis-allowableactions" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/permissions"/>
<link rel="cmis-relationships" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/associations"/>
<link rel="cmis-parents" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/parents"/>
<link rel="cmis-allversions" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/versions"/>
<link rel="cmis-stream" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/content" type="${node.mimetype}"/>
<link rel="cmis-type" href="${absurl(url.serviceContext)}/api/type/${cmistypeid(node)}"/>
[/#macro]

[#macro documentCMISProps node propfilter]
<cmis:properties>
  [@filter propfilter "IS_IMMUTABLE"][@prop "isImmutable" cmisproperty(node, "IS_IMMUTABLE") "Boolean"/][/@filter]
  [@filter propfilter "IS_LATEST_VERSION"][@prop "isLatestVersion" cmisproperty(node, "IS_LATEST_VERSION") "Boolean"/][/@filter]
  [@filter propfilter "IS_MAJOR_VERSION"][@prop "isMajorVersion" cmisproperty(node, "IS_MAJOR_VERSION") "Boolean"/][/@filter]
  [@filter propfilter "IS_LATEST_MAJOR_VERSION"][@prop "isLatestMajorVersion" cmisproperty(node, "IS_LATEST_MAJOR_VERSION") "Boolean"/][/@filter]
  [@filter propfilter "VERSION_SERIES_IS_CHECKED_OUT"][@prop "isVersionSeriesCheckedOut" cmisproperty(node, "VERSION_SERIES_IS_CHECKED_OUT") "Boolean"/][/@filter]
  [@filter propfilter "CREATION_DATE"][@prop "creationDate" node.properties.created "DateTime"/][/@filter]
  [@filter propfilter "LAST_MODIFICATION_DATE"][@prop "lastModificationDate" node.properties.modified "DateTime"/][/@filter]
  [@filter propfilter "OBJECT_ID"][@prop "objectId" node.nodeRef "ID"/][/@filter]
  [@filter propfilter "VERSION_SERIES_ID"][@prop "versionSeriesID" cmisproperty(node, "VERSION_SERIES_ID") "ID"/][/@filter]
  [@filter propfilter "VERSION_SERIES_CHECKED_OUT_ID"][@prop "versionSeriesCheckedOutID" cmisproperty(node, "VERSION_SERIES_CHECKED_OUT_ID")!"" "ID"/][/@filter]
  [@filter propfilter "CONTENT_STREAM_LENGTH"][@prop "contentStreamLength" node.properties.content.size "Integer"/][/@filter]
  [@filter propfilter "NAME"][@prop "name" node.name "String"/][/@filter]
  [@filter propfilter "ROOT_TYPE_QUERY_NAME"][@prop "baseType" "document" "String"/][/@filter]  [#-- TODO: spec issue 41 --]
  [@filter propfilter "OBJECT_TYPE_ID"][@prop "objectType" cmistypeid(node) "String"/][/@filter]
  [@filter propfilter "CREATED_BY"][@prop "createdBy" node.properties.creator "String"/][/@filter]
  [@filter propfilter "LAST_MODIFIED_BY"][@prop "lastModifiedBy" node.properties.modifier "String"/][/@filter]
  [@filter propfilter "CONTENT_STREAM_MIME_TYPE"][@prop "contentStreamMimetype" node.properties.content.mimetype "String"/][/@filter]
  [@filter propfilter "CONTENT_STREAM_FILENAME"][@prop "contentStreamName" node.name "String"/][/@filter]
  [@filter propfilter "VERSION_LABEL"][@prop "versionLabel" cmisproperty(node, "VERSION_LABEL")!"" "String"/][/@filter]
  [@filter propfilter "VERSION_SERIES_CHECKED_OUT_BY"][@prop "versionSeriesCheckedOutBy" cmisproperty(node, "VERSION_SERIES_CHECKED_OUT_BY")!"" "String"/][/@filter]
  [@filter propfilter "CHECKIN_COMMENT"][@prop "checkinComment" cmisproperty(node, "CHECKIN_COMMENT")!"" "String"/][/@filter]
  [@filter propfilter "CONTENT_STREAM_URI"][@prop "contentStreamURI" absurl(url.serviceContext) + "/api/node/" + node.nodeRef.storeRef.protocol + "/" + node.nodeRef.storeRef.identifier + "/" + node.nodeRef.id + "/content" "String"/][/@filter]
</cmis:properties>
[/#macro]


[#--                        --]
[#-- ATOM Entry for Version --]
[#--                        --]

[#macro version node version propfilter="*"]
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
[@documentCMISProps node propfilter/]
[#-- TODO: custom ns  <app:edited>${xmldate(node.properties.modified)}</app:edited> --]
[#-- TODO: custom ns  <alf:icon>${absurl(url.context)}${node.icon16}</alf:icon>  --]
[/#macro]


[#--                                     --]
[#-- ATOM Entry for Private Working Copy --]
[#--                                     --]

[#macro pwc node propfilter="*"]
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
[@documentCMISProps node propfilter/]
[#-- TODO: custom ns  <app:edited>${xmldate(node.properties.modified)}</app:edited> --]
[#-- TODO: the edit link refers to the updatable node resource, allowing updates on PWCs without checkin --]
[#-- TODO: custom ns  <alf:icon>${absurl(url.context)}${node.icon16}</alf:icon>  --]
[/#macro]


[#--                       --]
[#-- ATOM Entry for Folder --]
[#--                       --]

[#macro folder node propfilter="*"]
<author><name>${node.properties.creator}</name></author>
<content>${node.id}</content>  [#-- TODO --]
<id>urn:uuid:${node.id}</id>
<link rel="self" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}"/>
<link rel="edit" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}"/>
[@folderCMISLinks node/]
<published>${xmldate(node.properties.created)}</published>
<summary>${node.properties.description!node.properties.title!""}</summary>  [#-- TODO --]
<title>${node.name}</title>
<updated>${xmldate(node.properties.modified)}</updated>
[@folderCMISProps node propfilter/]
[#-- TODO: custom ns  <app:edited>${xmldate(node.properties.modified)}</app:edited>  --]
[#-- TODO: custom ns  <alf:icon>${absurl(url.context)}${node.icon16}</alf:icon>  --]
[/#macro]

[#macro folderCMISLinks node]
<link rel="cmis-allowableactions" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/permissions"/>
<link rel="cmis-relationships" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/associations"/>
[#if cmisproperty(node, "PARENT")??]
<link rel="cmis-parent" href="${absurl(url.serviceContext)}/api/node/${node.parent.nodeRef.storeRef.protocol}/${node.parent.nodeRef.storeRef.identifier}/${node.parent.nodeRef.id}"/>
<link rel="cmis-folderparent" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/parent"/>
[/#if]
<link rel="cmis-children" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/children"/>
<link rel="cmis-descendants" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/descendants"/>
<link rel="cmis-type" href="${absurl(url.serviceContext)}/api/type/${cmistypeid(node)}"/>
[/#macro]

[#macro folderCMISProps node propfilter]
<cmis:properties>
  [@filter propfilter "CREATION_DATE"][@prop "creationDate" node.properties.created "DateTime"/][/@filter]
  [@filter propfilter "LAST_MODIFICATION_DATE"][@prop "lastModificationDate" node.properties.modified "DateTime"/][/@filter]
  [@filter propfilter "OBJECT_ID"][@prop "objectId" node.nodeRef "ID"/][/@filter]
  [@filter propfilter "PARENT"][@prop "parent" cmisproperty(node, "PARENT")!"" "ID"/][/@filter]
  [@filter propfilter "NAME"][@prop "name" node.name "String"/][/@filter]
  [@filter propfilter "ROOT_TYPE_QUERY_NAME"][@prop "baseType" "folder" "String"/][/@filter]  [#-- TODO: spec issue 41 --]
  [@filter propfilter "OBJECT_TYPE_ID"][@prop "objectType" cmistypeid(node) "String"/][/@filter]
  [@filter propfilter "CREATED_BY"][@prop "createdBy" node.properties.creator "String"/][/@filter]
  [@filter propfilter "LAST_MODIFIED_BY"][@prop "lastModifiedBy" node.properties.modifier "String"/][/@filter]
</cmis:properties>
[/#macro]


[#--                          --]
[#-- ATOM Entry for Query Row --]
[#--                          --]

[#-- TODO: spec issue 47 --]
[#macro row row]
[#if row.nodes??]
[#assign node = row.nodes?first]
<author><name>${node.properties.creator!""}</name></author>
<content type="${node.mimetype}" src="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/content"/>
<id>urn:uuid:${node.id}</id>
<link rel="self" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}"/>
<title>${node.name}</title>
<updated>${xmldate(node.properties.modified)}</updated>
[/#if]
[#assign values = row.values]
[#if values?size &gt; 0]
<cmis:properties>
[#list values?keys as colname]
  [#assign coltype = row.getColumnType(colname)]
  [@prop colname values[colname] coltype/]
[/#list]
</cmis:properties>
[/#if]
[#-- TODO: custom ns  <alf:icon>${absurl(url.context)}${node.icon16}</alf:icon>  --]
[/#macro]


[#--                                --]
[#-- ATOM Entry for Type Definition --]
[#--                                --]

[#macro typedef typedef includeProperties=true includeInheritedProperties=true]
[#if true]  [#-- TODO: spec issue 40 --]
[@typedefCMISProps typedef includeProperties includeInheritedProperties/]
[#else]
<author><name>${person.properties.userName}</name></author>
<content>${typedef.objectTypeId}</content>  [#-- TODO --]
<id>urn:uuid:type-${typedef.objectTypeId}</id>
<link rel="self" href="${absurl(url.serviceContext)}/api/type/${typedef.objectTypeId}"/>
[@typedefCMISLinks typedef/]
<summary>${typedef.description!typedef.objectTypeDisplayName}</summary>
<title>${typedef.objectTypeDisplayName}</title>
<updated>${xmldate(date)}</updated>  [#-- TODO --]
[@typedefCMISProps typedef includeProperties/]
[/#if]
[/#macro]

[#macro typedefCMISLinks typedef]
<link rel="cmis-type" href="${absurl(url.serviceContext)}/api/type/${typedef.objectTypeId}"/>
<link rel="cmis-parent" href="${absurl(url.serviceContext)}/api/type/${typedef.parentTypeId}"/>
<link rel="cmis-children" href="${absurl(url.serviceContext)}/api/type/${typedef.objectTypeId}/children"/>
<link rel="cmis-descendants" href="${absurl(url.serviceContext)}/api/type/${typedef.objectTypeId}/descendants"/>
[/#macro]

[#macro typedefCMISProps typedef includeProperties=true includeInheritedProperties=true]
<cmis:type xmlns:cmis="http://www.cmis.org/2008/05">  [#-- TODO: spec issue 40, remove ns decl --]
  <cmis:objectId>${typedef.objectTypeId}</cmis:objectId>
  <cmis:baseType>[@cmisBaseType typedef.rootTypeQueryName/]</cmis:baseType>  [#-- TODO: remove spec issue 36 --]
  <cmis:lastModifiedBy>${xmldate(date)}</cmis:lastModifiedBy>  [#-- TODO: remove spec issue 36 --]
  <cmis:creationDate>${xmldate(date)}</cmis:creationDate>  [#-- TODO: remove spec issue 36 --]
  <cmis:queryName>${typedef.objectTypeQueryName}</cmis:queryName>
  <cmis:displayName>[#if typedef.objectTypeDisplayName??]${typedef.objectTypeDisplayName?xml}[/#if]</cmis:displayName>
  <cmis:baseTypeQueryName>${typedef.rootTypeQueryName}</cmis:baseTypeQueryName>
  <cmis:parentId>${typedef.parentTypeId!""}</cmis:parentId>
  <cmis:description>[#if typedef.description??]${typedef.description?xml}[/#if]</cmis:description>
  <cmis:isCreatable>${typedef.creatable?string}</cmis:isCreatable>
  <cmis:isFileable>${typedef.fileable?string}</cmis:isFileable>
  <cmis:isQueryable>${typedef.queryable?string}</cmis:isQueryable>
  <cmis:isControllable>${typedef.controllable?string}</cmis:isControllable>
  <cmis:isVersionable>${typedef.versionable?string}</cmis:isVersionable>
  <cmis:contentStreamAllowed>[@cmisContentStreamAllowed typedef.contentStreamAllowed/]</cmis:contentStreamAllowed>  [#-- TODO: spec issue 37 --]
  [#if includeProperties]
    [#list typedef.propertyDefinitions?values as propdef]
      [#if includeInheritedProperties || !propdef.inherited]
        [@propdefCMISProps propdef/]
      [/#if]
    [/#list]
  [/#if]
</cmis:type>
[/#macro]

[#macro propdefCMISProps propdef]
<cmis:property cmis:id="${propdef.propertyId}">
  <cmis:propertyName>${propdef.propertyName}</cmis:propertyName>
  <cmis:propertyId>${propdef.propertyId}</cmis:propertyId>
  <cmis:displayName>[#if propdef.displayName??]${propdef.displayName?xml}[/#if]</cmis:displayName>
  <cmis:description>[#if propdef.description??]${propdef.description?xml}[/#if]</cmis:description>
  <cmis:isInherited>${propdef.inherited?string}</cmis:isInherited>
  <cmis:propertyType>${propdef.propertyType}</cmis:propertyType>
  <cmis:cardinality>[@cmisCardinality propdef.cardinality/]</cmis:cardinality>
  [#if propdef.maximumLength != -1]
  <cmis:maxLength>${propdef.maximumLength}</cmis:maxLength>
  [/#if]
  [@cmisChoices propdef.choices/]
  <cmis:isOpenChoice>${propdef.openChoice?string}</cmis:isOpenChoice>
  <cmis:isRequired>${propdef.required?string}</cmis:isRequired>
  <cmis:defaultValue>${propdef.defaultValue!""}</cmis:defaultValue>
  <cmis:updateability>[@cmisUpdatability propdef.updatability/]</cmis:updateability> [#-- TODO spec issue 38 --]
  <cmis:isQueryable>${propdef.queryable?string}</cmis:isQueryable>
  <cmis:isOrderable>${propdef.orderable?string}</cmis:isOrderable>
</cmis:property>
[/#macro]


[#--                 --]
[#-- CMIS Properties --]
[#--                 --]

[#macro filter filter value]
[#if filter == "*" || filter?index_of(value) != -1 || filter?matches(value,'i')][#nested][/#if]
[/#macro]

[#macro prop name value type]
[#if type == "String"]
<cmis:propertyString cmis:name="${name}">${value}</cmis:propertyString>
[#elseif type == "Integer"]
<cmis:propertyInteger cmis:name="${name}">${value?c}</cmis:propertyInteger>
[#elseif type == "Decimal"]
<cmis:propertyDecimal cmis:name="${name}">${value?c}</cmis:propertyDecimal>
[#elseif type == "Boolean"]
<cmis:propertyBoolean cmis:name="${name}">${value?string}</cmis:propertyBoolean>
[#elseif type == "DateTime"]
<cmis:propertyDateTime cmis:name="${name}">${xmldate(value)}</cmis:propertyDateTime>
[#elseif type == "URI"]
<cmis:propertyURI cmis:name="${name}">${value}</cmis:propertyURI>
[#elseif type == "ID"]
<cmis:propertyID cmis:name="${name}">${value}</cmis:propertyID>
[#-- TODO: remaining property types --]
[/#if]
[/#macro]


[#-- TODO: spec issue 40 --]
[#macro cmisBaseType rootType]
[#if rootType = "DOCUMENT_OBJECT_TYPE"]document[#elseif rootType = "FOLDER_OBJECT_TYPE"]folder[#elseif rootType = "RELATIONSHIP_OBJECT_TYPE"]relationship[#elseif rootType = "POLICY_OBJECT_TYPE"]policy[#else][/#if][/#macro]

[#-- TODO: spec issue 37 --]
[#macro cmisContentStreamAllowed allowed]
[#if allowed = "NOT_ALLOWED"]notallowed[#elseif allowed = "ALLOWED"]allowed[#elseif allowed = "REQUIRED"]required[#else][/#if][/#macro]

[#-- TODO: spec issue 37 --]
[#macro cmisCardinality cardinality]
[#if cardinality = "SINGLE_VALUED"]Single[#elseif cardinality = "MULTI_VALUED"]Multi[#else][/#if][/#macro]

[#-- TODO: spec issue 37/38 --]
[#macro cmisUpdatability updatability]
[#if updatability = "READ_ONLY"]ro[#elseif updatability = "READ_AND_WRITE"]rw[#elseif updatability = "READ_AND_WRITE_WHEN_CHECKED_OUT"]checkedout[/#if][/#macro]

[#-- TODO: spec issue 39 --]
[#macro cmisChoices choices]
[#if choices?exists]
[#list choices as choice]
<cmis:choices index="${choice.index}">${choice.value}
[@cmisChoices choice.children/]
</cmis:choices>
[/#list]
[/#if]
[/#macro]


[#-- Helper to render Alfresco content type to Atom content type --]
[#macro contenttype type][#if type == "text/html"]text[#elseif type == "text/xhtml"]xhtml[#elseif type == "text/plain"]text<#else>${type}[/#if][/#macro]
