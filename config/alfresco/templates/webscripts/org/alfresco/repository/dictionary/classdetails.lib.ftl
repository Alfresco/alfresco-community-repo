<#macro classDefJSON classdefs key>
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
     <#else>
       "title" : "",
     </#if>
     <#if classdefs.description?exists>
       "description" : "${classdefs.description}",
     <#else>
       "description" : "",
     </#if>
      "parent" : {
        <#if classdefs.parentName?exists>
          "name" : "${classdefs.parentName.toPrefixString()}",
          "title" : "${classdefs.parentName.getLocalName()}",
          "url" : "${"/api/classes/" + classdefs.parentName.toPrefixString()?replace(":","_")}"
        </#if>
      },
     "defaultValues" : {
      <#if classdefs.defaultValues?exists>
     <#assign keys = classdefs.defaultValues?keys>
	<#list keys as key>
	"${key.toPrefixString()}" : {
			"name" : "${key.toPrefixString()}",
			"url" : "${"/api/classes/" + classdefs.name.toPrefixString()?replace(":","_") + "/property/" + key.toPrefixString()?replace(":","_")}"
	 }<#if key_has_next>,</#if>
	</#list>
      </#if>
     }, 
     "defaultAspects" : {
       <#if classdefs.defaultAspects?exists>
         <#list classdefs.defaultAspects as aspectdef>
          "${aspectdef.name.toPrefixString()}" : {
            "name" : "${aspectdef.name.toPrefixString()}",
            <#if aspectdef.title?exists>
            "title" : "${aspectdef.title}",
            </#if>
            "url" : "${"/api/classes/" + classdefs.name.toPrefixString()?replace(":","_") + "/property/" + aspectdef.name.toPrefixString()?replace(":","_")}"
           }<#if aspectdef_has_next>,</#if>
         </#list>
       </#if>
      },
      "properties" : {
       <#list propertydefs[key] as propertydefs>
	  "${propertydefs.name.toPrefixString()}": {
	    <#if propertydefs.name?exists>
	      "name" : "${propertydefs.name.toPrefixString()}",
	     </#if>
	     <#if propertydefs.title?exists>
	      "title" : "${propertydefs.title}",
	      </#if>
	      "url" : "${"/api/classes/" + classdefs.name.toPrefixString()?replace(":","_") + "/property/" + propertydefs.name.toPrefixString()?replace(":","_")}"
	    }<#if propertydefs_has_next>,</#if>
	</#list>
       },		
      "associations" : {
      <#assign flag = false>
      <#list assocdefs[key] as assocdefs>
       <#if (assocdefs.isChild()==false)&&(flag== true)><#assign flag = false>,</#if>
         <#if assocdefs.isChild() == false>
           <#assign flag=true>
	   "${assocdefs.name.toPrefixString()}": {
	  <#if assocdefs.name?exists>
           "name" : "${assocdefs.name.toPrefixString()}",
	  </#if>
	  <#if assocdefs.title?exists>
	   "title" : "${assocdefs.title}",
	  </#if>
	   "url" : "${"/api/classes/" + classdefs.name.toPrefixString()?replace(":","_") + "/association/" + assocdefs.name.toPrefixString()?replace(":","_")}"
	}
        </#if>
       </#list>
       },		
       "childassociations" : {
        <#assign flag = false>
	<#list assocdefs[key] as assocdefs>
	  <#if (assocdefs.isChild()==true)&&(flag== true)><#assign flag = false>,</#if>
	   <#if assocdefs.isChild() == true>
	    <#assign flag=true>
             "${assocdefs.name.toPrefixString()}": {
	    <#if assocdefs.name?exists>
    	     "name" : "${assocdefs.name.toPrefixString()}",
	    </#if>
            <#if assocdefs.title?exists>
	     "title" : "${assocdefs.title}",
	    </#if>
	     "url" : "${"/api/classes/" + classdefs.name.toPrefixString()?replace(":","_") + "/association/" + assocdefs.name.toPrefixString()?replace(":","_")}"
	   }
	  </#if>
	</#list>
	},		
      "url" : "${"/api/classes/" + classdefs.name.toPrefixString()?replace(":","_")}"		
  }
   </#escape>
</#macro>
