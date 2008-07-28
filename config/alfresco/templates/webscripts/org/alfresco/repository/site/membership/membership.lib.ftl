<#macro membershipJSON site role person>
	<#escape x as jsonUtils.encodeJSONString(x)>
		{
		   "role" : "${role}",
		   "person":
		   {
		 	  "userName" : "${person.properties.userName}",
		 	  "url" : "${url.serviceContext + "/api/people/" + person.properties.userName}",	 		      	 		   
		 	  "firstName" : "${person.properties.firstName}",
		      "lastName" : "${person.properties.lastName}"
		   },
		   "url" : "${url.serviceContext + "/api/sites/" + site.shortName + "/memberships/" + person.properties.userName}"	 		       
		}
	</#escape>
</#macro>