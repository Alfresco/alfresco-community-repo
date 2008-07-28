<#assign username=userId>
<#if firstName?exists>
   <#assign username = firstName + " " + lastName>
</#if>
<entry xmlns='http://www.w3.org/2005/Atom'>
   <title>Blog post deleted: ${(postTitle!'unknown')?html?xml}</title>
   <link rel="alternate" type="text/html" href="${(browsePostListUrl!'')?xml}" />
   <id>${id}</id>
   <updated>${xmldate(date)}</updated>
   <summary type="html">
      <![CDATA[${username} deleted blog post ${(postTitle!'unknown')?html}.]]>
   </summary>
   <author>
   <name>${userId!""}</name>
   </author> 
</entry>

