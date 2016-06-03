package org.alfresco.repo.publishing.flickr.springsocial.api.impl.xml;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

/**
 * 
 * @author Brian
 * @since 4.0
 */
@XmlRootElement(name = "photoid")
public class PhotoId implements FlickrPayload
{
    @XmlValue
    public String id;
}
