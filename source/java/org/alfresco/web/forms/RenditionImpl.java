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
import java.io.OutputStream;

import javax.faces.context.FacesContext;

import org.alfresco.model.ContentModel;
import org.alfresco.model.WCMAppModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.util.Pair;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.wcm.AVMUtil;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

/**
 * Encapsulation of a rendition.
 *
 * @author Ariel Backenroth
 */
public class RenditionImpl
   implements Rendition
{

   private static final Log LOGGER = LogFactory.getLog(RenditionImpl.class);

   private final NodeRef nodeRef;
   private transient RenderingEngineTemplate renderingEngineTemplate;

   public RenditionImpl(final NodeRef nodeRef)
   {
      if (nodeRef == null)
      {
         throw new NullPointerException();
      }
      this.nodeRef = nodeRef;
   }

   public RenditionImpl(final int version, final String avmPath)
   {
      this(AVMNodeConverter.ToNodeRef(version, avmPath));
   }

   /** the name of this rendition */
   public String getName()
   {
      final NodeService nodeService = this.getServiceRegistry().getNodeService();
      return (String)
         nodeService.getProperty(this.nodeRef, ContentModel.PROP_NAME);
   }

   /** the description of this rendition */
   public String getDescription()
   {
      final NodeService nodeService = this.getServiceRegistry().getNodeService();
      return (String)
         nodeService.getProperty(this.nodeRef, ContentModel.PROP_DESCRIPTION);
   }

   public String getWebappRelativePath()
   {
      return AVMUtil.getWebappRelativePath(this.getPath());
   }

   public String getSandboxRelativePath()
   {
      return AVMUtil.getSandboxRelativePath(this.getPath());
   }

   public FormInstanceData getPrimaryFormInstanceData()
      throws FileNotFoundException
   {
      final AVMService avmService = this.getServiceRegistry().getAVMLockingAwareService();
      final String fidAVMStoreRelativePath = (String)
         avmService.getNodeProperty(AVMNodeConverter.ToAVMVersionPath(this.nodeRef).getFirst(), 
                                    AVMNodeConverter.ToAVMVersionPath(this.nodeRef).getSecond(), 
                                    WCMAppModel.PROP_PRIMARY_FORM_INSTANCE_DATA).getValue(DataTypeDefinition.TEXT);
      String avmStore = AVMNodeConverter.ToAVMVersionPath(this.nodeRef).getSecond();
      avmStore = avmStore.substring(0, avmStore.indexOf(':'));
      final String path = avmStore + ':' + fidAVMStoreRelativePath;
      if (avmService.lookup(-1, path) == null)
      {
         throw new FileNotFoundException("unable to find primary form instance data " + path);
      }
      return new FormInstanceDataImpl(-1, path);
   }

   /** the rendering engine template that generated this rendition */
   public RenderingEngineTemplate getRenderingEngineTemplate()
   {
      if (this.renderingEngineTemplate == null)
      {
         final AVMService avmService = this.getServiceRegistry().getAVMLockingAwareService();
         PropertyValue pv = 
            avmService.getNodeProperty(AVMNodeConverter.ToAVMVersionPath(this.nodeRef).getFirst(), 
                                       AVMNodeConverter.ToAVMVersionPath(this.nodeRef).getSecond(), 
                                       WCMAppModel.PROP_PARENT_RENDERING_ENGINE_TEMPLATE);
         if (pv == null)
         {
            LOGGER.debug("property " + WCMAppModel.PROP_PARENT_RENDERING_ENGINE_TEMPLATE +
                         " not set on " + this.getPath());
            return null;
         }

         final NodeRef retNodeRef = (NodeRef)pv.getValue(DataTypeDefinition.NODE_REF);
         if (retNodeRef == null)
         {
            LOGGER.debug("unable to locate parent rendering engine template of rendition " +
                         this.getPath());
            return null;
         }
         pv = avmService.getNodeProperty(AVMNodeConverter.ToAVMVersionPath(this.nodeRef).getFirst(), 
                                         AVMNodeConverter.ToAVMVersionPath(this.nodeRef).getSecond(), 
                                         WCMAppModel.PROP_PARENT_RENDITION_PROPERTIES);
         if (pv == null)
         {
            LOGGER.debug("property " + WCMAppModel.PROP_PARENT_RENDITION_PROPERTIES +
                         " not set on " + this.getPath());
            return null;
         }

         final NodeRef rpNodeRef = (NodeRef)pv.getValue(DataTypeDefinition.NODE_REF);
         if (rpNodeRef == null)
         {
            LOGGER.debug("unable to locate parent rendering engine template properties of rendition " +
                         this.getPath());
            return null;
         }
         this.renderingEngineTemplate = new RenderingEngineTemplateImpl(retNodeRef, rpNodeRef);
      }
      return this.renderingEngineTemplate;
   }

   /** the node ref containing the contents of this rendition */
   public NodeRef getNodeRef()
   {
      return this.nodeRef;
   }
   
   public String getPath()
   {
      return AVMNodeConverter.ToAVMVersionPath(this.nodeRef).getSecond();
   }

   public String getUrl()
   {
      return AVMUtil.buildAssetUrl(this.getPath());
   }

   public String getFileTypeImage()
   {
      return Utils.getFileTypeImage(this.getName(), false);
   }

   public OutputStream getOutputStream()
   {
      final AVMService avmService = this.getServiceRegistry().getAVMLockingAwareService();
      final Pair<Integer, String> p = AVMNodeConverter.ToAVMVersionPath(this.nodeRef);
      return (avmService.lookup(p.getFirst(), p.getSecond()) == null
              ? avmService.createFile(AVMNodeConverter.SplitBase(p.getSecond())[0],
                                      AVMNodeConverter.SplitBase(p.getSecond())[1])
              : avmService.getFileOutputStream(this.getPath()));
   }

   public void regenerate()
      throws IOException,
      RenderingEngine.RenderingException,
      SAXException
   {
      this.regenerate(this.getPrimaryFormInstanceData());
   }

   @Deprecated
   public void regenerate(final FormInstanceData formInstanceData)
      throws IOException,
      RenderingEngine.RenderingException,
      SAXException
   {
      this.getRenderingEngineTemplate().render(formInstanceData, this);
   }

   private ServiceRegistry getServiceRegistry()
   {
      final FacesContext fc = FacesContext.getCurrentInstance();
      return Repository.getServiceRegistry(fc);
   }

   public int hashCode()
   {
      return this.getPath().hashCode() ^ this.getRenderingEngineTemplate().hashCode();
   }

   public String toString()
   {
      return (this.getClass().getName() + 
              "{path : " + this.getPath() + 
              ", rendering_engine_template : " + this.getRenderingEngineTemplate() +
              "}");
   }
}
