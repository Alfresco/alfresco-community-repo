<?xml version="1.0" encoding="UTF-8"?>
<rss version="2.0" xmlns:atom="http://www.w3.org/2005/Atom" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
<channel rdf:about="http://www.alfresco.com/">
   <title>Alfresco - Wiki Page</title>
   <description>Alfresco Wiki Page - Recent Changes</description>
   <lastBuildDate>${date?string("EEE, dd MMM yyyy HH:mm:ss zzz")}</lastBuildDate>
   <pubDate>${date?string("EEE, dd MMM yyyy HH:mm:ss zzz")}</pubDate>
   <generator>Alfresco ${server.edition} v${server.version}</generator>
   <image>
      <title>Alfresco - Wiki Page Recent Changes</title>
      <url>${absurl(url.context)?replace("alfresco", "share/proxy/alfresco")}/images/logo/AlfrescoLogo200.png</url>
   </image>
   <#list wiki.pages?sort_by(['modified'])?reverse as p>
   <#assign page = p.page>
   <item>
      <title>${(page.properties.title!"")?html}</title>
      <link>${absurl(url.context)?replace("alfresco", "share/page/site/${siteId}/wiki-page?title=${page.name}")}</link>
      <pubDate>${page.properties.modified?string("EEE, dd MMM yyyy HH:mm:ss zzz")}</pubDate>
      <guid isPermaLink="false">${page.id}</guid>
   </item>
</#list>
</channel>
</rss>