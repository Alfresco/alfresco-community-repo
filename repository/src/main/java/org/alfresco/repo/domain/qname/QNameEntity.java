/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
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
package org.alfresco.repo.domain.qname;

import org.alfresco.util.EqualsHelper;

/**
 * Entity for <b>alf_qname</b> persistence. 
 * 
 * @author Derek Hulley
 * @since 3.4
 */
public class QNameEntity
{
    public static final String EMPTY_LOCALNAME_SUBSTITUTE = ".empty";
    public static final Long CONST_LONG_ZERO = Long.valueOf(0L);

    private Long id;
    private Long version;
    private Long namespaceId;
    private String localName;
    
    public QNameEntity()
    {
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(512);
        sb.append("QNameEntity")
          .append("[ id=").append(id)
          .append(", nsId=").append(namespaceId)
          .append(", localName=").append(localName)
          .append("]");
        return sb.toString();
    }

    public void incrementVersion()
    {
        if (version >= Short.MAX_VALUE)
        {
            this.version = 0L;
        }
        else
        {
            this.version++;
        }
    }
    
    public Long getId()
    {
        return id;
    }
    public void setId(Long id)
    {
        this.id = id;
    }
    
    public Long getVersion()
    {
        return version;
    }
    public void setVersion(Long version)
    {
        this.version = version;
    }
    
    public Long getNamespaceId()
    {
        return namespaceId;
    }
    public void setNamespaceId(Long namespaceId)
    {
        this.namespaceId = namespaceId;
    }

    public String getLocalName()
    {
        return localName;
    }
    public void setLocalName(String localName)
    {
        this.localName = localName;
    }

    /**
     * Convenience getter to interpret the {@link #EMPTY_LOCALNAME_SUBSTITUTE}
     */
    public String getLocalNameSafe()
    {
        if (EqualsHelper.nullSafeEquals(localName, QNameEntity.EMPTY_LOCALNAME_SUBSTITUTE))
        {
            return "";
        }
        else
        {
            return localName;
        }
    }
    /**
     * Convenience setter to interpret the {@link #EMPTY_LOCALNAME_SUBSTITUTE}
     */
    public void setLocalNameSafe(String localName)
    {
        this.localName = (localName.length() == 0) ? QNameEntity.EMPTY_LOCALNAME_SUBSTITUTE : localName;
    }
}
