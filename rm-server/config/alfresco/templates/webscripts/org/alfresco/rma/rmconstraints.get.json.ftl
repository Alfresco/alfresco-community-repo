<#escape x as jsonUtils.encodeJSONString(x)>
{
	"data": {
	   "constraintName": "${constraintName}",
		"allowedValuesForCurrentUser" : [  
	            <#list allowedValuesForCurrentUser as item>
               {
                  "label": "${item}",
                  "value": "${item}"
               }<#if item_has_next>,</#if>
               </#list> 
            ]
   }     
}
</#escape>