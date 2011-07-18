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
package org.alfresco.repo.publishing.flickr.springsocial.api;

import org.alfresco.repo.publishing.flickr.springsocial.api.MediaOperations;
import org.alfresco.repo.publishing.flickr.springsocial.api.impl.FlickrTemplate;
import org.springframework.social.ApiBinding;

/**
 * Interface specifying a basic set of operations for interacting with Facebook.
 * Implemented by {@link FlickrTemplate}.
 * 
 * @author Craig Walls
 */
public interface Flickr extends ApiBinding
{
    boolean test();
    
    /**
     * API for performing operations on albums, photos, and videos.
     */
    MediaOperations mediaOperations();
}
