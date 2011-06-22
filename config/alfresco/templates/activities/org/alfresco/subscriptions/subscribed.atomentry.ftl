<#include "../slingshot-common.lib.ftl">
<#assign title>${subscriberUserName?xml} has subscribed to ${node?xml}</#assign>
<entry xmlns='http://www.w3.org/2005/Atom'>
   <title>${title}</title>
   <link rel="alternate" type="text/html"/>
   <icon></icon>
   <id>http://www.alfresco.org/rss/atom/${id}</id>
   <updated>${xmldate(date)}</updated>
   <summary type="html">
      <![CDATA[${title}.]]>
   </summary>
   <author>
      <name>${subscriberUserName?xml}</name>
   </author>
</entry>