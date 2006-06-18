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

import java.util.Date;

/**
 * All the information about a particular version.
 * @author britt
 */
public class VersionDescriptor
{
    /**
     * The name of the repository this version belongs to.
     */
    private String fRepositoryName;
    
    /**
     * The version id.
     */
    private int fVersionID;
    
    /**
     * The creator of this version.
     */
    private String fCreator;
    
    /**
     * The date of this version's creation.
     */
    private long fCreateDate;
    
    /**
     * New one up.
     * @param repName The repository name.
     * @param versionID The version id.
     * @param creator The creator.
     * @param createDate The create date.
     */
    public VersionDescriptor(String repName,
                             int versionID,
                             String creator,
                             long createDate)
    {
        fRepositoryName = repName;
        fVersionID = versionID;
        fCreator = creator;
        fCreateDate = createDate;
    }
    
    /**
     * Get the repository name.
     * @return The repository name.
     */
    public String getRepositoryName()
    {
        return fRepositoryName;
    }
    
    /**
     * Get the version ID
     * @return The version ID
     */
    public int getVersionID()
    {
        return fVersionID;
    }
    
    /**
     * Get the creator of this version.
     * @return The creator.
     */
    public String getCreator()
    {
        return fCreator;
    }
 
    /**
     * Get the creation date.
     * @return The creation date.
     */
    public long getCreateDate()
    {
        return fCreateDate;
    }
    
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        builder.append(fRepositoryName);
        builder.append(":");
        builder.append("" + fVersionID);
        builder.append(":");
        builder.append(fCreator);
        builder.append(":");
        builder.append(new Date(fCreateDate).toString());
        builder.append("]");
        return builder.toString();
    }
}
