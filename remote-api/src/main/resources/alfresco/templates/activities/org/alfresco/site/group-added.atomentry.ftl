<#include "../slingshot-common.lib.ftl">
<entry xmlns='http://www.w3.org/2005/Atom'>
   <title>${groupName?xml} added ${siteNetwork?xml} site</title>
   <link rel="alternate" type="text/html"/>
   <icon></icon>
   <id>http://www.alfresco.org/rss/atom/${id}</id>
   <updated>${xmldate(date)}</updated>
   <summary type="html">
      <![CDATA[${groupName?xml} added to ${siteNetwork?xml} site with role ${role?xml}]]>
   </summary>
   <author>
      <name>${userName?xml}</name>
      <uri>${userId?xml}</uri>
   </author>
</entry>