Back to [[HTTP API]].


'''NOTE:'''<br>
'''This document describes features to be found in Alfresco v2.1 (not yet released).'''<br>
'''Documentation for v2.0 can be found [[REST|here]].'''


= Introduction =

Welcome to the reference documentation for the Alfresco [[HTTP API]].

This document was generated from the URL

 GET <nowiki>http://</nowiki><host>:<port>${url.service}

on ${date?datetime} using Alfresco v${server.version}.


= Web Script Reference =

This section provides technical information for all ${webscripts?size} [[Web Scripts]], organised by Web Script Package. 

Documentation for each Web Script includes:

* Short Name
* Description
* Available URL templates
* Default [[Web Scripts Framework#HTTP Response Formats|response format]]
* How to specify an alternative response
* Authentication requirements
* Transaction requirements
* Location of Web Script description document

<#macro recursepackage package>
<#if package.scripts?size &gt; 0>
== Package: ${package.path} ==
<#list package.scripts as webscript>
<#assign desc = webscript.description>

=== ${desc.shortName} ===

<#if desc.description??><#if desc.description?ends_with(".")>${desc.description}<#else>${desc.description}.</#if><#else><i>[No description supplied]</i></#if>

<#list desc.URIs as uri>
 [http://localhost:8080/${url.serviceContext}${uri} ${desc.method} ${url.serviceContext}${uri}]
</#list>

Requirements:
* Default Format: ${desc.defaultFormat!"<i>Determined at run-time</i>"}
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
