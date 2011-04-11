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
     * @return              Returns <tt>true</tt> if the patch must forcefully run regardless of any other state
     */
    public boolean isForce();
    
    /**
     * Get patches that this patch depends on
     * 
     * @return Returns a list of patches
     */
    public List<Patch> getDependsOn();
    
    /**
     * Get patches that could have done the work already
     * 
     * @return Returns a list of patches
     */
    public List<Patch> getAlternatives();
    
    /**
     * Check if the patch is applicable to a given schema version.
     * 
     * @param version a schema version number
     * @return Returns <code>(fixesFromVersion <= version <= fixesToVersion)</code>
     */
    public boolean applies(int version);
    
    /**
     * Does the patch need to be wrapped in a transaction?
     * 
     * @return Returns true if the patch needs to be wrapped, false otherwise
     */
    public boolean requiresTransaction();
    
    /**
     * Applies the patch.  Typically this will be within the bounds of a new
     * transaction.
     * 
     * @return Returns the patch execution report
     * @throws PatchException if the patch failed to be applied
     */
    public String apply() throws PatchException;
}
