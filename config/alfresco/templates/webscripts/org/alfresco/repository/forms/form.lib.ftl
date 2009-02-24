<#macro formJSON form>
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
                <#list form.data.definition.fields?keys as k>
                {
                    <#list form.data.definition.fields[k]?keys as c>
                    <#if form.data.definition.fields[k][c]?is_boolean>
                    "${c}" : ${form.data.definition.fields[k][c]?string}<#if c_has_next>,</#if>
                    <#elseif form.data.definition.fields[k][c]?is_sequence>
                    "${c}" :
                    [{
                    <#list form.data.definition.fields[k][c] as q>
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
                    }]<#if c_has_next>,</#if>
                    <#else>
                    "${c}" : "${form.data.definition.fields[k][c]}"<#if c_has_next>,</#if>
                    </#if>
                    </#list>
                }<#if k_has_next>,</#if>
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