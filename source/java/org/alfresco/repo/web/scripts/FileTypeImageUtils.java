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
package org.alfresco.repo.web.scripts;

import java.util.HashMap;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;

import org.alfresco.service.cmr.repository.FileTypeImageSize;

/**
 * Contains utility methods
 * 
 * @author Roy Wetherall
 */
public class FileTypeImageUtils
{
    private static final String IMAGE_PREFIX16 = "/images/filetypes/";
    private static final String IMAGE_PREFIX32 = "/images/filetypes32/";
    private static final String IMAGE_PREFIX64 = "/images/filetypes64/";
    private static final String IMAGE_POSTFIX_GIF = ".gif";
    private static final String IMAGE_POSTFIX_PNG = ".png";
    private static final String DEFAULT_FILE_IMAGE16 = IMAGE_PREFIX16 + "_default" + IMAGE_POSTFIX_GIF;
    private static final String DEFAULT_FILE_IMAGE32 = IMAGE_PREFIX32 + "_default" + IMAGE_POSTFIX_GIF;
    private static final String DEFAULT_FILE_IMAGE64 = IMAGE_PREFIX64 + "_default" + IMAGE_POSTFIX_PNG;
    
    private static final Map<String, String> s_fileExtensionMap = new HashMap<String, String>(89, 1.0f);
    
    /**
     * Return the image path to the filetype icon for the specified file name string
     * 
     * @param name       File name to build filetype icon path for
     * @param small      True for the small 16x16 icon or false for the large 32x32 
     * 
     * @return the image path for the specified node type or the default icon if not found
     */
    public static String getFileTypeImage(String name, boolean small)
    {
       return getFileTypeImage(FacesContext.getCurrentInstance(), null, name,
              (small ? FileTypeImageSize.Small : FileTypeImageSize.Medium));
    }
    
    /**
     * Return the image path to the filetype icon for the specified file name string
     * 
     * @param fc         FacesContext
     * @param name       File name to build filetype icon path for
     * @param small      True for the small 16x16 icon or false for the large 32x32 
     * 
     * @return the image path for the specified node type or the default icon if not found
     */
    public static String getFileTypeImage(FacesContext fc, String name, boolean small)
    {
       return getFileTypeImage(fc, null, name, (small ? FileTypeImageSize.Small : FileTypeImageSize.Medium));
    }
    
    /**
     * Return the image path to the filetype icon for the specified file name string
     * 
     * @param fc         FacesContext
     * @param name       File name to build filetype icon path for
     * @param size       Size of the icon to return
     * 
     * @return the image path for the specified node type or the default icon if not found
     */
    public static String getFileTypeImage(FacesContext fc, String name, FileTypeImageSize size)
    {
       return getFileTypeImage(fc, null, name, size);
    }
    
    /**
     * Return the image path to the filetype icon for the specified file name string
     * 
     * @param sc         ServletContext
     * @param name       File name to build filetype icon path for
     * @param small      True for the small 16x16 icon or false for the large 32x32 
     * 
     * @return the image path for the specified node type or the default icon if not found
     */
    public static String getFileTypeImage(ServletContext sc, String name, boolean small)
    {
       return getFileTypeImage(null, sc, name, (small ? FileTypeImageSize.Small : FileTypeImageSize.Medium));
    }
    
    /**
     * Return the image path to the filetype icon for the specified file name string
     * 
     * @param sc         ServletContext
     * @param name       File name to build filetype icon path for
     * @param size       Size of the icon to return
     * 
     * @return the image path for the specified node type or the default icon if not found
     */
    public static String getFileTypeImage(ServletContext sc, String name, FileTypeImageSize size)
    {
       return getFileTypeImage(null, sc, name, size);
    }
    
    private static String getFileTypeImage(FacesContext fc, ServletContext sc, String name, FileTypeImageSize size)
    {
       String image = null;
       String defaultImage = null;
       switch (size)
       {
          case Small:
             defaultImage = DEFAULT_FILE_IMAGE16; break;
          case Medium:
             defaultImage = DEFAULT_FILE_IMAGE32; break;
          case Large:
             defaultImage = DEFAULT_FILE_IMAGE64; break;
       }
       
       int extIndex = name.lastIndexOf('.');
       if (extIndex != -1 && name.length() > extIndex + 1)
       {
          String ext = name.substring(extIndex + 1).toLowerCase();
          String key = ext + ' ' + size.toString();
          
          // found file extension for appropriate size image
          synchronized (s_fileExtensionMap)
          {
             image = s_fileExtensionMap.get(key);
             if (image == null)
             {
                // not found create for first time
                if (size != FileTypeImageSize.Large)
                {
                   image = (size == FileTypeImageSize.Small ? IMAGE_PREFIX16 : IMAGE_PREFIX32) +
                            ext + IMAGE_POSTFIX_GIF;
                }
                else
                {
                   image = IMAGE_PREFIX64 + ext + IMAGE_POSTFIX_PNG;
                }
                
                // does this image exist on the web-server?
                if ((fc != null && fc.getExternalContext().getResourceAsStream(image) != null) ||
                    (sc != null && sc.getResourceAsStream(image) != null))
                {
                   // found the image for this extension - save it for later
                   s_fileExtensionMap.put(key, image);
                }
                else if ((fc == null) && (sc == null))
                {
                   // we have neither FacesContext nor ServerContext so return the default image but don't cache it
                   image = defaultImage;
                }
                else
                {
                   // not found, save the default image for this extension instead
                   s_fileExtensionMap.put(key, defaultImage);
                   image = defaultImage;
                }
             }
          }
       }
       
       return (image != null ? image : defaultImage);
    }

}
