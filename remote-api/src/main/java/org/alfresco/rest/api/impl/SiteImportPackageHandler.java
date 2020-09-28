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

import org.alfresco.service.cmr.view.ImportPackageHandler;
import org.alfresco.service.cmr.view.ImporterException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;

// note: based on HomeSiteImportPackageHandler in Cloud/Thor module
public class SiteImportPackageHandler implements ImportPackageHandler
{
    private final static String SITEID_PLACEHOLDER = "${siteId}";

    private SiteSurfConfig config;
    private String siteId;

    public SiteImportPackageHandler(SiteSurfConfig config, String siteId)
    {
        this.config = config;
        this.siteId = siteId;
    }

    @Override
    public void startImport()
    {
    }

    @Override
    public Reader getDataStream()
    {
        return new StringReader(config.getImportView());
    }

    @Override
    public InputStream importStream(String contentPath)
    {
        String content = config.getImportContent(contentPath);
        if (content == null)
        {
            return null;
        }

        String siteContent = content.replace(SITEID_PLACEHOLDER, siteId);
        try
        {
            return new ByteArrayInputStream(siteContent.getBytes("UTF-8"));
        }
        catch(UnsupportedEncodingException e)
        {
            throw new ImporterException("Failed to read content " + contentPath, e);
        }
    }

    @Override
    public void endImport()
    {
    }
}
