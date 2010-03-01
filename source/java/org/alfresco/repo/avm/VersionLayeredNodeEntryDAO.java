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

package org.alfresco.repo.avm;

import java.util.List;

/**
 * DAO interface for VersionLayeredNodeEntries.
 * @author britt
 */
public interface VersionLayeredNodeEntryDAO
{
    /**
     * Save a newly created one.
     * @param entry
     */
    public void save(VersionLayeredNodeEntry entry);
    
    /**
     * Get all entries for a given version.
     * @param version
     * @return
     */
    public List<VersionLayeredNodeEntry> get(VersionRoot version);
    
    /**
     * Delete all entries for the given version.
     * @param version
     */
    public void delete(VersionRoot version);
}
