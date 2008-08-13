<?xml version="1.0" encoding="UTF-8"?>
<rss version="2.0" xmlns:atom="http://www.w3.org/2005/Atom" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
<channel rdf:about="http://www.alfresco.com/">
   <title>Alfresco - Wiki Page</title>
   <link>${absurl(url.context)}/</link>
   <description>Alfresco Wiki Page - Recent Changes</description>
   <lastBuildDate>${xmldate(date)}</lastBuildDate>
   <pubDate>${xmldate(date)}</pubDate>
   <generator>Alfresco ${server.edition} v${server.version}</generator>
   <image>
      <title>Alfresco - Wiki Page Recent Changes</title>
      <url>${absurl(url.context)?replace("alfresco", "share/proxy/alfresco")}/images/logo/AlfrescoLogo200.png</url>
   </image>
   <#list pageList.pages?sort_by(['modified'])?reverse as p>
   <#assign page = p.page>
   <item>
      <title>${page.properties.title!""?html}</title>
         <#assign navurl='/navigate/showDocDetails/' + page.nodeRef.storeRef.protocol + '/' + page.nodeRef.storeRef.identifier + '/' + page.nodeRef.id>
      <link>${absurl(url.context)?replace("alfresco", "share/proxy/alfresco")}/api/node/content/${page.storeType}/${page.storeId}/${page.id}/${page.name?url}</link>
      <pubDate>${xmldate(page.properties.modified)}</pubDate>
      <guid isPermaLink="false">${page.id}</guid>
   </item>
</#list>
</channel>
</rss>