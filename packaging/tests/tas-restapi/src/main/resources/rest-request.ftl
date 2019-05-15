 public RestReturnedModel ${operationId}() throws Exception
 {
<#if httpMethod == "POST">
  RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, "add-here-json-post-body-u-can-use-JsonBodyGenerator-or-generate-the-model-for-further-use", "${pathUrl}");             
<#elseif httpMethod == "DELETE">
  RestRequest request = RestRequest.simpleRequest(HttpMethod.DELETE, "${pathUrl}");
<#else>
  RestRequest request = RestRequest.simpleRequest(HttpMethod.${httpMethod}, "${pathUrl}?{parameters}", "add-1st-argument-here-or-remove-this", restWrapper.getParameters());        
</#if>
  return restWrapper.processModel(RestReturnedModel.class, request);
 }
------------------------------------------------------------
