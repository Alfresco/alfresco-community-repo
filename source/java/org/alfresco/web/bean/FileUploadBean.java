/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.web.bean;

import java.io.File;
import java.io.Serializable;

/**
 * Bean to hold the results of a file upload
 * 
 * @author gavinc
 */
public final class FileUploadBean implements Serializable
{

   private static final long serialVersionUID = 7667383955924957544L;
   
   public static final String FILE_UPLOAD_BEAN_NAME = "alfresco.UploadBean";

   public static String getKey(final String id)
   {
	return ((id == null || id.length() == 0) 
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
