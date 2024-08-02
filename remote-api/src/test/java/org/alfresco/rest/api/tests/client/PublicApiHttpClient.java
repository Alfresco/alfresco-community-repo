/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.rest.api.tests.client;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.opencmis.CMISDispatcherRegistry.Binding;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.web.scripts.BaseWebScriptTest.PatchMethod;
import org.alfresco.rest.api.tests.TestFixture;
import org.alfresco.rest.api.tests.client.AuthenticatedHttp.HttpRequestCallback;
import org.alfresco.rest.framework.Api;
import org.alfresco.rest.framework.Api.SCOPE;
import org.alfresco.rest.framework.core.ResourceInspector;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.Pair;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.methods.OptionsMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.methods.TraceMethod;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;

/**
 * A http client for talking to the rest apis. The caller can pass in a rest api
 * implementation class to the http method (get, post, put, delete supported)
 * and the url will be generated.
 *
 * @author steveglover
 */
public class PublicApiHttpClient
{
    private static final Log logger = LogFactory.getLog(PublicApiHttpClient.class);

    private static final String OLD_BASE_URL = "{0}://{1}:{2}{3}{4}{5}/api/";
    private static final String INDEX_URL = "{0}://{1}:{2}{3}{4}";
    private static final String BASE_URL = "{0}://{1}:{2}{3}{4}{5}/{6}/{7}/versions/{8}";
    private static final String PUBLICAPI_CMIS_SERVICE_URL = "{0}://{1}:{2}{3}{4}cmis/versions/{5}/{6}";
    private static final String PUBLICAPI_CMIS_URL = "{0}://{1}:{2}{3}{4}{5}/{6}/cmis/versions/{7}/{8}";
    private static final String PUBLICAPI_CMIS_URL_SUFFIX = "{0}/{1}/cmis/versions/{2}/{3}";
    private static final String ATOM_PUB_URL = "{0}://{1}:{2}{3}cmisatom";
    
    private String scheme = "http";
    private String host = TestFixture.HOST;
    private int port = TestFixture.PORT;

    private String contextPath;
    private String servletName;
    private AuthenticatedHttp authenticatedHttp;

    private ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
    private MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(this.resourcePatternResolver);

    // can be overriden by other clients like the workflow client
    protected String apiName = "alfresco";

    public PublicApiHttpClient(String host, int port, String contextPath, String servletName, AuthenticatedHttp authenticatedHttp)
    {
        this("http", host, port, contextPath, servletName, authenticatedHttp);
    }

    public PublicApiHttpClient(String scheme, String host, int port, String contextPath, String servletName, AuthenticatedHttp authenticatedHttp)
    {
        super();
        this.scheme = scheme;
        this.host = host;
        this.port = port;
        this.contextPath = contextPath;
        if (this.contextPath != null && !this.contextPath.isEmpty() && !this.contextPath.endsWith("/"))
        {
            this.contextPath = this.contextPath + "/";
        }
        if (this.contextPath != null && !this.contextPath.startsWith("/"))
        {
            this.contextPath = "/" + this.contextPath;
        }
        this.servletName = servletName;
        if (this.servletName != null && !this.servletName.isEmpty() && !this.servletName.endsWith("/"))
        {
            this.servletName = this.servletName + "/";
        }
        this.authenticatedHttp = authenticatedHttp;
    }

    public String getCmisUrl(String repositoryId, String operation)
    {
        StringBuilder url = new StringBuilder();
        if (repositoryId == null)
        {
            url.append(MessageFormat.format(ATOM_PUB_URL, new Object[] { scheme, host, String.valueOf(port), contextPath }));
        }
        else
        {
            url.append(MessageFormat.format(ATOM_PUB_URL, new Object[] { scheme, host, String.valueOf(port), contextPath }));
            url.append("/");
            url.append(repositoryId);
        }

        if (operation != null)
        {
            url.append("/");
            url.append(operation);
        }

        return url.toString();
    }

    public String getPublicApiCmisUrl(String networkId, Binding binding, String version, String operation)
    {
        StringBuilder url = new StringBuilder();
        if (networkId == null)
        {
            url.append(MessageFormat.format(PUBLICAPI_CMIS_SERVICE_URL,
                        new Object[] { scheme, host, String.valueOf(port), contextPath, servletName, version, binding.toString().toLowerCase() }));
        }
        else
        {
            url.append(MessageFormat.format(PUBLICAPI_CMIS_URL, new Object[] { scheme, host, String.valueOf(port), contextPath, servletName,
                        networkId, "public", version, binding.toString().toLowerCase() }));
        }

        if (operation != null)
        {
            url.append("/");
            url.append(operation);
        }

        return url.toString();
    }

    public String getPublicApiCmisUrlSuffix(String networkId, Binding binding, String version, String operation)
    {
        StringBuilder url = new StringBuilder();

        url.append(MessageFormat.format(PUBLICAPI_CMIS_URL_SUFFIX, new Object[] { networkId, "public", version, binding.toString().toLowerCase() }));

        if (operation != null)
        {
            url.append("/");
            url.append(operation);
        }

        return url.toString();
    }

    public void setHost(String host)
    {
        this.host = host;
    }

    public void setPort(int port)
    {
        this.port = port;
    }

    public void setContextPath(String contextPath)
    {
        this.contextPath = contextPath;
    }

    public void init()
    {
    }

    private void log(String msg)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug(msg);
        }
    }

    protected AnnotationMetadata getAnnotationMetadata(String classname) throws IOException
    {
        MetadataReader metadataReader = this.metadataReaderFactory.getMetadataReader(classname);
        AnnotationMetadata annotationMetaData = metadataReader.getAnnotationMetadata();
        return annotationMetaData;
    }

    public HttpResponse submitRequest(HttpMethod req, final RequestContext rq) throws HttpException, IOException
    {
        try
        {
            final long start = System.currentTimeMillis();

            final HttpRequestCallback<HttpResponse> callback = new HttpRequestCallback<HttpResponse>()
            {
                @Override
                public HttpResponse onCallSuccess(HttpMethod method) throws Exception
                {
                    long end = System.currentTimeMillis();

                    Map<String, String> headersMap = null;
                    Header[] headers = method.getResponseHeaders();
                    if (headers != null)
                    {
                        headersMap = new HashMap<String, String>(headers.length);
                        for (Header header : headers)
                        {
                            headersMap.put(header.getName(), header.getValue());
                        }
                    }

                    return new HttpResponse(method, rq.getRunAsUser(), method.getResponseBody(), headersMap, (end - start));
                }

                @Override
                public boolean onError(HttpMethod method, Throwable t)
                {
                    return false;
                }
            };

            HttpResponse response = null;
            if (rq.getPassword() != null)
            {
                response = authenticatedHttp.executeHttpMethodAuthenticated(req, rq.getRunAsUser(), rq.getPassword(), callback);
            }
            else if (rq.getRunAsUser() != null)
            {
                response = authenticatedHttp.executeHttpMethodAuthenticated(req, rq.getRunAsUser(), callback);
            }
            else
            {
                response = authenticatedHttp.executeHttpMethodUnauthenticated(req, callback);
            }
            return response;
        }
        finally
        {
            if (req != null)
            {
                req.releaseConnection();
            }
        }
    }

    public HttpResponse get(final RequestContext rq, final String urlSuffix, Map<String, String> params) throws IOException
    {
        RestApiEndpoint endpoint = new RestApiEndpoint(rq.getNetworkId(), urlSuffix, params);
        String url = endpoint.getUrl();

        GetMethod req = new GetMethod(url);
        return submitRequest(req, rq);
    }

    public HttpResponse get(final Class<?> c, final RequestContext rq, final Object entityId, final Object relationshipEntityId,
                Map<String, String> params) throws IOException
    {
        RestApiEndpoint endpoint = new RestApiEndpoint(c, rq.getNetworkId(), entityId, relationshipEntityId, params);
        String url = endpoint.getUrl();

        GetMethod req = new GetMethod(url);
        return submitRequest(req, rq);
    }

    public HttpResponse get(final RequestContext rq, String scope, final String entityCollectionName, final Object entityId,
                final String relationCollectionName, final Object relationshipEntityId, Map<String, String> params) throws IOException
    {
        return get(rq, scope, 1, entityCollectionName, entityId, relationCollectionName, relationshipEntityId, params, null);
    }

    public HttpResponse get(final RequestContext rq, String scope, final String entityCollectionName, final Object entityId,
                            final String relationCollectionName, final Object relationshipEntityId, Map<String, String> params, Map<String, String> headers) throws IOException
    {
        return get(rq, scope, 1, entityCollectionName, entityId, relationCollectionName, relationshipEntityId, params, headers);
    }

    public HttpResponse get(final RequestContext rq, final String scope, final int version, final String entityCollectionName, final Object entityId,
                final String relationCollectionName, final Object relationshipEntityId, Map<String, String> params, Map<String, String> headers) throws IOException
    {
        if (headers == null)
        {
            headers = Collections.emptyMap();
        }

        RestApiEndpoint endpoint = new RestApiEndpoint(rq.getNetworkId(), scope, version, entityCollectionName, entityId, relationCollectionName,
                    relationshipEntityId, params);
        String url = endpoint.getUrl();

        GetMethod req = new GetMethod(url);

        for (Entry<String, String> header : headers.entrySet())
        {
            req.addRequestHeader(header.getKey(), header.getValue());
        }

        return submitRequest(req, rq);
    }

    public HttpResponse get(final RequestContext rq, String scope, String password, final String entityCollectionName, final Object entityId,
                final String relationCollectionName, final Object relationshipEntityId, Map<String, String> params) throws IOException
    {
        return get(rq, scope, 1, password, entityCollectionName, entityId, relationCollectionName, relationshipEntityId, params);
    }

    public HttpResponse get(final RequestContext rq, final String scope, final int version, final String password, final String entityCollectionName, final Object entityId,
                final String relationCollectionName, final Object relationshipEntityId, Map<String, String> params) throws IOException
    {
        RestApiEndpoint endpoint = new RestApiEndpoint(rq.getNetworkId(), scope, version, entityCollectionName, entityId, relationCollectionName,
                    relationshipEntityId, params);
        String url = endpoint.getUrl();

        GetMethod req = new GetMethod(url);
        return submitRequest(req, rq);
    }

    public HttpResponse get(final String urlSuffix, final RequestContext rq, Map<String, String> params) throws IOException
    {
        RestApiEndpoint endpoint = new RestApiEndpoint(urlSuffix, params);
        String url = endpoint.getUrl();

        GetMethod req = new GetMethod(url);
        return submitRequest(req, rq);
    }

    public HttpResponse getWithPassword(final String urlSuffix, final RequestContext rq, Map<String, String> params) throws IOException
    {
        RestApiEndpoint endpoint = new RestApiEndpoint(urlSuffix, params);
        String url = endpoint.getUrl();

        GetMethod req = new GetMethod(url);
        return submitRequest(req, rq);
    }

    public HttpResponse post(final Class<?> c, final RequestContext rq, final Object entityId, final Object relationshipEntityId, final String body)
                throws IOException
    {
        RestApiEndpoint endpoint = new RestApiEndpoint(c, rq.getNetworkId(), entityId, relationshipEntityId, null);
        String url = endpoint.getUrl();

        PostMethod req = new PostMethod(url.toString());
        if (body != null)
        {
            StringRequestEntity requestEntity = new StringRequestEntity(body, "application/json", "UTF-8");
            req.setRequestEntity(requestEntity);
        }
        return submitRequest(req, rq);
    }

    public HttpResponse post(final RequestContext rq, final String urlSuffix, String body) throws IOException
    {
        RestApiEndpoint endpoint = new RestApiEndpoint(rq.getNetworkId(), urlSuffix, null);
        String url = endpoint.getUrl();

        PostMethod req = new PostMethod(url.toString());
        if (body != null)
        {
            StringRequestEntity requestEntity = new StringRequestEntity(body, "application/json", "UTF-8");
            req.setRequestEntity(requestEntity);
        }
        return submitRequest(req, rq);
    }

    public HttpResponse post(final RequestContext rq, Binding cmisBinding, String version, String cmisOperation, final String body) throws IOException
    {
        RestApiEndpoint endpoint = new RestApiEndpoint(rq.getNetworkId(), cmisBinding, version, cmisOperation, null);
        String url = endpoint.getUrl();

        PostMethod req = new PostMethod(url.toString());
        if (body != null)
        {
            StringRequestEntity requestEntity = null;
            if (cmisBinding.equals(Binding.atom))
            {
                requestEntity = new StringRequestEntity(body, "text/xml", "UTF-8");
            }
            else if (cmisBinding.equals(Binding.browser))
            {
                requestEntity = new StringRequestEntity(body, "application/json", "UTF-8");
            }
            req.setRequestEntity(requestEntity);
        }
        return submitRequest(req, rq);
    }

    public HttpResponse put(final RequestContext rq, Binding cmisBinding, String version, String cmisOperation, final String body) throws IOException
    {
        RestApiEndpoint endpoint = new RestApiEndpoint(rq.getNetworkId(), cmisBinding, version, cmisOperation, null);
        String url = endpoint.getUrl();

        PutMethod req = new PutMethod(url.toString());
        if (body != null)
        {
            StringRequestEntity requestEntity = null;
            if (cmisBinding.equals(Binding.atom))
            {
                requestEntity = new StringRequestEntity(body, "text/xml", "UTF-8");
            }
            else if (cmisBinding.equals(Binding.browser))
            {
                requestEntity = new StringRequestEntity(body, "application/json", "UTF-8");
            }
            req.setRequestEntity(requestEntity);
        }
        return submitRequest(req, rq);
    }

    public HttpResponse get(final RequestContext rq, Binding cmisBinding, String version, String cmisOperation, Map<String, String> parameters)
                throws IOException
    {
        RestApiEndpoint endpoint = new RestApiEndpoint(rq.getNetworkId(), cmisBinding, version, cmisOperation, parameters);
        String url = endpoint.getUrl();

        GetMethod req = new GetMethod(url.toString());
        return submitRequest(req, rq);
    }

    public HttpResponse delete(final RequestContext rq, Binding cmisBinding, String version, String cmisOperation) throws IOException
    {
        RestApiEndpoint endpoint = new RestApiEndpoint(rq.getNetworkId(), cmisBinding, version, cmisOperation, null);
        String url = endpoint.getUrl();

        DeleteMethod req = new DeleteMethod(url.toString());
        return submitRequest(req, rq);
    }

    public HttpResponse head(final RequestContext rq, Binding cmisBinding, String version, String cmisOperation) throws IOException
    {
        RestApiEndpoint endpoint = new RestApiEndpoint(rq.getNetworkId(), cmisBinding, version, cmisOperation, null);
        String url = endpoint.getUrl();

        HeadMethod req = new HeadMethod(url.toString());
        return submitRequest(req, rq);
    }

    public HttpResponse options(final RequestContext rq, Binding cmisBinding, String version, String cmisOperation) throws IOException
    {
        RestApiEndpoint endpoint = new RestApiEndpoint(rq.getNetworkId(), cmisBinding, version, cmisOperation, null);
        String url = endpoint.getUrl();

        OptionsMethod req = new OptionsMethod(url.toString());
        return submitRequest(req, rq);
    }

    public HttpResponse trace(final RequestContext rq, Binding cmisBinding, String version, String cmisOperation) throws IOException
    {
        RestApiEndpoint endpoint = new RestApiEndpoint(rq.getNetworkId(), cmisBinding, version, cmisOperation, null);
        String url = endpoint.getUrl();

        TraceMethod req = new TraceMethod(url.toString());
        return submitRequest(req, rq);
    }

    public HttpResponse patch(final RequestContext rq, Binding cmisBinding, String version, String cmisOperation) throws IOException
    {
        RestApiEndpoint endpoint = new RestApiEndpoint(rq.getNetworkId(), cmisBinding, version, cmisOperation, null);
        String url = endpoint.getUrl();

        PatchMethod req = new PatchMethod(url.toString());
        return submitRequest(req, rq);
    }

    public HttpResponse post(final RequestContext rq, final String scope, final String entityCollectionName, final Object entityId,
                final String relationCollectionName, final Object relationshipEntityId, final String body) throws IOException
    {
        return post(rq, scope, entityCollectionName, entityId, relationCollectionName, relationshipEntityId, body, "application/json");
    }

    public HttpResponse post(final RequestContext rq, final String scope, final String entityCollectionName, final Object entityId,
            final String relationCollectionName, final Object relationshipEntityId, final String body, final Map<String, String> params) throws IOException
    {
        return post(rq, scope, 1, entityCollectionName, entityId, relationCollectionName, relationshipEntityId, body, "application/json", params);
    }

    public HttpResponse post(final RequestContext rq, final String scope, final String entityCollectionName, final Object entityId,
                final String relationCollectionName, final Object relationshipEntityId, final String body, String contentType) throws IOException
    {
        return post(rq, scope, 1, entityCollectionName, entityId, relationCollectionName, relationshipEntityId, body, contentType);
    }

    public HttpResponse post(final RequestContext rq, final String scope, final int version, final String entityCollectionName, final Object entityId,
            final String relationCollectionName, final Object relationshipEntityId, final String body, String contentType) throws IOException
    {
        return post(rq, scope, version, entityCollectionName, entityId, relationCollectionName, relationshipEntityId, body, contentType, null);
    }

    public HttpResponse post(final RequestContext rq, final String scope, final int version, final String entityCollectionName, final Object entityId,
                final String relationCollectionName, final Object relationshipEntityId, final String body, String contentType, final Map<String, String> params) throws IOException
    {
        RestApiEndpoint endpoint = new RestApiEndpoint(rq.getNetworkId(), scope, version, entityCollectionName, entityId, relationCollectionName,
                    relationshipEntityId, params);
        String url = endpoint.getUrl();

        PostMethod req = new PostMethod(url.toString());
        if (body != null)
        {
            if (contentType == null || contentType.isEmpty())
            {
                contentType = "application/json";
            }
            StringRequestEntity requestEntity = new StringRequestEntity(body, contentType, "UTF-8");
            req.setRequestEntity(requestEntity);
        }
        return submitRequest(req, rq);
    }

    public HttpResponse post(final RequestContext rq, final String scope, final String entityCollectionName, final Object entityId,
                             final String relationCollectionName, final Object relationshipEntityId, final byte[] body, String contentType) throws IOException
    {
        return post(rq, scope, 1, entityCollectionName, entityId, relationCollectionName, relationshipEntityId, body, contentType);
    }

    public HttpResponse post(final RequestContext rq, final String scope, final int version, final String entityCollectionName, final Object entityId,
                             final String relationCollectionName, final Object relationshipEntityId, final byte[] body, String contentType) throws IOException
    {
        RestApiEndpoint endpoint = new RestApiEndpoint(rq.getNetworkId(), scope, version, entityCollectionName, entityId, relationCollectionName,
                relationshipEntityId, null);
        String url = endpoint.getUrl();

        PostMethod req = new PostMethod(url.toString());
        if (body != null)
        {
            if (contentType == null || contentType.isEmpty())
            {
                contentType = "application/octet-stream";
            }
            ByteArrayRequestEntity requestEntity = new ByteArrayRequestEntity(body, contentType);
            req.setRequestEntity(requestEntity);
        }
        return submitRequest(req, rq);
    }

    public HttpResponse delete(final Class<?> c, final RequestContext rq, final Object entityId, final Object relationshipEntityId) throws IOException
    {
        RestApiEndpoint endpoint = new RestApiEndpoint(c, rq.getNetworkId(), entityId, relationshipEntityId, null);
        String url = endpoint.getUrl();

        DeleteMethod req = new DeleteMethod(url);
        return submitRequest(req, rq);
    }

    public HttpResponse delete(final RequestContext rq, final String scope, final String entityCollectionName, final Object entityId,
                final String relationCollectionName, final Object relationshipEntityId) throws IOException
    {
        return delete(rq, scope, 1, entityCollectionName, entityId, relationCollectionName, relationshipEntityId, null);
    }

    public HttpResponse delete(final RequestContext rq, final String scope, final int version, final String entityCollectionName, final Object entityId,
                final String relationCollectionName, final Object relationshipEntityId, final Map<String, String> params) throws IOException
    {
        RestApiEndpoint endpoint = new RestApiEndpoint(rq.getNetworkId(), scope, version, entityCollectionName, entityId, relationCollectionName,
                    relationshipEntityId, params);
        String url = endpoint.getUrl();

        DeleteMethod req = new DeleteMethod(url);
        return submitRequest(req, rq);
    }

    public HttpResponse put(final Class<?> c, final RequestContext rq, final Object entityId, final Object relationshipEntityId, final String body)
                throws IOException
    {
        RestApiEndpoint endpoint = new RestApiEndpoint(c, rq.getNetworkId(), entityId, relationshipEntityId, null);
        String url = endpoint.getUrl();

        PutMethod req = new PutMethod(url);
        if (body != null)
        {
            StringRequestEntity requestEntity = new StringRequestEntity(body, "application/json", "UTF-8");
            req.setRequestEntity(requestEntity);
        }
        return submitRequest(req, rq);
    }

    public HttpResponse put(final RequestContext rq, final String scope, final String entityCollectionName, final Object entityId,
                final String relationCollectionName, final Object relationshipEntityId, final String body, final Map<String, String> params) throws IOException
    {
        return put(rq, scope, 1, entityCollectionName, entityId, relationCollectionName, relationshipEntityId, body, params);
    }

    public HttpResponse put(final RequestContext rq, final String scope, final int version, final String entityCollectionName, final Object entityId,
                final String relationCollectionName, final Object relationshipEntityId, final String body, Map<String, String> params) throws IOException
    {
        RestApiEndpoint endpoint = new RestApiEndpoint(rq.getNetworkId(), scope, version, entityCollectionName, entityId, relationCollectionName,
                    relationshipEntityId, params);
        String url = endpoint.getUrl();

        PutMethod req = new PutMethod(url);
        if (body != null)
        {
            StringRequestEntity requestEntity = new StringRequestEntity(body, "application/json", "UTF-8");
            req.setRequestEntity(requestEntity);
        }
        return submitRequest(req, rq);
    }

    public HttpResponse putBinary(final RequestContext rq, final String scope, final int version, final String entityCollectionName,
                final Object entityId, final String relationCollectionName, final Object relationshipEntityId, final BinaryPayload payload,
                final Map<String, String> params) throws IOException
    {
        RestApiEndpoint endpoint = new RestApiEndpoint(rq.getNetworkId(), scope, version, entityCollectionName, entityId, relationCollectionName,
                    relationshipEntityId, params);
        String url = endpoint.getUrl();

        PutMethod req = new PutMethod(url);
        if (payload != null)
        {
            BinaryRequestEntity requestEntity = new BinaryRequestEntity(payload.getFile(), payload.getMimeType(), payload.getCharset());
            req.setRequestEntity(requestEntity);
        }
        return submitRequest(req, rq);
    }

    public HttpResponse execute(RequestBuilder requestBuilder) throws IOException
    {
        return submitRequest(requestBuilder.getHttpMethod(), requestBuilder.getRequestContext());
    }

    /*
     * Encapsulates information relating to a rest api end point, generating and
     * encoding urls based on the rest api implementation class.
     */
    private class RestApiEndpoint
    {
        private String url;

        RestApiEndpoint(String url, Map<String, String> params) throws IOException
        {
            StringBuilder sb = new StringBuilder(
                        MessageFormat.format(INDEX_URL, new Object[] { scheme, host, String.valueOf(port), contextPath, servletName }));
            if (url != null)
            {
                sb.append(url);
            }

            addParams(sb, params);

            this.url = sb.toString();
        }

        RestApiEndpoint(String tenantDomain, String url, Map<String, String> params) throws IOException
        {
            StringBuilder sb = new StringBuilder(MessageFormat.format(OLD_BASE_URL, new Object[] { scheme, host, String.valueOf(port), contextPath,
                        servletName, tenantDomain == null ? TenantUtil.DEFAULT_TENANT : tenantDomain }));
            sb.append("/");
            sb.append(url);

            addParams(sb, params);

            this.url = sb.toString();
        }

        RestApiEndpoint(Class<?> resourceClass, String tenantDomain, Object collectionEntityId, Object relationEntityId, Map<String, String> params)
                    throws IOException
        {
            StringBuilder sb = new StringBuilder();

            Api api = ResourceInspector.inspectApi(resourceClass);
            SCOPE scope = api.getScope();
            int version = api.getVersion();
            String name = api.getName();
            if (StringUtils.isEmpty(name))
            {
                name = apiName;
            }

            Pair<String, String> relationshipCollectionInfo = getRelationCollectionInfo(resourceClass);

            sb.append(MessageFormat.format(BASE_URL, new Object[] { scheme, host, String.valueOf(port), contextPath, servletName,
                        tenantDomain == null ? TenantUtil.DEFAULT_TENANT : tenantDomain, scope.toString(), name, version }));

            if (relationshipCollectionInfo != null)
            {
                String entityCollectionName = relationshipCollectionInfo.getFirst();
                String relationshipCollectionName = relationshipCollectionInfo.getSecond();
                String relationEntityIdString = encodeToString(relationEntityId);
                String collectionEntityIdString = encodeToString(collectionEntityId);

                sb.append('/');
                sb.append(entityCollectionName);
                sb.append('/');
                if (collectionEntityIdString != null)
                {
                    sb.append(collectionEntityIdString);
                    sb.append('/');
                }

                sb.append(relationshipCollectionName);
                sb.append('/');
                if (relationEntityIdString != null)
                {
                    sb.append(relationEntityIdString);
                    sb.append('/');
                }
            }
            else
            {
                String entityCollectionName = getEntityCollectionInfo(resourceClass);
                if (entityCollectionName != null)
                {
                    String collectionEntityIdString = encodeToString(collectionEntityId);

                    sb.append('/');
                    sb.append(entityCollectionName);
                    sb.append('/');
                    if (collectionEntityIdString != null)
                    {
                        sb.append(collectionEntityIdString);
                        sb.append('/');
                    }
                }
                else
                {
                    throw new RuntimeException();
                }
            }

            addParams(sb, params);

            this.url = sb.toString();
        }

        RestApiEndpoint(String networkId, Binding cmisBinding, String version, String cmisOperation, Map<String, String> params) throws IOException
        {
            StringBuilder sb = new StringBuilder();

            if (networkId != null)
            {
                sb.append(getPublicApiCmisUrl(networkId, cmisBinding, version, cmisOperation));
            }
            else
            {
                throw new IllegalArgumentException();
            }

            addParams(sb, params);

            this.url = sb.toString();
        }

        RestApiEndpoint(String tenantDomain, String scope, int version, String collectionName, Object collectionEntityId, String relationName,
                    Object relationEntityId, Map<String, String> params) throws IOException
        {
            this(tenantDomain, scope, apiName, version, collectionName, collectionEntityId, relationName, relationEntityId, params);
        }

        RestApiEndpoint(String tenantDomain, String scope, String apiName, int version, String collectionName, Object collectionEntityId,
                    String relationName, Object relationEntityId, Map<String, String> params) throws IOException
        {
            StringBuilder sb = new StringBuilder();

            if (tenantDomain == null || tenantDomain.equals(TenantService.DEFAULT_DOMAIN))
            {
                tenantDomain = TenantUtil.DEFAULT_TENANT;
            }

            if (StringUtils.isEmpty(apiName))
            {
                apiName = PublicApiHttpClient.this.apiName;
            }

            sb.append(MessageFormat.format(BASE_URL,
                        new Object[] { scheme, host, String.valueOf(port), contextPath, servletName, tenantDomain, scope, apiName, version }));

            if (collectionName != null)
            {
                sb.append('/');
                sb.append(collectionName);
                if (collectionEntityId != null)
                {
                    sb.append('/');
                    sb.append(collectionEntityId);
                }
            }

            if (relationName != null)
            {
                sb.append('/');
                sb.append(relationName);
                if (relationEntityId != null)
                {
                    sb.append('/');
                    sb.append(relationEntityId);
                }
            }

            addParams(sb, params);

            this.url = sb.toString();
        }

        RestApiEndpoint(String tenantDomain, String scope, String collectionName, Object collectionEntityId, String relationName,
                    Object relationEntityId, Map<String, String> params) throws IOException
        {
            this(tenantDomain, scope, 1, collectionName, collectionEntityId, relationName, relationEntityId, params);
        }

        private void addParams(StringBuilder sb, Map<String, String> params) throws UnsupportedEncodingException
        {
            if (params != null && params.size() > 0)
            {
                sb.append("?");

                for (String paramName : params.keySet())
                {
                    sb.append(URLEncoder.encode(paramName, "UTF-8"));
                    sb.append("=");
                    sb.append(URLEncoder.encode(params.get(paramName), "UTF-8"));
                    sb.append("&");
                }

                sb.deleteCharAt(sb.length() - 1);
            }
        }

        private String encodeToString(Object o) throws UnsupportedEncodingException
        {
            String ret = null;

            if (o instanceof NodeRef)
            {
                NodeRef nodeRef = (NodeRef) o;
                ret = (o != null ? nodeRef.getId() : null);
            }
            else
            {
                ret = (o != null ? o.toString() : null);
            }

            return ret;
        }

        private Pair<String, String> getRelationCollectionInfo(Class<?> resourceClass) throws IOException
        {
            AnnotationMetadata annotationMetaData = getAnnotationMetadata(resourceClass.getCanonicalName());
            if (annotationMetaData.isConcrete() && annotationMetaData.isIndependent())
            {
                if (annotationMetaData.getAnnotationTypes().contains(RelationshipResource.class.getCanonicalName()))
                {
                    Map<String, Object> attrs = annotationMetaData.getAnnotationAttributes(RelationshipResource.class.getName());
                    String relationshipCollectionName = (String) attrs.get("name");
                    Class<?> entityResource = (Class<?>) attrs.get("entityResource");

                    String entityCollectionName = getEntityCollectionInfo(entityResource.getCanonicalName());

                    Pair<String, String> ret = new Pair<String, String>(entityCollectionName, relationshipCollectionName);
                    return ret;
                }
                else
                {
                    return null;
                }
            }
            else
            {
                throw new AlfrescoRuntimeException("");
            }
        }

        private String getEntityCollectionInfo(Class<?> resourceClass) throws IOException
        {
            return getEntityCollectionInfo(resourceClass.getCanonicalName());
        }

        private String getEntityCollectionInfo(String className) throws IOException
        {
            AnnotationMetadata annotationMetaData = getAnnotationMetadata(className);
            if (annotationMetaData.isConcrete() && annotationMetaData.isIndependent())
            {
                if (annotationMetaData.getAnnotationTypes().contains(EntityResource.class.getCanonicalName()))
                {
                    Map<String, Object> attrs = annotationMetaData.getAnnotationAttributes(EntityResource.class.getName());
                    return (String) attrs.get("name");
                }
                else
                {
                    return null;
                }
            }
            else
            {
                throw new AlfrescoRuntimeException("");
            }
        }

        public String getUrl() throws UnsupportedEncodingException
        {
            return url;
        }
    }

    /**
     * @author Jamal Kaabi-Mofrad
     */
    public static class BinaryRequestEntity implements RequestEntity
    {
        private final File file;
        private final String mimeType;
        private final String charset;

        public BinaryRequestEntity(File file, String mimeType, String charset)
        {
            this.file = file;
            this.mimeType = mimeType;
            this.charset = charset;
        }

        @Override
        public boolean isRepeatable()
        {
            return true;
        }

        @Override
        public void writeRequest(OutputStream out) throws IOException
        {
            InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
            try
            {
                int len;
                byte[] buffer = new byte[8190];
                while ((len = inputStream.read(buffer)) != -1)
                {
                    out.write(buffer, 0, len);
                }
            }
            finally
            {
                inputStream.close();
            }
        }

        @Override
        public long getContentLength()
        {
            return file.length();
        }

        @Override
        public String getContentType()
        {
            if (charset == null)
            {
                return mimeType;
            }
            if (mimeType == null)
            {
                return null;
            }
            return mimeType + "; charset=" + charset;
        }
    }

    /**
     * @author Jamal Kaabi-Mofrad
     */
    public static class BinaryPayload
    {
        private File file;
        private String mimeType;
        private String charset;

        public BinaryPayload(File file, String mimeType, String charset)
        {
            this.file = file;
            this.mimeType = mimeType;
            this.charset = charset;
        }

        public BinaryPayload(File file, String mimeType)
        {
            this(file, mimeType, null);
        }

        public BinaryPayload(File file)
        {
            this(file, null, null);
        }

        public File getFile()
        {
            return file;
        }

        public String getMimeType()
        {
            return mimeType;
        }

        public String getCharset()
        {
            return charset;
        }
    }

    /**
     * @author Jamal Kaabi-Mofrad
     */
    public abstract class RequestBuilder<T extends HttpMethod>
    {
        private RequestContext requestContext;
        private String scope;
        private String apiName = "alfresco"; // default api namespace
        private int version = 1;
        private String entityCollectionName;
        private Object entityId;
        private String relationCollectionName;
        private Object relationshipEntityId;
        private Map<String, String> params;
        private Map<String, String> headers;

        public abstract T getHttpMethod() throws IOException;

        protected void setRequestHeaderIfAny(T methodBase)
        {
            if (headers != null)
            {
                for (Entry<String, String> headerNameValue : headers.entrySet())
                {
                    methodBase.addRequestHeader(headerNameValue.getKey(), headerNameValue.getValue());
                }
            }
        }

        public RequestContext getRequestContext()
        {
            return requestContext;
        }

        public RequestBuilder setRequestContext(RequestContext requestContext)
        {
            this.requestContext = requestContext;
            return this;
        }

        public String getScope()
        {
            return scope;
        }

        public RequestBuilder setScope(String scope)
        {
            this.scope = scope;
            return this;
        }

        public String getApiName()
        {
            return apiName;
        }

        public RequestBuilder setApiName(String apiName)
        {
            this.apiName = apiName;
            return this;
        }

        public int getVersion()
        {
            return version;
        }

        public RequestBuilder setVersion(int version)
        {
            this.version = version;
            return this;
        }

        public String getEntityCollectionName()
        {
            return entityCollectionName;
        }

        public RequestBuilder setEntityCollectionName(String entityCollectionName)
        {
            this.entityCollectionName = entityCollectionName;
            return this;
        }

        public Object getEntityId()
        {
            return entityId;
        }

        public RequestBuilder setEntityId(Object entityId)
        {
            this.entityId = entityId;
            return this;
        }

        public String getRelationCollectionName()
        {
            return relationCollectionName;
        }

        public RequestBuilder setRelationCollectionName(String relationCollectionName)
        {
            this.relationCollectionName = relationCollectionName;
            return this;
        }

        public Object getRelationshipEntityId()
        {
            return relationshipEntityId;
        }

        public RequestBuilder setRelationshipEntityId(Object relationshipEntityId)
        {
            this.relationshipEntityId = relationshipEntityId;
            return this;
        }

        public Map<String, String> getParams()
        {
            return params;
        }

        public RequestBuilder setParams(Map<String, String> params)
        {
            this.params = params;
            return this;
        }

        public Map<String, String> getHeaders()
        {
            return headers;
        }

        public RequestBuilder setHeaders(Map<String, String> headers)
        {
            this.headers = headers;
            return this;
        }
    }

    public class GetRequestBuilder extends RequestBuilder<GetMethod>
    {
        private String password;

        public String getPassword()
        {
            return password;
        }

        public GetRequestBuilder setPassword(String password)
        {
            this.password = password;
            return this;
        }

        @Override
        public GetMethod getHttpMethod() throws IOException
        {
            RestApiEndpoint endpoint = new RestApiEndpoint(getRequestContext().getNetworkId(),
                        getScope(), getApiName(), getVersion(), getEntityCollectionName(),
                        getEntityId(), getRelationCollectionName(), getRelationshipEntityId(), getParams());
            String url = endpoint.getUrl();

            GetMethod req = new GetMethod(url);
            setRequestHeaderIfAny(req);
            return req;
        }
    }

    public class DeleteRequestBuilder extends RequestBuilder<DeleteMethod>
    {
        @Override
        public DeleteMethod getHttpMethod() throws IOException
        {
            RestApiEndpoint endpoint = new RestApiEndpoint(getRequestContext().getNetworkId(),
                        getScope(), getApiName(), getVersion(), getEntityCollectionName(),
                        getEntityId(), getRelationCollectionName(), getRelationshipEntityId(), getParams());
            String url = endpoint.getUrl();

            DeleteMethod req = new DeleteMethod(url);
            setRequestHeaderIfAny(req);
            return req;
        }
    }

    public abstract class PostPutRequestBuilder<T extends HttpMethodBase> extends RequestBuilder<T>
    {
        private String bodyAsString;
        private byte[] bodyAsByteArray;
        private String contentType;

        public String getBodyAsString()
        {
            return bodyAsString;
        }

        public PostPutRequestBuilder setBodyAsString(String bodyAsString)
        {
            this.bodyAsString = bodyAsString;
            return this;
        }

        public byte[] getBodyAsByteArray()
        {
            return bodyAsByteArray;
        }

        public PostPutRequestBuilder setBodyAsByteArray(byte[] bodyAsByteArray)
        {
            this.bodyAsByteArray = bodyAsByteArray;
            return this;
        }

        public String getContentType()
        {
            return contentType;
        }

        public PostPutRequestBuilder setContentType(String contentType)
        {
            this.contentType = contentType;
            return this;
        }
    }

    public class PostRequestBuilder extends PostPutRequestBuilder<PostMethod>
    {

        @Override
        public PostMethod getHttpMethod() throws IOException
        {
            RestApiEndpoint endpoint = new RestApiEndpoint(getRequestContext().getNetworkId(),
                        getScope(), getApiName(), getVersion(), getEntityCollectionName(),
                        getEntityId(), getRelationCollectionName(), getRelationshipEntityId(), getParams());
            String url = endpoint.getUrl();

            PostMethod req = new PostMethod(url.toString());
            String contentType = getContentType();
            if (getBodyAsString() != null)
            {

                if (contentType == null || contentType.isEmpty())
                {
                    contentType = "application/json";
                }
                StringRequestEntity requestEntity = new StringRequestEntity(getBodyAsString(), contentType, "UTF-8");
                req.setRequestEntity(requestEntity);
            }
            else if (getBodyAsByteArray() != null)
            {
                if (contentType == null || contentType.isEmpty())
                {
                    contentType = "application/octet-stream";
                }
                ByteArrayRequestEntity requestEntity = new ByteArrayRequestEntity(getBodyAsByteArray(), contentType);
                req.setRequestEntity(requestEntity);
            }
            setRequestHeaderIfAny(req);
            return req;
        }
    }

    public class PutRequestBuilder extends PostPutRequestBuilder<PutMethod>
    {
        private BinaryPayload binaryPayload;

        public BinaryPayload getBinaryPayload()
        {
            return binaryPayload;
        }

        public PutRequestBuilder setBinaryPayload(BinaryPayload binaryPayload)
        {
            this.binaryPayload = binaryPayload;
            return this;
        }

        @Override
        public PutMethod getHttpMethod() throws IOException
        {
            RestApiEndpoint endpoint = new RestApiEndpoint(getRequestContext().getNetworkId(),
                        getScope(), getApiName(), getVersion(), getEntityCollectionName(),
                        getEntityId(), getRelationCollectionName(), getRelationshipEntityId(), getParams());
            String url = endpoint.getUrl();

            PutMethod req = new PutMethod(url);
            if (getBodyAsString() != null)
            {
                StringRequestEntity requestEntity = new StringRequestEntity(getBodyAsString(), "application/json", "UTF-8");
                req.setRequestEntity(requestEntity);
            }
            else if (binaryPayload != null)
            {
                BinaryRequestEntity requestEntity = new BinaryRequestEntity(binaryPayload.getFile(), binaryPayload.getMimeType(),
                            binaryPayload.getCharset());
                req.setRequestEntity(requestEntity);
            }
            setRequestHeaderIfAny(req);
            return req;
        }
    }
}
