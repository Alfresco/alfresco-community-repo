package org.alfresco.repo.publishing.linkedin.springsocial.api.impl.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.alfresco.repo.publishing.linkedin.springsocial.api.ShareVisibility;
import org.alfresco.repo.publishing.linkedin.springsocial.api.ShareVisibilityCode;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "code" })
@XmlRootElement(name = "visibility")
public class JaxbShareVisibilityImpl implements ShareVisibility
{
    @XmlElement(required = true)
    protected ShareVisibilityCode code;

    public JaxbShareVisibilityImpl()
    {
        super();
    }

    public JaxbShareVisibilityImpl(ShareVisibilityCode code)
    {
        super();
        this.code = code;
    }

    public ShareVisibilityCode getCode()
    {
        return code;
    }

    public void setCode(ShareVisibilityCode value)
    {
        this.code = value;
    }

}