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
package org.alfresco.web.ui.common.component;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;
import javax.faces.component.UIForm;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.FacesEvent;

import org.alfresco.web.app.Application;
import org.alfresco.web.ui.common.PanelGenerator;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.WebResources;

/**
 * @author Kevin Roast
 */
public class UIStatusMessage extends SelfRenderingComponent
{
   /**
    * Default Constructor 
    */
   public UIStatusMessage()
   {
      setRendererType(null);
      
      // add default message to display
      FacesContext fc = FacesContext.getCurrentInstance();
      String msg = Application.getMessage(fc, MSG_DEFAULT_STATUS);
      String time = Utils.getTimeFormat(fc).format(new Date(System.currentTimeMillis()));
      this.messages.add(new FacesMessage(FacesMessage.SEVERITY_INFO, time, msg));
   }

   /**
    * @see javax.faces.component.UIComponent#getFamily()
    */
   public String getFamily()
   {
      return "org.alfresco.faces.StatusMessage";
   }
   
   /**
    * @see javax.faces.component.StateHolder#restoreState(javax.faces.context.FacesContext, java.lang.Object)
    */
   public void restoreState(FacesContext context, Object state)
   {
      Object values[] = (Object[])state;
      // standard component attributes are restored by the super class
      super.restoreState(context, values[0]);
      this.border = (String)values[1];
      this.bgcolor = (String)values[2];
      this.messages = (List)values[3];
      this.currentMessage = ((Integer)values[4]).intValue();
   }
   
   /**
    * @see javax.faces.component.StateHolder#saveState(javax.faces.context.FacesContext)
    */
   public Object saveState(FacesContext context)
   {
      Object values[] = new Object[5];
      // standard component attributes are saved by the super class
      values[0] = super.saveState(context);
      values[1] = this.border;
      values[2] = this.bgcolor;
      values[3] = this.messages;
      values[4] = Integer.valueOf(this.currentMessage);
      return values;
   }

   /**
    * @see javax.faces.component.UIComponentBase#encodeBegin(javax.faces.context.FacesContext)
    */
   public void encodeBegin(FacesContext context) throws IOException
   {
      if (isRendered() == false)
      {
         return;
      }
      
      ResponseWriter out = context.getResponseWriter();
      
      String bgColor = getBgcolor();
      if (bgColor == null)
      {
         bgColor = PanelGenerator.BGCOLOR_WHITE;
      }
      
      String panel = getBorder();
      if (panel != null)
      {
         PanelGenerator.generatePanelStart(out,
               context.getExternalContext().getRequestContextPath(),
               panel,
               bgColor);
      }
      
      // Previous Message icon image - clicking shows previous message
      out.write("<table width=100% cellspacing=0 cellpadding=0><tr><td>");
      String field = getHiddenFieldName();
      String leftValue = getClientId(context) + NamingContainer.SEPARATOR_CHAR + Integer.toString(ACTION_PREVIOUS);
      String leftOnclick = Utils.generateFormSubmit(context, this, field, leftValue);
      out.write(Utils.buildImageTag(context, WebResources.IMAGE_MOVELEFT, 12, 12, null, leftOnclick, "absmiddle"));
      out.write("</td><td width=100% align=center>");
      
      // get messages for the component and crop the stack to the maximum history size
      Iterator<FacesMessage> msgIterator = context.getMessages(STATUS_MESSAGE);
      while (msgIterator.hasNext())
      {
         if (messages.size() >= HISTORY_SIZE)
         {
            messages.remove(HISTORY_SIZE);
         }
         // add new messages to the stack in turn
         messages.add(0, msgIterator.next());
         // reset current message to top if new one added
         currentMessage = 0;
      }
      
      // TODO: show different icon depending on SEVERITY of the message?
      // Message text
      String style = CSS_ERROR;
      String icon = WebResources.IMAGE_INFO;
      FacesMessage msg = messages.get(currentMessage);
      if (msg.getSeverity() == FacesMessage.SEVERITY_INFO)
      {
         style = CSS_INFO;
      }
      else if (msg.getSeverity() == FacesMessage.SEVERITY_WARN)
      {
         style = CSS_WARNING;
      }
      
      out.write(Utils.buildImageTag(context, icon, null, "absmiddle"));
      out.write("&nbsp;<span class='");
      out.write(style);
      out.write("'>");
      out.write(msg.getSummary());
      out.write(" - ");
      out.write(Utils.encode(msg.getDetail()));
      out.write("</span>");
      out.write("</td><td>");
      
      // Next Message icon image - clicking shows next message
      String rightValue = getClientId(context) + NamingContainer.SEPARATOR_CHAR + Integer.toString(ACTION_NEXT);
      String rightOnclick = Utils.generateFormSubmit(context, this, field, rightValue);
      out.write(Utils.buildImageTag(context, WebResources.IMAGE_MOVERIGHT, 12, 12, null, rightOnclick, "absmiddle"));
      out.write("</td></tr></table>");
      
      if (panel != null)
      {
         PanelGenerator.generatePanelEnd(out, 
               context.getExternalContext().getRequestContextPath(),
               panel);
      }
   }
   
   /**
    * @see javax.faces.component.UIComponentBase#decode(javax.faces.context.FacesContext)
    */
   public void decode(FacesContext context)
   {
      Map requestMap = context.getExternalContext().getRequestParameterMap();
      String fieldId = getHiddenFieldName();
      String value = (String)requestMap.get(fieldId);
      
      // we encoded the value to start with our Id
      if (value != null && value.startsWith(getClientId(context)))
      {
         // we were clicked, strip out the value
         int action = Integer.parseInt(value.substring(getClientId(context).length() + 1));
         
         // raise an event to represent the requested action
         MessageEvent event = new MessageEvent(this, action);
         queueEvent(event);
      }
   }
   
   /**
    * @see javax.faces.component.UICommand#broadcast(javax.faces.event.FacesEvent)
    */
   public void broadcast(FacesEvent event) throws AbortProcessingException
   {
      if (event instanceof MessageEvent)
      {
         switch (((MessageEvent)event).Action)
         {
            case ACTION_NEXT:
               currentMessage++;
               if (currentMessage >= this.messages.size())
               {
                  currentMessage = 0;
               }
               break;
               
            case ACTION_PREVIOUS:
               currentMessage--;
               if (currentMessage < 0)
               {
                  currentMessage = this.messages.size() - 1;
               }
               break;
         }
      }
      else
      {
         super.broadcast(event);
      }
   }
   
   /**
    * @return Returns the bgcolor.
    */
   public String getBgcolor()
   {
      ValueBinding vb = getValueBinding("bgcolor");
      if (vb != null)
      {
         this.bgcolor = (String)vb.getValue(getFacesContext());
      }
      
      return this.bgcolor;
   }
   
   /**
    * @param bgcolor    The bgcolor to set.
    */
   public void setBgcolor(String bgcolor)
   {
      this.bgcolor = bgcolor;
   }

   /**
    * @return Returns the border name.
    */
   public String getBorder()
   {
      ValueBinding vb = getValueBinding("border");
      if (vb != null)
      {
         this.border = (String)vb.getValue(getFacesContext());
      }
      
      return this.border;
   }

   /**
    * @param border  The border name to user.
    */
   public void setBorder(String border)
   {
      this.border = border;
   }
   
   
   // ------------------------------------------------------------------------------
   // Private helpers
   
   /**
    * We use a hidden field name based on the parent form component Id and
    * the string "status" to give a hidden field name that can be shared by all status messages
    * within a single UIForm component.
    * 
    * @return hidden field name
    */
   private String getHiddenFieldName()
   {
      UIForm form = Utils.getParentForm(getFacesContext(), this);
      return form.getClientId(getFacesContext()) + NamingContainer.SEPARATOR_CHAR + "status";
   }
   
   
   // ------------------------------------------------------------------------------
   // Private members 
   
   public static final String STATUS_MESSAGE = "status-message";
   
   private final static String CSS_INFO      = "statusInfoText";
   private final static String CSS_WARNING   = "statusWarningText";
   private final static String CSS_ERROR     = "statusErrorText";
   
   private final static int ACTION_PREVIOUS = 0;
   private final static int ACTION_NEXT = 1;
   
   private final static int HISTORY_SIZE = 10;
   
   private final static String MSG_DEFAULT_STATUS = "status_message_default";
   
   private List<FacesMessage> messages = new LinkedList<FacesMessage>();
   private int currentMessage = 0;
   
   // component settings
   private String border = null;
   private String bgcolor = null;
   
   
   // ------------------------------------------------------------------------------
   // Inner classes
   
   /**
    * Class representing the an action that occurs when the previous/next buttons are clicked.
    */
   public static class MessageEvent extends ActionEvent
   {
      public MessageEvent(UIComponent component, int action)
      {
         super(component);
         Action = action;
      }
      
      public int Action;
   }
}
