<#assign username=userId>
<#if firstName?exists>
   <#assign username = firstName + " " + lastName>
</#if>
<entry xmlns='http://www.w3.org/2005/Atom'>
   <title>Reply added: ${topicTitle!'unknown'}</title>
   <link rel="alternate" type="text/html" href="${(browseTopicUrl!'')?replace("&", "&amp;")}" />
   <id>${id}</id>
   <updated>${xmldate(date)}</updated>
   <summary type="html">
      <![CDATA[${username} added a reply to topic <a href="${(browseTopicUrl!'')}">${topicTitle!'unknown'}</a>.]]>
   </summary>
   <author>
   <name>${userId!""}</name>
   </author> 
</entry>

