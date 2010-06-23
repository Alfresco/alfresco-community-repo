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
package org.alfresco.repo.domain.avm;

import java.util.Map;

/**
 * DAO service to remove AVM locks.
 * 
 * Added here for now, since this is currently an WCM-specific use-case to optimise lock removal
 * based on matching path (bypassing the Attribute Service -> PropVal DAO). See also AbstractPropertyValueDAOImpl.
 * Affected table is currently:
 * 
 *     <b>alf_prop_unique_ctx</b>
 *
 * @author janv
 * @since 3.4
 */
public interface AVMLockDAO
{
    /**
     * Remove all locks for a specific AVM store that start with a given directory path
     * that also optionally match a map of lock data entries.
     * 
     * @param avmStore              the name of the AVM store
     * @param dirPath               optional - start with given directory path or null to match all
     * @param lockDataToMatch       optional - lock data to match (note: all entries must match) or null/empty to match all
     */
    public void removeLocks(String avmStore, String dirPath, final Map<String, String> lockDataToMatch);
}
