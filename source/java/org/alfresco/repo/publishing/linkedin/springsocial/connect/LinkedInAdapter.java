/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.publishing.linkedin.springsocial.connect;

import org.alfresco.repo.publishing.linkedin.springsocial.api.AlfrescoLinkedIn;
import org.springframework.social.connect.ApiAdapter;
import org.springframework.social.connect.ConnectionValues;
import org.springframework.social.connect.UserProfile;
import org.springframework.social.connect.UserProfileBuilder;
import org.springframework.social.linkedin.api.LinkedInProfile;
import org.springframework.web.client.HttpClientErrorException;

/**
 * 
 * @author Brian
 * @since 4.0
 */
public class LinkedInAdapter implements ApiAdapter<AlfrescoLinkedIn>
{
    public boolean test(AlfrescoLinkedIn linkedin) {
        try {
            linkedin.getUserProfile();
            return true;
        } catch (HttpClientErrorException e) {
            // TODO: Have api throw more specific exception and trigger off of that.
            return false;
        }
    }

    public void setConnectionValues(AlfrescoLinkedIn linkedin, ConnectionValues values) {
        LinkedInProfile profile = linkedin.getUserProfile();
        values.setProviderUserId(profile.getId());
        values.setDisplayName(profile.getFirstName() + " " + profile.getLastName());
        values.setProfileUrl(profile.getPublicProfileUrl());
        values.setImageUrl(profile.getProfilePictureUrl());
    }

    public UserProfile fetchUserProfile(AlfrescoLinkedIn linkedin) {
        LinkedInProfile profile = linkedin.getUserProfile();
        return new UserProfileBuilder().setName(profile.getFirstName() + " " + profile.getLastName()).build();
    }
    
    public void updateStatus(AlfrescoLinkedIn linkedin, String message) {
        // not supported yet
    }
    
}
