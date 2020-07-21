<#include "../slingshot-common.lib.ftl">
<#assign title><#if memberFirstName??>${memberFirstName?xml}</#if><#if memberLastName??> ${memberLastName?xml}</#if> joined ${siteNetwork?xml} site</#assign>
<entry xmlns='http://www.w3.org/2005/Atom'>
   <title>${title}</title>
   <link rel="alternate" type="text/html"/>
   <icon></icon>
   <id>http://www.alfresco.org/rss/atom/${id}</id>
   <updated>${xmldate(date)}</updated>
   <summary type="html">
      <![CDATA[${title} (with role ${role}).]]>
   </summary>
   <author>
      <name>${userName?xml}</name>
      <uri>${userId?xml}</uri>
   </author>
</entry>