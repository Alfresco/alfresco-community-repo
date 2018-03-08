/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2018 Alfresco Software Limited
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
import org.apache.commons.io.IOUtils;
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
import java.io.InputStream;
import java.io.StringWriter;
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

    public RemoteTransformerClient(String name, String baseUrl)
    {
        this.name = name;
        this.baseUrl = baseUrl == null || baseUrl.trim().isEmpty() ? null : baseUrl.trim();
    }

    public String getBaseUrl()
    {
        return baseUrl;
    }

    public void request(ContentReader reader, ContentWriter writer, String sourceMimetype, String sourceExtension,
                        String targetExtension, long timeoutMs, Log logger, String... args) throws IllegalAccessException
    {
        if (args.length % 2 != 0)
        {
            throw new IllegalAccessException("There should be a value for each request property");
        }

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        ContentType contentType = ContentType.create(sourceMimetype);
        builder.addBinaryBody("file", reader.getContentInputStream(), contentType, "tmp."+sourceExtension);
        StringJoiner sj = new StringJoiner(" ");
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
        HttpEntity reqEntity = builder.build();

        String url = baseUrl + "/transform";
        HttpPost httppost = new HttpPost(url);
        httppost.setEntity(reqEntity);

        if (logger.isDebugEnabled())
        {
            logger.debug("Remote "+name+' '+sourceExtension+' '+targetExtension+' '+url+' '+sj.toString());
        }

        try (CloseableHttpClient httpclient = HttpClients.createDefault())
        {
            try (CloseableHttpResponse response = httpclient.execute(httppost))
            {
                StatusLine statusLine = response.getStatusLine();
                if (statusLine == null)
                {
                    throw new AlfrescoRuntimeException("Remote "+name+" returned no status " + url + ' ' + sj.toString());
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
                                logger.debug("Remote " + name + ' ' + sourceExtension + ' ' + targetExtension +
                                        " returned. length=" + responseContentLength +
                                        " type=" + responseContentType +
                                        " encoding=" + responseContentEncoding);
                            }

                            writer.putContent(resEntity.getContent());
                            EntityUtils.consume(resEntity);
                        }
                        catch (IOException e)
                        {
                            throw new AlfrescoRuntimeException("Remote " + name + " failed to read the returned content", e);
                        }
                    }
                    else
                    {
                        String message = getErrorMessage(resEntity);
                        String msg = "Remote " + name + " returned a " + statusCode + " status " + message +
                                ' ' + url + ' ' + sj.toString();
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
                    throw new AlfrescoRuntimeException("Remote " + name + " did not return an entity " + url);
                }
            }
            catch (IOException e)
            {
                throw new AlfrescoRuntimeException("Remote " + name + " failed to read the returned content", e);
            }
        }
        catch (IOException e)
        {
            throw new AlfrescoRuntimeException("Remote " + name + " failed to create an HttpClient", e);
        }
    }

    public Pair<Boolean, String> checkCommand(Log logger)
    {
        // TODO Have a long retry and give up after a few goes and return "unknown".

        String url = baseUrl + "/version";
        HttpGet httpGet = new HttpGet(url);

        if (logger.isDebugEnabled())
        {
            logger.debug("Remote "+name+' '+" check command" +url);
        }

        try (CloseableHttpClient httpclient = HttpClients.createDefault())
        {
            try (CloseableHttpResponse response = httpclient.execute(httpGet))
            {
                StatusLine statusLine = response.getStatusLine();
                if (statusLine == null)
                {
                    throw new AlfrescoRuntimeException("Remote "+name+" check command returned no status " + url);
                }
                HttpEntity resEntity = response.getEntity();
                if (resEntity != null)
                {
                    int statusCode = statusLine.getStatusCode();
                    if (statusCode == 200)
                    {
                        try
                        {
                            String version = EntityUtils.toString(resEntity);

                            if (logger.isDebugEnabled())
                            {
                                long responseContentLength = resEntity.getContentLength();
                                Header responseContentType = resEntity.getContentType();
                                Header responseContentEncoding = resEntity.getContentEncoding();
                                logger.debug("Remote " + name +
                                        " check command returned. length=" + responseContentLength +
                                        " type=" + responseContentType +
                                        " encoding=" + responseContentEncoding+
                                        " content="+version);
                            }

                            EntityUtils.consume(resEntity);
                            return new Pair<>(true, version);
                        }
                        catch (IOException e)
                        {
                            throw new AlfrescoRuntimeException("Remote " + name + " check command failed to read the returned content", e);
                        }
                    }
                    else
                    {
                        String message = getErrorMessage(resEntity);
                        throw new AlfrescoRuntimeException("Remote " + name + " check command returned a " + statusCode + " status " + message + ' ' + url);
                    }
                }
                else
                {
                    throw new AlfrescoRuntimeException("Remote " + name + " check command did not return an entity " + url);
                }
            }
            catch (IOException e)
            {
                throw new AlfrescoRuntimeException("Remote " + name + " check command failed to read the returned content", e);
            }
        }
        catch (IOException e)
        {
            throw new AlfrescoRuntimeException("Remote " + name + " check command failed to create an HttpClient", e);
        }
    }

    private String getErrorMessage(HttpEntity resEntity) throws IOException
    {
        String message = "";
        String content = EntityUtils.toString(resEntity);
        System.out.println("Response content: " + content);
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

    /**
     * Helper method that returns the same result type as {@link #checkCommand(Log)} given a local checkCommand.
     */
    public static Pair<Boolean,String> checkCommand(RuntimeExec checkCommand)
    {
        ExecutionResult result = checkCommand.execute();
        Boolean success = result.getSuccess();
        String output = success ? result.getStdOut().trim() : result.toString();
        return new Pair<>(success, output);
    }
}
