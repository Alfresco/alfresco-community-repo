<#assign username=userId>
<#if firstName?exists>
   <#assign username = firstName + " " + lastName>
</#if>
<entry xmlns='http://www.w3.org/2005/Atom'>
   <title>New blog post: ${title!'unknown'}</title>
   <link rel="alternate" type="text/html" href="${(browsePostUrl!'')?replace("&", "&amp;")}" />
   <id>${id}</id>
   <updated>${xmldate(date)}</updated>
   <summary type="html">
      <![CDATA[${username} added blog post <a href="${(browsePostUrl!'')}">${title!'unknown'}</a>.]]>
   </summary>
   <author>
   <name>${userId!""}</name>
   </author> 
</entry>

