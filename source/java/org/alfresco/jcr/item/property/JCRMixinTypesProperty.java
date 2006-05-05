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
package org.alfresco.jcr.item.property;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.jcr.RepositoryException;

import org.alfresco.jcr.dictionary.JCRNamespace;
import org.alfresco.jcr.dictionary.NodeTypeImpl;
import org.alfresco.jcr.item.NodeImpl;
import org.alfresco.jcr.item.PropertyImpl;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

/**
 * Implementation for nt:base primaryType property
 * 
 * @author David Caruana
 */
public class JCRMixinTypesProperty extends PropertyImpl
{
    public static QName PROPERTY_NAME = QName.createQName(JCRNamespace.JCR_URI, "mixinTypes");
    

    /**
     * Construct
     * 
     * @param node
     */
    public JCRMixinTypesProperty(NodeImpl node)
    {
        super(node, PROPERTY_NAME);
    }

    @Override
    protected Object getPropertyValue() throws RepositoryException
    {
        // get aspects from node
        NodeImpl nodeImpl = getNodeImpl();
        NodeService nodeService = nodeImpl.getSessionImpl().getRepositoryImpl().getServiceRegistry().getNodeService();
        Set<QName> aspects = nodeService.getAspects(nodeImpl.getNodeRef());

        // resolve against session namespace prefix resolver
        List<String> aspectNames = new ArrayList<String>(aspects.size() + 1);
        for (QName aspect : aspects)
        {
            aspectNames.add(aspect.toPrefixString(nodeImpl.getSessionImpl().getNamespaceResolver()));
        }
        
        // add JCR referenceable
        aspectNames.add(NodeTypeImpl.MIX_REFERENCEABLE.toPrefixString(nodeImpl.getSessionImpl().getNamespaceResolver()));
        
        return aspectNames; 
    }
    
}
