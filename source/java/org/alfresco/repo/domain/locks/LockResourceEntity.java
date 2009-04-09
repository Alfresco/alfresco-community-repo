/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.domain.locks;

/**
 * Entity bean for <b>alf_lock_resource</b> table.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class LockResourceEntity
{
    private Long id;
    private Long version;
    private Long qnameNamespaceId;
    private String qnameLocalName;

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

    /**
     * @return                  Returns the ID of the namespace that the lock belongs to
     */
    public Long getQnameNamespaceId()
    {
        return qnameNamespaceId;
    }

    /**
     * @param namespaceId       the ID of the namespace that the lock belongs to
     */
    public void setQnameNamespaceId(Long namespaceId)
    {
        this.qnameNamespaceId = namespaceId;
    }

    /**
     * @return                  Returns the lock qualified name localname
     */
    public String getQnameLocalName()
    {
        return qnameLocalName;
    }

    /**
     * @param qnameLocalName    the lock qualified name localname
     */
    public void setQnameLocalName(String qnameLocalName)
    {
        this.qnameLocalName = qnameLocalName;
    }
}
