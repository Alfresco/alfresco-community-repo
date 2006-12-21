/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.web.forms;

import java.util.LinkedList;
import java.util.List;
import javax.faces.context.FacesContext;
import org.alfresco.model.ContentModel;
import org.alfresco.model.WCMAppModel;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.service.ServiceRegistry;
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
      this.nodeRef = nodeRef;
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

   public Form getForm()
   {
      final NodeService nodeService = this.getServiceRegistry().getNodeService();
      final NodeRef formNodeRef = (NodeRef)
         nodeService.getProperty(this.nodeRef, 
                                 WCMAppModel.PROP_PARENT_FORM);
      return new FormImpl(formNodeRef);
   }

   /** the node ref containing the contents of this rendition */
   public NodeRef getNodeRef()
   {
      return this.nodeRef;
   }

   public String getUrl()
   {
      return AVMConstants.buildAVMAssetUrl(this.getPath());
   }

   public List<Rendition> getRenditions()
   {
      final AVMService avmService = this.getServiceRegistry().getAVMService();
      final List<Rendition> result = new LinkedList<Rendition>();
      for (RenderingEngineTemplate ret : this.getForm().getRenderingEngineTemplates())
      {
         final String renditionAvmPath = ret.getOutputPathForRendition(this);
         if (avmService.lookup(-1, renditionAvmPath) == null)
         {
            LOGGER.warn("unable to locate rendition " + renditionAvmPath +
                        " for form instance data " + this.getName());
         }
         else
         {
            final NodeRef renditionNodeRef = 
               AVMNodeConverter.ToNodeRef(-1, renditionAvmPath);
            result.add(new RenditionImpl(renditionNodeRef));
         }
      }
      return result;
   }

   private ServiceRegistry getServiceRegistry()
   {
      final FacesContext fc = FacesContext.getCurrentInstance();
      return Repository.getServiceRegistry(fc);
   }
}
