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
 * http://www.alfresco.com/legal/licensing" 
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

import javax.faces.context.FacesContext;

import org.alfresco.model.WCMAppModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.avm.AVMNotFoundException;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.wcm.AVMUtil;
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
   
   private static final Log LOGGER = LogFactory.getLog(RenditionImpl.class);

   private final NodeRef nodeRef;
   private transient FormsService formsService;

   /* package */ FormInstanceDataImpl(final NodeRef nodeRef,
                                      final FormsService formsService)
   {
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
            return this.getFormsService().getWebForm(parentFormName);
         }
         else
         {
            return this.getFormsService().getForm(parentFormName);
         }
      }
      catch (FormNotFoundException fnfe)
      {
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
      return AVMUtil.buildAssetUrl(this.getPath());
   }

   public List<FormInstanceData.RegenerateResult> regenerateRenditions()
      throws FormNotFoundException
   {
      if (LOGGER.isDebugEnabled())
         LOGGER.debug("regenerating renditions of " + this);

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
      for (final Rendition r : this.getRenditions())
      {
         final RenderingEngineTemplate ret = r.getRenderingEngineTemplate();
         if (ret == null || !allRets.contains(ret))
         {
            continue;
         }
         try
         {
            LOGGER.debug("regenerating rendition " + r + " using template " + ret);
            ret.render(this, r);
            allRets.remove(ret);
            result.add(new RegenerateResult(ret, r));
         }
         catch (Exception e)
         {
            result.add(new RegenerateResult(ret, e));
         }
      }

      // render all renditions for newly added templates
      for (final RenderingEngineTemplate ret : allRets)
      {
         try
         {
            final String path = ret.getOutputPathForRendition(this, originalParentAvmPath);
            LOGGER.debug("regenerating rendition of " + this.getPath() + 
                         " at " + path + " using template " + ret);
            
            result.add(new RegenerateResult(ret, ret.render(this, path)));
         }
         catch (Exception e)
         {
            result.add(new RegenerateResult(ret, e));
         }
      }
      return result;
   }

   public List<Rendition> getRenditions()
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
         if (avmService.lookup(-1, storeName + ':' + (String)path) == null)
         {
            LOGGER.debug("ignoring dangling rendition at " + storeName + ':' + (String)path);
         }
         else
         {
            final Rendition r = new RenditionImpl(-1, 
                                                  storeName + ':' + (String)path, 
                                                  this.getFormsService());
            try
            {
               if (!this.equals(r.getPrimaryFormInstanceData()))
               {
                  LOGGER.debug("rendition " + r + 
                               " points at form instance data " + r.getPrimaryFormInstanceData() +
                               " instead of " + this + ". Not including in renditions list.");
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
