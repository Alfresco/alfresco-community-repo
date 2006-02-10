<?xml version="1.0"?>
<rss version="2.0">
   <channel>
      <title>Alfresco</title>
      <copyright>Copyright (c) 2005 Alfresco Software, Inc. All rights reserved.</copyright>
      <#assign hostname="http://localhost:8080/alfresco">
      <#assign spaceref="${hostname}/navigate/browse/${space.nodeRef.storeRef.protocol}/${space.nodeRef.storeRef.identifier}/${space.nodeRef.id}">
      <#assign datetimeformat="EEE, dd MMM yyyy hh:mm:ss zzz">
      <link>${spaceref}</link>
      <description>Recent Changes to '${space.name}'</description>
      <language>en-us</language>
      <lastBuildDate>${date?string(datetimeformat)}</lastBuildDate>
      <pubDate>${date?string(datetimeformat)}</pubDate>
      <ttl>120</ttl>
      <generator>Alfresco 1.2</generator>
      <image>
         <title>${space.name}</title>
         <width>32</width>
         <height>32</height>
         <link>${spaceref}</link>
         <url>${hostname}${space.icon32}</url>
      </image>
      <#assign weekms=1000*60*60*24*7>
      <#list space.childrenByXPath[".//*[subtypeOf('cm:content')]"] as child>
      <#if (dateCompare(child.properties["cm:modified"], date, weekms) == 1) || (dateCompare(child.properties["cm:created"], date, weekms) == 1)>
      <item>
         <title>${child.properties.name}</title>
         <link>${hostname}${child.url}</link>
         <description>
            ${"<a href='${hostname}${child.url}'>"?xml}${child.properties.name}${"</a>"?xml}
            <#if child.properties["cm:description"]?exists && child.properties["cm:description"] != "">
               ${child.properties["cm:description"]}
            </#if>
         </description>
         <pubDate>${child.properties["cm:modified"]?string(datetimeformat)}</pubDate>
         <guid isPermaLink="false">${hostname}${child.url}</guid>
      </item>
      </#if>
      </#list>
    
   </channel>
</rss>
