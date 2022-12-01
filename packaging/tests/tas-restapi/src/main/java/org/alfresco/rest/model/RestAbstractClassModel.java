/*-
 * #%L
 * alfresco-tas-restapi
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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
package org.alfresco.rest.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.utility.model.TestModel;

public class RestAbstractClassModel extends TestModel implements IRestModel<RestAbstractClassModel>
{
    @JsonProperty(value = "entry")
    RestAbstractClassModel model;

    @Override
    public RestAbstractClassModel onModel()
    {
        return model;
    }

    public String id;
    public String title;
    public String description;
    public String parentId;
    public Boolean isContainer = null;
    public Boolean isArchive = null;
    public Boolean includedInSupertypeQuery = null;
    public List<String> mandatoryAspects = null;
    public List<RestClassAssociationModel> associations = null;
    public List <RestPropertyDefinitionModel> properties = null;

    @JsonProperty(value = "model")
    public RestClassModel modelInfo;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getParentId()
    {
        return parentId;
    }

    public void setParentId(String parentId)
    {
        this.parentId = parentId;
    }

    public Boolean getContainer()
    {
        return isContainer;
    }

    public void setContainer(Boolean container)
    {
        isContainer = container;
    }

    public Boolean getArchive()
    {
        return isArchive;
    }

    public void setArchive(Boolean archive)
    {
        isArchive = archive;
    }

    public Boolean getIncludedInSupertypeQuery()
    {
        return includedInSupertypeQuery;
    }

    public void setIncludedInSupertypeQuery(Boolean includedInSupertypeQuery)
    {
        this.includedInSupertypeQuery = includedInSupertypeQuery;
    }

    public List<String> getMandatoryAspects()
    {
        return mandatoryAspects;
    }

    public void setMandatoryAspects(List<String> mandatoryAspects)
    {
        this.mandatoryAspects = mandatoryAspects;
    }

    public List<RestClassAssociationModel> getAssociations()
    {
        return associations;
    }

    public void setAssociations(List<RestClassAssociationModel> associations)
    {
        this.associations = associations;
    }

    public List<RestPropertyDefinitionModel> getProperties() {
        return properties;
    }

    public void setProperties(List<RestPropertyDefinitionModel> properties)
    {
        this.properties = properties;
    }

    public RestClassModel getModelInfo()
    {
        return modelInfo;
    }

    public void setModelInfo(RestClassModel modelInfo)
    {
        this.modelInfo = modelInfo;
    }
}
