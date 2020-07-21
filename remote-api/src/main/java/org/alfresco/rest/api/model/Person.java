/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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

import com.fasterxml.jackson.annotation.JsonProperty;
import org.alfresco.model.ContentModel;
import org.alfresco.rest.framework.resource.UniqueId;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a person (aka user) within the system.
 * 
 * @author steveglover
 *
 */
public class Person implements Serializable
{
	private static final long serialVersionUID = 1L;
	protected String userName;
	protected Boolean enabled;
	protected NodeRef avatarId;
	protected String firstName;
	protected String lastName;
	protected String displayName;
	protected String jobTitle;
	protected String location;
	protected String telephone;
	protected String mobile;
	protected String email;
	protected String skypeId;
	protected String instantMessageId;
	protected String userStatus;
	protected Date statusUpdatedAt;
	protected String googleId;
	protected Long quota;
	protected Long quotaUsed;
	protected Boolean emailNotificationsEnabled;
	protected String description;
	protected transient Company company;
	protected String password;
	protected String oldPassword;
	protected transient Map<String, Object> properties;
	protected transient List<String> aspectNames;
	protected Map<String, Boolean> capabilities;

	private Map<QName, Boolean> setFields = new HashMap<>(7);

	public static final QName PROP_PERSON_DESCRIPTION = QName.createQName("RestApi", "description");
	public static final QName PROP_PERSON_COMPANY = QName.createQName("RestApi", "company");
	public static final QName PROP_PERSON_AVATAR_ID = QName.createQName("RestApi", "avatarId");
	public static final QName PROP_PERSON_OLDPASSWORD = QName.createQName("RestApi", "oldPassword");
	public static final QName PROP_PERSON_PASSWORD = QName.createQName("RestApi", "password");

	public Person()
    {
	}

	public Person(
			String userName,
			Boolean enabled,
			NodeRef avatarId,
			String firstName,
			String lastName,
			String jobTitle,
			String location,
			String telephone,
			String mobile,
			String email,
			String skypeId,
			String instantMessageId,
			String userStatus,
			Date statusUpdatedAt,
			String googleId,
			Long quota,
			Long quotaUsed,
			Boolean emailNotificationsEnabled,
			String description,
			Company company)
	{
		setUserName(userName);
		setEnabled(enabled);
		setAvatarId(avatarId);
		setFirstName(firstName);
		setLastName(lastName);
		setJobTitle(jobTitle);
		setLocation(location);
		setTelephone(telephone);
		setMobile(mobile);
        if (email != null)
        {
            setEmail(email);
        }
		setSkypeId(skypeId);
		setInstantMessageId(instantMessageId);
		setUserStatus(userStatus);
		setGoogleId(googleId);
		setEmailNotificationsEnabled(emailNotificationsEnabled);
		setDescription(description);
		setCompany(company);

		// system-maintained / derived
		setStatusUpdatedAt(statusUpdatedAt);
		setQuota(quota);
		updateDisplayName();
        if (quotaUsed != null)
        {
            setQuotaUsed(quotaUsed);
        }
	}

	public Person(NodeRef nodeRef, Map<QName, Serializable> nodeProps, boolean enabled)
    {
        mapProperties(nodeProps);
		this.enabled = enabled;
	    updateDisplayName();
	}

	protected void mapProperties(Map<QName, Serializable> nodeProps)
    {
		nodeProps.remove(ContentModel.PROP_CONTENT);

		setUserName((String) nodeProps.get(ContentModel.PROP_USERNAME));
		setFirstName((String) nodeProps.get(ContentModel.PROP_FIRSTNAME));
		setLastName((String) nodeProps.get(ContentModel.PROP_LASTNAME));
		setJobTitle((String) nodeProps.get(ContentModel.PROP_JOBTITLE));

		setLocation((String) nodeProps.get(ContentModel.PROP_LOCATION));
		setTelephone((String) nodeProps.get(ContentModel.PROP_TELEPHONE));
		setMobile((String) nodeProps.get(ContentModel.PROP_MOBILE));
		setEmail((String) nodeProps.get(ContentModel.PROP_EMAIL));

		String organization = (String) nodeProps.get(ContentModel.PROP_ORGANIZATION);
		String companyAddress1 = (String) nodeProps.get(ContentModel.PROP_COMPANYADDRESS1);
		String companyAddress2 = (String) nodeProps.get(ContentModel.PROP_COMPANYADDRESS2);
		String companyAddress3 = (String) nodeProps.get(ContentModel.PROP_COMPANYADDRESS3);
		String companyPostcode = (String) nodeProps.get(ContentModel.PROP_COMPANYPOSTCODE);
		String companyTelephone = (String) nodeProps.get(ContentModel.PROP_COMPANYTELEPHONE);
		String companyFax = (String) nodeProps.get(ContentModel.PROP_COMPANYFAX);
		String companyEmail = (String) nodeProps.get(ContentModel.PROP_COMPANYEMAIL);
		setCompany(new Company(organization, companyAddress1, companyAddress2, companyAddress3, companyPostcode, companyTelephone, companyFax, companyEmail));

		setSkypeId((String) nodeProps.get(ContentModel.PROP_SKYPE));
		setInstantMessageId((String) nodeProps.get(ContentModel.PROP_INSTANTMSG));
		setUserStatus((String) nodeProps.get(ContentModel.PROP_USER_STATUS));
		setGoogleId((String) nodeProps.get(ContentModel.PROP_GOOGLEUSERNAME));
		Boolean bool = (Boolean)nodeProps.get(ContentModel.PROP_EMAIL_FEED_DISABLED);
		setEmailNotificationsEnabled(bool == null ? Boolean.TRUE : !bool.booleanValue());
		setDescription((String)nodeProps.get(PROP_PERSON_DESCRIPTION));

		// system-maintained / derived
		setStatusUpdatedAt((Date) nodeProps.get(ContentModel.PROP_USER_STATUS_TIME));
		setQuota((Long) nodeProps.get(ContentModel.PROP_SIZE_QUOTA));
		setQuotaUsed((Long) nodeProps.get(ContentModel.PROP_SIZE_CURRENT));
    }

	public Company getCompany()
	{
		return company;
	}

	public void setCompany(Company company)
	{
		this.company = company;
		setFields.put(PROP_PERSON_COMPANY, true);
	}

	public String getInstantMessageId()
	{
		return instantMessageId;
	}

	public void setInstantMessageId(String instantMessageId)
	{
		this.instantMessageId = instantMessageId;
		setFields.put(ContentModel.PROP_INSTANTMSG, true);
	}

	public String getGoogleId()
	{
		return googleId;
	}

	public void setGoogleId(String googleId)
	{
		this.googleId = googleId;
		setFields.put(ContentModel.PROP_GOOGLEUSERNAME, true);
	}

	public Long getQuota()
	{
		return quota;
	}

	// TEMP: for tracking (pending REPO-110)
	protected void setQuota(Long quota)
	{
		this.quota = quota;
		setFields.put(ContentModel.PROP_SIZE_QUOTA, true);
	}

	public Long getQuotaUsed()
	{
		return quotaUsed;
	}

	// TEMP: for tracking (pending REPO-110)
	protected void setQuotaUsed(Long quotaUsed)
	{
		this.quotaUsed = quotaUsed;
		setFields.put(ContentModel.PROP_SIZE_CURRENT, true);
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
		setFields.put(PROP_PERSON_DESCRIPTION, true);
	}

	@JsonProperty("id")
	@UniqueId
	public String getUserName() 
	{
		return userName;
	}

	public void setUserName(String userName)
	{
		this.userName = userName;
		setFields.put(ContentModel.PROP_USERNAME, true);
	}

	public Boolean isEnabled()
	{
		return enabled;
	}

	public void setEnabled(Boolean enabled)
	{
		this.enabled = enabled;
		setFields.put(ContentModel.PROP_ENABLED, true);
	}

	public void setAvatarId(NodeRef avatarId)
	{
		this.avatarId = avatarId;
		setFields.put(PROP_PERSON_AVATAR_ID, true);
	}

	public void setPassword(String password)
	{
		this.password = password;
		setFields.put(PROP_PERSON_PASSWORD, true);
	}

	public void setOldPassword(String oldPassword)
	{
		this.oldPassword = oldPassword;
		setFields.put(PROP_PERSON_OLDPASSWORD, true);
	}

	public NodeRef getAvatarId()
	{
		return avatarId;
	}

	public String getFirstName()
	{
		return firstName;
	}

	public void setFirstName(String firstName)
	{
		this.firstName = firstName;
		setFields.put(ContentModel.PROP_FIRSTNAME, true);
		updateDisplayName();
	}

	public void setLastName(String lastName)
	{
		this.lastName = lastName;
		setFields.put(ContentModel.PROP_LASTNAME, true);
		updateDisplayName();
	}

	public String getLastName()
	{
		return lastName;
	}

	public String getJobTitle()
	{
		return jobTitle;
	}

	public void setJobTitle(String jobTitle)
	{
		this.jobTitle = jobTitle;
		setFields.put(ContentModel.PROP_JOBTITLE, true);
	}

	public String getLocation()
	{
		return location;
	}

	public void setLocation(String location)
	{
		this.location = location;
		setFields.put(ContentModel.PROP_LOCATION, true);
	}

	public String getTelephone()
	{
		return telephone;
	}

	public void setTelephone(String telephone)
	{
		this.telephone = telephone;
		setFields.put(ContentModel.PROP_TELEPHONE, true);
	}

	public String getMobile()
	{
		return mobile;
	}

	public void setMobile(String mobile)
	{
		this.mobile = mobile;
		setFields.put(ContentModel.PROP_MOBILE, true);
	}

	public String getEmail()
	{
		return email;
	}

	public void setEmail(String email)
	{
		this.email = email;
		setFields.put(ContentModel.PROP_EMAIL, true);
	}

	public String getSkypeId()
	{
		return skypeId;
	}

	public void setSkypeId(String skypeId)
	{
		this.skypeId = skypeId;
		setFields.put(ContentModel.PROP_SKYPE, true);
	}

	public String getUserStatus()
	{
		return userStatus;
	}

	public void setUserStatus(String userStatus)
	{
		this.userStatus = userStatus;
		setFields.put(ContentModel.PROP_USER_STATUS, true);
	}

	public Date getStatusUpdatedAt()
	{
		return statusUpdatedAt;
	}

	// TEMP: for tracking (pending REPO-110)
	protected void setStatusUpdatedAt(Date statusUpdatedAt)
	{
		this.statusUpdatedAt = statusUpdatedAt;
		setFields.put(ContentModel.PROP_USER_STATUS_TIME, true);
	}

	public Boolean isEmailNotificationsEnabled()
	{
		return emailNotificationsEnabled;
	}

	public void setEmailNotificationsEnabled(Boolean emailNotificationsEnabled)
	{
		this.emailNotificationsEnabled = emailNotificationsEnabled;
		setFields.put(ContentModel.PROP_EMAIL_FEED_DISABLED, true);
	}

	public String getPassword()
	{
		return this.password;
	}

	public String getOldPassword()
	{
		return oldPassword;
	}

	public Map<String, Object> getProperties()
	{
		return properties;
	}

	public void setProperties(Map<String, Object> properties)
	{
		this.properties = properties;
	}

	public List<String> getAspectNames()
	{
		return aspectNames;
	}

	public void setAspectNames(List<String> aspectNames)
	{
		this.aspectNames = aspectNames;
	}
	
    public Map<String, Boolean> getCapabilities()
    {
        return capabilities;
    }

    public void setCapabilities(Map<String, Boolean> capabilities)
    {
        this.capabilities = capabilities;
    }

	public boolean wasSet(QName fieldName)
	{
		Boolean b = setFields.get(fieldName);
		return (b != null ? b : false);
	}

	public String getDisplayName()
	{
		return displayName;
	}

	@Override
	public String toString()
	{
		return "Person [userName=" + userName + ", enabled=" + enabled
				+ ", avatarId=" + avatarId + ", firstName=" + firstName
				+ ", lastName=" + lastName + ", jobTitle=" + jobTitle
				+ ", location=" + location
				+ ", telephone=" + telephone + ", mobile=" + mobile
				+ ", email=" + email + ", skypeId=" + skypeId
				+ ", instantMessageId=" + instantMessageId + ", userStatus="
				+ userStatus + ", statusUpdatedAt=" + statusUpdatedAt
				+ ", googleId=" + googleId + ", quota=" + quota
				+ ", quotaUsed=" + quotaUsed + ", emailNotificationsEnabled="
				+ emailNotificationsEnabled + ", description=" + description
				+ ", company=" + company + "]";
	}

	public Map<QName, Serializable> toProperties()
	{
		Map<QName, Serializable> props = new HashMap<>();
		populateProps(props);
		return props;
	}

	private void addToMap(Map<QName, Serializable> properties, QName name, Serializable value)
	{
		if (name != null)
		{
			Boolean b = setFields.get(name);
			if (Boolean.TRUE.equals(b))
			{
				properties.put(name, value);
			}
		}
	}
	
	private void populateProps(Map<QName, Serializable> properties)
	{
		addToMap(properties, ContentModel.PROP_USERNAME, getUserName());
		addToMap(properties, ContentModel.PROP_FIRSTNAME, getFirstName());
		addToMap(properties, ContentModel.PROP_LASTNAME, getLastName());
		addToMap(properties, ContentModel.PROP_JOBTITLE, getJobTitle());
		addToMap(properties, ContentModel.PROP_LOCATION, getLocation());
		addToMap(properties, ContentModel.PROP_TELEPHONE, getTelephone());
		addToMap(properties, ContentModel.PROP_MOBILE, getMobile());
        if (wasSet(ContentModel.PROP_EMAIL))
        {
            addToMap(properties, ContentModel.PROP_EMAIL, getEmail());
        }
		
		if (wasSet(PROP_PERSON_COMPANY))
		{
			Company company = getCompany();

			int setCount = 0;
			if (company != null)
			{
				if (company.wasSet(ContentModel.PROP_ORGANIZATION))
				{
					setCount++;
					properties.put(ContentModel.PROP_ORGANIZATION, company.getOrganization());
				}

				if (company.wasSet(ContentModel.PROP_COMPANYADDRESS1))
				{
					setCount++;
					properties.put(ContentModel.PROP_COMPANYADDRESS1, company.getAddress1());
				}

				if (company.wasSet(ContentModel.PROP_COMPANYADDRESS2))
				{
					setCount++;
					properties.put(ContentModel.PROP_COMPANYADDRESS2, company.getAddress2());
				}

				if (company.wasSet(ContentModel.PROP_COMPANYADDRESS3))
				{
					setCount++;
					properties.put(ContentModel.PROP_COMPANYADDRESS3, company.getAddress3());
				}

				if (company.wasSet(ContentModel.PROP_COMPANYPOSTCODE))
				{
					setCount++;
					properties.put(ContentModel.PROP_COMPANYPOSTCODE, company.getPostcode());
				}

				if (company.wasSet(ContentModel.PROP_COMPANYTELEPHONE))
				{
					setCount++;
					properties.put(ContentModel.PROP_COMPANYTELEPHONE, company.getTelephone());
				}

				if (company.wasSet(ContentModel.PROP_COMPANYFAX))
				{
					setCount++;
					properties.put(ContentModel.PROP_COMPANYFAX, company.getFax());
				}

				if (company.wasSet(ContentModel.PROP_COMPANYEMAIL))
				{
					setCount++;
					properties.put(ContentModel.PROP_COMPANYEMAIL, company.getEmail());
				}
			}

			if (setCount == 0)
			{
				// company was null or {} (no individual properties set)
				properties.put(ContentModel.PROP_ORGANIZATION, null);
				properties.put(ContentModel.PROP_COMPANYADDRESS1, null);
				properties.put(ContentModel.PROP_COMPANYADDRESS2, null);
				properties.put(ContentModel.PROP_COMPANYADDRESS3, null);
				properties.put(ContentModel.PROP_COMPANYPOSTCODE, null);
				properties.put(ContentModel.PROP_COMPANYTELEPHONE, null);
				properties.put(ContentModel.PROP_COMPANYFAX, null);
				properties.put(ContentModel.PROP_COMPANYEMAIL, null);
			}
		}

//		addToMap(properties, ContentModel.ASSOC_AVATAR, getAvatarId());
		addToMap(properties, ContentModel.PROP_SKYPE, getSkypeId());
		addToMap(properties, ContentModel.PROP_INSTANTMSG, getInstantMessageId());
		addToMap(properties, ContentModel.PROP_USER_STATUS, getUserStatus());
		addToMap(properties, ContentModel.PROP_USER_STATUS_TIME, getStatusUpdatedAt());
		addToMap(properties, ContentModel.PROP_GOOGLEUSERNAME, getGoogleId());
		addToMap(properties, ContentModel.PROP_SIZE_QUOTA, getQuota());
        if (wasSet(ContentModel.PROP_SIZE_CURRENT))
        {
            addToMap(properties, ContentModel.PROP_SIZE_CURRENT, getQuotaUsed());
        }
		addToMap(properties, ContentModel.PROP_PERSONDESC, getDescription());
        addToMap(properties, ContentModel.PROP_ENABLED, isEnabled());

        Boolean isEmailNotificationsEnabled = isEmailNotificationsEnabled();
        addToMap(properties, ContentModel.PROP_EMAIL_FEED_DISABLED, (isEmailNotificationsEnabled == null ? null : !isEmailNotificationsEnabled.booleanValue()));
	}

	private void updateDisplayName()
	{
		displayName = ((firstName != null ? firstName + " " : "") + (lastName != null ? lastName : "")).trim();
	}
}
