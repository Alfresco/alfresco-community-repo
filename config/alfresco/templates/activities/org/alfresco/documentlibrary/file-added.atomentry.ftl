<#assign username=userId>
<#if firstName?exists>
   <#assign username = firstName + " " + lastName>
</#if>
<entry xmlns='http://www.w3.org/2005/Atom'>
   <title>${(fileName!"File uploaded")?xml}</title>
   <link rel="alternate" type="text/html" href="${browseURL?xml}" />
   <id>${id}</id>
   <updated>${xmldate(date)}</updated>
   <summary type="html">
      <![CDATA[${username} added <a href="${contentURL}" target="_blank">${fileName}</a> to the <a href="${browseURL}">Document Library</a>.]]>
   </summary>
   <author>
   <name>${userId!""}</name>
   </author> 
</entry>

