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

import org.alfresco.service.cmr.admin.PatchInfo;

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
     * Set the complete list of patches.  All patch IDs must remain static for the duration their
     * existence.  This allows us to recognise the 
     * 
     * @param patches the complete list of patches (either applied or not)
     */
    public void setPatches(List<Patch> patches);
    
    /**
     * Get a list of all previously applied patches
     * 
     * @return Returns a list of patch application information
     */
    public List<PatchInfo> getAppliedPatches();
    
    /**
     * Apply all outstanding patches that are relevant to the repo.
     * If there is a failure, then the patches that were applied will remain so,
     * but the process will not attempt to apply any further patches.
     * <p>
     * Patches have a version, e.g. <b>1.1.1</b>, which is the version of the repo
     * after which the patch should be applied.  If the repository version is <b>1.1.2</b>
     * then the patch will not be applied as the repository was created with a newer version
     * of the codebase, thereby rendering the patch unecessary.  If the repository is at
     * version <b>1.1</b> then the patch needs to be applied as the codebase of the repository
     * is newer than the repository creation.
     * 
     * @return Returns true if all outstanding patches were applied, or false if the process
     *      was termintated before all patches could be applied.
     */
    public boolean applyOutstandingPatches();
}
