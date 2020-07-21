{
	"data":
	[
<#list actiondefinitions as actiondefinition>
	   {
	      "name" : "${jsonUtils.encodeJSONString(actiondefinition.name)}",
          "displayLabel" : <#if actiondefinition.title??>"${jsonUtils.encodeJSONString(actiondefinition.title)}"<#else>null</#if>,
          "description" : <#if actiondefinition.description??>"${jsonUtils.encodeJSONString(actiondefinition.description)}"<#else>null</#if>,
          "adHocPropertiesAllowed" : ${actiondefinition.adhocPropertiesAllowed?string},          
          "parameterDefinitions" :
          [
          <#if actiondefinition.parameterDefinitions??>
          <#list actiondefinition.parameterDefinitions as parameterDefinition>
	          {
	             "name" : "${jsonUtils.encodeJSONString(parameterDefinition.name)}",
	         	 "displayLabel" : <#if parameterDefinition.displayLabel??>"${jsonUtils.encodeJSONString(parameterDefinition.displayLabel)}"<#else>null</#if>,
	        	 "type" : "${shortQName(parameterDefinition.type)}",
	        	 <#if parameterDefinition.parameterConstraintName??>
	        	 "constraint" : "${jsonUtils.encodeJSONString(parameterDefinition.parameterConstraintName)}",
	        	 </#if>
	        	 "isMultiValued" : ${parameterDefinition.multiValued?string},
	        	 "isMandatory" : ${parameterDefinition.mandatory?string}
	          }<#if (parameterDefinition_has_next)>,</#if>
		  </#list>
		  </#if>
          ],
          "applicableTypes" : 
          [
          <#if actiondefinition.applicableTypes??>
          <#list actiondefinition.applicableTypes as applicableType>          
              "${shortQName(applicableType)}"         	 
          <#if (applicableType_has_next)>,</#if>
		  </#list>
		  </#if>
          ]        
	   }<#if (actiondefinition_has_next)>,</#if>
	</#list>
	]
}