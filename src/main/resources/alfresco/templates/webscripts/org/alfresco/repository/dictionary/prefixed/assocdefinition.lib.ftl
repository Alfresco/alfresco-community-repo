<#macro assocDefJSON assocdefs>
<#escape x as jsonUtils.encodeJSONString(x)>
  {
   <#if assocdefs.name?exists>
    "name" : "${assocdefs.name.toPrefixString()}",
   </#if>
   <#if assocdefs.getTitle(messages)?has_content>
    "title" : "${assocdefs.getTitle(messages)}",
   <#else>
    "title" : "",
   </#if>
   <#if assocdefs.getDescription(messages)?has_content>
    "description" : "${assocdefs.getDescription(messages)}",
   <#else>
    "description" : "",
   </#if>
   <#if assocdefs.isChild() == true>
    "isChildAssociation" : true,
   <#else>   
    "isChildAssociation" : false,
   </#if>	
   <#if assocdefs.isProtected() == true>
    "protected" : true,
   <#else>
    "protected" : false,
   </#if>
    "source" : {
	<#if assocdefs.getSourceClass().name?exists>
	"class" : "${assocdefs.getSourceClass().name.toPrefixString()}",
	</#if>
	<#if assocdefs.getSourceRoleName()?exists>
	"role" : "${assocdefs.getSourceRoleName().toPrefixString()}",
	</#if>
	"mandatory" : ${assocdefs.isSourceMandatory()?string},
	"many" : ${assocdefs.isSourceMany()?string}
     },		
    "target" : {
    <#if assocdefs.getTargetClass().name?exists>
	"class" : "${assocdefs.getTargetClass().name.toPrefixString()}",
	</#if>
	<#if assocdefs.getTargetRoleName()?exists>
	"role" : "${assocdefs.getTargetRoleName().toPrefixString()}",
	</#if>
	"mandatory" : ${assocdefs.isTargetMandatory()?string},
	"many" : ${assocdefs.isTargetMany()?string}
     },
   <#if assocdefs.isChild() == true>
    <#if assocdefs.getRequiredChildName()?exists>
    "requiredChildName" : "${assocdefs.getRequiredChildName()}",
    </#if>
    <#if assocdefs.getDuplicateChildNamesAllowed() == true>
	"duplicateChildNameAllowed" : true,
	    <#else>
	"duplicateChildNameAllowed" : false,
	</#if>
   </#if>
    "url" : "${"/api/defclasses/" + url.templateArgs.prefix + "/" + url.templateArgs.shortClassName + "/association/" + assocdefs.name.toPrefixString()?replace(":","/")}"
  }
</#escape>
</#macro>