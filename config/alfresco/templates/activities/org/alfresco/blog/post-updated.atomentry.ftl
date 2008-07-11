<#assign username=userId>
<#if firstName?exists>
   <#assign username = firstName + " " + lastName>
</#if>
<entry xmlns='http://www.w3.org/2005/Atom'>
   <title>Blog post updated: ${title!'unknown'}</title>
   <link rel="alternate" type="text/html" href="${(browsePostUrl!'')?replace("&", "&amp;")}" />
   <id>${id}</id>
   <updated>${xmldate(date)}</updated>
   <summary type="html">
      <![CDATA[${username} updated blog post <a href="${(browsePostUrl!'')}">${title}</a>.]]>
   </summary>
   <author>
   <name>${userId!""}</name>
   </author> 
</entry>

