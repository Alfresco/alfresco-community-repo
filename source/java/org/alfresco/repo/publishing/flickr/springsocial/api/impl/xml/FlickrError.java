package org.alfresco.repo.publishing.flickr.springsocial.api.impl.xml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * @author Brian
 * @since 4.0
 */

@XmlRootElement(name = "err")
public class FlickrError implements FlickrPayload
{
    @XmlAttribute
    public String code;

    @XmlAttribute
    public String msg;
}
