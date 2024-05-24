/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2024 Alfresco Software Limited
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
package org.alfresco.repo.sa;

import java.util.Optional;
import java.util.Set;

/**
 * A service account registry that allows service accounts to be registered
 * with their corresponding roles.
 *
 * @author Jamal Kaabi-Mofrad
 */
public interface ServiceAccountRegistry
{
    /**
     * Registers a service account with its corresponding role.
     *
     * @param serviceAccountName The name of the service account to be registered.
     * @param serviceAccountRole The role of the service account to be registered.
     */
    void register(String serviceAccountName, String serviceAccountRole);

    /**
     * Retrieves the role of a specific service account.
     *
     * @param serviceAccountName The name of the service account.
     * @return An Optional containing the role of the service account if it exists, otherwise an empty Optional.
     */
    Optional<String> getServiceAccountRole(String serviceAccountName);

    /**
     * Retrieves the names of all service accounts.
     *
     * @return A set of service account names. If no service accounts are present, an empty set is returned.
     */
    Set<String> getServiceAccountNames();
}
