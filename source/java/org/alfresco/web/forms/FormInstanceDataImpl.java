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
package org.alfresco.web.forms;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.model.WCMAppModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avm.locking.AVMLockingService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.wcm.util.WCMUtil;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.wcm.AVMUtil;
import org.alfresco.web.bean.wcm.WebProject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Encapsulation of a rendition.
 *
 * @author Ariel Backenroth
 */
/* package */ class FormInstanceDataImpl implements FormInstanceData
{
   private static final long serialVersionUID = -7806221587661854013L;

   private static final Log logger = LogFactory.getLog(RenditionImpl.class);

   private final NodeRef nodeRef;
   private final WebProject webProject;
   private transient FormsService formsService;

   /* package */ FormInstanceDataImpl(final NodeRef nodeRef,
               final FormsService formsService)
   {
      this(nodeRef, formsService, null);
   }
   
   /* package */ FormInstanceDataImpl(final NodeRef nodeRef,
                                      final FormsService formsService,
                                      final WebProject webProject)
   {
      this.webProject = webProject;
      
      if (nodeRef == null)
      {
         throw new NullPointerException();
      }
      if (formsService == null)
      {
         throw new NullPointerException();
      }
      final AVMService avmService = this.getServiceRegistry().getAVMService();
      if (!avmService.hasAspect(AVMNodeConverter.ToAVMVersionPath(nodeRef).getFirst(), 
                                AVMNodeConverter.ToAVMVersionPath(nodeRef).getSecond(),
                                WCMAppModel.ASPECT_FORM_INSTANCE_DATA))
      {
         throw new IllegalArgumentException("node " + nodeRef +
                                            " does not have aspect " + WCMAppModel.ASPECT_FORM_INSTANCE_DATA);
      }
      this.nodeRef = nodeRef;
      this.formsService = formsService;
   }
   
   /* package */ FormInstanceDataImpl(final int version, 
                                      final String avmPath,
                                      final FormsService formsService)
   {
      this(AVMNodeConverter.ToNodeRef(version, avmPath), formsService);
   }
   
   private FormsService getFormsService()
   {
      if (formsService == null)
      {
         formsService = (FormsService) FacesHelper.getManagedBean(FacesContext.getCurrentInstance(), "FormsService");
      }
      return formsService;
   }

   /** the name of this rendition */
   public String getName()
   {
//      final AVMService avmService = this.getServiceRegistry().getAVMService();
//      return avmService.getNodeProperty(AVMNodeConverter.ToAVMVersionPath(this.nodeRef).getFirst(), 
//                                        AVMNodeConverter.ToAVMVersionPath(this.nodeRef).getSecond(), 
//                                        ContentModel.PROP_NAME).getStringValue();
      return AVMNodeConverter.SplitBase(AVMNodeConverter.ToAVMVersionPath(this.nodeRef).getSecond())[1];
   }

   public String getWebappRelativePath()
   {
      return AVMUtil.getWebappRelativePath(this.getPath());
   }

   public String getSandboxRelativePath()
   {
      return AVMUtil.getSandboxRelativePath(this.getPath());
   }

   public String getPath()
   {
      return AVMNodeConverter.ToAVMVersionPath(this.nodeRef).getSecond();
   }

   public Document getDocument()
      throws IOException, SAXException
   {
      return XMLUtil.parse(AVMNodeConverter.ToAVMVersionPath(nodeRef).getFirst(),
                           AVMNodeConverter.ToAVMVersionPath(nodeRef).getSecond(),
                           this.getServiceRegistry().getAVMService());
   }

   public Form getForm()
      throws FormNotFoundException
   {
      final String parentFormName = this.getParentFormName();
      try
      {
         // TODO - forms should be identified by nodeRef rather than name (which can be non-unique)
         if (getNodeRef().getStoreRef().getProtocol().equals(StoreRef.PROTOCOL_AVM))
         {
            if (webProject != null)
            {
               return webProject.getForm(parentFormName);
            }
            
            return this.getFormsService().getWebForm(parentFormName);
         }
         else
         {
            return this.getFormsService().getForm(parentFormName);
         }
      }
      catch (FormNotFoundException fnfe)
      {
         if (webProject != null)
         {
            throw new FormNotFoundException(parentFormName, webProject, this);
         }
          
         throw new FormNotFoundException(parentFormName, this);
      }
   }

   /** the node ref containing the contents of this rendition */
   public NodeRef getNodeRef()
   {
      return this.nodeRef;
   }

   public String getUrl()
   {
      return AVMUtil.getPreviewURI(this.getPath());
   }

   public List<FormInstanceData.RegenerateResult> regenerateRenditions()
      throws FormNotFoundException
   {
      if (logger.isDebugEnabled())
      {
         logger.debug("regenerating renditions of " + this);
      }
      
      AVMLockingService avmLockService = this.getServiceRegistry().getAVMLockingService();
      final AVMService avmService = this.getServiceRegistry().getAVMService();
      PropertyValue pv = avmService.getNodeProperty(
               AVMNodeConverter.ToAVMVersionPath(this.nodeRef).getFirst(), 
               AVMNodeConverter.ToAVMVersionPath(this.nodeRef).getSecond(), 
               WCMAppModel.PROP_ORIGINAL_PARENT_PATH);

      String originalParentAvmPath = (pv == null) ? 
               AVMNodeConverter.SplitBase(this.getPath())[0] : pv.getStringValue();
      
      final HashSet<RenderingEngineTemplate> allRets = 
         new HashSet<RenderingEngineTemplate>(this.getForm().getRenderingEngineTemplates());
      final List<RegenerateResult> result = new LinkedList<RegenerateResult>();
      // regenerate existing renditions
      String path = null;
      
      for (final Rendition r : this.getRenditions())
      {
         // Try to skip renditions without rendering engine template.
         if (r instanceof RenditionImpl)
         {
             RenditionImpl rImpl = (RenditionImpl)r;
             RenderingEngineTemplate ret = rImpl.getRenderingEngineTemplate();
             if ((ret != null) && (ret instanceof RenderingEngineTemplateImpl))
             {
                 RenderingEngineTemplateImpl retImpl = (RenderingEngineTemplateImpl) ret;
                 if (!retImpl.isExists())
                 {
                     continue;
                 }
             }

         }
         final RenderingEngineTemplate ret = r.getRenderingEngineTemplate();
         if (ret == null || !allRets.contains(ret))
         {
            continue;
         }
         
         String lockOwner = null;
         try
         {
            if (logger.isDebugEnabled())
            {
               logger.debug("regenerating rendition " + r + " using template " + ret);
            }
            
            path = r.getPath();
            lockOwner = avmLockService.getLockOwner(AVMUtil.getStoreId(path), AVMUtil.getStoreRelativePath(path));
            if (lockOwner != null)
            {
               if (logger.isDebugEnabled())
               {
                  logger.debug("Lock already exists for " + path);
               }
            }
            
            ret.render(this, r);
            allRets.remove(ret);
            result.add(new RegenerateResult(ret, path, r, lockOwner));
         }
         catch (Exception e)
         {
            result.add(new RegenerateResult(ret, path, e, lockOwner));
            
            // remove lock if there wasn't one before
            if (lockOwner == null)
            {
               avmLockService.removeLock(AVMUtil.getStoreId(path), AVMUtil.getStoreRelativePath(path));
               
               if (logger.isDebugEnabled())
               {
                  logger.debug("Removed lock for " + path + " as it failed to generate");
               }
            }
         }
      }
      
      // get current username for lock checks
      String username = Application.getCurrentUser(FacesContext.getCurrentInstance()).getUserName();
      
      // render all renditions for newly added templates
      for (final RenderingEngineTemplate ret : allRets)
      {
          String lockOwner = null;
          String currentLockStore = null;
          boolean lockModified = false;
          
         try
         {
            path = ret.getOutputPathForRendition(this, originalParentAvmPath, getName().replaceAll("(.+)\\..*", "$1"));
            
            if (logger.isDebugEnabled())
            {
               logger.debug("regenerating rendition of " + this.getPath() + 
                            " at " + path + " using template " + ret);
            }
            
            String storeId = AVMUtil.getStoreId(path);
            String storePath = AVMUtil.getStoreRelativePath(path);
            String storeName = AVMUtil.getStoreName(path);

            Map<String, String> lockData = avmLockService.getLockData(storeId, storePath);
            if (lockData != null) 
            {
               lockOwner = avmLockService.getLockOwner(storeId, storePath);
               currentLockStore = lockData.get(WCMUtil.LOCK_KEY_STORE_NAME);
            }
            
            if (lockOwner != null)
            {
               if (logger.isDebugEnabled())
               {
                  logger.debug("Lock already exists for " + path);
               }
               
               if (currentLockStore.equals(storeName) == false)
               {
                   if (lockOwner.equals(username))
                   {
                      lockModified = true;
                      
                      // lock already exists on path, check it's owned by the current user
                      if (logger.isDebugEnabled())
                      {
                         logger.debug("transferring lock from " + currentLockStore + " to " + storeName + " for path: " + path);
                      }
                      
                      lockData.put(WCMUtil.LOCK_KEY_STORE_NAME, storeName);
                      avmLockService.modifyLock(storeId, storePath, lockOwner, storeId, storePath, lockData);
                   }
               }
            }
            
            result.add(new RegenerateResult(ret, path, ret.render(this, path), lockOwner));
         }
         catch (Exception e)
         {
            result.add(new RegenerateResult(ret, path, e, lockOwner));

            String storeId = AVMUtil.getStoreId(path);
            String storePath = AVMUtil.getStoreRelativePath(path);
            String storeName = AVMUtil.getStoreName(path);
            
            if (lockOwner == null)
            {
               // remove lock if there wasn't one before
               avmLockService.removeLock(storeId, storePath);
               
               if (logger.isDebugEnabled())
               {
                  logger.debug("Removed lock for " + path + " as it failed to generate");
               }
            }
            else if (lockModified)
            {
                if (logger.isDebugEnabled())
                {
                   logger.debug("transferring lock from " + storeName + " to " + currentLockStore + " for path: " + path);
                }
                
                Map<String, String> lockData = avmLockService.getLockData(storeId, storePath);
                lockData.put(WCMUtil.LOCK_KEY_STORE_NAME, currentLockStore);
                avmLockService.modifyLock(storeId, storePath, lockOwner, storeId, storePath, lockData);
            }
         }
      }
      return result;
   }
   
   public List<Rendition> getRenditions()
   {
       return getRenditions(false);
   }
   
   public List<Rendition> getRenditions(boolean includeDeleted)
   {
      final AVMService avmService = this.getServiceRegistry().getAVMLockingAwareService();
      final PropertyValue pv = 
         avmService.getNodeProperty(-1, this.getPath(), WCMAppModel.PROP_RENDITIONS);
      final Collection<Serializable> renditionPaths = (pv == null 
                                                       ? Collections.EMPTY_LIST
                                                       : pv.getCollection(DataTypeDefinition.TEXT));
      final String storeName = AVMUtil.getStoreName(this.getPath());
      final List<Rendition> result = new ArrayList<Rendition>(renditionPaths.size());
      for (Serializable path : renditionPaths)
      {
         String avmRenditionPath = AVMUtil.buildAVMPath(storeName, (String)path);
         if (avmService.lookup(-1, avmRenditionPath, includeDeleted) == null)
         {
            if (logger.isDebugEnabled())
            {
               logger.debug("ignoring dangling rendition at: " + avmRenditionPath);
            }
         }
         else
         {
            final Rendition r = new RenditionImpl(-1,
                                                  avmRenditionPath,
                                                  this.getFormsService());
            try
            {
               if (!this.equals(r.getPrimaryFormInstanceData(includeDeleted)))
               {
                  if (logger.isDebugEnabled())
                  {
                     logger.debug("rendition " + r + 
                                  " points at form instance data " + r.getPrimaryFormInstanceData(includeDeleted) +
                                  " instead of " + this + ". Not including in renditions list.");
                  }
                  continue;
               }
            }
            catch (FileNotFoundException fnfe)
            {
               continue;
            }
            if (r.getRenderingEngineTemplate() != null)
            {
               result.add(r);
            }
         }
      }
      return result;
   }

   private ServiceRegistry getServiceRegistry()
   {
      final FacesContext fc = FacesContext.getCurrentInstance();
      return Repository.getServiceRegistry(fc);
   }

   public int hashCode()
   {
      return this.getPath().hashCode();
   }

   public String toString()
   {
      try
      {
         return (this.getClass().getName() + "{path : " + this.getPath() +
                 ", form : " + this.getForm().getName() + "}");
      }
      catch (FormNotFoundException fnfe)
      {
         return (this.getClass().getName() + "{path : " + this.getPath() +
                 ", form : " + this.getParentFormName() + " NOT_FOUND!  }");

      }
   }

   public boolean equals(final Object other)
   {
      return (other instanceof FormInstanceDataImpl &&
              this.getNodeRef().equals(((FormInstanceDataImpl)other).getNodeRef()));

   }

   protected String getParentFormName()
   {
      final AVMService avmService = this.getServiceRegistry().getAVMService();
      return avmService.getNodeProperty(AVMNodeConverter.ToAVMVersionPath(this.nodeRef).getFirst(), 
                                        AVMNodeConverter.ToAVMVersionPath(this.nodeRef).getSecond(), 
                                        WCMAppModel.PROP_PARENT_FORM_NAME).getStringValue();
   }
}
