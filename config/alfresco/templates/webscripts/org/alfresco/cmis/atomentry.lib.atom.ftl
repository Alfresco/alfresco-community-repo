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
<cmis:object>
[@documentCMISProps node propfilter/]
[#if includeallowableactions][@allowableactions node/][/#if]
</cmis:object>
<cmis:terminator/>
<app:edited>${xmldate(node.properties.modified)}</app:edited>
<alf:icon>${absurl(url.context)}${node.icon16}</alf:icon>
[/@entry]
[/#macro]

[#macro documentCMISLinks node]
<link rel="allowableactions" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/permissions"/>
<link rel="relationships" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/associations"/>
<link rel="parents" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/parents"/>
<link rel="allversions" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/versions"/>
[@linkstream node "stream"/]
<link rel="type" href="${absurl(url.serviceContext)}/api/type/${cmistype(node).typeId.id!"unknown"}"/>
<link rel="repository" href="[@serviceuri/]"/>
[/#macro]

[#macro documentCMISProps node propfilter]
<cmis:properties>
  [#-- TODO: Spec issue: BaseType not a property --]
  [@filter propfilter "BaseType"][@propvalue "BaseType" "document" "STRING"/][/@filter]

  [#assign typedef = cmistype(node)]
  [#list typedef.propertyDefinitions?values as propdef]
    [@filter propfilter propdef.propertyId.name][@prop propdef.propertyId.name node propdef.dataType/][/@filter]
  [/#list]
</cmis:properties>
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
<cmis:object>
[@documentCMISProps node propfilter/]
[#if includeallowableactions][@allowableactions node/][/#if]
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
<cmis:object>
[#-- recurse for depth greater than 1 --]
[@folderCMISProps node propfilter/]
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
<cmis:terminator/>
<app:edited>${xmldate(node.properties.modified)}</app:edited>
<alf:icon>${absurl(url.context)}${node.icon16}</alf:icon>
[/@entry]
[/#macro]

[#macro folderCMISLinks node]
<link rel="allowableactions" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/permissions"/>
<link rel="relationships" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/associations"/>
[#if cmisproperty(node, "ParentId")??]
<link rel="parent" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/parent"/>
[/#if]
<link rel="children" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/children"/>
<link rel="descendants" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}/descendants"/>
<link rel="type" href="${absurl(url.serviceContext)}/api/type/${cmistype(node).typeId.id!"unknown"}"/>
<link rel="repository" href="[@serviceuri/]"/>
[/#macro]

[#macro folderCMISProps node propfilter]
<cmis:properties>
  [#-- TODO: Spec issue: BaseType not a property --]
  [@filter propfilter "BaseType"][@propvalue "BaseType" "folder" "STRING"/][/@filter]

  [#assign typedef = cmistype(node)]
  [#list typedef.propertyDefinitions?values as propdef]
    [@filter propfilter propdef.propertyId.name][@prop propdef.propertyId.name node propdef.dataType/][/@filter]
  [/#list]
</cmis:properties>
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
<title>${node.name}</title>
<updated>${xmldate(node.properties.modified)}</updated>
[/#if]
<cmis:object>
<cmis:properties>

[#-- TODO: spec issue: baseType to become formal property --]
[#if row.nodes?? && node.isDocument]
  [@propvalue "BaseType" "document" "STRING"/]
[#else]
  [@propvalue "BaseType" "folder" "STRING"/]
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
[#if row.nodes?? && includeallowableactions][@allowableactions node/][/#if]
</cmis:object>
<cmis:terminator/>
[#if row.nodes??]<alf:icon>${absurl(url.context)}${node.icon16}</alf:icon>[/#if]
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
[#-- TODO: check validity of abs url prefix --]
<cmis:propertyUri cmis:name="${name}">[@urivalue absurl(url.serviceContext) + value/]</cmis:propertyUri>
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
<cmis:terminator/>
[/@entry]
[/#macro]

[#macro typedefCMISLinks typedef]
<link rel="type" href="${absurl(url.serviceContext)}/api/type/${typedef.typeId.id}"/>
[#if typedef.parentType??]
<link rel="parent" href="${absurl(url.serviceContext)}/api/type/${typedef.parentType.typeId.id}"/>
[/#if]
<link rel="children" href="${absurl(url.serviceContext)}/api/type/${typedef.typeId.id}/children"/>
<link rel="descendants" href="${absurl(url.serviceContext)}/api/type/${typedef.typeId.id}/descendants"/>
<link rel="repository" href="[@serviceuri/]"/>
[/#macro]

[#macro typedefCMISProps typedef includeProperties=true includeInheritedProperties=true]
[#if typedef.baseType.typeId.id = "document"]
[@documenttypedefCMISProps typedef includeProperties includeInheritedProperties/]
[#elseif typedef.baseType.typeId.id = "folder"]
[@foldertypedefCMISProps typedef includeProperties includeInheritedProperties/]
[#elseif typedef.baseType.typeId.id = "relationship"]
[@relationshiptypedefCMISProps typedef includeProperties includeInheritedProperties/]
[#elseif typedef.baseType.typeId.id = "policy"]
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
  <cmis:typeId>${typedef.typeId.id}</cmis:typeId>
  <cmis:queryName>${typedef.queryName}</cmis:queryName>
  <cmis:displayName>[#if typedef.displayName??]${typedef.displayName?xml}[/#if]</cmis:displayName>
  <cmis:baseType>${typedef.baseType.typeId.id}</cmis:baseType>  [#-- TODO: remove spec issue 36 --]
  <cmis:baseTypeQueryName>${typedef.baseType.queryName}</cmis:baseTypeQueryName>
[#if typedef.parentType??]  
  <cmis:parentId>${typedef.parentType.typeId.id}</cmis:parentId>
[/#if]
  <cmis:description>[#if typedef.description??]${typedef.description?xml}[/#if]</cmis:description>
  <cmis:creatable>${typedef.creatable?string}</cmis:creatable>
  <cmis:fileable>${typedef.fileable?string}</cmis:fileable>
  <cmis:queryable>${typedef.queryable?string}</cmis:queryable>
  <cmis:controllable>${typedef.controllable?string}</cmis:controllable>
  <cmis:includedInSupertypeQuery>${typedef.includeInSuperTypeQuery?string}</cmis:includedInSupertypeQuery>
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
  <cmis:name>${propdef.propertyId.name}</cmis:name>
  <cmis:id>${propdef.propertyId}</cmis:id>
  [#-- TODO: CMIS spec issue: wait for definition of this --]
  <cmis:package>TODO</cmis:package>
  <cmis:displayName>[#if propdef.displayName??]${propdef.displayName?xml}[/#if]</cmis:displayName>
[#if propdef.description??]
  <cmis:description>${propdef.description?xml}</cmis:description>
[/#if]
  <cmis:propertyType>${propdef.dataType.label}</cmis:propertyType>
  <cmis:cardinality>${propdef.cardinality.label}</cmis:cardinality>
  <cmis:updateability>${propdef.updatability.label}</cmis:updateability>
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