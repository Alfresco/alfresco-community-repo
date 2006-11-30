/*
 * Copyright (C) 2006 Alfresco, Inc.
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

package org.alfresco.repo.avm;

import java.util.List;

/**
 * DAO for history links.
 * @author britt
 */
public interface HistoryLinkDAO
{
    /**
     * Save and unsaved HistoryLink.
     * @param link
     */
    public void save(HistoryLink link);
    
    /**
     * Get the history link with the given descendent.
     * @param descendent The descendent.
     * @return The HistoryLink or null if not found.
     */
    public HistoryLink getByDescendent(AVMNode descendent);
    
    /**
     * Get all the descendents of a node.
     * @param ancestor The ancestor node.
     * @return A List of AVMNode descendents.
     */
    public List<HistoryLink> getByAncestor(AVMNode ancestor);

    /**
     * Delete a HistoryLink
     * @param link The link to delete.
     */
    public void delete(HistoryLink link);
}
