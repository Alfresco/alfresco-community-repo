/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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

package org.alfresco.service.cmr.site;

import org.alfresco.api.AlfrescoPublicApi;

/**
 * Site member's information. The member can either be an individual or a group.
 * 
 * @author Jamal Kaabi-Mofrad
 * @since odin
 */
@AlfrescoPublicApi
public interface SiteMemberInfo
{

    /**
     * Get the member's name. The name can either be the name of an individual
     * or a group
     * 
     * @return String member's name
     */
    public String getMemberName();


    /**
     * Get the member's role
     * 
     * @return String member's role
     */
    public String getMemberRole();


    /**
     * Indicates whether a member belongs to a group with access rights to the
     * site or not
     * 
     * @return <tt>true</tt> if the member belongs to a group with access
     *         rights, otherwise <tt>false</tt>
     */
    public boolean isMemberOfGroup();

}
