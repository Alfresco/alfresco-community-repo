<#include "../slingshot-common.lib.ftl">
<entry xmlns='http://www.w3.org/2005/Atom'>
   <title>Comment updated on ${(htmlTitle!'')?html?xml}</title>
   <link rel="alternate" type="text/html" href="" />
   <id>http://www.alfresco.org/rss/atom/${id}</id>
   <updated>${xmldate(date)}</updated>
   <summary type="html">
      <![CDATA[Comment on &quot;${htmlTitle}&quot; updated by ${userName?html}.]]>
   </summary>
   <author>
      <name>${userName?xml}</name>
      <uri>${userId?xml}</uri>
   </author>
</entry>