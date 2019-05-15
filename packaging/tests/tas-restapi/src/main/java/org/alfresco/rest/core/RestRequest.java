package org.alfresco.rest.core;

import static org.alfresco.utility.report.log.Step.STEP;

import org.springframework.http.HttpMethod;

import io.restassured.RestAssured;

/**
 * @author Paul Brodner
 */
public class RestRequest
{
    private String body = "";
    private HttpMethod httpMethod;
    private String path;
    private Object[] pathParams;
    private String contentType = "UTF-8";

    private RestRequest(HttpMethod httpMethod, String path, String... pathParams)
    {
        setHttpMethod(httpMethod);
        setPath(path);
        setPathParams(pathParams);
        STEP(toString());
    }

    private RestRequest(HttpMethod httpMethod, String body, String path, String... pathParams)
    {
        setHttpMethod(httpMethod);
        setPath(path);
        setPathParams(pathParams);
        setBody(body);
        STEP(toString());
    }

    /**
     * Use this request when no body is needed
     * 
     * @param httpMethod
     * @param path
     * @param pathParams
     * @return
     */
    public static RestRequest simpleRequest(HttpMethod httpMethod, String path, String... pathParams)
    {       
        return new RestRequest(httpMethod, path, pathParams);
    }

    /**
     * Use this request when a body has to be provided
     * 
     * @param httpMethod
     * @param body
     * @param path
     * @param pathParams
     * @return
     */
    public static RestRequest requestWithBody(HttpMethod httpMethod, String body, String path, String... pathParams)
    {
        return new RestRequest(httpMethod, body, path, pathParams);
    }

    public String getBody()
    {
        return body;
    }

    public void setBody(String body)
    {
        this.body = body;
    }

    public HttpMethod getHttpMethod()
    {
        return httpMethod;
    }

    public void setHttpMethod(HttpMethod httpMethod)
    {
        this.httpMethod = httpMethod;
    }

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public Object[] getPathParams()
    {
        return pathParams;
    }

    public void setPathParams(Object[] pathParams)
    {
        this.pathParams = pathParams;
    }

    public String getContentType()
    {
        return contentType;
    }

    public void setContentType(String contentType)
    {
        this.contentType = contentType;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder()
                    .append("Request: ")
                    .append(getHttpMethod())
                    .append(" ")
                    .append(RestAssured.baseURI)
                    .append(":")
                    .append(RestAssured.port)
                    .append("/")
                    .append(RestAssured.basePath)
                    .append("/");
        
        String getPathFormatted = getPath();
        if(getPath().contains("{"))
        {
            getPathFormatted = getPath().replaceAll("\\{.*?}", "%s");
            getPathFormatted = String.format(getPathFormatted, getPathParams());
        }
        sb.append(getPathFormatted);
                
        if(!getBody().isEmpty())
        {
            sb.append("\nbody:")
              .append(getBody());
        }
        sb.append("\n");
        
        
        return sb.toString();
    }
}
