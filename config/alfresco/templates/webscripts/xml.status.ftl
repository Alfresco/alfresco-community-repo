<?xml version="1.0" encoding="UTF-8"?>
<response>
  <status>
    <code>${status.code}</code>
    <name>${status.codeName}</name>
    <description>${status.codeDescription}</description>
  </status>
  <message>${status.message!""}</message>
  <exception><#if status.exception?exists>${status.exception.class.name}<#if status.exception.message?exists> - ${status.exception.message}</#if></#if></exception>
  <callstack>
  <#if status.exception?exists>
   <@recursestack status.exception/>
  </#if>
  </callstack>
</response>

<#macro recursestack exception>
   <#if exception.cause?exists>
      <@recursestack exception=exception.cause/>
   </#if>
   <#if exception.cause?exists == false>
      ${exception}
      <#list exception.stackTrace as element>
         ${element}
      </#list>  
   <#else>
      ${exception}
      ${exception.stackTrace[0]}
   </#if>
</#macro>
