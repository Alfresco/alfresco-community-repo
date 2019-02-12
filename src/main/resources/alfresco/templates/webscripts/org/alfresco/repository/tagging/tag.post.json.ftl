<#escape x as jsonUtils.encodeJSONString(x)>
{
   "name": "${tag.name}",
   "nodeRef": "${tag.nodeRef}",
   "displayPath": "${tag.displayPath}",
   "qnamePath": "${tag.qnamePath}",
   "itemExists": ${tagExists?string}
}
</#escape>