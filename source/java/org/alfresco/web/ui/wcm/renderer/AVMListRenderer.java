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
package org.alfresco.web.ui.wcm.renderer;

import javax.faces.context.FacesContext;

import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.wcm.AVMNode;
import org.alfresco.web.ui.common.component.data.UIRichList;
import org.alfresco.web.ui.common.renderer.data.RichListRenderer;

/**
 * @author kevinr
 */
public class AVMListRenderer extends RichListRenderer
{
   // ------------------------------------------------------------------------------
   // Inner classes
   
   /**
    * Class to implement a Details view for the RichList component
    * 
    * @author kevinr
    */
   public static class DetailsViewRenderer extends RichListRenderer.DetailsViewRenderer
   {
      private static final long serialVersionUID = -2753231623981676638L;
      
      @Override
      public String getRowStyle(FacesContext context, UIRichList richList, Object row)
      {
         String rowStyle = (String)richList.getAttributes().get("rowStyleClass");
         String altStyle = (String)richList.getAttributes().get("altRowStyleClass");
         if (altStyle != null && (this.rowIndex++ & 1) == 1)
         {
            rowStyle = altStyle;
         }
         
         if (row instanceof AVMNode)
         {
            AVMNodeDescriptor avmRef = ((AVMNode)row).getDescriptor();
            
            if ((avmRef.isLayeredDirectory() && avmRef.isPrimary()) || avmRef.isLayeredFile())
            {
               AVMService avmService = Repository.getServiceRegistry(context).getAVMService();
               if ((avmService.lookup(avmRef.getIndirectionVersion(), avmRef.getIndirection()) == null) &&
                   (! avmRef.getOpacity()))
               {
                  rowStyle = STALE_CSS;
               }
            }
         }
         
         return rowStyle;
      }
   }
}
