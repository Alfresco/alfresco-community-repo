/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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

package org.alfresco.rest.api.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.alfresco.repo.dictionary.Facetable;
import org.alfresco.repo.dictionary.IndexTokenisationMode;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.i18n.MessageLookup;

/**
 * @author Jamal Kaabi-Mofrad
 */
public class CustomModelProperty extends AbstractCommonDetails
{
    private String dataType;
    private boolean isMandatory;
    private boolean isMandatoryEnforced;
    private boolean isMultiValued;
    private String defaultValue;
    private boolean isIndexed = true;
    private Facetable facetable = Facetable.UNSET;
    private IndexTokenisationMode indexTokenisationMode;
    private List<String> constraintRefs = Collections.emptyList();
    private List<CustomModelConstraint> constraints = Collections.emptyList();

    public CustomModelProperty()
    {
    }

    public CustomModelProperty(PropertyDefinition propertyDefinition, MessageLookup messageLookup)
    {
        this.name = propertyDefinition.getName().getLocalName();
        this.prefixedName = propertyDefinition.getName().toPrefixString();
        this.title = propertyDefinition.getTitle(messageLookup);
        this.dataType = propertyDefinition.getDataType().getName().toPrefixString();
        this.description = propertyDefinition.getDescription(messageLookup);
        this.isMandatory = propertyDefinition.isMandatory();
        this.isMandatoryEnforced = propertyDefinition.isMandatoryEnforced();
        this.isMultiValued = propertyDefinition.isMultiValued();
        this.defaultValue = propertyDefinition.getDefaultValue();
        this.isIndexed = propertyDefinition.isIndexed();
        this.facetable = propertyDefinition.getFacetable();
        this.indexTokenisationMode = propertyDefinition.getIndexTokenisationMode();
        List<ConstraintDefinition> constraintDefs = propertyDefinition.getConstraints();
        if (constraintDefs.size() > 0)
        {
            this.constraintRefs = new ArrayList<>();
            this.constraints = new ArrayList<>();
            for (ConstraintDefinition cd : constraintDefs)
            {
                if (cd.getRef() != null)
                {
                    constraintRefs.add(cd.getRef().toPrefixString());
                }
                else
                {
                    constraints.add(new CustomModelConstraint(cd, messageLookup));
                }
            }
        }
    }

    public String getDataType()
    {
        return this.dataType;
    }

    public void setDataType(String dataType)
    {
        this.dataType = dataType;
    }

    public boolean isMandatory()
    {
        return this.isMandatory;
    }

    public void setMandatory(boolean isMandatory)
    {
        this.isMandatory = isMandatory;
    }

    public boolean isMandatoryEnforced()
    {
        return this.isMandatoryEnforced;
    }

    public void setMandatoryEnforced(boolean isMandatoryEnforced)
    {
        this.isMandatoryEnforced = isMandatoryEnforced;
    }

    public boolean isMultiValued()
    {
        return this.isMultiValued;
    }

    public void setMultiValued(boolean isMultiValued)
    {
        this.isMultiValued = isMultiValued;
    }

    public String getDefaultValue()
    {
        return this.defaultValue;
    }

    public void setDefaultValue(String defaultValue)
    {
        this.defaultValue = defaultValue;
    }

    public boolean isIndexed()
    {
        return this.isIndexed;
    }

    public void setIndexed(boolean isIndexed)
    {
        this.isIndexed = isIndexed;
    }

    public Facetable getFacetable()
    {
        return this.facetable;
    }

    public void setFacetable(Facetable facetable)
    {
        this.facetable = facetable;
    }

    public IndexTokenisationMode getIndexTokenisationMode()
    {
        return this.indexTokenisationMode;
    }

    public void setIndexTokenisationMode(IndexTokenisationMode indexTokenisationMode)
    {
        this.indexTokenisationMode = indexTokenisationMode;
    }

    public List<String> getConstraintRefs()
    {
        return this.constraintRefs;
    }

    public void setConstraintRefs(List<String> constraintRefs)
    {
        this.constraintRefs = constraintRefs;
    }

    public List<CustomModelConstraint> getConstraints()
    {
        return this.constraints;
    }

    public void setConstraints(List<CustomModelConstraint> constraints)
    {
        this.constraints = constraints;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder(612);
        builder.append("CustomModelProperty [name=").append(this.name)
                    .append(", prefixedName=").append(this.prefixedName)
                    .append(", title=").append(this.title)
                    .append(", description=").append(this.description)
                    .append(", dataType=").append(this.dataType)
                    .append(", isMandatory=").append(this.isMandatory)
                    .append(", isMandatoryEnforced=").append(this.isMandatoryEnforced)
                    .append(", isMultiValued=").append(this.isMultiValued)
                    .append(", defaultValue=").append(this.defaultValue)
                    .append(", isIndexed=").append(this.isIndexed)
                    .append(", facetable=").append(this.facetable)
                    .append(", indexTokenisationMode=").append(this.indexTokenisationMode)
                    .append(", constraintRefs=").append(this.constraintRefs)
                    .append(", constraints=").append(this.constraints)
                    .append(']');
        return builder.toString();
    }
}
