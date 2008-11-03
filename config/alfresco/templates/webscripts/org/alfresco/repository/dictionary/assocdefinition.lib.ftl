<#macro assocDefJSON assocdefs>
<#escape x as jsonUtils.encodeJSONString(x)>
  {
    <#if assocdefs.name?exists>
        "name" : "${assocdefs.name}",
     </#if>
    <#if assocdefs.title?exists>
        "title" : "${assocdefs.title}",
     </#if>
     <#if assocdefs.description?exists>
        "description" : "${assocdefs.description}",
     </#if>
     <#if assocdefs.isChild() == true>
        "isChild" : true,
     <#else>   
     	"isChild" : false,
     </#if>	
     <#if assocdefs.isProtected() == true>
     	"protected" : true,
     <#else>
     	"protected" : false,
     </#if>
     	"source" :
     	{
		"class" : "${assocdefs.getSourceClass().name}",
		"isSourceMandatory" : ${assocdefs.isSourceMandatory()?string},
		"isSourceMany" : ${assocdefs.isSourceMany()?string}
	 	},		
	 	"target" :
     	{
		"class" : "${assocdefs.getTargetClass()}",
		"isTargetMandatory" : ${assocdefs.isTargetMandatory()?string},
		"isTargetMany" : ${assocdefs.isTargetMany()?string}
	 	},		
	  "url" : "${url.serviceContext + "/api/classes" + assocdefs.name}"
   }
</#escape>
</#macro>