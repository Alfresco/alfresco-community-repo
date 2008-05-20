<?xml version="1.0" encoding="UTF-8"?>
<feed xmlns="http://www.w3.org/2005/Atom">
<generator version="0.1">Alfresco (0.1)</generator>
<link rel="self" href=""/>
<title>Alfresco Activities Site Feed for ${siteId}</title>
<#if feedEntries??>
<#list feedEntries as feedEntry>${feedEntry.activitySummary}</#list>
<#else></#if>
</feed>