/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.rest.api.model;

import java.util.List;

public class ActionDefinition
{
    private String id;
    private String name;
    private String title;
    private String description;
    private List<String> applicableTypes;
    private boolean adhocPropertiesAllowed;
    private boolean trackStatus;
    private List<ParameterDefinition> parameterDefinitions;

    /**
     * For Jackson deserialisation.
     */
    public ActionDefinition()
    {
    }

    public ActionDefinition(String id,
                            String name,
                            String title,
                            String description,
                            List<String> applicableTypes,
                            boolean adhocPropertiesAllowed,
                            boolean trackStatus,
                            List<ParameterDefinition> parameterDefinitions)
    {
        this.id = id;
        this.name = name;
        this.title = title;
        this.description = description;
        this.applicableTypes = applicableTypes;
        this.adhocPropertiesAllowed = adhocPropertiesAllowed;
        this.trackStatus = trackStatus;
        this.parameterDefinitions = parameterDefinitions;
    }

    /**
     * Will be used as a synonym for name.
     */
    public String getId()
    {
        return id;
    }
    
    public String getName()
    {
        return name;
    }

    public String getTitle()
    {
        return title;
    }

    public String getDescription()
    {
        return description;
    }

    public List<String> getApplicableTypes()
    {
        return applicableTypes;
    }

    public boolean isAdhocPropertiesAllowed()
    {
        return adhocPropertiesAllowed;
    }

    public boolean isTrackStatus()
    {
        return trackStatus;
    }

    public List<ParameterDefinition> getParameterDefinitions()
    {
        return parameterDefinitions;
    }

    public static class ParameterDefinition
    {
        private String name;
        private String type;
        private boolean multiValued;
        private boolean mandatory;
        private String displayLabel;
        private String parameterConstraintName;

        /**
         * For Jackson deserialisation.
         */
        public ParameterDefinition()
        {
        }

        public ParameterDefinition(String name,
                                   String type,
                                   boolean multiValued,
                                   boolean mandatory,
                                   String displayLabel,
                                   String parameterConstraintName)
        {
            this.name = name;
            this.type = type;
            this.multiValued = multiValued;
            this.mandatory = mandatory;
            this.displayLabel = displayLabel;
            this.parameterConstraintName = parameterConstraintName;
        }

        public String getName()
        {
            return name;
        }

        public String getType()
        {
            return type;
        }

        public boolean isMultiValued()
        {
            return multiValued;
        }

        public boolean isMandatory()
        {
            return mandatory;
        }

        public String getDisplayLabel()
        {
            return displayLabel;
        }

        public String getParameterConstraintName()
        {
            return parameterConstraintName;
        }
    }
}
