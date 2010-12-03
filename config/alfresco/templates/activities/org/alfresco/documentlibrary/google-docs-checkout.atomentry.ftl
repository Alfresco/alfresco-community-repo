<#include "../slingshot-common.lib.ftl">
<entry xmlns='http://www.w3.org/2005/Atom'>
   <title>File checked out to Google Docs: ${htmlTitle?xml}</title>
   <link rel="alternate" type="text/html" href="" />
   <id>http://www.alfresco.org/rss/atom/${id}</id>
   <updated>${xmldate(date)}</updated>
   <summary type="html">
      <![CDATA[&quot;${htmlTitle}&quot; document checked out to Google Docs by ${userName?html}.]]>
   </summary>
   <author>
      <name>${userName?xml}</name>
      <uri>${userId?xml}</uri>
   </author>
</entry>