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
package org.alfresco.jcr.dictionary;


import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;
import javax.jcr.version.OnParentVersionAction;

import org.alfresco.service.cmr.dictionary.ChildAssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;


/**
 * Alfresco implementation of a JCR Node Definition
 * 
 * @author David Caruana
 *
 */
public class NodeDefinitionImpl implements NodeDefinition
{
    private NodeTypeManagerImpl typeManager;
    private ChildAssociationDefinition assocDef;
    
    /**
     * Construct
     * 
     * @param typeManager
     * @param assocDef
     */
    public NodeDefinitionImpl(NodeTypeManagerImpl typeManager, ChildAssociationDefinition assocDef)
    {
        this.typeManager = typeManager;
        this.assocDef = assocDef;
    }
    
    /* (non-Javadoc)
     * @see javax.jcr.nodetype.NodeDefinition#getRequiredPrimaryTypes()
     */
    public NodeType[] getRequiredPrimaryTypes()
    {
        // Note: target class is mandatory in Alfresco
        ClassDefinition target = assocDef.getTargetClass();
        return new NodeType[] { typeManager.getNodeTypeImpl(target.getName()) };
    }

    /* (non-Javadoc)
     * @see javax.jcr.nodetype.NodeDefinition#getDefaultPrimaryType()
     */
    public NodeType getDefaultPrimaryType()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jcr.nodetype.NodeDefinition#allowsSameNameSiblings()
     */
    public boolean allowsSameNameSiblings()
    {
        return assocDef.getDuplicateChildNamesAllowed();
    }

    /* (non-Javadoc)
     * @see javax.jcr.nodetype.ItemDefinition#getDeclaringNodeType()
     */
    public NodeType getDeclaringNodeType()
    {
        return typeManager.getNodeTypeImpl(assocDef.getSourceClass().getName());        
    }

    /* (non-Javadoc)
     * @see javax.jcr.nodetype.ItemDefinition#getName()
     */
    public String getName()
    {
        return assocDef.getName().toPrefixString(typeManager.getNamespaceService());
    }

    /* (non-Javadoc)
     * @see javax.jcr.nodetype.ItemDefinition#isAutoCreated()
     */
    public boolean isAutoCreated()
    {
        return isMandatory();
    }

    /* (non-Javadoc)
     * @see javax.jcr.nodetype.ItemDefinition#isMandatory()
     */
    public boolean isMandatory()
    {
        return assocDef.isTargetMandatory();
    }

    /* (non-Javadoc)
     * @see javax.jcr.nodetype.ItemDefinition#getOnParentVersion()
     */
    public int getOnParentVersion()
    {
        // TODO: Check this correct
        return OnParentVersionAction.INITIALIZE;
    }

    /* (non-Javadoc)
     * @see javax.jcr.nodetype.ItemDefinition#isProtected()
     */
    public boolean isProtected()
    {
        return assocDef.isProtected();
    }

}
