/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.repo.exporter;

import java.io.InputStream;
import java.util.Locale;

import org.springframework.extensions.surf.util.ParameterCheck;

import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.view.ExportPackageHandler;
import org.alfresco.service.cmr.view.Exporter;
import org.alfresco.service.cmr.view.ExporterContext;
import org.alfresco.service.namespace.QName;

/**
 * Exporter that transforms content properties to URLs.
 * 
 * All other Repository information is exported using the delegated exporter.
 * 
 * @author David Caruana
 */
/* package */ class URLExporter
        implements Exporter
{
    private Exporter exporter;
    private ExportPackageHandler streamHandler;

    /**
     * Construct
     * 
     * @param exporter
     *            exporter to delegate to
     * @param streamHandler
     *            the handler for transforming content streams to URLs
     */
    public URLExporter(Exporter exporter, ExportPackageHandler streamHandler)
    {
        ParameterCheck.mandatory("Exporter", exporter);
        ParameterCheck.mandatory("Stream Handler", streamHandler);

        this.exporter = exporter;
        this.streamHandler = streamHandler;
    }

    /* (non-Javadoc)
     * 
     * @see org.alfresco.service.cmr.view.Exporter#start() */
    public void start(ExporterContext context)
    {
        exporter.start(context);
    }

    /* (non-Javadoc)
     * 
     * @see org.alfresco.service.cmr.view.Exporter#startNamespace(java.lang.String, java.lang.String) */
    public void startNamespace(String prefix, String uri)
    {
        exporter.startNamespace(prefix, uri);
    }

    /* (non-Javadoc)
     * 
     * @see org.alfresco.service.cmr.view.Exporter#endNamespace(java.lang.String) */
    public void endNamespace(String prefix)
    {
        exporter.endNamespace(prefix);
    }

    /* (non-Javadoc)
     * 
     * @see org.alfresco.service.cmr.view.Exporter#startNode(org.alfresco.service.cmr.repository.NodeRef) */
    public void startNode(NodeRef nodeRef)
    {
        exporter.startNode(nodeRef);
    }

    /* (non-Javadoc)
     * 
     * @see org.alfresco.service.cmr.view.Exporter#endNode(org.alfresco.service.cmr.repository.NodeRef) */
    public void endNode(NodeRef nodeRef)
    {
        exporter.endNode(nodeRef);
    }

    /* (non-Javadoc)
     * 
     * @see org.alfresco.service.cmr.view.Exporter#startAspects(org.alfresco.service.cmr.repository.NodeRef) */
    public void startAspects(NodeRef nodeRef)
    {
        exporter.startAspects(nodeRef);
    }

    /* (non-Javadoc)
     * 
     * @see org.alfresco.service.cmr.view.Exporter#endAspects(org.alfresco.service.cmr.repository.NodeRef) */
    public void endAspects(NodeRef nodeRef)
    {
        exporter.endAspects(nodeRef);
    }

    /* (non-Javadoc)
     * 
     * @see org.alfresco.service.cmr.view.Exporter#startAspect(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName) */
    public void startAspect(NodeRef nodeRef, QName aspect)
    {
        exporter.startAspect(nodeRef, aspect);
    }

    /* (non-Javadoc)
     * 
     * @see org.alfresco.service.cmr.view.Exporter#endAspect(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName) */
    public void endAspect(NodeRef nodeRef, QName aspect)
    {
        exporter.endAspect(nodeRef, aspect);
    }

    /* (non-Javadoc)
     * 
     * @see org.alfresco.service.cmr.view.Exporter#startACL(org.alfresco.service.cmr.repository.NodeRef) */
    public void startACL(NodeRef nodeRef)
    {
        exporter.startACL(nodeRef);
    }

    /* (non-Javadoc)
     * 
     * @see org.alfresco.service.cmr.view.Exporter#permission(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.security.AccessPermission) */
    public void permission(NodeRef nodeRef, AccessPermission permission)
    {
        exporter.permission(nodeRef, permission);
    }

    /* (non-Javadoc)
     * 
     * @see org.alfresco.service.cmr.view.Exporter#endACL(org.alfresco.service.cmr.repository.NodeRef) */
    public void endACL(NodeRef nodeRef)
    {
        exporter.endACL(nodeRef);
    }

    /* (non-Javadoc)
     * 
     * @see org.alfresco.service.cmr.view.Exporter#startProperties(org.alfresco.service.cmr.repository.NodeRef) */
    public void startProperties(NodeRef nodeRef)
    {
        exporter.startProperties(nodeRef);
    }

    /* (non-Javadoc)
     * 
     * @see org.alfresco.service.cmr.view.Exporter#endProperties(org.alfresco.service.cmr.repository.NodeRef) */
    public void endProperties(NodeRef nodeRef)
    {
        exporter.endProperties(nodeRef);
    }

    /* (non-Javadoc)
     * 
     * @see org.alfresco.service.cmr.view.Exporter#startProperty(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName) */
    public void startProperty(NodeRef nodeRef, QName property)
    {
        exporter.startProperty(nodeRef, property);
    }

    /* (non-Javadoc)
     * 
     * @see org.alfresco.service.cmr.view.Exporter#endProperty(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName) */
    public void endProperty(NodeRef nodeRef, QName property)
    {
        exporter.endProperty(nodeRef, property);
    }

    /* (non-Javadoc)
     * 
     * @see org.alfresco.service.cmr.view.Exporter#startValueCollection(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName) */
    public void startValueCollection(NodeRef nodeRef, QName property)
    {
        exporter.startValueCollection(nodeRef, property);
    }

    /* (non-Javadoc)
     * 
     * @see org.alfresco.service.cmr.view.Exporter#endValueCollection(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName) */
    public void endValueCollection(NodeRef nodeRef, QName property)
    {
        exporter.endValueCollection(nodeRef, property);
    }

    /* (non-Javadoc)
     * 
     * @see org.alfresco.service.cmr.view.Exporter#value(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName, java.io.Serializable) */
    public void value(NodeRef nodeRef, QName property, Object value, int index)
    {
        exporter.value(nodeRef, property, value, index);
    }

    /* (non-Javadoc)
     * 
     * @see org.alfresco.service.cmr.view.Exporter#content(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName, java.io.InputStream) */
    public void content(NodeRef nodeRef, QName property, InputStream content, ContentData contentData, int index)
    {
        // Handle the stream by converting it to a URL and export the URL
        ContentData exportedContentData = streamHandler.exportContent(content, new NodeContentData(nodeRef, contentData));
        value(nodeRef, property, exportedContentData, index);
    }

    /* (non-Javadoc)
     * 
     * @see org.alfresco.service.cmr.view.Exporter#startAssoc(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName) */
    public void startAssoc(NodeRef nodeRef, QName assoc)
    {
        exporter.startAssoc(nodeRef, assoc);
    }

    /* (non-Javadoc)
     * 
     * @see org.alfresco.service.cmr.view.Exporter#endAssoc(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName) */
    public void endAssoc(NodeRef nodeRef, QName assoc)
    {
        exporter.endAssoc(nodeRef, assoc);
    }

    /* (non-Javadoc)
     * 
     * @see org.alfresco.service.cmr.view.Exporter#startAssocs(org.alfresco.service.cmr.repository.NodeRef) */
    public void startAssocs(NodeRef nodeRef)
    {
        exporter.startAssocs(nodeRef);
    }

    /* (non-Javadoc)
     * 
     * @see org.alfresco.service.cmr.view.Exporter#endAssocs(org.alfresco.service.cmr.repository.NodeRef) */
    public void endAssocs(NodeRef nodeRef)
    {
        exporter.endAssocs(nodeRef);
    }

    /* (non-Javadoc)
     * 
     * @see org.alfresco.service.cmr.view.Exporter#startReference(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName) */
    public void startReference(NodeRef nodeRef, QName childName)
    {
        exporter.startReference(nodeRef, childName);
    }

    /* (non-Javadoc)
     * 
     * @see org.alfresco.service.cmr.view.Exporter#endReference(org.alfresco.service.cmr.repository.NodeRef) */
    public void endReference(NodeRef nodeRef)
    {
        exporter.endReference(nodeRef);
    }

    public void startValueMLText(NodeRef nodeRef, Locale locale, boolean isNull)
    {
        exporter.startValueMLText(nodeRef, locale, isNull);
    }

    public void endValueMLText(NodeRef nodeRef)
    {
        exporter.endValueMLText(nodeRef);
    }

    /* (non-Javadoc)
     * 
     * @see org.alfresco.service.cmr.view.Exporter#warning(java.lang.String) */
    public void warning(String warning)
    {
        exporter.warning(warning);
    }

    /* (non-Javadoc)
     * 
     * @see org.alfresco.service.cmr.view.Exporter#end() */
    public void end()
    {
        exporter.end();
    }

}
