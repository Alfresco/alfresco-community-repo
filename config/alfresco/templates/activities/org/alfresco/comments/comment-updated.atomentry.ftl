<#assign username=userId>
<#if firstName?exists>
   <#assign username = firstName + " " + lastName>
</#if>
<entry xmlns='http://www.w3.org/2005/Atom'>
   <title>${username?html?xml} updated comment</title>
   <link rel="alternate" type="text/html" href="${(browsePostUrl!'')?xml}" />
   <id>${id}</id>
   <updated>${xmldate(date)}</updated>
   <summary type="html">
      <![CDATA[${username?html} updated comment on <a href="${(browseItemUrl!'')}">${(itemTitle!'unknown')?html}</a>]]>
   </summary>
   <author>
   <name>${userId!""}</name>
   </author> 
</entry>

