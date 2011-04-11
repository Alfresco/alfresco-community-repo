<#-- get the path location from the passed in args -->
<#if args.p?exists><#assign path=args.p><#else><#assign path=""></#if>
<#-- see if lucene query specified - this overrides any path argument -->
<#if !args.q?exists || args.q?length=0>
   <#assign query="">
   <#-- resolve the path (from Company Home) into a node or fall back to userhome-->
   <#if path?starts_with("/Company Home")>
      <#if path?length=13>
         <#assign home=companyhome>
      <#elseif companyhome.childByNamePath[args.p[14..]]?exists>
         <#assign home=companyhome.childByNamePath[args.p[14..]]>
      <#else>
         <#assign home=userhome>
      </#if>
   <#else>
      <#assign home=userhome>
   </#if>
<#else>
   <#assign query=args.q>
</#if>
<?xml version="1.0" encoding="UTF-8"?>
<rss version="2.0" xmlns:atom="http://www.w3.org/2005/Atom" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
<channel rdf:about="http://www.alfresco.com/">
   <title>Alfresco - My Documents</title>
   <link>${absurl(url.context)}/</link>
   <description>Alfresco - My Documents</description>
   <lastBuildDate>${date?string("EEE, dd MMM yyyy HH:mm:ss zzz")}</lastBuildDate>
   <pubDate>${date?string("EEE, dd MMM yyyy HH:mm:ss zzz")}</pubDate>
   <generator>Alfresco ${server.edition} v${server.version}</generator>
   <image>
      <title>Alfresco - My Documents</title>
      <url>${absurl(url.context)}/images/logo/AlfrescoLogo200.png</url>
   </image>
<#assign weekms=1000*60*60*24*7>
<#assign count=0>
<#-- get the filter mode from the passed in args -->
<#-- filters: 0=all, 1=word, 2=html, 3=pdf, 4=recent -->
<#if args.f?exists && args.f?length!=0><#assign filter=args.f?number><#else><#assign filter=0></#if>
<#if home?exists>
   <#assign docs=home.children?reverse>
<#else>
   <#assign docs=companyhome.childrenByLuceneSearch[query]?sort_by('name')>
</#if>
<#list docs as d>
   <#if d.isDocument>
      <#assign isImage=(d.mimetype="image/gif" || d.mimetype="image/jpeg" || d.mimetype="image/png")>
      <#assign isMP3=(d.mimetype="audio/x-mpeg" || d.mimetype="audio/mpeg")>
      <#if (filter=0) ||
           (filter=1 && d.mimetype="application/msword" || d.mimetype="application/vnd.openxmlformats-officedocument.wordprocessingml.document") ||
           (filter=2 && d.mimetype="text/html") ||
           (filter=3 && d.mimetype="application/pdf") ||
           (filter=4 && (dateCompare(d.properties["cm:modified"],date,weekms) == 1 || dateCompare(d.properties["cm:created"], date, weekms) == 1))>
         <#assign count=count+1>
   <item>
      <title>${d.name?html}</title>
         <#assign navurl='/navigate/showDocDetails/' + d.nodeRef.storeRef.protocol + '/' + d.nodeRef.storeRef.identifier + '/' + d.nodeRef.id>
      <link>${absurl(url.context)}${navurl}?ticket=${session.ticket}</link>
         <#if isMP3>
      <enclosure url="${absurl(url.context)}${d.url}ticket=${session.ticket}}" length="${d.size?string?replace(",","")}" type="audio/mpeg"/>
         </#if>
      <description>
         <#if isImage>&lt;img src=&quot;${absurl(url.context)}${d.url}?ticket=${session.ticket}&quot;&gt;&lt;br&gt;</#if>
         <#if d.properties.description?exists>${d.properties.description?html}</#if>
      </description>
      <pubDate>${d.properties.modified?string("EEE, dd MMM yyyy HH:mm:ss zzz")}</pubDate>
      <guid isPermaLink="false">${d.id}</guid>
   </item>
      </#if>
   </#if>
</#list>
</channel>
</rss>