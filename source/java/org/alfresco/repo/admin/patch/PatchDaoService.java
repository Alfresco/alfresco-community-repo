/*
 * Copyright (C) 2005 Alfresco, Inc.
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
package org.alfresco.repo.admin.patch;

import java.util.Date;
import java.util.List;

import org.alfresco.repo.domain.AppliedPatch;

/**
 * Provides data access support for patch persistence. 
 * 
 * @since 1.2
 * @author Derek Hulley
 */
public interface PatchDaoService
{
    /**
     * Creates and saves a new instance of the patch.  This will not have all the mandatory
     * properties set - only the ID.
     * 
     * @param id the unique key
     * @return Returns a new instance that can be manipulated
     */
    public AppliedPatch newAppliedPatch(String id);
    
    /**
     * Retrieve an existing patch
     * 
     * @param id the patch unique ID
     * @return Returns the patch instance or null if one has not been persisted
     */
    public AppliedPatch getAppliedPatch(String id);
    
    /**
     * Detaches the given instance from the persistence engine.  This will
     * ensure that any changes made to the java object do not get persisted,
     * allowing the objects to be passed out to external clients without any
     * concern of their lifecycle.
     * 
     * @param appliedPatch the object to detach from persistence
     */
    public void detach(AppliedPatch appliedPatch);
    
    /**
     * Get a list of all applied patches
     * 
     * @return Returns a list of all applied patches
     */
    public List<AppliedPatch> getAppliedPatches();
    
    /**
     * Get a list of all patches applied between the given dates.
     * 
     * @param from the lower date limit or null to ignore
     * @param to the upper date limit or null to ignore
     * @return Returns applied patches for the date range, but also patches without
     *      a date
     */
    public List<AppliedPatch> getAppliedPatches(Date from, Date to);
}
