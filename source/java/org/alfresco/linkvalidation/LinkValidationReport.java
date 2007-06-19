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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.util.ParameterCheck;

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
   private String store;
   private String webapp;
   private int numberFilesChecked = -1;
   private int numberLinksChecked = -1;
   private int numberBrokenLinks = -1;
   private boolean successful = true;
   private Date completedAt;
   
   private Throwable error;
   private List<String> brokenFiles;
   private Map<String, HrefManifestEntry> brokenLinksByFile;
   
   private static final long serialVersionUID = 7562964706845609991L;
   
   /**
    * Constructs a link validation report from the results of a check of the 
    * staging area.
    * 
    * @param store The store the link check was run against
    * @param webapp The webapp within the store the check was run against
    * @param status The object containing status i.e. file, link counts and the list
    *               of files containing broken links
    * @param manifests The manifest of broken links and files
    */
   public LinkValidationReport(String store, String webapp, HrefValidationProgress status, 
            List<HrefManifestEntry> manifests)
   {
      this.store = store;
      this.webapp = webapp;
      this.completedAt = new Date();
      this.numberFilesChecked = status.getFileUpdateCount();
      this.numberLinksChecked = status.getUrlUpdateCount();
      
      // create list and map
      this.brokenFiles = new ArrayList<String>(manifests.size());
      this.brokenLinksByFile = new HashMap<String, HrefManifestEntry>(manifests.size());
      
      // build the required list and map
      storeBrokenFiles(manifests);
   }
   
   /**
    * Constructs a link validation report from the results of a comparison check
    * between the staging area and another sandbox i.e. an authors sandbox or a 
    * workflow sandbox.
    * 
    * @param store The store the link check was run against
    * @param webapp The webapp within the store the check was run against
    * @param status The object containing status i.e. file, link counts and the list
    *               of files containing broken links
    * @param brokenByDelete Object representing the broken links caused by deleted assets
    * @param brokenByNewOrMod Object representing the broken links caused by new or 
    *                         modified assets
    */
   public LinkValidationReport(String store, String webapp, HrefValidationProgress status, 
            HrefManifest brokenByDelete, HrefManifest brokenByNewOrMod)
   {
      this.store = store;
      this.webapp = webapp;
      this.completedAt = new Date();
      this.numberFilesChecked = status.getFileUpdateCount();
      this.numberLinksChecked = status.getUrlUpdateCount();
      
      // get the lists of broken files
      List<HrefManifestEntry> byDelete = brokenByDelete.getManifestEntries();
      List<HrefManifestEntry> byNewOrMod = brokenByNewOrMod.getManifestEntries();
      
      // create list and map
      this.brokenFiles = new ArrayList<String>(byDelete.size() + byNewOrMod.size());
      this.brokenLinksByFile = new HashMap<String, HrefManifestEntry>(
               byDelete.size() + byNewOrMod.size());
      
      // build the required list and map
      storeBrokenFiles(byDelete);
      storeBrokenFiles(byNewOrMod);
   }
   
   /**
    * Constructs a link validation report from an error that occurred
    *
    * @param store The store the link check was run against
    * @param webapp The webapp within the store the check was run against
    * @param error The error that caused the link check to fail
    */
   public LinkValidationReport(String store, String webapp, Throwable error)
   {
      this.store = store;
      this.webapp = webapp;
      this.completedAt = new Date();
      this.setError(error);
      
      this.brokenFiles = Collections.emptyList();
      this.brokenLinksByFile = Collections.emptyMap();
   }
   
   public String getStore()
   {
      return this.store;
   }
   
   public String getWebapp()
   {
      return this.webapp;
   }
   
   public Date getCheckCompletedAt()
   {
      return this.completedAt;
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
      
      HrefManifestEntry manifest = this.brokenLinksByFile.get(file);
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
   
   @Override
   public String toString()
   {
      StringBuilder buffer = new StringBuilder(super.toString());
      buffer.append(" (store=").append(this.store);
      buffer.append(" webapp=").append(this.webapp);
      buffer.append(" error=").append(this.error).append(")");
      return buffer.toString();
   }
   
   /**
    * Stores the given list of manifest entries in the internal lists and maps
    * 
    * @param manifests Manifest entries to store
    */
   protected void storeBrokenFiles(List<HrefManifestEntry> manifests)
   {
      ParameterCheck.mandatory("manifests", manifests);
      
      for (HrefManifestEntry manifest : manifests)
      {
         String fileName = manifest.getFileName();
         this.brokenFiles.add(fileName);
         this.brokenLinksByFile.put(fileName, manifest);
         this.numberBrokenLinks = this.numberBrokenLinks + manifest.getHrefs().size();
      }
   }
}






