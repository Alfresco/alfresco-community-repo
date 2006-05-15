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


/**
 * @author britt
 */
public class LayeredFileNodeBeanImpl extends FileNodeBeanImpl implements
        LayeredFileNodeBean
{
    /**
     * The indirection.
     */
    private String fIndirection;
    
    /**
     * Anonymous constructor.
     */
    public LayeredFileNodeBeanImpl()
    {
        super();
    }
    
    /**
     * Rich constructor.
     * @param id The ID to set.
     * @param versionID The version id.
     * @param branchID The branch id.
     * @param ancestor The ancestor.
     * @param mergedFrom The node that merged into us.
     * @param parent The parent.
     * @param indirection The indirection pointer.
     */
    public LayeredFileNodeBeanImpl(long id,
                                   long versionID,
                                   long branchID,
                                   AVMNodeBean ancestor,
                                   AVMNodeBean mergedFrom,
                                   DirectoryNodeBean parent,
                                   RepositoryBean repository,
                                   BasicAttributesBean attrs,
                                   String indirection)
    {
        super(id, versionID, branchID, ancestor, mergedFrom, parent, repository, attrs);
        fIndirection = indirection;
    }

    /**
     * @see org.alfresco.repo.avm.hibernate.LayeredFileNodeBean#setIndirection(java.lang.String)
     */
    public void setIndirection(String indirection)
    {
        fIndirection = indirection;
    }

    /**
     * @see org.alfresco.repo.avm.hibernate.LayeredFileNodeBean#getIndirection()
     */
    public String getIndirection()
    {
        return fIndirection;
    }
}
