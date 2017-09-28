/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.rest.v0;

import org.alfresco.rest.core.v0.BaseAPI;
import org.springframework.stereotype.Component;

/**
 * Helper methods for performing actions on user trashcan
 *
 * @author Oana Nechiforescu
 * @since 2.6
 */
@Component
public class UserTrashcanAPI extends BaseAPI
{
    private static final String EMPTY_TRASHCAN = "{0}archive/workspace/SpacesStore";

    /**
     * Clears the trashcan for the current user
     *
     * @param username the username
     * @param password the password
     *
     * @return true if the request succeeded, false if not
     */
    public boolean emptyTrashcan(String username, String password)
    {
        return doDeleteRequest(username, password, EMPTY_TRASHCAN) != null;
    }

}
