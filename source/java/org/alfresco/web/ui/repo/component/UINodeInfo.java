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
package org.alfresco.web.ui.repo.component;

import java.io.IOException;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;

import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;
import org.springframework.extensions.webscripts.ui.common.component.SelfRenderingComponent;

/**
 * JSF component that displays information about a node.
 * 
 * @author gavinc
 */
public class UINodeInfo extends SelfRenderingComponent
{
   protected final static String NODE_INFO_SCRIPTS_WRITTEN = "_alfNodeInfoScripts";
   
   protected Object value = null;
   
   
   // ------------------------------------------------------------------------------
   // Component Impl 

   @Override
   public String getFamily()
   {
      return "org.alfresco.faces.NodeInfo";
   }
   
   @Override
   public void restoreState(FacesContext context, Object state)
   {
      Object values[] = (Object[])state;
      // standard component attributes are restored by the super class
      super.restoreState(context, values[0]);
      this.value = values[1];
   }
   
   @Override
   public Object saveState(FacesContext context)
   {
      Object values[] = new Object[] {
         super.saveState(context),
         this.value};
      return values;
   }
   
   @Override
   @SuppressWarnings("unchecked")
   public void encodeBegin(FacesContext context) throws IOException
   {
      if (!isRendered()) return;
      
      // if AJAX is disabled don't render anything
      if (Application.getClientConfig(context).isNodeSummaryEnabled())
      {
         ResponseWriter out = context.getResponseWriter();
         
         outputNodeInfoScripts(context, out);
         
         // wrap the child components in a <span> that has the onmouseover
         // event which kicks off the request for node information
         // we key the node info panel by the noderef string of the current node
         String noderef = Repository.getStoreRef().toString() + '/' + (String)this.getValue();
         out.write("<span onclick=\"AlfNodeInfoMgr.toggle('");
         out.write(noderef);
         out.write("',this);\">");
      }
   }

   protected static void outputNodeInfoScripts(FacesContext context, ResponseWriter out) throws IOException
   {
      // write out the JavaScript specific to the NodeInfo component, ensure it's only done once
      Object present = context.getExternalContext().getRequestMap().get(NODE_INFO_SCRIPTS_WRITTEN);
      if (present == null)
      {
         out.write("<script>var AlfNodeInfoMgr = new Alfresco.PanelManager(" +
                   "\"NodeInfoBean.sendNodeInfo\", \"noderef\");</script>");
         
         context.getExternalContext().getRequestMap().put(
               NODE_INFO_SCRIPTS_WRITTEN, Boolean.TRUE);
      }
   }

   @Override
   public void encodeEnd(FacesContext context) throws IOException
   {
      if (!isRendered()) return;
      
      // if AJAX is disabled don't render anything
      if (Application.getClientConfig(context).isNodeSummaryEnabled())
      {
         context.getResponseWriter().write("</span>");
      }
   }
   
   
   // ------------------------------------------------------------------------------
   // Strongly typed component property accessors

   /**
    * Get the value - the value is used in a equals() match against the current value in the
    * parent ModeList component to set the selected item.
    *
    * @return the value
    */
   public Object getValue()
   {
      ValueBinding vb = getValueBinding("value");
      if (vb != null)
      {
         this.value = vb.getValue(getFacesContext());
      }
      
      return this.value;
   }

   /**
    * Set the value - the value is used in a equals() match against the current value in the
    * parent ModeList component to set the selected item.
    *
    * @param value     the value
    */
   public void setValue(Object value)
   {
      this.value = value;
   }
}
