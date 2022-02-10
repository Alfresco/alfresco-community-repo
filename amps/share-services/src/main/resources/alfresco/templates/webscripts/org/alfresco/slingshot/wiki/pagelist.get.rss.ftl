<?xml version="1.0" encoding="UTF-8"?>
<rss version="2.0" xmlns:atom="http://www.w3.org/2005/Atom" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
<channel>
   <title>Alfresco - Wiki Page</title>
   <link>${shareUrl}/proxy/alfresco-feed/slingshot/wiki/pages/${siteId}?format=rss</link>
   <description>Alfresco Wiki Page - Recent Changes</description>
   <lastBuildDate><#assign locale_original=.locale><#setting locale="en_US">${date?string("EEE, dd MMM yyyy HH:mm:ss Z")}<#setting locale=locale_original></lastBuildDate>
   <pubDate><#assign locale_original=.locale><#setting locale="en_US">${date?string("EEE, dd MMM yyyy HH:mm:ss Z")}<#setting locale=locale_original></pubDate>
   <generator>Alfresco ${server.edition} v${server.version}</generator>
   <image>
      <title>Alfresco - Wiki Page Recent Changes</title>
      <link>${shareUrl}/proxy/alfresco-feed/slingshot/wiki/pages/${siteId}?format=rss</link>
      <url>${shareUrl}/proxy/alfresco/images/logo/AlfrescoLogo200.png</url>
   </image>
   <#list wiki.pages?sort_by(['modified'])?reverse as p>
   <#assign node = p.node>
   <#assign page = p.page>
   <item>
      <title>${(page.title!"")?html}</title>
      <link>${shareUrl}/page/site/${siteId}/wiki-page?title=${page.systemName?url('UTF-8')}</link>
      <pubDate><#assign locale_original=.locale><#setting locale="en_US">${page.modifiedAt?string("EEE, dd MMM yyyy HH:mm:ss Z")}<#setting locale=locale_original></pubDate>
      <guid isPermaLink="false">${node.id}</guid>
   </item>
</#list>
</channel>
</rss>
