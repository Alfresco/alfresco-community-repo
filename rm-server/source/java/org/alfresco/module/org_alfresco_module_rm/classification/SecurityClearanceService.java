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

import java.util.List;

import org.alfresco.query.PagingResults;
import org.alfresco.service.cmr.security.NoSuchPersonException;

/**
 * This service offers access to users' security clearance levels.
 *
 * @author Neil Mc Erlean
 * @author David Webster
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
     * @param queryParams parameters for the query.
     * @return security clearances for the specified page of users.
     */
    PagingResults<SecurityClearance> getUsersSecurityClearance(UserQueryParams queryParams);

    /**
     * Check if a classification can be accessed by the current user.
     *
     * @param classificationId The classification level to look for.
     * @return {@code true} if the user can access the classification level; {@code false} if the user doesn't have
     *         clearance, or the classification level doesn't exist.
     */
    boolean isCurrentUserClearedForClassification(String classificationId);

    /**
     * Set the clearance level for a user.
     *
     * @param userName The username of the user.
     * @param clearanceId The identifier for the new clearance level.
     * @return the user's security clearance
     */
    SecurityClearance setUserSecurityClearance(String userName, String clearanceId);

    /**
     * Returns an immutable list of the defined clearance levels.
     *
     * @return clearance levels in descending order from highest to lowest
     * (where fewer users have access to the highest clearance levels
     * and therefore access to the most restricted documents).
     */
    List<ClearanceLevel> getClearanceLevels();

    /**
     * Checks if the current user has any clearance set
     *
     * @return <code>true</code> if the current user has a clearance set different than "No Clearance", <code>false</code> otherwise
     */
    boolean hasCurrentUserClearance();

    /**
     * Checks if the user with the given id has any clearance set
     *
     * @param userId {@link String} The user id
     * @return <code>true</code> if the user with the given id has a clearance set different than "No Clearance", <code>false</code> otherwise
     */
    boolean hasUserClearance(String userId);
}
