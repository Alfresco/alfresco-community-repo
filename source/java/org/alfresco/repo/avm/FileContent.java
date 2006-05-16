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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Formatter;

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
     * The name of the file.
     */
    private String fName;
    
    /**
     * The directory path of the file.
     */
    private String fPath;
    
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
        BufferedOutputStream out = new BufferedOutputStream(getOutputStream(superRepo));
        // Make an empty file.
        try
        {
            out.close();
        }
        catch (IOException ie)
        {
            throw new AlfrescoRuntimeException("Couldn't close file.", ie);
        }
        superRepo.getSession().save(fData);
    }
    
    /**
     * Copy constructor, sort of.
     * @param other The content to copy from.
     * @param superRepo The SuperRepository.
     */
    public FileContent(FileContent other, SuperRepository superRepo)
    {
        fData = new ContentBeanImpl(superRepo.issueContentID());
        // Copy the contents from other to this.
        BufferedInputStream in = new BufferedInputStream(other.getInputStream(superRepo));
        BufferedOutputStream out = new BufferedOutputStream(this.getOutputStream(superRepo));
        try
        {
            byte [] buff = new byte[4096];  // Nyah, nyah.
            int bytesRead;
            while ((bytesRead = in.read(buff)) != -1)
            {
                out.write(buff, 0, bytesRead);
            }
            out.close();
            in.close();
        }
        catch (IOException ie)
        {
            throw new AlfrescoRuntimeException("I/O failure in Copy on Write.", ie);
        }
        superRepo.getSession().save(fData);
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
        try
        {
            return new FileInputStream(getContentPath(superRepo));
        }
        catch (IOException ie)
        {
            throw new AlfrescoRuntimeException("Could not open for reading: " + getContentPath(superRepo), ie);
        }
    }

    /**
     * Get an output stream to the content.
     * @param superRepo The SuperRepository.
     * @return an OutputStream.
     */
    public OutputStream getOutputStream(SuperRepository superRepo)
    {
        try
        {
            File dir = new File(getDirectoryPath(superRepo));
            if (!dir.exists())
            {
                dir.mkdirs();
            }
            return new FileOutputStream(getContentPath(superRepo));
        }
        catch (IOException ie)
        {
            throw new AlfrescoRuntimeException("Could not open for writing: " + getContentPath(superRepo), ie);
        }
    }

    /**
     * Get the underlying data bean.  Don't abuse the privilege.
     * @return The data bean.
     */
    public ContentBean getDataBean()
    {
        return fData;
    }
    
    /**
     * Retrieve the full path for this content. 
     * @param superRepo
     * @return The full path for this content.
     */
    private String getContentPath(SuperRepository superRepo)
    {
        if (fName == null)
        {
            calcPathData(superRepo);
        }
        return fName;
    }
    
    /**
     * Get the directory path for this content.
     * @param superRepo
     * @return The directory path.
     */
    private String getDirectoryPath(SuperRepository superRepo)
    {
        if (fPath == null)
        {
            calcPathData(superRepo);
        }
        return fPath;
    }
    
    /**
     * Calculate the path data.
     */
    private void calcPathData(SuperRepository superRepo)
    {
        long id = fData.getId();
        Formatter form = new Formatter(new StringBuilder());
        form.format("%016x", id);
        String name = form.toString();
        form = new Formatter(new StringBuilder());
        form.format("/%02x/%02x/%02x", 
                    (id & 0xff000000) >> 24,
                    (id & 0xff0000) >> 16,
                    (id & 0xff00) >> 8);
        String dir = form.toString();
        fPath = superRepo.getStorageRoot() + dir;
        fName = fPath + "/" + name;
    }
}
