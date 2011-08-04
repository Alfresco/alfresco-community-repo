package org.alfresco.repo.publishing.linkedin.springsocial.api.impl.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.alfresco.repo.publishing.linkedin.springsocial.api.Share;
import org.alfresco.repo.publishing.linkedin.springsocial.api.ShareVisibility;
import org.alfresco.repo.publishing.linkedin.springsocial.api.ShareVisibilityCode;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "comment", "visibility" })
@XmlRootElement(name = "share")
public class JaxbShareImpl implements Share
{
    @XmlElement(required = true, name = "comment")
    protected String comment;

    @XmlElement(required = true, name = "visibility")
    protected JaxbShareVisibilityImpl visibility = new JaxbShareVisibilityImpl(ShareVisibilityCode.ANYONE);

    public String getComment()
    {
        return comment;
    }

    public void setComment(String comment)
    {
        this.comment = comment;
    }

    public ShareVisibility getVisibility()
    {
        return visibility;
    }

    public void setVisibility(ShareVisibility visibility)
    {
        this.visibility = new JaxbShareVisibilityImpl(visibility.getCode());
    }

}
