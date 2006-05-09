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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.avm.hibernate.ContentBean;
import org.alfresco.repo.avm.hibernate.ContentBeanImpl;

/**
 * Content that is readable and writeable.
 * @author britt
 */
public class FileContent
{
    /**
     * The data containing bean.
     */
    private ContentBean fData;
    
    /**
     * Make one from a bean.
     * @param data The Bean with the data.
     */
    public FileContent(ContentBean data)
    {
        fData = data;
    }
    
    /**
     * Make a brand new one.
     * @param superRepo The SuperRepository.
     */
    public FileContent(SuperRepository superRepo)
    {
        fData = new ContentBeanImpl(superRepo.issueContentID());
    }
    
    /**
     * Copy constructor, sort of.
     * @param other The content to copy from.
     * @param superRepo The SuperRepository.
     */
    public FileContent(FileContent other, SuperRepository superRepo)
    {
        fData = new ContentBeanImpl(superRepo.issueContentID());
        // TODO Something.
    }

    /**
     * Get the number of files that refer to this content.
     * @return The reference count.
     */
    public int getRefCount()
    {
        return fData.getRefCount();
    }

    /**
     * Set the reference count.
     * @param count The count to set.
     */
    public void setRefCount(int count)
    {
        fData.setRefCount(count);
    }

    /**
     * Get an input stream from the content.
     * @param superRepo The SuperRepository.
     * @return An InputStream.
     */
    public InputStream getInputStream(SuperRepository superRepo)
    {
        // TODO Something.
        return null;
    }

    /**
     * Get an output stream to the content.
     * @param superRepo The SuperRepository.
     * @return an OutputStream.
     */
    public OutputStream getOutputStream(SuperRepository superRepo)
    {
        // TODO Something.
        return null;
    }

    /**
     * Get the underlying data bean.  Don't abuse the privilege.
     * @return The data bean.
     */
    public ContentBean getDataBean()
    {
        return fData;
    }
}
