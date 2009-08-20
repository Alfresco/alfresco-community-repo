/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
 * http://www.alfresco.com/legal/licensing
 */
package org.alfresco.repo.audit.model;

import java.io.File;
import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.audit.model._3.Audit;
import org.alfresco.util.PropertyCheck;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.ResourceUtils;

/**
 * A component used to load Audit configuration XML documents.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class AuditModelReader implements InitializingBean
{
    private URL configUrl;
    private AuditModelRegistry auditModelRegistry;
    
    /**
     * Set the XML location using <b>file:</b>, <b>classpath:</b> or any of the
     * {@link ResourceUtils Spring-supported} formats.
     * 
     * @param configUrl             the location of the XML file
     */
    public void setConfigUrl(URL configUrl)
    {
        this.configUrl = configUrl;
    }

    /**
     * 
     * @param auditModelRegistry    the registry that combines all loaded models
     */
    public void setAuditModelRegistry(AuditModelRegistry auditModelRegistry)
    {
        this.auditModelRegistry = auditModelRegistry;
    }

    /**
     * Pulls in the configuration and registers it
     */
    public void afterPropertiesSet() throws Exception
    {
        PropertyCheck.mandatory(this, "configUrl", configUrl);
        PropertyCheck.mandatory(this, "auditModelRegistry", auditModelRegistry);
        
        File file = new File(configUrl.getFile());
        if (!file.exists())
        {
            throw new AlfrescoRuntimeException("The Audit configuration XML was not found: " + configUrl);
        }
        
        // Load it
        JAXBContext jaxbCtx = JAXBContext.newInstance("org.alfresco.repo.audit.model._3");
        Unmarshaller jaxbUnmarshaller = jaxbCtx.createUnmarshaller();
        try
        {
            @SuppressWarnings("unchecked")
            JAXBElement<Audit> auditElement = (JAXBElement<Audit>) jaxbUnmarshaller.unmarshal(configUrl);
            Audit audit = auditElement.getValue();
            // Now register it
            auditModelRegistry.registerModel(configUrl, audit);
        }
        catch (UnmarshalException e)
        {
            throw new AlfrescoRuntimeException(
                    "Failed to read Audit configuration XML: \n" +
                    "   URL:   " + configUrl + "\n" +
                    "   Error: " + e.getMessage());
        }
    }
}
