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
