/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.dictionary;

import java.util.ArrayList;
import java.util.Collection;
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
import org.alfresco.util.EqualsHelper;


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
    private List<ConstraintDefinition> constraintDefs = Collections.emptyList();
    
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
            M2PropertyOverride override,
            NamespacePrefixResolver prefixResolver,
            Map<QName, ConstraintDefinition> modelConstraints)
    {
        this.classDef = classDef;
        this.m2Property = createOverriddenProperty(propertyDef, override, prefixResolver, modelConstraints);
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
        constraintDefs = buildConstraints(
                m2Property.getConstraints(),
                this,
                prefixResolver,
                modelConstraints);
    }
    
    private static List<ConstraintDefinition> buildConstraints(
            List<M2Constraint> m2constraints,
            M2PropertyDefinition m2PropertyDef,
            NamespacePrefixResolver prefixResolver,
            Map<QName, ConstraintDefinition> modelConstraints)
    {
        List<ConstraintDefinition> constraints = new ArrayList<ConstraintDefinition>(5);
        Map<QName, ConstraintDefinition> constraintsByQName = new HashMap<QName, ConstraintDefinition>(7);
        for (M2Constraint constraint : m2constraints)
        {
            ConstraintDefinition def = new M2ConstraintDefinition(m2PropertyDef, constraint, prefixResolver);
            QName qname = def.getName();
            if (constraintsByQName.containsKey(qname))
            {
                throw new DictionaryException(
                        "d_dictionary.property.err.duplicate_constraint_on_property",
                        def.getName().toPrefixString(), m2PropertyDef.name.toPrefixString());
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
        // done
        return constraints;
    }
    
    /**
     * Create a property definition whose values are overridden
     * 
     * @param propertyDef  the property definition to override
     * @param override  the overridden values
     * @return  the property definition
     */
    private M2Property createOverriddenProperty(
            PropertyDefinition propertyDef,
            M2PropertyOverride override,
            NamespacePrefixResolver prefixResolver,
            Map<QName, ConstraintDefinition> modelConstraints)
    {
        M2Property property = new M2Property();
        property.setOverride(true);
        
        // Process Default Value
        String defaultValue = override.getDefaultValue();
        property.setDefaultValue(defaultValue == null ? propertyDef.getDefaultValue() : defaultValue);

        // Process Mandatory Value
        Boolean isOverrideMandatory = override.isMandatory();
        boolean isOverrideMandatoryEnforced = override.isMandatoryEnforced();
        if (isOverrideMandatory != null && propertyDef.isMandatory())
        {
            // the override specified whether the property should be mandatory or not
            // check that the mandatory enforcement is not relaxed
            if (!isOverrideMandatory)
            {
                throw new DictionaryException(
                        "d_dictionary.property.err.cannot_relax_mandatory",
                        propertyDef.getName().toPrefixString());
            }
            else if (!isOverrideMandatoryEnforced && propertyDef.isMandatoryEnforced())
            {
                throw new DictionaryException(
                        "d_dictionary.property.err.cannot_relax_mandatory_enforcement",
                        propertyDef.getName().toPrefixString());
            }
        }
        property.setMandatory(isOverrideMandatory == null ? propertyDef.isMandatory() : isOverrideMandatory);
        property.setMandatoryEnforced(isOverrideMandatoryEnforced);

        // inherit or override constraints
        List<M2Constraint> overrideConstraints = override.getConstraints();
        if (overrideConstraints != null)
        {
            constraintDefs = buildConstraints(
                    overrideConstraints,
                    (M2PropertyDefinition) propertyDef,
                    prefixResolver,
                    modelConstraints);
        }
        else
        {
            this.constraintDefs = propertyDef.getConstraints();
        }

        // Copy all other properties as they are
        property.setDescription(propertyDef.getDescription());
        property.setIndexed(propertyDef.isIndexed());
        property.setIndexedAtomically(propertyDef.isIndexedAtomically());
        property.setMultiValued(propertyDef.isMultiValued());
        property.setProtected(propertyDef.isProtected());
        property.setStoredInIndex(propertyDef.isStoredInIndex());
        property.setTitle(propertyDef.getTitle());
        property.setIndexTokenisationMode(propertyDef.getIndexTokenisationMode());
        
        return property;
    }
    
    /**
     * @see #getName()
     */
    @Override
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("Name: " + getName() + "\n");
        sb.append("Title: " + getTitle() + "\n");
        sb.append("Description: " + getDescription() + "\n");
        sb.append("Default Value: " + getDefaultValue() + "\n");
        sb.append("DataType Name: " + getDataType().getName() + "\n");
        sb.append("ContainerClass Name: " + getContainerClass().getName() + "\n");
        sb.append("isMultiValued: " + isMultiValued() + "\n");
        sb.append("isMandatory: " + isMandatory() + "\n");
        sb.append("isMandatoryEnforced: " + isMandatoryEnforced() + "\n");
        sb.append("isProtected: " + isProtected() + "\n");
        sb.append("isIndexed: " + isIndexed() + "\n");
        sb.append("isStoredInIndex: " + isStoredInIndex() + "\n");
        sb.append("isIndexedAtomically: " + isIndexedAtomically() + "\n");
        sb.append("indexTokenisationMode: " + getIndexTokenisationMode() + "\n");
        
        return sb.toString();
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

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.dictionary.PropertyDefinition#isOverride()
     */
    public boolean isOverride()
    {
        return m2Property.isOverride();
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
    
    public boolean isMandatoryEnforced()
    {
        return m2Property.isMandatoryEnforced();
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
    public IndexTokenisationMode getIndexTokenisationMode()
    {
        return m2Property.getIndexTokenisationMode();
    }

    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.dictionary.PropertyDefinition#getConstraints()
     */
    public List<ConstraintDefinition> getConstraints()
    {
        return constraintDefs;
    }
    
    /* package */ M2ModelDiff diffProperty(PropertyDefinition propDef)
    {
        M2ModelDiff modelDiff = null;
        boolean isUpdated = false;
        boolean isUpdatedIncrementally = false;
        
        if (this == propDef)
        {
            modelDiff = new M2ModelDiff(name, M2ModelDiff.TYPE_PROPERTY, M2ModelDiff.DIFF_UNCHANGED);
            return modelDiff;
        }
        
        // check name - cannot be null
        if (! name.equals(propDef.getName()))
        { 
            isUpdated = true;
        }
        
        // check title
        if (! EqualsHelper.nullSafeEquals(getTitle(), propDef.getTitle(), false))
        { 
            isUpdatedIncrementally = true;
        }
        
        // check description
        if (! EqualsHelper.nullSafeEquals(getDescription(), propDef.getDescription(), false))
        { 
            isUpdatedIncrementally = true;
        }
        
        // check default value
        if (! EqualsHelper.nullSafeEquals(getDefaultValue(), propDef.getDefaultValue(), false))
        { 
            isUpdatedIncrementally = true;
        }
        
        // check datatype qname (TODO check datatype defs separately)
        if (! EqualsHelper.nullSafeEquals(getDataType().getName(), propDef.getDataType().getName()))
        { 
            isUpdated = true;
        }
        
        // check container class qname
        if (! EqualsHelper.nullSafeEquals(getContainerClass().getName(), propDef.getContainerClass().getName()))
        { 
            isUpdated = true;
        }
        
        // check multi-valued
        if (isMultiValued() != propDef.isMultiValued())
        { 
            isUpdated = true;
        }
        
        // check mandatory
        if (isMandatory() != propDef.isMandatory())
        { 
            isUpdated = true;
        }
        
        // check mandatory enforced
        if (isMandatoryEnforced() != propDef.isMandatoryEnforced())
        { 
            isUpdated = true;
        }
        
        // check protected
        if (isProtected() != propDef.isProtected())
        { 
            isUpdated = true;
        }
        
        //
        // property indexing - is index enabled -> stored, atomic, tokenized (true, false, both)
        //
        //    <index enabled="true">
        //       <atomic>true</atomic>
        //       <stored>false</stored> 
        //       <tokenised>both</tokenised>
        //    </index>
        //
        
        if (isIndexed() != propDef.isIndexed())
        { 
            isUpdatedIncrementally = true;
        }
        
        if (isStoredInIndex() != propDef.isStoredInIndex())
        { 
            isUpdatedIncrementally = true;
        }
        
        if (isIndexedAtomically() != propDef.isIndexedAtomically())
        { 
            isUpdatedIncrementally = true;
        }
        
        if (! EqualsHelper.nullSafeEquals(getIndexTokenisationMode().toString(), propDef.getIndexTokenisationMode().toString(), false))
        { 
            isUpdatedIncrementally = true;
        }
        
        
        // check override
        if (isOverride() != propDef.isOverride())
        { 
            isUpdated = true;
        }
        
        // TODO - check prop constraints (inline and referenced)
        
        if (isUpdated)
        {
            modelDiff = new M2ModelDiff(name, M2ModelDiff.TYPE_PROPERTY, M2ModelDiff.DIFF_UPDATED);
        }
        else if (isUpdatedIncrementally)
        {
            modelDiff = new M2ModelDiff(name, M2ModelDiff.TYPE_PROPERTY, M2ModelDiff.DIFF_UPDATED_INC);
        }
        else
        {
            modelDiff = new M2ModelDiff(name, M2ModelDiff.TYPE_PROPERTY, M2ModelDiff.DIFF_UNCHANGED);
        }
        
        return modelDiff;
    }
    
    /*package*/ static Collection<M2ModelDiff> diffPropertyLists(Collection<PropertyDefinition> previousProperties, Collection<PropertyDefinition> newProperties)
    {
        List<M2ModelDiff> modelDiffs = new ArrayList<M2ModelDiff>();
        
        for (PropertyDefinition previousProperty : previousProperties)
        {
            boolean found = false;
            for (PropertyDefinition newProperty : newProperties)
            {
                if (newProperty.getName().equals(previousProperty.getName()))
                {
                    modelDiffs.add(((M2PropertyDefinition)previousProperty).diffProperty(newProperty));
                    found = true;
                    break;
                }
            }
            
            if (! found)
            {
                modelDiffs.add(new M2ModelDiff(previousProperty.getName(), M2ModelDiff.TYPE_PROPERTY, M2ModelDiff.DIFF_DELETED));
            }
        }
        
        for (PropertyDefinition newProperty : newProperties)
        {
            boolean found = false;
            for (PropertyDefinition previousProperty : previousProperties)
            {
                if (newProperty.getName().equals(previousProperty.getName()))
                {
                    found = true;
                    break;
                }
            }
            
            if (! found)
            {
                modelDiffs.add(new M2ModelDiff(newProperty.getName(), M2ModelDiff.TYPE_PROPERTY, M2ModelDiff.DIFF_CREATED));
            }
        }
        
        return modelDiffs;
    }
}
