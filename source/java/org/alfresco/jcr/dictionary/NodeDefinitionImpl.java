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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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
