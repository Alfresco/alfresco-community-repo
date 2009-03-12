<#import "webproject.lib.ftl" as webprojectLib/>

{
  "data":[
	<#list webprojects as webproject>
		<@webprojectLib.webprojectJSON webproject=webproject/>
		<#if webproject_has_next>,</#if>
	</#list>
  ]
}
