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

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.rest.framework.resource.UniqueId;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Represents a user of the system.
 * 
 * @author steveglover
 *
 */
public class Person
{
    public static final QName PROP_PERSON_DESCRIPTION = QName.createQName("RestApi", "description");

	protected String userName;
	protected Boolean enabled;
	protected NodeRef avatarId;
	protected String firstName;
	protected String lastName;
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
	protected Company company;
	protected String password;

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
		this.userName = userName;
		this.enabled = enabled;
		this.avatarId = avatarId;
		this.firstName = firstName;
		this.lastName = lastName;
		this.jobTitle = jobTitle;
		this.location = location;
		this.telephone = telephone;
		this.mobile = mobile;
		this.email = email;
		this.skypeId = skypeId;
		this.instantMessageId = instantMessageId;
		this.userStatus = userStatus;
		this.statusUpdatedAt = statusUpdatedAt;
		this.googleId = googleId;
		this.quota = quota;
		this.quotaUsed = quotaUsed;
		this.emailNotificationsEnabled = emailNotificationsEnabled;
		this.description = description;
		this.company = company;
	}

	public Person(NodeRef nodeRef, Map<QName, Serializable> nodeProps, boolean enabled)
    {
        mapProperties(nodeProps);
		this.enabled = enabled;
	}

	protected void mapProperties(Map<QName, Serializable> nodeProps)
    {
		nodeProps.remove(ContentModel.PROP_CONTENT);

		this.userName = (String) nodeProps.get(ContentModel.PROP_USERNAME);
		this.firstName = (String) nodeProps.get(ContentModel.PROP_FIRSTNAME);
		this.lastName = (String) nodeProps.get(ContentModel.PROP_LASTNAME);
		this.jobTitle = (String) nodeProps.get(ContentModel.PROP_JOBTITLE);

		this.location = (String) nodeProps.get(ContentModel.PROP_LOCATION);
		this.telephone = (String) nodeProps.get(ContentModel.PROP_TELEPHONE);
		this.mobile = (String) nodeProps.get(ContentModel.PROP_MOBILE);
		this.email = (String) nodeProps.get(ContentModel.PROP_EMAIL);

		String organization = (String) nodeProps.get(ContentModel.PROP_ORGANIZATION);
		String companyAddress1 = (String) nodeProps.get(ContentModel.PROP_COMPANYADDRESS1);
		String companyAddress2 = (String) nodeProps.get(ContentModel.PROP_COMPANYADDRESS2);
		String companyAddress3 = (String) nodeProps.get(ContentModel.PROP_COMPANYADDRESS3);
		String companyPostcode = (String) nodeProps.get(ContentModel.PROP_COMPANYPOSTCODE);
		String companyTelephone = (String) nodeProps.get(ContentModel.PROP_COMPANYTELEPHONE);
		String companyFax = (String) nodeProps.get(ContentModel.PROP_COMPANYFAX);
		String companyEmail = (String) nodeProps.get(ContentModel.PROP_COMPANYEMAIL);
		this.company = new Company(organization, companyAddress1, companyAddress2, companyAddress3, companyPostcode, companyTelephone, companyFax, companyEmail);

		this.skypeId = (String) nodeProps.get(ContentModel.PROP_SKYPE);
		this.instantMessageId = (String) nodeProps.get(ContentModel.PROP_INSTANTMSG);
		this.userStatus = (String) nodeProps.get(ContentModel.PROP_USER_STATUS);
		this.statusUpdatedAt = (Date) nodeProps.get(ContentModel.PROP_USER_STATUS_TIME);
		this.googleId = (String) nodeProps.get(ContentModel.PROP_GOOGLEUSERNAME);
		this.quota = (Long) nodeProps.get(ContentModel.PROP_SIZE_QUOTA);
		this.quotaUsed = (Long) nodeProps.get(ContentModel.PROP_SIZE_CURRENT);
		Boolean bool = (Boolean)nodeProps.get(ContentModel.PROP_EMAIL_FEED_DISABLED);
		this.emailNotificationsEnabled = (bool == null ? Boolean.TRUE : !bool.booleanValue());
		this.description = (String)nodeProps.get(PROP_PERSON_DESCRIPTION);
    }
	
	public Company getCompany()
	{
		return company;
	}

	public String getInstantMessageId()
	{
		return instantMessageId;
	}

	public String getGoogleId()
	{
		return googleId;
	}

	public Long getQuota()
	{
		return quota;
	}

	public Long getQuotaUsed()
	{
		return quotaUsed;
	}

	public String getDescription()
	{
		return description;
	}

	@UniqueId
	public String getUserName() 
	{
		return userName;
	}

	public Boolean isEnabled()
	{
		return enabled;
	}

	public void setAvatarId(NodeRef avatarId)
	{
		this.avatarId = avatarId;
	}
	
	public void setPassword(String password)
	{
		this.password = password;
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
	}

	public void setLastName(String lastName)
	{
		this.lastName = lastName;
	}

	public String getLastName()
	{
		return lastName;
	}

	public String getJobTitle()
	{
		return jobTitle;
	}

	public String getLocation()
	{
		return location;
	}

	public String getTelephone()
	{
		return telephone;
	}

	public String getMobile()
	{
		return mobile;
	}

	public String getEmail()
	{
		return email;
	}

	public String getSkypeId()
	{
		return skypeId;
	}

	public String getUserStatus()
	{
		return userStatus;
	}

	public Date getStatusUpdatedAt()
	{
		return statusUpdatedAt;
	}

	public Boolean isEmailNotificationsEnabled()
	{
		return emailNotificationsEnabled;
	}

	public String getPassword()
	{
		return this.password;
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
		if(name != null && value != null)
		{
			properties.put(name, value);
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
		addToMap(properties, ContentModel.PROP_EMAIL, getEmail());

		Company company = getCompany();
		if(company != null)
		{
			addToMap(properties, ContentModel.PROP_ORGANIZATION, company.getOrganization());
			addToMap(properties, ContentModel.PROP_COMPANYADDRESS1, company.getAddress1());
			addToMap(properties, ContentModel.PROP_COMPANYADDRESS2, company.getAddress2());
			addToMap(properties, ContentModel.PROP_COMPANYADDRESS3, company.getAddress3());
			addToMap(properties, ContentModel.PROP_COMPANYPOSTCODE, company.getPostcode());
			addToMap(properties, ContentModel.PROP_COMPANYTELEPHONE, company.getTelephone());
			addToMap(properties, ContentModel.PROP_COMPANYFAX, company.getFax());
			addToMap(properties, ContentModel.PROP_COMPANYEMAIL, company.getEmail());
		}
		else
		{
			addToMap(properties, ContentModel.PROP_ORGANIZATION, null);
			addToMap(properties, ContentModel.PROP_COMPANYADDRESS1, null);
			addToMap(properties, ContentModel.PROP_COMPANYADDRESS2, null);
			addToMap(properties, ContentModel.PROP_COMPANYADDRESS3, null);
			addToMap(properties, ContentModel.PROP_COMPANYPOSTCODE, null);
			addToMap(properties, ContentModel.PROP_COMPANYTELEPHONE, null);
			addToMap(properties, ContentModel.PROP_COMPANYFAX, null);
			addToMap(properties, ContentModel.PROP_COMPANYEMAIL, null);
		}

//		addToMap(properties, ContentModel.ASSOC_AVATAR, getAvatarId());
		addToMap(properties, ContentModel.PROP_SKYPE, getSkypeId());
		addToMap(properties, ContentModel.PROP_INSTANTMSG, getInstantMessageId());
		addToMap(properties, ContentModel.PROP_USER_STATUS, getUserStatus());
		addToMap(properties, ContentModel.PROP_USER_STATUS_TIME, getStatusUpdatedAt());
		addToMap(properties, ContentModel.PROP_GOOGLEUSERNAME, getGoogleId());
		addToMap(properties, ContentModel.PROP_SIZE_QUOTA, getQuota());
		addToMap(properties, ContentModel.PROP_SIZE_CURRENT, getQuotaUsed());
		addToMap(properties, ContentModel.PROP_PERSONDESC, getDescription());
	}
	
}
