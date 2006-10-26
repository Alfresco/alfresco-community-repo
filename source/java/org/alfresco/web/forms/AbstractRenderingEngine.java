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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.faces.context.FacesContext;
import org.alfresco.model.WCMModel;
import org.alfresco.repo.avm.AVMRemote;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.web.bean.wcm.AVMConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.jsf.FacesContextUtils;

/**
 * Provides helper functions for form data renderers.
 */
public abstract class AbstractRenderingEngine
   implements RenderingEngine
{
   private static final Log LOGGER = LogFactory.getLog(AbstractRenderingEngine.class);

   protected static final String ALFRESCO_NS = "http://www.alfresco.org/alfresco";
   protected static final String ALFRESCO_NS_PREFIX = "alfresco";

   private final NodeRef nodeRef;
   protected final NodeService nodeService;
   protected final ContentService contentService;

   protected AbstractRenderingEngine(final NodeRef nodeRef,
                                     final NodeService nodeService,
                                     final ContentService contentService)
   {
      this.nodeRef = nodeRef;
      this.nodeService = nodeService;
      this.contentService = contentService;
   }

   /**
    * Returns the node ref for the rendering engine template.
    *
    * @return the node ref for the rendering engine template.
    */
   public NodeRef getNodeRef()
   {
      return this.nodeRef;
   }

   /**
    * Returns the file extension to use for generated assets.
    *
    * @return the file extension to use for generated assets.
    */
   public String getFileExtensionForRendition()
   {
      return (String)
         this.nodeService.getProperty(this.nodeRef, 
                                      WCMModel.PROP_FILE_EXTENSION_FOR_RENDITION);
   }

   /**
    * Returns the file extension to use for generated assets.
    *
    * @return the file extension to use for generated assets.
    */
   public String getMimetypeForRendition()
   {
      return (String)
         this.nodeService.getProperty(this.nodeRef, 
                                      WCMModel.PROP_MIMETYPE_FOR_RENDITION);
   }

   protected static AVMRemote getAVMRemote()
   {
      final FacesContext fc = 
         FacesContext.getCurrentInstance();
      final WebApplicationContext wac = 
         FacesContextUtils.getRequiredWebApplicationContext(fc);
      return (AVMRemote)wac.getBean("avmRemote");
   }

   protected static FormDataFunctions getFormDataFunctions()
   {
      return new FormDataFunctions(AbstractRenderingEngine.getAVMRemote());
   }

   /**
    * Converts the provided path to an absolute path within the avm.
    *
    * @param parentAVMPath used as the parent path if the provided path
    * is relative, otherwise used to extract the parent path portion up until
    * the webapp directory.
    * @param path a path relative to the parentAVMPath path, or if it is
    * absolute, it is relative to the webapp used in the parentAVMPath.
    *
    * @return an absolute path within the avm using the paths provided.
    */
   protected static String toAVMPath(final String parentAVMPath, final String path)
   {
      String parent = parentAVMPath;
      if (path == null || 
          path.length() == 0 || 
          ".".equals(path) || 
          "./".equals(path))
      {
         return parentAVMPath;
      }

      if (path.charAt(0) == '/')
      {
         final Pattern p = Pattern.compile("([^:]+:/" + AVMConstants.DIR_APPBASE +
                                           "/[^/]+/[^/]+).*");
         final Matcher m = p.matcher(parentAVMPath);
         if (m.matches())
         {
            parent = m.group(1);
         }
      } 
      else if (parentAVMPath.charAt(parentAVMPath.length() - 1) != '/')
      {
         parent = parent + '/';
      }

      final String result = parent + path;
      LOGGER.debug("built full avmPath " + result + 
                   " for parent " + parentAVMPath + 
                   " and request path " + path);
      return result;
   }
}