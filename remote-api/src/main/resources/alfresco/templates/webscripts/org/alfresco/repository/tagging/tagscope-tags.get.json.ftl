<#if (noscopefound?? && noscopefound)>
{
   "tags" : []
}
<#else>
   {
      "tags" : [
         <#import "tagging.lib.ftl" as taggingLib/>
         <#list tags as item>
            <@taggingLib.tagJSON item=item />
            <#if item_has_next>,</#if>
         </#list>
      ]
   }
</#if>