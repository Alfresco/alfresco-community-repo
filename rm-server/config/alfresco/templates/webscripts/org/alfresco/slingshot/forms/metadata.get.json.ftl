{
	"kind" : "${kind}"
	<#if extended>
	,
	"aspects": 
	[
   	   <#list aspects as aspect>
          {
      	     "name": "${aspect.name}",
      	     "prefixedName": "${aspect.prefixedName}"
   	      }
          <#if aspect_has_next>,</#if>
       </#list>
    ]
    </#if>
}