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
 * http://www.alfresco.com/legal/licensing
 */
package org.alfresco.linkvalidation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Object representing the result of a link validation action being executed.
 * This object combines the results of the multiple service calls required to
 * detect broken links and retrieve them.
 * <p>
 * This object is then typically added to the store being checked as a store
 * property.
 * </p>
 * 
 * @author gavinc
 */
public class LinkValidationReport implements Serializable
{
   private int numberFilesChecked = -1;
   private int numberLinksChecked = -1;
   private int numberBrokenLinks = -1;
   private boolean successful = true;
   
   private Throwable error;
   private List<String> brokenFiles;
   private Map<String, HrefManifest> brokenLinksByFile;
   
   private static final long serialVersionUID = 7562964706845609991L;
   
   /**
    * Constructs a link validation report from the results of a check
    * 
    * @param status The object containing status i.e. file, link counts and the list
    *               of files containing broken links
    * @param manifests The manifest of broken links and files
    */
   public LinkValidationReport(HrefValidationProgress status, List<HrefManifest> manifests)
   {
      this.numberFilesChecked = status.getFileUpdateCount();
      this.numberLinksChecked = status.getUrlUpdateCount();
      
      // create a list of broken files
      this.brokenFiles = new ArrayList<String>(manifests.size());
      
      // create a map of broken links by file.
      this.brokenLinksByFile = new HashMap<String, HrefManifest>(manifests.size());
      
      // build the required list and maps
      for (HrefManifest manifest : manifests)
      {
         String fileName = manifest.getFileName();
         this.brokenFiles.add(fileName);
         this.brokenLinksByFile.put(fileName, manifest);
         this.numberBrokenLinks = this.numberBrokenLinks + manifest.getHrefs().size();
      }
   }
   
   /**
    * Constructs a link validation report from an error that occurred
    * 
    * @param error The error that caused the link check to fail
    */
   public LinkValidationReport(Throwable error)
   {
      this.setError(error);
      
      this.brokenFiles = Collections.emptyList();
      this.brokenLinksByFile = Collections.emptyMap();
   }
   
   public int getNumberFilesChecked()
   {
      return this.numberFilesChecked;
   }
   
   public int getNumberLinksChecked()
   {
      return this.numberLinksChecked;
   }
   
   public int getNumberBrokenFiles()
   {
      return this.brokenFiles.size();
   }
   
   public int getNumberBrokenLinks()
   {
      return this.numberBrokenLinks;
   }
   
   public List<String> getFilesWithBrokenLinks()
   {
      return this.brokenFiles;
   }
   
   public List<String> getBrokenLinksForFile(String file)
   {
      List<String> links = null;
      
      HrefManifest manifest = this.brokenLinksByFile.get(file);
      if (manifest != null)
      {
         links = manifest.getHrefs();
      }
      
      return links;
   }
   
   public boolean wasSuccessful()
   {
      return this.successful;
   }
   
   public void setError(Throwable error)
   {
      this.error = error;
      this.successful = false;
   }
   
   public Throwable getError()
   {
      return this.error;
   }
}






