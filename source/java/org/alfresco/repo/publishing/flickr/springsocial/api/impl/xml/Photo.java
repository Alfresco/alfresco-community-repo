package org.alfresco.repo.publishing.flickr.springsocial.api.impl.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import org.alfresco.repo.publishing.flickr.springsocial.api.PhotoInfo;

/**
 * 
 * @author Brian
 * @since 4.0
 */
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
