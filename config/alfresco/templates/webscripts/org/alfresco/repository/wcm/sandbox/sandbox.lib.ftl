<#macro sandboxJSON webproject sandbox>
   <#escape x as jsonUtils.encodeJSONString(x)>
      {
         "sandboxref" : "${sandbox.sandboxRef}",
         "name" : "${sandbox.name}",
         "creator" : "${sandbox.creator}",
         "createdDate" : { "iso8601" : "${sandbox.createdDateAsISO8601}" },
         "storeNames" : [ 
		<#assign names = sandbox.storeNames />
    	  <#list names as name>
    	    "${name}" <#if name_has_next>,</#if>	 	   
          </#list>
  		],
        "isAuthorSandbox" : ${sandbox.authorSandbox?string("true", "false")},
        "isStagingSandbox" : ${sandbox.stagingSandbox?string("true", "false")},
        "url" : "${url.serviceContext + "/api/wcm/webprojects/" + webproject.webProjectRef + "/sandboxes/" + sandbox.sandboxRef}"
      }
   </#escape>
</#macro>
