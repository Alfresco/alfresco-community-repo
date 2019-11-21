package org.alfresco.rest.aos;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.core.RestRequest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

public class AosApiTest extends RestTest 
{
	/*
	 * @author: Catalin Gornea 
	 * 
	 * simple test for demo purposes on how to use Aos with Rest
	 */	
	@Test(enabled=false)
	public void assertResponsIsSuccesufulWhenGetRootDirectory() throws Exception 
	{
		restClient.authenticateUser(dataUser.getAdminUser()).withAosAPI();
		RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "");
		restClient.process(request);
		restClient.assertStatusCodeIs(HttpStatus.OK);
	}
}
