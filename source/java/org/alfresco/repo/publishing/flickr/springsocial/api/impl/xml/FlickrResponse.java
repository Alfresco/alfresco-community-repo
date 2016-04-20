package org.alfresco.repo.publishing.flickr.springsocial.api.impl.xml;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * @author Brian
 * @since 4.0
 */
@XmlRootElement(name = "rsp")
public class FlickrResponse
{
    @XmlAttribute
    public String stat = "ok";
    @XmlAnyElement(lax = true)
    public FlickrPayload payload;

    public String toString()
    {
        return "FlickrResponse[stat=" + stat + ", payload=" + payload + "]";
    }
}
