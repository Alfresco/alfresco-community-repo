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
import java.io.OutputStream;

import javax.faces.context.FacesContext;

import org.alfresco.repo.web.scripts.FileTypeImageUtils;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

/**
 * Encapsulation of a rendition.
 *
 * @author Ariel Backenroth
 */
/* package */ class RenditionImpl
   implements Rendition
{

   private static final long serialVersionUID = -342658762155499039L;

   private static final Log LOGGER = LogFactory.getLog(RenditionImpl.class);

   private final NodeRef nodeRef;
   transient private FormsService formsService;
   transient private RenderingEngineTemplate renderingEngineTemplate;
   private String descriptionAttribute;

   /* package */ RenditionImpl(final NodeRef nodeRef, final FormsService formsService)
   {
      if (nodeRef == null)
      {
         throw new NullPointerException();
      }
      if (formsService == null)
      {
         throw new NullPointerException();
      }
//        // WCM
//      final AVMService avmService = this.getServiceRegistry().getAVMService();
//      if (!avmService.hasAspect(AVMNodeConverter.ToAVMVersionPath(nodeRef).getFirst(),
//                                AVMNodeConverter.ToAVMVersionPath(nodeRef).getSecond(), 
//                                WCMAppModel.ASPECT_RENDITION))
//      {
//         throw new IllegalArgumentException("node " + nodeRef +
//                                            " does not have aspect " + WCMAppModel.ASPECT_RENDITION);
//      }
      this.nodeRef = nodeRef;
      this.formsService = formsService;
   }

   /* package */ RenditionImpl(final int version, 
                               final String avmPath, 
                               final FormsService formsService)
   {
//      // WCM
//      this(AVMNodeConverter.ToNodeRef(version, avmPath), formsService);
      this(null, formsService);
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
//      // WCM
//      return AVMNodeConverter.SplitBase(AVMNodeConverter.ToAVMVersionPath(this.nodeRef).getSecond())[1];
      return null;
   }

   /** the description of this rendition */
   public String getDescription()
   {
//      // WCM
//      final AVMService avmService = this.getServiceRegistry().getAVMService();
//      return avmService.getNodeProperty(AVMNodeConverter.ToAVMVersionPath(this.nodeRef).getFirst(), 
//                                        AVMNodeConverter.ToAVMVersionPath(this.nodeRef).getSecond(), 
//                                        ContentModel.PROP_DESCRIPTION).getStringValue();
      return null;
   }

   public String getWebappRelativePath()
   {
//      // WCM
//      return AVMUtil.getWebappRelativePath(this.getPath());
      return null;
   }

   public String getSandboxRelativePath()
   {
//      // WCM
//      return AVMUtil.getSandboxRelativePath(this.getPath());
      return null;
   }

   public FormInstanceData getPrimaryFormInstanceData()
   throws FileNotFoundException
   {
       return getPrimaryFormInstanceData(false);
   }
   
   public FormInstanceData getPrimaryFormInstanceData(boolean includeDeleted)
      throws FileNotFoundException
   {
//      // WCM
//      final AVMService avmService = this.getServiceRegistry().getAVMLockingAwareService();
//      final String fidAVMStoreRelativePath = (String)
//         avmService.getNodeProperty(AVMNodeConverter.ToAVMVersionPath(this.nodeRef).getFirst(), 
//                                    AVMNodeConverter.ToAVMVersionPath(this.nodeRef).getSecond(), 
//                                    WCMAppModel.PROP_PRIMARY_FORM_INSTANCE_DATA).getValue(DataTypeDefinition.TEXT);
//      String avmStore = AVMNodeConverter.ToAVMVersionPath(this.nodeRef).getSecond();
//      avmStore = avmStore.substring(0, avmStore.indexOf(':'));
//      final String path = avmStore + ':' + fidAVMStoreRelativePath;
//      if (avmService.lookup(-1, path, includeDeleted) == null)
//      {
//         throw new FileNotFoundException("unable to find primary form instance data " + path);
//      }
//      return this.getFormsService().getFormInstanceData(-1, path);
       return null;
   }

   /** the rendering engine template that generated this rendition */
   public RenderingEngineTemplate getRenderingEngineTemplate()
   {
//      // WCM
//      if (this.renderingEngineTemplate == null)
//      {
//         final AVMService avmService = this.getServiceRegistry().getAVMLockingAwareService();
//         PropertyValue pv = 
//            avmService.getNodeProperty(AVMNodeConverter.ToAVMVersionPath(this.nodeRef).getFirst(), 
//                                       AVMNodeConverter.ToAVMVersionPath(this.nodeRef).getSecond(), 
//                                       WCMAppModel.PROP_PARENT_RENDERING_ENGINE_TEMPLATE);
//         if (pv == null)
//         {
//            LOGGER.debug("property " + WCMAppModel.PROP_PARENT_RENDERING_ENGINE_TEMPLATE +
//                         " not set on " + this.getPath());
//            return null;
//         }
//
//         final NodeRef retNodeRef = (NodeRef)pv.getValue(DataTypeDefinition.NODE_REF);
//         if (retNodeRef == null)
//         {
//            LOGGER.debug("unable to locate parent rendering engine template of rendition " +
//                         this.getPath());
//            return null;
//         }
//         pv = avmService.getNodeProperty(AVMNodeConverter.ToAVMVersionPath(this.nodeRef).getFirst(), 
//                                         AVMNodeConverter.ToAVMVersionPath(this.nodeRef).getSecond(), 
//                                         WCMAppModel.PROP_PARENT_RENDITION_PROPERTIES);
//         if (pv == null)
//         {
//            LOGGER.debug("property " + WCMAppModel.PROP_PARENT_RENDITION_PROPERTIES +
//                         " not set on " + this.getPath());
//            return null;
//         }
//
//         final NodeRef rpNodeRef = (NodeRef)pv.getValue(DataTypeDefinition.NODE_REF);
//         if (rpNodeRef == null)
//         {
//            LOGGER.debug("unable to locate parent rendering engine template properties of rendition " +
//                         this.getPath());
//            return null;
//         }
//         this.renderingEngineTemplate = new RenderingEngineTemplateImpl(retNodeRef, rpNodeRef, this.getFormsService());
//      }
//      return this.renderingEngineTemplate;
      return null;
   }

   /** the node ref containing the contents of this rendition */
   public NodeRef getNodeRef()
   {
      return this.nodeRef;
   }
   
   public String getPath()
   {
//       // WCM
//      return AVMNodeConverter.ToAVMVersionPath(this.nodeRef).getSecond();
       return null;
   }

   public String getUrl()
   {
//       // WCM
//      return AVMUtil.getPreviewURI(this.getPath());
      return null;
   }

   public String getFileTypeImage()
   {
      return FileTypeImageUtils.getFileTypeImage(this.getName(), false);
   }

   public OutputStream getOutputStream()
   {
//       // WCM
//      final AVMService avmService = this.getServiceRegistry().getAVMLockingAwareService();
//      final Pair<Integer, String> p = AVMNodeConverter.ToAVMVersionPath(this.nodeRef);
//      return (avmService.lookup(p.getFirst(), p.getSecond()) == null
//              ? avmService.createFile(AVMNodeConverter.SplitBase(p.getSecond())[0],
//                                      AVMNodeConverter.SplitBase(p.getSecond())[1])
//              : avmService.getFileOutputStream(this.getPath()));
      return null;
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
      return this.getPath().hashCode();
   }

   public boolean equals(final Object other)
   {
      return (other instanceof RenditionImpl &&
              this.getNodeRef().equals(((RenditionImpl)other).getNodeRef()));
   }

   public String toString()
   {
      return (this.getClass().getName() + 
              "{path : " + this.getPath() + 
              ", rendering_engine_template : " + this.getRenderingEngineTemplate() +
              "}");
   }
   
   public String getDescriptionAttribute()
   {
       if (StringUtils.isEmpty(this.descriptionAttribute))
       {
           this.descriptionAttribute = buildDescriptionAttribute();
       }
       return this.descriptionAttribute;
   }
   
   public String getLabelAttribute()
   {
      StringBuilder builder = new StringBuilder("<b>");
      builder.append(Utils.encode(this.getName()));
      builder.append("</b>");
      return builder.toString();
   }

   private String buildDescriptionAttribute()
   {
       int hashCode = hashCode();
       String contextPath = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();
       StringBuilder attribute = new StringBuilder(255);
       attribute.append("<span style=\"float:right;\"><a id=\"preview").append(hashCode).append("\" ");
       attribute.append("href=\"").append(getUrl()).append("\" ");
       attribute.append("style=\"text-decoration: none;\" ");
       attribute.append("target=\"window_").append(hashCode).append("_").append(getName()).append("\">");
       attribute.append("<img src=\"").append(contextPath).append("/images/icons/preview_website.gif\" ");
       attribute.append("align=\"absmiddle\" style=\"border: 0px\" alt=\"").append(getName()).append("\">");
       attribute.append("</a></span><span>").append(getDescription()).append("</span>");
       return attribute.toString();
   }
}
