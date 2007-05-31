Back to [[HTTP API]].


'''NOTE:'''<br>
'''This document describes features to be found in Alfresco v2.1 (not yet released).'''<br>
'''Documentation for v2.0 can be found [[REST|here]].'''


= Introduction =

This page provides reference documentation for the Alfresco [[HTTP API]].

Generated on ${date?datetime} from

 GET <nowiki>http://</nowiki><host>:<port>/alfresco/service/indexall.mediawiki

= API Reference =

The following reference provides a list of all available Web Scripts organised by Web Script package.
<#macro recursepackage package>
<#if package.scripts?size &gt; 0>
== Package: ${package.path} ==
<#list package.scripts as webscript>
<#assign desc = webscript.description>

=== ${desc.shortName} ===

<#if desc.description??><#if desc.description?ends_with(".")>${desc.description}<#else>${desc.description}.</#if><#else></#if>

<#list desc.URIs as uri>
 [http://localhost:8080/${url.serviceContext}${uri.URI} ${desc.method} ${url.serviceContext}${uri.URI}] => ${uri.format}<#if uri.format = desc.defaultFormat> (default)</#if>
</#list>

Requirements:
* Authentication: ${desc.requiredAuthentication}
* Transaction: ${desc.requiredTransaction}
* Format Style: ${desc.formatStyle}

Definition:
* Id: ${desc.id}
* Description: ${desc.storePath}/${desc.descPath}
</#list>
</#if>
<#list package.children as childpath>
  <@recursepackage package=childpath/>
</#list>
</#macro>
    
<@recursepackage package=rootpackage/>
