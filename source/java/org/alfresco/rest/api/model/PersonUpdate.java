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

import org.alfresco.model.ContentModel;
import org.alfresco.rest.framework.resource.UniqueId;
import org.alfresco.service.namespace.QName;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Create or update a person.
 *
 * @since 5.2
 * @author Matt Ward
 */
public class PersonUpdate
{
    protected final String userName;
    protected final String firstName;
    protected final String lastName;
    protected final String description;
    protected final String email;
    protected final String skypeId;
    protected final String googleId;
    protected final String instantMessageId;
    protected final String jobTitle;
    protected final String location;
    protected final Company company;
    protected final String mobile;
    protected final String telephone;
    protected final String userStatus;
    protected final Boolean enabled;
    protected final Boolean emailNotificationsEnabled;


    private PersonUpdate(
            String userName,
            String firstName,
            String lastName,
            String description,
            String email,
            String skypeId,
            String googleId,
            String instantMessageId,
            String jobTitle,
            String location,
            Company company,
            String mobile,
            String telephone,
            String userStatus,
            Boolean enabled,
            Boolean emailNotificationsEnabled)
    {
        this.userName = userName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.description = description;
        this.email = email;
        this.skypeId = skypeId;
        this.googleId = googleId;
        this.instantMessageId = instantMessageId;
        this.jobTitle = jobTitle;
        this.location = location;
        this.company = company;
        this.mobile = mobile;
        this.telephone = telephone;
        this.userStatus = userStatus;
        this.enabled = enabled;
        this.emailNotificationsEnabled = emailNotificationsEnabled;
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

    public String getFirstName()
    {
        return firstName;
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

    public Boolean isEmailNotificationsEnabled()
    {
        return emailNotificationsEnabled;
    }

    @Override
    public String toString()
    {
        return "Person [userName=" + userName
                + ", enabled=" + enabled
                + ", firstName=" + firstName
                + ", lastName=" + lastName
                + ", jobTitle=" + jobTitle
                + ", location=" + location
                + ", telephone=" + telephone
                + ", mobile=" + mobile
                + ", email=" + email
                + ", skypeId=" + skypeId
                + ", instantMessageId=" + instantMessageId
                + ", userStatus=" + userStatus
                + ", googleId=" + googleId
                + ", emailNotificationsEnabled=" + emailNotificationsEnabled
                + ", description=" + description
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
        // What's the correct behaviour here? Store it as "content" somehow?
        // so that it can be 'inlined' by the code in PeopleImpl.processPersonProperties ?
        addToMap(properties, ContentModel.PROP_PERSONDESC, getDescription());
        addToMap(properties, ContentModel.PROP_EMAIL, getEmail());
        addToMap(properties, ContentModel.PROP_SKYPE, getSkypeId());
        addToMap(properties, ContentModel.PROP_GOOGLEUSERNAME, getGoogleId());
        addToMap(properties, ContentModel.PROP_INSTANTMSG, getInstantMessageId());
        addToMap(properties, ContentModel.PROP_JOBTITLE, getJobTitle());
        addToMap(properties, ContentModel.PROP_LOCATION, getLocation());

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

        addToMap(properties, ContentModel.PROP_MOBILE, getMobile());
        addToMap(properties, ContentModel.PROP_TELEPHONE, getTelephone());
        addToMap(properties, ContentModel.PROP_USER_STATUS, getUserStatus());
        addToMap(properties, ContentModel.PROP_EMAIL_FEED_DISABLED,
                isEmailNotificationsEnabled() != null ? !isEmailNotificationsEnabled() : null);

    }

    public static class Builder
    {
        private String userName; // is "id" in request JSON
        private String firstName;
        private String lastName;
        private String description;
        private String email;
        private String skypeId;
        private String googleId;
        private String instantMessageId;
        private String jobTitle;
        private String location;
        private Company company;
        private String mobile;
        private String telephone;
        private String userStatus;
        private Boolean enabled;
        private Boolean emailNotificationsEnabled;

        public Builder id(String userId)
        {
            this.userName = userId;
            return this;
        }

        public Builder firstName(String fn)
        {
            this.firstName = fn;
            return this;
        }

        public Builder lastName(String ln)
        {
            this.lastName = ln;
            return this;
        }

        public Builder description(String description)
        {
            this.description = description;
            return this;
        }

        public Builder email(String email)
        {
            this.email = email;
            return this;
        }

        public Builder skypeId(String skypeId)
        {
            this.skypeId = skypeId;
            return this;
        }

        public Builder googleId(String googleId)
        {
            this.googleId = googleId;
            return this;
        }

        public Builder instantMessageId(String instantMessageId)
        {
            this.instantMessageId = instantMessageId;
            return this;
        }

        public Builder jobTitle(String jobTitle)
        {
            this.jobTitle = jobTitle;
            return this;
        }

        public Builder location(String location)
        {
            this.location = location;
            return this;
        }

        public Builder company(Company company)
        {
            this.company = company;
            return this;
        }

        public Builder mobile(String mobile)
        {
            this.mobile = mobile;
            return this;
        }

        public Builder telephone(String telephone)
        {
            this.telephone = telephone;
            return this;
        }

        public Builder userStatus(String userStatus)
        {
            this.userStatus = userStatus;
            return this;
        }

        public Builder enabled(Boolean enabled)
        {
            this.enabled = enabled;
            return this;
        }

        public Builder emailNotificationsEnabled(Boolean emailNotificationsEnabled)
        {
            this.emailNotificationsEnabled = emailNotificationsEnabled;
            return this;
        }

        public PersonUpdate build()
        {
            return new PersonUpdate(
                    userName,
                    firstName,
                    lastName,
                    description,
                    email,
                    skypeId,
                    googleId,
                    instantMessageId,
                    jobTitle,
                    location,
                    company,
                    mobile,
                    telephone,
                    userStatus,
                    enabled,
                    emailNotificationsEnabled
            );
        }
    }
}
