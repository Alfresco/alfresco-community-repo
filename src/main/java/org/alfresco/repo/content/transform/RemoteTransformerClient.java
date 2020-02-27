/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2019 Alfresco Software Limited
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
package org.alfresco.repo.content.transform;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.util.Pair;
import org.alfresco.util.exec.RuntimeExec;
import org.alfresco.util.exec.RuntimeExec.ExecutionResult;
import org.apache.commons.logging.Log;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.StringJoiner;

/**
 * Client class that transfers content (from a ContentReader) to a remote transformation agent together with
 * request parameters that will be used to transform the content. The transformed content is then returned and
 * saved in a ContentWriter. In the event of an error an Exception is thrown.
 *
 * @since 6.0
 */
public class RemoteTransformerClient
{
    private final String name;
    private final String baseUrl;

    // The length of time to wait after a connection problem before checking availability again.
    private long startupRetryPeriod = 15000;

    // When to check availability.
    private long checkAvailabilityAfter = 0L;

    // The initial value indicates we have not had a success yet.
    // Only changed once on success. This is stored so it can always be returned.
    private Pair<Boolean, String> checkResult = new Pair<>(null, null);

    public RemoteTransformerClient(String name, String baseUrl)
    {
        this.name = name;
        this.baseUrl = baseUrl == null || baseUrl.trim().isEmpty() ? null : baseUrl.trim();
    }

    public void setStartupRetryPeriodSeconds(int startupRetryPeriodSeconds)
    {
        startupRetryPeriod = startupRetryPeriodSeconds*1000;
    }

    public String getBaseUrl()
    {
        return baseUrl;
    }

    public void request(ContentReader reader, ContentWriter writer, String sourceMimetype, String sourceExtension,
                        String targetExtension, long timeoutMs, Log logger, String... args)
    {
        if (args.length % 2 != 0)
        {
            throw new IllegalArgumentException("There should be a value for each request property");
        }

        StringJoiner sj = new StringJoiner(" ");
        HttpEntity reqEntity = getRequestEntity(reader, sourceMimetype, sourceExtension, targetExtension, timeoutMs, args, sj);

        request(logger, sourceExtension, targetExtension, reqEntity, writer, sj.toString());
    }

    HttpEntity getRequestEntity(ContentReader reader, String sourceMimetype, String sourceExtension,
                                        String targetExtension, long timeoutMs, String[] args, StringJoiner sj)
    {
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        ContentType contentType = ContentType.create(sourceMimetype);
        builder.addBinaryBody("file", reader.getContentInputStream(), contentType, "tmp."+sourceExtension);
        builder.addTextBody("targetExtension", targetExtension);
        sj.add("targetExtension" + '=' + targetExtension);
        for (int i=0; i< args.length; i+=2)
        {
            if (args[i+1] != null)
            {
                builder.addTextBody(args[i], args[i + 1]);

                sj.add(args[i] + '=' + args[i + 1]);
            }
        }

        if (timeoutMs > 0)
        {
            String timeoutMsString = Long.toString(timeoutMs);
            builder.addTextBody("timeout", timeoutMsString);
            sj.add("timeout=" + timeoutMsString);
        }
        return builder.build();
    }

    void request(Log logger, String sourceExtension, String targetExtension, HttpEntity reqEntity, ContentWriter writer, String args)
    {
        String url = baseUrl + (baseUrl.endsWith("/") ? "" : "/") + "transform";
        HttpPost httppost = new HttpPost(url);
        httppost.setEntity(reqEntity);

        if (logger.isDebugEnabled())
        {
            logger.debug(name+' '+sourceExtension+' '+targetExtension+' '+url+' '+args);
        }

        try
        {
            try (CloseableHttpClient httpclient = HttpClients.createDefault())
            {
                try (CloseableHttpResponse response = execute(httpclient, httppost))
                {
                    StatusLine statusLine = response.getStatusLine();
                    if (statusLine == null)
                    {
                        throw new AlfrescoRuntimeException(name+" returned no status " + url + ' ' + args);
                    }
                    HttpEntity resEntity = response.getEntity();
                    if (resEntity != null)
                    {
                        int statusCode = statusLine.getStatusCode();
                        if (statusCode == 200)
                        {
                            try
                            {
                                if (logger.isDebugEnabled())
                                {
                                    long responseContentLength = resEntity.getContentLength();
                                    Header responseContentEncoding = resEntity.getContentEncoding();
                                    Header responseContentType = resEntity.getContentType();
                                    logger.debug(name + ' ' + sourceExtension + ' ' + targetExtension +
                                            " returned. length=" + responseContentLength +
                                            " type=" + responseContentType +
                                            " encoding=" + responseContentEncoding);
                                }

                                writer.putContent(resEntity.getContent());
                                EntityUtils.consume(resEntity);
                            }
                            catch (IOException e)
                            {
                                throw new AlfrescoRuntimeException(name + " failed to read the returned content", e);
                            }
                        }
                        else
                        {
                            String message = getErrorMessage(resEntity);
                            String msg = (name + " returned a " + statusCode + " status " + message +
                                    ' ' + url + ' ' + args).trim();
                            if (statusCode == 401)
                            {
                                throw new UnsupportedTransformationException(msg);
                            }
                            else if (statusCode == 402)
                            {
                                throw new UnimportantTransformException(msg);
                            }
                            else
                            {
                                throw new AlfrescoRuntimeException(msg);
                            }
                        }
                    }
                    else
                    {
                        throw new AlfrescoRuntimeException(name + " did not return an entity " + url);
                    }
                }
                catch (IOException e)
                {
                    // In the case of transform requests, unlike version checks, it is only the failure to connect that
                    // forces a wait before trying again.
                    connectionFailed();
                    throw new AlfrescoRuntimeException(name + " failed to connect or to read the response", e);
                }
            }
            catch (IOException e)
            {
                throw new AlfrescoRuntimeException(name + " failed to create an HttpClient", e);
            }
        }
        catch (AlfrescoRuntimeException e)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug(e.getMessage());
            }
            throw e;
        }
    }

    /**
     *  Indicates if a remote transform:
     *  a) ready probe has ever indicated success {@code new Pair<>(true, <version string>)},
     *  b) a ready probe has just failed {@code new Pair<>(false, <error string>)}, or
     *  c) we are not performing a ready check as we have just done one {@code new Pair<>(null, null)}.
     */
    public Pair<Boolean, String> check(Log logger)
    {
        if (!isTimeToCheckAvailability())
        {
            logger.debug(name+' '+" too early to check availability");
            Pair<Boolean, String> result = getCheckResult();
            return result;
        }

        String url = baseUrl + "/version";
        HttpGet httpGet = new HttpGet(url);

        try
        {
            try (CloseableHttpClient httpclient = HttpClients.createDefault())
            {
                try (CloseableHttpResponse response = execute(httpclient, httpGet))
                {
                    StatusLine statusLine = response.getStatusLine();
                    if (statusLine == null)
                    {
                        throw new AlfrescoRuntimeException(name+" check returned no status " + url);
                    }
                    HttpEntity resEntity = response.getEntity();
                    if (resEntity != null)
                    {
                        int statusCode = statusLine.getStatusCode();
                        if (statusCode == 200)
                        {
                            try
                            {
                                String version = getContent(resEntity);

                                if (logger.isTraceEnabled())
                                {
                                    long responseContentLength = resEntity.getContentLength();
                                    Header responseContentType = resEntity.getContentType();
                                    Header responseContentEncoding = resEntity.getContentEncoding();
                                    logger.trace(name +
                                            " check returned. length=" + responseContentLength +
                                            " type=" + responseContentType +
                                            " encoding=" + responseContentEncoding+
                                            " content="+version);
                                }

                                EntityUtils.consume(resEntity);
                                connectionSuccess();
                                Pair<Boolean, String> success = new Pair<>(true, version);
                                setCheckResult(success);
                                return success;
                            }
                            catch (IOException e)
                            {
                                throw new AlfrescoRuntimeException(name + " check failed to read the returned content", e);
                            }
                        }
                        else
                        {
                            String message = getErrorMessage(resEntity);
                            throw new AlfrescoRuntimeException(name + " check returned a " + statusCode + " status " + message + ' ' + url);
                        }
                    }
                    else
                    {
                        throw new AlfrescoRuntimeException(name + " check did not return an entity " + url);
                    }
                }
                catch (IOException e)
                {
                    throw new AlfrescoRuntimeException(name + " check failed to connect or to read the response", e);
                }
            }
            catch (IOException e)
            {
                throw new AlfrescoRuntimeException(name + " check failed to create an HttpClient", e);
            }
        }
        catch (AlfrescoRuntimeException e)
        {
            // In the case of version checks, unlike transform requests, any failure forces a wait before trying again.
            connectionFailed();
            if (logger.isDebugEnabled())
            {
                logger.debug(e.getMessage());
            }
            // Indicates there was a check made and that it failed
            Pair<Boolean, String> failure = new Pair<>(false, e.getMessage());
            return failure;
        }
    }

    // Tests mock the return values
    CloseableHttpResponse execute(CloseableHttpClient httpclient, HttpPost httppost) throws IOException
    {
        return httpclient.execute(httppost);
    }

    // Tests mock the return values
    CloseableHttpResponse execute(CloseableHttpClient httpclient, HttpGet httpGet) throws IOException
    {
        return httpclient.execute(httpGet);
    }

    // Strip out just the error message in the response
    private String getErrorMessage(HttpEntity resEntity) throws IOException
    {
        String message = "";
        String content = getContent(resEntity);
        int i = content.indexOf("\"message\":\"");
        if (i != -1)
        {
            int j = content.indexOf("\",\"path\":", i);
            if (j != -1)
            {
                message = content.substring(i+11, j);
            }
        }
        return message;
    }

    // Tests mock the return values
    String getContent(HttpEntity resEntity) throws IOException
    {
        return EntityUtils.toString(resEntity);
    }

    /**
     * Helper method that returns the same result type as {@link #check(Log)} given a local checkCommand.
     */
    public static Pair<Boolean,String> check(RuntimeExec checkCommand)
    {
        ExecutionResult result = checkCommand.execute();
        Boolean success = result.getSuccess();
        String output = success ? result.getStdOut().trim() : result.toString();
        return new Pair<>(success, output);
    }

    synchronized void connectionFailed()
    {
        checkAvailabilityAfter = System.currentTimeMillis() + startupRetryPeriod;
    }

    synchronized void connectionSuccess()
    {
        checkAvailabilityAfter = Long.MAX_VALUE;
    }

    private synchronized boolean isTimeToCheckAvailability()
    {
        return System.currentTimeMillis() > checkAvailabilityAfter;
    }

    public synchronized boolean isAvailable()
    {
        return checkAvailabilityAfter == Long.MAX_VALUE;
    }

    private synchronized Pair<Boolean, String> getCheckResult()
    {
        return checkResult;
    }

    private synchronized void setCheckResult(Pair<Boolean, String> checkResult)
    {
        this.checkResult = checkResult;
    }
}
