/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.classification;

import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.service.cmr.security.NoSuchPersonException;

/**
 * This service offers access to users' security clearance levels.
 *
 * @author Neil Mc Erlean
 * @since 3.0
 */
public interface SecurityClearanceService
{
    /**
     * Get the currently authenticated user's security clearance.
     *
     * @return the security clearance for the currently authenticated user.
     * @throws NoSuchPersonException if the current user's person node cannot be found.
     */
    SecurityClearance getUserSecurityClearance();

    /**
     * Get users' security clearances.
     *
     * @param userNameFragment A username fragment which will be used to apply a 'starts with' query.
     * @param sortAscending if @code true} returns data sorted in ascending order by username.
     * @param req paging request definition.
     * @return security clearances for the specified page of users.
     */
    PagingResults<SecurityClearance> getUsersSecurityClearance(String userNameFragment,
                                                               boolean sortAscending,
                                                               PagingRequest req);
}
