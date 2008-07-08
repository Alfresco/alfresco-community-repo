<?xml version="1.0" encoding="UTF-8"?>
<rss version="2.0" xmlns:atom="http://www.w3.org/2005/Atom" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
<channel rdf:about="http://www.alfresco.com/">
   <title>Alfresco - Documents</title>
   <link>${absurl(url.context)}/</link>
   <description>Alfresco Document List</description>
   <lastBuildDate>${xmldate(date)}</lastBuildDate>
   <pubDate>${xmldate(date)}</pubDate>
   <generator>Alfresco ${server.edition} v${server.version}</generator>
   <image>
      <title>Alfresco - My Documents</title>
      <url>${absurl(url.context)?replace("alfresco", "slingshot/proxy/alfresco")}/images/logo/AlfrescoLogo200.png</url>
   </image>
<#list doclist.items as item>
   <#assign d = item.asset>
   <#if d.isDocument>
      <#assign isImage=(d.mimetype="image/gif" || d.mimetype="image/jpeg" || d.mimetype="image/png")>
      <#assign isMP3=(d.mimetype="audio/x-mpeg" || d.mimetype="audio/mpeg")>
   <item>
      <title>${d.name?html}</title>
         <#assign navurl='/navigate/showDocDetails/' + d.nodeRef.storeRef.protocol + '/' + d.nodeRef.storeRef.identifier + '/' + d.nodeRef.id>
      <link>${absurl(url.context)?replace("alfresco", "slingshot/proxy/alfresco")}/api/node/content/${d.storeType}/${d.storeId}/${d.id}/${d.name?url}</link>
         <#if isMP3>
      <enclosure url="api/node/content/${d.storeType}/${d.storeId}/${d.id}/${d.name?url}" length="${d.size?string?replace(",","")}" type="audio/mpeg"/>
         </#if>
      <description>
         <#if isImage || true>&lt;img src=&quot;${absurl(url.context)?replace("alfresco", "slingshot/proxy/alfresco")}/api/node/content/${d.storeType}/${d.storeId}/${d.id}/${d.name?url}&quot;&gt;&lt;br/&gt;</#if>
         <#if d.properties.description?exists>${d.properties.description?html}</#if>
      </description>
      <pubDate>${xmldate(d.properties.modified)}</pubDate>
      <guid isPermaLink="false">${d.id}</guid>
   </item>
   </#if>
</#list>
</channel>
</rss>