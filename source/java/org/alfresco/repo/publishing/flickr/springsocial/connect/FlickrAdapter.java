/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.alfresco.repo.publishing.flickr.springsocial.connect;

import org.alfresco.repo.publishing.flickr.springsocial.api.Flickr;
import org.springframework.social.ApiException;
import org.springframework.social.connect.ApiAdapter;
import org.springframework.social.connect.ConnectionValues;
import org.springframework.social.connect.UserProfile;
import org.springframework.social.connect.UserProfileBuilder;

/**
 * Facebook ApiAdapter implementation.
 * 
 * @author Keith Donald
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

    public void setConnectionValues(Flickr facebook, ConnectionValues values)
    {
    }

    public UserProfile fetchUserProfile(Flickr facebook)
    {
        return new UserProfileBuilder().setName("Brian").setFirstName("Brian").setLastName(
                "Brian").setEmail("Brian").setUsername("Brian").build();
    }

    public void updateStatus(Flickr facebook, String message)
    {
    }

}
