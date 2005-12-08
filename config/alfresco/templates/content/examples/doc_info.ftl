<#-- Shows some general info about the current document, including NodeRef and aspects applied -->
<h3>=====Template Start=====</h3>

<h4>Current Document Info:</h4>
<b>Name:</b> ${document.name}<br>
<b>Ref:</b> ${document.nodeRef}<br>
<b>Type:</b> ${document.type}<br>
<b>Content URL:</b> <a href="/alfresco${document.url}">/alfresco${document.url}</a><br>
<b>Locked:</b> <#if document.isLocked>Yes<#else>No</#if><br>
<b>Aspects:</b>
<table>
<#list document.aspects as aspect>
   <tr><td>${aspect}</td></tr>
</#list>
</table>

<h3>=====Template End=====</h3>
