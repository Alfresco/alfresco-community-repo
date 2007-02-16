/*
 * Copyright (C) 2005 Alfresco, Inc.
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
package org.alfresco.web.bean;

import java.io.File;

/**
 * Bean to hold the results of a file upload
 * 
 * @author gavinc
 */
public final class FileUploadBean
{

   public static final String FILE_UPLOAD_BEAN_NAME = "alfresco.UploadBean";

   public static String getKey(final String id)
   {
	return (id == null 
		? FILE_UPLOAD_BEAN_NAME 
		: FILE_UPLOAD_BEAN_NAME + "-" + id);
   }
   
   private File file;
   private String fileName;
   private String filePath;
   
   /**
    * @return Returns the file
    */
   public File getFile()
   {
      return file;
   }
   
   /**
    * @param file The file to set
    */
   public void setFile(File file)
   {
      this.file = file;
   }

   /**
    * @return Returns the name of the file uploaded
    */
   public String getFileName()
   {
      return fileName;
   }

   /**
    * @param fileName The name of the uploaded file
    */
   public void setFileName(String fileName)
   {
      this.fileName = fileName;
   }

   /**
    * @return Returns the path of the file uploaded
    */
   public String getFilePath()
   {
      return filePath;
   }

   /**
    * @param filePath The file path of the uploaded file
    */
   public void setFilePath(String filePath)
   {
      this.filePath = filePath;
   }
}
