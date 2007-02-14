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

import java.io.*;
import java.util.*;
import javax.faces.context.FacesContext;
import org.alfresco.model.ContentModel;
import org.alfresco.model.WCMAppModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.avm.AVMNotFoundException;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.wcm.AVMConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

/**
 * Encapsulation of a rendition.
 *
 * @author Ariel Backenroth
 */
public class FormInstanceDataImpl
   implements FormInstanceData
{

   private static final Log LOGGER = LogFactory.getLog(RenditionImpl.class);

   private final NodeRef nodeRef;

   public FormInstanceDataImpl(final NodeRef nodeRef)
   {
      if (nodeRef == null)
      {
         throw new NullPointerException();
      }
      this.nodeRef = nodeRef;
   }

   public FormInstanceDataImpl(final int version, final String avmPath)
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

   public String getWebappRelativePath()
   {
      return AVMConstants.getWebappRelativePath(this.getPath());
   }

   public String getSandboxRelativePath()
   {
      return AVMConstants.getSandboxRelativePath(this.getPath());
   }

   public String getPath()
   {
      return AVMNodeConverter.ToAVMVersionPath(this.nodeRef).getSecond();
   }

   public Document getDocument()
      throws IOException, SAXException
   {
      return XMLUtil.parse(this.getNodeRef(), 
                           this.getServiceRegistry().getContentService());
   }

   public Form getForm()
   {
      final NodeService nodeService = this.getServiceRegistry().getNodeService();
      final String parentFormName = (String)
         nodeService.getProperty(this.nodeRef, 
                                 WCMAppModel.PROP_PARENT_FORM_NAME);
      return FormsService.getInstance().getForm(parentFormName);
   }

   /** the node ref containing the contents of this rendition */
   public NodeRef getNodeRef()
   {
      return this.nodeRef;
   }

   public String getUrl()
   {
      return AVMConstants.buildAssetUrl(this.getPath());
   }

   public List<Rendition> getRenditions()
   {
      final AVMService avmService = this.getServiceRegistry().getAVMService();
      final PropertyValue pv = 
         avmService.getNodeProperty(-1, this.getPath(), WCMAppModel.PROP_RENDITIONS);
      final Collection<Serializable> renditionPaths = (pv == null 
                                                       ? Collections.EMPTY_LIST
                                                       : pv.getCollection(DataTypeDefinition.TEXT));
      final String storeName = AVMConstants.getStoreName(this.getPath());
      final List<Rendition> result = new ArrayList<Rendition>(renditionPaths.size());
      for (Serializable path : renditionPaths)
      {
         result.add(new RenditionImpl(AVMNodeConverter.ToNodeRef(-1, storeName + ':' + (String)path)));
         
      }
      return result;
   }

   private ServiceRegistry getServiceRegistry()
   {
      final FacesContext fc = FacesContext.getCurrentInstance();
      return Repository.getServiceRegistry(fc);
   }
}
