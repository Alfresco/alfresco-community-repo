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
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.admin.patch;

import java.util.Date;
import java.util.List;

/**
 * Manages patches applied against the repository.
 * <p>
 * Patches are injected into this class and any attempted applications are recorded
 * for later auditing.
 * 
 * @since 1.2
 * @author Derek Hulley
 */
public interface PatchService
{
    /**
     * Registers a patch with the service that executes them.
     * 
     * @param patch the patch to register
     */
    public void registerPatch(Patch patch);
    
    /**
     * Apply all outstanding patches that are relevant to the repo.
     * If there is a failure, then the patches that were applied will remain so,
     * but the process will not attempt to apply any further patches.
     * 
     * @return Returns true if all outstanding patches were applied, or false if the process
     *      was termintated before all patches could be applied.
     */
    public boolean applyOutstandingPatches();
    
    /**
     * Retrieves all applied patches between two specific times.
     * 
     * @param from the start date of the search, or null to get all patches from the start
     * @param to the end date of the search, or null to g
     * @return Returns all applied patches (successful or not)
     */
    public List<PatchInfo> getPatches(Date fromDate, Date toDate);
}
