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
package org.alfresco.jcr.api;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.util.ParameterCheck;


/**
 * Helper to retrieve an Alfresco Node Reference from a JCR Node
 * 
 * @author David Caruana
 */
public class JCRNodeRef
{

    /**
     * Gets the Node Reference for the specified Node
     * 
     * @param node  JCR Node
     * @return  Alfresco Node Reference
     * @throws RepositoryException
     */
    public static NodeRef getNodeRef(Node node)
        throws RepositoryException
    {
        ParameterCheck.mandatory("Node", node);

        Property protocol = node.getProperty(NamespaceService.SYSTEM_MODEL_PREFIX + ":" + ContentModel.PROP_STORE_PROTOCOL.getLocalName()); 
        Property identifier = node.getProperty(NamespaceService.SYSTEM_MODEL_PREFIX + ":" + ContentModel.PROP_STORE_IDENTIFIER.getLocalName()); 
        Property uuid = node.getProperty(NamespaceService.SYSTEM_MODEL_PREFIX + ":" + ContentModel.PROP_NODE_UUID.getLocalName());
        
        return new NodeRef(new StoreRef(protocol.getString(), identifier.getString()), uuid.getString());
    }
    
    
}
