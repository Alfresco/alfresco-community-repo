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
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.rest.core.assertion.IModelAssertion;
import org.alfresco.utility.model.TestModel;

/**
 * Person Model implementation
 */
public class RestPersonModel extends TestModel implements IModelAssertion<RestPersonModel>, IRestModel<RestPersonModel>
{
    @JsonProperty(value = "entry")
    RestPersonModel personModel;
    
    @Override
    public RestPersonModel onModel()
    {
        return personModel;
    }
    
    private List<String> aspectNames;

    @JsonProperty(required = true)
    private String firstName;
    @JsonProperty(required = true)
    private String id;
    @JsonProperty(required = true)
    private boolean enabled;
    @JsonProperty(required = true)
    private String email;
    private String lastName;
    private String displayName;
    private boolean emailNotificationsEnabled;

    private RestCompanyModel company;

    private String avatarId;
    private String location;
    private String instantMessageId;
    private String googleId;
    private String skypeId;
    private String description;
    private String telephone;
    private String jobTitle;
    private String mobile;
    private String statusUpdatedAt;
    private String userStatus;
    private String password;
    private Object properties;
    private String quotaUsed;
    private String quota;
    private Map<String, Boolean> capabilities;

    public RestPersonModel()
    {
    }

    public RestPersonModel(String firstName, boolean emailNotificationsEnabled, RestCompanyModel company, String id, boolean enabled, String email)
    {
        super();
        this.firstName = firstName;
        this.emailNotificationsEnabled = emailNotificationsEnabled;
        this.company = company;
        this.id = id;
        this.enabled = enabled;
        this.email = email;
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

    public boolean getEmailNotificationsEnabled()
    {
        return emailNotificationsEnabled;
    }

    public void setEmailNotificationsEnabled(boolean emailNotificationsEnabled)
    {
        this.emailNotificationsEnabled = emailNotificationsEnabled;
    }

    public RestCompanyModel getCompany()
    {
        return company;
    }

    public void setCompany(RestCompanyModel company)
    {
        this.company = company;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public boolean getEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getAvatarId()
    {
        return avatarId;
    }

    public void setAvatarId(String avatarId)
    {
        this.avatarId = avatarId;
    }

    public String getLocation()
    {
        return location;
    }

    public void setLocation(String location)
    {
        this.location = location;
    }

    public String getInstantMessageId()
    {
        return instantMessageId;
    }

    public void setInstantMessageId(String instantMessageId)
    {
        this.instantMessageId = instantMessageId;
    }

    public String getGoogleId()
    {
        return googleId;
    }

    public void setGoogleId(String googleId)
    {
        this.googleId = googleId;
    }

    public String getSkypeId()
    {
        return skypeId;
    }

    public void setSkypeId(String skypeId)
    {
        this.skypeId = skypeId;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getTelephone()
    {
        return telephone;
    }

    public void setTelephone(String telephone)
    {
        this.telephone = telephone;
    }

    public String getJobTitle()
    {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle)
    {
        this.jobTitle = jobTitle;
    }

    public String getMobile()
    {
        return mobile;
    }

    public void setMobile(String mobile)
    {
        this.mobile = mobile;
    }

    public String getStatusUpdatedAt()
    {
        return statusUpdatedAt;
    }

    public void setStatusUpdatedAt(String statusUpdatedAt)
    {
        this.statusUpdatedAt = statusUpdatedAt;
    }

    public String getUserStatus()
    {
        return userStatus;
    }

    public void setUserStatus(String userStatus)
    {
        this.userStatus = userStatus;
    }

    public List<String> getAspectNames()
    {
        return aspectNames;
    }

    public void setAspectNames(List<String> aspectNames)
    {
        this.aspectNames = aspectNames;
    }

    public Object getProperties()
    {
        return properties;
    }

    public void setProperties(Object properties)
    {
        this.properties = properties;
    }

    public Map<String, Boolean> getCapabilities()
    {
        return capabilities;
    }

    public void setCapabilities(Map<String, Boolean> capabilities)
    {
        this.capabilities = capabilities;
    }
    
    public String getPassword()
    {
        return password;
    }


    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public void setDisplayName(String displayName)
    {
        this.displayName = displayName;
    }

    public String getQuotaUsed() {
		return quotaUsed;
	}


	public void setQuotaUsed(String quotaUsed) {
		this.quotaUsed = quotaUsed;
	}

	public String getQuota() {
		return quota;
	}

	public void setQuota(String quota) {
		this.quota = quota;
	}


	/**
     * Generate a PersonModel with random values for all existing fields excluding fields specified as ingnoredFields
     * 
     * @param ignoredFields field to be excluded when generating a random model
     * @return
     */
    public static RestPersonModel getRandomPersonModel(String... ignoredFields)
    {
        RestPersonModel personModel = new RestPersonModel();
        setRandomValuesForAllFields(personModel, ignoredFields);
        return personModel;
    }
}
