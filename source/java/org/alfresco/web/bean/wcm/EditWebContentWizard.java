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
package org.alfresco.web.bean.wcm;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.wcm.util.WCMUtil;
import org.alfresco.web.app.Application;
import org.alfresco.web.forms.Form;
import org.alfresco.web.forms.FormInstanceData;
import org.alfresco.web.forms.FormNotFoundException;
import org.alfresco.web.forms.Rendition;
import org.alfresco.web.forms.XMLUtil;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Bean implementation for the "Edit Web Content Wizard" dialog
 */
public class EditWebContentWizard extends CreateWebContentWizard
{
   private static final long serialVersionUID = 439996926303151006L;

   private static final Log logger = LogFactory.getLog(EditWebContentWizard.class);
   
   private AVMNode avmNode;
   private Form form;
   
   protected Set<String> existingLocks = null;

   // ------------------------------------------------------------------------------
   // Wizard implementation
   
   @Override
   public void init(final Map<String, String> parameters)
   {
      super.init(parameters);
      this.avmNode = this.avmBrowseBean.getAvmActionNode();
      if (this.avmNode == null)
      {
         throw new IllegalArgumentException("Edit Form wizard requires action node context.");
      }

      if (logger.isDebugEnabled())
         logger.debug("path is " + this.avmNode.getPath());

      this.createdPath = AVMUtil.getCorrespondingPathInPreviewStore(this.avmNode.getPath());
      final WebProject webProject = new WebProject(this.createdPath);
      
      try
      {
         this.formInstanceData = this.getFormsService().getFormInstanceData(-1, this.createdPath);
         this.formName = this.formInstanceData.getForm().getName();
         this.fileName = this.formInstanceData.getName();
         
         this.form = webProject.getForm(this.formName);
      }
      catch (FormNotFoundException fnfe)
      {
         Utils.addErrorMessage(fnfe.getMessage(), fnfe);
      }
      this.content = this.getAvmService().getContentReader(-1, this.createdPath).getContentString();
      this.mimeType = MimetypeMap.MIMETYPE_XML;
      
      // calculate which locks are present at init time
      this.existingLocks = new HashSet<String>(4);
      String lock = this.getAvmLockingService().getLockOwner(AVMUtil.getStoreId(this.createdPath),
              AVMUtil.getStoreRelativePath(this.createdPath));
      if (lock != null)
      {
         this.existingLocks.add(this.createdPath);
         
         if (logger.isDebugEnabled())
            logger.debug("Lock exists for xml instance " + this.createdPath + " at initialisation");
      }
      
      for (final Rendition r : this.formInstanceData.getRenditions())
      {
         String path = r.getPath();
         lock = this.getAvmLockingService().getLockOwner(AVMUtil.getStoreId(path),
                 AVMUtil.getStoreRelativePath(path));
         if (lock != null)
         {
            this.existingLocks.add(path);
            
            if (logger.isDebugEnabled())
               logger.debug("Lock exists for rendition " + path + " at initialisation");
         }
      }
   }

   @Override
   public String cancel()
   {
      if (this.formInstanceData != null && this.renditions != null)
      {
         if (this.existingLocks.contains(this.createdPath) == false)
         {
            // there wasn't an existing lock on the form at the start of the 
            // wizard so remove the one present now
            if (logger.isDebugEnabled())
               logger.debug("removing form instance data lock from " + 
                     AVMUtil.getCorrespondingPathInMainStore(this.createdPath) +
                     " as user chose to cancel and it wasn't present at initialisation");
            
            this.getAvmLockingService().removeLock(AVMUtil.getStoreId(this.createdPath),
                                                   AVMUtil.getStoreRelativePath(this.createdPath));
         }
         
         for (Rendition r : this.renditions)
         {
            String path = r.getPath();
            
            if (this.existingLocks.contains(path) == false)
            {
               // there wasn't a lock on the rendition at the start of
               // the wizard so remove the one present now
               if (logger.isDebugEnabled())
                  logger.debug("removing lock from rendition " + 
                        AVMUtil.getCorrespondingPathInMainStore(path) + 
                        " as user chose to cancel and it wasn't present at initialisation");
               
               this.getAvmLockingService().removeLock(AVMUtil.getStoreId(path),
                                                 AVMUtil.getStoreRelativePath(path));
            }
         }
      }
      
      return super.cancel();
   }

   @Override
   public String back()
   {
      if ("content".equals(Application.getWizardManager().getCurrentStepName()))
      {
         //override in order not to delete these items
         this.formInstanceData = null;
         this.renditions = null;
      }
      return super.back();
   }

   @Override
   protected void saveContent()
      throws Exception
   {
      if (logger.isDebugEnabled())
         logger.debug("saving " + this.createdPath);
      
      String storeId = AVMUtil.getStoreId(this.createdPath);
      String storePath = AVMUtil.getStoreRelativePath(this.createdPath);
      String storeName = AVMUtil.getStoreName(this.createdPath);
      String lockOwner = this.getAvmLockingService().getLockOwner(storeId, storePath);
      Map<String, String> lockData = this.getAvmLockingService().getLockData(storeId, storePath);

      if (lockOwner != null)
      {
          if (logger.isDebugEnabled())
                logger.debug("transferring lock from " + lockData.get(WCMUtil.LOCK_KEY_STORE_NAME) + " to " + storeName);

          lockData.put(WCMUtil.LOCK_KEY_STORE_NAME, AVMUtil.getStoreName(this.createdPath));
          this.getAvmLockingService().modifyLock(storeId, storePath, lockOwner, storeId, storePath, lockData);
      }

      final ContentWriter writer = this.getAvmService().getContentWriter(this.createdPath, true);
      this.content = XMLUtil.toString(this.getInstanceDataDocument(), false);
      writer.putContent(this.content);

      // XXXarielb might not need to do this reload
      this.formInstanceData = this.getFormsService().getFormInstanceData(-1, this.createdPath);
      final List<FormInstanceData.RegenerateResult> result = this.formInstanceData.regenerateRenditions();
      this.renditions = new LinkedList<Rendition>();
      for (FormInstanceData.RegenerateResult rr : result)
      {
         if (rr.getException() != null)
         {
            Utils.addErrorMessage("error regenerating rendition using " + rr.getRenderingEngineTemplate().getName() + 
                                  ": " + rr.getException().getMessage(),
                                  rr.getException());

            // if the renditions were locked before the regenerate, move the lock back to main store
            String path = rr.getPath();
            
            if (rr.getLockOwner() != null)
            {
                this.existingLocks.add(path);
            }
            
            if (this.existingLocks.contains(path))
            {
               String renditionStoreId = AVMUtil.getStoreId(path);
               String renditionStorePath = AVMUtil.getStoreRelativePath(path);
               String renditionStoreName = AVMUtil.getCorrespondingMainStoreName(AVMUtil.getStoreName(path));
               Map<String, String> renditionlockData = this.getAvmLockingService().getLockData(AVMUtil.getStoreId(path), AVMUtil.getStoreRelativePath(path));
               renditionlockData.put(WCMUtil.LOCK_KEY_STORE_NAME, renditionStoreName);

               if (logger.isDebugEnabled())
                        logger.debug("transferring existing lock for " + path + " back to " + renditionStoreName);

               this.getAvmLockingService().modifyLock(renditionStoreId, renditionStorePath, lockOwner, renditionStoreId, renditionStorePath, renditionlockData);
            }
         }
         else
         {
            final Rendition r = rr.getRendition();
            this.renditions.add(r);

            String path = r.getPath();
            
            if (rr.getLockOwner() != null)
            {
                this.existingLocks.add(path);
            
                String renditionStoreId = AVMUtil.getStoreId(path);
                String renditionStorePath = AVMUtil.getStoreRelativePath(path);
                String renditionStoreName = AVMUtil.getCorrespondingMainStoreName(AVMUtil.getStoreName(path));
                Map<String, String> renditionlockData = this.getAvmLockingService().getLockData(AVMUtil.getStoreId(path), AVMUtil.getStoreRelativePath(path));
                renditionlockData.put(WCMUtil.LOCK_KEY_STORE_NAME, renditionStoreName);
    
                if (logger.isDebugEnabled())
                        logger.debug("transferring lock for " + path + " back to " + renditionStoreName);
    
                this.getAvmLockingService().modifyLock(renditionStoreId, renditionStorePath, lockOwner, renditionStoreId, renditionStorePath, renditionlockData);
            }
         }
      }
   }

   /** Indicates whether or not the wizard is currently in edit mode */
   @Override
   public boolean getEditMode()
   {
      return true;
   }

   @Override
   public boolean getSubmittable()
   {
      return !AVMUtil.isWorkflowStore(AVMUtil.getStoreName(this.createdPath));
   }

   /** 
    * Overridden to avoid calling getWebProject since potentially there is no web project 
    * context in workflow scenario.
    */
   @Override 
   public Form getForm()
   {
      return this.form;
   }
}
