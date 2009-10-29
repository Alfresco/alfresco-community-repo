[#ftl]

[#--            --]
[#-- ATOM Entry --]
[#--            --]

[#macro entry ns=""]
<entry[#if ns != ""] ${ns}[/#if]>
[#nested]
</entry>
[/#macro]

[#macro objectCMISProps object propfilter]
<cmis:properties>
  [#assign typedef = cmistype(object)]
  
  [#list typedef.propertyDefinitions?values as propdef]
    [@filter propfilter propdef.propertyId.id][@prop propdef.propertyId.id object propdef.dataType/][/@filter]
  [/#list]
</cmis:properties>
[/#macro]


[#--                         --]
[#-- ATOM Entry for Document --]
[#--                         --]

[#macro document node propfilter="*" includeallowableactions=false includerelationships="none" ns=""]
[@entry ns]
<author><name>${node.properties.creator!""}</name></author>
[@contentstream node/]
<id>urn:uuid:${node.id}</id>
<link rel="self" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}"/>
[@linkstream node "enclosure"/]
<link rel="edit" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}"/>
[@linkstream node "edit-media"/]
[@documentCMISLinks node=node/]
<published>${xmldate(node.properties.created)}</published>
<summary>[@contentsummary node/]</summary>
<title>${node.name}</title>
<updated>${xmldate(node.properties.modified)}</updated>
<app:edited>${xmldate(node.properties.modified)}</app:edited>
<alf:icon>${absurl(url.context)}${node.icon16}</alf:icon>
<cmis:object>
[@objectCMISProps node propfilter/]
[#if includeallowableactions][@allowableactions node/][/#if]
</cmis:object>
[/@entry]
[/#macro]

[#macro documentCMISLinks node]
<link rel="allowableactions" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/permissions"/>
<link rel="relationships" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/rels"/>
<link rel="parents" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/parents"/>
<link rel="allversions" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/versions"/>
[@linkstream node "stream"/]
<link rel="type" href="${absurl(url.serviceContext)}/api/type/${cmistype(node).typeId.id!"unknown"}"/>
<link rel="repository" href="[@serviceuri/]"/>
[/#macro]


[#--                        --]
[#-- ATOM Entry for Version --]
[#--                        --]

[#macro version node version propfilter="*" ns=""]
[@entry ns]
<author><name>${node.properties.creator}</name></author>
[@contentstream node/]
<id>urn:uuid:${node.id}</id>
<link rel="self" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}"/>
[@linkstream node "enclosure"/]
[@documentCMISLinks node=node/]
<published>${xmldate(node.properties.created)}</published>
<summary>[@contentsummary node/]</summary>
<title>${node.name}</title>
<updated>${xmldate(node.properties.modified)}</updated>
<app:edited>${xmldate(node.properties.modified)}</app:edited>
<alf:icon>${absurl(url.context)}${node.icon16}</alf:icon>
<cmis:object>
[@objectCMISProps node propfilter/]
</cmis:object>
[/@entry]
[/#macro]


[#--                                     --]
[#-- ATOM Entry for Private Working Copy --]
[#--                                     --]

[#macro pwc node propfilter="*" includeallowableactions=false includerelationships="none" ns=""]
[@entry ns]
<author><name>${node.properties.creator}</name></author>
[@contentstream node/]
<id>urn:uuid:${node.id}</id>
<link rel="self" href="${absurl(url.serviceContext)}/api/pwc/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}"/>
[@linkstream node "enclosure"/]
<link rel="edit" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}"/>
[@linkstream node "edit-media"/]
[@documentCMISLinks node=node/]
<published>${xmldate(node.properties.created)}</published>
<summary>[@contentsummary node/]</summary>
<title>${node.name}</title>
<updated>${xmldate(node.properties.modified)}</updated>
<app:edited>${xmldate(node.properties.modified)}</app:edited>
[#-- TODO: the edit link refers to the updatable node resource, allowing updates on PWCs without checkin --]
<alf:icon>${absurl(url.context)}${node.icon16}</alf:icon>
<cmis:object>
[@objectCMISProps node propfilter/]
[#if includeallowableactions][@allowableactions node/][/#if]
</cmis:object>
[/@entry]
[/#macro]


[#--                       --]
[#-- ATOM Entry for Folder --]
[#--                       --]

[#macro folder node propfilter="*" typesfilter="any" includeallowableactions=false includerelationships="none" ns="" depth=1 maxdepth=1]
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
<app:edited>${xmldate(node.properties.modified)}</app:edited>
<alf:icon>${absurl(url.context)}${node.icon16}</alf:icon>
<cmis:object>
[#-- recurse for depth greater than 1 --]
[@objectCMISProps node propfilter/]
[#if includeallowableactions][@allowableactions node/][/#if]
</cmis:object>
[#if depth < maxdepth || depth == -1]
[#list cmischildren(node, typesfilter) as child]
  [#if child.isDocument]
    [@document child propfilter includeallowableactions includerelationships/]
  [#else]
    [@folder child propfilter typesfilter includeallowableactions includerelationships/]
    [@folder child propfilter typesfilter includeallowableactions includerelationships ns depth+1 maxdepth/]
  [/#if]
[/#list]
[/#if]
[/@entry]
[/#macro]

[#macro folderCMISLinks node]
<link rel="allowableactions" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/permissions"/>
<link rel="relationships" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/rels"/>
[#if cmisproperty(node, "cmis:ParentId")?is_string]
<link rel="parents" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/parent"/>
[/#if]
<link rel="children" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/children"/>
<link rel="descendants" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/descendants"/>
<link rel="type" href="${absurl(url.serviceContext)}/api/type/${cmistype(node).typeId.id!"unknown"}"/>
<link rel="repository" href="[@serviceuri/]"/>
[/#macro]


[#--                             --]
[#-- ATOM Entry for Relationship --]
[#--                             --]

[#macro assoc assoc propfilter="*" includeallowableactions=false ns=""]
[@entry ns]
<author><name>${xmldate(date)}</name></author>  [#-- TODO: [@namedvalue "cmis:CreatedBy" assoc "STRING"/] --]
<content>[@namedvalue "cmis:ObjectId" assoc "ID"/]</content>  [#-- TODO: spec id, how to map? --]
<id>[@namedvalue "cmis:ObjectId" assoc "ID"/]</id>   [#-- TODO: id compliant --]
<link rel="self" href="${absurl(url.serviceContext)}[@assocuri assoc/]"/>
<link rel="edit" href="${absurl(url.serviceContext)}[@assocuri assoc/]"/>
[@assocCMISLinks assoc=assoc/]
<published>${xmldate(date)}</published>  [#-- TODO: [@namedvalue "cmis:CreationDate" assoc "DATETIME"/] --]
<summary>[@namedvalue "cmis:ObjectId" assoc "ID"/]</summary>  [#-- TODO: spec id, how to map? --]
<title>[@namedvalue "cmis:ObjectId" assoc "ID"/]</title>  [#-- TODO: spec id, how to map? --]
<updated>${xmldate(date)}</updated>  [#-- TODO: [@namedvalue "cmis:LastModificationDate" assoc "DATETIME"/] --]
<app:edited>${xmldate(date)}</app:edited>  [#-- TODO: [@namedvalue "cmis:LastModificationDate" assoc "DATETIME"/] --]
<cmis:object>
[@objectCMISProps assoc propfilter/]
[#-- TODO: [#if includeallowableactions][@allowableactions node/][/#if] --]
</cmis:object>
[/@entry]
[/#macro]

[#macro assocCMISLinks assoc]
[#-- TODO: <link rel="allowableactions" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/permissions"/> --]
<link rel="type" href="${absurl(url.serviceContext)}/api/type/${cmistype(assoc).typeId.id!"unknown"}"/>
<link rel="source" href="${absurl(url.serviceContext)}[@nodeuri assoc.source/]"/>
<link rel="target" href="${absurl(url.serviceContext)}[@nodeuri assoc.target/]"/>
<link rel="repository" href="[@serviceuri/]"/>
[/#macro]


[#--                          --]
[#-- ATOM Entry for Query Row --]
[#--                          --]

[#-- TODO: spec issue 47 --]
[#macro row row includeallowableactions=false]
[@entry]
[#if row.nodes??]
[#assign node = row.nodes?first]
<author><name>${node.properties.creator!""}</name></author>

[#-- TODO: review if consistent with ATOM --]
[#if node.isDocument]
  [@contentstream node/]
[#else]
  <content>${node.id}</content>  [#-- TODO --]
[/#if]    

<id>urn:uuid:${node.id}</id>
<link rel="self" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}"/>
<link rel="edit" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}"/>
[#if node.isDocument]
  [@linkstream node "enclosure"/]
  [@linkstream node "edit-media"/]
  [@documentCMISLinks node=node/]
[#else]
  [@folderCMISLinks node=node/]
[/#if]
<title>${node.name}</title>
<updated>${xmldate(node.properties.modified)}</updated>
<alf:icon>${absurl(url.context)}${node.icon16}</alf:icon>
[/#if]
<cmis:object>
<cmis:properties>

[#assign rowvalues = row.values]
[#list rowvalues?keys as colname]
  [#assign coltype = row.getColumnType(colname)]
  [#if rowvalues[colname]??]
  [@propvalue colname rowvalues[colname] coltype/]
  [#else]
  [@propnull colname coltype/]
  [/#if]
[/#list]
</cmis:properties>
[#if row.nodes?? && includeallowableactions][@allowableactions node/][/#if]
</cmis:object>
[/@entry]
[/#macro]


[#--                 --]
[#-- CMIS Properties --]
[#--                 --]

[#macro filter filter value]
[#if filter == "*" || filter?index_of(value) != -1 || filter?matches(value,'i')][#nested][/#if]
[/#macro]

[#macro prop name object type]
[#assign value=cmisproperty(object, name)/]
[#if value?is_string || value?is_number || value?is_boolean || value?is_date || value?is_enumerable]
[@propvalue name value type/]
[#elseif value.class.canonicalName?ends_with("NULL")]
[@propnull name type/]
[/#if]
[/#macro]

[#macro propvalue name value type]
[#if type == "STRING"]
<cmis:propertyString id="${name}">[@values value;v]<cmis:value>[@stringvalue v/]</cmis:value>[/@values]</cmis:propertyString>
[#elseif type == "INTEGER"]
<cmis:propertyInteger id="${name}">[@values value;v]<cmis:value>[@integervalue v/]</cmis:value>[/@values]</cmis:propertyInteger>
[#elseif type == "DECIMAL"]
<cmis:propertyDecimal id="${name}">[@values value;v]<cmis:value>[@decimalvalue v/]</cmis:value>[/@values]</cmis:propertyDecimal>
[#elseif type == "BOOLEAN"]
<cmis:propertyBoolean id="${name}">[@values value;v]<cmis:value>[@booleanvalue v/]</cmis:value>[/@values]</cmis:propertyBoolean>
[#elseif type == "DATETIME"]
<cmis:propertyDateTime id="${name}">[@values value;v]<cmis:value>[@datetimevalue v/]</cmis:value>[/@values]</cmis:propertyDateTime>
[#elseif type == "URI"]
[#-- TODO: check validity of abs url prefix --]
<cmis:propertyUri id="${name}">[@values value;v]<cmis:value>[@urivalue absurl(url.serviceContext) + v/]</cmis:value>[/@values]</cmis:propertyUri>
[#elseif type == "ID"]
<cmis:propertyId id="${name}">[@values value;v]<cmis:value>[@idvalue v/]</cmis:value>[/@values]</cmis:propertyId>
[#-- TODO: remaining property types --]
[/#if]
[/#macro]

[#macro propnull name type]
[#if type == "STRING"]
<cmis:propertyString id="${name}"/>
[#elseif type == "INTEGER"]
<cmis:propertyInteger id="${name}"/>
[#elseif type == "DECIMAL"]
<cmis:propertyDecimal id="${name}"/>
[#elseif type == "BOOLEAN"]
<cmis:propertyBoolean id="${name}"/>
[#elseif type == "DATETIME"]
<cmis:propertyDateTime id="${name}"/>
[#elseif type == "URI"]
<cmis:propertyUri id="${name}"/>
[#elseif type == "ID"]
<cmis:propertyId id="${name}"/>
[#-- TODO: remaining property types --]
[/#if]
[/#macro]


[#--             --]
[#-- CMIS Values --]
[#--             --]

[#macro namedvalue name object type]
[#assign value=cmisproperty(object, name)/]
[#if value?is_string || value?is_number || value?is_boolean || value?is_date || value?is_enumerable][@typedvalue value type/][#elseif value.class.canonicalName?ends_with("NULL")][/#if]
[/#macro]

[#macro typedvalue value type]
[#if type == "STRING"]
[@values value;v][@stringvalue v/][/@values]
[#elseif type == "INTEGER"]
[@values value;v][@integervalue v/][/@values]
[#elseif type == "DECIMAL"]
[@values value;v][@decimalvalue v/][/@values]
[#elseif type == "BOOLEAN"]
[@values value;v][@booleanvalue v/][/@values]
[#elseif type == "DATETIME"]
[@values value;v][@datetimevalue v/][/@values]
[#elseif type == "URI"]
[#-- TODO: check validity of abs url prefix --]
[@values value;v][@urivalue absurl(url.serviceContext) + v/][/@values]
[#elseif type == "ID"]
[@values value;v][@idvalue v/][/@values]
[#-- TODO: remaining property types --]
[/#if]
[/#macro]

[#macro values vals][#if vals?is_enumerable][#list vals as val][#nested val][/#list][#else][#nested vals][/#if][/#macro]

[#macro stringvalue value]${value}[/#macro]
[#macro integervalue value]${value?c}[/#macro]
[#macro decimalvalue value]${value?c}[/#macro]
[#macro booleanvalue value]${value?string}[/#macro]
[#macro datetimevalue value]${xmldate(value)}[/#macro]
[#macro urivalue value]${value}[/#macro]
[#macro idvalue value]${value}[/#macro]


[#--                        --]
[#-- CMIS Allowable Actions --]
[#--                        --]

[#macro allowableactions node ns=""]
<cmis:allowableActions[#if ns != ""] ${ns}[/#if]>
[#nested]
[#assign typedef = cmistype(node)]
[#list typedef.actionEvaluators?values as actionevaluator]
  [@allowableaction node actionevaluator/]
[/#list]
</cmis:allowableActions>
[/#macro]

[#macro allowableaction node actionevaluator]
<cmis:${actionevaluator.action.label}>${actionevaluator.isAllowed(node.nodeRef)?string}</cmis:${actionevaluator.action.label}>
[/#macro]


[#--                                --]
[#-- ATOM Entry for Type Definition --]
[#--                                --]

[#macro typedef typedef includeProperties=true includeInheritedProperties=true ns=""]
[@entry ns=ns]
<author><name>${person.properties.userName}</name></author>
<content>${typedef.typeId.id}</content>  [#-- TODO --]
<id>urn:uuid:type-${typedef.typeId.id}</id>
<link rel="self" href="${absurl(url.serviceContext)}/api/type/${typedef.typeId.id}"/>
[@typedefCMISLinks typedef/]
<summary>[#if typedef.description??]${typedef.description?xml}[#else]${typedef.displayName?xml}[/#if]</summary>
<title>${typedef.displayName}</title>
<updated>${xmldate(date)}</updated>  [#-- TODO --]
[@typedefCMISProps typedef includeProperties/]
[/@entry]
[/#macro]

[#macro typedefCMISLinks typedef]
<link rel="type" href="${absurl(url.serviceContext)}/api/type/${typedef.baseType.typeId.id}"/>
[#if typedef.parentType??]
<link rel="parents" href="${absurl(url.serviceContext)}/api/type/${typedef.parentType.typeId.id}"/>
[/#if]
<link rel="children" href="${absurl(url.serviceContext)}/api/type/${typedef.typeId.id}/children"/>
<link rel="descendants" href="${absurl(url.serviceContext)}/api/type/${typedef.typeId.id}/descendants"/>
<link rel="repository" href="[@serviceuri/]"/>
[/#macro]

[#macro typedefCMISProps typedef includeProperties=true includeInheritedProperties=true]
[#if typedef.baseType.typeId.id = "cmis:Document"]
[@documenttypedefCMISProps typedef includeProperties includeInheritedProperties/]
[#elseif typedef.baseType.typeId.id = "cmis:Folder"]
[@foldertypedefCMISProps typedef includeProperties includeInheritedProperties/]
[#elseif typedef.baseType.typeId.id = "cmis:Relationship"]
[@relationshiptypedefCMISProps typedef includeProperties includeInheritedProperties/]
[#elseif typedef.baseType.typeId.id = "cmis:Policy"]
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
  <cmis:id>${typedef.typeId.id}</cmis:id>
  <cmis:localName>${typedef.typeId.localName}</cmis:localName>
  <cmis:localNamespace>${typedef.typeId.localNamespace}</cmis:localNamespace>
  <cmis:displayName>[#if typedef.displayName??]${typedef.displayName?xml}[/#if]</cmis:displayName>
  <cmis:queryName>${typedef.queryName}</cmis:queryName>
  <cmis:description>[#if typedef.description??]${typedef.description?xml}[/#if]</cmis:description>
  <cmis:baseTypeId>${typedef.baseType.typeId.id}</cmis:baseTypeId>
[#if typedef.parentType??]  
  <cmis:parentId>${typedef.parentType.typeId.id}</cmis:parentId>
[/#if]
  <cmis:creatable>${typedef.creatable?string}</cmis:creatable>
  <cmis:fileable>${typedef.fileable?string}</cmis:fileable>
  <cmis:queryable>${typedef.queryable?string}</cmis:queryable>
  <cmis:fulltextindexed>${typedef.fullTextIndexed?string}</cmis:fulltextindexed>
  <cmis:includedInSupertypeQuery>${typedef.includeInSuperTypeQuery?string}</cmis:includedInSupertypeQuery>
  <cmis:controllablePolicy>${typedef.controllablePolicy?string}</cmis:controllablePolicy>
  <cmis:controllableACL>${typedef.controllableACL?string}</cmis:controllableACL>
  [#if includeProperties]
    [#assign ownedprops = typedef.ownedPropertyDefinitions?keys]
    [#list typedef.propertyDefinitions?values as propdef]
      [#assign inherited = !ownedprops?seq_contains(propdef.propertyId)]
      [#if includeInheritedProperties || !inherited]
        [@propdefCMISProps propdef inherited/]
      [/#if]
    [/#list]
  [/#if]
[/#macro]

[#macro propdefCMISProps propdef inherited=false]
[#if propdef.dataType == "BOOLEAN"]
[@booleanpropdefCMISProps propdef inherited/]
[#elseif propdef.dataType == "ID"]
[@idpropdefCMISProps propdef inherited/]
[#elseif propdef.dataType == "INTEGER"]
[@integerpropdefCMISProps propdef inherited/]
[#elseif propdef.dataType == "DATETIME"]
[@datetimepropdefCMISProps propdef inherited/]
[#elseif propdef.dataType == "DECIMAL"]
[@decimalpropdefCMISProps propdef inherited/]
[#elseif propdef.dataType == "HTML"]
[@htmlpropdefCMISProps propdef inherited/]
[#elseif propdef.dataType == "STRING"]
[@stringpropdefCMISProps propdef inherited/]
[#elseif propdef.dataType == "URI"]
[@uripropdefCMISProps propdef inherited/]
[#elseif propdef.dataType == "XML"]
[@xmlpropdefCMISProps propdef inherited/]
[/#if]
[/#macro]

[#macro booleanpropdefCMISProps propdef inherited=false]
<cmis:propertyBooleanDefinition>
[@abstractpropdefCMISProps propdef inherited/]
</cmis:propertyBooleanDefinition>
[/#macro]

[#macro idpropdefCMISProps propdef inherited=false]
<cmis:propertyIdDefinition>
[@abstractpropdefCMISProps propdef inherited/]
</cmis:propertyIdDefinition>
[/#macro]

[#macro integerpropdefCMISProps propdef inherited=false]
<cmis:propertyIntegerDefinition>
[@abstractpropdefCMISProps propdef inherited/]
[#-- TODO: minValue, maxValue --]
</cmis:propertyIntegerDefinition>
[/#macro]

[#macro datetimepropdefCMISProps propdef inherited=false]
<cmis:propertyDateTimeDefinition>
[@abstractpropdefCMISProps propdef inherited/]
</cmis:propertyDateTimeDefinition>
[/#macro]

[#macro decimalpropdefCMISProps propdef inherited=false]
<cmis:propertyDecimalDefinition>
[@abstractpropdefCMISProps propdef inherited/]
</cmis:propertyDecimalDefinition>
[/#macro]

[#macro htmlpropdefCMISProps propdef inherited=false]
<cmis:propertyHtmlDefinition>
[@abstractpropdefCMISProps propdef inherited/]
</cmis:propertyHtmlDefinition>
[/#macro]

[#macro stringpropdefCMISProps propdef inherited=false]
<cmis:propertyStringDefinition>
[@abstractpropdefCMISProps propdef inherited/]
[#if propdef.maximumLength != -1]
<cmis:maxLength>${propdef.maximumLength}</cmis:maxLength>
[/#if]
</cmis:propertyStringDefinition>
[/#macro]

[#macro uripropdefCMISProps propdef inherited=false]
<cmis:propertyUriDefinition>
[@abstractpropdefCMISProps propdef inherited/]
</cmis:propertyUriDefinition>
[/#macro]

[#macro xmlpropdefCMISProps propdef inherited=false]
<cmis:propertyXmlDefinition>
[@abstractpropdefCMISProps propdef inherited/]
[#-- TODO: scheme, uri --]
</cmis:propertyXmlDefinition>
[/#macro]

[#macro abstractpropdefCMISProps propdef inherited=false]
  <cmis:id>${propdef.propertyId.id}</cmis:id>
  <cmis:localName>${propdef.propertyId.localName}</cmis:localName>
  <cmis:localNamespace>${propdef.propertyId.localNamespace}</cmis:localNamespace>
  <cmis:displayName>[#if propdef.displayName??]${propdef.displayName?xml}[/#if]</cmis:displayName>
  <cmis:queryName>${propdef.queryName}</cmis:queryName>
[#if propdef.description??]
  <cmis:description>${propdef.description?xml}</cmis:description>
[/#if]
  <cmis:propertyType>${propdef.dataType.label}</cmis:propertyType>
  <cmis:cardinality>${propdef.cardinality.label}</cmis:cardinality>
  <cmis:updatability>${propdef.updatability.label}</cmis:updatability>
  <cmis:inherited>${inherited?string}</cmis:inherited>
  <cmis:required>${propdef.required?string}</cmis:required>
  <cmis:queryable>${propdef.queryable?string}</cmis:queryable>
  <cmis:orderable>${propdef.orderable?string}</cmis:orderable>
  [@cmisChoices propdef.choices propdef.dataType/]
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
<cmis:choiceString cmis:key="${choice.name}">
[@cmisChoices choice.children type/]
<cmis:value>[@stringvalue choice.value/]</cmis:value>
</cmis:choiceString>
[#elseif type == "INTEGER"]
<cmis:choiceInteger cmis:key="${choice.name}">
[@cmisChoices choice.children type/]
<cmis:value>[@stringvalue choice.value/]</cmis:value>
</cmis:choiceInteger>
[#elseif type == "DECIMAL"]
<cmis:choiceDecimal cmis:key="${choice.name}">
[@cmisChoices choice.children type/]
<cmis:value>[@stringvalue choice.value/]</cmis:value>
</cmis:choiceDecimal>
[#elseif type == "BOOLEAN"]
<cmis:choiceBoolean cmis:key="${choice.name}">
[@cmisChoices choice.children type/]
<cmis:value>[@stringvalue choice.value/]</cmis:value>
</cmis:choiceBoolean>
[#elseif type == "DATETIME"]
<cmis:choiceDateTime cmis:key="${choice.name}">
[@cmisChoices choice.children type/]
<cmis:value>[@stringvalue choice.value/]</cmis:value>
</cmis:choiceDateTime>
[#elseif type == "URI"]
<cmis:choiceUri cmis:key="${choice.name}">
[@cmisChoices choice.children type/]
<cmis:value>[@stringvalue choice.value/]</cmis:value>
</cmis:choiceUri>
[#elseif type == "ID"]
<cmis:choiceId cmis:key="${choice.name}">
[@cmisChoices choice.children type/]
<cmis:value>[@stringvalue choice.value/]</cmis:value>
</cmis:choiceId>
[#-- TODO: remaining property types --]
[/#if]
[/#list]
[/#if]
[/#macro]

[#-- Helper to render Atom Summary --]
[#macro contentsummary node][#if node.properties.description??]${node.properties.description}[#elseif node.properties.title??]${node.properties.title}[#elseif node.mimetype?? && node.mimetype == "text/plain"]${cropContent(node.properties.content, 50)}[#else]${node.properties.name}[/#if][/#macro]

[#-- Helper to render Alfresco content type to Atom content type --]
[#macro contenttype type][#if type == "text/html"]text[#elseif type == "text/xhtml"]xhtml[#elseif type == "text/plain"]text<#else>${type}[/#if][/#macro]

[#-- Helper to render atom content element --]
[#macro contentstream node]<content[#if node.mimetype??] type="${node.mimetype}"[/#if] src="[@contenturi node/]"/>[/#macro]

[#-- Helper to render atom content element --]
[#macro linkstream node rel=""]<link[#if rel !=""] rel="${rel}"[/#if][#if node.mimetype??] type="${node.mimetype}"[/#if] href="[@contenturi node/]"/>[/#macro]

[#-- Helper to render Alfresco content stream uri --]
[#macro contenturi node]${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/content[#if node.properties.name?? && node.properties.name?last_index_of(".") != -1]${encodeuri(node.properties.name?substring(node.properties.name?last_index_of(".")))}[/#if][/#macro]

[#-- Helper to render Alfresco service document uri --]
[#macro serviceuri]${absurl(url.serviceContext)}/api/repository[/#macro]

[#-- Helper to render Node Ref --]
[#macro noderef node]${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}[/#macro]

[#-- Helper to render Alfresco Node uri --]
[#macro nodeuri node]/api/node/[@noderef node/][/#macro]

[#-- Helper to render Alfresco Assoc uri --]
[#macro assocuri assoc]/api/rel/[@noderef assoc.source/]/type/${cmistype(assoc).typeId.id!"undefined"}/target/[@noderef assoc.target/][/#macro]
