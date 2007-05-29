Back to [[HTTP API]].


'''NOTE:'''<br>
'''This document describes features to be found in Alfresco v2.1 (not yet released).'''<br>
'''Documentation for v2.0 can be found [[REST|here]].'''


= Introduction =

This page provides reference documentation for the Alfresco [[HTTP API]].

Generated on ${date?datetime} from

 GET <nowiki>http://</nowiki><host>:<port>/alfresco/service/index?format=wiki

= API Reference =
<#list webscripts as webscript>
<#assign desc = webscript.description>

== ${desc.shortName} ==

<#if desc.description??><#if desc.description?ends_with(".")>${desc.description}<#else>${desc.description}.</#if><#else></#if>

<#list desc.URIs as uri>
 [http://localhost:8080/${url.serviceContext}${uri.URI} ${desc.method} ${url.serviceContext}${uri.URI}] => ${uri.format}<#if uri.format = desc.defaultFormat> (default)</#if>
</#list>

Requirements:
* Authentication: ${desc.requiredAuthentication}
* Transaction: ${desc.requiredTransaction}

Definition:
* Id: ${desc.id}
* Store: ${desc.storePath}/${desc.descPath}

</#list>