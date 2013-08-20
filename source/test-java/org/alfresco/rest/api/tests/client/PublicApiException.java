package org.alfresco.rest.api.tests.client;

public class PublicApiException extends Exception
{
	private static final long serialVersionUID = 2622686430090577966L;

	private HttpResponse httpResponse;

	public PublicApiException(Exception e)
	{
		super(e);
	}
	
	public PublicApiException(String message, HttpResponse httpResponse)
	{
		super(message + "/n" + httpResponse.toString());
		this.httpResponse = httpResponse;
	}

	public HttpResponse getHttpResponse()
	{
		return httpResponse;
	}
	
}
