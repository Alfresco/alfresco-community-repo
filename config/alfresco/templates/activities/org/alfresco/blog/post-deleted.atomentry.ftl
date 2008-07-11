<#assign username=userId>
<#if firstName?exists>
   <#assign username = firstName + " " + lastName>
</#if>
<entry xmlns='http://www.w3.org/2005/Atom'>
   <title>Blog post deleted: ${title!'unknown'}</title>
   <link rel="alternate" type="text/html" href="${(browsePostListUrl!'')?replace("&", "&amp;")}" />
   <id>${id}</id>
   <updated>${xmldate(date)}</updated>
   <summary type="html">
      <![CDATA[${username} deleted blog post ${title}.]]>
   </summary>
   <author>
   <name>${userId!""}</name>
   </author> 
</entry>

