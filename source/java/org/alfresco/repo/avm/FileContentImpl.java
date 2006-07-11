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
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.Formatter;

/**
 * Content that is readable and writeable.
 * @author britt
 */
class FileContentImpl implements FileContent, Serializable
{
    static final long serialVersionUID = -7450825236235397307L;

    /**
     * The Object ID.
     */
    private long fID;
    
    /**
     * The reference count of this FileContent.
     */
    private int fRefCount;
    
    /**
     * The version (for concurrency control).
     */
    private long fVers;
    
    /**
     * The name of the file.
     */
    private String fName;
    
    /**
     * The directory path of the file.
     */
    private String fPath;

    /**
     * Default constructor.
     */
    public FileContentImpl()
    {
        fName = null;
        fPath = null;
    }
    
    /**
     * Make a brand new one.
     * @param id The id for this content.
     */
    public FileContentImpl(long id)
    {
        fID = id;
        fRefCount = 1;
        // Initialize the contents.
        try
        {
            OutputStream out = getOutputStream();
            out.close();
        }
        catch (IOException ie)
        {
            throw new AVMException("File data error.", ie);
        }
        AVMContext.fgInstance.fFileContentDAO.save(this);
    }
    
    /**
     * Initialize with the given content.
     * @param id
     * @param content
     */
    public FileContentImpl(long id, File content)
    {
        fID = id;
        fRefCount = 1;
        // Initialize the contents.
        try
        {
            OutputStream out = getOutputStream();
            InputStream in = new FileInputStream(content);
            byte [] buff = new byte[8192];
            int count;
            while ((count = in.read(buff)) != -1)
            {
                out.write(buff, 0, count);
            }
            out.close();
            in.close();
        }
        catch (IOException ie)
        {
            throw new AVMException("I/O Error.", ie);
        }
        AVMContext.fgInstance.fFileContentDAO.save(this);
    }
    
    /**
     * Copy constructor, sort of.
     * @param other The content to copy from.
     * @param id The id for this content.
     */
    public FileContentImpl(FileContent other, long id)
    {
        fID = id;
        fRefCount = 1;
        // Copy the contents from other to this.
        BufferedInputStream in = new BufferedInputStream(other.getInputStream());
        BufferedOutputStream out = new BufferedOutputStream(getOutputStream());
        try
        {
            byte [] buff = new byte[4096];  // Nyah, nyah.
            int bytesRead;
            while ((bytesRead = in.read(buff)) != -1)
            {
                out.write(buff, 0, bytesRead);
            }
            out.flush();
            out.close();
            in.close();
        }
        catch (IOException ie)
        {
            throw new AVMException("I/O failure in Copy on Write.", ie);
        }
        AVMContext.fgInstance.fFileContentDAO.save(this);
    }

    /**
     * Get this FileContent's reference count.
     * @return The reference count.
     */
    public int getRefCount()
    {
        return fRefCount;
    }
    
    /**
     * Set the reference count on this.
     * @param count The reference count to set.
     */
    public void setRefCount(int count)
    {
        fRefCount = count;
    }

    /**
     * Get an InputStream from this FileContent.
     * @return An InputStream.
     */
    public InputStream getInputStream()
    {
        try
        {
            return new FileInputStream(getContentPath());
        }
        catch (IOException ie)
        {
            throw new AVMException("Could not open for reading: " + getContentPath(), ie);
        }
    }

    /**
     * Gets an ouptut stream to this node.
     * @return An OutputStream.
     */
    public OutputStream getOutputStream()
    {
        try
        {
            File dir = new File(getDirectoryPath());
            if (!dir.exists())
            {
                dir.mkdirs();
            }
            return new FileOutputStream(getContentPath());
        }
        catch (IOException ie)
        {
            throw new AVMException("Could not open for writing: " + getContentPath(), ie);
        }
    }

    /**
     * Get a random access file from this content. It's the responsibility of
     * the caller of this to insure that this object has been copied if the
     * access argument is a write mode.
     * @param access The access more for RandomAccessFile.
     * @return A RandomAccessFile.
     */
    public RandomAccessFile getRandomAccess(String access)
    {
        try
        {
            return new RandomAccessFile(getContentPath(), access);
        }
        catch (IOException ie)
        {
            throw new AVMException("Could not open for random access: " + getContentPath(), ie);
        }
    }
    
    /**
     * Delete the contents of this file from the backing store.
     */
    public void delete()
    {
        File file = new File(getContentPath());
        file.delete();
    }

    /**
     * Get the length of this content.
     * @return The length of the content.
     */
    public long getLength()
    {
        File file = new File(getContentPath());
        return file.length();
    }
    
    /**
     * Retrieve the full path for this content. 
     * @return The full path for this content.
     */
    private synchronized String getContentPath()
    {
        if (fName == null)
        {
            calcPathData();
        }
        return fName;
    }
    
    /**
     * Get the directory path for this content.
     * @return The directory path.
     */
    private synchronized String getDirectoryPath()
    {
        if (fPath == null)
        {
            calcPathData();
        }
        return fPath;
    }
    
    /**
     * Calculate the path data.
     */
    private void calcPathData()
    {
        Formatter form = new Formatter(new StringBuilder());
        form.format("%016x", fID);
        String name = form.toString();
        form = new Formatter(new StringBuilder());
        form.format("/%02x/%02x/%02x", 
                    (fID & 0xff000000) >> 24,
                    (fID & 0xff0000) >> 16,
                    (fID & 0xff00) >> 8);
        String dir = form.toString();
        fPath = SuperRepository.GetInstance().getStorageRoot() + dir;
        fName = fPath + "/" + name;
    }
    
    /**
     * Set the version for concurrency control.
     * @param vers The value to set. 
     */
    protected void setVers(long vers)
    {
        fVers = vers;
    }
    
    /**
     * Get the version for concurrency control.
     * @return The version.
     */
    protected long getVers()
    {
        return fVers;
    }
    
    /**
     * Set the object id.  For Hibernate.
     * @param id
     */
    protected void setId(long id)
    {
        fID = id;
    }

    /**
     * Get the object id.
     * @return The object id.
     */
    public long getId()
    {
        return fID;
    }

    /**
     * Equals predicate.  Based on object ID.
     * @param obj The obect to compare against.
     * @return Equality.
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!(obj instanceof FileContent))
        {
            return false;
        }
        return fID == ((FileContent)obj).getId();
    }

    /**
     * Generate a hashCode.
     * @return The hashCode.
     */
    @Override
    public int hashCode()
    {
        return (int)fID;
    }
}
