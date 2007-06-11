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
package org.alfresco.web.bean.wcm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.linkvalidation.LinkValidationReport;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Object used to retrieve and store the state of a link validaton process.
 * The object is given an initial link validation report object (the result
 * of a LinkValidationService service call) which is used by components
 * to display the result of the links check.
 * <p>
 * Further reports can then subsequently be given to this object at which
 * point a difference is calculated i.e. which links (if any) have been
 * fixed. This allows components to display the progress of link fixing.
 * </p>
 * 
 * @author gavinc
 */
public class LinkValidationState
{
   private String store;
   private boolean checkBeenReRun = false;
   
   private int noFilesCheckedStart = 0;
   private int noFilesCheckedLast = 0;
   
   private int noLinksCheckedStart = 0;
   private int noLinksCheckedLast = 0;
   
   private int noBrokenFilesStart = 0;
   private int noBrokenFilesLast = 0;
   
   private int noBrokenLinksStart = 0;
   private int noBrokenLinksLast = 0;
   
   private int noFixedFiles = 0;
   private int noFixedLinks = 0;
   
   private List<String> brokenStaticFilesStart;
   private List<String> brokenFormsStart;
   
   private List<String> brokenStaticFilesLast;
   private List<String> brokenFormsLast;
   
   private List<String> fixedStaticFiles;
   private List<String> fixedForms;
   
   private List<String> fixedStaticFilesLast;
   private List<String> fixedFormsLast;
   
   private Map<String, List<String>> brokenLinksByFile;
   private Map<String, List<String>> brokenLinksByForm;
   
   private static Log logger = LogFactory.getLog(LinkValidationState.class);
   
   /**
    * Default constructor
    */
   public LinkValidationState(String store, LinkValidationReport initialReport)
   {
      this.store = store;
      
      processReport(initialReport, false);
   }
   
   // ------------------------------------------------------------------------------
   // Getters
   
   /**
    * @return The number of files checked by the initial link check
    */
   public int getInitialNumberFilesChecked()
   {
      return this.noFilesCheckedStart;
   }
   
   /**
    * @return The number of links checked by the initial link check
    */
   public int getInitialNumberLinksChecked()
   {
      return this.noLinksCheckedStart;
   }
   
   /**
    * @return The number of broken files found by the initial link check
    */
   public int getInitialNumberBrokenFiles()
   {
      return this.noBrokenFilesStart;
   }
   
   /**
    * @return The number of broken links found by the initial link check
    */
   public int getInitialNumberBrokenLinks()
   {
      return this.noBrokenLinksStart;
   }
   
   /**
    * @return The number of files checked by the latest link check
    */
   public int getNumberFilesChecked()
   {
      return this.noFilesCheckedLast;
   }
   
   /**
    * @return The number of links checked by the latest link check
    */
   public int getNumberLinksChecked()
   {
      return this.noLinksCheckedLast;
   }
   
   /**
    * @return The number of broken files found by the latest link check
    */
   public int getNumberBrokenFiles()
   {
      return this.noBrokenFilesLast;
   }
   
   /**
    * @return The number of broken links found by the latest link check
    */
   public int getNumberBrokenLinks()
   {
      return this.noBrokenLinksLast;
   }
   
   /**
    * @return The number of files fixed since the initial link check
    */
   public int getNumberFixedFiles()
   {
      return this.noFixedFiles;
   }
   
   /**
    * @return The number of links fixed since the initial link check
    */
   public int getNumberFixedLinks()
   {
      return this.noFixedLinks;
   }
   
   /**
    * @return A list of paths to non-generated files that contain broken links
    */
   public List<String> getStaticFilesWithBrokenLinks()
   {
      return this.brokenStaticFilesLast;
   }
   
   /**
    * @return A list of forms that have generated files containing broken links
    */
   public List<String> getFormsWithBrokenLinks()
   {
      return this.brokenFormsLast;
   }
   
   /**
    * @return A list of all files found (including those generated by forms) that
    *         contain broken links
    */
   public List<String> getAllFilesWithBrokenLinks()
   {
      throw new UnsupportedOperationException();
   }
   
   /**
    * @param form The name of a form to find broken files for
    * @return The list of broken files generated by the given form
    */
   public List<String> getFilesBrokenByForm(String form)
   {
      return this.brokenLinksByForm.get(form);
   }
   
   /**
    * @param file The path to a file with broken links
    * @return The list of broken links within the given file 
    */
   public List<String> getBrokenLinksForFile(String file)
   {
      return this.brokenLinksByFile.get(file);
   }
   
   /**
    * @return The list of non-generated files that have been fixed since the 
    *         initial link check
    */
   public List<String> getFixedStaticFiles()
   {
      return this.fixedStaticFiles;
   }
   
   /**
    * @return The list of forms that have been fixed since the initial link check
    */
   public List<String> getFixedForms()
   {
      return this.fixedForms;
   }
   
   /**
    * @return The list of non-generated files fixed in the last run of the link check
    */
   public List<String> getStaticFilesFixedInLastRun()
   {
      return this.fixedStaticFilesLast;
   }
   
   /**
    * @return The list of forms fixed in the last run of the link check
    */
   public List<String> getFormsFixedInLastRun()
   {
      return this.fixedFormsLast;
   }
   
   // ------------------------------------------------------------------------------
   // Implementation
   
   /**
    * Determines whether the link validation check has been re-run since
    * the intial check
    * 
    * @return true if the link check has been re-run, false otherwise
    */
   public boolean hasCheckBeenReRun()
   {
      return this.checkBeenReRun;
   }
   
   /**
    * Updates the link validation state with the result from a re-run of
    * the link check
    * 
    * @param newReport The report to compare the intial report with
    */
   public void updateState(LinkValidationReport newReport)
   {
      // process the new report
      processReport(newReport, true);
   }
   
   public String toString()
   {
      StringBuilder buffer = new StringBuilder(super.toString());
      buffer.append(" (store=").append(this.store).append(")");
      return buffer.toString();
   }
   
   // ------------------------------------------------------------------------------
   // Private Helpers

   public void processReport(LinkValidationReport report, boolean updatedReport)
   {
      this.checkBeenReRun = updatedReport;
      
      if (updatedReport == false)
      {
         // setup initial counts
         this.noBrokenFilesStart = report.getNumberBrokenFiles();
         this.noBrokenLinksStart = report.getNumberBrokenLinks();
         this.noFilesCheckedStart = report.getNumberFilesChecked();
         this.noLinksCheckedStart = report.getNumberLinksChecked();
         this.noBrokenFilesLast = report.getNumberBrokenFiles();
         this.noBrokenLinksLast = report.getNumberBrokenLinks();
         this.noFilesCheckedLast = report.getNumberFilesChecked();
         this.noLinksCheckedLast = report.getNumberLinksChecked();
         
         // setup initial lists
         this.brokenStaticFilesStart = report.getFilesWithBrokenLinks();
         this.brokenFormsStart = Collections.emptyList();
         this.brokenStaticFilesLast = report.getFilesWithBrokenLinks();
         this.brokenFormsLast = Collections.emptyList();
         
         this.fixedStaticFiles = Collections.emptyList();
         this.fixedForms = Collections.emptyList();
         this.fixedFormsLast = Collections.emptyList();
         this.fixedStaticFiles = Collections.emptyList();
         this.fixedStaticFilesLast = Collections.emptyList();
         
         // setup initial maps
         this.brokenLinksByFile = new HashMap<String, List<String>>();
         this.brokenLinksByForm = Collections.emptyMap();
         
         // populate initial maps
         for (String file : this.brokenStaticFilesLast)
         {
            this.brokenLinksByFile.put(file, report.getBrokenLinksForFile(file));
         }
      }
      else
      {
         // update the relevant counts
         this.noBrokenFilesLast = report.getNumberBrokenFiles();
         this.noBrokenLinksLast = report.getNumberBrokenLinks();
         this.noFilesCheckedLast = report.getNumberFilesChecked();
         this.noLinksCheckedLast = report.getNumberLinksChecked();
         
         this.noFixedFiles = this.noBrokenFilesStart - this.noBrokenFilesLast;
         this.noFixedLinks = this.noBrokenLinksStart - this.noBrokenLinksLast;
         
         if (this.noFixedFiles < 0)
         {
            this.noFixedFiles = 0;
         }
         if (this.noFixedLinks < 0)
         {
            this.noFixedLinks = 0;
         }
         
         // go through the list of files still broken and find which ones
         // were fixed in the last re-run of the report
         this.brokenStaticFilesLast = report.getFilesWithBrokenLinks();
         this.fixedStaticFiles = new ArrayList<String>();
         this.fixedStaticFilesLast = new ArrayList<String>();
         for (String file : this.brokenStaticFilesStart)
         {
            if (this.brokenStaticFilesLast.contains(file) == false)
            {
               this.fixedStaticFilesLast.add(file);
               this.fixedStaticFiles.add(file);
            }
         }
         
         // update the broken files info
         this.brokenLinksByFile.clear();
         for (String file : this.brokenStaticFilesLast)
         {
            this.brokenLinksByFile.put(file, report.getBrokenLinksForFile(file));
         }
      }
   }
}







