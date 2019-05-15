package org.alfresco.rest.model;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.rest.core.assertion.ModelAssertion;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.model.TestModel;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 
 * "activitySummary": {
            "firstName": "string",
            "lastName": "string",
            "parentObjectId": "string",
            "title": "string",
            "objectId": "string"
          }
 *
 * @author Cristina Axinte
 * 
 */
public class RestActivitySummaryModel extends TestModel implements IRestModel<RestActivitySummaryModel>
{
    @JsonProperty(value = "entry")
    RestActivitySummaryModel activitySummaryModel;
    
    
    @Override
    public RestActivitySummaryModel onModel() 
    {        
      return activitySummaryModel;
    }
        
    String firstName;
    String lastName;
    String parentObjectId;
    String title;
    String objectId;    
    String memberFirstName;
    UserRole role;
    String memberLastName;
    String memberPersonId;
    
    public String getMemberFirstName()    
    {
        return memberFirstName;
    }
    public void setMemberFirstName(String memberFirstName)
    {
        this.memberFirstName = memberFirstName;
    }
    public UserRole getRole()
    {
        return role;
    }
    public void setRole(UserRole role)
    {
        this.role = role;
    }
    public String getMemberLastName()
    {
        return memberLastName;
    }
    public void setMemberLastName(String memberLastName)
    {
        this.memberLastName = memberLastName;
    }
    public String getMemberPersonId()
    {
        return memberPersonId;
    }
    public void setMemberPersonId(String memberPersonId)
    {
        this.memberPersonId = memberPersonId;
    }
    
    public String getFirstName()
    {
        return firstName;
    }
    public void setFirstName(String firstName)
    {
        this.firstName = firstName;
    }

    public String getLastName()
    {
        return lastName;
    }
    public void setLastName(String lastName)
    {
        this.lastName = lastName;
    }

    public String getParentObjectId()
    {
        return parentObjectId;
    }
    public void setParentObjectId(String parentObjectId)
    {
        this.parentObjectId = parentObjectId;
    }
    public String getTitle()
    {
        return title;
    }
    public void setTitle(String title)
    {
        this.title = title;
    }
    public String getObjectId()
    {
        return objectId;
    }
    public void setObjectId(String objectId)
    {
        this.objectId = objectId;
    }
    
    @Override
    public ModelAssertion<RestActivitySummaryModel> and() {
        return assertThat();
    }   
    
    @Override
    public ModelAssertion<RestActivitySummaryModel> assertThat() {
        return new ModelAssertion<RestActivitySummaryModel>(this);
    }   
}
