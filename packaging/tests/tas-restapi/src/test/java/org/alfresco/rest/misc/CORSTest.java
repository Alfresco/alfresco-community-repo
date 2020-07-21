package org.alfresco.rest.misc;

import io.restassured.RestAssured;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.core.RestRequest;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

public class CORSTest extends RestTest
{

	@Test(groups = { TestGroup.REST_API, TestGroup.SANITY, TestGroup.CORE})
	public void assertCORSisEnabledAndWorking()
	{
		// Origin url which should be different from the repository url
		String validOriginUrl = "http://localhost:4200";
		String invalidOriginUrl1 = "http://localhost:4201";
		String invalidOriginUrl2 = "http://example.com";
		RestAssured.basePath = "alfresco/api/-default-/public/authentication/versions/1";
		restClient.configureRequestSpec().setBasePath(RestAssured.basePath);

		RestRequest request = RestRequest.simpleRequest(HttpMethod.OPTIONS, "tickets");

		// Don't specify header Access-Control-Request-Method
		restClient.configureRequestSpec().addHeader("Origin", validOriginUrl);
		restClient.process(request);
		restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);

		// request not allowed method
		restClient.configureRequestSpec().addHeader("Access-Control-Request-Method", "PATCH");
		restClient.configureRequestSpec().addHeader("Origin", validOriginUrl);
		restClient.process(request);
		restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN);

		// request invalid method
		restClient.configureRequestSpec().addHeader("Access-Control-Request-Method", "invalid");
		restClient.configureRequestSpec().addHeader("Origin", validOriginUrl);
		restClient.process(request);
		restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN);

		// use invalidOriginUrl1 as origin
		restClient.configureRequestSpec().addHeader("Access-Control-Request-Method", "POST");
		restClient.configureRequestSpec().addHeader("Origin", invalidOriginUrl1);
		restClient.process(request);
		restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN);

		// use invalidOriginUrl2 as origin
		restClient.configureRequestSpec().addHeader("Origin", invalidOriginUrl2);
		restClient.process(request);
		restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN);

		// use validOriginUrl
		restClient.configureRequestSpec().addHeader("Access-Control-Request-Method", "POST");
		restClient.configureRequestSpec().addHeader("Origin", validOriginUrl);
		restClient.process(request);

		restClient.assertStatusCodeIs(HttpStatus.OK);
		restClient.assertHeaderValueContains("Access-Control-Allow-Origin", validOriginUrl);
		restClient.assertHeaderValueContains("Access-Control-Allow-Credentials", "true");
		restClient.assertHeaderValueContains("Access-Control-Max-Age", "10");
		restClient.assertHeaderValueContains("Access-Control-Allow-Methods", "POST");
	}

	@Test(groups = { TestGroup.REST_API, TestGroup.SANITY, TestGroup.CORE})
	public void assertCORSisEnabledAndWorkingForDiscovery()
	{
		// Origin url which should be different from the repository url
		String validOriginUrl = "http://localhost:4200";
		RestAssured.basePath = "alfresco/api";
		restClient.configureRequestSpec().setBasePath(RestAssured.basePath);

		RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "discovery");

		// use validOriginUrl
		restClient.configureRequestSpec().addHeader("Access-Control-Request-Method", "GET");
		restClient.configureRequestSpec().addHeader("Origin", validOriginUrl);

		UserModel userModel = dataUser.createRandomTestUser();
		restClient.authenticateUser(userModel).process(request);

		restClient.assertStatusCodeIs(HttpStatus.OK);
		restClient.assertHeaderValueContains("Access-Control-Allow-Origin", validOriginUrl);
		restClient.assertHeaderValueContains("Access-Control-Allow-Credentials", "true");
		restClient.assertHeaderValueContains("Access-Control-Expose-Headers", "Access-Control-Allow-Origin,Access-Control-Allow-Credentials");
	}
}
