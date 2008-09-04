[#ftl]

[#--            --]
[#-- ATOM Entry --]
[#--            --]

[#macro entry ns=""]
<entry[#if ns != ""] ${ns}[/#if]>
[#nested]
</entry>
[/#macro]


[#--                         --]
[#-- ATOM Entry for Document --]
[#--                         --]

[#macro document node propfilter="*" ns=""]
[@entry ns]
<author><name>${node.properties.creator!""}</name></author>
<content type="${node.mimetype}" src="[@contentlink node/]"/>
<id>urn:uuid:${node.id}</id>
<link rel="self" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}"/>
<link rel="enclosure" href="[@contentlink node/]" type="${node.mimetype}"/>
<link rel="edit" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}"/>
<link rel="edit-media" href="[@contentlink node/]" type="${node.mimetype}"/>
[@documentCMISLinks node=node/]
<published>${xmldate(node.properties.created)}</published>
<summary>${node.properties.description!node.properties.title!cropContent(node, 50)}</summary>
<title>${node.name}</title>
<updated>${xmldate(node.properties.modified)}</updated>
<cmis:object>
[@documentCMISProps node propfilter/]
</cmis:object>
<cmis:terminator/>
<app:edited>${xmldate(node.properties.modified)}</app:edited>
<alf:icon>${absurl(url.context)}${node.icon16}</alf:icon>
[/@entry]
[/#macro]

[#macro documentCMISLinks node]
<link rel="cmis-allowableactions" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/permissions"/>
<link rel="cmis-relationships" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/associations"/>
<link rel="cmis-parents" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/parents"/>
<link rel="cmis-allversions" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/versions"/>
<link rel="cmis-stream" href="[@contentlink node/]" type="${node.mimetype}"/>
<link rel="cmis-type" href="${absurl(url.serviceContext)}/api/type/${cmistypeid(node)}"/>
<link rel="cmis-repository" href="[@servicelink/]"/>
[/#macro]

[#macro documentCMISProps node propfilter]
<cmis:properties>
  [@filter propfilter "ObjectId"][@prop "ObjectId" node "ID"/][/@filter]
  [#-- TODO: Spec issue - add root type id to meta-model --]
  [@filter propfilter "BaseType"][@propvalue "BaseType" "document" "STRING"/][/@filter]
  [@filter propfilter "ObjectTypeId"][@prop "ObjectTypeId" node "STRING"/][/@filter]
  [@filter propfilter "CreatedBy"][@prop "CreatedBy" node "STRING"/][/@filter]
  [@filter propfilter "CreationDate"][@prop "CreationDate" node "DATETIME"/][/@filter]
  [@filter propfilter "LastModifiedBy"][@prop "LastModifiedBy" node "STRING"/][/@filter]
  [@filter propfilter "LastModificationDate"][@prop "LastModificationDate" node "DATETIME"/][/@filter]
  [#-- TODO: ChangeToken --]
  [@filter propfilter "Name"][@prop "Name" node "STRING"/][/@filter]
  [@filter propfilter "IsImmutable"][@prop "IsImmutable" node "BOOLEAN"/][/@filter]
  [@filter propfilter "isLatestVersion"][@prop "IsLatestVersion" node "BOOLEAN"/][/@filter]
  [@filter propfilter "IsMajorVersion"][@prop "IsMajorVersion" node "BOOLEAN"/][/@filter]
  [@filter propfilter "isLatestMajorVersion"][@prop "IsLatestMajorVersion" node "BOOLEAN"/][/@filter]
  [@filter propfilter "VersionLabel"][@prop "VersionLabel" node "STRING"/][/@filter]
  [@filter propfilter "VersionSeriesId"][@prop "VersionSeriesId" node "ID"/][/@filter]
  [@filter propfilter "IsVersionSeriesCheckedOut"][@prop "IsVersionSeriesCheckedOut" node "BOOLEAN"/][/@filter]
  [@filter propfilter "VersionSeriesCheckedOutBy"][@prop "VersionSeriesCheckedOutBy" node "STRING"/][/@filter]
  [@filter propfilter "VersionSeriesCheckedOutId"][@prop "VersionSeriesCheckedOutId" node "ID"/][/@filter]
  [@filter propfilter "CheckinComment"][@prop "CheckinComment" node "STRING"/][/@filter]
  [#-- TODO: ContentStreamAllowed --]
  [@filter propfilter "ContentStreamLength"][@prop "ContentStreamLength" node "INTEGER"/][/@filter]
  [@filter propfilter "ContentStreamMimeType"][@prop "ContentStreamMimeType" node "STRING"/][/@filter]
  [@filter propfilter "ContentStreamFilename"][@prop "ContentStreamFilename" node "STRING"/][/@filter]
  [@filter propfilter "ContentStreamURI"][@propvalue "ContentStreamURI" absurl(url.serviceContext) + "/api/node/" + node.nodeRef.storeRef.protocol + "/" + node.nodeRef.storeRef.identifier + "/" + node.nodeRef.id + "/content." + node.properties.name "STRING"/][/@filter]
</cmis:properties>
[/#macro]


[#--                        --]
[#-- ATOM Entry for Version --]
[#--                        --]

[#macro version node version propfilter="*" ns=""]
[@entry ns]
<author><name>${node.properties.creator}</name></author>
<content type="${node.mimetype}" src="[@contentlink node/]"/>
<id>urn:uuid:${node.id}</id>
<link rel="self" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}"/>
<link rel="enclosure" href="[@contentlink node/]" type="${node.mimetype}"/>
[@documentCMISLinks node=node/]
<published>${xmldate(node.properties.created)}</published>
<summary>${node.properties.description!node.properties.title!cropContent(node.properties.content, 50)}</summary>
<title>${node.name}</title>
<updated>${xmldate(node.properties.modified)}</updated>
<cmis:object>
[@documentCMISProps node propfilter/]
</cmis:object>
<cmis:terminator/>
<app:edited>${xmldate(node.properties.modified)}</app:edited>
<alf:icon>${absurl(url.context)}${node.icon16}</alf:icon>
[/@entry]
[/#macro]


[#--                                     --]
[#-- ATOM Entry for Private Working Copy --]
[#--                                     --]

[#macro pwc node propfilter="*" ns=""]
[@entry ns]
<author><name>${node.properties.creator}</name></author>
<content type="${node.mimetype}" src="[@contentlink node/]"/>
<id>urn:uuid:${node.id}</id>
<link rel="self" href="${absurl(url.serviceContext)}/api/pwc/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}"/>
<link rel="enclosure" href="[@contentlink node/]" type="${node.mimetype}"/>
<link rel="edit" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}"/>
<link rel="edit-media" href="[@contentlink node/]" type="${node.mimetype}"/>
[@documentCMISLinks node=node/]
<published>${xmldate(node.properties.created)}</published>
<summary>${node.properties.description!node.properties.title!cropContent(node.properties.content, 50)}</summary>
<title>${node.name}</title>
<updated>${xmldate(node.properties.modified)}</updated>
<cmis:object>
[@documentCMISProps node propfilter/]
</cmis:object>
<cmis:terminator/>
<app:edited>${xmldate(node.properties.modified)}</app:edited>
[#-- TODO: the edit link refers to the updatable node resource, allowing updates on PWCs without checkin --]
<alf:icon>${absurl(url.context)}${node.icon16}</alf:icon>
[/@entry]
[/#macro]


[#--                       --]
[#-- ATOM Entry for Folder --]
[#--                       --]

[#macro folder node propfilter="*" typesfilter="any" ns="" depth=1 maxdepth=1]
[@entry ns]
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
<cmis:object>
[#-- recurse for depth greater than 1 --]
[@folderCMISProps node propfilter/]
</cmis:object>
[#if depth < maxdepth || depth == -1]
[#list cmischildren(node, typesfilter) as child]
  [#if child.isDocument]
    [@entryLib.document child propfilter/]
  [#else]
    [@entryLib.folder child propfilter/]
    [@folder child propfilter typesfilter ns depth+1 maxdepth/]
  [/#if]
[/#list]
[/#if]
<cmis:terminator/>
<app:edited>${xmldate(node.properties.modified)}</app:edited>
<alf:icon>${absurl(url.context)}${node.icon16}</alf:icon>
[/@entry]
[/#macro]

[#macro folderCMISLinks node]
<link rel="cmis-allowableactions" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/permissions"/>
<link rel="cmis-relationships" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/associations"/>
[#if cmisproperty(node, "ParentId")??]
<link rel="cmis-parent" href="${absurl(url.serviceContext)}/api/node/${node.parent.nodeRef.storeRef.protocol}/${node.parent.nodeRef.storeRef.identifier}/${node.parent.nodeRef.id}"/>
<link rel="cmis-folderparent" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/parent"/>
[/#if]
<link rel="cmis-children" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/children"/>
<link rel="cmis-descendants" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/descendants"/>
<link rel="cmis-type" href="${absurl(url.serviceContext)}/api/type/${cmistypeid(node)}"/>
<link rel="cmis-repository" href="[@servicelink/]"/>
[/#macro]

[#macro folderCMISProps node propfilter]
<cmis:properties>
  [@filter propfilter "ObjectId"][@prop "ObjectId" node "ID"/][/@filter]
  [#-- TODO: Spec issue - add root type id to meta-model --]
  [@filter propfilter "BaseType"][@propvalue "BaseType" "folder" "STRING"/][/@filter]
  [@filter propfilter "ObjectTypeId"][@prop "ObjectTypeId" node "STRING"/][/@filter]
  [@filter propfilter "CreatedBy"][@prop "CreatedBy" node "STRING"/][/@filter]
  [@filter propfilter "CreationDate"][@prop "CreationDate" node "DATETIME"/][/@filter]
  [@filter propfilter "LastModifiedBy"][@prop "LastModifiedBy" node "STRING"/][/@filter]
  [@filter propfilter "LastModificationDate"][@prop "LastModificationDate" node "DATETIME"/][/@filter]
  [@filter propfilter "Name"][@prop "Name" node "STRING"/][/@filter]
  [@filter propfilter "ParentId"][@prop "ParentId" node "ID"/][/@filter]
</cmis:properties>
[/#macro]


[#--                          --]
[#-- ATOM Entry for Query Row --]
[#--                          --]

[#-- TODO: spec issue 47 --]
[#macro row row]
[@entry]
[#if row.nodes??]
[#assign node = row.nodes?first]
<author><name>${node.properties.creator!""}</name></author>
<content type="${node.mimetype}" src="[@contentlink node/]"/>
<id>urn:uuid:${node.id}</id>
<link rel="self" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}"/>
<title>${node.name}</title>
<updated>${xmldate(node.properties.modified)}</updated>
[/#if]
<cmis:object>
<cmis:properties>

[#-- TODO: spec issue: baseType to become formal property --]
[#if node??]
  [#if node.isDocument]
    [@propvalue "BaseType" "document" "STRING"/]
  [#else]
    [@propvalue "BaseType" "folder" "STRING"/]
  [/#if]    
[/#if]

[#assign values = row.values]
[#list values?keys as colname]
  [#assign coltype = row.getColumnType(colname)]
  [#if values[colname]??]
  [@propvalue colname values[colname] coltype/]
  [#else]
  [@propnull colname coltype/]
  [/#if]
[/#list]
</cmis:properties>
</cmis:object>
<cmis:terminator/>
<alf:icon>${absurl(url.context)}${node.icon16}</alf:icon>
[/@entry]
[/#macro]


[#--                 --]
[#-- CMIS Properties --]
[#--                 --]

[#macro filter filter value]
[#if filter == "*" || filter?index_of(value) != -1 || filter?matches(value,'i')][#nested][/#if]
[/#macro]

[#macro prop name node type]
[#-- TODO: Freemarker doesn't support NULL - better workaround required --]
[#assign value=cmisproperty(node, name)!"__N_U_L_L__"/]
[#if value?is_string && value == "__N_U_L_L__"]
[@propnull name type/]
[#else]
[@propvalue name value type/]
[/#if]
[/#macro]

[#macro propvalue name value type]
[#if type == "STRING"]
<cmis:propertyString cmis:name="${name}">[@stringvalue value/]</cmis:propertyString>
[#elseif type == "INTEGER"]
<cmis:propertyInteger cmis:name="${name}">[@integervalue value/]</cmis:propertyInteger>
[#elseif type == "DECIMAL"]
<cmis:propertyDecimal cmis:name="${name}">[@decimalvalue value/]</cmis:propertyDecimal>
[#elseif type == "BOOLEAN"]
<cmis:propertyBoolean cmis:name="${name}">[@booleanvalue value/]</cmis:propertyBoolean>
[#elseif type == "DATETIME"]
<cmis:propertyDateTime cmis:name="${name}">[@datetimevalue value/]</cmis:propertyDateTime>
[#elseif type == "URI"]
<cmis:propertyUri cmis:name="${name}">[@urivalue value/]</cmis:propertyUri>
[#elseif type == "ID"]
<cmis:propertyId cmis:name="${name}">[@idvalue value/]</cmis:propertyId>
[#-- TODO: remaining property types --]
[/#if]
[/#macro]

[#macro propnull name type]
[#if type == "STRING"]
<cmis:propertyString cmis:name="${name}"/>
[#elseif type == "INTEGER"]
<cmis:propertyInteger cmis:name="${name}"/>
[#elseif type == "DECIMAL"]
<cmis:propertyDecimal cmis:name="${name}"/>
[#elseif type == "BOOLEAN"]
<cmis:propertyBoolean cmis:name="${name}"/>
[#elseif type == "DATETIME"]
<cmis:propertyDateTime cmis:name="${name}"/>
[#elseif type == "URI"]
<cmis:propertyUri cmis:name="${name}"/>
[#elseif type == "ID"]
<cmis:propertyId cmis:name="${name}"/>
[#-- TODO: remaining property types --]
[/#if]
[/#macro]


[#--             --]
[#-- CMIS Values --]
[#--             --]

[#macro stringvalue value]<cmis:value>${value}</cmis:value>[/#macro]
[#macro integervalue value]<cmis:value>${value?c}</cmis:value>[/#macro]
[#macro decimalvalue value]<cmis:value>${value?c}</cmis:value>[/#macro]
[#macro booleanvalue value]<cmis:value>${value?string}</cmis:value>[/#macro]
[#macro datetimevalue value]<cmis:value>${xmldate(value)}</cmis:value>[/#macro]
[#macro urivalue value]<cmis:value>${value}</cmis:value>[/#macro]
[#macro idvalue value]<cmis:value>${value}</cmis:value>[/#macro]


[#--                                --]
[#-- ATOM Entry for Type Definition --]
[#--                                --]

[#macro typedef typedef includeProperties=true includeInheritedProperties=true ns=""]
[@entry ns=ns]
<author><name>${person.properties.userName}</name></author>
<content>${typedef.objectTypeId}</content>  [#-- TODO --]
<id>urn:uuid:type-${typedef.objectTypeId}</id>
<link rel="self" href="${absurl(url.serviceContext)}/api/type/${typedef.objectTypeId}"/>
[@typedefCMISLinks typedef/]
<summary>[#if typedef.description??]${typedef.description?xml}[#else]${typedef.objectTypeDisplayName?xml}[/#if]</summary>
<title>${typedef.objectTypeDisplayName}</title>
<updated>${xmldate(date)}</updated>  [#-- TODO --]
[@typedefCMISProps typedef includeProperties/]
<cmis:terminator/>
[/@entry]
[/#macro]

[#macro typedefCMISLinks typedef]
<link rel="cmis-type" href="${absurl(url.serviceContext)}/api/type/${typedef.objectTypeId}"/>
[#if typedef.parentTypeId??]
<link rel="cmis-parent" href="${absurl(url.serviceContext)}/api/type/${typedef.parentTypeId}"/>
[/#if]
<link rel="cmis-children" href="${absurl(url.serviceContext)}/api/type/${typedef.objectTypeId}/children"/>
<link rel="cmis-descendants" href="${absurl(url.serviceContext)}/api/type/${typedef.objectTypeId}/descendants"/>
<link rel="cmis-repository" href="[@servicelink/]"/>
[/#macro]

[#macro typedefCMISProps typedef includeProperties=true includeInheritedProperties=true]
[#if typedef.rootTypeId.toString() = "document"]
[@documenttypedefCMISProps typedef includeProperties includeInheritedProperties/]
[#elseif typedef.rootTypeId.toString() = "folder"]
[@foldertypedefCMISProps typedef includeProperties includeInheritedProperties/]
[#elseif typedef.rootTypeId.toString() = "relationship"]
[@relationshiptypedefCMISProps typedef includeProperties includeInheritedProperties/]
[#elseif typedef.rootTypeId.toString() = "policy"]
[@policytypedefCMISProps typedef includeProperties includeInheritedProperties/]
[/#if]
[/#macro]

[#macro documenttypedefCMISProps typedef includeProperties=true includeInheritedProperties=true]
<cmis:documentType>
  [@objecttypedefCMISProps typedef includeProperties includeInheritedProperties/]
  <cmis:versionable>${typedef.versionable?string}</cmis:versionable>
  <cmis:contentStreamAllowed>${typedef.contentStreamAllowed.label}</cmis:contentStreamAllowed>
</cmis:documentType>
[/#macro]

[#macro foldertypedefCMISProps typedef includeProperties=true includeInheritedProperties=true]
<cmis:folderType>
  [@objecttypedefCMISProps typedef includeProperties includeInheritedProperties/]
</cmis:folderType>
[/#macro]

[#macro relationshiptypedefCMISProps typedef includeProperties=true includeInheritedProperties=true]
<cmis:relationshipType>
  [@objecttypedefCMISProps typedef includeProperties includeInheritedProperties/]
  [#-- TODO: source and target types --]
</cmis:relationshipType>
[/#macro]

[#macro policytypedefCMISProps typedef includeProperties=true includeInheritedProperties=true]
<cmis:policyType>
  [@objecttypedefCMISProps typedef includeProperties includeInheritedProperties/]
</cmis:policyType>
[/#macro]

[#macro objecttypedefCMISProps typedef includeProperties=true includeInheritedProperties=true]
  <cmis:typeId>${typedef.objectTypeId}</cmis:typeId>
  <cmis:queryName>${typedef.objectTypeQueryName}</cmis:queryName>
  <cmis:displayName>[#if typedef.objectTypeDisplayName??]${typedef.objectTypeDisplayName?xml}[/#if]</cmis:displayName>
  <cmis:baseType>${typedef.rootTypeId}</cmis:baseType>  [#-- TODO: remove spec issue 36 --]
  <cmis:baseTypeQueryName>${typedef.rootTypeQueryName}</cmis:baseTypeQueryName>
[#if typedef.parentTypeId??]  
  <cmis:parentId>${typedef.parentTypeId}</cmis:parentId>
[/#if]
  <cmis:description>[#if typedef.description??]${typedef.description?xml}[/#if]</cmis:description>
  <cmis:creatable>${typedef.creatable?string}</cmis:creatable>
  <cmis:fileable>${typedef.fileable?string}</cmis:fileable>
  <cmis:queryable>${typedef.queryable?string}</cmis:queryable>
  <cmis:controllable>${typedef.controllable?string}</cmis:controllable>
  <cmis:includedInSupertypeQuery>${typedef.includedInSupertypeQuery?string}</cmis:includedInSupertypeQuery>
  [#if includeProperties]
    [#list typedef.propertyDefinitions?values as propdef]
      [#if includeInheritedProperties || !propdef.inherited]
        [@propdefCMISProps propdef/]
      [/#if]
    [/#list]
  [/#if]
[/#macro]

[#macro propdefCMISProps propdef]
[#if propdef.propertyType == "BOOLEAN"]
[@booleanpropdefCMISProps propdef/]
[#elseif propdef.propertyType == "ID"]
[@idpropdefCMISProps propdef/]
[#elseif propdef.propertyType == "INTEGER"]
[@integerpropdefCMISProps propdef/]
[#elseif propdef.propertyType == "DATETIME"]
[@datetimepropdefCMISProps propdef/]
[#elseif propdef.propertyType == "DECIMAL"]
[@decimalpropdefCMISProps propdef/]
[#elseif propdef.propertyType == "HTML"]
[@htmlpropdefCMISProps propdef/]
[#elseif propdef.propertyType == "STRING"]
[@stringpropdefCMISProps propdef/]
[#elseif propdef.propertyType == "URI"]
[@uripropdefCMISProps propdef/]
[#elseif propdef.propertyType == "XML"]
[@xmlpropdefCMISProps propdef/]
[/#if]
[/#macro]

[#macro booleanpropdefCMISProps propdef]
<cmis:propertyBooleanDefinition>
[@abstractpropdefCMISProps propdef/]
</cmis:propertyBooleanDefinition>
[/#macro]

[#macro idpropdefCMISProps propdef]
<cmis:propertyIdDefinition>
[@abstractpropdefCMISProps propdef/]
</cmis:propertyIdDefinition>
[/#macro]

[#macro integerpropdefCMISProps propdef]
<cmis:propertyIntegerDefinition>
[@abstractpropdefCMISProps propdef/]
[#-- TODO: minValue, maxValue --]
</cmis:propertyIntegerDefinition>
[/#macro]

[#macro datetimepropdefCMISProps propdef]
<cmis:propertyDateTimeDefinition>
[@abstractpropdefCMISProps propdef/]
</cmis:propertyDateTimeDefinition>
[/#macro]

[#macro decimalpropdefCMISProps propdef]
<cmis:propertyDecimalDefinition>
[@abstractpropdefCMISProps propdef/]
</cmis:propertyDecimalDefinition>
[/#macro]

[#macro htmlpropdefCMISProps propdef]
<cmis:propertyHtmlDefinition>
[@abstractpropdefCMISProps propdef/]
</cmis:propertyHtmlDefinition>
[/#macro]

[#macro stringpropdefCMISProps propdef]
<cmis:propertyStringDefinition>
[@abstractpropdefCMISProps propdef/]
[#if propdef.maximumLength != -1]
<cmis:maxLength>${propdef.maximumLength}</cmis:maxLength>
[/#if]
</cmis:propertyStringDefinition>
[/#macro]

[#macro uripropdefCMISProps propdef]
<cmis:propertyUriDefinition>
[@abstractpropdefCMISProps propdef/]
</cmis:propertyUriDefinition>
[/#macro]

[#macro xmlpropdefCMISProps propdef]
<cmis:propertyXmlDefinition>
[@abstractpropdefCMISProps propdef/]
[#-- TODO: scheme, uri --]
</cmis:propertyXmlDefinition>
[/#macro]

[#macro abstractpropdefCMISProps propdef]
  <cmis:name>${propdef.propertyName}</cmis:name>
  <cmis:id>${propdef.propertyId}</cmis:id>
  <cmis:displayName>[#if propdef.displayName??]${propdef.displayName?xml}[/#if]</cmis:displayName>
[#if propdef.description??]
  <cmis:description>${propdef.description?xml}</cmis:description>
[/#if]
  <cmis:propertyType>${propdef.propertyType.label}</cmis:propertyType>
  <cmis:cardinality>${propdef.cardinality.label}</cmis:cardinality>
  <cmis:updateability>${propdef.updatability.label}</cmis:updateability>
  <cmis:inherited>${propdef.inherited?string}</cmis:inherited>
  <cmis:required>${propdef.required?string}</cmis:required>
  <cmis:queryable>${propdef.queryable?string}</cmis:queryable>
  <cmis:orderable>${propdef.orderable?string}</cmis:orderable>
  [@cmisChoices propdef.choices propdef.propertyType/]
  <cmis:openChoice>${propdef.openChoice?string}</cmis:openChoice>
[#if propdef.defaultValue??]
  [#-- TODO: defaults for HTML and XML property types --]
  <cmis:defaultValue><cmis:value>${propdef.defaultValue}</cmis:value></cmis:defaultValue>
[/#if]
[/#macro]

[#macro cmisChoices choices type]
[#if choices?exists]
[#list choices as choice]
[#if type == "STRING"]
<cmis:choiceString cmis:index="${choice.index}" cmis:key="${choice.name}">
[@cmisChoices choice.children type/]
[@stringvalue choice.value/]
</cmis:choiceString>
[#elseif type == "INTEGER"]
<cmis:choiceInteger cmis:index="${choice.index}" cmis:key="${choice.name}">
[@cmisChoices choice.children type/]
[@stringvalue choice.value/]
</cmis:choiceInteger>
[#elseif type == "DECIMAL"]
<cmis:choiceDecimal cmis:index="${choice.index}" cmis:key="${choice.name}">
[@cmisChoices choice.children type/]
[@stringvalue choice.value/]
</cmis:choiceDecimal>
[#elseif type == "BOOLEAN"]
<cmis:choiceBoolean cmis:index="${choice.index}" cmis:key="${choice.name}">
[@cmisChoices choice.children type/]
[@stringvalue choice.value/]
</cmis:choiceBoolean>
[#elseif type == "DATETIME"]
<cmis:choiceDateTime cmis:index="${choice.index}" cmis:key="${choice.name}">
[@cmisChoices choice.children type/]
[@stringvalue choice.value/]
</cmis:choiceDateTime>
[#elseif type == "URI"]
<cmis:choiceUri cmis:index="${choice.index}" cmis:key="${choice.name}">
[@cmisChoices choice.children type/]
[@stringvalue choice.value/]
</cmis:choiceUri>
[#elseif type == "ID"]
<cmis:choiceId cmis:index="${choice.index}" cmis:key="${choice.name}">
[@cmisChoices choice.children type/]
[@stringvalue choice.value/]
</cmis:choiceId>
[#-- TODO: remaining property types --]
[/#if]
[/#list]
[/#if]
[/#macro]


[#-- Helper to render Alfresco content type to Atom content type --]
[#macro contenttype type][#if type == "text/html"]text[#elseif type == "text/xhtml"]xhtml[#elseif type == "text/plain"]text<#else>${type}[/#if][/#macro]

[#-- Helper to render Alfresco content stream link --]
[#macro contentlink node]${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/content[#if node.properties.name??].${encodeuri(node.properties.name)}[/#if][/#macro]

[#-- Helper to render Alfresco service document link --]
[#macro servicelink]${absurl(url.serviceContext)}/api/repository[/#macro]