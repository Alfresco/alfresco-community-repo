<#-- renders an rm role object -->
<#macro roleJSON role>
<#escape x as jsonUtils.encodeJSONString(x)>
{
   "name": "${role.name}",
   "displayLabel": "${role.displayLabel}",
   "capabilities":
   {
   <#list role.capabilities as capability>
      "${capability.name}": "${capability.title}" <#if capability_has_next>,</#if>
   </#list>
   }
   <#if role.showAuths>
   ,
   "assignedUsers" :
   [
   <#list role.assignedUsers as user>
      {
      	"name" : "${user.name}",
      	"displayLabel" : "${user.displayLabel}"
      }<#if user_has_next>,</#if>
   </#list>
   ],
   "assignedGroups" :
   [
   <#list role.assignedGroups as group>
      {
      	"name" : "${group.name}",
      	"displayLabel" : "${group.displayLabel}"
      }<#if group_has_next>,</#if>
   </#list>
   ]
   <#if role.groupShortName??>
   ,"groupShortName": "${role.groupShortName}"
   </#if>
   </#if>
}
</#escape>
</#macro>