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
    [@filter propfilter propdef.queryName][@prop propdef.propertyId.id object propdef.dataType.label/][/@filter]
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
[@linksLib.linknodeself node/]
[@linksLib.linkstream node "enclosure"/]
[@linksLib.linknodeedit node/]
[@linksLib.linkstream node "edit-media"/]
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
<cmisra:pathSegment>${node.name}</cmisra:pathSegment>
[/@entry]
[/#macro]

[#macro documentCMISLinks node]
[@linksLib.linkallowableactions node/]
[@linksLib.linkrelationships node/]
[@linksLib.linkparents node/]
[@linksLib.linkversions node/]
[@linksLib.linktype node/]
[@linksLib.linkservice/]
[/#macro]


[#--                        --]
[#-- ATOM Entry for Version --]
[#--                        --]

[#macro version node version propfilter="*" ns=""]
[@entry ns]
<author><name>${node.properties.creator}</name></author>
[@contentstream node/]
<id>urn:uuid:${node.id}</id>
[@linksLib.linknodeself node/]
[@linksLib.linkstream node "enclosure"/]
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
[#assign pwcuri]/cmis/pwc/[@linksLib.noderef node/][/#assign]
[@linksLib.linkself href="${pwcuri}"/]
[@linksLib.linkstream node "enclosure"/]
[@linksLib.linknodeedit node/]
[@linksLib.linkstream node "edit-media"/]
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
<cmisra:pathSegment>${node.name}</cmisra:pathSegment>
[/@entry]
[/#macro]


[#--                       --]
[#-- ATOM Entry for Folder --]
[#--                       --]

[#macro foldertree node propfilter="*" includeallowableactions=false includerelationships="none" ns="" maxdepth=-1]
[@folder node propfilter "folders" includeallowableactions includerelationships ns 1 maxdepth "" "tree"/]
[/#macro]

[#macro folder node propfilter="*" typesfilter="any" includeallowableactions=false includerelationships="none" ns="" depth=1 maxdepth=1 relativePathSegment="" nestedkind=""]
[@entry ns]
<author><name>${node.properties.creator}</name></author>
<content>${node.id}</content>  [#-- TODO --]
<id>urn:uuid:${node.id}</id>
[@linksLib.linknodeself node/]
[@linksLib.linknodeedit node/]
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
<cmisra:pathSegment>${node.name}</cmisra:pathSegment>
[#if relativePathSegment != ""]
<cmisra:relativePathSegment>${relativePathSegment}</cmisra:relativePathSegment>
[/#if]
[#-- recurse for depth greater than 1 --]
[#if maxdepth == -1 || depth &lt; maxdepth]
[#assign nested = cmischildren(node, typesfilter)/]
[#if nested?size > 0]
<cmisra:children>
[@feedLib.node node "${nestedkind}"]
  [#if nestedkind == "tree"][@linksLib.linktree node "self"/][#else][@linksLib.linkdescendants node "self"/][/#if]
[/@feedLib.node]
[#list nested as child]
  [#if child.isDocument]
    [@document child propfilter includeallowableactions includerelationships/]
  [#else]
    [@folder child propfilter typesfilter includeallowableactions includerelationships ns depth+1 maxdepth "" nestedkind/]
  [/#if]
[/#list]
</cmisra:children>
[/#if]
[/#if]
[/@entry]
[/#macro]

[#macro folderCMISLinks node]
[@linksLib.linkallowableactions node/]
[@linksLib.linkrelationships node/]
[#if cmisproperty(node, cmisconstants.PROP_PARENT_ID)?is_string]
[@linksLib.linkparent node/]
[/#if]
[@linksLib.linkchildren node/]
[@linksLib.linkdescendants node/]
[@linksLib.linktree node/]
[@linksLib.linktype node/]
[@linksLib.linkservice/]
[/#macro]


[#--                             --]
[#-- ATOM Entry for Relationship --]
[#--                             --]

[#macro assoc assoc propfilter="*" includeallowableactions=false ns=""]
[@entry ns]
<author><name>${xmldate(date)}</name></author>  [#-- TODO: [@namedvalue cmisconstants.PROP_CREATED_BY assoc cmisconstants.DATATYPE_STRING/] --]
<content>[@namedvalue cmisconstants.PROP_OBJECT_ID assoc cmisconstants.DATATYPE_ID/]</content>  [#-- TODO: spec id, how to map? --]
<id>[@namedvalue cmisconstants.PROP_OBJECT_ID assoc cmisconstants.DATATYPE_ID/]</id>   [#-- TODO: id compliant --]
[@linksLib.linkassocself assoc/]
[@linksLib.linkassocedit assoc/]
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
[#-- TODO: <link rel="allowableactions" href="${absurl(url.serviceContext)}/cmis/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/permissions"/> --]
[@linksLib.linktype assoc/]
[@linksLib.linktosource assoc.source/]
[@linksLib.linktotarget assoc.target/]
[@linksLib.linkservice/]
[/#macro]


[#--                          --]
[#-- ATOM Entry for Query Row --]
[#--                          --]

[#-- TODO: spec issue 47 --]
[#macro row row includeallowableactions=false]
[@entry]
[#-- TODO: calculate multiNodeResultSet from result set --]
[#if row.nodes?? && row.nodes?size == 1][#assign node = row.nodes?first/][/#if]
[#if node??]
<author><name>${node.properties.creator!""}</name></author>
[#-- TODO: review if consistent with ATOM --]
[#if node.isDocument]
  [@contentstream node/]
[#else]
  <content>${node.id}</content>  [#-- TODO --]
[/#if]    
<id>urn:uuid:${node.id}</id>
[@linksLib.linknodeself node/]
[@linksLib.linknodeedit node/]
[#if node.isDocument]
  [@linksLib.linkstream node "enclosure"/]
  [@linksLib.linkstream node "edit-media"/]
  [@documentCMISLinks node=node/]
[#else]
  [@folderCMISLinks node=node/]
[/#if]
<title>${node.name}</title>
<updated>${xmldate(node.properties.modified)}</updated>
<alf:icon>${absurl(url.context)}${node.icon16}</alf:icon>
[#else]
<author><name>${person.properties.userName}</name></author>
<id>urn:uuid:row-${row.index?c}</id>
<title>Row ${row.index?c}</title>
<updated>${xmldate(now)}</updated>
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
[#if node?? && includeallowableactions][@allowableactions node/][/#if]
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
[#if type == cmisconstants.DATATYPE_STRING]
<cmis:propertyString propertyDefinitionId="${name}">[@values value;v]<cmis:value>[@stringvalue v/]</cmis:value>[/@values]</cmis:propertyString>
[#elseif type == cmisconstants.DATATYPE_INTEGER]
<cmis:propertyInteger propertyDefinitionId="${name}">[@values value;v]<cmis:value>[@integervalue v/]</cmis:value>[/@values]</cmis:propertyInteger>
[#elseif type == cmisconstants.DATATYPE_DECIMAL]
<cmis:propertyDecimal propertyDefinitionId="${name}">[@values value;v]<cmis:value>[@decimalvalue v/]</cmis:value>[/@values]</cmis:propertyDecimal>
[#elseif type == cmisconstants.DATATYPE_BOOLEAN]
<cmis:propertyBoolean propertyDefinitionId="${name}">[@values value;v]<cmis:value>[@booleanvalue v/]</cmis:value>[/@values]</cmis:propertyBoolean>
[#elseif type == cmisconstants.DATATYPE_DATETIME]
<cmis:propertyDateTime propertyDefinitionId="${name}">[@values value;v]<cmis:value>[@datetimevalue v/]</cmis:value>[/@values]</cmis:propertyDateTime>
[#elseif type == cmisconstants.DATATYPE_URI]
[#-- TODO: check validity of abs url prefix --]
<cmis:propertyUri propertyDefinitionId="${name}">[@values value;v]<cmis:value>[@urivalue absurl(url.serviceContext) + v/]</cmis:value>[/@values]</cmis:propertyUri>
[#elseif type == cmisconstants.DATATYPE_ID]
<cmis:propertyId propertyDefinitionId="${name}">[@values value;v]<cmis:value>[@idvalue v/]</cmis:value>[/@values]</cmis:propertyId>
[#-- TODO: remaining property types --]
[/#if]
[/#macro]

[#macro propnull name type]
[#if type == cmisconstants.DATATYPE_STRING]
<cmis:propertyString propertyDefinitionId="${name}"/>
[#elseif type == cmisconstants.DATATYPE_INTEGER]
<cmis:propertyInteger propertyDefinitionId="${name}"/>
[#elseif type == cmisconstants.DATATYPE_DECIMAL]
<cmis:propertyDecimal propertyDefinitionId="${name}"/>
[#elseif type == cmisconstants.DATATYPE_BOOLEAN]
<cmis:propertyBoolean propertyDefinitionId="${name}"/>
[#elseif type == cmisconstants.DATATYPE_DATETIME]
<cmis:propertyDateTime propertyDefinitionId="${name}"/>
[#elseif type == cmisconstants.DATATYPE_URI]
<cmis:propertyUri propertyDefinitionId="${name}"/>
[#elseif type == cmisconstants.DATATYPE_ID]
<cmis:propertyId propertyDefinitionId="${name}"/>
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
[@linksLib.linktypeself typedefn/]
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
[@feedLib.generic "urn:uuid:type-${typedefn.typeId.id}-descendants" "Type ${typedefn.displayName} Descendants" "${person.properties.userName}"]
  [@linksLib.linktypedescendants typedefn "self"/]
[/@feedLib.generic]
[#list nested as child]
  [@typedef child includeProperties includeInheritedProperties ns depth+1 maxdepth/]
[/#list]
</cmisra:children>
[/#if]
[/#if]
[/@entry]
[/#macro]

[#macro typedefCMISLinks typedef]
[@linksLib.linktype typedef/]
[#if typedef.parentType??]
[@linksLib.linktypeparent typedef/]
[/#if]
[@linksLib.linktypechildren typedef/]
[@linksLib.linktypedescendants typedef/]
[@linksLib.linkservice/]
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
<cmisra:type cmisra:id="${typedef.typeId.id}" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="cmis:cmisTypeDocumentDefinitionType">
  [@objecttypedefCMISProps typedef includeProperties includeInheritedProperties/]
  <cmis:versionable>${typedef.versionable?string}</cmis:versionable>
  <cmis:contentStreamAllowed>${typedef.contentStreamAllowed.label}</cmis:contentStreamAllowed>
</cmisra:type>
[/#macro]

[#macro foldertypedefCMISProps typedef includeProperties=true includeInheritedProperties=true]
<cmisra:type cmisra:id="${typedef.typeId.id}" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="cmis:cmisTypeFolderDefinitionType">
  [@objecttypedefCMISProps typedef includeProperties includeInheritedProperties/]
</cmisra:type>
[/#macro]

[#macro relationshiptypedefCMISProps typedef includeProperties=true includeInheritedProperties=true]
<cmisra:type cmisra:id="${typedef.typeId.id}" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="cmis:cmisTypeRelationshipDefinitionType">
  [@objecttypedefCMISProps typedef includeProperties includeInheritedProperties/]
  [#list typedef.allowedSourceTypes as allowedSourceType]
    <cmis:allowedSourceTypes>${allowedSourceType.typeId.id}</cmis:allowedSourceTypes>
  [/#list]
  [#list typedef.allowedTargetTypes as allowedTargetType]
    <cmis:allowedTargetTypes>${allowedTargetType.typeId.id}</cmis:allowedTargetTypes>
  [/#list]
</cmisra:type>
[/#macro]

[#macro policytypedefCMISProps typedef includeProperties=true includeInheritedProperties=true]
<cmisra:type cmisra:id="${typedef.typeId.id}" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="cmis:cmisTypePolicyDefinitionType">
  [@objecttypedefCMISProps typedef includeProperties includeInheritedProperties/]
</cmisra:type>
[/#macro]

[#macro objecttypedefCMISProps typedef includeProperties=true includeInheritedProperties=true]
  <cmis:id>${typedef.typeId.id}</cmis:id>
  <cmis:localName>${typedef.typeId.localName}</cmis:localName>
  <cmis:localNamespace>${typedef.typeId.localNamespace}</cmis:localNamespace>
  <cmis:displayName>[#if typedef.displayName??]${typedef.displayName?xml}[/#if]</cmis:displayName>
  <cmis:queryName>${typedef.queryName}</cmis:queryName>
  <cmis:description>[#if typedef.description??]${typedef.description?xml}[/#if]</cmis:description>
  <cmis:baseId>${typedef.baseType.typeId.id}</cmis:baseId>
[#if typedef.parentType??]  
  <cmis:parentId>${typedef.parentType.typeId.id}</cmis:parentId>
[/#if]
  <cmis:creatable>${typedef.creatable?string}</cmis:creatable>
  <cmis:fileable>${typedef.fileable?string}</cmis:fileable>
  <cmis:queryable>${typedef.queryable?string}</cmis:queryable>
  <cmis:fulltextIndexed>${typedef.fullTextIndexed?string}</cmis:fulltextIndexed>
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
[/#if]
[/#macro]


[#macro booleanpropdefCMISProps propdef inherited=false]
<cmis:propertyBooleanDefinition>
[@abstractpropdefCMISProps propdef inherited/]
[#if propdef.defaultValue??]
  <cmis:defaultValue><cmis:value>${propdef.defaultValue}</cmis:value></cmis:defaultValue>
[/#if]
  [@cmisChoices propdef.choices propdef.dataType.label/]
</cmis:propertyBooleanDefinition>
[/#macro]

[#macro idpropdefCMISProps propdef inherited=false]
<cmis:propertyIdDefinition>
[@abstractpropdefCMISProps propdef inherited/]
[#if propdef.defaultValue??]
  <cmis:defaultValue><cmis:value>${propdef.defaultValue}</cmis:value></cmis:defaultValue>
[/#if]
  [@cmisChoices propdef.choices propdef.dataType.label/]
</cmis:propertyIdDefinition>
[/#macro]

[#macro integerpropdefCMISProps propdef inherited=false]
<cmis:propertyIntegerDefinition>
[@abstractpropdefCMISProps propdef inherited/]
[#if propdef.defaultValue??]
  <cmis:defaultValue><cmis:value>${propdef.defaultValue}</cmis:value></cmis:defaultValue>
[/#if]
  [#-- TODO: maxValue, minValue --]
  [@cmisChoices propdef.choices propdef.dataType.label/]
</cmis:propertyIntegerDefinition>
[/#macro]

[#macro datetimepropdefCMISProps propdef inherited=false]
<cmis:propertyDateTimeDefinition>
[@abstractpropdefCMISProps propdef inherited/]
[#if propdef.defaultValue??]
  <cmis:defaultValue><cmis:value>${propdef.defaultValue}</cmis:value></cmis:defaultValue>
[/#if]
  [@cmisChoices propdef.choices propdef.dataType.label/]
</cmis:propertyDateTimeDefinition>
[/#macro]

[#macro decimalpropdefCMISProps propdef inherited=false]
<cmis:propertyDecimalDefinition>
[@abstractpropdefCMISProps propdef inherited/]
[#if propdef.defaultValue??]
  <cmis:defaultValue><cmis:value>${propdef.defaultValue}</cmis:value></cmis:defaultValue>
[/#if]
  [#-- TODO: maxValue, minValue, precision --]
  [@cmisChoices propdef.choices propdef.dataType.label/]
</cmis:propertyDecimalDefinition>
[/#macro]

[#macro htmlpropdefCMISProps propdef inherited=false]
<cmis:propertyHtmlDefinition>
[@abstractpropdefCMISProps propdef inherited/]
[#if propdef.defaultValue??]
  <cmis:defaultValue><cmis:value>${propdef.defaultValue}</cmis:value></cmis:defaultValue>
[/#if]
  [@cmisChoices propdef.choices propdef.dataType.label/]
</cmis:propertyHtmlDefinition>
[/#macro]

[#macro stringpropdefCMISProps propdef inherited=false]
<cmis:propertyStringDefinition>
[@abstractpropdefCMISProps propdef inherited/]
[#if propdef.defaultValue??]
  <cmis:defaultValue><cmis:value>${propdef.defaultValue}</cmis:value></cmis:defaultValue>
[/#if]
  [#-- TODO: maxValue, minValue, precision --]
[#if propdef.maximumLength != -1]
  <cmis:maxLength>${propdef.maximumLength}</cmis:maxLength>
[/#if]
  [@cmisChoices propdef.choices propdef.dataType.label/]
</cmis:propertyStringDefinition>
[/#macro]

[#macro uripropdefCMISProps propdef inherited=false]
<cmis:propertyUriDefinition>
[@abstractpropdefCMISProps propdef inherited/]
[#if propdef.defaultValue??]
  <cmis:defaultValue><cmis:value>${propdef.defaultValue}</cmis:value></cmis:defaultValue>
[/#if]
  [@cmisChoices propdef.choices propdef.dataType.label/]
</cmis:propertyUriDefinition>
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
  <cmis:openChoice>${propdef.openChoice?string}</cmis:openChoice>
[/#macro]

[#macro cmisChoices choices type]
[#if choices?exists]
[#list choices as choice]
[#if type == cmisconstants.DATATYPE_STRING]
<cmis:choiceString displayName="${choice.name}">
[@cmisChoices choice.children type/]
<cmis:value>[@stringvalue choice.value/]</cmis:value>
</cmis:choiceString>
[#elseif type == cmisconstants.DATATYPE_INTEGER]
<cmis:choiceInteger displayName="${choice.name}">
[@cmisChoices choice.children type/]
<cmis:value>[@stringvalue choice.value/]</cmis:value>
</cmis:choiceInteger>
[#elseif type == cmisconstants.DATATYPE_DECIMAL]
<cmis:choiceDecimal displayName="${choice.name}">
[@cmisChoices choice.children type/]
<cmis:value>[@stringvalue choice.value/]</cmis:value>
</cmis:choiceDecimal>
[#elseif type == cmisconstants.DATATYPE_BOOLEAN]
<cmis:choiceBoolean displayName="${choice.name}">
[@cmisChoices choice.children type/]
<cmis:value>[@stringvalue choice.value/]</cmis:value>
</cmis:choiceBoolean>
[#elseif type == cmisconstants.DATATYPE_DATETIME]
<cmis:choiceDateTime displayName="${choice.name}">
[@cmisChoices choice.children type/]
<cmis:value>[@stringvalue choice.value/]</cmis:value>
</cmis:choiceDateTime>
[#elseif type == cmisconstants.DATATYPE_URI]
<cmis:choiceUri displayName="${choice.name}">
[@cmisChoices choice.children type/]
<cmis:value>[@stringvalue choice.value/]</cmis:value>
</cmis:choiceUri>
[#elseif type == cmisconstants.DATATYPE_ID]
<cmis:choiceId displayName="${choice.name}">
[@cmisChoices choice.children type/]
<cmis:value>[@stringvalue choice.value/]</cmis:value>
</cmis:choiceId>
[#-- TODO: remaining property types --]
[/#if]
[/#list]
[/#if]
[/#macro]


[#--                                --]
[#-- General Utils                  --]
[#--                                --]

[#-- Helper to render Atom Summary --]
[#macro contentsummary node][#if node.properties.description??]${node.properties.description}[#elseif node.properties.title??]${node.properties.title}[#elseif node.mimetype?? && node.mimetype == "text/plain"]${cropContent(node.properties.content, 50)}[#else]${node.properties.name}[/#if][/#macro]

[#-- Helper to render Alfresco content type to Atom content type --]
[#macro contenttype type][#if type == "text/html"]text[#elseif type == "text/xhtml"]xhtml[#elseif type == "text/plain"]text<#else>${type}[/#if][/#macro]

[#-- Helper to render atom content element --]
[#macro contentstream node]<content[#if node.mimetype??] type="${node.mimetype}"[/#if] src="[@linksLib.contenturi node/]"/>[/#macro]
