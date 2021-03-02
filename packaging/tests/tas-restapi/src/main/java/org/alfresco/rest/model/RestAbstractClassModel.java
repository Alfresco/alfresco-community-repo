package org.alfresco.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.alfresco.rest.core.IRestModel;
import org.alfresco.rest.core.assertion.ModelAssertion;
import org.alfresco.utility.model.TestModel;

import java.util.List;

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

    /**
     * DSL for assertion on this rest model
     * @return
     */
    @Override
    public ModelAssertion<RestAbstractClassModel> assertThat()
    {
      return new ModelAssertion<RestAbstractClassModel>(this);
    }
    
    @Override
    public ModelAssertion<RestAbstractClassModel> and()
    {
      return assertThat();
    }
}