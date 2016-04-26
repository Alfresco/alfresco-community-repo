package org.alfresco.repo.publishing.flickr.springsocial.connect;

import org.alfresco.repo.publishing.flickr.springsocial.api.Flickr;
import org.springframework.social.ApiException;
import org.springframework.social.connect.ApiAdapter;
import org.springframework.social.connect.ConnectionValues;
import org.springframework.social.connect.UserProfile;
import org.springframework.social.connect.UserProfileBuilder;

/**
 * 
 * @author Brian
 * @since 4.0
 */
public class FlickrAdapter implements ApiAdapter<Flickr>
{

    public boolean test(Flickr flickr)
    {
        try
        {
            flickr.test();
            return true;
        }
        catch (ApiException e)
        {
            return false;
        }
    }

    public void setConnectionValues(Flickr flickr, ConnectionValues values)
    {
    }

    public UserProfile fetchUserProfile(Flickr flickr)
    {
        return new UserProfileBuilder().setName("Brian").setFirstName("Brian").setLastName(
                "Brian").setEmail("Brian").setUsername("Brian").build();
    }

    public void updateStatus(Flickr flickr, String message)
    {
    }

}
