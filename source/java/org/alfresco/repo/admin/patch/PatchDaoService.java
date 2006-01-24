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
     * Get a list of all patches applied between the given dates
     * 
     * @param from the lower date limit or null to ignore
     * @param to the upper date limit or null to ignore
     * @return Returns all applied patches
     */
    public List<AppliedPatch> getAppliedPatches(Date from, Date to);
}
