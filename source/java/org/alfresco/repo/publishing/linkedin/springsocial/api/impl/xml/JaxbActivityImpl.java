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
package org.alfresco.repo.publishing.linkedin.springsocial.api.impl.xml;

import java.util.Locale;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.alfresco.repo.publishing.linkedin.springsocial.api.Activity;

/**
 * 
 * @author Brian
 * @since 4.0
 */
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
