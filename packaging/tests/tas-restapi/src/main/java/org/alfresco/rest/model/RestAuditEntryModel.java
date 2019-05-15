package org.alfresco.rest.model;

import java.util.Map;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.rest.core.assertion.ModelAssertion;
import org.alfresco.utility.model.TestModel;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RestAuditEntryModel extends TestModel implements IRestModel<RestAuditEntryModel>
{
    @Override
    public ModelAssertion<RestAuditEntryModel> assertThat()
    {
        return new ModelAssertion<RestAuditEntryModel>(this);
    }

    @Override
    public ModelAssertion<RestAuditEntryModel> and()
    {
        return assertThat();
    }

    @JsonProperty(value = "entry")
    RestAuditEntryModel model;

    @Override
    public RestAuditEntryModel onModel()
    {
        return model;
    }

    @JsonProperty(required = true)
    private String id;
    private String auditApplicationId ;
    private String createdAt;
    @JsonProperty("createdByUser")
    private RestByUserModel createdByUser;

    @JsonProperty("values")
    Map<String, Object>  values;

public Map<String, Object> getValues()
    {
        return values;
    }

    public void setValues(Map<String, Object> values)
    {
        this.values = values;
    }

    public String getId()
    {
        return this.id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getAuditApplicationId()
    {
        return this.auditApplicationId;
    }

    public void setAuditApplicationId(String auditApplicationId)
    {
        this.auditApplicationId = auditApplicationId;
    }

    public RestByUserModel getCreatedByUser()
    {
        return this.createdByUser;
    }

    public void setCreatedByUser(RestByUserModel createdByUser)
    {
        this.createdByUser = createdByUser;
    }

    public String getCreatedAt()
    {
        return this.createdAt;
    }

    public void setCreatedAt(String createdAt)
    {
        this.createdAt = createdAt;
    }

}
