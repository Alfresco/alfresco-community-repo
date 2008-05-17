/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.person;

import java.net.URL;

/**
 * @author Glen Johnson
 */
public class PersonDetails
{
    private String userName;
    private String title;
    private String firstName;
    private String lastName;
    private String organisation;
    private String jobTitle;
    private String emailAddress;
    private String bio;
    private URL avatarUrl;  
   
    public PersonDetails(String userName, String title, String firstName, String lastName, String organisation,
            String jobTitle, String emailAddress, String bio, URL avatarUrl)
    {
        this.userName = userName;
        this.title = title;
        this.firstName = firstName;
        this.lastName = lastName;
        this.organisation = organisation;
        this.jobTitle = jobTitle;
        this.emailAddress = emailAddress;
        this.bio = bio;
        this.avatarUrl = avatarUrl;
    }

    /**
     * @return the userName
     */
    public String getUserName()
    {
        return userName;
    }

    /**
     * @param userName the userName to set
     */
    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    /**
     * @return the title
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title)
    {
        this.title = title;
    }

    /**
     * @return the firstName
     */
    public String getFirstName()
    {
        return firstName;
    }

    /**
     * @param firstName the firstName to set
     */
    public void setFirstName(String firstName)
    {
        this.firstName = firstName;
    }

    /**
     * @return the lastName
     */
    public String getLastName()
    {
        return lastName;
    }

    /**
     * @param lastName the lastName to set
     */
    public void setLastName(String lastName)
    {
        this.lastName = lastName;
    }

    /**
     * @return the organisation
     */
    public String getOrganisation()
    {
        return organisation;
    }

    /**
     * @param organisation the organisation to set
     */
    public void setOrganisation(String organisation)
    {
        this.organisation = organisation;
    }

    /**
     * @return the jobTitle
     */
    public String getJobTitle()
    {
        return jobTitle;
    }

    /**
     * @param jobTitle the jobTitle to set
     */
    public void setJobTitle(String jobTitle)
    {
        this.jobTitle = jobTitle;
    }

    /**
     * @return the emailAddress
     */
    public String getEmailAddress()
    {
        return emailAddress;
    }

    /**
     * @param emailAddress the emailAddress to set
     */
    public void setEmailAddress(String emailAddress)
    {
        this.emailAddress = emailAddress;
    }

    /**
     * @return the bio
     */
    public String getBio()
    {
        return bio;
    }

    /**
     * @param bio the bio to set
     */
    public void setBio(String bio)
    {
        this.bio = bio;
    }

    /**
     * @return the avatarUrl
     */
    public URL getAvatarUrl()
    {
        return avatarUrl;
    }

    /**
     * @param avatarUrl the avatarUrl to set
     */
    public void setAvatarUrl(URL avatarUrl)
    {
        this.avatarUrl = avatarUrl;
    }
}
