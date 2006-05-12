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
 * A FolderEntry is a simple value class containing type
 * information and name for an entry in a folder list.
 * @author britt
 */
public class FolderEntry
{
    /**
     * The name of the entry.
     */
    private String fName;
    
    /**
     * The type of the entry.
     */
    private AVMNodeType fType;

    /**
     * @return the name
     */
    public String getName()
    {
        return fName;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        fName = name;
    }

    /**
     * @return the type
     */
    public AVMNodeType getType()
    {
        return fType;
    }

    /**
     * @param type the type to set
     */
    public void setType(AVMNodeType type)
    {
        fType = type;
    }
}
