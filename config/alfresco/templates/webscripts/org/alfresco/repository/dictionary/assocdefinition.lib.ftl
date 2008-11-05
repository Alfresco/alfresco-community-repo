<#macro assocDefJSON assocdefs>
<#escape x as jsonUtils.encodeJSONString(x)>
  {
    <#if assocdefs.name?exists>
        "name" : "${assocdefs.name.toPrefixString()}",
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
		"class" : "${assocdefs.getSourceClass().name.toPrefixString()}",
		"isSourceMandatory" : ${assocdefs.isSourceMandatory()?string},
		"isSourceMany" : ${assocdefs.isSourceMany()?string}
	 	},		
	 	"target" :
     	{
		"class" : "${assocdefs.getTargetClass().name.toPrefixString()}",
		"isTargetMandatory" : ${assocdefs.isTargetMandatory()?string},
		"isTargetMany" : ${assocdefs.isTargetMany()?string}
	 	},
	  <#if assocdefs.isChild() == true>
       "url" : "${"/api/classes/" + url.templateArgs.classname + "/childassociation/" + assocdefs.name.toPrefixString()?replace(":","_")}"
       <#else>
       "url" : "${"/api/classes/" + url.templateArgs.classname + "/association/" + assocdefs.name.toPrefixString()?replace(":","_")}"
       </#if>
   }
</#escape>
</#macro>