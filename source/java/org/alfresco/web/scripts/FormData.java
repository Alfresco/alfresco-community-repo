/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.web.scripts;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.alfresco.repo.jscript.Scopeable;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.jscript.ScriptNode.ScriptContent;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;


/**
 * Form Data
 * 
 * @author davidc
 */
public class FormData implements Serializable, Scopeable
{
    private static final long serialVersionUID = 1832644544828452385L;

    private Scriptable scope;
    private HttpServletRequest req;
    private ServletFileUpload upload;
    private List<FileItem> files = null;
   
    /**
     * Construct
     * 
     * @param req
     */
    public FormData(HttpServletRequest req)
    {
        this.req = req;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.jscript.Scopeable#setScope(org.mozilla.javascript.Scriptable)
     */
	public void setScope(Scriptable scope)
	{
		this.scope = scope;
	}

	/**
	 * Determine if multi-part form data has been provided
	 * 
	 * @return  true => multi-part
	 */
	public boolean getIsMultiPart()
	{
	    return upload.isMultipartContent(req);
	}

    public boolean jsGet_isMultipart()
    {
        return getIsMultiPart();
    }

    /**
     * Determine if form data has specified field
     * 
     * @param name  field to look for
     * @return  true => form data contains field
     */
	public boolean hasField(String name)
	{
	    List<FileItem> files = getFiles();
	    for (FileItem file : files)
	    {
	        if (file.getFieldName().equals(name))
	        {
	            return true;
	        }
	    }
	    return false;
	}

	/**
	 * Gets the Form fields
	 * 
	 * @return  array of FormField
	 */
    public Scriptable getFields()
    {
        List<FileItem> files = getFiles();
        Object[] fields = new Object[files.size()];
        int i = 0;
        for (FileItem file : files)
        {
            fields[i++] = new FormField(file);
        }
        return Context.getCurrentContext().newArray(this.scope, fields);        
    }

    public Scriptable jsGet_fields()
    {
        return getFields();
    }

    /**
     * Helper to parse servlet request form data
     * 
     * @return  parsed form data
     */
	private List<FileItem> getFiles()
	{
	    // NOTE: This class is not thread safe - it is expected to be constructed on each thread.
	    if (files == null)
	    {
	        FileItemFactory factory = new DiskFileItemFactory();
	        upload = new ServletFileUpload(factory);
	        try
	        {
	            files = upload.parseRequest(req);
	        }
            catch(FileUploadException e)
            {
                // NOTE: assume no files can be located
                files = Collections.EMPTY_LIST;
            }
	    }
	    return files;
	}
 

	/**
	 * Form Field
	 * 
	 * @author davidc
	 */
	public class FormField implements Serializable
	{
	    private FileItem file;

	    /**
	     * Construct
	     * 
	     * @param file
	     */
	    public FormField(FileItem file)
	    {
	        this.file = file;
	    }
	    
	    /**
	     * @return  field name
	     */
	    public String getName()
	    {
	        return file.getFieldName();
	    }
	    
	    public String jsGet_name()
	    {
	        return getName();
	    }
	    
	    /**
	     * @return  true => field represents a file
	     */
	    public boolean getIsFile()
	    {
	        return !file.isFormField();
	    }
	    
        public boolean jsGet_isFile()
        {
            return getIsFile();
        }

        /**
         * @return  field value (for file, attempts conversion to string)
         */
        public String getValue()
	    {
	        return file.getString();
	    }
        
        public String jsGet_value()
        {
            return getValue();
        }

        /**
         * @return  field as content
         */
	    public ScriptContent getContent()
	    {
	        try
	        {
	            return new ScriptNode.ScriptContentStream(file.getInputStream(), getMimetype(), null);
	        }
	        catch(IOException e)
	        {
	            return null;
	        }
	    }

	    public ScriptContent jsGet_content()
        {
            return getContent();
        }

	    /**
	     * @return  mimetype
	     */
	    public String getMimetype()
	    {
	        return file.getContentType();
	    }

        public String jsGet_mimetype()
        {
            return getMimetype();
        }

        /**
         * @return  filename (only for file fields, otherwise null)
         */
	    public String getFilename()
	    {
	        return file.getName();
	    }
	    
        public String jsGet_filename()
        {
            return getFilename();
        }

	}
	
}
