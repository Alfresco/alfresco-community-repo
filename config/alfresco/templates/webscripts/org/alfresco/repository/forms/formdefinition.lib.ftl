<#-- This function is used below to detect numerical property values of Infinity, -Infinity and NaN -->
<#function isNonFiniteNumber value>
    <#if value?is_number>
        <#if value?c == '\xfffd'><#return true>
        <#elseif value?c == '\x221e'><#return true>
        <#elseif value?c == '-\x221e'><#return true>
        <#else><#return false>
        </#if>
    <#else><#return false>
    </#if>
</#function>

<#macro formDefJSON form>
<#escape x as jsonUtils.encodeJSONString(x)>
{
    "data":
    {
        "item": "${form.item}",
        "submissionUrl": "${form.submissionUrl}",
        "type": "${form.type}",
        "definition":
        {
            "fields":
            [
                <#list form.fields as field>
                {
                    "name": "${field.name}",
                    "label": "${field.label!""}",
                    <#if field.description??>"description": "${field.description}",</#if>
                    "protectedField": ${field.protectedField?string},
                    <#if field.defaultValue??>"defaultValue": "${field.defaultValue}",</#if>
                    <#if field.group??>"group": "${field.group.id}",</#if>
                    <#if field.binding??>"binding": "${field.binding}",</#if>
                    "dataKeyName": "${field.dataKeyName}",
                    <#if field.dataType??>
                    "type": "property",
                    "dataType": "${field.dataType}",
                    <#if field.dataTypeParameters??>"dataTypeParameters": 
                    ${field.dataTypeParameters.asJSON},</#if>
                    <#if field.constraints??>"constraints": 
                    [
                    <#list field.constraints as cnstrnt>
                       {
                          "type": "${cnstrnt.type}"<#if cnstrnt.parametersAsJSON??>,
                          "parameters": 
                          ${cnstrnt.parametersAsJSON}
                          </#if>
                       }<#if cnstrnt_has_next>,</#if>
                    </#list>
                    ],</#if>
                    "mandatory": ${field.mandatory?string},
                    "repeating": ${field.repeating?string}
                    <#else>
                    "type": "association",
                    "endpointType": "${field.endpointType}",
                    "endpointDirection": "${field.endpointDirection}",
                    "endpointMandatory": ${field.endpointMandatory?string},
                    "endpointMany": ${field.endpointMany?string}
                    </#if>
                }<#if field_has_next>,</#if>
                </#list>
            ]
        },
        "formData":
        {
            <#list form.formData?keys as k>
            <#if form.formData[k]?is_boolean>
            <#-- Render boolean data without the surrounding inverted commas -->
            "${k}": ${form.formData[k]?string}<#if k_has_next>,</#if>
            <#-- For now, do not render non-finite numbers (Infinity, NaN) - see ALF-16606 -->
            <#elseif isNonFiniteNumber(form.formData[k]) == true>
            <#-- Render (finite) number data without the surrounding inverted commas and no formatting -->
            <#elseif form.formData[k]?is_number>
            "${k}": ${form.formData[k]?c}<#if k_has_next>,</#if>
            <#else>
            <#-- All other data rendered with inverted commas -->
            "${k}": "${form.formData[k]}"<#if k_has_next>,</#if>
            </#if>
            </#list>
        }
    }
}
</#escape>
</#macro>