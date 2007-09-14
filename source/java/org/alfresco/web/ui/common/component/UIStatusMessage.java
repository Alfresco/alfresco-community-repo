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
      return new Object[]
      {
         super.saveState(context),
         this.border,
         this.bgcolor,
         this.messages,
         Integer.valueOf(this.currentMessage)
      };
   }

   /**
    * @see javax.faces.component.UIComponentBase#encodeBegin(javax.faces.context.FacesContext)
    */
   public void encodeBegin(FacesContext context) throws IOException
   {
      if (!isRendered())
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
      out.write("<table style'width:100%;' cellspacing='0' cellpadding='0'><tr><td>");
      String field = getHiddenFieldName();
      String leftValue = getClientId(context) + NamingContainer.SEPARATOR_CHAR + Integer.toString(ACTION_PREVIOUS);
      String leftOnclick = Utils.generateFormSubmit(context, this, field, leftValue);
      out.write(Utils.buildImageTag(context, WebResources.IMAGE_MOVELEFT, 12, 12, null, leftOnclick, "middle"));
      out.write("</td><td style='width:100%;' align='center'>");
      
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
      
      out.write(Utils.buildImageTag(context, icon, null, "middle"));
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
      out.write(Utils.buildImageTag(context, WebResources.IMAGE_MOVERIGHT, 12, 12, null, rightOnclick, "middle"));
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
