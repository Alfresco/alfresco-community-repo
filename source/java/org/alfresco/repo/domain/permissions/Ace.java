/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.domain.permissions;

import org.alfresco.repo.security.permissions.ACEType;


/**
 * Entity for <b>alf_access_control_entry</b> persistence.
 * 
 * @author janv
 * @since 3.4
 */
public interface Ace
{
    public Long getId();
    public Long getPermissionId();
    public Long getAuthorityId();
    public boolean isAllowed();
    public Integer getApplies();
    public Long getContextId();
    public ACEType getAceType();
}
