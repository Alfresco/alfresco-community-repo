[#ftl]

[#--            --]
[#-- ATOM Entry --]
[#--            --]

[#macro entry ns=""]
<entry[#if ns != ""] ${ns}[/#if]>
[#nested]
</entry>
[/#macro]

[#macro nodeCMISProps node propfilter]
<cmis:properties>
  [@typeCMISProps node cmistype(node) propfilter/]
  [#-- Nest the Alfresco extension for aspects and their properties --]
  <alf:aspects>
    [#list cmisaspects(node) as aspectdef]
      <alf:appliedAspects>${aspectdef.typeId.id}</alf:appliedAspects>
    [/#list]
    <alf:properties>
      [#list cmisaspects(node) as aspectdef]
        [@typeCMISProps node aspectdef propfilter/]
      [/#list]
    </alf:properties>
  </alf:aspects>
</cmis:properties>
[/#macro]

[#macro objectCMISProps object propfilter]
<cmis:properties>
  [@typeCMISProps object cmistype(object) propfilter/]
</cmis:properties>
[/#macro]

[#macro typeCMISProps object typedef propfilter]
  [#list typedef.propertyDefinitions?values as propdef]
    [@filter propfilter propdef.queryName][@prop object propdef/][/@filter]
  [/#list]
[/#macro]


[#--                         --]
[#-- ATOM Entry for Document --]
[#--                         --]

[#macro document node renditionfilter="cmis:none" propfilter="*" includeallowableactions=false includerelationships="none" includeacl=false ns=""]
[@entry ns]
<author><name>${node.properties.creator!""}</name></author>
[@contentstream node/]
<id>urn:uuid:${node.id}</id>
[@linksLib.linknodeself node/]
[@linksLib.linkstream node "enclosure"/]
[@linksLib.linknodeedit node/]
[@linksLib.linkstream node "edit-media"/]
[@documentCMISLinks node=node/]
[#local renditionsMap=cmisrenditions(node, renditionfilter)/]
[@renditionLinks node renditionsMap/]
<published>${xmldate(node.properties.created)}</published>
<summary>[@contentsummary node/]</summary>
<title>${node.name?xml}</title>
<updated>${xmldate(node.properties.modified)}</updated>
<app:edited>${xmldate(node.properties.modified)}</app:edited>
<alf:icon>${absurl(url.context)}${node.icon16}</alf:icon>
<cmisra:object>
[@nodeCMISProps node propfilter/]
[#if includeallowableactions][@allowableactions node/][/#if]
[@relationships node includerelationships includeallowableactions propfilter/]
[#if includeacl][@aclreport node/][/#if]
[@renditions renditionsMap/]
</cmisra:object>
<cmisra:pathSegment>${node.name?xml}</cmisra:pathSegment>
[/@entry]
[/#macro]

[#macro documentCMISLinks node]
[@linksLib.linkallowableactions node/]
[@linksLib.linkrelationships node/]
[@linksLib.linkpolicies node/]
[@linksLib.linkacl node/]
[@linksLib.linkparents node/]
[@linksLib.linkversions node/]
[#if node.isWorkingCopy]
  [#local nodeuri][@linksLib.nodeuri cmisproperty(node, cmisconstants.PROP_VERSION_SERIES_ID, true)/][/#local]
  [@linksLib.linkvia href="${nodeuri}"/]
[#else]
  [@linksLib.linkcurrentversion node/]
  [#if cmisproperty(node, cmisconstants.PROP_IS_VERSION_SERIES_CHECKED_OUT)][@linksLib.linkpwc cmisproperty(node, cmisconstants.PROP_VERSION_SERIES_CHECKED_OUT_ID, true)/][/#if]
[/#if]
[@linksLib.linktype node/]
[@linksLib.linkservice/]
[/#macro]

[#--                       --]
[#-- ATOM Entry for Folder --]
[#--                       --]

[#macro foldertree node renditionfilter="cmis:none" propfilter="*" includeallowableactions=false includerelationships="none" includeacl=false ns="" maxdepth=-1]
[@folder node renditionfilter propfilter "folders" includeallowableactions includerelationships includeacl ns 1 maxdepth "" "tree"/]
[/#macro]

[#macro folder node renditionfilter="cmis:none" propfilter="*" typesfilter="any" includeallowableactions=false includerelationships="none" includeacl=false ns="" depth=1 maxdepth=1 relativePathSegment="" nestedkind=""]
[@entry ns]
<author><name>${node.properties.creator!""}</name></author>
[@contentstream node/]
<id>urn:uuid:${node.id}</id>
[@linksLib.linknodeself node/]
[@linksLib.linknodeedit node/]
[@folderCMISLinks node/]
<published>${xmldate(node.properties.created)}</published>
<summary>[@foldersummary node/]</summary>
<title>${node.name?xml}</title>
<updated>${xmldate(node.properties.modified)}</updated>
<app:edited>${xmldate(node.properties.modified)}</app:edited>
<alf:icon>${absurl(url.context)}${node.icon16}</alf:icon>
<cmisra:object>
[@nodeCMISProps node propfilter/]
[#if includeallowableactions][@allowableactions node/][/#if]
[@relationships node includerelationships includeallowableactions propfilter/]
[#if includeacl][@aclreport node/][/#if]
</cmisra:object>
<cmisra:pathSegment>${node.name?xml}</cmisra:pathSegment>
[#if relativePathSegment != ""]
<cmisra:relativePathSegment>${relativePathSegment?xml}</cmisra:relativePathSegment>
[/#if]
[#-- recurse for depth greater than 1 --]
[#if maxdepth == -1 || depth &lt; maxdepth]
[#assign nested = cmischildren(node, typesfilter)/]
[#if nested?size > 0]
<cmisra:children>
[@feedLib.feed]
[@feedLib.node node "${nestedkind}"]
  [#if nestedkind == "tree"][@linksLib.linktree node "self"/][#else][@linksLib.linkdescendants node "self"/][/#if]
[/@feedLib.node]
[#list nested as child]
  [#if child.isDocument]
    [@document child renditionfilter propfilter includeallowableactions includerelationships includeacl/]
  [#else]
    [@folder child renditionfilter propfilter typesfilter includeallowableactions includerelationships includeacl ns depth+1 maxdepth "" nestedkind/]
  [/#if]
[/#list]
[/@feedLib.feed]
</cmisra:children>
[/#if]
[/#if]
[/@entry]
[/#macro]

[#macro folderCMISLinks node]
[@linksLib.linkallowableactions node/]
[@linksLib.linkrelationships node/]
[@linksLib.linkpolicies node/]
[@linksLib.linkacl node/]
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
<author><name>${assoc.source.properties.creator!""}</name></author>
<content>[@namedvalue cmisconstants.PROP_OBJECT_ID assoc cmisconstants.DATATYPE_ID/]</content>
<id>[@namedvalue cmisconstants.PROP_OBJECT_ID assoc cmisconstants.DATATYPE_ID/]</id>   [#-- TODO: id compliant --]
[@linksLib.linkassocself assoc/]
[@linksLib.linkassocedit assoc/]
[@assocCMISLinks assoc=assoc/]
<title>[@namedvalue cmisconstants.PROP_NAME assoc cmisconstants.DATATYPE_STRING/]</title>
<updated>${xmldate(date)}</updated>  [#-- TODO: [@namedvalue cmisconstants.PROP_LAST_MODIFICATION_DATE assoc cmisconstants.DATATYPE_DATETIME/] --]
<app:edited>${xmldate(date)}</app:edited>  [#-- TODO: [@namedvalue cmisconstants.PROP_LAST_MODIFICATION_DATE assoc cmisconstants.DATATYPE_DATETIME/] --]
<cmisra:object>
[@objectCMISProps assoc propfilter/]
[#if includeallowableactions][@assocallowableactions assoc/][/#if]
</cmisra:object>
[/@entry]
[/#macro]

[#macro assocCMISLinks assoc]
[@linksLib.linkassocallowableactions assoc/]
[@linksLib.linktype assoc/]
[@linksLib.linktosource assoc.source/]
[@linksLib.linktotarget assoc.target/]
[@linksLib.linkservice/]
[/#macro]


[#--                          --]
[#-- ATOM Entry for Query Row --]
[#--                          --]

[#-- TODO: spec issue 47 --]
[#macro row row renditionfilter="cmis:none" includeallowableactions=false includerelationships="none"]
[@entry]
[#-- TODO: calculate multiNodeResultSet from result set --]
[#if row.node??]
[#assign node = row.node/]
<author><name>${node.properties.creator!""}</name></author>
[@contentstream node/]
<id>urn:uuid:${node.id}</id>
[@linksLib.linknodeself node/]
[@linksLib.linknodeedit node/]
[#if node.isDocument]
  [@linksLib.linkstream node "enclosure"/]
  [@linksLib.linkstream node "edit-media"/]
  [@documentCMISLinks node=node/]
  [#assign renditionsMap=cmisrenditions(node, renditionfilter)/]
  [@renditionLinks node renditionsMap/]
[#else]
  [@folderCMISLinks node=node/]
[/#if]
<title>${node.name?xml}</title>
<updated>${xmldate(node.properties.modified)}</updated>
<alf:icon>${absurl(url.context)}${node.icon16}</alf:icon>
[#else]
<author><name>${person.properties.userName?xml}</name></author>
<id>urn:uuid:row-${row.index?c}</id>
<title>Row ${row.index?c}</title>
<updated>${xmldate(now)}</updated>
[/#if]
<cmisra:object>
<cmis:properties>
[#assign rowvalues = row.values]
[#list rowvalues?keys as colname]
  [#assign coltype = row.getColumnType(colname)]
  [#if row.getPropertyDefinition(colname)??]
    [#assign propdef = row.getPropertyDefinition(colname)]
    [#if rowvalues[colname]??]
      [@propvalue rowvalues[colname] coltype propdef.propertyId.id propdef.displayName colname propdef.propertyId.localName/]
    [#else]
      [@propnull coltype propdef.propertyId.id propdef.displayName colname propdef.propertyId.localName/]
    [/#if]
  [#else]
    [#if rowvalues[colname]??]
      [@propvalue value=rowvalues[colname] type=coltype queryname=colname/]
    [#else]
      [@propnull type=coltype queryname=colname/]
    [/#if]
  [/#if]
[/#list]
</cmis:properties>
[#if node??]
[#if includeallowableactions][@allowableactions node/][/#if]
[@relationships node includerelationships includeallowableactions/]
[#if node.isDocument]
[@renditions renditionsMap/]
[/#if]
[/#if]
</cmisra:object>
[/@entry]
[/#macro]

[#--                                 --]
[#-- ATOM Entry for Change Log Entry --]
[#--                                 --]

[#macro changeentry event node includeacl=false]
[@entry]
<author><name>${person.properties.userName}</name></author>
<id>urn:uuid:${node.id}</id>
[#if node.exists][@linksLib.linkacl node/][/#if]
[@linksLib.linkservice/]
<title>Change Log Entry</title>
<updated>${xmldate(event.changeTime)}</updated>
<cmisra:object>
<cmis:properties>
  [@propvalue event.objectId cmisconstants.DATATYPE_ID "cmis:objectId"/]
</cmis:properties>    
<cmis:changeEventInfo>
  <cmis:changeType>${event.changeType.label}</cmis:changeType>
  <cmis:changeTime>${xmldate(event.changeTime)}</cmis:changeTime>
</cmis:changeEventInfo>
[#if includeacl && node.exists][@aclreport node/][/#if]
</cmisra:object>
[/@entry]
[/#macro]

[#--                 --]
[#-- CMIS Properties --]
[#--                 --]

[#macro filter filter value]
[#if filter == "*" || filter?index_of(value) != -1 || filter?matches(value,'i')][#nested][/#if]
[/#macro]

[#macro prop object propdef]
[#assign value=cmisproperty(object, propdef.propertyId.id)/]
[#if value?is_string || value?is_number || value?is_boolean || value?is_date || value?is_enumerable]
[@propvaluedef value propdef/]
[#elseif value.class.canonicalName?ends_with("NULL")]
[@propnulldef propdef/]
[/#if]
[/#macro]

[#macro propvaluedef value propdef]
  [@propvalue value propdef.dataType.label propdef.propertyId.id propdef.displayName propdef.queryName propdef.propertyId.localName/]
[/#macro]

[#macro propvalue value type defid="" displayname="" queryname="" localname=""]
[#if type == cmisconstants.DATATYPE_STRING]
<cmis:propertyString [@propargs defid displayname queryname localname/]>[@values value;v]<cmis:value>[@stringvalue v/]</cmis:value>[/@values]</cmis:propertyString>
[#elseif type == cmisconstants.DATATYPE_INTEGER]
<cmis:propertyInteger [@propargs defid displayname queryname localname/]>[@values value;v]<cmis:value>[@integervalue v/]</cmis:value>[/@values]</cmis:propertyInteger>
[#elseif type == cmisconstants.DATATYPE_DECIMAL]
<cmis:propertyDecimal [@propargs defid displayname queryname localname/]>[@values value;v]<cmis:value>[@decimalvalue v/]</cmis:value>[/@values]</cmis:propertyDecimal>
[#elseif type == cmisconstants.DATATYPE_BOOLEAN]
<cmis:propertyBoolean [@propargs defid displayname queryname localname/]>[@values value;v]<cmis:value>[@booleanvalue v/]</cmis:value>[/@values]</cmis:propertyBoolean>
[#elseif type == cmisconstants.DATATYPE_DATETIME]
<cmis:propertyDateTime [@propargs defid displayname queryname localname/]>[@values value;v]<cmis:value>[@datetimevalue v/]</cmis:value>[/@values]</cmis:propertyDateTime>
[#elseif type == cmisconstants.DATATYPE_URI]
[#-- TODO: check validity of abs url prefix --]
<cmis:propertyUri [@propargs defid displayname queryname localname/]>[@values value;v]<cmis:value>[@urivalue absurl(url.serviceContext) + v/]</cmis:value>[/@values]</cmis:propertyUri>
[#elseif type == cmisconstants.DATATYPE_ID]
<cmis:propertyId [@propargs defid displayname queryname localname/]>[@values value;v]<cmis:value>[@idvalue v/]</cmis:value>[/@values]</cmis:propertyId>
[#-- TODO: remaining property types --]
[/#if]
[/#macro]

[#macro propnulldef propdef]
  [@propnull propdef.dataType.label propdef.propertyId.id propdef.displayName propdef.queryName propdef.propertyId.localName/]
[/#macro]

[#macro propnull type defid="" displayname="" queryname="" localname=""]
[#if type == cmisconstants.DATATYPE_STRING]
<cmis:propertyString [@propargs defid displayname queryname localname/]/>
[#elseif type == cmisconstants.DATATYPE_INTEGER]
<cmis:propertyInteger [@propargs defid displayname queryname localname/]/>
[#elseif type == cmisconstants.DATATYPE_DECIMAL]
<cmis:propertyDecimal [@propargs defid displayname queryname localname/]/>
[#elseif type == cmisconstants.DATATYPE_BOOLEAN]
<cmis:propertyBoolean [@propargs defid displayname queryname localname/]/>
[#elseif type == cmisconstants.DATATYPE_DATETIME]
<cmis:propertyDateTime [@propargs defid displayname queryname localname/]/>
[#elseif type == cmisconstants.DATATYPE_URI]
<cmis:propertyUri [@propargs defid displayname queryname localname/]/>
[#elseif type == cmisconstants.DATATYPE_ID]
<cmis:propertyId [@propargs defid displayname queryname localname/]/>
[#-- TODO: remaining property types --]
[/#if]
[/#macro]

[#macro propargs defid="" displayname="" queryname="" localname=""][#if defid !=""]propertyDefinitionId="${defid?xml}"[/#if][#if displayname != ""] displayName="${displayname?xml}"[/#if][#if queryname != ""] queryName="${queryname?xml}"[/#if][/#macro]

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

[#macro stringvalue value]${value?xml}[/#macro]
[#macro integervalue value]${value?c}[/#macro]
[#macro decimalvalue value]${value?c}[/#macro]
[#macro booleanvalue value]${value?string}[/#macro]
[#macro datetimevalue value]${xmldate(value)}[/#macro]
[#macro urivalue value]${value?xml}[/#macro]
[#macro idvalue value][#if value?is_hash && value.nodeRef??]${value.nodeRef?xml}[#else]${value?xml}[/#if][/#macro]

[#--                    --]
[#-- CMIS Relationships --]
[#--                    --]

[#macro relationships node includerelationships="none" includeallowableactions=false propfilter="*"]
[#if includerelationships != "none"]
[#list cmisassocs(node, includerelationships) as assoc]
  <cmis:relationship>
    [@objectCMISProps assoc propfilter/]
    [#if includeallowableactions][@assocallowableactions assoc/][/#if]
  </cmis:relationship>
[/#list]
[/#if]
[/#macro]

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

[#macro assocallowableactions assoc ns=""]
<cmis:allowableActions[#if ns != ""] ${ns}[/#if]>
[#nested]
[#assign typedef = cmistype(assoc)]
[#list typedef.actionEvaluators?values as actionevaluator]
  [@assocallowableaction assoc actionevaluator/]
[/#list]
</cmis:allowableActions>
[/#macro]

[#macro assocallowableaction assoc actionevaluator]
<cmis:${actionevaluator.action.label}>${actionevaluator.isAllowed(assoc.associationRef)?string}</cmis:${actionevaluator.action.label}>
[/#macro]

[#--                           --]
[#-- CMIS Access Control Lists --]
[#--                           --]

[#macro aclreport node ns=""]
<cmis:acl[#if ns != ""] ${ns}[/#if]>
[#local report = cmisacl(node)]
[#list report.accessControlEntries as entry]
  [@accessControlEntry entry/]
[/#list]
</cmis:acl>
[#if ns == ""]
<cmis:exactACL>${(report.exact)?string}</cmis:exactACL>
[/#if]
[/#macro]

[#macro accessControlEntry entry]
<cmis:permission>
  <cmis:principal>
    <cmis:principalId>${entry.principalId}</cmis:principalId>
  </cmis:principal>
  <cmis:permission>${entry.permission}</cmis:permission>
  <cmis:direct>${entry.direct?string}</cmis:direct>
</cmis:permission>
[/#macro]

[#--            --]
[#-- Renditions --]
[#--            --]

[#macro renditionLinks node renditionsMap]
[#list renditionsMap.renditions as rendition]
[@linksLib.linkrendition node=node rendition=rendition renditionNode=renditionsMap.renditionNodes[rendition_index]/]
[/#list]
[/#macro]

[#macro renditions renditionsMap]
[#list renditionsMap.renditions as rendition]
<cmis:rendition>
  <cmis:streamId>${rendition.streamId}</cmis:streamId>
  <cmis:mimetype>${rendition.mimeType}</cmis:mimetype>
  <cmis:length>[#if rendition.length??]${rendition.length?c}[#else]-1[/#if]</cmis:length>
  <cmis:kind>${rendition.kind}</cmis:kind>
  [#if rendition.title??]<cmis:title>${rendition.title}</cmis:title>[/#if]
  [#if rendition.height??]<cmis:height>${rendition.height?c}</cmis:height>[/#if]
  [#if rendition.width??]<cmis:width>${rendition.width?c}</cmis:width>[/#if]
</cmis:rendition>
[/#list]
[/#macro]

[#--                                --]
[#-- ATOM Entry for Type Definition --]
[#--                                --]

[#macro typedef typedefn includeProperties=true includeInheritedProperties=true ns="" depth=1 maxdepth=1]
[@entry ns=ns]
<author><name>${person.properties.userName?xml}</name></author>
<content>${typedefn.typeId.id}</content>  [#-- TODO --]
<id>urn:uuid:type-${typedefn.typeId.id}</id>
[@linksLib.linktypeself typedefn/]
[@typedefCMISLinks typedefn/]
<summary>[#if typedefn.description??]${typedefn.description?xml}[#else]${typedefn.displayName?xml}[/#if]</summary>
<title>${typedefn.displayName?xml}</title>
<updated>${xmldate(date)}</updated>  [#-- TODO --]
[@typedefCMISProps typedefn includeProperties/]
[#-- recurse for depth greater than 1 --]
[#if maxdepth == -1 || depth &lt; maxdepth]
[#assign nested = typedefn.getSubTypes(false)/]
[#if nested?size > 0]
<cmisra:children>
[@feedLib.feed]
[@feedLib.generic "urn:uuid:type-${typedefn.typeId.id}-descendants" "Type ${typedefn.displayName} Descendants" "${person.properties.userName}"]
  [@linksLib.linktypedescendants typedefn "self"/]
[/@feedLib.generic]
[#list nested as child]
  [@typedef child includeProperties includeInheritedProperties ns depth+1 maxdepth/]
[/#list]
[/@feedLib.feed]
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
  <cmis:localName>${typedef.typeId.localName?xml}</cmis:localName>
  <cmis:localNamespace>${typedef.typeId.localNamespace?xml}</cmis:localNamespace>
  <cmis:displayName>[#if typedef.displayName??]${typedef.displayName?xml}[/#if]</cmis:displayName>
  <cmis:queryName>${typedef.queryName?xml}</cmis:queryName>
  <cmis:description>[#if typedef.description??]${typedef.description?xml}[/#if]</cmis:description>
  <cmis:baseId>${typedef.baseType.typeId.id}</cmis:baseId>
[#if typedef.parentType??]  
  <cmis:parentId>${typedef.parentType.typeId.id}</cmis:parentId>
[/#if]
  <cmis:creatable>${typedef.creatable?string}</cmis:creatable>
  <cmis:fileable>${typedef.fileable?string}</cmis:fileable>
  <cmis:queryable>${typedef.queryable?string}</cmis:queryable>
  <cmis:fulltextIndexed>${typedef.fullTextIndexed?string}</cmis:fulltextIndexed>
  <cmis:includedInSupertypeQuery>${typedef.includedInSuperTypeQuery?string}</cmis:includedInSupertypeQuery>
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
  <cmis:defaultValue><cmis:value>${propdef.defaultValue?xml}</cmis:value></cmis:defaultValue>
[/#if]
  [@cmisChoices propdef.choices propdef.dataType.label/]
</cmis:propertyBooleanDefinition>
[/#macro]

[#macro idpropdefCMISProps propdef inherited=false]
<cmis:propertyIdDefinition>
[@abstractpropdefCMISProps propdef inherited/]
[#if propdef.defaultValue??]
  <cmis:defaultValue><cmis:value>${propdef.defaultValue?xml}</cmis:value></cmis:defaultValue>
[/#if]
  [@cmisChoices propdef.choices propdef.dataType.label/]
</cmis:propertyIdDefinition>
[/#macro]

[#macro integerpropdefCMISProps propdef inherited=false]
<cmis:propertyIntegerDefinition>
[@abstractpropdefCMISProps propdef inherited/]
[#if propdef.defaultValue??]
  <cmis:defaultValue><cmis:value>${propdef.defaultValue?xml}</cmis:value></cmis:defaultValue>
[/#if]
[#if propdef.minValue??]
  <cmis:minValue>${propdef.minValue?c}</cmis:minValue>
[/#if]
[#if propdef.maxValue??]
  <cmis:maxValue>${propdef.maxValue?c}</cmis:maxValue>
[/#if]
  [@cmisChoices propdef.choices propdef.dataType.label/]
</cmis:propertyIntegerDefinition>
[/#macro]

[#macro datetimepropdefCMISProps propdef inherited=false]
<cmis:propertyDateTimeDefinition>
[@abstractpropdefCMISProps propdef inherited/]
[#if propdef.defaultValue??]
  <cmis:defaultValue><cmis:value>${propdef.defaultValue?xml}</cmis:value></cmis:defaultValue>
[/#if]
  [@cmisChoices propdef.choices propdef.dataType.label/]
</cmis:propertyDateTimeDefinition>
[/#macro]

[#macro decimalpropdefCMISProps propdef inherited=false]
<cmis:propertyDecimalDefinition>
[@abstractpropdefCMISProps propdef inherited/]
[#if propdef.defaultValue??]
  <cmis:defaultValue><cmis:value>${propdef.defaultValue?xml}</cmis:value></cmis:defaultValue>
[/#if]
[#if propdef.minValue??]
  <cmis:minValue>${propdef.minValue?c}</cmis:minValue>
[/#if]
[#if propdef.maxValue??]
  <cmis:maxValue>${propdef.maxValue?c}</cmis:maxValue>
[/#if]
  [@cmisChoices propdef.choices propdef.dataType.label/]
</cmis:propertyDecimalDefinition>
[/#macro]

[#macro htmlpropdefCMISProps propdef inherited=false]
<cmis:propertyHtmlDefinition>
[@abstractpropdefCMISProps propdef inherited/]
[#if propdef.defaultValue??]
  <cmis:defaultValue><cmis:value>${propdef.defaultValue?xml}</cmis:value></cmis:defaultValue>
[/#if]
  [@cmisChoices propdef.choices propdef.dataType.label/]
</cmis:propertyHtmlDefinition>
[/#macro]

[#macro stringpropdefCMISProps propdef inherited=false]
<cmis:propertyStringDefinition>
[@abstractpropdefCMISProps propdef inherited/]
[#if propdef.defaultValue??]
  <cmis:defaultValue><cmis:value>${propdef.defaultValue?xml}</cmis:value></cmis:defaultValue>
[/#if]
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
  <cmis:defaultValue><cmis:value>${propdef.defaultValue?xml}</cmis:value></cmis:defaultValue>
[/#if]
  [@cmisChoices propdef.choices propdef.dataType.label/]
</cmis:propertyUriDefinition>
[/#macro]

[#macro abstractpropdefCMISProps propdef inherited=false]
  <cmis:id>${propdef.propertyId.id}</cmis:id>
  <cmis:localName>${propdef.propertyId.localName?xml}</cmis:localName>
  <cmis:localNamespace>${propdef.propertyId.localNamespace?xml}</cmis:localNamespace>
  <cmis:displayName>[#if propdef.displayName??]${propdef.displayName?xml}[/#if]</cmis:displayName>
  <cmis:queryName>${propdef.queryName?xml}</cmis:queryName>
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
<cmis:choiceString displayName="${choice.name?xml}">
[@cmisChoices choice.children type/]
<cmis:value>[@stringvalue choice.value/]</cmis:value>
</cmis:choiceString>
[#elseif type == cmisconstants.DATATYPE_INTEGER]
<cmis:choiceInteger displayName="${choice.name?xml}">
[@cmisChoices choice.children type/]
<cmis:value>[@stringvalue choice.value/]</cmis:value>
</cmis:choiceInteger>
[#elseif type == cmisconstants.DATATYPE_DECIMAL]
<cmis:choiceDecimal displayName="${choice.name?xml}">
[@cmisChoices choice.children type/]
<cmis:value>[@stringvalue choice.value/]</cmis:value>
</cmis:choiceDecimal>
[#elseif type == cmisconstants.DATATYPE_BOOLEAN]
<cmis:choiceBoolean displayName="${choice.name?xml}">
[@cmisChoices choice.children type/]
<cmis:value>[@stringvalue choice.value/]</cmis:value>
</cmis:choiceBoolean>
[#elseif type == cmisconstants.DATATYPE_DATETIME]
<cmis:choiceDateTime displayName="${choice.name?xml}">
[@cmisChoices choice.children type/]
<cmis:value>[@stringvalue choice.value/]</cmis:value>
</cmis:choiceDateTime>
[#elseif type == cmisconstants.DATATYPE_URI]
<cmis:choiceUri displayName="${choice.name?xml}">
[@cmisChoices choice.children type/]
<cmis:value>[@stringvalue choice.value/]</cmis:value>
</cmis:choiceUri>
[#elseif type == cmisconstants.DATATYPE_ID]
<cmis:choiceId displayName="${choice.name?xml}">
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
[#macro contentsummary node][#if node.properties.description??]${node.properties.description?xml}[#elseif node.properties.title??]${node.properties.title?xml}[#elseif node.properties.name??]${node.properties.name?xml}[#else][/#if][/#macro]
[#macro foldersummary node][#if node.properties.description??]${node.properties.description?xml}[#elseif node.properties.title??]${node.properties.title?xml}[#elseif node.properties.name??]${node.properties.name?xml}[#else][/#if][/#macro]

[#-- Helper to render Alfresco content type to Atom content type --]
[#macro contenttype type][#if type == "text/html"]text[#elseif type == "text/xhtml"]xhtml[#elseif type == "text/plain"]text[#else]${type}[/#if][/#macro]

[#-- Helper to render atom content element --]
[#macro contentstream node]<content[#if node.mimetype??] type="${node.mimetype}"[/#if] src="[@linksLib.contenturi node/]"/>[/#macro]
