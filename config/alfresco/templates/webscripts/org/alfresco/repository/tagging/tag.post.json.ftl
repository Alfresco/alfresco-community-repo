<#escape x as jsonUtils.encodeJSONString(x)>
{
   "name": "${tag.name}",
   "nodeRef": "${tag.nodeRef}",
   "itemExists": ${tagExists?string}
}
</#escape>