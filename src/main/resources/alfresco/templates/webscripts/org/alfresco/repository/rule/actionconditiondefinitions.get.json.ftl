{
	"data":
	[
<#list actionconditiondefinitions as actionconditiondefinition>
	   {
	      "name" : "${jsonUtils.encodeJSONString(actionconditiondefinition.name)}",
          "displayLabel" : <#if actionconditiondefinition.title??>"${jsonUtils.encodeJSONString(actionconditiondefinition.title)}"<#else>null</#if>,
          "description" : <#if actionconditiondefinition.description??>"${jsonUtils.encodeJSONString(actionconditiondefinition.description)}"<#else>null</#if>,
          "adHocPropertiesAllowed" : ${actionconditiondefinition.adhocPropertiesAllowed?string},
          "parameterDefinitions" :
          [
          <#if actionconditiondefinition.parameterDefinitions??>
          <#list actionconditiondefinition.parameterDefinitions as parameterDefinition>
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
          ]        
	   }<#if (actionconditiondefinition_has_next)>,</#if>
	</#list>
	]
}