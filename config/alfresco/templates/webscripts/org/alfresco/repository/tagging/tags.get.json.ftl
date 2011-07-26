<#import "tagging.lib.ftl" as taggingLib/>
<#escape x as jsonUtils.encodeJSONString(x)>

<#if details == true>
{
	"data":
	{
		"items":
		[
		   <#list tags as tag>
              <@taggingLib.tagJSONDetails item=tag/><#if tag_has_next>,</#if>
           </#list>
        ]
    }
}
<#else>	
[
   <#list tags as tag>
      "${tag}"<#if tag_has_next>,</#if>
   </#list>
]
</#if>
	
</#escape>