/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.rest.api.model;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Representation of a document node.
 * 
 * @author steveglover
 * @author janv
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Document extends Node
{
    public Document() {
        super();
    }

    public Document(NodeRef nodeRef, NodeRef parentNodeRef, Map<QName, Serializable> nodeProps, Map<String, UserInfo> mapUserInfo, ServiceRegistry sr)
    {
        super(nodeRef, parentNodeRef, nodeProps, mapUserInfo, sr);

        Serializable val = nodeProps.get(ContentModel.PROP_CONTENT);

        if ((val != null) && (val instanceof ContentData)) {
            ContentData cd = (ContentData)val;
            String mimeType = cd.getMimetype();
            String mimeTypeName = sr.getMimetypeService().getDisplaysByMimetype().get(mimeType);
            contentInfo = new ContentInfo(mimeType, mimeTypeName, cd.getSize(), cd.getEncoding());
        }

        this.isFolder = false;
    }

    @Override
    public String toString()
    {
        return "Document [contentInfo=" + contentInfo.toString() + ", nodeRef="
                    + nodeRef + ", name=" + name + ", createdAt=" + createdAt
                    + ", modifiedAt=" + modifiedAt + ", createdBy=" + createdBy
                    + ", modifiedBy=" + modifiedBy + "]";
    }

    // TODO for backwards compat' - set explicitly when needed (ie. favourites)
    private String mimeType;
    private BigInteger sizeInBytes;
    private String versionLabel;

    /**
     * @deprecated
     */
    public String getMimeType()
    {
        return mimeType;
    }

    /**
     * @deprecated
     */
    public BigInteger getSizeInBytes()
    {
        return sizeInBytes;
    }

    /**
     * @deprecated
     */
    public String getVersionLabel()
    {
        return versionLabel;
    }

    /**
     * @deprecated
     */
    public void setMimeType(String mimeType)
    {
        this.mimeType = mimeType;
    }

    /**
     * @deprecated
     */
    public void setSizeInBytes(BigInteger sizeInBytes)
    {
        this.sizeInBytes = sizeInBytes;
    }

    /**
     * @deprecated
     */
    public void setVersionLabel(String versionLabel)
    {
        this.versionLabel = versionLabel;
    }

}
