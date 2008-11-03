<#import "propertydefinition.lib.ftl" as propertyDefLib/>
<#import "assocdefinition.lib.ftl" as assocDefLib/>
<#macro classDefJSON classdefs>
   <#escape x as jsonUtils.encodeJSONString(x)>
    {
    <#if classdefs.name?exists>
        "name" : "${classdefs.name.toPrefixString()}",
     </#if>
    <#if classdefs.isAspect() == true>
        "isAspect" : true,
      <#else>
        "isAspect" : false,
	 </#if>
    <#if classdefs.title?exists>
        "title" : "${classdefs.title}",
     </#if>
     <#if classdefs.description?exists>
        "description" : "${classdefs.description}",
     </#if>
        "properties" :
		 {
		<#list propertydefs as propertydefs>
		"${propertydefs.name.toPrefixString()}":
		{
			<#if propertydefs.name?exists>
			"name" : "${propertydefs.name.toPrefixString()}",
			</#if>
		    <#if propertydefs.title?exists>
		    "title" : "${propertydefs.title}",
		     </#if>
		     "url" : "${url.serviceContext + "/api/classes/" + classdefs.name.toPrefixString()?replace(":","_") + "/property/" + propertydefs.name.toPrefixString()?replace(":","_")}"
		 }<#if propertydefs_has_next>,</#if>
		</#list>
		 },		
		 "associations" :
		 {
		<#list assocdefs as assocdefs>
			<#if assocdefs.isChild() == false>
			  "${assocdefs.name.toPrefixString()}":
				{
				<#if assocdefs.name?exists>
    			"name" : "${assocdefs.name.toPrefixString()}",
				</#if>
			    <#if assocdefs.title?exists>
			    "title" : "${assocdefs.title}",
			     </#if>
			     "url" : "${url.serviceContext + "/api/classes/"  + classdefs.name.toPrefixString()?replace(":","_") + "/association/" + assocdefs.name.toPrefixString()?replace(":","_")}"
				}
			</#if>
		 <#if assocdefs_has_next>,</#if>
		</#list>
		 },		
		 "childassociations" :
		 {
		<#list assocdefs as assocdefs>
			<#if assocdefs.isChild() == true>
			  "${assocdefs.name.toPrefixString()}":
				{
				<#if assocdefs.name?exists>
    			"name" : "${assocdefs.name.toPrefixString()}",
				</#if>
			    <#if assocdefs.title?exists>
			    "title" : "${assocdefs.title}",
			     </#if>
			     "url" : "${url.serviceContext + "/api/classes/"  + classdefs.name.toPrefixString()?replace(":","_") + "/childassociation/" + assocdefs.name.toPrefixString()?replace(":","_")}"
				}
			</#if>
		 <#if assocdefs_has_next>,</#if>
		</#list>
		 },		
		 "url" : "${url.serviceContext + "/api/classes/" + classdefs.name.toPrefixString()?replace(":","_")}"		
  }
   </#escape>
</#macro>
