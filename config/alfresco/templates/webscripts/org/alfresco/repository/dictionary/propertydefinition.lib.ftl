<#macro propertyDefJSON propertydefs>
   <#escape x as jsonUtils.encodeJSONString(x)>
  {
    <#if propertydefs.name?exists>
        "name" : "${propertydefs.name}",
     </#if>
    <#if propertydefs.title?exists>
        "title" : "${propertydefs.title}",
     </#if>
     <#if propertydefs.description?exists>
        "description" : "${propertydefs.description}",
     </#if>
     <#if propertydefs.defaultValues?exists>
     	"defaultValues" : "${propertydefs.defaultValues}",
     <#else>
     	"defaultValues" : "",
     </#if>
     <#if propertydefs.dataType?exists>
        "dataType" : "${propertydefs.dataType.title}",
     </#if>
     	"multiValued" : "${propertydefs.multiValued?string}",
        "mandatory" : "${propertydefs.mandatory?string}",
        "enforced" : "${propertydefs.mandatoryEnforced?string}",
      	"protected" : "${propertydefs.protected?string}",
       	"indexed" : "${propertydefs.indexed?string}",
       	"indexedAtomically" : "${propertydefs.indexedAtomically?string}",
       	"constraints" :
       	[
       		<#if propertydefs.constraints?exists>
       		<#list propertydefs.constraints as constraintdefs>
				"name" : "${constraintdefs.name}"
			</#list>
			</#if>
       	],
       	"url" : "${url.serviceContext + "/api/classes" + propertydefs.name}"
   }
   </#escape>
</#macro>
