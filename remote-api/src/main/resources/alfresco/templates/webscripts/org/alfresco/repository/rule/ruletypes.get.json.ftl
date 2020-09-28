{
	"data":
	[
<#list ruletypes as ruletype>
	   {
	      "name" : "${jsonUtils.encodeJSONString(ruletype.name)}",
	      "displayLabel" : "${jsonUtils.encodeJSONString(ruletype.displayLabel)}",
	      "url" : "${"/api/ruletypes/" + jsonUtils.encodeJSONString(ruletype.name)}"
	   }<#if (ruletype_has_next)>,</#if>
	</#list>
	]
}