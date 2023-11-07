/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
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
package org.alfresco.transform.registry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.httpclient.HttpClient4Factory;
import org.alfresco.httpclient.HttpClientConfig;
import org.alfresco.repo.content.transform.LocalPassThroughTransform;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.transform.config.TransformConfig;
import org.alfresco.transform.config.Transformer;
import org.alfresco.util.ConfigFileFinder;
import org.apache.commons.logging.Log;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.List;

import static org.alfresco.transform.common.RequestParamMap.ENDPOINT_TRANSFORM_CONFIG_LATEST;

/**
 * This class reads multiple T-Engine config and local files and registers as if they were all
 * in one file. Transform options are shared between all sources.<p>
 *
 * The caller should make calls to {@link #addRemoteConfig(List, String)}, {@link #addLocalConfig(String)} or
 * {@link CombinedTransformConfig#addTransformConfig(TransformConfig, String, String, AbstractTransformRegistry)}
 * followed by a call to {@link #register(TransformServiceRegistryImpl)}.
 *
 * @author adavis
 */
public class CombinedConfig extends CombinedTransformConfig
{
    private final Log log;

    private ObjectMapper jsonObjectMapper = new ObjectMapper();
    private ConfigFileFinder configFileFinder;
    private int tEngineCount;

    private final HttpClientConfig httpClientConfig;

    public CombinedConfig(Log log, AbstractTransformRegistry registry, HttpClientConfig httpClientConfig)
    {
        this.httpClientConfig = httpClientConfig;
        this.log = log;

        configFileFinder = new ConfigFileFinder(jsonObjectMapper)
        {
            @Override
            protected void readJson(JsonNode jsonNode, String readFrom, String baseUrl)
            {
                TransformConfig transformConfig = jsonObjectMapper.convertValue(jsonNode, TransformConfig.class);
                addTransformConfig(transformConfig, readFrom, baseUrl, registry);
            }
        };
    }

    public boolean addLocalConfig(String path)
    {
        return configFileFinder.readFiles(path, log);
    }

    public boolean addRemoteConfig(List<String> urls, String remoteType) throws IOException
    {
        try(CloseableHttpClient httpclient = HttpClient4Factory.createHttpClient(httpClientConfig))
        {
            boolean successReadingConfig = true;
            for (String url : urls)
            {
                if (addRemoteConfig(httpclient, url, remoteType))
                {
                    tEngineCount++;
                } else
                {
                    successReadingConfig = false;
                }
            }
            return successReadingConfig;
        }
    }

    private boolean addRemoteConfig(CloseableHttpClient httpclient, String baseUrl, String remoteType)
    {
        String url = baseUrl + (baseUrl.endsWith("/") ? "" : "/") + ENDPOINT_TRANSFORM_CONFIG_LATEST;
        HttpGet httpGet = new HttpGet(url);
        boolean successReadingConfig = true;
        try
        {
            try (CloseableHttpResponse response = execute(httpclient, httpGet))
            {
                HttpEntity resEntity = response.getEntity();
                if (resEntity != null)
                {
                    int statusCode = response.getCode();
                    if (statusCode == 200)
                    {
                        try
                        {
                            String content = getContent(resEntity);
                            try (StringReader reader = new StringReader(content))
                            {
                                int transformCount = transformerCount();
                                configFileFinder.readFile(reader, remoteType+" on "+baseUrl, "json", baseUrl, log);
                                if (transformCount == transformerCount())
                                {
                                    successReadingConfig = false;
                                }
                            }

                            EntityUtils.consume(resEntity);
                        }
                        catch (IOException | ParseException e)
                        {
                            throw new AlfrescoRuntimeException("Failed to read the returned content from "+
                                    remoteType+" on " + url, e);
                        }
                    }
                    else
                    {
                        String message = getErrorMessage(resEntity);
                        throw new AlfrescoRuntimeException(remoteType+" on " + url+" returned a " + statusCode +
                                " status " + message);
                    }
                }
                else
                {
                    throw new AlfrescoRuntimeException(remoteType+" on " + url+" did not return an entity " + url);
                }
            }
            catch (IOException | ParseException e)
            {
                throw new AlfrescoRuntimeException("Failed to connect or to read the response from "+remoteType+
                        " on " + url, e);
            }

        }
        catch (AlfrescoRuntimeException e)
        {
            log.error(e.getMessage());
            successReadingConfig = false;
        }
        return successReadingConfig;
    }

    // Tests mock the return values
    CloseableHttpResponse execute(CloseableHttpClient httpclient, HttpGet httpGet) throws IOException
    {
        return httpclient.execute(httpGet);
    }

    // Tests mock the return values
    String getContent(HttpEntity resEntity) throws IOException, ParseException
    {
        return EntityUtils.toString(resEntity);
    }

    // Strip out just the error message in the response
    private String getErrorMessage(HttpEntity resEntity) throws IOException, ParseException
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

    @Override
    protected boolean isPassThroughTransformName(String name)
    {
        return name.equals(LocalPassThroughTransform.NAME);
    }

    /**
     * Adds a PassThrough transform where the source and target mimetypes are identical, or transforms to "text/plain"
     * from selected text based types.
     * @param mimetypeService to find all the mimetypes
     */
    public void addPassThroughTransformer(MimetypeService mimetypeService, AbstractTransformRegistry registry)
    {
        List<String> mimetypes = mimetypeService.getMimetypes();
        Transformer transformer = LocalPassThroughTransform.getConfig(mimetypes);
        TransformConfig transformConfig = TransformConfig.builder()
                .withTransformers(Collections.singletonList(transformer))
                .build();
        addTransformConfig(transformConfig, "based on mimetype list", null, registry);
    }

    public void register(TransformServiceRegistryImpl registry)
    {
        TransformServiceRegistryImpl.Data data = registry.getData();
        data.setTEngineCount(tEngineCount);
        data.setFileCount(configFileFinder.getFileCount());

        combineTransformerConfig(registry);
        registerCombinedTransformers(registry);
    }
}
