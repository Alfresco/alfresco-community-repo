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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.alfresco.service.cmr.dictionary.ModelDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
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
    private M2Property m2Property;
    private QName name;
    private QName propertyTypeName;
    private DataTypeDefinition dataType;
    private List<ConstraintDefinition> constraints = new ArrayList<ConstraintDefinition>(5);
    private Map<QName, ConstraintDefinition> constraintsByQName = new HashMap<QName, ConstraintDefinition>(7);
    
    /*package*/ M2PropertyDefinition(
            ClassDefinition classDef,
            M2Property m2Property,
            NamespacePrefixResolver prefixResolver)
    {
        this.classDef = classDef;
        this.m2Property = m2Property;

        // Resolve Names
        this.name = QName.createQName(m2Property.getName(), prefixResolver);
        this.propertyTypeName = QName.createQName(m2Property.getType(), prefixResolver);
    }
    
    
    /*package*/ M2PropertyDefinition(
            ClassDefinition classDef,
            PropertyDefinition propertyDef,
            M2PropertyOverride override)
    {
        this.classDef = classDef;
        this.m2Property = createOverriddenProperty(propertyDef, override);
        this.name = propertyDef.getName();
        this.dataType = propertyDef.getDataType();
        this.propertyTypeName = this.dataType.getName();
    }
    
    
    /*package*/ void resolveDependencies(
            ModelQuery query,
            NamespacePrefixResolver prefixResolver,
            Map<QName, ConstraintDefinition> modelConstraints)
    {
        if (propertyTypeName == null)
        {
            throw new DictionaryException(
                    "d_dictionary.property.err.property_type_not_specified",
                    name.toPrefixString());
        }
        dataType = query.getDataType(propertyTypeName);
        if (dataType == null)
        {
            throw new DictionaryException(
                    "d_dictionary.property.err.property_type_not_found",
                    propertyTypeName.toPrefixString(), name.toPrefixString());
        }
        
        // ensure content properties are not multi-valued
        if (propertyTypeName.equals(DataTypeDefinition.CONTENT) && isMultiValued())
        {
            throw new DictionaryException("d_dictionary.property.err.single_valued_content");
        }

        // Construct constraints
        for (M2Constraint constraint : m2Property.getConstraints())
        {
            ConstraintDefinition def = new M2ConstraintDefinition(this, constraint, prefixResolver);
            QName qname = def.getName();
            if (constraintsByQName.containsKey(qname))
            {
                throw new DictionaryException(
                        "d_dictionary.property.err.duplicate_constraint_on_property",
                        def.getName().toPrefixString(), name.toPrefixString());
            }
            else if (modelConstraints.containsKey(qname))
            {
                throw new DictionaryException(
                        "d_dictionary.model.err.duplicate_constraint_on_model",
                        def.getName().toPrefixString());
            }
            constraintsByQName.put(qname, def);
            constraints.add(def);
            modelConstraints.put(qname, def);
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
            value = m2Property.getTitle();
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
            value = m2Property.getDescription();
        }
        return value;
    }
    

    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.PropertyDefinition#getDefaultValue()
     */
    public String getDefaultValue()
    {
        return m2Property.getDefaultValue();
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
        return m2Property.isMultiValued();
    }

    
    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.PropertyDefinition#isMandatory()
     */
    public boolean isMandatory()
    {
        return m2Property.isMandatory();
    }
    

    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.PropertyDefinition#isProtected()
     */
    public boolean isProtected()
    {
        return m2Property.isProtected();
    }
    

    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.PropertyDefinition#isIndexed()
     */
    public boolean isIndexed()
    {
        return m2Property.isIndexed();
    }
    

    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.PropertyDefinition#isStoredInIndex()
     */
    public boolean isStoredInIndex()
    {
        return m2Property.isStoredInIndex();
    }
    

    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.PropertyDefinition#isIndexedAtomically()
     */
    public boolean isIndexedAtomically()
    {
        return m2Property.isIndexedAtomically();
    }
    

    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.PropertyDefinition#isTokenisedInIndex()
     */
    public boolean isTokenisedInIndex()
    {
        return m2Property.isTokenisedInIndex();
    }

    public List<ConstraintDefinition> getConstraints()
    {
        return Collections.unmodifiableList(constraints);
    }
}
