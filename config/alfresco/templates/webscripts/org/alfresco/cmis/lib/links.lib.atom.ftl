[#ftl]

[#--                                --]
[#-- Link Relations                 --]
[#--                                --]

[#-- Link to repository service document --]
[#macro linkservice]
<link rel="${cmisconstants.REL_SERVICE}" href="${absurl(url.serviceContext)}/cmis"/>
[/#macro]

[#-- Link to node allowable actions --]
[#macro linkallowableactions node]
<link rel="${cmisconstants.REL_ALLOWABLE_ACTIONS}" href="${absurl(url.serviceContext)}[@nodeuri node/]/allowableactions"/>
[/#macro]

[#-- Link to node allowable actions --]
[#macro linkassocallowableactions assoc]
<link rel="${cmisconstants.REL_ALLOWABLE_ACTIONS}" href="${absurl(url.serviceContext)}[@assocuri assoc/]/allowableactions"/>
[/#macro]

[#-- Link to node relationships --]
[#macro linkrelationships node]
<link rel="${cmisconstants.REL_RELATIONSHIPS}" href="${absurl(url.serviceContext)}[@nodeuri node/]/rels"/>
[/#macro]

[#-- Link to node policies --]
[#macro linkpolicies node]
<link rel="${cmisconstants.REL_POLICIES}" href="${absurl(url.serviceContext)}[@nodeuri node/]/pols"/>
[/#macro]

[#-- Link to node acl --]
[#macro linkacl node]
<link rel="${cmisconstants.REL_ACL}" href="${absurl(url.serviceContext)}[@nodeuri node/]/acl"/>
[/#macro]

[#-- Link to node parents --]
[#macro linkparents node]
<link rel="${cmisconstants.REL_UP}" href="${absurl(url.serviceContext)}[@nodeuri node/]/parents" type="${cmisconstants.MIMETYPE_FEED}"/>
[/#macro]

[#-- Link to folder parent --]
[#macro linkparent node]
<link rel="${cmisconstants.REL_UP}" href="${absurl(url.serviceContext)}[@nodeuri node.parent/]" type="${cmisconstants.MIMETYPE_ENTRY}"/>
[/#macro]

[#-- Link to node children --]
[#macro linkchildren node rel=""]
<link rel="[#if rel == ""]${cmisconstants.REL_DOWN}[#else]${rel}[/#if]" href="${absurl(url.serviceContext)}[@nodeuri node/]/children" type="${cmisconstants.MIMETYPE_FEED}"/>
[/#macro]

[#-- Link to node descendants --]
[#macro linkdescendants node rel=""]
<link rel="[#if rel == ""]${cmisconstants.REL_DOWN}[#else]${rel}[/#if]" href="${absurl(url.serviceContext)}[@nodeuri node/]/descendants" type="${cmisconstants.MIMETYPE_CMISTREE}"/>
[/#macro]

[#-- Link to node tree --]
[#macro linktree node rel=""]
<link rel="[#if rel == ""]${cmisconstants.REL_FOLDER_TREE}[#else]${rel}[/#if]" href="${absurl(url.serviceContext)}[@nodeuri node/]/tree" type="${cmisconstants.MIMETYPE_FEED}"/>
[/#macro]

[#-- Link to node versions --]
[#macro linkversions node]
<link rel="${cmisconstants.REL_VERSION_HISTORY}" href="${absurl(url.serviceContext)}[@nodeuri node/]/versions"/>
[/#macro]

[#-- Link to current node version --]
[#macro linkcurrentversion node]
<link rel="${cmisconstants.REL_CURRENT_VERSION}" href="${absurl(url.serviceContext)}[@nodeuri node/]?returnVersion=latest"/>
[/#macro]

[#-- Link to working copy node --]
[#macro linkpwc pwc]
<link rel="${cmisconstants.REL_WORKING_COPY}" href="${absurl(url.serviceContext)}[@pwcuri pwc/]"/>
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
<link[#if rel != ""] rel="${rel}"[/#if] href="[@contenturi node/]"[#if node.mimetype??] type="${node.mimetype}"[/#if]/>
[/#macro]

[#-- Link to node type --]
[#macro linktype object]
<link rel="${cmisconstants.REL_DESCRIBED_BY}" href="${absurl(url.serviceContext)}[@typeuri cmistype(object)/]"/>
[/#macro]

[#-- Link to type parent --]
[#macro linktypeparent typedef]
<link rel="${cmisconstants.REL_UP}" href="${absurl(url.serviceContext)}[@typeuri typedef.parentType/]" type="${cmisconstants.MIMETYPE_ENTRY}"/>
[/#macro]

[#-- Link to type children --]
[#macro linktypechildren typedef]
<link rel="${cmisconstants.REL_DOWN}" href="${absurl(url.serviceContext)}[@typeuri typedef/]/children" type="${cmisconstants.MIMETYPE_FEED}"/>
[/#macro]

[#-- Link to type descendants --]
[#macro linktypedescendants typedef rel=""]
<link rel="[#if rel == ""]${cmisconstants.REL_DOWN}[#else]${rel}[/#if]" href="${absurl(url.serviceContext)}[@typeuri typedef/]/descendants" type="${cmisconstants.MIMETYPE_CMISTREE}"/>
[/#macro]

[#-- Link to self --]
[#macro linkself href="" type=""]
<link rel="self" href="[#if href == ""]${absurl(encodeuri(url.full))?xml}[#else]${absurl(url.serviceContext)}${href}[/#if]"[#if type != ""] type="${type}"[/#if]/>
[/#macro]

[#macro linknodeself node]
<link rel="self" href="${absurl(url.serviceContext)}[#if node.isWorkingCopy][@pwcuri node/][#else][@nodeuri node/][/#if]"/>
[/#macro]

[#macro linkassocself assoc]
<link rel="self" href="${absurl(url.serviceContext)}[@assocuri assoc/]"/>
[/#macro]

[#macro linktypeself typedef]
<link rel="self" href="${absurl(url.serviceContext)}[@typeuri typedef/]"/>
[/#macro]

[#-- Link to edit --]
[#macro linkedit href="" type=""]
<link rel="edit" href="[#if href == ""]${absurl(encodeuri(url.full))?xml}[#else]${absurl(url.serviceContext)}${href}[/#if]"[#if type != ""] type="${type}"[/#if]/>
[/#macro]

[#macro linknodeedit node]
<link rel="edit" href="${absurl(url.serviceContext)}[@nodeuri node/]"/>
[/#macro]

[#macro linkassocedit assoc]
<link rel="edit" href="${absurl(url.serviceContext)}[@assocuri assoc/]"/>
[/#macro]

[#-- Link to via --]
[#macro linkvia href="" type=""]
<link rel="via" href="${absurl(url.serviceContext)}${href}"[#if type != ""] type="${type}"[/#if]/>
[/#macro]

[#-- Link to rendition --]
[#macro linkrendition node rendition renditionNode={}]
[#switch rendition.kind]
[#case "alf:icon16"]
<link rel="${cmisconstants.REL_ALTERNATE}" href="${absurl(url.context)}${node.icon16}"[#if rendition.mimeType??] type="${rendition.mimeType}"[/#if] cmisra:renditionKind="${rendition.kind}"[#if rendition.length??] length="${rendition.length?c}"[/#if][#if rendition.title??] title="${rendition.title}"[/#if]/>
[#break]
[#case "alf:icon32"]
<link rel="${cmisconstants.REL_ALTERNATE}" href="${absurl(url.context)}${node.icon32}"[#if rendition.mimeType??] type="${rendition.mimeType}"[/#if] cmisra:renditionKind="${rendition.kind}"[#if rendition.length??] length="${rendition.length?c}"[/#if][#if rendition.title??] title="${rendition.title}"[/#if]/>
[#break]
[#default]
<link rel="${cmisconstants.REL_ALTERNATE}" href="[@contenturi renditionNode/]"[#if rendition.mimeType??] type="${rendition.mimeType}"[/#if] cmisra:renditionKind="${rendition.kind}"[#if rendition.length??] length="${rendition.length?c}"[/#if][#if rendition.title??] title="${rendition.title}"[/#if]/>
[/#switch]
[/#macro]

[#--                                --]
[#-- URLs                           --]
[#--                                --]

[#-- Helper to render Alfresco service document uri --]
[#macro serviceuri]${absurl(url.serviceContext)}/cmis[/#macro]

[#-- Helper to render Alfresco content stream uri --]
[#macro contenturi node]${absurl(url.serviceContext)}/cmis/[@noderef node/]/content[#if node.properties.name?? && node.properties.name?last_index_of(".") != -1]${stringUtils.urlEncode(node.properties.name?substring(node.properties.name?last_index_of(".")))}[/#if][/#macro]

[#-- Helper to render Store Ref --]
[#macro storeref store]s/${store.protocol}:${store.identifier}[/#macro]

[#-- Helper to render Node Ref --]
[#macro noderef node][@storeref node.nodeRef.storeRef/]/i/${node.nodeRef.id}[/#macro]

[#-- Helper to render Alfresco Node uri --]
[#macro nodeuri node]/cmis/[@noderef node/][/#macro]

[#-- Helper to render Alfresco PWC uri --]
[#macro pwcuri node]/cmis/pwc/[@noderef node/][/#macro]

[#-- Helper to render Alfresco Assoc uri --]
[#macro assocuri assoc]/cmis/rel/${assoc.associationRef.id?c}[/#macro]

[#-- Helper to render Alfresco Type uri --]
[#macro typeuri typedef]/cmis/type/${typedef.typeId.id}[/#macro]
