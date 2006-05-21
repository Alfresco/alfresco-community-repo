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
package org.alfresco.repo.avm.hibernate;

import java.util.HashMap;
import java.util.Map;


/**
 * A plain directory node is just a map of names to AVMNodes. 
 * @author britt
 */
public class PlainDirectoryNodeBeanImpl extends DirectoryNodeBeanImpl implements PlainDirectoryNodeBean
{
    /**
     * The child map.
     */
    private Map<String, DirectoryEntry> fChildren;
    
    /**
     * Whether this is a root node.
     */
    private boolean fIsRoot;
    
    /**
     * Anonymous constructor.
     */
    public PlainDirectoryNodeBeanImpl()
    {
        super();
    }
    
    /**
     * Rich constructor.
     * @param id The id to assign it.
     * @param versionID The version id.
     * @param branchID The branch id.
     * @param ancestor The ancestor.
     * @param mergedFrom The node that merged into us.
     * @param parent The parent.
     * @param isRoot Whether this is a root node.
     */
    public PlainDirectoryNodeBeanImpl(long id,
                                      int versionID,
                                      long branchID,
                                      AVMNodeBean ancestor,
                                      AVMNodeBean mergedFrom,
                                      DirectoryNodeBean parent,
                                      RepositoryBean repository,
                                      BasicAttributesBean attrs,
                                      boolean isRoot)
    {
        super(id, versionID, branchID, ancestor, mergedFrom, parent, repository, attrs);
        fChildren = new HashMap<String, DirectoryEntry>();
        fIsRoot = isRoot;
    }

    /* (non-Javadoc)
     * @see org.alfresco.proto.avm.PlainDirectoryNode#setChildren(java.util.Map)
     */
    public void setChildren(Map<String, DirectoryEntry> children)
    {
        fChildren = children;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.proto.avm.PlainDirectoryNode#getChildren()
     */
    public Map<String, DirectoryEntry> getChildren()
    {
        return fChildren;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.hibernate.PlainDirectoryNodeBean#getIsRoot()
     */
    public boolean getIsRoot()
    {
        return fIsRoot;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.hibernate.PlainDirectoryNodeBean#setIsRoot(boolean)
     */
    public void setIsRoot(boolean isRoot)
    {
        fIsRoot = isRoot;
    }
}
