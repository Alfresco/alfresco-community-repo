<style>
<!--
#app5607893292_appbody { padding-left: 7px; }
-->
</style>

<div id="appbody">

<table>
 <tr>
    <td><img src="${url.context}/images/logo/AlfrescoLogo32.png" alt="Alfresco" /></td>
    <td><nobr><span class="mainTitle">Web Script Status ${status.code} - ${status.codeName}</span></nobr></td>
 </tr>
</table>
<br>
<table>
 <tr><td><b>Error:</b><td>${status.codeName} (${status.code}) - ${status.codeDescription}
 <tr><td>&nbsp;
 <tr><td><b>Message:</b><td>${status.message!"<i>&lt;Not specified&gt;</i>"}
 <#if status.exception?exists>
   <tr><td>&nbsp;     
   <@recursestack status.exception/>
 </#if>
 <tr><td><b>Server</b>:<td>Alfresco ${server.edition} v${server.version} schema ${server.schema}
 <tr><td><b>Time</b>:<td>${date?datetime}
 <tr><td>&nbsp;
</table>

</div>


<#macro recursestack exception>
   <#if exception.cause?exists>
      <@recursestack exception=exception.cause/>
   </#if>
   <tr><td><b>Exception:</b><td>${exception.class.name}<#if exception.message?exists> - ${exception.message}</#if>
   <tr><td><td>&nbsp;
   <#if exception.cause?exists == false>
      <#list exception.stackTrace as element>
         <tr><td><td>${element}
      </#list>  
      <#else>
         <tr><td><td>${exception.stackTrace[0]}
      </#if>
   <tr><td><td>&nbsp;
</#macro>