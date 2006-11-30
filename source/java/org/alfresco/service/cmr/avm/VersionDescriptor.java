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

package org.alfresco.service.cmr.avm;

import java.io.Serializable;
import java.util.Date;

/**
 * All the information about a particular version.
 * @author britt
 */
public class VersionDescriptor implements Serializable
{
    private static final long serialVersionUID = 9045221398461856268L;

    /**
     * The name of the store this version belongs to.
     */
    private String fAVMStoreName;
    
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
     * The short description.
     */
    private String fTag;

    /**
     * The long description.
     */
    private String fDescription;
    
    /**
     * New one up.
     * @param storeName The store name.
     * @param versionID The version id.
     * @param creator The creator.
     * @param createDate The create date.
     */
    public VersionDescriptor(String storeName,
                             int versionID,
                             String creator,
                             long createDate,
                             String tag,
                             String description)
    {
        fAVMStoreName = storeName;
        fVersionID = versionID;
        fCreator = creator;
        fCreateDate = createDate;
        fTag = tag;
        fDescription = description;
    }
    
    /**
     * Get the store name.
     * @return The store name.
     */
    public String getAVMStoreName()
    {
        return fAVMStoreName;
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
    
    /**
     * Get the short description.
     * @return The short description.
     */
    public String getTag()
    {
        return fTag;
    }
    
    /**
     * Get the long description.
     * @return
     */
    public String getDescription()
    {
        return fDescription;
    }
    
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        builder.append(fAVMStoreName);
        builder.append(":");
        builder.append("" + fVersionID);
        builder.append(":");
        builder.append(fCreator);
        builder.append(":");
        builder.append(new Date(fCreateDate).toString());
        builder.append(":");
        builder.append(fTag);
        builder.append("]");
        return builder.toString();
    }
}
