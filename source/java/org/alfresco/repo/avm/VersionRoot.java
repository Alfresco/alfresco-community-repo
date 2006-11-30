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

/**
 * Represents a single version root.
 * @author britt
 */
public interface VersionRoot
{
    /**
     * @return the createDate
     */
    public long getCreateDate();

    /**
     * @param createDate the createDate to set
     */
    public void setCreateDate(long createDate);

    /**
     * @return the creator
     */
    public String getCreator();

    /**
     * @param creator the creator to set
     */
    public void setCreator(String creator);

    /**
     * @return the id
     */
    public long getId();

    /**
     * @param id the id to set
     */
    public void setId(long id);

    /**
     * @return the AVMStore
     */
    public AVMStore getAvmStore();

    /**
     * @param store the store to set
     */
    public void setAvmStore(AVMStore store);

    /**
     * @return the root
     */
    public DirectoryNode getRoot();

    /**
     * @param root the root to set
     */
    public void setRoot(DirectoryNode root);
    
    /**
     * Set the version id.
     * @param versionID
     */
    public void setVersionID(int versionID);
    
    /**
     * Get the version id.
     * @return The version id.
     */
    public int getVersionID();
    
    /**
     * Get the tag (short description).
     * @return The tag.
     */
    public String getTag();
    
    /**
     * Get the thick description.
     * @return The thick description.
     */
    public String getDescription();
}