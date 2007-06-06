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
   <#list status.exception.stackTrace as element>
     ${element}
   </#list>
  </#if>
  </callstack>
</response>