<#assign username=userId>
<#if firstName?exists>
   <#assign username = firstName + " " + lastName>
</#if>
<entry xmlns='http://www.w3.org/2005/Atom'>
   <title>Discussion topic deleted: ${topicTitle!'unknown'}</title>
   <id>${id}</id>
   <updated>${xmldate(date)}</updated>
   <summary type="html">
      <![CDATA[${username} deleted topic ${topicTitle!'unknown'}.]]>
   </summary>
   <author>
   <name>${userId!""}</name>
   </author> 
</entry>

