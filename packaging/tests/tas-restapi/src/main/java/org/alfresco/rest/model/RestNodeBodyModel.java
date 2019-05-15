package org.alfresco.rest.model;

import java.util.List;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.rest.core.assertion.ModelAssertion;
import org.alfresco.utility.model.TestModel;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Generated from 'Alfresco Core REST API' swagger file
 * Base Path {@linkplain /alfresco/api/-default-/public/alfresco/versions/1}
 */
public class RestNodeBodyModel extends TestModel implements IRestModel<RestNodeBodyModel>
{
    @Override
    public ModelAssertion<RestNodeBodyModel> assertThat()
    {
        return new ModelAssertion<RestNodeBodyModel>(this);
    }

    @Override
    public ModelAssertion<RestNodeBodyModel> and()
    {
        return assertThat();
    }

    @JsonProperty(value = "entry")
    RestNodeBodyModel model;

    @Override
    public RestNodeBodyModel onModel()
    {
        return model;
    }

    /**
     * The name must not contain spaces or the following special characters: * " < > \ / ? : and |.
     * The character . must not be used at the end of the name.
     */

    @JsonProperty(required = true)
    private String name;

    @JsonProperty(required = true)
    private String nodeType;

    private List<String> aspectNames;

    private Object properties;

    private String relativePath;

    private Object association;

    private List<String> secondaryChildren;

    private List<String> targets;

    public String getName()
    {
        return this.name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getNodeType()
    {
        return this.nodeType;
    }

    public void setNodeType(String nodeType)
    {
        this.nodeType = nodeType;
    }

     

    public Object getProperties()
    {
        return this.properties;
    }

    public void setProperties(Object properties)
    {
        this.properties = properties;
    }

    public String getRelativePath()
    {
        return this.relativePath;
    }

    public void setRelativePath(String relativePath)
    {
        this.relativePath = relativePath;
    }

    public Object getAssociation()
    {
        return this.association;
    }

    public void setAssociation(Object association)
    {
        this.association = association;
    }

    public List<String> getAspectNames()
    {
        return aspectNames;
    }

    public void setAspectNames(List<String> aspectNames)
    {
        this.aspectNames = aspectNames;
    }

    public List<String> getSecondaryChildren()
    {
        return secondaryChildren;
    }

    public void setSecondaryChildren(List<String> secondaryChildren)
    {
        this.secondaryChildren = secondaryChildren;
    }

    public List<String> getTargets()
    {
        return targets;
    }

    public void setTargets(List<String> targets)
    {
        this.targets = targets;
    }
}
