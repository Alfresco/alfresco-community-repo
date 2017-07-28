<#include "../slingshot-common.lib.ftl">
<#assign title>${followerUserName?xml} is now following ${userUserName?xml}</#assign>
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
      <name>${followerUserName?xml}</name>
   </author>
</entry>