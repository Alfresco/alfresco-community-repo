<#macro classDefJSON classdefs>
   <#escape x as jsonUtils.encodeJSONString(x)>
      {
        <#if classdefs.name?exists>
            "name" : "${classdefs.name}",
         </#if>
        <#if classdefs.title?exists>
            "title" : "${classdefs.title}",
         </#if>
         <#if classdefs.description?exists>
            "description" : "${classdefs.description}",
         </#if>
         <#if classdefs.isAspect?exists>
	        "isAspect" : "it does exist"
	     </#if>
       }
   </#escape>
</#macro>
