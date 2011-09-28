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
package org.alfresco.repo.publishing.slideshare;

import com.benfante.jslideshare.SlideShareAPI;
import com.benfante.jslideshare.SlideShareErrorException;
import com.benfante.jslideshare.SlideShareException;

/**
 * 
 * @author Brian
 * @since 4.0
 */
public interface SlideShareApi extends SlideShareAPI
{
    String deleteSlideshow(String username, String password, String id) throws SlideShareException,
            SlideShareErrorException;
}
