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
package org.alfresco.service.cmr.view;

import java.io.InputStream;
import java.util.Collection;

import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.namespace.QName;


/**
 * Contract for an exporter.  An exporter is responsible for actually exporting
 * the content of the Repository to a destination point e.g. file system. 
 * 
 * @author David Caruana
 */
public interface Exporter
{
    /**
     * Start of Export
     */
    public void start(ExporterContext context);

    /**
     * Start export of namespace
     * 
     * @param prefix  namespace prefix
     * @param uri  namespace uri
     */
    public void startNamespace(String prefix, String uri);
    
    /**
     * End export of namespace
     * 
     * @param prefix  namespace prefix
     */
    public void endNamespace(String prefix);
    
    /**
     * Start export of node
     * 
     * @param nodeRef  the node reference
     */
    public void startNode(NodeRef nodeRef);
    
    /**
     * End export of node
     * 
     * @param nodeRef  the node reference
     */
    public void endNode(NodeRef nodeRef);
    
    /**
     * Start export of aspects
     * 
     * @param nodeRef
     */
    public void startAspects(NodeRef nodeRef);
    
    /**
     * Start export of aspect
     * 
     * @param nodeRef  the node reference
     * @param aspect  the aspect
     */
    public void startAspect(NodeRef nodeRef, QName aspect);
    
    /**
     * End export of aspect
     * 
     * @param nodeRef  the node reference
     * @param aspect  the aspect
     */
    public void endAspect(NodeRef nodeRef, QName aspect);
    
    /**
     * End export of aspects
     * 
     * @param nodeRef
     */
    public void endAspects(NodeRef nodeRef);

    
    public void startACL(NodeRef nodeRef);

    public void permission(NodeRef nodeRef, AccessPermission permission);
    
    public void endACL(NodeRef nodeRef);
    
    
    
    /**
     * Start export of properties
     * 
     * @param nodeRef  the node reference
     */
    public void startProperties(NodeRef nodeRef);
    
    /**
     * Start export of property
     * 
     * @param nodeRef  the node reference
     * @param property  the property name
     */
    public void startProperty(NodeRef nodeRef, QName property);
    
    /**
     * End export of property
     * 
     * @param nodeRef  the node reference
     * @param property  the property name
     */
    public void endProperty(NodeRef nodeRef, QName property);
    
    /**
     * End export of properties
     * 
     * @param nodeRef  the node reference
     */
    public void endProperties(NodeRef nodeRef);
    
    /**
     * Export single valued property
     * 
     * @param nodeRef  the node reference
     * @param property  the property name
     * @param value  the value
     */
    public void value(NodeRef nodeRef, QName property, Object value);

    /**
     * Export multi valued property
     * 
     * @param nodeRef  the node reference
     * @param property  the property name
     * @param value  the value
     */
    public void value(NodeRef nodeRef, QName property, Collection values);
    
    /**
     * Export content stream
     * 
     * @param nodeRef  the node reference
     * @param property  the property name
     * @param content  the content stream
     * @param contentData  content descriptor
     */
    public void content(NodeRef nodeRef, QName property, InputStream content, ContentData contentData);
    
    /**
     * Start export of associations
     * 
     * @param nodeRef
     */
    public void startAssocs(NodeRef nodeRef);
    
    /**
     * Start export of association
     * 
     * @param nodeRef  the node reference
     * @param assoc  the association name
     */
    public void startAssoc(NodeRef nodeRef, QName assoc);
    
    /**
     * End export of association
     * 
     * @param nodeRef  the node reference
     * @param assoc  the association name
     */
    public void endAssoc(NodeRef nodeRef, QName assoc);

    /**
     * End export of associations
     * 
     * @param nodeRef
     */
    public void endAssocs(NodeRef nodeRef);
    
    /**
     * Export warning
     * 
     * @param warning  the warning message
     */
    public void warning(String warning);
    
    /**
     * End export
     */
    public void end();

}
