<#import "sandbox.lib.ftl" as sandboxLib/>
{
  "data":[
  <#if sandboxes??>
	<#assign boxNames = sandboxes?keys />
    <#list boxNames as boxName>		 	   
	   <@sandboxLib.sandboxJSON webproject=webproject sandbox=sandboxes[boxName]/>
	   <#if boxName_has_next>,</#if>
    </#list>
  </#if>
  ]
}

