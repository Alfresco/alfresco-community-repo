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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.linkvalidation.LinkValidationReport;
import org.alfresco.model.WCMAppModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.web.bean.repository.Repository;
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
   private Date initialCheckedCompletedAt;
   private boolean checkBeenReRun = false;
   
   private int noFilesCheckedStart = -1;
   private int noFilesCheckedLast = -1;
   
   private int noLinksCheckedStart = -1;
   private int noLinksCheckedLast = -1;
   
   private int noBrokenFilesStart = -1;
   private int noBrokenFilesLast = -1;
   
   private int noBrokenLinksStart = -1;
   private int noBrokenLinksLast = -1;
   
   private int noFixedFiles = -1;
   private int noFixedLinks = -1;
   
   private List<String> brokenStaticFilesStart;
   private List<String> brokenFormsStart;
   
   private List<String> brokenStaticFilesLast;
   private List<String> brokenFormsLast;
   
   private List<String> fixedFiles;
   private List<String> fixedForms;
   
   private Map<String, List<String>> brokenLinksByFile;
   private Map<String, List<String>> brokenFilesByForm;
   
   private Throwable cause;
   
   private static Log logger = LogFactory.getLog(LinkValidationState.class);
   
   /**
    * Default constructor
    */
   public LinkValidationState(String store, LinkValidationReport initialReport)
   {
      this.store = store;
      this.initialCheckedCompletedAt = new Date();
      
      processReport(initialReport, false);
   }
   
   // ------------------------------------------------------------------------------
   // Getters
   
   /**
    * @return The store this validation state represents
    */
   public String getStore()
   {
      return this.store;
   }
   
   /**
    * @return The date the initial check was completed
    */
   public Date getInitialCheckCompletedAt()
   {
      return this.initialCheckedCompletedAt;
   }
   
   /**
    * @return The error that caused the last report to fail
    */
   public Throwable getError()
   {
      return this.cause;
   }
   
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
   public List<String> getBrokenFilesByForm(String form)
   {
      return this.brokenFilesByForm.get(form);
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
    * @return The list of files that have been fixed since the 
    *         initial link check
    */
   public List<String> getFixedFiles()
   {
      return this.fixedFiles;
   }
   
   /**
    * @return The list of forms that have been fixed since the
    *         initial link check
    */
   public List<String> getFixedForms()
   {
      return this.fixedForms;
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
   
   @Override
   public String toString()
   {
      StringBuilder buffer = new StringBuilder(super.toString());
      buffer.append(" (store=").append(this.store);
      buffer.append(" error=").append(this.cause).append(")");
      return buffer.toString();
   }
   
   // ------------------------------------------------------------------------------
   // Private Helpers

   public void processReport(LinkValidationReport report, boolean rerunReport)
   {
      this.checkBeenReRun = rerunReport;
      this.cause = report.getError();
      
      if (this.cause == null)
      {
         if (rerunReport == false)
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
            this.noFixedFiles = 0;
            this.noFixedLinks = 0;
            
            // setup fixed lists
            this.fixedFiles = new ArrayList<String>(this.noBrokenFilesStart);
            this.fixedForms = new ArrayList<String>(this.noBrokenFilesStart);
   
            // process the broken files and determine which ones are static files
            // and which ones are generated
            processFiles(report.getFilesWithBrokenLinks(), rerunReport, report);
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
            
            // process the broken files and determine which ones are static files
            // and which ones are generated
            processFiles(report.getFilesWithBrokenLinks(), rerunReport, report);
            
            // go through the list of files & forms still broken and find which ones
            // were fixed in the last re-run of the report
            for (String file : this.brokenStaticFilesStart)
            {
               if (this.brokenStaticFilesLast.contains(file) == false &&
                   this.fixedFiles.contains(file) == false)
               {
                  this.fixedFiles.add(file);
               }
            }
            for (String file : this.brokenFormsStart)
            {
               if (this.brokenFormsLast.contains(file) == false &&
                   this.fixedForms.contains(file) == false)
               {
                  this.fixedForms.add(file);
               }
            }
         }
      }
   }
   
   protected void processFiles(List<String> files, boolean rerunReport, LinkValidationReport report)
   {
      AVMService avmService = Repository.getServiceRegistry(
               FacesContext.getCurrentInstance()).getAVMService();
      NodeService nodeService = Repository.getServiceRegistry(
               FacesContext.getCurrentInstance()).getNodeService();
      
      if (logger.isDebugEnabled())
      {
         if (rerunReport)
            logger.debug("Processing files from updated report: " + report);
         else
            logger.debug("Processing files from initial report: " + report);
      }
         
      // reset the 'last' lists and the maps
      this.brokenStaticFilesLast = new ArrayList<String>(this.noBrokenFilesLast);
      this.brokenFormsLast = new ArrayList<String>(this.noBrokenFilesLast);
      this.brokenFilesByForm = new HashMap<String, List<String>>(this.noBrokenFilesLast);
      this.brokenLinksByFile = new HashMap<String, List<String>>(this.noBrokenFilesLast);
      
      // iterate around the files and determine which ones are generated and static
      for (String file : files)
      {
         if (avmService.hasAspect(-1, file, WCMAppModel.ASPECT_FORM_INSTANCE_DATA))
         {
            if (avmService.hasAspect(-1, file, WCMAppModel.ASPECT_RENDITION))
            {
               if (logger.isDebugEnabled())
                  logger.debug("Processing generated file: " + file);
               
               // store the broken links in the file
               this.brokenLinksByFile.put(file, report.getBrokenLinksForFile(file));
               
               // find the XML that generated this file
               NodeRef nodeRef = AVMNodeConverter.ToNodeRef(-1, file);
               String xmlPath = (String)nodeService.getProperty(nodeRef, 
                        WCMAppModel.PROP_PRIMARY_FORM_INSTANCE_DATA);
               xmlPath = this.store + ":" + xmlPath;
               
               if (logger.isDebugEnabled())
                  logger.debug("Found source XML for '" + file + "': " + xmlPath);
               
               // store the XML as a broken form (if not already)
               if (this.brokenFormsLast.contains(xmlPath) == false)
               {
                  this.brokenFormsLast.add(xmlPath);
               }
               
               // store the list of files generated by the XML in the map
               List<String> genFiles = this.brokenFilesByForm.get(xmlPath);
               if (genFiles == null)
               {
                  genFiles = new ArrayList<String>(1);
               }
               genFiles.add(file);
               this.brokenFilesByForm.put(xmlPath, genFiles);
            }
         }
         else
         {
            if (logger.isDebugEnabled())
               logger.debug("Processing static file: " + file);
            
            // the file does not have the form instance data aspect so it must
            // have been added manually
            this.brokenStaticFilesLast.add(file);
            this.brokenLinksByFile.put(file, report.getBrokenLinksForFile(file));
         }
      }
      
      // if this is the first run of the report setup the initial lists
      if (rerunReport == false)
      {
         this.brokenStaticFilesStart = new ArrayList<String>(this.brokenStaticFilesLast.size());
         this.brokenStaticFilesStart.addAll(this.brokenStaticFilesLast);

         this.brokenFormsStart = new ArrayList<String>(this.brokenFormsLast.size());
         this.brokenFormsStart.addAll(this.brokenFormsLast);
      }
   }
}







