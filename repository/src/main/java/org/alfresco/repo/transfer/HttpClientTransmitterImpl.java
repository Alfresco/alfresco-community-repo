/*
 * #%L
 * Alfresco Repository
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

package org.alfresco.repo.transfer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.transfer.TransferException;
import org.alfresco.service.cmr.transfer.TransferProgress;
import org.alfresco.service.cmr.transfer.TransferTarget;
import org.alfresco.service.cmr.transfer.TransferVersion;
import org.alfresco.util.HttpClientHelper;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.ProxyHost;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.protocol.DefaultProtocolSocketFactory;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.httpclient.protocol.SSLProtocolSocketFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * HTTP implementation of TransferTransmitter.
 *
 * Sends data via HTTP to the server.
 *
 * @author brian
 */
public class HttpClientTransmitterImpl implements TransferTransmitter
{
    private static final Log log = LogFactory.getLog(HttpClientTransmitterImpl.class);

    private static final String MSG_UNSUPPORTED_PROTOCOL = "transfer_service.comms.unsupported_protocol";
    private static final String MSG_UNSUCCESSFUL_RESPONSE = "transfer_service.comms.unsuccessful_response";
    private static final String MSG_HTTP_REQUEST_FAILED = "transfer_service.comms.http_request_failed";

    private static final int DEFAULT_HTTP_PORT = 80;
    private static final int DEFAULT_HTTPS_PORT = 443;
    private static final String HTTP_SCHEME_NAME = "http";    // lowercase is important
    private static final String HTTPS_SCHEME_NAME = "https";  // lowercase is important

    private HttpClient httpClient = null;
    private Protocol httpProtocol = new Protocol(HTTP_SCHEME_NAME, new DefaultProtocolSocketFactory(), DEFAULT_HTTP_PORT);
    private Protocol httpsProtocol = new Protocol(HTTPS_SCHEME_NAME, (ProtocolSocketFactory) new SSLProtocolSocketFactory(), DEFAULT_HTTPS_PORT);
    private Map<String,Protocol> protocolMap = null;
    private HttpMethodFactory httpMethodFactory = null;

    private ContentService contentService;

    private NodeService nodeService;
    private boolean isAuthenticationPreemptive = false;

    private ProxyHost httpProxyHost;
    private ProxyHost httpsProxyHost;
    private Credentials httpProxyCredentials;
    private Credentials httpsProxyCredentials;
    private AuthScope httpAuthScope;
    private AuthScope httpsAuthScope;

    public HttpClientTransmitterImpl()
    {
        protocolMap = new TreeMap<String,Protocol>();
        protocolMap.put(HTTP_SCHEME_NAME, httpProtocol);
        protocolMap.put(HTTPS_SCHEME_NAME, httpsProtocol);

        httpClient = new HttpClient();
        httpClient.setHttpConnectionManager(new MultiThreadedHttpConnectionManager());
        httpMethodFactory = new StandardHttpMethodFactoryImpl();

        // Create an HTTP Proxy Host if appropriate system properties are set
        httpProxyHost = HttpClientHelper.createProxyHost("http.proxyHost", "http.proxyPort", DEFAULT_HTTP_PORT);
        httpProxyCredentials = HttpClientHelper.createProxyCredentials("http.proxyUser", "http.proxyPassword");
        httpAuthScope = createProxyAuthScope(httpProxyHost);

        // Create an HTTPS Proxy Host if appropriate system properties are set
        httpsProxyHost = HttpClientHelper.createProxyHost("https.proxyHost", "https.proxyPort", DEFAULT_HTTPS_PORT);
        httpsProxyCredentials = HttpClientHelper.createProxyCredentials("https.proxyUser", "https.proxyPassword");
        httpsAuthScope = createProxyAuthScope(httpsProxyHost);
    }

    public void init()
    {
        PropertyCheck.mandatory(this, "contentService", contentService);
        httpClient.getParams().setAuthenticationPreemptive(isAuthenticationPreemptive);
    }
    
    /**
     * By default this class uses the standard SSLProtocolSocketFactory, but this method allows this to be overridden.
     * Useful if, for example, one wishes to permit support of self-signed certificates on the target.
     * @param socketFactory ProtocolSocketFactory
     */
    public void setHttpsSocketFactory(ProtocolSocketFactory socketFactory)
    {
        protocolMap.put(HTTPS_SCHEME_NAME, new Protocol(HTTPS_SCHEME_NAME, socketFactory, DEFAULT_HTTPS_PORT));
    }

    /**
     * By default, this class uses a plain HttpClient instance with the only non-default
     * option being the multi-threaded connection manager.
     * Use this method to replace this with your own HttpClient instance configured how you wish
     * @param httpClient HttpClient
     */
    public void setHttpClient(HttpClient httpClient)
    {
        this.httpClient = httpClient;
    }

    /**
     * Whether httpClient will use preemptive authentication or not.
     * @param isAuthenticationPreemptive boolean
     */
    public void setIsAuthenticationPreemptive(boolean isAuthenticationPreemptive)
    {
        this.isAuthenticationPreemptive = isAuthenticationPreemptive;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.transfer.Transmitter#verifyTarget(org.alfresco.service.cmr.transfer.TransferTarget)
     */
    public void verifyTarget(TransferTarget target) throws TransferException
    {
        HttpMethod verifyRequest = getPostMethod();
        try
        {
            HostConfiguration hostConfig = getHostConfig(target);
            HttpState httpState = getHttpState(target);

            verifyRequest.setPath(target.getEndpointPath() + "/test");
            try
            {
                int response = httpClient.executeMethod(hostConfig, verifyRequest, httpState);
                checkResponseStatus("verifyTarget", response, verifyRequest);
            }
            catch (RuntimeException e)
            {
                throw e;
            }
            catch (Exception e)
            {
                String error = "Failed to execute HTTP request to target";
                log.debug(error, e);
                throw new TransferException(MSG_HTTP_REQUEST_FAILED, new Object[]{"verifyTraget", target.toString(), e.toString()}, e);
            }
        }
        finally
        {
            verifyRequest.releaseConnection();
        }
    }

    /**
     *
     * @param methodName String
     * @param response int
     * @param method HttpMethod
     */
    private void checkResponseStatus(String methodName, int response, HttpMethod method)
    {
        if (response != 200)
        {
            Throwable error = null;
            try
            {
                log.error("Received \"unsuccessful\" response code from target server: " + response);
                String errorPayload = method.getResponseBodyAsString();
                JSONObject errorObj = new JSONObject(errorPayload);
                error = rehydrateError(errorObj);
            }
            catch (Exception ex)
            {
                throw new TransferException(MSG_UNSUCCESSFUL_RESPONSE, new Object[] {methodName, response});
            }
            if ((error != null) && TransferException.class.isAssignableFrom(error.getClass()))
            {
                throw (TransferException)error;
            }
            else
            {
                throw new TransferException(MSG_UNSUCCESSFUL_RESPONSE, new Object[] {methodName, response});
            }
        }
    }

    /**
     * Get the HTTPState for a transfer target
     * @param target TransferTarget
     * @return HttpState
     */
    private HttpState getHttpState(TransferTarget target)
    {
        HttpState httpState = new HttpState();
        httpState.setCredentials(new AuthScope(target.getEndpointHost(), target.getEndpointPort(),
                                 AuthScope.ANY_REALM),
                                 new UsernamePasswordCredentials(target.getUsername(), new String(target.getPassword())));

        String requiredProtocol = target.getEndpointProtocol();
        if (requiredProtocol == null)
        {
            throw new TransferException(MSG_UNSUPPORTED_PROTOCOL, new Object[] {requiredProtocol});
        }

        Protocol protocol = protocolMap.get(requiredProtocol.toLowerCase().trim());
        if (protocol == null) 
        {
            log.error("Unsupported protocol: " + requiredProtocol);
            throw new TransferException(MSG_UNSUPPORTED_PROTOCOL, new Object[] {requiredProtocol});
        }

        // Use the appropriate Proxy credentials if required
        if (httpProxyHost != null && HTTP_SCHEME_NAME.equals(protocol.getScheme()) && HttpClientHelper.requiresProxy(target.getEndpointHost()))
        {
            if (httpProxyCredentials != null)
            {
                httpState.setProxyCredentials(httpAuthScope, httpProxyCredentials);

                if (log.isDebugEnabled())
                {
                    log.debug("Using HTTP proxy credentials for proxy: " + httpProxyHost.getHostName());
                }
            }
        }
        else if (httpsProxyHost != null && HTTPS_SCHEME_NAME.equals(protocol.getScheme()) && HttpClientHelper.requiresProxy(target.getEndpointHost()))
        {
            if (httpsProxyCredentials != null)
            {
                httpState.setProxyCredentials(httpsAuthScope, httpsProxyCredentials);

                if (log.isDebugEnabled())
                {
                    log.debug("Using HTTPS proxy credentials for proxy: " + httpsProxyHost.getHostName());
                }
            }
        } 

        return httpState;
    }

    /**
     * @param target TransferTarget
     * @return HostConfiguration
     */
    private HostConfiguration getHostConfig(TransferTarget target)
    {
        String requiredProtocol = target.getEndpointProtocol();
        if (requiredProtocol == null)
        {
            throw new TransferException(MSG_UNSUPPORTED_PROTOCOL, new Object[] {requiredProtocol});
        }

        Protocol protocol = protocolMap.get(requiredProtocol.toLowerCase().trim());
        if (protocol == null) 
        {
            log.error("Unsupported protocol: " + target.getEndpointProtocol());
            throw new TransferException(MSG_UNSUPPORTED_PROTOCOL, new Object[] {requiredProtocol});
        }

        HostConfiguration hostConfig = new HostConfiguration();
        hostConfig.setHost(target.getEndpointHost(), target.getEndpointPort(), protocol);
        
        // Use the appropriate Proxy Host if required
        if (httpProxyHost != null && HTTP_SCHEME_NAME.equals(protocol.getScheme()) && HttpClientHelper.requiresProxy(target.getEndpointHost()))
        {
            hostConfig.setProxyHost(httpProxyHost);

            if (log.isDebugEnabled())
            {
                log.debug("Using HTTP proxy host for: " + target.getEndpointHost());
            }
        }
        else if (httpsProxyHost != null && HTTPS_SCHEME_NAME.equals(protocol.getScheme()) && HttpClientHelper.requiresProxy(target.getEndpointHost()))
        {
            hostConfig.setProxyHost(httpsProxyHost);

            if (log.isDebugEnabled())
            {
                log.debug("Using HTTPS proxy host for: " + target.getEndpointHost());
            }
        } 
        return hostConfig;
    }
    
    /**
     * Create suitable AuthScope for ProxyHost. If the ProxyHost is null, no AuthsScope will be created.
     * @param proxyHost ProxyHost
     * @return AuthScope for provided ProxyHost, null otherwise.
     */
    private AuthScope createProxyAuthScope(final ProxyHost proxyHost)
    {
        AuthScope authScope = null;
        if (proxyHost !=  null) 
        {
            authScope = new AuthScope(proxyHost.getHostName(), proxyHost.getPort());
        }
        return authScope;
    }

    public Transfer begin(TransferTarget target, String fromRepositoryId, TransferVersion fromVersion) throws TransferException
    {
        PostMethod beginRequest = getPostMethod();
        try
        {
            HostConfiguration hostConfig = getHostConfig(target);
            HttpState httpState = getHttpState(target);

            beginRequest.setPath(target.getEndpointPath() + "/begin");
            try
            {
                NameValuePair[] nameValuePair = new NameValuePair[] {
                        new NameValuePair(TransferCommons.PARAM_FROM_REPOSITORYID, fromRepositoryId),
                        new NameValuePair(TransferCommons.PARAM_ALLOW_TRANSFER_TO_SELF, "false"),
                        new NameValuePair(TransferCommons.PARAM_VERSION_EDITION, fromVersion.getEdition()),
                        new NameValuePair(TransferCommons.PARAM_VERSION_MAJOR, fromVersion.getVersionMajor()),
                        new NameValuePair(TransferCommons.PARAM_VERSION_MINOR, fromVersion.getVersionMinor()),
                        new NameValuePair(TransferCommons.PARAM_VERSION_REVISION, fromVersion.getVersionRevision())
                };

                //add the parameter defining the root of the transfer on the file system if exist
                NodeRef transferRootNode = this.getFileTransferRootNodeRef(target.getNodeRef());
                if (transferRootNode != null)
                {
                    //add the parameter
                    ArrayList<NameValuePair> nameValuePairArrayList= new ArrayList<NameValuePair>(nameValuePair.length + 1);
                    Collections.addAll(nameValuePairArrayList,nameValuePair);
                    nameValuePairArrayList.add(new NameValuePair(TransferCommons.PARAM_ROOT_FILE_TRANSFER,  transferRootNode.toString()));
                    nameValuePair = nameValuePairArrayList.toArray(new NameValuePair[0]);
                }

                beginRequest.setRequestBody(nameValuePair);

                int responseStatus = httpClient.executeMethod(hostConfig, beginRequest, httpState);

                checkResponseStatus("begin", responseStatus, beginRequest);
                //If we get here then we've received a 200 response
                //We're expecting the transfer id encoded in a JSON object...
                JSONObject response = new JSONObject(beginRequest.getResponseBodyAsString());

                Transfer transfer = new Transfer();
                transfer.setTransferTarget(target);

                String transferId = response.getString(TransferCommons.PARAM_TRANSFER_ID);
                transfer.setTransferId(transferId);

                if(response.has(TransferCommons.PARAM_VERSION_MAJOR))
                {
                    String versionMajor = response.getString(TransferCommons.PARAM_VERSION_MAJOR);
                    String versionMinor = response.getString(TransferCommons.PARAM_VERSION_MINOR);
                    String versionRevision = response.getString(TransferCommons.PARAM_VERSION_REVISION);
                    String edition = response.getString(TransferCommons.PARAM_VERSION_EDITION);
                    TransferVersion version = new TransferVersionImpl(versionMajor, versionMinor, versionRevision, edition);
                    transfer.setToVersion(version);
                }
                else
                {
                    TransferVersion version = new TransferVersionImpl("0", "0", "0", "Unknown");
                    transfer.setToVersion(version);
                }

                if(log.isDebugEnabled())
                {
                    log.debug("begin transfer transferId:" + transferId +", target:" + target);
                }

                return transfer;
            }
            catch (RuntimeException e)
            {
                log.debug("unexpected exception", e);
                throw e;
            }
            catch (Exception e)
            {
                String error = "Failed to execute HTTP request to target";
                log.debug(error, e);
                throw new TransferException(MSG_HTTP_REQUEST_FAILED, new Object[] {"begin", target.toString(), e.toString()}, e);
            }
        }
        finally
        {
            log.debug("releasing connection");
            beginRequest.releaseConnection();
        }
    }

    public void sendManifest(Transfer transfer, File manifest, OutputStream result) throws TransferException
    {
        TransferTarget target = transfer.getTransferTarget();
        PostMethod postSnapshotRequest = getPostMethod();
        MultipartRequestEntity requestEntity;

        if(log.isDebugEnabled())
        {
            log.debug("does manifest exist? " + manifest.exists());
            log.debug("sendManifest file : " + manifest.getAbsoluteFile());
        }


        try
        {
            HostConfiguration hostConfig = getHostConfig(target);
            HttpState httpState = getHttpState(target);

            try
            {
                postSnapshotRequest.setPath(target.getEndpointPath() + "/post-snapshot");

                //Put the transferId on the query string
                postSnapshotRequest.setQueryString(
                        new NameValuePair[] {new NameValuePair("transferId", transfer.getTransferId())});

                //TODO encapsulate the name of the manifest part
                //And add the manifest file as a "part"
                Part file = new FilePart(TransferCommons.PART_NAME_MANIFEST, manifest);
                requestEntity = new MultipartRequestEntity(new Part[] {file}, postSnapshotRequest.getParams());
                postSnapshotRequest.setRequestEntity(requestEntity);

                int responseStatus = httpClient.executeMethod(hostConfig, postSnapshotRequest, httpState);
                checkResponseStatus("sendManifest", responseStatus, postSnapshotRequest);

                InputStream is = postSnapshotRequest.getResponseBodyAsStream();

                final ReadableByteChannel inputChannel = Channels.newChannel(is);
                final WritableByteChannel outputChannel = Channels.newChannel(result);
                try
                {
                    // copy the channels
                    channelCopy(inputChannel, outputChannel);
                }
                finally
                {
                    inputChannel.close();
                    outputChannel.close();
                }

                return;
            }
            catch (RuntimeException e)
            {
                throw e;
            }
            catch (Exception e)
            {
                String error = "Failed to execute HTTP request to target";
                log.debug(error, e);
                throw new TransferException(MSG_HTTP_REQUEST_FAILED, new Object[]{"sendManifest", target.toString(), e.toString()}, e);
            }
        }
        finally
        {
            postSnapshotRequest.releaseConnection();
        }
    }

    public void abort(Transfer transfer) throws TransferException
    {
        TransferTarget target = transfer.getTransferTarget();
        HttpMethod abortRequest = getPostMethod();
        try
        {
            HostConfiguration hostConfig = getHostConfig(target);
            HttpState httpState = getHttpState(target);

            abortRequest.setPath(target.getEndpointPath() + "/abort");
            //Put the transferId on the query string
            abortRequest.setQueryString(
                    new NameValuePair[] {new NameValuePair("transferId", transfer.getTransferId())});

            try
            {
                int responseStatus = httpClient.executeMethod(hostConfig, abortRequest, httpState);
                checkResponseStatus("abort", responseStatus, abortRequest);
                //If we get here then we've received a 200 response
                //We're expecting the transfer id encoded in a JSON object...
            }
            catch (RuntimeException e)
            {
                throw e;
            }
            catch (Exception e)
            {
                String error = "Failed to execute HTTP request to target";
                log.debug(error, e);
                throw new TransferException(MSG_HTTP_REQUEST_FAILED, new Object[]{"abort", target.toString(), e.toString()}, e);
            }
        }
        finally
        {
            abortRequest.releaseConnection();
        }
    }

    public void commit(Transfer transfer) throws TransferException
    {
        TransferTarget target = transfer.getTransferTarget();
        HttpMethod commitRequest = getPostMethod();
        try
        {
            HostConfiguration hostConfig = getHostConfig(target);
            HttpState httpState = getHttpState(target);

            commitRequest.setPath(target.getEndpointPath() + "/commit");
            //Put the transferId on the query string
            commitRequest.setQueryString(
                    new NameValuePair[] {new NameValuePair("transferId", transfer.getTransferId())});
            try
            {
                int responseStatus = httpClient.executeMethod(hostConfig, commitRequest, httpState);
                checkResponseStatus("commit", responseStatus, commitRequest);
                //If we get here then we've received a 200 response
                //We're expecting the transfer id encoded in a JSON object...
            }
            catch (RuntimeException e)
            {
                throw e;
            }
            catch (Exception e)
            {
                String error = "Failed to execute HTTP request to target";
                log.error(error, e);
                throw new TransferException(MSG_HTTP_REQUEST_FAILED, new Object[]{"commit", target.toString(), e.toString()}, e);
            }
        }
        finally
        {
            commitRequest.releaseConnection();
        }
    }

    public void prepare(Transfer transfer) throws TransferException
    {
        TransferTarget target = transfer.getTransferTarget();
        HttpMethod prepareRequest = getPostMethod();
        try
        {
            HostConfiguration hostConfig = getHostConfig(target);
            HttpState httpState = getHttpState(target);

            prepareRequest.setPath(target.getEndpointPath() + "/prepare");
            //Put the transferId on the query string
            prepareRequest.setQueryString(
                    new NameValuePair[] {new NameValuePair("transferId", transfer.getTransferId())});
            try
            {
                int responseStatus = httpClient.executeMethod(hostConfig, prepareRequest, httpState);
                checkResponseStatus("prepare", responseStatus, prepareRequest);
                //If we get here then we've received a 200 response
                //We're expecting the transfer id encoded in a JSON object...
            }
            catch (RuntimeException e)
            {
                throw e;
            }
            catch (Exception e)
            {
                String error = "Failed to execute HTTP request to target";
                log.debug(error, e);
                throw new TransferException(MSG_HTTP_REQUEST_FAILED, new Object[]{"prepare", target.toString(), e.toString()}, e);
            }
        }
        finally
        {
            prepareRequest.releaseConnection();
        }
    }

    /**
     *
     */
    public void sendContent(Transfer transfer, Set<ContentData> data) throws TransferException
    {
        if(log.isDebugEnabled())
        {
            log.debug("send content to transfer:" + transfer);
        }

        TransferTarget target = transfer.getTransferTarget();
        PostMethod postContentRequest = getPostMethod();

        try
        {
            HostConfiguration hostConfig = getHostConfig(target);
            HttpState httpState = getHttpState(target);

            try
            {
                postContentRequest.setPath(target.getEndpointPath() + "/post-content");
                //Put the transferId on the query string
                postContentRequest.setQueryString(
                        new NameValuePair[] {new NameValuePair("transferId", transfer.getTransferId())});

                //Put the transferId on the query string
                postContentRequest.setQueryString(
                            new NameValuePair[] {new NameValuePair("transferId", transfer.getTransferId())});

                Part[] parts = new Part[data.size()];

                int index = 0;
                for(ContentData content : data)
                {
                    String contentUrl = content.getContentUrl();
                    String fileName = TransferCommons.URLToPartName(contentUrl);
                    log.debug("content partName: " + fileName);

                    parts[index++] = new ContentDataPart(getContentService(), fileName, content);
                }

                MultipartRequestEntity requestEntity = new MultipartRequestEntity(parts, postContentRequest.getParams());
                postContentRequest.setRequestEntity(requestEntity);

                int responseStatus = httpClient.executeMethod(hostConfig, postContentRequest, httpState);
                checkResponseStatus("sendContent", responseStatus, postContentRequest);

                if(log.isDebugEnabled())
                {
                    log.debug("sent content");
                }

            }
            catch (RuntimeException e)
            {
                throw e;
            }
            catch (Exception e)
            {
                String error = "Failed to execute HTTP request to target";
                log.debug(error, e);
                throw new TransferException(MSG_HTTP_REQUEST_FAILED, new Object[]{"sendContent", target.toString(), e.toString()}, e);
            }
        }
        finally
        {
            postContentRequest.releaseConnection();
        }
    } // end of sendContent

    /**
     *
     */
    public TransferProgress getStatus(Transfer transfer) throws TransferException
    {
        TransferTarget target = transfer.getTransferTarget();
        HttpMethod statusRequest = getPostMethod();
        try
        {
            HostConfiguration hostConfig = getHostConfig(target);
            HttpState httpState = getHttpState(target);

            statusRequest.setPath(target.getEndpointPath() + "/status");
            //Put the transferId on the query string
            statusRequest.setQueryString(
                    new NameValuePair[] {new NameValuePair("transferId", transfer.getTransferId())});

            try
            {
                int responseStatus = httpClient.executeMethod(hostConfig, statusRequest, httpState);
                checkResponseStatus("status", responseStatus, statusRequest);
                //If we get here then we've received a 200 response
                String statusPayload = statusRequest.getResponseBodyAsString();
                JSONObject statusObj = new JSONObject(statusPayload);
                //We're expecting the transfer progress encoded in a JSON object...
                int currentPosition  = statusObj.getInt("currentPosition");
                int endPosition  = statusObj.getInt("endPosition");
                String statusStr= statusObj.getString("status");

                TransferProgress p = new TransferProgress();

                if(statusObj.has("error"))
                {
                    JSONObject errorJSON = statusObj.getJSONObject("error");
                    Throwable throwable = rehydrateError(errorJSON);
                    p.setError(throwable);
                }

                p.setStatus(TransferProgress.Status.valueOf(statusStr));
                p.setCurrentPosition(currentPosition);
                p.setEndPosition(endPosition);

                return p;
            }
            catch (RuntimeException e)
            {
                throw e;
            }
            catch (Exception e)
            {
                String error = "Failed to execute HTTP request to target";
                log.debug(error, e);
                throw new TransferException(MSG_HTTP_REQUEST_FAILED, new Object[]{"status", target.toString(), e.toString()}, e);
            }
        }
        finally
        {
            statusRequest.releaseConnection();
        }
    }

    /**
     *
     */
    public void getTransferReport(Transfer transfer, OutputStream result)
    {
        TransferTarget target = transfer.getTransferTarget();
        PostMethod getReportRequest = getPostMethod();
        try
        {
            HostConfiguration hostConfig = getHostConfig(target);
            HttpState httpState = getHttpState(target);

            try
            {
                getReportRequest.setPath(target.getEndpointPath() + "/report");

                //Put the transferId on the query string
                getReportRequest.setQueryString(
                        new NameValuePair[] {new NameValuePair("transferId", transfer.getTransferId())});

                int responseStatus = httpClient.executeMethod(hostConfig, getReportRequest, httpState);
                checkResponseStatus("getReport", responseStatus, getReportRequest);

                InputStream is = getReportRequest.getResponseBodyAsStream();

                // Now copy the response input stream to result.
                final ReadableByteChannel inputChannel = Channels.newChannel(is);
                final WritableByteChannel outputChannel = Channels.newChannel(result);
                try
                {
                    // copy the channels
                    channelCopy(inputChannel, outputChannel);
                }
                finally
                {
                    // closing the channels
                    inputChannel.close();
                    outputChannel.close();
                }

                return;
            }
            catch (RuntimeException e)
            {
                throw e;
            }
            catch (Exception e)
            {
                String error = "Failed to execute HTTP request to target";
                log.debug(error, e);
                throw new TransferException(MSG_HTTP_REQUEST_FAILED, new Object[]{"getTransferReport", target.toString(), e.toString()}, e);
            }
        }
        finally
        {
            getReportRequest.releaseConnection();
        }
    }

    private static void channelCopy(final ReadableByteChannel src, final WritableByteChannel dest) throws IOException
    {
        final ByteBuffer buffer = ByteBuffer.allocateDirect(2 * 1024);
        while (src.read(buffer) != -1)
        {
            // prepare the buffer to be drained
            ((Buffer) buffer).flip();
            // write to the channel, may block
             dest.write(buffer);

            // If partial transfer, shift remainder down
            // If buffer is empty, same as doing clear()
            buffer.compact();
        }

        // EOF will leave buffer in fill state
        ((Buffer) buffer).flip();

        // make sure the buffer is fully drained.
        while (buffer.hasRemaining())
        {
            dest.write(buffer);
        }
    }

    protected PostMethod getPostMethod()
    {
        return httpMethodFactory.createPostMethod();
    }

    /**
     *
     * @param errorJSON A JSON object expected to hold the name of the error class ("errorType"),
     * the error message ("errorMessage"), and, optionally, the Alfresco message id ("alfrescoErrorId")
     * and Alfresco message parameters ("alfrescoErrorParams").
     * @return The rehydrated error object, or null if errorJSON is null.
     * Throws {@code JSONException} if an error occurs while parsing the supplied JSON object
     */
    private Throwable rehydrateError(JSONObject errorJSON)
    {
        if (errorJSON == null)
        {
            return null;
        }

        String errorMessage = errorJSON.optString("errorMessage", StringUtils.EMPTY);
        String errorId = errorJSON.optString("alfrescoMessageId", null);

        Object[] errorParams = new Object[0];
        JSONArray errorParamArray = errorJSON.optJSONArray("alfrescoMessageParams");
        if (errorParamArray != null)
        {
            int length = errorParamArray.length();
            errorParams = new Object[length];
            for (int i = 0; i < length; ++i)
            {
                errorParams[i] = errorParamArray.getString(i);
            }
        }

        return new TransferException(errorId == null ? errorMessage : errorId, errorParams);
    }

    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    public ContentService getContentService()
    {
        return contentService;
    }

    public void setHttpMethodFactory(HttpMethodFactory httpMethodFactory)
    {
        this.httpMethodFactory = httpMethodFactory;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    private NodeRef getFileTransferRootNodeRef(NodeRef transferNodeRef)
    {
        //testing if transferring to file system
        if(!TransferModel.TYPE_FILE_TRANSFER_TARGET.equals(nodeService.getType(transferNodeRef)))
            return null;

        //get association
        List<AssociationRef> assocs = nodeService.getTargetAssocs(transferNodeRef, TransferModel.ASSOC_ROOT_FILE_TRANSFER);
        if(assocs.size() == 0 || assocs.size() > 1)
            return null;

        return assocs.get(0).getTargetRef();
    }


}
