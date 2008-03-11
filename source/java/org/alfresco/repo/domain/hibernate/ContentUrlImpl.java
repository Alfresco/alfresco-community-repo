/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
 * http://www.alfresco.com/legal/licensing
 */
package org.alfresco.repo.domain.hibernate;

import java.io.Serializable;

import org.alfresco.repo.domain.ContentUrl;

/**
 * Bean containing all the persistence data representing a <b>Content Url</b>.
 * <p>
 * This implementation of the {@link org.alfresco.repo.domain.Node Node} interface is
 * Hibernate specific.
 * 
 * @author Derek Hulley
 * @since 2.0
 */
public class ContentUrlImpl extends LifecycleAdapter implements ContentUrl, Serializable
{
    private static final long serialVersionUID = -7368859912728834288L;

    private Long id;
    private String contentUrl;
//    private boolean isOrphaned;

    public ContentUrlImpl()
    {
//        isOrphaned = false;
    }

    public Long getId()
    {
        return id;
    }

    /**
     * For Hibernate Use
     */
    @SuppressWarnings("unused")
    private void setId(Long id)
    {
        this.id = id;
    }

    public String getContentUrl()
    {
        return contentUrl;
    }

    public void setContentUrl(String contentUrl)
    {
        this.contentUrl = contentUrl;
    }
//
//    public boolean isOrphaned()
//    {
//        return isOrphaned;
//    }
//
//    public void setOrphaned(boolean isOrphaned)
//    {
//        this.isOrphaned = isOrphaned;
//    }
}
