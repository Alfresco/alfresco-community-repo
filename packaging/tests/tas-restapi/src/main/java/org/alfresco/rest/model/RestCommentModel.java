package org.alfresco.rest.model;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.rest.core.assertion.ModelAssertion;
import org.alfresco.utility.model.TestModel;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RestCommentModel extends TestModel implements IRestModel<RestCommentModel>
{
    @JsonProperty(value = "entry")
    RestCommentModel model;

    @Override
    public RestCommentModel onModel()
    {         
        return model;
    }

    private String createdAt;
    private RestPersonModel createdBy;
    private String edited;
    private String modifiedAt;
    private boolean canEdit;
    private RestPersonModel modifiedBy;
    private boolean canDelete;
    private String id;
    private String content;

    public String getCreatedAt()
    {
        return createdAt;
    }

    public void setCreatedAt(String createdAt)
    {
        this.createdAt = createdAt;
    }

    public String getEdited()
    {
        return edited;
    }

    public void setEdited(String edited)
    {
        this.edited = edited;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public RestPersonModel getCreatedBy()
    {
        return createdBy;
    }

    public void RestPersonModel(RestPersonModel createdBy)
    {
        this.createdBy = createdBy;
    }

    public String getModifiedAt()
    {
        return modifiedAt;
    }

    public void setModifiedAt(String modifiedAt)
    {
        this.modifiedAt = modifiedAt;
    }

    public boolean isCanEdit()
    {
        return canEdit;
    }

    public void setCanEdit(boolean canEdit)
    {
        this.canEdit = canEdit;
    }

    public RestPersonModel getModifiedBy()
    {
        return modifiedBy;
    }

    public void setModifiedBy(RestPersonModel modifiedBy)
    {
        this.modifiedBy = modifiedBy;
    }

    public boolean isCanDelete()
    {
        return canDelete;
    }

    public void setCanDelete(boolean canDelete)
    {
        this.canDelete = canDelete;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }
    
    @Override
    public ModelAssertion<RestCommentModel> and() 
    {      
        return assertThat();
    }   
    
    @Override
    public ModelAssertion<RestCommentModel> assertThat() 
    {      
      return new ModelAssertion<RestCommentModel>(this);
    }   
    
}    