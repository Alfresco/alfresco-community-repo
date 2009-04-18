<#macro formDefJSON form>
    <#escape x as jsonUtils.encodeJSONString(x)>
{
    "data" :
    {
        "item" : "${form.data.item}",
        "submissionUrl" : "${form.data.submissionUrl}",
        "type" : "${form.data.type}",
        "definition" :
        {
            "fields" :
            [
                <#list form.data.definition.fields as field>
                {
                    <#list field?keys as key>
                    <#if field[key]?is_boolean>
                    "${key}" : ${field[key]?string}<#if key_has_next>,</#if>
                    <#elseif field[key]?is_sequence>
                    "${key}" :
                    [{
                    <#list field[key] as q>
                        "type" : "${q.type}"<#if q.params?exists>,
                        "params" : {
                        <#list q.params?keys as p>
                            <#-- Render booleans without the inverted commas -->

                            <#-- Can I create a macro for boolean rendering? -->

                            <#if q.params[p]?is_boolean>
                            "${p}" : ${q.params[p]}<#if p_has_next>,</#if>
                            <#else>
                            "${p}" : "${q.params[p]}"<#if p_has_next>,</#if>
                            </#if>
                        </#list>
                        }
                        </#if>
                    </#list>
                    }]<#if key_has_next>,</#if>
                    <#else>
                    "${key}" : "${field[key]}"<#if key_has_next>,</#if>
                    </#if>
                    </#list>
                }<#if field_has_next>,</#if>
                </#list>
            ]
        },
        "formData" :
        {
            <#list form.data.formData?keys as k>
            <#if form.data.formData[k]?is_boolean>
                    <#-- Render boolean data without the surrounding inverted commas -->
            "${k}" : ${form.data.formData[k]?string}<#if k_has_next>,</#if>
            <#elseif form.data.formData[k]?is_number>
                    <#-- Render number data without the surrounding inverted commas and no formatting -->
            "${k}" : ${form.data.formData[k]?c}<#if k_has_next>,</#if>
            <#else>
                    <#-- All other data rendered with inverted commas -->
            "${k}" : "${form.data.formData[k]}"<#if k_has_next>,</#if>
            </#if>
            </#list>
        }
    }
}
	</#escape>
</#macro>