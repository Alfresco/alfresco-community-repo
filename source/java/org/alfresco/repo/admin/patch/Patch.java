/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
