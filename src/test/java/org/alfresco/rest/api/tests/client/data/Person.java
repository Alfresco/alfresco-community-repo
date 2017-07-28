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
package org.alfresco.rest.api.tests.client.data;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.text.Collator;
import java.util.*;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.rest.api.tests.QueriesPeopleApiTest;
import org.alfresco.rest.api.tests.client.PublicApiClient.ExpectedPaging;
import org.alfresco.rest.api.tests.client.PublicApiClient.ListResponse;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Person
    extends org.alfresco.rest.api.model.Person
        implements Serializable, Comparable<Person>, ExpectedComparison
{
    private static final long serialVersionUID = 3185698391792389751L;

    private String id;

    private static Collator collator = Collator.getInstance();

    public Person()
    {
        super();
    }
    
    public Person(String id, String username, Boolean enabled, String firstName, String lastName,
            Company company, String skype, String location, String tel,
            String mob, String instantmsg, String google, String description)
    {
        super(username, enabled, null, firstName, lastName, null, location, tel, mob, null, skype, instantmsg, null, null, google, null, null, null, description, company);
        this.id = id;
    }

    public Person(String userName,
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
                  org.alfresco.rest.api.model.Company company,
                  Map<String, Object> properties,
                  List<String> aspectNames)
    {
        super(userName,
                enabled,
                avatarId,
                firstName,
                lastName,
                jobTitle,
                location,
                telephone,
                mobile,
                email,
                skypeId,
                instantMessageId,
                userStatus,
                statusUpdatedAt,
                googleId,
                quota,
                quotaUsed,
                emailNotificationsEnabled,
                description,
                company);
        this.id = userName;
        this.properties = properties;
        this.aspectNames = aspectNames;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    /**
     * Note: used for string comparisons in tests.
     *
     * @see QueriesPeopleApiTest#checkApiCall(java.lang.String, java.lang.String, java.lang.String, org.alfresco.rest.api.tests.client.PublicApiClient.Paging, int, java.lang.String[])
     */
    @Override
    public String toString()
    {
        return "Person [" + (id != null ? "id=" + id + ", " : "")
                + (enabled != null ? "enabled=" + enabled + ", " : "")
                + (firstName != null ? "firstName=" + firstName + ", " : "")
                + (lastName != null ? "lastName=" + lastName + ", " : "")
                + (company != null ? "company=" + company + ", " : "company=" + new Company().toString() + ", ")
                + (skypeId != null ? "skype=" + skypeId + ", " : "")
                + (location != null ? "location=" + location + ", " : "")
                + (telephone != null ? "tel=" + telephone + ", " : "")
                + (mobile != null ? "mob=" + mobile + ", " : "")
                + (instantMessageId != null ? "instantmsg=" + instantMessageId + ", " : "")
                + (googleId != null ? "google=" + googleId + ", " : "")
                + (description != null ? "description=" + description + ", " : "")
                + "]";
    }

    @SuppressWarnings("unchecked")
    public JSONObject toJSON(boolean fullVisibility)
    {
        JSONObject personJson = new JSONObject();

        if (getUserName() != null)
        {
            personJson.put("id", getUserName());
        }
        personJson.put("firstName", getFirstName());
        personJson.put("lastName", getLastName());

        if(fullVisibility)
        {
            personJson.put("description", getDescription());
            personJson.put("email", getEmail());
            personJson.put("skypeId", getSkypeId());
            personJson.put("googleId", getGoogleId());
            personJson.put("instantMessageId", getInstantMessageId());
            personJson.put("jobTitle", getJobTitle());
            personJson.put("location", getLocation());
            if (company != null)
            {
                personJson.put("company", new Company(company).toJSON());
            }
            personJson.put("mobile", getMobile());
            personJson.put("telephone", getTelephone());
            personJson.put("userStatus", getUserStatus());
            personJson.put("enabled", isEnabled());
            personJson.put("emailNotificationsEnabled", isEmailNotificationsEnabled());
            personJson.put("properties", getProperties());
            personJson.put("aspectNames", getAspectNames());
        }
        return personJson;
    }
    
    public static Person parsePerson(JSONObject jsonObject)
    {
        String userId = (String)jsonObject.get("id");
        String firstName = (String)jsonObject.get("firstName");
        String lastName = (String)jsonObject.get("lastName");

        String description = (String)jsonObject.get("description");
        String email = (String) jsonObject.get("email");
        String skypeId = (String)jsonObject.get("skypeId");
        String googleId = (String)jsonObject.get("googleId");
        String instantMessageId = (String)jsonObject.get("instantMessageId");
        String jobTitle = (String) jsonObject.get("jobTitle");
        String location = (String)jsonObject.get("location");

        Company company = null;
        JSONObject companyJSON = (JSONObject)jsonObject.get("company");
        if(companyJSON != null)
        {
            String organization = (String)companyJSON.get("organization");
            String address1 = (String)companyJSON.get("address1");
            String address2 = (String)companyJSON.get("address2");
            String address3 = (String)companyJSON.get("address3");
            String postcode = (String)companyJSON.get("postcode");
            String companyTelephone = (String)companyJSON.get("telephone");
            String fax = (String)companyJSON.get("fax");
            String companyEmail = (String)companyJSON.get("email");
            if (organization != null ||
                    address2 != null ||
                    address3 != null ||
                    postcode != null ||
                    companyTelephone != null ||
                    fax != null ||
                    companyEmail != null)
            {
                company = new Company(organization, address1, address2, address3, postcode, companyTelephone, fax, companyEmail);
            }
            else
            {
                company = new Company();
            }
        }

        String mobile = (String)jsonObject.get("mobile");
        String telephone = (String)jsonObject.get("telephone");
        String userStatus = (String) jsonObject.get("userStatus");
        Boolean enabled = (Boolean)jsonObject.get("enabled");
        Boolean emailNotificationsEnabled = (Boolean) jsonObject.get("emailNotificationsEnabled");
        List<String> aspectNames = (List<String>) jsonObject.get("aspectNames");
        Map<String, Object> properties = (Map<String, Object>) jsonObject.get("properties");
        
        Person person = new Person(
                userId,
                enabled,
                null, // avatarId is not accepted by "create person"
                firstName,
                lastName,
                jobTitle,
                location,
                telephone,
                mobile,
                email,
                skypeId,
                instantMessageId,
                userStatus,
                null, // userStatusUpdateAt is read only - not used in create person
                googleId,
                null, // quota - not used in create person
                null, // quotaUsers - not used
                emailNotificationsEnabled,
                description,
                company,
                properties,
                aspectNames
        );
        return person;
    }


    private static class UserContext
    {
        private String networkId;
        private String personId;

        UserContext(String networkId, String personId)
        {
            super();
            this.networkId = networkId;
            this.personId = personId;
        }

        String getNetworkId()
        {
            return networkId;
        }

        String getPersonId()
        {
            return personId;
        }
    }

    private static ThreadLocal<UserContext> userContext = new ThreadLocal<UserContext>();
    public static void setUserContext(String personId)
    {
        String networkId = Person.getNetworkId(personId);
        userContext.set(new UserContext(networkId, personId));
    }
    
    public static void clearUserContext()
    {
        userContext.set(null);
    }
    
    public static UserContext gettUserContext()
    {
        return userContext.get();
    }
    
    public static String getNetworkId(String personId)
    {
        int idx = personId.indexOf("@");
        return(idx == -1 ? TenantService.DEFAULT_DOMAIN : personId.substring(idx + 1));
    }

    private String getNetworkId()
    {
        return Person.getNetworkId(id);
    }
    
    public boolean isVisible()
    {
        boolean ret = true;

        UserContext uc = gettUserContext();
        String networkId = getNetworkId();
        if(uc != null)
        {
            if(!networkId.equals(uc.getNetworkId()))
            {
                ret = false;
            }
        }

        return ret;
    }
    
    @Override
    public void expected(Object o)
    {
        assertTrue("o is an instance of " + o.getClass(), o instanceof Person);

        Person other = (Person)o;
        
        AssertUtil.assertEquals("userId", getId(), other.getId());
        AssertUtil.assertEquals("firstName", firstName, other.getFirstName());
        AssertUtil.assertEquals("lastName", lastName, other.getLastName());
        AssertUtil.assertEquals("enabled", enabled, other.isEnabled());

        if(isVisible())
        {
            AssertUtil.assertEquals("skype", getSkypeId(), other.getSkypeId());
            AssertUtil.assertEquals("location", getLocation(), other.getLocation());
            AssertUtil.assertEquals("tel", getTelephone(), other.getTelephone());
            AssertUtil.assertEquals("mobile", getMobile(), other.getMobile());
            AssertUtil.assertEquals("instanceMessageId", getInstantMessageId(), other.getInstantMessageId());
            AssertUtil.assertEquals("googleId", getGoogleId(), other.getGoogleId());
            if(company != null)
            {
                new Company(company).expected(getCompany());
            }
        }
    }

    @Override
    public int compareTo(Person o)
    {
        int ret = Person.collator.compare(lastName, o.getLastName());
        if(ret == 0)
        {
            ret = Person.collator.compare(firstName, o.getFirstName());
        }
        return ret;
    }

    public static ListResponse<Person> parsePeople(JSONObject jsonObject)
    {
        List<Person> people = new ArrayList<Person>();

        JSONObject jsonList = (JSONObject)jsonObject.get("list");
        assertNotNull(jsonList);

        JSONArray jsonEntries = (JSONArray)jsonList.get("entries");
        assertNotNull(jsonEntries);

        for(int i = 0; i < jsonEntries.size(); i++)
        {
            JSONObject jsonEntry = (JSONObject)jsonEntries.get(i);
            JSONObject entry = (JSONObject)jsonEntry.get("entry");
            people.add(parsePerson(entry));
        }

        ExpectedPaging paging = ExpectedPaging.parsePagination(jsonList);
        ListResponse<Person> resp = new ListResponse<Person>(paging, people);
        return resp;
    }
    
}
