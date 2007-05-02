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
package org.alfresco.web.bean.preview;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.web.bean.repository.Node;

/**
 * Backing bean for the Preview Document in Template action page
 * 
 * @author Kevin Roast
 */
public class DocumentPreviewBean extends BasePreviewBean
{
   /**
    * Returns the document this bean is currently representing
    * 
    * @return The document Node
    */
   public Node getNode()
   {
      return this.browseBean.getDocument();
   }
   
   /**
    * Returns a model for use by a template on the Document Details page.
    * 
    * @return model containing current document and current space info.
    */
   @SuppressWarnings("unchecked")
   public Map getTemplateModel()
   {
      HashMap model = new HashMap(3, 1.0f);
      
      model.put("document", getNode().getNodeRef());
      model.put("space", this.navigator.getCurrentNode().getNodeRef());
      model.put(TemplateService.KEY_IMAGE_RESOLVER, imageResolver);
      
      return model;
   }
}
