/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/lgpl.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.web.app;

import java.util.ArrayList;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Debug phase listener that simply logs when each phase is entered and exited.
 * 
 * @author gavinc
 */
public class DebugPhaseListener implements PhaseListener
{
   private static final Log logger = LogFactory.getLog(DebugPhaseListener.class);
   
   public int indent = 0;
   public static final String INDENT = "   ";
   
   /**
    * @see javax.faces.event.PhaseListener#afterPhase(javax.faces.event.PhaseEvent)
    */
   public void afterPhase(PhaseEvent event)
   {
      if (logger.isDebugEnabled())
      {
         if (event.getPhaseId() == PhaseId.RENDER_RESPONSE)
         {
            printComponentTree(FacesContext.getCurrentInstance().getViewRoot());
         }
         
         logger.debug("********** Exiting phase: " + event.getPhaseId().toString());
      }
   }
   
   /**
    * @see javax.faces.event.PhaseListener#beforePhase(javax.faces.event.PhaseEvent)
    */
   public void beforePhase(PhaseEvent event)
   {
      if (logger.isDebugEnabled())
         logger.debug("********** Entering phase: " + event.getPhaseId().toString());
   }
   
   /**
    * @see javax.faces.event.PhaseListener#getPhaseId()
    */
   public PhaseId getPhaseId()
   {
      return PhaseId.ANY_PHASE;
   }
   
   public void printComponentTree(UIComponent comp)
   {
      printComponentInfo(comp);
      
      List complist = comp.getChildren();
      if (complist.size()>0)
         indent++;
      for   (int i = 0; i < complist.size(); i++) 
      {
         UIComponent uicom = (UIComponent) complist.get(i);
         printComponentTree(uicom);
         if (i+1 == complist.size())
            indent--;
      }
   }
   
   public void printComponentInfo(UIComponent comp)
   {
      if (comp.getId() == null)
      {
         logger.debug("UIViewRoot" + " " + "(" + comp.getClass().getName() + ")");
      } 
      else 
      {
         logger.debug(getIndent() + "|");
         logger.debug(getIndent() + comp.getId() + " " + "(" + comp.getClass().getName() + ")");
      }  
   }
   
   public String getIndent()
   {
      String indent = "";
      for (int i=0; i<this.indent; i++)
      {
         indent += INDENT;
      }
      
      return indent;         
   } 
}
