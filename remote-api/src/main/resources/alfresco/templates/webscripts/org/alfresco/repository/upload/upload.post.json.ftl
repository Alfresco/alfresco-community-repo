<#escape x as jsonUtils.encodeJSONString(x)>
{
   "nodeRef": "${document.nodeRef}",
   "fileName": "${document.name}",
   "status":
   {
      "code": 200,
      "name": "OK",
      "description": "File uploaded successfully"
   }
}
</#escape>