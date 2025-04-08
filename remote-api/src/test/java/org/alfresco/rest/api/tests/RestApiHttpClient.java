/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.rest.api.tests;

/**
 * A http client for talking to the rest apis.
 * 
 * The caller can pass in a rest api implementation class to the http method (get, post, put, delete supported) and the url will be generated.
 * 
 * @author steveglover
 *
 */
public class RestApiHttpClient
{
    /* private static final Log logger = LogFactory.getLog(RestApiHttpClient.class);
     * 
     * private static final String INDEX_URL = "http://{0}:{1}{2}/a"; private static final String BASE_URL = "http://{0}:{1}{2}/a/{3}/{4}/alfresco/versions/1/";
     * 
     * private String host = "localhost"; private int port = 8081; private HttpClient httpClient;
     * 
     * private int maxTotalConnections = 255; private int maxHostConnections = 255;
     * 
     * private String contextPath;
     * 
     * private ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver(); private MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(this.resourcePatternResolver);
     * 
     * public RestApiHttpClient() { }
     * 
     * public RestApiHttpClient(String host, int port, String contextPath) { super(); this.host = host; this.port = port; this.contextPath = contextPath; constructHttpClient(); }
     * 
     * public void setHost(String host) { this.host = host; }
     * 
     * public void setPort(int port) { this.port = port; }
     * 
     * public void setContextPath(String contextPath) { this.contextPath = contextPath; }
     * 
     * public void init() { constructHttpClient(); }
     * 
     * private void log(String msg) { if(logger.isDebugEnabled()) { logger.debug(msg); } }
     * 
     * protected void constructHttpClient() { MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager(); this.httpClient = new HttpClient(connectionManager); HttpClientParams params = httpClient.getParams(); params.setBooleanParameter(HttpConnectionParams.TCP_NODELAY, true); params.setBooleanParameter(HttpConnectionParams.STALE_CONNECTION_CHECK, false); HttpConnectionManagerParams connectionManagerParams = httpClient.getHttpConnectionManager().getParams(); connectionManagerParams.setMaxTotalConnections(maxTotalConnections); connectionManagerParams.setDefaultMaxConnectionsPerHost(maxHostConnections);
     * 
     * httpClient.getHostConfiguration().setHost(host, port); httpClient.getParams().setAuthenticationPreemptive(true); }
     * 
     * protected AnnotationMetadata getAnnotationMetadata(String classname) throws IOException { MetadataReader metadataReader = this.metadataReaderFactory.getMetadataReader(classname); AnnotationMetadata annotationMetaData = metadataReader.getAnnotationMetadata(); return annotationMetaData; }
     * 
     * public JSONObject submitRequest(HttpMethod req, String user, String password, int expectedStatus) throws HttpException, IOException { String contentAsString = null;
     * 
     * try { httpClient.getState().setCredentials( new AuthScope(host, port, null), new UsernamePasswordCredentials(user, password) );
     * 
     * httpClient.getParams().setAuthenticationPreemptive(true);
     * 
     * String requestBody = null; if(req instanceof PostMethod) { PostMethod post = (PostMethod)req; RequestEntity entity = post.getRequestEntity(); if(entity != null) { ByteArrayOutputStream bos = new ByteArrayOutputStream(); entity.writeRequest(bos); requestBody = new String(bos.toByteArray(), "UTF-8"); } } else if(req instanceof PutMethod) { PutMethod put = (PutMethod)req; RequestEntity entity = put.getRequestEntity(); if(entity != null) { ByteArrayOutputStream bos = new ByteArrayOutputStream(); entity.writeRequest(bos); requestBody = new String(bos.toByteArray(), "UTF-8"); } }
     * 
     * long start = System.currentTimeMillis();
     * 
     * int ret = httpClient.executeMethod(req);
     * 
     * long end = System.currentTimeMillis();
     * 
     * contentAsString = req.getResponseBodyAsString();
     * 
     * log(req.getName() + " request " + req.getURI().toString()); log(requestBody != null ? " \nbody = " + requestBody + "\n" : ""); log("user " + user); log(" returned " + ret + " and took " + (end - start) + "ms"); log("Response content " + contentAsString);
     * 
     * assertEquals(expectedStatus, ret); } finally { if(req != null) { req.releaseConnection(); } }
     * 
     * JSONObject jsonRsp = (contentAsString != null ? (JSONObject)JSONValue.parse(contentAsString) : null); return jsonRsp; }
     * 
     * public JSONObject get(final Class<?> c, final String tenantDomain, final TestPerson runAsUser, final Object entityId, final Object relationshipEntityId, Map<String, String> params, final int expectedStatus) throws IOException { RestApiEndpoint endpoint = new RestApiEndpoint(c, tenantDomain, entityId, relationshipEntityId, params); String url = endpoint.getUrl();
     * 
     * GetMethod req = new GetMethod(url); return submitRequest(req, runAsUser.getUsername(), runAsUser.getPassword(), expectedStatus); }
     * 
     * public JSONObject post(final Class<?> c, final String tenantDomain, final TestPerson runAsUser, final Object entityId, final Object relationshipEntityId, final String body, final int expectedStatus) throws IOException { RestApiEndpoint endpoint = new RestApiEndpoint(c, tenantDomain, entityId, relationshipEntityId, null); String url = endpoint.getUrl();
     * 
     * PostMethod req = new PostMethod(url.toString()); if(body != null) { StringRequestEntity requestEntity = new StringRequestEntity(body, "application/json", "UTF-8"); req.setRequestEntity(requestEntity); } return submitRequest(req, runAsUser.getUsername(), runAsUser.getPassword(), expectedStatus); }
     * 
     * public JSONObject delete(final Class<?> c, final String tenantDomain, final TestPerson runAsUser, final Object entityId, final Object relationshipEntityId, Map<String, String> params, final int expectedStatus) throws IOException { RestApiEndpoint endpoint = new RestApiEndpoint(c, tenantDomain, entityId, relationshipEntityId, params); String url = endpoint.getUrl();
     * 
     * DeleteMethod req = new DeleteMethod(url); return submitRequest(req, runAsUser.getUsername(), runAsUser.getPassword(), expectedStatus); }
     * 
     * public JSONObject put(final Class<?> c, final String tenantDomain, final TestPerson runAsUser, final Object entityId, final Object relationshipEntityId, final String body, final int expectedStatus) throws IOException { RestApiEndpoint endpoint = new RestApiEndpoint(c, tenantDomain, entityId, relationshipEntityId, null); String url = endpoint.getUrl();
     * 
     * PutMethod req = new PutMethod(url); if(body != null) { StringRequestEntity requestEntity = new StringRequestEntity(body, "application/json", "UTF-8"); req.setRequestEntity(requestEntity); } return submitRequest(req, runAsUser.getUsername(), runAsUser.getPassword(), expectedStatus); }
     * 
     * 
     * Encapsulates information relating to a rest api end point, generating and encoding urls based on the rest api implementation class.
     * 
     * private class RestApiEndpoint { private Object collectionEntityId; private Object relationEntityId; private String tenantDomain; private Class<?> resourceClass; private SCOPE scope; private Map<String, String> params;
     * 
     * private String entityCollectionName; private String relationshipCollectionName;
     * 
     * RestApiEndpoint(Class<?> resourceClass, String tenantDomain, Object collectionEntityId, Object relationEntityId, Map<String, String> params) throws IOException { this.tenantDomain = tenantDomain; this.resourceClass = resourceClass; this.collectionEntityId = collectionEntityId; this.relationEntityId = relationEntityId; this.params = params;
     * 
     * if(resourceClass != null) { Api api = ResourceInspector.inspectApi(resourceClass); this.scope = api.getScope();
     * 
     * extractRelationCollectionInfo(); extractEntityCollectionInfo(); } }
     * 
     * private String encodeToString(Object o) throws UnsupportedEncodingException { String ret = null;
     * 
     * if(o instanceof NodeRef) { NodeRef nodeRef = (NodeRef)o; ret = (o != null ? nodeRef.getId() : null); } else { ret = (o != null ? o.toString() : null); }
     * 
     * return ret; }
     * 
     * private void extractRelationCollectionInfo() throws IOException { AnnotationMetadata annotationMetaData = getAnnotationMetadata(resourceClass.getCanonicalName()); if(annotationMetaData.isConcrete() && annotationMetaData.isIndependent()) { if(annotationMetaData.getAnnotationTypes().contains(RelationshipResource.class.getCanonicalName())) { Map<String, Object> attrs = annotationMetaData.getAnnotationAttributes(RelationshipResource.class.getName()); this.relationshipCollectionName = (String)attrs.get("name"); Class<?> entityResource = (Class<?>)attrs.get("entityResource");
     * 
     * extractEntityCollectionInfo(entityResource.getCanonicalName()); } } else { throw new AlfrescoRuntimeException(""); } }
     * 
     * private void extractEntityCollectionInfo() throws IOException { extractEntityCollectionInfo(resourceClass.getCanonicalName()); }
     * 
     * private void extractEntityCollectionInfo(String className) throws IOException { AnnotationMetadata annotationMetaData = getAnnotationMetadata(className); if(annotationMetaData.isConcrete() && annotationMetaData.isIndependent()) { if(annotationMetaData.getAnnotationTypes().contains(EntityResource.class.getCanonicalName())) { Map<String, Object> attrs = annotationMetaData.getAnnotationAttributes(EntityResource.class.getName()); this.entityCollectionName = (String)attrs.get("name"); } } else { throw new AlfrescoRuntimeException(""); } }
     * 
     * public String getUrl() throws UnsupportedEncodingException { StringBuilder sb = new StringBuilder();
     * 
     * if(resourceClass != null) { sb.append(MessageFormat.format(BASE_URL, new Object[] {host, String.valueOf(port), contextPath, tenantDomain == null ? TenantWebScriptServlet.DEFAULT_TENANT : tenantDomain, scope.toString()}));
     * 
     * String collectionEntityIdString = encodeToString(collectionEntityId); String relationEntityIdString = encodeToString(relationEntityId);
     * 
     * if(entityCollectionName != null) { sb.append(entityCollectionName); sb.append("/"); if(collectionEntityIdString != null) { sb.append(collectionEntityIdString); sb.append("/"); } }
     * 
     * if(relationshipCollectionName != null) { sb.append(relationshipCollectionName); sb.append("/"); if(relationEntityIdString != null) { sb.append(relationEntityIdString); sb.append("/"); } } } else { sb.append(MessageFormat.format(INDEX_URL, new Object[] {host, String.valueOf(port), contextPath})); }
     * 
     * if(params != null && params.size() > 0) { sb.append("?");
     * 
     * for(String paramName : params.keySet()) { sb.append(URLEncoder.encode(paramName, "UTF-8")); sb.append("="); sb.append(URLEncoder.encode(params.get(paramName), "UTF-8")); sb.append("&"); }
     * 
     * sb.deleteCharAt(sb.length() - 1); }
     * 
     * String url = sb.toString(); return url; } } */
}
