<?xml version="1.0" encoding="UTF-8"?>
<rss version="2.0" xmlns:atom="http://www.w3.org/2005/Atom">
   <channel>
      <title>Alfresco Activities Site Feed for ${siteId}</title>
      <copyright>Copyright (c) 2008-2010 Alfresco Software, Inc. All rights reserved.</copyright>
      <link></link>
      <description>Alfresco Activities Site Feed for ${siteId}</description>
      <language>en-us</language>
      <lastBuildDate>${date?string("EEE, dd MMM yyyy HH:mm:ss zzz")}</lastBuildDate>
      <pubDate>${date?string("EEE, dd MMM yyyy HH:mm:ss zzz")}</pubDate>
      <ttl>120</ttl>
      <generator>Alfresco (0.1)</generator>
<#if feedEntries??>
   <#list feedEntries as feedEntry>
      ${feedEntry.activitySummary}
   </#list>
</#if>
   </channel>
</rss>