<#macro permissionsJSON permissions>
   <#escape x as jsonUtils.encodeJSONString(x)>
[
   <#list permissions as perm>
   {
      "authority":
      {
      <#if perm.authority.avatar??>
         "avatar": "${"api/node/" + perm.authority.avatar.nodeRef?string?replace('://','/') + "/content/thumbnails/avatar"}",
      </#if>
         "name": "${perm.authority.name}",
         "displayName": "${perm.authority.displayName!perm.authority.name}"
      },
      "role": "${perm.role}"
   }<#if perm_has_next>,</#if>
   </#list>
]
   </#escape>
</#macro>

<#escape x as jsonUtils.encodeJSONString(x)>
{
   "inherited": <@permissionsJSON data.inherited />,
   "isInherited": ${data.isInherited?string},
   "canReadInherited": ${data.canReadInherited?string},
   "direct": <@permissionsJSON data.direct />,
   "settable":
   [
      <#list data.settable as settable>"${settable}"<#if settable_has_next>,</#if></#list>
   ]
}
</#escape>