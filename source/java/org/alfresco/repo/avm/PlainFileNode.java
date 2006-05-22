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

import org.alfresco.repo.avm.hibernate.BasicAttributesBean;
import org.alfresco.repo.avm.hibernate.BasicAttributesBeanImpl;
import org.alfresco.repo.avm.hibernate.PlainFileNodeBean;
import org.alfresco.repo.avm.hibernate.PlainFileNodeBeanImpl;

/**
 * A plain old file. Contains a Content object.
 * @author britt
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
     * This is the constructor used when a brand new plain file is being made.
     * @param repos A Repository.
     */
    public PlainFileNode(Repository repos)
    {
        FileContent content = new FileContent(repos.getSuperRepository());
        long time = System.currentTimeMillis();
        BasicAttributesBean attrs = new BasicAttributesBeanImpl("britt",
                                                                "britt",
                                                                "britt",
                                                                time,
                                                                time,
                                                                time);
        fData = new PlainFileNodeBeanImpl(repos.getSuperRepository().issueID(),
                                          -1,
                                          0,
                                          null,
                                          null,
                                          null,
                                          repos.getDataBean(),
                                          attrs,
                                          content.getDataBean());
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
        // Setup sensible BasicAttributes.
        long time = System.currentTimeMillis();
        // TODO Figure out how to get user from context.
        BasicAttributesBean attrs = new BasicAttributesBeanImpl(other.getDataBean().getBasicAttributes());
        attrs.setCreateDate(time);
        attrs.setModDate(time);
        attrs.setAccessDate(time);
        attrs.setCreator("britt");
        attrs.setLastModifier("britt");
        fData = new PlainFileNodeBeanImpl(repos.getSuperRepository().issueID(),
                                          -1,
                                          0,
                                          null,
                                          null,
                                          null,
                                          repos.getDataBean(),
                                          attrs,
                                          other.fData.getContent());
        repos.getSuperRepository().getSession().save(fData);
        fData.getContent().setRefCount(fData.getContent().getRefCount() + 1);
        setDataBean(fData);
    }

    /**
     * Constructor that takes a FileContent to share.
     * @param content The FileContent to share.
     * @param repos The Repository.
     */
    public PlainFileNode(FileContent content,
                         Repository repos,
                         BasicAttributesBean oAttrs)
    {
        // Setup sensible BasicAttributes.
        long time = System.currentTimeMillis();
        // TODO Figure out how to get user from context.
        
        BasicAttributesBean attrs = new BasicAttributesBeanImpl(oAttrs);
        attrs.setCreateDate(time);
        attrs.setModDate(time);
        attrs.setAccessDate(time);
        attrs.setCreator("britt");
        attrs.setLastModifier("britt");
        fData = new PlainFileNodeBeanImpl(repos.getSuperRepository().issueID(),
                                          -1,
                                          0,
                                          null,
                                          null,
                                          null,
                                          repos.getDataBean(),
                                          attrs,
                                          content.getDataBean());
        repos.getSuperRepository().getSession().save(fData);
        fData.getContent().setRefCount(fData.getContent().getRefCount() + 1);
        setDataBean(fData);
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
        PlainFileNode newMe = new PlainFileNode(this, lPath.getRepository());
        newMe.setAncestor(this);
        newMe.setBranchID(lPath.getHighestBranch());
        return newMe;
    }

    /**
     * Get the type of this node.
     * @return The type.
     */
    public int getType()
    {
        return AVMNodeType.PLAIN_FILE;
    }

    /**
     * Get content for reading.
     */
    public FileContent getContentForRead(int version, Repository repo)
    {
        return new FileContent(fData.getContent());
    }

    /**
     * Get content for writing.
     * @param repo The Repository.
     */
    public FileContent getContentForWrite(Repository repo)
    {
        FileContent fc = new FileContent(fData.getContent());
        if (fData.getContent().getRefCount() > 1)
        {
            fc = new FileContent(fc, repo.getSuperRepository());
            fData.setContent(fc.getDataBean());
        }
        return fc;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMNode#toString(org.alfresco.repo.avm.Lookup)
     */
    @Override
    public String toString(Lookup lPath)
    {
        return "[PF:" + fData.getId() + "]";
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMNode#handlePostCopy(org.alfresco.repo.avm.DirectoryNode)
     */
    @Override
    public void handlePostCopy(DirectoryNode parent)
    {
    }
}
