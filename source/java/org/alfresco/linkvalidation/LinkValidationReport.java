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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
   private int baseSnapshotVersion = -1;
   private int latestSnapshotVersion = -1;
   private int maxNumberLinksInReport = -1;
   private boolean successful = true;
   private boolean maxLinksReached = false;
   private Date completedAt;
   
   private Throwable error;
   private List<String> brokenFiles;
   private Map<String, HrefManifestEntry> brokenLinksByFile;
   
   private static final long serialVersionUID = 7562964706845609991L;
   private static Log logger = LogFactory.getLog(LinkValidationReport.class);
   
   /**
    * Constructs a link validation report from the results of a check of the 
    * staging area.
    * 
    * @param store The store the link check was run against
    * @param webapp The webapp within the store the check was run against
    * @param manifest The manifest of broken links and snapshot info
    * @param noFilesChecked The number of files checked
    * @param noLinksChecked The number of links checked
    * @param maxNumberLinksInReport The maximum number of links to store in
    *        the report, -1 will store all links passed in the manifest object
    */
   public LinkValidationReport(String store, String webapp, HrefManifest manifest,
            int noFilesChecked, int noLinksChecked, int maxNumberLinksInReport)
   {
      this.store = store;
      this.webapp = webapp;
      this.completedAt = new Date();
      this.numberBrokenLinks = 0;
      this.numberFilesChecked = noFilesChecked;
      this.numberLinksChecked = noLinksChecked;
      this.baseSnapshotVersion   = manifest.getBaseSnapshotVersion(); 
      this.latestSnapshotVersion = manifest.getLatestSnapshotVersion();
      this.maxNumberLinksInReport = maxNumberLinksInReport;
      
      // create list and map
      List<HrefManifestEntry> manifests = manifest.getManifestEntries();
      this.brokenFiles = new ArrayList<String>(manifests.size());
      this.brokenLinksByFile = new HashMap<String, HrefManifestEntry>(manifests.size());
      
      // build the required list and map
      storeBrokenFiles(manifests);
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
   
   public int getMaxNumberLinksInReport()
   {
      return this.maxNumberLinksInReport;
   }
   
   public boolean hasMaxNumberLinksExceeded()
   {
      return this.maxLinksReached;
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
   
   public int getBaseSnapshotVersion()
   {
      return this.baseSnapshotVersion;
   }
   
   public int getLatestSnapshotVersion()
   {
      return this.latestSnapshotVersion;
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
      buffer.append(" baseSnapshot=").append(this.baseSnapshotVersion);
      buffer.append(" latestSnapshot=").append(this.latestSnapshotVersion);
      buffer.append(" maxNumberLinksInReport=").append(this.maxNumberLinksInReport);
      buffer.append(" maxLinksReached=").append(this.maxLinksReached);
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
      
      // iterate over required amount of links and store them
      for (HrefManifestEntry manifest : manifests)
      {
         String fileName = manifest.getFileName();
         
         this.brokenFiles.add(fileName);
         this.brokenLinksByFile.put(fileName, manifest);
         this.numberBrokenLinks = this.numberBrokenLinks + manifest.getHrefs().size();
         
         // check whether we have exceeded the maximum number
         // of links, if we have break out
         if (this.maxNumberLinksInReport != -1 &&
             (this.numberBrokenLinks > this.maxNumberLinksInReport))
         {
            if (logger.isWarnEnabled())
               logger.warn("Maximum number of links ("+ this.maxNumberLinksInReport + 
                           ") for report has been exceeded at file number: " + 
                           this.brokenFiles.size());
            
            this.maxLinksReached = true;
            break;
         }
      }
   }
}






