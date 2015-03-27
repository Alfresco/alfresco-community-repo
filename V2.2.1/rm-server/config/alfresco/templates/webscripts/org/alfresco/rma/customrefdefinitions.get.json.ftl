<#escape x as jsonUtils.encodeJSONString(x)>
{
	"data":
	{
        "customReferences":
        [
            <#list customRefs as ref>
            {
                <#assign keys = ref?keys>
                <#list keys as key>"${key}": "${ref[key]}"<#if key_has_next>,</#if></#list>
            }<#if ref_has_next>,</#if>
            </#list>
        ]
	}
}
</#escape>