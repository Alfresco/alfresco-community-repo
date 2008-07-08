<#assign username=userId>
<#if firstName?exists>
   <#assign username = firstName + " " + lastName>
</#if>
<entry xmlns='http://www.w3.org/2005/Atom'>
   <title>${fileName!"File uploaded"}</title>
   <link rel="alternate" type="text/html" href="${browseURL?replace("&", "&amp;")}" />
   <id>${id}</id>
   <updated>${xmldate(date)}</updated>
   <summary type="html">
      <![CDATA[${username} added <a href="${contentURL}">${fileName}</a> to the <a href="${browseURL}">Document Library</a>.]]>
   </summary>
   <author>
   <name>${userId!""}</name>
   </author> 
</entry>

