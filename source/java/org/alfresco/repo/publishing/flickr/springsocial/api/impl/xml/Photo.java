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
package org.alfresco.repo.publishing.flickr.springsocial.api.impl.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import org.alfresco.repo.publishing.flickr.springsocial.api.PhotoInfo;

@XmlRootElement(name = "photo")
public class Photo implements FlickrPayload, PhotoInfo
{
    @XmlAttribute
    public String id;

    @XmlElement(name = "urls")
    public UrlList urlList = new UrlList();

    public static class UrlList
    {
        @XmlElement(name = "url")
        public List<PhotoUrl> urls = new ArrayList<PhotoUrl>();
    }

    public static class PhotoUrl
    {
        @XmlAttribute
        public String type;
        @XmlValue
        public String url;
    }

    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public String getPrimaryUrl()
    {
        return urlList.urls.isEmpty() ? null : urlList.urls.get(0).url;
    }
}
