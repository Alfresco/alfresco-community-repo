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
<link rel="self" href="${absurl(url.serviceContext)}[@nodeuri node/]"/>
[@linkstream node "enclosure"/]
<link rel="edit" href="${absurl(url.serviceContext)}[@nodeuri node/]"/>
[@linkstream node "edit-media"/]
[@documentCMISLinks node=node/]
<published>${xmldate(node.properties.created)}</published>
<summary>[@contentsummary node/]</summary>
<title>${node.name}</title>
<updated>${xmldate(node.properties.modified)}</updated>
<app:edited>${xmldate(node.properties.modified)}</app:edited>
<alf:icon>${absurl(url.context)}${node.icon16}</alf:icon>
<cmisra:object>
[@objectCMISProps node propfilter/]
[#if includeallowableactions][@allowableactions node/][/#if]
</cmisra:object>
[/@entry]
[/#macro]

[#macro documentCMISLinks node]
[@linkallowableactions node/]
[@linkrelationships node/]
[@linkparents node/]
[@linkversions node/]
[@linktype node/]
[@linkservice/]
[/#macro]


[#--                        --]
[#-- ATOM Entry for Version --]
[#--                        --]

[#macro version node version propfilter="*" ns=""]
[@entry ns]
<author><name>${node.properties.creator}</name></author>
[@contentstream node/]
<id>urn:uuid:${node.id}</id>
<link rel="self" href="${absurl(url.serviceContext)}[@nodeuri node/]"/>
[@linkstream node "enclosure"/]
[@documentCMISLinks node=node/]
<published>${xmldate(node.properties.created)}</published>
<summary>[@contentsummary node/]</summary>
<title>${node.name}</title>
<updated>${xmldate(node.properties.modified)}</updated>
<app:edited>${xmldate(node.properties.modified)}</app:edited>
<alf:icon>${absurl(url.context)}${node.icon16}</alf:icon>
<cmisra:object>
[@objectCMISProps node propfilter/]
</cmisra:object>
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
<link rel="self" href="${absurl(url.serviceContext)}/api/pwc/[@noderef node/]"/>
[@linkstream node "enclosure"/]
<link rel="edit" href="${absurl(url.serviceContext)}[@nodeuri node/]"/>
[@linkstream node "edit-media"/]
[@documentCMISLinks node=node/]
<published>${xmldate(node.properties.created)}</published>
<summary>[@contentsummary node/]</summary>
<title>${node.name}</title>
<updated>${xmldate(node.properties.modified)}</updated>
<app:edited>${xmldate(node.properties.modified)}</app:edited>
[#-- TODO: the edit link refers to the updatable node resource, allowing updates on PWCs without checkin --]
<alf:icon>${absurl(url.context)}${node.icon16}</alf:icon>
<cmisra:object>
[@objectCMISProps node propfilter/]
[#if includeallowableactions][@allowableactions node/][/#if]
</cmisra:object>
[/@entry]
[/#macro]


[#--                       --]
[#-- ATOM Entry for Folder --]
[#--                       --]

[#macro foldertree node propfilter="*" includeallowableactions=false includerelationships="none" ns="" maxdepth=-1]
[@folder node propfilter "folders" includeallowableactions includerelationships ns 1 maxdepth "tree"/]
[/#macro]

[#macro folder node propfilter="*" typesfilter="any" includeallowableactions=false includerelationships="none" ns="" depth=1 maxdepth=1 nestedkind=""]
[@entry ns]
<author><name>${node.properties.creator}</name></author>
<content>${node.id}</content>  [#-- TODO --]
<id>urn:uuid:${node.id}</id>
<link rel="self" href="${absurl(url.serviceContext)}[@nodeuri node/]"/>
<link rel="edit" href="${absurl(url.serviceContext)}[@nodeuri node/]"/>
[@folderCMISLinks node/]
<published>${xmldate(node.properties.created)}</published>
<summary>${node.properties.description!node.properties.title!""}</summary>  [#-- TODO --]
<title>${node.name}</title>
<updated>${xmldate(node.properties.modified)}</updated>
<app:edited>${xmldate(node.properties.modified)}</app:edited>
<alf:icon>${absurl(url.context)}${node.icon16}</alf:icon>
<cmisra:object>
[@objectCMISProps node propfilter/]
[#if includeallowableactions][@allowableactions node/][/#if]
</cmisra:object>
[#-- recurse for depth greater than 1 --]
[#if maxdepth == -1 || depth &lt; maxdepth]
[#assign nested = cmischildren(node, typesfilter)/]
[#if nested?size > 0]
<cmisra:children>
[@feedLib.node node "${nestedkind}"/]
[#list nested as child]
  [#if child.isDocument]
    [@document child propfilter includeallowableactions includerelationships/]
  [#else]
    [@folder child propfilter typesfilter includeallowableactions includerelationships ns depth+1 maxdepth nestedkind/]
  [/#if]
[/#list]
</cmisra:children>
[/#if]
[/#if]
[/@entry]
[/#macro]

[#macro folderCMISLinks node]
[@linkallowableactions node/]
[@linkrelationships node/]
[#if cmisproperty(node, cmisconstants.PROP_PARENT_ID)?is_string]
[@linkparent node/]
[/#if]
[@linkchildren node/]
[@linkdescendants node/]
[@linktree node/]
[@linktype node/]
[@linkservice/]
[/#macro]


[#--                             --]
[#-- ATOM Entry for Relationship --]
[#--                             --]

[#macro assoc assoc propfilter="*" includeallowableactions=false ns=""]
[@entry ns]
<author><name>${xmldate(date)}</name></author>  [#-- TODO: [@namedvalue cmisconstants.PROP_CREATED_BY assoc cmisconstants.DATATYPE_STRING/] --]
<content>[@namedvalue cmisconstants.PROP_OBJECT_ID assoc cmisconstants.DATATYPE_ID/]</content>  [#-- TODO: spec id, how to map? --]
<id>[@namedvalue cmisconstants.PROP_OBJECT_ID assoc cmisconstants.DATATYPE_ID/]</id>   [#-- TODO: id compliant --]
<link rel="self" href="${absurl(url.serviceContext)}[@assocuri assoc/]"/>
<link rel="edit" href="${absurl(url.serviceContext)}[@assocuri assoc/]"/>
[@assocCMISLinks assoc=assoc/]
<published>${xmldate(date)}</published>  [#-- TODO: [@namedvalue cmisconstants.PROP_CREATION_DATE assoc cmisconstants.DATATYPE_DATETIME/] --]
<summary>[@namedvalue cmisconstants.PROP_OBJECT_ID assoc cmisconstants.DATATYPE_ID/]</summary>  [#-- TODO: spec id, how to map? --]
<title>[@namedvalue cmisconstants.PROP_OBJECT_ID assoc cmisconstants.DATATYPE_ID/]</title>  [#-- TODO: spec id, how to map? --]
<updated>${xmldate(date)}</updated>  [#-- TODO: [@namedvalue cmisconstants.PROP_LAST_MODIFICATION_DATE assoc cmisconstants.DATATYPE_DATETIME/] --]
<app:edited>${xmldate(date)}</app:edited>  [#-- TODO: [@namedvalue cmisconstants.PROP_LAST_MODIFICATION_DATE assoc cmisconstants.DATATYPE_DATETIME/] --]
<cmisra:object>
[@objectCMISProps assoc propfilter/]
[#-- TODO: [#if includeallowableactions][@allowableactions node/][/#if] --]
</cmisra:object>
[/@entry]
[/#macro]

[#macro assocCMISLinks assoc]
[#-- TODO: <link rel="allowableactions" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/permissions"/> --]
[@linktype assoc/]
[@linktosource assoc.source/]
[@linktotarget assoc.target/]
[@linkservice/]
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
<link rel="self" href="${absurl(url.serviceContext)}[@nodeuri node/]"/>
<link rel="edit" href="${absurl(url.serviceContext)}[@nodeuri node/]"/>
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
<cmisra:object>
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
</cmisra:object>
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
[#if type.label == cmisconstants.DATATYPE_STRING]
<cmis:propertyString pdid="${name}">[@values value;v]<cmis:value>[@stringvalue v/]</cmis:value>[/@values]</cmis:propertyString>
[#elseif type.label == cmisconstants.DATATYPE_INTEGER]
<cmis:propertyInteger pdid="${name}">[@values value;v]<cmis:value>[@integervalue v/]</cmis:value>[/@values]</cmis:propertyInteger>
[#elseif type.label == cmisconstants.DATATYPE_DECIMAL]
<cmis:propertyDecimal pdid="${name}">[@values value;v]<cmis:value>[@decimalvalue v/]</cmis:value>[/@values]</cmis:propertyDecimal>
[#elseif type.label == cmisconstants.DATATYPE_BOOLEAN]
<cmis:propertyBoolean pdid="${name}">[@values value;v]<cmis:value>[@booleanvalue v/]</cmis:value>[/@values]</cmis:propertyBoolean>
[#elseif type.label == cmisconstants.DATATYPE_DATETIME]
<cmis:propertyDateTime pdid="${name}">[@values value;v]<cmis:value>[@datetimevalue v/]</cmis:value>[/@values]</cmis:propertyDateTime>
[#elseif type.label == cmisconstants.DATATYPE_URI]
[#-- TODO: check validity of abs url prefix --]
<cmis:propertyUri pdid="${name}">[@values value;v]<cmis:value>[@urivalue absurl(url.serviceContext) + v/]</cmis:value>[/@values]</cmis:propertyUri>
[#elseif type.label == cmisconstants.DATATYPE_ID]
<cmis:propertyId pdid="${name}">[@values value;v]<cmis:value>[@idvalue v/]</cmis:value>[/@values]</cmis:propertyId>
[#-- TODO: remaining property types --]
[/#if]
[/#macro]

[#macro propnull name type]
[#if type.label == cmisconstants.DATATYPE_STRING]
<cmis:propertyString pdid="${name}"/>
[#elseif type.label == cmisconstants.DATATYPE_INTEGER]
<cmis:propertyInteger pdid="${name}"/>
[#elseif type.label == cmisconstants.DATATYPE_DECIMAL]
<cmis:propertyDecimal pdid="${name}"/>
[#elseif type.label == cmisconstants.DATATYPE_BOOLEAN]
<cmis:propertyBoolean pdid="${name}"/>
[#elseif type.label == cmisconstants.DATATYPE_DATETIME]
<cmis:propertyDateTime pdid="${name}"/>
[#elseif type.label == cmisconstants.DATATYPE_URI]
<cmis:propertyUri pdid="${name}"/>
[#elseif type.label == cmisconstants.DATATYPE_ID]
<cmis:propertyId pdid="${name}"/>
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
[#if type== cmisconstants.DATATYPE_STRING]
[@values value;v][@stringvalue v/][/@values]
[#elseif type == cmisconstants.DATATYPE_INTEGER]
[@values value;v][@integervalue v/][/@values]
[#elseif type == cmisconstants.DATATYPE_DECIMAL]
[@values value;v][@decimalvalue v/][/@values]
[#elseif type == cmisconstants.DATATYPE_BOOLEAN]
[@values value;v][@booleanvalue v/][/@values]
[#elseif type == cmisconstants.DATATYPE_DATETIME]
[@values value;v][@datetimevalue v/][/@values]
[#elseif type == cmisconstants.DATATYPE_URI]
[#-- TODO: check validity of abs url prefix --]
[@values value;v][@urivalue absurl(url.serviceContext) + v/][/@values]
[#elseif type == cmisconstants.DATATYPE_ID]
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

[#macro typedef typedefn includeProperties=true includeInheritedProperties=true ns="" depth=1 maxdepth=1]
[@entry ns=ns]
<author><name>${person.properties.userName}</name></author>
<content>${typedefn.typeId.id}</content>  [#-- TODO --]
<id>urn:uuid:type-${typedefn.typeId.id}</id>
<link rel="self" href="${absurl(url.serviceContext)}/api/type/${typedefn.typeId.id}"/>
[@typedefCMISLinks typedefn/]
<summary>[#if typedefn.description??]${typedefn.description?xml}[#else]${typedefn.displayName?xml}[/#if]</summary>
<title>${typedefn.displayName}</title>
<updated>${xmldate(date)}</updated>  [#-- TODO --]
[@typedefCMISProps typedefn includeProperties/]
[#-- recurse for depth greater than 1 --]
[#if maxdepth == -1 || depth &lt; maxdepth]
[#assign nested = typedefn.getSubTypes(false)/]
[#if nested?size > 1]
<cmisra:children>
[@feedLib.typedef typedefn=typedefn kind="descendants" author="${person.properties.userName}"/]
[#list nested as child]
  [@typedef child includeProperties includeInheritedProperties ns depth+1 maxdepth/]
[/#list]
</cmisra:children>
[/#if]
[/#if]
[/@entry]
[/#macro]

[#macro typedefCMISLinks typedef]
[@linktype typedef/]
[#if typedef.parentType??]
[@linktypeparent typedef/]
[/#if]
[@linktypechildren typedef/]
[@linktypedescendants typedef/]
[@linkservice/]
[/#macro]

[#macro typedefCMISProps typedef includeProperties=true includeInheritedProperties=true]
[#if typedef.baseType.typeId.id = cmisconstants.TYPE_DOCUMENT]
[@documenttypedefCMISProps typedef includeProperties includeInheritedProperties/]
[#elseif typedef.baseType.typeId.id = cmisconstants.TYPE_FOLDER]
[@foldertypedefCMISProps typedef includeProperties includeInheritedProperties/]
[#elseif typedef.baseType.typeId.id =  cmisconstants.TYPE_RELATIONSHIP]
[@relationshiptypedefCMISProps typedef includeProperties includeInheritedProperties/]
[#elseif typedef.baseType.typeId.id =  cmisconstants.TYPE_POLICY]
[@policytypedefCMISProps typedef includeProperties includeInheritedProperties/]
[/#if]
[/#macro]

[#macro documenttypedefCMISProps typedef includeProperties=true includeInheritedProperties=true]
<cmisra:documentType>
  [@objecttypedefCMISProps typedef includeProperties includeInheritedProperties/]
  <cmis:versionable>${typedef.versionable?string}</cmis:versionable>
  <cmis:contentStreamAllowed>${typedef.contentStreamAllowed.label}</cmis:contentStreamAllowed>
</cmisra:documentType>
[/#macro]

[#macro foldertypedefCMISProps typedef includeProperties=true includeInheritedProperties=true]
<cmisra:folderType>
  [@objecttypedefCMISProps typedef includeProperties includeInheritedProperties/]
</cmisra:folderType>
[/#macro]

[#macro relationshiptypedefCMISProps typedef includeProperties=true includeInheritedProperties=true]
<cmisra:relationshipType>
  [@objecttypedefCMISProps typedef includeProperties includeInheritedProperties/]
  [#-- TODO: source and target types --]
</cmisra:relationshipType>
[/#macro]

[#macro policytypedefCMISProps typedef includeProperties=true includeInheritedProperties=true]
<cmisra:policyType>
  [@objecttypedefCMISProps typedef includeProperties includeInheritedProperties/]
</cmisra:policyType>
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
[#if propdef.dataType.label == cmisconstants.DATATYPE_BOOLEAN]
[@booleanpropdefCMISProps propdef inherited/]
[#elseif propdef.dataType.label == cmisconstants.DATATYPE_ID]
[@idpropdefCMISProps propdef inherited/]
[#elseif propdef.dataType.label == cmisconstants.DATATYPE_INTEGER]
[@integerpropdefCMISProps propdef inherited/]
[#elseif propdef.dataType.label == cmisconstants.DATATYPE_DATETIME]
[@datetimepropdefCMISProps propdef inherited/]
[#elseif propdef.dataType.label == cmisconstants.DATATYPE_DECIMAL]
[@decimalpropdefCMISProps propdef inherited/]
[#elseif propdef.dataType.label == cmisconstants.DATATYPE_HTML]
[@htmlpropdefCMISProps propdef inherited/]
[#elseif propdef.dataType.label == cmisconstants.DATATYPE_STRING]
[@stringpropdefCMISProps propdef inherited/]
[#elseif propdef.dataType.label == cmisconstants.DATATYPE_URI]
[@uripropdefCMISProps propdef inherited/]
[#elseif propdef.dataType.label == cmisconstants.DATATYPE_XML]
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
[#if type.label == cmisconstants.DATATYPE_STRING]
<cmis:choiceString cmis:key="${choice.name}">
[@cmisChoices choice.children type/]
<cmis:value>[@stringvalue choice.value/]</cmis:value>
</cmis:choiceString>
[#elseif type.label == cmisconstants.DATATYPE_INTEGER]
<cmis:choiceInteger cmis:key="${choice.name}">
[@cmisChoices choice.children type/]
<cmis:value>[@stringvalue choice.value/]</cmis:value>
</cmis:choiceInteger>
[#elseif type.label == cmisconstants.DATATYPE_DECIMAL]
<cmis:choiceDecimal cmis:key="${choice.name}">
[@cmisChoices choice.children type/]
<cmis:value>[@stringvalue choice.value/]</cmis:value>
</cmis:choiceDecimal>
[#elseif type.label == cmisconstants.DATATYPE_BOOLEAN]
<cmis:choiceBoolean cmis:key="${choice.name}">
[@cmisChoices choice.children type/]
<cmis:value>[@stringvalue choice.value/]</cmis:value>
</cmis:choiceBoolean>
[#elseif type.label == cmisconstants.DATATYPE_DATETIME]
<cmis:choiceDateTime cmis:key="${choice.name}">
[@cmisChoices choice.children type/]
<cmis:value>[@stringvalue choice.value/]</cmis:value>
</cmis:choiceDateTime>
[#elseif type.label == cmisconstants.DATATYPE_URI]
<cmis:choiceUri cmis:key="${choice.name}">
[@cmisChoices choice.children type/]
<cmis:value>[@stringvalue choice.value/]</cmis:value>
</cmis:choiceUri>
[#elseif type.label == cmisconstants.DATATYPE_ID]
<cmis:choiceId cmis:key="${choice.name}">
[@cmisChoices choice.children type/]
<cmis:value>[@stringvalue choice.value/]</cmis:value>
</cmis:choiceId>
[#-- TODO: remaining property types --]
[/#if]
[/#list]
[/#if]
[/#macro]


[#--                                --]
[#-- Link Relations                 --]
[#--                                --]

[#-- Link to repository service document --]
[#macro linkservice]
<link rel="${cmisconstants.REL_SERVICE}" href="${absurl(url.serviceContext)}/api/repository"/>
[/#macro]

[#-- Link to node allowable actions --]
[#macro linkallowableactions node]
<link rel="${cmisconstants.REL_ALLOWABLE_ACTIONS}" href="${absurl(url.serviceContext)}[@nodeuri node/]/allowableactions"/>
[/#macro]

[#-- Link to node relationships --]
[#macro linkrelationships node]
<link rel="${cmisconstants.REL_RELATIONSHIPS}" href="${absurl(url.serviceContext)}[@nodeuri node/]/rels"/>
[/#macro]

[#-- Link to node parents --]
[#macro linkparents node]
<link rel="${cmisconstants.REL_UP}" type="application/atom+xml;type=feed" href="${absurl(url.serviceContext)}[@nodeuri node/]/parents"/>
[/#macro]

[#-- Link to folder parent --]
[#macro linkparent node]
<link rel="${cmisconstants.REL_UP}" type="application/atom+xml;type=entry" href="${absurl(url.serviceContext)}[@nodeuri node.parent/]"/>
[/#macro]

[#-- Link to node children --]
[#macro linkchildren node]
<link rel="${cmisconstants.REL_DOWN}" type="application/atom+xml;type=feed" href="${absurl(url.serviceContext)}[@nodeuri node/]/children"/>
[/#macro]

[#-- Link to node descendants --]
[#macro linkdescendants node]
<link rel="${cmisconstants.REL_DOWN}" type="application/cmistree+xml" href="${absurl(url.serviceContext)}[@nodeuri node/]/descendants"/>
[/#macro]

[#-- Link to node tree --]
[#macro linktree node]
<link rel="${cmisconstants.REL_FOLDER_TREE}" type="application/cmistree+xml" href="${absurl(url.serviceContext)}[@nodeuri node/]/tree"/>
[/#macro]

[#-- Link to node versions --]
[#macro linkversions node]
<link rel="${cmisconstants.REL_VERSION_HISTORY}" href="${absurl(url.serviceContext)}[@nodeuri node/]/versions"/>
[/#macro]

[#-- Link to source node --]
[#macro linktosource node]
<link rel="${cmisconstants.REL_ASSOC_SOURCE}" href="${absurl(url.serviceContext)}[@nodeuri node/]"/>
[/#macro]

[#-- Link to target node --]
[#macro linktotarget node]
<link rel="${cmisconstants.REL_ASSOC_TARGET}" href="${absurl(url.serviceContext)}[@nodeuri node/]"/>
[/#macro]

[#-- Link to content stream --]
[#macro linkstream node rel=""]
<link[#if rel !=""] rel="${rel}"[/#if][#if node.mimetype??] type="${node.mimetype}"[/#if] href="[@contenturi node/]"/>
[/#macro]

[#-- Link to node type --]
[#macro linktype object]
<link rel="${cmisconstants.REL_DESCRIBED_BY}" href="${absurl(url.serviceContext)}[@typeuri cmistype(object)/]"/>
[/#macro]

[#-- Link to type parent --]
[#macro linktypeparent typedef]
<link rel="${cmisconstants.REL_UP}" type="application/atom+xml;type=entry" href="${absurl(url.serviceContext)}[@typeuri typedef.parentType/]"/>
[/#macro]

[#-- Link to type children --]
[#macro linktypechildren typedef]
<link rel="${cmisconstants.REL_DOWN}" type="application/atom+xml;type=feed" href="${absurl(url.serviceContext)}[@typeuri typedef/]/children"/>
[/#macro]

[#-- Link to type descendants --]
[#macro linktypedescendants typedef]
<link rel="${cmisconstants.REL_DOWN}" type="application/cmistree+xml" href="${absurl(url.serviceContext)}[@typeuri typedef/]/descendants"/>
[/#macro]


[#--                                --]
[#-- General Utils                  --]
[#--                                --]

[#-- Helper to render Atom Summary --]
[#macro contentsummary node][#if node.properties.description??]${node.properties.description}[#elseif node.properties.title??]${node.properties.title}[#elseif node.mimetype?? && node.mimetype == "text/plain"]${cropContent(node.properties.content, 50)}[#else]${node.properties.name}[/#if][/#macro]

[#-- Helper to render Alfresco content type to Atom content type --]
[#macro contenttype type][#if type == "text/html"]text[#elseif type == "text/xhtml"]xhtml[#elseif type == "text/plain"]text<#else>${type}[/#if][/#macro]

[#-- Helper to render atom content element --]
[#macro contentstream node]<content[#if node.mimetype??] type="${node.mimetype}"[/#if] src="[@contenturi node/]"/>[/#macro]

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

[#-- Helper to render Alfresco Type uri --]
[#macro typeuri typedef]/api/type/${typedef.typeId.id}[/#macro]
