/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.web.forms;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import javax.faces.context.FacesContext;
import org.alfresco.model.ContentModel;
import org.alfresco.model.WCMAppModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TemplateNode;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.wcm.AVMConstants;
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
      return AVMConstants.getWebappRelativePath(this.getPath());
   }

   public String getSandboxRelativePath()
   {
      return AVMConstants.getSandboxRelativePath(this.getPath());
   }

   public FormInstanceData getPrimaryFormInstanceData()
      throws FileNotFoundException
   {
      final AVMService avmService = this.getServiceRegistry().getAVMService();
      final NodeService nodeService = this.getServiceRegistry().getNodeService();
      final String fidAVMStoreRelativePath = (String)
         nodeService.getProperty(this.nodeRef, 
                                 WCMAppModel.PROP_PRIMARY_FORM_INSTANCE_DATA);
      String avmStore = AVMNodeConverter.ToAVMVersionPath(this.nodeRef).getSecond();
      avmStore = avmStore.substring(0, avmStore.indexOf(':'));
      final String path = avmStore + ':' + fidAVMStoreRelativePath;
      if (avmService.lookup(-1, path) == null)
      {
         throw new FileNotFoundException("unable to find primary form instance data " + path);
      }
      return new FormInstanceDataImpl(AVMNodeConverter.ToNodeRef(-1, path));
   }

   /** the rendering engine template that generated this rendition */
   public RenderingEngineTemplate getRenderingEngineTemplate()
   {
      if (this.renderingEngineTemplate == null)
      {
         final NodeService nodeService = this.getServiceRegistry().getNodeService();
         final NodeRef retNodeRef = (NodeRef)
            nodeService.getProperty(this.nodeRef, 
                                    WCMAppModel.PROP_PARENT_RENDERING_ENGINE_TEMPLATE);
         final NodeRef rpNodeRef = (NodeRef)
            nodeService.getProperty(this.nodeRef, 
                                    WCMAppModel.PROP_PARENT_RENDITION_PROPERTIES);
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
      return AVMConstants.buildAssetUrl(this.getPath());
   }

   public String getFileTypeImage()
   {
      return Utils.getFileTypeImage(this.getName(), false);
   }

   public OutputStream getOutputStream()
   {
      return this.getServiceRegistry().getAVMService().getFileOutputStream(this.getPath());
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
}
