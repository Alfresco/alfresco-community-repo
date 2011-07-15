/*
 * Copyright 2010 the original author or authors.
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
