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
