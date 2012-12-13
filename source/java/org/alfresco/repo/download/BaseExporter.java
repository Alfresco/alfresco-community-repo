/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.repo.download;

import java.io.InputStream;
import java.util.Locale;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.view.Exporter;
import org.alfresco.service.cmr.view.ExporterContext;
import org.alfresco.service.namespace.QName;

/**
 * Base {@link Exporter} providing a default implementation of all methods.
 *
 * @author Alex Miller
 */
abstract class BaseExporter implements Exporter
{
    private CheckOutCheckInService checkOutCheckInService;
    protected NodeService nodeService;
    
    BaseExporter(CheckOutCheckInService checkOutCheckInService, NodeService nodeService)
    {
        this.checkOutCheckInService = checkOutCheckInService;
        this.nodeService = nodeService;
    }
    
    @Override
    public void start(ExporterContext context)
    {
    }

    @Override
    public void startNamespace(String prefix, String uri)
    {
    }

    @Override
    public void endNamespace(String prefix)
    {
    }

    @Override
    public void startNode(NodeRef nodeRef)
    {
    }

    @Override
    public void endNode(NodeRef nodeRef)
    {
    }

    @Override
    public void startReference(NodeRef nodeRef, QName childName)
    {
    }

    @Override
    public void endReference(NodeRef nodeRef)
    {
    }

    @Override
    public void startAspects(NodeRef nodeRef)
    {
    }

    @Override
    public void startAspect(NodeRef nodeRef, QName aspect)
    {
    }

    @Override
    public void endAspect(NodeRef nodeRef, QName aspect)
    {
    }

    @Override
    public void endAspects(NodeRef nodeRef)
    {
    }

    @Override
    public void startACL(NodeRef nodeRef)
    {
    }

    @Override
    public void permission(NodeRef nodeRef, AccessPermission permission)
    {
    }

    @Override
    public void endACL(NodeRef nodeRef)
    {
    }

    @Override
    public void startProperties(NodeRef nodeRef)
    {
    }

    @Override
    public void startProperty(NodeRef nodeRef, QName property)
    {
    }

    @Override
    public void endProperty(NodeRef nodeRef, QName property)
    {
    }

    @Override
    public void endProperties(NodeRef nodeRef)
    {
    }

    @Override
    public void startValueCollection(NodeRef nodeRef, QName property)
    {
    }

    @Override
    public void startValueMLText(NodeRef nodeRef, Locale locale, boolean isNull)
    {
    }

    @Override
    public void endValueMLText(NodeRef nodeRef)
    {
    }

    @Override
    public void value(NodeRef nodeRef, QName property, Object value, int index)
    {
    }

    @Override
    public void content(NodeRef nodeRef, QName property, InputStream content, ContentData contentData, int index)
    {
        if (checkOutCheckInService.isCheckedOut(nodeRef) == true)
        {
            String owner = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_LOCK_OWNER);
            if (AuthenticationUtil.getRunAsUser().equals(owner) == true)
            {
                return;
            }
        }
        
        if (checkOutCheckInService.isWorkingCopy(nodeRef) == true)
        {
            String owner = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_WORKING_COPY_OWNER);
            if (AuthenticationUtil.getRunAsUser().equals(owner) == false)
            {
                return;
            }
        }
        
        contentImpl(nodeRef, property, content, contentData, index);
    }

    /**
     * Template method for actually dealing with the content.
     * 
     * Called by the content method, after filtering for working copies.
     * 
     */
    protected abstract void contentImpl(NodeRef nodeRef, QName property, InputStream content, ContentData contentData, int index);

    @Override
    public void endValueCollection(NodeRef nodeRef, QName property)
    {
    }

    @Override
    public void startAssocs(NodeRef nodeRef)
    {
    }

    @Override
    public void startAssoc(NodeRef nodeRef, QName assoc)
    {
    }

    @Override
    public void endAssoc(NodeRef nodeRef, QName assoc)
    {
    }

    @Override
    public void endAssocs(NodeRef nodeRef)
    {
    }

    @Override
    public void warning(String warning)
    {
    }

    @Override
    public void end()
    {
    }

}
