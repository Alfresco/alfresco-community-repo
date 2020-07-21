<#escape x as jsonUtils.encodeJSONString(x)>
{
   "name": "${tag.name}",
   "nodeRef": "${tag.nodeRef}",
   "displayPath": "${tag.displayPath}",
   "itemExists": ${tagExists?string}
}
</#escape>