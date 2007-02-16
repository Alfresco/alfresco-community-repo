/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.admin.patch.impl;

import java.io.InputStream;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.ConfigurationChecker;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.admin.PatchException;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

/**
 * Ensures that the required content snippet is added to the system descriptor
 * to enable robust checking of the content store by the configuration checker.
 * 
 * @author Derek Hulley
 */
public class SystemDescriptorContentPatch extends AbstractPatch
{
    private static final String MSG_SUCCESS = "patch.systemDescriptorContent.result";
    private static final String ERR_NO_VERSION_PROPERTIES = "patch.systemDescriptorContent.err.no_version_properties";
    private static final String ERR_NO_SYSTEM_DESCRIPTOR = "patch.systemDescriptorContent.err.no_descriptor";
    
    private ConfigurationChecker configurationChecker;
    private ContentService contentService;
    
    public SystemDescriptorContentPatch()
    {
    }
    
    public void setConfigurationChecker(ConfigurationChecker configurationChecker)
    {
        this.configurationChecker = configurationChecker;
    }

    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    @Override
    protected void checkProperties()
    {
        super.checkProperties();
        checkPropertyNotNull(configurationChecker, "configurationChecker");
        checkPropertyNotNull(contentService, "contentService");
    }

    @Override
    protected String applyInternal() throws Exception
    {
        InputStream is = null;
        try
        {
            // get the version.properties
            ResourceLoader resourceLoader = new DefaultResourceLoader();
            Resource resource = resourceLoader.getResource("classpath:alfresco/version.properties");
            if (!resource.exists())
            {
                throw new PatchException(ERR_NO_VERSION_PROPERTIES);
            }
            is = resource.getInputStream();
            // get the system descriptor
            NodeRef descriptorNodeRef = configurationChecker.getSystemDescriptor();
            if (descriptorNodeRef == null)
            {
                throw new PatchException(ERR_NO_SYSTEM_DESCRIPTOR);
            }
            // get the writer
            ContentWriter writer = contentService.getWriter(descriptorNodeRef, ContentModel.PROP_SYS_VERSION_PROPERTIES, true);
            // upload
            writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
            writer.setEncoding("UTF8");
            writer.putContent(is);
            // done
            String msg = I18NUtil.getMessage(MSG_SUCCESS);
            return msg;
        }
        finally
        {
            if (is != null)
            {
                try { is.close(); } catch (Throwable e) {}
            }
        }
    }
}




























