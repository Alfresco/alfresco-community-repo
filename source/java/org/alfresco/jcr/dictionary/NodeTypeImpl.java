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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.jcr.dictionary;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.PropertyDefinition;

import org.alfresco.jcr.item.ValueImpl;
import org.alfresco.jcr.item.property.JCRMixinTypesProperty;
import org.alfresco.jcr.item.property.JCRPrimaryTypeProperty;
import org.alfresco.service.cmr.dictionary.ChildAssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.QName;

/**
 * Alfresco implementation of a Node Type Definition
 * 
 * @author David Caruana
 */
public class NodeTypeImpl implements NodeType
{
    // The required nt:base type specified by JCR
    public static QName NT_BASE = QName.createQName(JCRNamespace.NT_URI, "base");

    // The optional mix:referenceable specified by JCR
    public static QName MIX_REFERENCEABLE = QName.createQName(JCRNamespace.MIX_URI, "referenceable");
    // The optional mix:lockable specified by JCR
    public static QName MIX_LOCKABLE = QName.createQName(JCRNamespace.MIX_URI, "lockable");
    // The optional mix:versionable specified by JCR
    public static QName MIX_VERSIONABLE = QName.createQName(JCRNamespace.MIX_URI, "versionable");

    
    private NodeTypeManagerImpl typeManager;
    private ClassDefinition classDefinition;

    
    /**
     * Construct
     * 
     * @param classDefinition  Alfresco class definition
     */    
    public NodeTypeImpl(NodeTypeManagerImpl typeManager, ClassDefinition classDefinition)
    {
        this.typeManager = typeManager;
        this.classDefinition = classDefinition;
    }

    /* (non-Javadoc)
     * @see javax.jcr.nodetype.NodeType#getName()
     */
    public String getName()
    {
        return classDefinition.getName().toPrefixString(typeManager.getNamespaceService());
    }

    /* (non-Javadoc)
     * @see javax.jcr.nodetype.NodeType#isMixin()
     */
    public boolean isMixin()
    {
        return classDefinition.isAspect();
    }

    /* (non-Javadoc)
     * @see javax.jcr.nodetype.NodeType#hasOrderableChildNodes()
     */
    public boolean hasOrderableChildNodes()
    {
        // Note: For now, we don't expose this through JCR
        return false;
    }

    /* (non-Javadoc)
     * @see javax.jcr.nodetype.NodeType#getPrimaryItemName()
     */
    public String getPrimaryItemName()
    {
        // NOTE: Alfresco does not support the notion of PrimaryItem (not yet anyway)
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jcr.nodetype.NodeType#getSupertypes()
     */
    public NodeType[] getSupertypes()
    {
        List<NodeType> nodeTypes = new ArrayList<NodeType>();
        NodeType[] declaredSupertypes = getDeclaredSupertypes();
        while (declaredSupertypes.length > 0)
        {
            // Alfresco supports single inheritence only
            NodeType supertype = declaredSupertypes[0];
            nodeTypes.add(supertype);
            declaredSupertypes = supertype.getDeclaredSupertypes();
        }
        return nodeTypes.toArray(new NodeType[nodeTypes.size()]);
    }

    /* (non-Javadoc)
     * @see javax.jcr.nodetype.NodeType#getDeclaredSupertypes()
     */
    public NodeType[] getDeclaredSupertypes()
    {
        // return no supertype when type is nt:base
        if (classDefinition.getName().equals(NT_BASE))
        {
            return new NodeType[] {};
        }
        
        // return root type when no parent (nt:base if a type hierarchy)
        QName parent = classDefinition.getParentName();
        if (parent == null)
        {
            if (classDefinition.isAspect())
            {
                return new NodeType[] {};
            }
            else
            {
                return new NodeType[] { typeManager.getNodeTypeImpl(NT_BASE) };
            }
        }
        
        // return the supertype
        return new NodeType[] { typeManager.getNodeTypeImpl(parent) };
    }

    /* (non-Javadoc)
     * @see javax.jcr.nodetype.NodeType#isNodeType(java.lang.String)
     */
    public boolean isNodeType(String nodeTypeName)
    {
        QName name = QName.createQName(nodeTypeName, typeManager.getNamespaceService());
        
        // is it one of standard types
        if (name.equals(NodeTypeImpl.NT_BASE))
        {
            return true;
        }

        // is it part of this class hierarchy
        return typeManager.getSession().getRepositoryImpl().getServiceRegistry().getDictionaryService().isSubClass(name, classDefinition.getName());
    }

    /* (non-Javadoc)
     * @see javax.jcr.nodetype.NodeType#getPropertyDefinitions()
     */
    public PropertyDefinition[] getPropertyDefinitions()
    {
        Map<QName, org.alfresco.service.cmr.dictionary.PropertyDefinition> propDefs = classDefinition.getProperties();
        PropertyDefinition[] defs = new PropertyDefinition[propDefs.size() + (classDefinition.isAspect() ? 0 : 2)];
        int i = 0;
        for (org.alfresco.service.cmr.dictionary.PropertyDefinition propDef : propDefs.values())
        {
            defs[i++] = new PropertyDefinitionImpl(typeManager, propDef);
        }
        
        if (!classDefinition.isAspect())
        {
            // add nt:base properties
            defs[i++] = typeManager.getPropertyDefinitionImpl(JCRPrimaryTypeProperty.PROPERTY_NAME);
            defs[i++] = typeManager.getPropertyDefinitionImpl(JCRMixinTypesProperty.PROPERTY_NAME);
        }
        
        return defs;
    }

    /* (non-Javadoc)
     * @see javax.jcr.nodetype.NodeType#getDeclaredPropertyDefinitions()
     */
    public PropertyDefinition[] getDeclaredPropertyDefinitions()
    {
        Map<QName, org.alfresco.service.cmr.dictionary.PropertyDefinition> propDefs = classDefinition.getProperties();
        List<PropertyDefinition> defs = new ArrayList<PropertyDefinition>();
        for (org.alfresco.service.cmr.dictionary.PropertyDefinition propDef : propDefs.values())
        {
            if (propDef.getContainerClass().equals(classDefinition))
            {
                defs.add(new PropertyDefinitionImpl(typeManager, propDef));
            }
        }
        
        if (classDefinition.equals(NT_BASE))
        {
            // add nt:base properties
            defs.add(typeManager.getPropertyDefinitionImpl(JCRPrimaryTypeProperty.PROPERTY_NAME));
            defs.add(typeManager.getPropertyDefinitionImpl(JCRMixinTypesProperty.PROPERTY_NAME));
        }
        
        return defs.toArray(new PropertyDefinition[defs.size()]);
    }

    /* (non-Javadoc)
     * @see javax.jcr.nodetype.NodeType#getChildNodeDefinitions()
     */
    public NodeDefinition[] getChildNodeDefinitions()
    {
        Map<QName, ChildAssociationDefinition> assocDefs = classDefinition.getChildAssociations();
        NodeDefinition[] defs = new NodeDefinition[assocDefs.size()];
        int i = 0;
        for (ChildAssociationDefinition assocDef : assocDefs.values())
        {
            defs[i++] = new NodeDefinitionImpl(typeManager, assocDef);
        }
        return defs;
    }

    /* (non-Javadoc)
     * @see javax.jcr.nodetype.NodeType#getDeclaredChildNodeDefinitions()
     */
    public NodeDefinition[] getDeclaredChildNodeDefinitions()
    {
        Map<QName, ChildAssociationDefinition> assocDefs = classDefinition.getChildAssociations();
        List<NodeDefinition> defs = new ArrayList<NodeDefinition>();
        for (ChildAssociationDefinition assocDef : assocDefs.values())
        {
            if (assocDef.getSourceClass().equals(classDefinition))
            {
                defs.add(new NodeDefinitionImpl(typeManager, assocDef));
            }
        }
        return defs.toArray(new NodeDefinition[defs.size()]);
    }

    /* (non-Javadoc)
     * @see javax.jcr.nodetype.NodeType#canSetProperty(java.lang.String, javax.jcr.Value)
     */
    public boolean canSetProperty(String propertyName, Value value)
    {
        try
        {
            // is an attempt to remove property being made
            if (value == null)
            {
                return canRemoveItem(propertyName);
            }
            
            // retrieve property definition
            QName propertyQName = QName.createQName(propertyName, typeManager.getNamespaceService());
            Map<QName, org.alfresco.service.cmr.dictionary.PropertyDefinition> propDefs = classDefinition.getProperties();
            org.alfresco.service.cmr.dictionary.PropertyDefinition propDef = propDefs.get(propertyQName);
            if (propDef == null)
            {
                // Alfresco doesn't have residual properties yet
                return false;
            }
            
            // is property read-write
            if (propDef.isProtected() || propDef.isMultiValued())
            {
                return false;
            }
            
            // get required type to convert to
            int requiredType = DataTypeMap.convertDataTypeToPropertyType(propDef.getDataType().getName());
            if (requiredType == PropertyType.UNDEFINED)
            {
                requiredType = value.getType();
            }

            // convert value to required type
            // Note: Invalid conversion will throw exception
            ValueImpl.getValue(typeManager.getSession().getTypeConverter(), requiredType, value);
            
            // Note: conversion succeeded
            return true;
        }
        catch(RepositoryException e)
        {
            // Note: Not much can be done really            
        }
        
        return false;
    }

    /* (non-Javadoc)
     * @see javax.jcr.nodetype.NodeType#canSetProperty(java.lang.String, javax.jcr.Value[])
     */
    public boolean canSetProperty(String propertyName, Value[] values)
    {
        try
        {
            // is an attempt to remove property being made
            if (values == null)
            {
                return canRemoveItem(propertyName);
            }
            
            // retrieve property definition
            QName propertyQName = QName.createQName(propertyName, typeManager.getNamespaceService());
            Map<QName, org.alfresco.service.cmr.dictionary.PropertyDefinition> propDefs = classDefinition.getProperties();
            org.alfresco.service.cmr.dictionary.PropertyDefinition propDef = propDefs.get(propertyQName);
            if (propDef == null)
            {
                // Alfresco doesn't have residual properties yet
                return false;
            }
            
            // is property read write
            if (propDef.isProtected() || !propDef.isMultiValued())
            {
                return false;
            }

            // determine type of values to check
            int valueType = PropertyType.UNDEFINED;
            for (Value value : values)
            {
                if (value != null)
                {
                    if (valueType != PropertyType.UNDEFINED && value.getType() != valueType)
                    {
                        // do not allow collection mixed type values
                        return false;
                    }
                    valueType = value.getType();
                }
            }
            
            // get required type to convert to
            int requiredType = DataTypeMap.convertDataTypeToPropertyType(propDef.getDataType().getName());
            if (requiredType == PropertyType.UNDEFINED)
            {
                requiredType = valueType;
            }

            // convert values to required format
            // Note: Invalid conversion will throw exception
            for (Value value : values)
            {
                if (value != null)
                {
                    ValueImpl.getValue(typeManager.getSession().getTypeConverter(), requiredType, value);
                }
            }
            
            // Note: conversion succeeded
            return true;
        }
        catch(RepositoryException e)
        {
            // Note: Not much can be done really            
        }
        
        return false;
    }

    /* (non-Javadoc)
     * @see javax.jcr.nodetype.NodeType#canAddChildNode(java.lang.String)
     */
    public boolean canAddChildNode(String childNodeName)
    {
        // NOTE: Alfresco does not have default primary type notion
        return false;
    }

    /* (non-Javadoc)
     * @see javax.jcr.nodetype.NodeType#canAddChildNode(java.lang.String, java.lang.String)
     */
    public boolean canAddChildNode(String childNodeName, String nodeTypeName)
    {
        boolean canAdd = false;
        Map<QName, ChildAssociationDefinition> assocDefs = classDefinition.getChildAssociations();
        QName childNodeQName = QName.createQName(childNodeName, typeManager.getNamespaceService());
        ChildAssociationDefinition assocDef = assocDefs.get(childNodeQName);
        if (assocDef != null)
        {
            QName nodeTypeQName = QName.createQName(nodeTypeName, typeManager.getNamespaceService());
            DictionaryService dictionaryService = typeManager.getSession().getRepositoryImpl().getServiceRegistry().getDictionaryService();
            canAdd = dictionaryService.isSubClass(nodeTypeQName, assocDef.getTargetClass().getName());
        }
        return canAdd;
    }

    /* (non-Javadoc)
     * @see javax.jcr.nodetype.NodeType#canRemoveItem(java.lang.String)
     */
    public boolean canRemoveItem(String itemName)
    {
        boolean isProtected = false;
        boolean isMandatory = false;
        
        // TODO: Property and Association names can clash? What to do?
        QName itemQName = QName.createQName(itemName, typeManager.getNamespaceService());
        Map<QName, org.alfresco.service.cmr.dictionary.PropertyDefinition> propDefs = classDefinition.getProperties();
        org.alfresco.service.cmr.dictionary.PropertyDefinition propDef = propDefs.get(itemQName);
        if (propDef != null)
        {
            isProtected = propDef.isProtected();
            isMandatory = propDef.isMandatory();
        }
        Map<QName, ChildAssociationDefinition> assocDefs = classDefinition.getChildAssociations();
        ChildAssociationDefinition assocDef = assocDefs.get(itemQName);
        if (assocDef != null)
        {
            isProtected |= assocDef.isProtected();
            isMandatory |= assocDef.isTargetMandatory();
        }
        
        return !isProtected && !isMandatory;
    }
    
}
