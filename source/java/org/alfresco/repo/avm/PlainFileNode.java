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

import org.alfresco.repo.avm.hibernate.ContentBean;
import org.alfresco.repo.avm.hibernate.ContentBeanImpl;
import org.alfresco.repo.avm.hibernate.PlainFileNodeBean;
import org.alfresco.repo.avm.hibernate.PlainFileNodeBeanImpl;

/**
 * @author britt
 *
 */
public class PlainFileNode extends FileNode
{
    /**
     * The data bean.
     */
    private PlainFileNodeBean fData;
    
    /**
     * Construct one from its data bean.
     * @param data The data bean.
     */
    public PlainFileNode(PlainFileNodeBean data)
    {
        fData = data;
        setDataBean(data);
    }

    /**
     * Make one from just a repository.
     * @param repos A Repository.
     */
    public PlainFileNode(Repository repos)
    {
        ContentBean content = new ContentBeanImpl(repos.getSuperRepository().issueContentID());
        fData = new PlainFileNodeBeanImpl(repos.getSuperRepository().issueID(),
                                          -1,
                                          -1,
                                          null,
                                          null,
                                          null,
                                          repos.getDataBean(),
                                          content);
        content.setRefCount(1);
        // Transitive persistence should take care of content.
        repos.getSuperRepository().getSession().save(fData);
        setDataBean(fData);
    }
    
    /**
     * Copy on write constructor.
     * @param other The node we are being copied from.
     * @param repos The Repository.
     */
    public PlainFileNode(PlainFileNode other,
                         Repository repos)
    {
        fData = new PlainFileNodeBeanImpl(repos.getSuperRepository().issueID(),
                                          -1,
                                          -1,
                                          null,
                                          null,
                                          null,
                                          repos.getDataBean(),
                                          other.fData.getContent());
        repos.getSuperRepository().getSession().save(fData);
        fData.getContent().setRefCount(fData.getContent().getRefCount() + 1);
        setDataBean(fData);
    }
    
    /**
     * Handle setting repository after a COW.
     * @param parent The possibly new parent directory.
     */
    public void handlePostCopy(DirectoryNode parent)
    {
        if (parent != null)
        {
            setRepository(parent.getRepository());
        }
    }

    /**
     * Copy on write logic.
     * @param lPath The lookup path. 
     */
    public AVMNode possiblyCopy(Lookup lPath)
    {
        if (!shouldBeCopied())
        {
            return null;
        }
        PlainFileNode newMe = new PlainFileNode(this, getRepository());
        newMe.setAncestor(this);
        newMe.setBranchID(lPath.getHighestBranch());
        return newMe;
    }

    /**
     * Get the type of this node.
     * @return The type.
     */
    public AVMNodeType getType()
    {
        return AVMNodeType.PLAIN_FILE;
    }

    /**
     * Get content for reading.
     */
    public FileContent getContentForRead(int version)
    {
        return FileContentFactory.CreateFileContentFromBean(fData.getContent());
    }

    /**
     * Get content for writing.
     * @param repo The Repository.
     */
    public FileContent getContentForWrite(Repository repo)
    {
        if (fData.getContent().getRefCount() > 1)
        {
            fData.setContent(new ContentBeanImpl(repo.getSuperRepository().issueContentID()));
            // Need to copy the underlying file data.
        }
        return FileContentFactory.CreateFileContentFromBean(fData.getContent());
    }
}
