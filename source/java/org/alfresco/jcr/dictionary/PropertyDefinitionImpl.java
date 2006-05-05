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

import javax.jcr.Value;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.PropertyDefinition;
import javax.jcr.version.OnParentVersionAction;

import org.alfresco.jcr.item.ValueImpl;
import org.alfresco.jcr.item.property.JCRMixinTypesProperty;
import org.alfresco.jcr.item.property.JCRPrimaryTypeProperty;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;

/**
 * Alfresco implementation of a JCR Property Definition
 * 
 * @author David Caruana
 */
public class PropertyDefinitionImpl implements PropertyDefinition
{
    /** Session */
    private NodeTypeManagerImpl typeManager;
    
    /** Alfresco Property Definition */
    private org.alfresco.service.cmr.dictionary.PropertyDefinition propDef;
    
    
    /**
     * Construct
     * 
     * @param propDef  Alfresco Property Definition
     */
    public PropertyDefinitionImpl(NodeTypeManagerImpl typeManager, org.alfresco.service.cmr.dictionary.PropertyDefinition propDef)
    {
        this.typeManager = typeManager;
        this.propDef = propDef;
    }

    /* (non-Javadoc)
     * @see javax.jcr.nodetype.PropertyDefinition#getRequiredType()
     */
    public int getRequiredType()
    {
        // TODO: Switch on data type
        if (propDef.getName().equals(ContentModel.PROP_CONTENT))
        {
            return DataTypeMap.convertDataTypeToPropertyType(DataTypeDefinition.CONTENT);
        }
        return DataTypeMap.convertDataTypeToPropertyType(propDef.getDataType().getName());
    }
    
    /* (non-Javadoc)
     * @see javax.jcr.nodetype.PropertyDefinition#getValueConstraints()
     */
    public String[] getValueConstraints()
    {
        return new String[] {};
    }

    /* (non-Javadoc)
     * @see javax.jcr.nodetype.PropertyDefinition#getDefaultValues()
     */
    public Value[] getDefaultValues()
    {
        String defaultValue = propDef.getDefaultValue();
        if (defaultValue == null)
        {
            return null;
        }
        return new Value[] { new ValueImpl(typeManager.getSession(), getRequiredType(), defaultValue) };
    }

    /* (non-Javadoc)
     * @see javax.jcr.nodetype.PropertyDefinition#isMultiple()
     */
    public boolean isMultiple()
    {
        return propDef.isMultiValued();
    }

    /* (non-Javadoc)
     * @see javax.jcr.nodetype.ItemDefinition#getDeclaringNodeType()
     */
    public NodeType getDeclaringNodeType()
    {
        ClassDefinition declaringClass = propDef.getContainerClass();
        return typeManager.getNodeTypeImpl(declaringClass.getName());
    }

    /* (non-Javadoc)
     * @see javax.jcr.nodetype.ItemDefinition#getName()
     */
    public String getName()
    {
        return propDef.getName().toPrefixString(typeManager.getNamespaceService());
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
        return propDef.isMandatory();
    }

    /* (non-Javadoc)
     * @see javax.jcr.nodetype.ItemDefinition#getOnParentVersion()
     */
    public int getOnParentVersion()
    {
        // TODO: There's no equivalent in Alfresco, so hard code for now
        if (propDef.getName().equals(JCRPrimaryTypeProperty.PROPERTY_NAME) ||
            propDef.getName().equals(JCRMixinTypesProperty.PROPERTY_NAME))
        {    
            return OnParentVersionAction.COMPUTE;
        }
        
        // TODO: Check this
        return OnParentVersionAction.INITIALIZE;
    }

    /* (non-Javadoc)
     * @see javax.jcr.nodetype.ItemDefinition#isProtected()
     */
    public boolean isProtected()
    {
        return propDef.isProtected();
    }

}
