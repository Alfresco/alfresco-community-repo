<#include "../slingshot-common.lib.ftl">
<entry xmlns='http://www.w3.org/2005/Atom'>
   <title>${groupName?xml} role changed for ${siteNetwork?xml} site</title>
   <link rel="alternate" type="text/html"/>
   <icon></icon>
   <id>http://www.alfresco.org/rss/atom/${id}</id>
   <updated>${xmldate(date)}</updated>
   <summary type="html">
      <![CDATA[Group ${groupName?xml} role changed to ${role?xml} in site ${siteNetwork?xml}.]]>
   </summary>
   <author>
      <name>${userName?xml}</name>
      <uri>${userId?xml}</uri>
   </author>
</entry>