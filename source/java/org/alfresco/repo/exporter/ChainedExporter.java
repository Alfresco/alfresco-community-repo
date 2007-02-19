/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
package org.alfresco.repo.exporter;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.view.Exporter;
import org.alfresco.service.cmr.view.ExporterContext;
import org.alfresco.service.namespace.QName;


/**
 * Exporter that wraps one or more other exporters and invokes them in the provided order.
 * 
 * @author David Caruana
 */
/*package*/ class ChainedExporter
    implements Exporter
{
    private Exporter[] exporters;

    
    /**
     * Construct
     * 
     * @param exporters  array of exporters to invoke
     */
    /*package*/ ChainedExporter(Exporter[] exporters)
    {
        List<Exporter> exporterList = new ArrayList<Exporter>();
        for (Exporter exporter : exporters)
        {
            if (exporter != null)
            {
                exporterList.add(exporter);
            }
        }
        this.exporters = exporterList.toArray(new Exporter[exporterList.size()]);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#start()
     */
    public void start(ExporterContext context)
    {
        for (Exporter exporter : exporters)
        {
            exporter.start(context);
        }
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#startNamespace(java.lang.String, java.lang.String)
     */
    public void startNamespace(String prefix, String uri)
    {
        for (Exporter exporter : exporters)
        {
            exporter.startNamespace(prefix, uri);
        }
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#endNamespace(java.lang.String)
     */
    public void endNamespace(String prefix)
    {
        for (Exporter exporter : exporters)
        {
            exporter.endNamespace(prefix);
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#startNode(org.alfresco.service.cmr.repository.NodeRef)
     */
    public void startNode(NodeRef nodeRef)
    {
        for (Exporter exporter : exporters)
        {
            exporter.startNode(nodeRef);
        }
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#endNode(org.alfresco.service.cmr.repository.NodeRef)
     */
    public void endNode(NodeRef nodeRef)
    {
        for (Exporter exporter : exporters)
        {
            exporter.endNode(nodeRef);
        }
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#startAspects(org.alfresco.service.cmr.repository.NodeRef)
     */
    public void startAspects(NodeRef nodeRef)
    {
        for (Exporter exporter : exporters)
        {
            exporter.startAspects(nodeRef);
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#endAspects(org.alfresco.service.cmr.repository.NodeRef)
     */
    public void endAspects(NodeRef nodeRef)
    {
        for (Exporter exporter : exporters)
        {
            exporter.endAspects(nodeRef);
        }
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#startAspect(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     */
    public void startAspect(NodeRef nodeRef, QName aspect)
    {
        for (Exporter exporter : exporters)
        {
            exporter.startAspect(nodeRef, aspect);
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#endAspect(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     */
    public void endAspect(NodeRef nodeRef, QName aspect)
    {
        for (Exporter exporter : exporters)
        {
            exporter.endAspect(nodeRef, aspect);
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#startACL(org.alfresco.service.cmr.repository.NodeRef)
     */
    public void startACL(NodeRef nodeRef)
    {
        for (Exporter exporter : exporters)
        {
            exporter.startACL(nodeRef);
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#permission(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.security.AccessPermission)
     */
    public void permission(NodeRef nodeRef, AccessPermission permission)
    {
        for (Exporter exporter : exporters)
        {
            exporter.permission(nodeRef, permission);
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#endACL(org.alfresco.service.cmr.repository.NodeRef)
     */
    public void endACL(NodeRef nodeRef)
    {
        for (Exporter exporter : exporters)
        {
            exporter.endACL(nodeRef);
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#startProperties(org.alfresco.service.cmr.repository.NodeRef)
     */
    public void startProperties(NodeRef nodeRef)
    {
        for (Exporter exporter : exporters)
        {
            exporter.startProperties(nodeRef);
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#endProperties(org.alfresco.service.cmr.repository.NodeRef)
     */
    public void endProperties(NodeRef nodeRef)
    {
        for (Exporter exporter : exporters)
        {
            exporter.endProperties(nodeRef);
        }
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#startProperty(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     */
    public void startProperty(NodeRef nodeRef, QName property)
    {
        for (Exporter exporter : exporters)
        {
            exporter.startProperty(nodeRef, property);
        }
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#endProperty(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     */
    public void endProperty(NodeRef nodeRef, QName property)
    {
        for (Exporter exporter : exporters)
        {
            exporter.endProperty(nodeRef, property);
        }
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#startValueCollection(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     */
    public void startValueCollection(NodeRef nodeRef, QName property)
    {
        for (Exporter exporter : exporters)
        {
            exporter.startValueCollection(nodeRef, property);
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#endValueCollection(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     */
    public void endValueCollection(NodeRef nodeRef, QName property)
    {
        for (Exporter exporter : exporters)
        {
            exporter.endValueCollection(nodeRef, property);
        }
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#value(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName, java.io.Serializable)
     */
    public void value(NodeRef nodeRef, QName property, Object value, int index)
    {
        for (Exporter exporter : exporters)
        {
            exporter.value(nodeRef, property, value, index);
        }
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#content(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName, java.io.InputStream)
     */
    public void content(NodeRef nodeRef, QName property, InputStream content, ContentData contentData, int index)
    {
        for (Exporter exporter : exporters)
        {
            exporter.content(nodeRef, property, content, contentData, index);
        }
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#startAssoc(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     */
    public void startAssoc(NodeRef nodeRef, QName assoc)
    {
        for (Exporter exporter : exporters)
        {
            exporter.startAssoc(nodeRef, assoc);
        }
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#endAssoc(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     */
    public void endAssoc(NodeRef nodeRef, QName assoc)
    {
        for (Exporter exporter : exporters)
        {
            exporter.endAssoc(nodeRef, assoc);
        }
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#startAssocs(org.alfresco.service.cmr.repository.NodeRef)
     */
    public void startAssocs(NodeRef nodeRef)
    {
        for (Exporter exporter : exporters)
        {
            exporter.startAssocs(nodeRef);
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#endAssocs(org.alfresco.service.cmr.repository.NodeRef)
     */
    public void endAssocs(NodeRef nodeRef)
    {
        for (Exporter exporter : exporters)
        {
            exporter.endAssocs(nodeRef);
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#startReference(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     */
    public void startReference(NodeRef nodeRef, QName childName)
    {
        for (Exporter exporter : exporters)
        {
            exporter.startReference(nodeRef, childName);
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#endReference(org.alfresco.service.cmr.repository.NodeRef)
     */
    public void endReference(NodeRef nodeRef)
    {
        for (Exporter exporter : exporters)
        {
            exporter.endReference(nodeRef);
        }
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#warning(java.lang.String)
     */
    public void warning(String warning)
    {
        for (Exporter exporter : exporters)
        {
            exporter.warning(warning);
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#end()
     */
    public void end()
    {
        for (Exporter exporter : exporters)
        {
            exporter.end();
        }
    }

}