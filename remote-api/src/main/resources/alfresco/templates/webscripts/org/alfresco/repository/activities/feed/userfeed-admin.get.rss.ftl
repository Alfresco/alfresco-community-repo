<?xml version="1.0" encoding="UTF-8"?>
<rss version="2.0">
   <channel>
      <title>Alfresco Activities User Feed for ${feedUserId?xml}</title>
      <copyright>Copyright (c) 2008-2010 Alfresco Software, Inc. All rights reserved.</copyright>
      <link></link>
      <description>Alfresco Activities User Feed for ${feedUserId?xml}</description>
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