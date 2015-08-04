<#escape x as jsonUtils.encodeJSONString(x)>
{
   <#if importedModelName??>
    "modelName": "${importedModelName}",
   </#if>
   <#if shareExtXMLFragment??>
    "shareExtModule": "${shareExtXMLFragment}",
   </#if>
    "status":
    {
      "code": 200,
      "name": "OK",
      "description": "Model uploaded successfully"
    }
}
</#escape>