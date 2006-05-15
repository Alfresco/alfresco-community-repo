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

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.avm.hibernate.BasicAttributesBean;
import org.alfresco.repo.avm.hibernate.BasicAttributesBeanImpl;
import org.alfresco.repo.avm.hibernate.LayeredFileNodeBean;
import org.alfresco.repo.avm.hibernate.LayeredFileNodeBeanImpl;

/**
 * A LayeredFileNode behaves like a copy on write symlink.
 * @author britt
 */
public class LayeredFileNode extends FileNode implements Layered
{
    /**
     * The data bean.
     */
    private LayeredFileNodeBean fData;
    
    /**
     * Construct one from its data bean.
     */
    public LayeredFileNode(LayeredFileNodeBean data)
    {
        fData = data;
        setDataBean(fData);
    }
    
    /**
     * Basically a copy constructor.
     * @param other The file to make a copy of.
     * @param repo The repository that contains us.
     */
    public LayeredFileNode(LayeredFileNode other, Repository repo)
    {
        // TODO Something.
    }

    public LayeredFileNode(FileNode file,
                             Repository repos,
                             Lookup srcLookup,
                             String name)
    {
        // TODO Something.
    }
    
    /**
     * Make a brand new layered file node.
     * @param indirection The thing we point to.
     * @param repo The repository we belong to.
     */
    public LayeredFileNode(String indirection, Repository repo)
    {
        long time = System.currentTimeMillis();
        BasicAttributesBean attrs = new BasicAttributesBeanImpl("britt",
                                                                "britt",
                                                                "britt",
                                                                time,
                                                                time,
                                                                time);
        repo.getSuperRepository().getSession().save(attrs);
        fData = new LayeredFileNodeBeanImpl(repo.getSuperRepository().issueID(),
                                            -1L,
                                            -1L,
                                            null,
                                            null,
                                            null,
                                            repo.getDataBean(),
                                            attrs,
                                            indirection);
        repo.getSuperRepository().getSession().save(fData);
        setDataBean(fData);
    }
    
    /**
     * Set the repository after a copy.
     * @param parent The parent after copying.
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
     * @param lPath The path by which this was found.
     */
    public AVMNode possiblyCopy(Lookup lPath)
    {
        // LayeredFileNodes are always copied.
        PlainFileNode newMe = new PlainFileNode(getRepository());
        newMe.setAncestor(this);
        return newMe;
    }
    
    /**
     * Get the type of this node.
     * @return The type.
     */
    public AVMNodeType getType()
    {
        return AVMNodeType.LAYERED_FILE;
    }

    /**
     * Get the content of the specified version.
     * @return A FileContent object.
     */
    public FileContent getContentForRead(int version)
    {
        Lookup lookup = getRepository().getSuperRepository().lookup(version, fData.getIndirection());
        AVMNode node = lookup.getCurrentNode();
        if (!(node instanceof FileNode))
        {
            throw new AlfrescoRuntimeException("Missing Link.");
        }
        FileNode file = (FileNode)node;
        return file.getContentForRead(version);
    }

    /**
     * Get File Content for writing.  Should never be called.
     */
    public FileContent getContentForWrite(Repository repo)
    {
        assert false : "Never happens";
        return null;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.Layered#getUnderlying(org.alfresco.repo.avm.Lookup)
     */
    public String getUnderlying(Lookup lookup)
    {
        return fData.getIndirection();
    }
}
