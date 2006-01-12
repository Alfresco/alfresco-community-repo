/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.exporter;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
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
     * @see org.alfresco.service.cmr.view.Exporter#value(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName, java.io.Serializable)
     */
    public void value(NodeRef nodeRef, QName property, Object value)
    {
        for (Exporter exporter : exporters)
        {
            exporter.value(nodeRef, property, value);
        }
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#value(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName, java.util.Collection)
     */
    public void value(NodeRef nodeRef, QName property, Collection values)
    {
        for (Exporter exporter : exporters)
        {
            exporter.value(nodeRef, property, values);
        }
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#content(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName, java.io.InputStream)
     */
    public void content(NodeRef nodeRef, QName property, InputStream content, ContentData contentData)
    {
        for (Exporter exporter : exporters)
        {
            exporter.content(nodeRef, property, content, contentData);
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