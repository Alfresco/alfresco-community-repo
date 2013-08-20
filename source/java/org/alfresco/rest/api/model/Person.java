/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.rest.api.model;

import java.io.Serializable;
import java.util.Date;
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

	public Person()
    {
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
	
}
