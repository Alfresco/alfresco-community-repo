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

   protected AbstractRenderingEngine()
   {
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
}