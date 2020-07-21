(
  TYPE:"{http://www.alfresco.org/model/content/1.0}person" AND
  (
<#list 1..terms?size as i>
      d\:content:"${terms[i - 1]}"
</#list>
<#list 1..terms?size as i>
      d\:text:"${terms[i - 1]}*"
</#list>
  )
)