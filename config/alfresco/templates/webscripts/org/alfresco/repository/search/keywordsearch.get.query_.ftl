(
  TYPE:"{http://www.alfresco.org/model/content/1.0}content" AND 
  (
    (
<#list 1..terms?size as i>
      @\{http\://www.alfresco.org/model/content/1.0\}name:${terms[i - 1]} <#if (i < terms?size)> OR </#if>
</#list>
    ) 
    (
<#list 1..terms?size as i>
      TEXT:${terms[i - 1]} <#if (i < terms?size)> OR </#if>
</#list>
    )
  )
)