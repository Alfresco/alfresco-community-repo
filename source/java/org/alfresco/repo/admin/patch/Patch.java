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

import java.util.List;

import org.alfresco.service.cmr.admin.PatchException;

/**
 * A patch is an executable class that makes a change to persisted data.
 * <p>
 * Auditing information is not maintained by the patch - rather it is solely
 * responsible for the execution of the processes necessary to apply the patch.
 * <p>
 * Patches must not be reappliable.  It is up to the patch management systems
 * to ensure that patches are <b>never reapplied</b>.
 * 
 * @see org.alfresco.repo.admin.patch.AbstractPatch
 * @since 1.2
 * @author Derek Hulley
 */
public interface Patch
{
    public String getId();
    
    public String getDescription();
    
    /**
     * @return Returns the smallest schema number that this patch may be applied to
     */
    public int getFixesFromSchema();

    /**
     * @return Returns the largest schema number that this patch may be applied to
     */
    public int getFixesToSchema();
    
    /**
     * @return Returns the schema number that this patch attempts to bring the repo up to
     */
    public int getTargetSchema();

    /**
     * Get patches that this patch depends on
     * 
     * @return Returns a list of patches
     */
    public List<Patch> getDependsOn();
    
    /**
     * Check if the patch is applicable to a given schema version.
     * 
     * @param version a schema version number
     * @return Returns <code>(fixesFromVersion <= version <= fixesToVersion)</code>
     */
    public boolean applies(int version);
    
    /**
     * Applies the patch.  Typically this will be within the bounds of a new
     * transaction.
     * 
     * @return Returns the patch execution report
     * @throws PatchException if the patch failed to be applied
     */
    public String apply() throws PatchException;
}
