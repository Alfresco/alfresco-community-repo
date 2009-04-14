<#escape x as jsonUtils.encodeJSONString(x)>
{
   "nodeRef": "${image.nodeRef}",
   "fileName": "${image.name}",
   "status":
   {
      "code": 200,
      "name": "OK",
      "description" : "File uploaded successfully"
   }
}
</#escape>