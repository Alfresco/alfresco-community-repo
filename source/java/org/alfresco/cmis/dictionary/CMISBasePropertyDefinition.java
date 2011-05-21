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
package org.alfresco.cmis.dictionary;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;

import org.alfresco.cmis.CMISCardinalityEnum;
import org.alfresco.cmis.CMISChoice;
import org.alfresco.cmis.CMISDataTypeEnum;
import org.alfresco.cmis.CMISDictionaryModel;
import org.alfresco.cmis.CMISPropertyDefinition;
import org.alfresco.cmis.CMISPropertyId;
import org.alfresco.cmis.CMISTypeDefinition;
import org.alfresco.cmis.CMISUpdatabilityEnum;
import org.alfresco.cmis.CMISPropertyAccessor;
import org.alfresco.cmis.CMISPropertyLuceneBuilder;
import org.alfresco.cmis.mapping.AbstractProperty;
import org.alfresco.cmis.mapping.CMISMapping;
import org.alfresco.repo.dictionary.IndexTokenisationMode;
import org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint;
import org.alfresco.repo.dictionary.constraint.NumericRangeConstraint;
import org.alfresco.repo.dictionary.constraint.StringLengthConstraint;
import org.alfresco.repo.search.impl.lucene.analysis.DateAnalyser;
import org.alfresco.repo.search.impl.lucene.analysis.DateTimeAnalyser;
import org.alfresco.repo.search.impl.lucene.analysis.DoubleAnalyser;
import org.alfresco.repo.search.impl.lucene.analysis.FloatAnalyser;
import org.alfresco.repo.search.impl.lucene.analysis.IntegerAnalyser;
import org.alfresco.repo.search.impl.lucene.analysis.LongAnalyser;
import org.alfresco.repo.search.impl.lucene.analysis.PathAnalyser;
import org.alfresco.repo.search.impl.lucene.analysis.VerbatimAnalyser;
import org.alfresco.service.cmr.dictionary.Constraint;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.util.ISO9075;

/**
 * CMIS Property Definition
 * 
 * @author andyh
 */
public class CMISBasePropertyDefinition implements CMISPropertyDefinition, Serializable
{
    private static final long serialVersionUID = -8119257313852558466L;

    // Properties of Property
    private CMISTypeDefinition typeDef;

    private CMISPropertyId propertyId;

    private String queryName;

    private String displayName;

    private String description;

    private CMISDataTypeEnum propertyType;

    private CMISCardinalityEnum cardinality;

    private Double minValue = null;
    
    private Double maxValue = null; 
    
    private int maximumLength = -1;

    private Collection<CMISChoice> choices = new HashSet<CMISChoice>();

    private boolean isOpenChoice = false;

    private boolean required;

    private String defaultValue;

    private CMISUpdatabilityEnum updatability;

    private boolean queryable;

    private boolean orderable;

    private AbstractProperty propertyAccessor;

    /**
     * Construct
     * 
     * @param cmisMapping
     * @param propertyId
     * @param propDef
     * @param typeDef
     */
    public CMISBasePropertyDefinition(CMISMapping cmisMapping, CMISPropertyId propertyId, PropertyDefinition propDef, CMISTypeDefinition typeDef)
    {
        this.propertyId = propertyId;
        this.typeDef = typeDef;
        queryName = ISO9075.encodeSQL(cmisMapping.buildPrefixEncodedString(propertyId.getQName()));
        displayName = (propDef.getTitle() != null) ? propDef.getTitle() : propertyId.getId();
        description = propDef.getDescription() != null ? propDef.getDescription() : displayName;
        propertyType = cmisMapping.getDataType(propDef.getDataType());
        cardinality = propDef.isMultiValued() ? CMISCardinalityEnum.MULTI_VALUED : CMISCardinalityEnum.SINGLE_VALUED;
        for (ConstraintDefinition constraintDef : propDef.getConstraints())
        {
            Constraint constraint = constraintDef.getConstraint();
            if (constraint instanceof ListOfValuesConstraint)
            {
                int position = 1; // CMIS is 1 based (according to XSDs)
                ListOfValuesConstraint lovc = (ListOfValuesConstraint) constraint;
                for (String allowed : lovc.getAllowedValues())
                {
                    CMISChoice choice = new CMISChoice(allowed, allowed, position++);
                    choices.add(choice);
                }
            }
            if (constraint instanceof StringLengthConstraint)
            {
                StringLengthConstraint slc = (StringLengthConstraint) constraint;
                maximumLength = slc.getMaxLength();
            }
            if (constraint instanceof NumericRangeConstraint)
            {
                NumericRangeConstraint nrc = (NumericRangeConstraint) constraint;
                minValue = nrc.getMinValue();
                maxValue = nrc.getMaxValue();
            }
        }
        required = propDef.isMandatory();
        defaultValue = propDef.getDefaultValue();
        if (propertyId.getId().equals(CMISDictionaryModel.PROP_OBJECT_TYPE_ID) ||
            propertyId.getId().equals(CMISDictionaryModel.PROP_SOURCE_ID) ||
            propertyId.getId().equals(CMISDictionaryModel.PROP_TARGET_ID))
        {
            // Fix http://issues.alfresco.com/jira/browse/ALF-2637
            updatability = CMISUpdatabilityEnum.ON_CREATE;
        }
        else
        {
            updatability = propDef.isProtected() ? CMISUpdatabilityEnum.READ_ONLY : CMISUpdatabilityEnum.READ_AND_WRITE;
        }
        queryable = propDef.isIndexed();
        if (queryable)
        {
            if (cardinality == CMISCardinalityEnum.SINGLE_VALUED)
            {
                IndexTokenisationMode indexTokenisationMode = IndexTokenisationMode.TRUE;
                if (propDef.getIndexTokenisationMode() != null)
                {
                    indexTokenisationMode = propDef.getIndexTokenisationMode();
                }
                switch (indexTokenisationMode)
                {
                case BOTH:
                case FALSE:
                    orderable = true;
                    break;
                case TRUE:
                default:
                    String analyserClassName = propDef.getDataType().getAnalyserClassName();
                    if(propDef.getDataType().getName().equals(DataTypeDefinition.BOOLEAN))
                    {
                        orderable = true;
                    }
                    else if (analyserClassName.equals(DateTimeAnalyser.class.getCanonicalName())
                            || analyserClassName.equals(DateAnalyser.class.getCanonicalName())
                            || analyserClassName.equals(DoubleAnalyser.class.getCanonicalName()) || analyserClassName.equals(FloatAnalyser.class.getCanonicalName())
                            || analyserClassName.equals(IntegerAnalyser.class.getCanonicalName()) || analyserClassName.equals(LongAnalyser.class.getCanonicalName())
                            || analyserClassName.equals(PathAnalyser.class.getCanonicalName()) || analyserClassName.equals(VerbatimAnalyser.class.getCanonicalName()))
                    {
                        orderable = true;
                    }
                    else
                    {
                        orderable = false;
                    }
                }
            }
            else
            {
                orderable = false;
            }
        }
        else
        {
            orderable = false;
        }
        propertyAccessor = cmisMapping.getPropertyAccessor(propertyId);
    }

    /**
     * Get Property Id
     * 
     * @return
     */
    public CMISPropertyId getPropertyId()
    {
        return propertyId;
    }

    /**
     * Get Owning Type
     * 
     * @return
     */
    public CMISTypeDefinition getOwningType()
    {
        return typeDef;
    }

    /**
     * Get the query name
     * 
     * @return
     */
    public String getQueryName()
    {
        return queryName;
    }

    /**
     * Get the display name
     * 
     * @return
     */
    public String getDisplayName()
    {
        return displayName;
    }

    /**
     * Get the description
     * 
     * @return
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Get the property type
     * 
     * @return
     */
    public CMISDataTypeEnum getDataType()
    {
        return propertyType;
    }

    /**
     * Get the cardinality
     * 
     * @return
     */
    public CMISCardinalityEnum getCardinality()
    {
        return cardinality;
    }

    /**
     * For variable length properties, get the maximum length allowed. Unsupported.
     * 
     * @return
     */
    public int getMaximumLength()
    {
        return maximumLength;
    }

    /* (non-Javadoc)
     * @see org.alfresco.cmis.CMISPropertyDefinition#getMinValue()
     */
    public Double getMinValue()
    {
        return minValue;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.cmis.CMISPropertyDefinition#getMaxValue()
     */
    public Double getMaxValue()
    {
        return maxValue;
    }
    
    /**
     * Get the choices available as values for this property TODO: not implemented yet
     * 
     * @return
     */
    public Collection<CMISChoice> getChoices()
    {
        return choices;
    }

    /**
     * Is this a choice where a user can enter other values (ie a list with common options)
     * 
     * @return
     */
    public boolean isOpenChoice()
    {
        return isOpenChoice;
    }

    /**
     * Is this property required?
     * 
     * @return
     */
    public boolean isRequired()
    {
        return required;
    }

    /**
     * get the default value as a String
     * 
     * @return
     */
    public String getDefaultValue()
    {
        return defaultValue;
    }

    /**
     * Is this property updatable?
     * 
     * @return
     */
    public CMISUpdatabilityEnum getUpdatability()
    {
        return updatability;
    }

    /**
     * Is this property queryable?
     * 
     * @return
     */
    public boolean isQueryable()
    {
        return queryable;
    }

    /**
     * Is this property orderable in queries?
     * 
     * @return
     */
    public boolean isOrderable()
    {
        return orderable;
    }

    /**
     * Gets the property accessor (for reading / writing values)
     * 
     * @return
     */
    public CMISPropertyAccessor getPropertyAccessor()
    {
        return propertyAccessor;
    }

    /**
     * Gets the property Lucene builder
     * 
     * @return
     */
    public CMISPropertyLuceneBuilder getPropertyLuceneBuilder()
    {
        return propertyAccessor;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("CMISPropertyDefinition[");
        builder.append("OwningTypeId=").append(getOwningType().getTypeId()).append(", ");
        builder.append("Id=").append(getPropertyId().getId()).append(", ");
        builder.append("LocalName=").append(getPropertyId().getLocalName()).append(", ");
        builder.append("Namespace=").append(getPropertyId().getLocalNamespace()).append(", ");
        builder.append("InternalQName=").append(getPropertyId().getQName()).append(", ");
        builder.append("QueryName=").append(getQueryName()).append(", ");
        builder.append("DisplayName=").append(getDisplayName()).append(", ");
        builder.append("Description=").append(getDescription()).append(", ");
        builder.append("PropertyType=").append(getDataType()).append(", ");
        builder.append("Cardinality=").append(getCardinality()).append(", ");
        builder.append("MaximumLength=").append(getMaximumLength()).append(", ");
        builder.append("Choices=").append(getChoices()).append(", ");
        builder.append("IsOpenChoice=").append(isOpenChoice()).append(", ");
        builder.append("Required=").append(isRequired()).append(", ");
        builder.append("Default=").append(getDefaultValue()).append(", ");
        builder.append("Updatable=").append(getUpdatability()).append(", ");
        builder.append("Queryable=").append(isQueryable()).append(", ");
        builder.append("Orderable=").append(isOrderable());
        builder.append("]");
        return builder.toString();
    }

}
