<#import "propertydefinition.lib.ftl" as propertyDefLib/>
<#import "assocdefinition.lib.ftl" as assocDefLib/>
<#macro classDefJSON classdefs>
   <#escape x as jsonUtils.encodeJSONString(x)>
      {
        <#if classdefs.name?exists>
            "name" : "${classdefs.name}",
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
			 [
				<#list propertydefs as propertydefs>
					<@propertyDefLib.propertyDefJSON propertydefs=propertydefs/>
					<#if propertydefs_has_next>,</#if>
				</#list>
			 ],		
			 "associations" :
			 [
				<#list assocdefs as assocdefs>
					<@assocDefLib.assocDefJSON assocdefs=assocdefs/>
					<#if assocdefs_has_next>,</#if>
				</#list>
			 ],
			 "childassociations" :
			 [
				<#list assocdefs as assocdefs>
					<#if assocdefs.isChild() == true>
					<@assocDefLib.assocDefJSON assocdefs=assocdefs/>
					<#if assocdefs_has_next>,</#if>
					</#if>
				</#list>
			 ],		
			 "url" : "${url.serviceContext + "/api/classes" + classdefs.name}"		
	  }
   </#escape>
</#macro>
