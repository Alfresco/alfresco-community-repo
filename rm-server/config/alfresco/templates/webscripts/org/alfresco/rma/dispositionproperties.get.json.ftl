{
   "data" : 
   {
      "properties":
	  [
	     <#list properties as item>
		 {
		    "label": "${item.label}",
		    "value": "${item.value}"
		  }<#if item_has_next>,</#if>
		  </#list>
	  ]
   }
}