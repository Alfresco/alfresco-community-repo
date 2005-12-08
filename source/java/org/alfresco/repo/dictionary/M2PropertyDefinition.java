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
package org.alfresco.repo.dictionary;

import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.alfresco.service.cmr.dictionary.ModelDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;


/**
 * Compiled Property Definition
 * 
 * @author David Caruana
 */
/*package*/ class M2PropertyDefinition implements PropertyDefinition
{
    private ClassDefinition classDef;
    private M2Property property;
    private QName name;
    private QName propertyTypeName;
    private DataTypeDefinition dataType;
    
    
    /*package*/ M2PropertyDefinition(ClassDefinition classDef, M2Property m2Property, NamespacePrefixResolver resolver)
    {
        this.classDef = classDef;
        this.property = m2Property;

        // Resolve Names
        this.name = QName.createQName(property.getName(), resolver);
        this.propertyTypeName = QName.createQName(property.getType(), resolver);
    }
    
    
    /*package*/ M2PropertyDefinition(ClassDefinition classDef, PropertyDefinition propertyDef, M2PropertyOverride override)
    {
        this.classDef = classDef;
        this.property = createOverriddenProperty(propertyDef, override);
        this.name = propertyDef.getName();
        this.dataType = propertyDef.getDataType();
        this.propertyTypeName = this.dataType.getName();
    }
    
    
    /*package*/ void resolveDependencies(ModelQuery query)
    {
        if (propertyTypeName == null)
        {
            throw new DictionaryException("Property type of property " + name.toPrefixString() + " must be specified");
        }
        dataType = query.getDataType(propertyTypeName);
        if (dataType == null)
        {
            throw new DictionaryException("Property type " + propertyTypeName.toPrefixString() + " of property " + name.toPrefixString() + " is not found");
        }
        
        // ensure content properties are not multi-valued
        if (propertyTypeName.equals(DataTypeDefinition.CONTENT) && isMultiValued())
        {
            throw new DictionaryException("Content properties must be single-valued");
        }
    }
    
    
    /**
     * Create a property definition whose values are overridden
     * 
     * @param propertyDef  the property definition to override
     * @param override  the overridden values
     * @return  the property definition
     */
    private M2Property createOverriddenProperty(PropertyDefinition propertyDef, M2PropertyOverride override)
    {
        M2Property property = new M2Property();
        
        // Process Default Value
        String defaultValue = override.getDefaultValue();
        property.setDefaultValue(defaultValue == null ? propertyDef.getDefaultValue() : defaultValue);

        // Process Mandatory Value
        Boolean isMandatory = override.isMandatory();
        if (isMandatory != null)
        {
            if (propertyDef.isMandatory() == true && isMandatory == false)
            {
                throw new DictionaryException("Cannot relax mandatory attribute of property " + propertyDef.getName().toPrefixString());
            }
        }
        property.setMandatory(isMandatory == null ? propertyDef.isMandatory() : isMandatory);

        // Copy all other properties as they are
        property.setDescription(propertyDef.getDescription());
        property.setIndexed(propertyDef.isIndexed());
        property.setIndexedAtomically(propertyDef.isIndexedAtomically());
        property.setMultiValued(propertyDef.isMultiValued());
        property.setProtected(propertyDef.isProtected());
        property.setStoredInIndex(propertyDef.isStoredInIndex());
        property.setTitle(propertyDef.getTitle());
        property.setTokenisedInIndex(propertyDef.isTokenisedInIndex());
        
        return property;
    }
    
    /**
     * @see #getName()
     */
    @Override
    public String toString()
    {
        return getName().toString();
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.dictionary.PropertyDefinition#getModel()
     */
    public ModelDefinition getModel()
    {
        return classDef.getModel();
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.PropertyDefinition#getName()
     */
    public QName getName()
    {
        return name;
    }
    
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.PropertyDefinition#getTitle()
     */
    public String getTitle()
    {
        String value = M2Label.getLabel(classDef.getModel(), "property", name, "title"); 
        if (value == null)
        {
            value = property.getTitle();
        }
        return value;
    }
    

    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.PropertyDefinition#getDescription()
     */
    public String getDescription()
    {
        String value = M2Label.getLabel(classDef.getModel(), "property", name, "description"); 
        if (value == null)
        {
            value = property.getDescription();
        }
        return value;
    }
    

    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.PropertyDefinition#getDefaultValue()
     */
    public String getDefaultValue()
    {
        return property.getDefaultValue();
    }

    
    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.PropertyDefinition#getPropertyType()
     */
    public DataTypeDefinition getDataType()
    {
        return dataType;
    }
    

    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.PropertyDefinition#getContainerClass()
     */
    public ClassDefinition getContainerClass()
    {
        return classDef;
    }
    

    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.PropertyDefinition#isMultiValued()
     */
    public boolean isMultiValued()
    {
        return property.isMultiValued();
    }

    
    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.PropertyDefinition#isMandatory()
     */
    public boolean isMandatory()
    {
        return property.isMandatory();
    }
    

    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.PropertyDefinition#isProtected()
     */
    public boolean isProtected()
    {
        return property.isProtected();
    }
    

    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.PropertyDefinition#isIndexed()
     */
    public boolean isIndexed()
    {
        return property.isIndexed();
    }
    

    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.PropertyDefinition#isStoredInIndex()
     */
    public boolean isStoredInIndex()
    {
        return property.isStoredInIndex();
    }
    

    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.PropertyDefinition#isIndexedAtomically()
     */
    public boolean isIndexedAtomically()
    {
        return property.isIndexedAtomically();
    }
    

    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.PropertyDefinition#isTokenisedInIndex()
     */
    public boolean isTokenisedInIndex()
    {
        return property.isTokenisedInIndex();
    }
    
}
