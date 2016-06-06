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
package org.alfresco.rest.api.impl;

import org.alfresco.error.AlfrescoRuntimeException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.extensions.webscripts.ClassPathStoreResourceResolver;
import org.springframework.util.FileCopyUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

// note: based on HomeSiteSurfConfig in Cloud/Thor module
public class SiteSurfConfig implements ApplicationContextAware, InitializingBean
{
    private ApplicationContext applicationContext;
    private String configPath;
    private String packageName = "surf-config";
    private String importView;
    private Map<String, String> importContent;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        this.applicationContext = applicationContext;
    }

    public void setConfigPath(String configPath)
    {
        if (!configPath.endsWith("/"))
        {
            configPath = configPath + "/";
        }
        this.configPath = configPath;
    }

    public void setPackageName(String packageName)
    {
        this.packageName = packageName;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        if (this.configPath == null)
        {
            setConfigPath("alfresco/bootstrap/site");
        }
        importView = loadImportView();
        importContent = loadContent();
    }

    public String getImportView()
    {
        return importView;
    }

    public String getImportContent(String contentPath)
    {
        return importContent.get(contentPath);
    }

    private String loadImportView() throws IOException
    {
        ClassPathResource importViewResource = new ClassPathResource(configPath + packageName + ".xml");
        if (!importViewResource.exists())
        {
            throw new AlfrescoRuntimeException("Cannot find site config " + importViewResource.getPath());
        }
        return convert(importViewResource.getInputStream());
    }

    private Map<String, String> loadContent() throws IOException
    {
        ClassPathStoreResourceResolver resourceResolver = new ClassPathStoreResourceResolver(applicationContext);
        Resource[] contentResources = resourceResolver.getResources("classpath*:" + configPath + packageName + "/*.*");
        Map<String, String> content = new HashMap<String, String>();
        for (Resource contentResource : contentResources)
        {
            String fileName = contentResource.getFilename();
            // ignore hidden directories / files
            if (fileName.startsWith("."))
            {
                continue;
            }
            String key = packageName + "/" + fileName;
            String value = convert(contentResource.getInputStream());
            content.put(key, value);
        }
        return content;
    }

    private String convert(InputStream contentInputStream) throws IOException
    {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        FileCopyUtils.copy(contentInputStream, os);
        byte[] bytes = os.toByteArray();
        String content = new String(bytes, "UTF-8");
        return content;
    }
}
