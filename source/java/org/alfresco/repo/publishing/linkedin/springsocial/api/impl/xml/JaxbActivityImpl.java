package org.alfresco.repo.publishing.linkedin.springsocial.api.impl.xml;

import java.util.Locale;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.alfresco.repo.publishing.linkedin.springsocial.api.Activity;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"contentType", "body"})
@XmlRootElement(name = "activity")
public class JaxbActivityImpl implements Activity
{
    @XmlElement(name = "content-type")
    protected String contentType = "linkedin-html";
    @XmlElement(required = true)
    protected String body;
    @XmlAttribute(required = true)
    protected String locale = Locale.getDefault().toString();

    public String getContentType()
    {
        return contentType;
    }

    public void setContentType(String value)
    {
        this.contentType = value;
    }

    public String getBody()
    {
        return body;
    }

    public void setBody(String value)
    {
        this.body = value;
    }

    public String getLocale()
    {
        return locale;
    }

    public void setLocale(String value)
    {
        this.locale = value;
    }
}
