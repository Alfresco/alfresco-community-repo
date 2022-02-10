<#escape x as jsonUtils.encodeJSONString(x)>
{
   "permissions":
   {
      "create": ${container.hasPermission("CreateChildren")?string},
      "edit": ${container.hasPermission("Write")?string}
   }
}
</#escape>