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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.web.ui.common.renderer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLEncoder;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.UIActionLink;
import org.alfresco.web.ui.common.component.UIMenu;
import org.alfresco.web.ui.repo.component.UIActions;

/**
 * @author kevinr
 */
public class ActionLinkRenderer extends BaseRenderer
{
   // ------------------------------------------------------------------------------
   // Renderer implementation 
   
   /**
    * @see javax.faces.render.Renderer#decode(javax.faces.context.FacesContext, javax.faces.component.UIComponent)
    */
   public void decode(FacesContext context, UIComponent component)
   {
      Map requestMap = context.getExternalContext().getRequestParameterMap();
      String fieldId = Utils.getActionHiddenFieldName(context, component);
      String value = (String)requestMap.get(fieldId);
      // we are clicked if the hidden field contained our client id
      if (value != null && value.equals(component.getClientId(context)))
      {
         // get all the params for this actionlink, see if any values have been set
         // on the request which match our params and set them into the component
         UIActionLink link = (UIActionLink)component;
         Map<String, String> destParams = link.getParameterMap();
         Map<String, String> actionParams = getParameterComponents(link);
         if (actionParams != null)
         {
            for (String name : actionParams.keySet())
            {
               String paramValue = (String)requestMap.get(name);
               destParams.put(name, paramValue);
            }
         }
         
         ActionEvent event = new ActionEvent(component);
         component.queueEvent(event);
      }
   }
   
   /**
    * @see javax.faces.render.Renderer#encodeEnd(javax.faces.context.FacesContext, javax.faces.component.UIComponent)
    */
   public void encodeEnd(FacesContext context, UIComponent component) throws IOException
   {
      // always check for this flag - as per the spec
      if (!component.isRendered())
      {
         return;
      }
      
      UIActionLink link = (UIActionLink)component;
      
      // if there is no value for the link there will be no visible output
      // on the page so don't bother rendering anything
      if (link.getValue() != null)
      {
         Writer out = context.getResponseWriter();
         
         UIComponent verticalContiner = getVerticalContainer(link);
         if (verticalContiner != null)
         {
            int padding = link.getPadding();
               
            if (verticalContiner instanceof UIActions)
            {
               padding = ((UIActions)verticalContiner).getVerticalSpacing();
            }
            // render as menu item style action link
            renderMenuAction(context, out, link, padding);
         }
         else
         {
            // render as action link
            renderActionLink(context, out, link);
         }
      }
   }
   
   /**
    * Render ActionLink as plain link and image
    * 
    * @param context
    * @param link
    */
   private void renderActionLink(FacesContext context, Writer out, UIActionLink link)
      throws IOException
   {
      // output the action link - with an image icon if specified, else just the text link part
      String image = link.getImage();
      if (image != null)
      {
         int padding = link.getPadding();
         if (padding != 0)
         {
            // TODO: make this width value a property!
            out.write("<table cellspacing=0 cellpadding=0><tr><td width=16>");
         }
         
         // if we are not show the text link, then the image is the clickable element
         if (link.getShowLink() == false)
         {
            renderActionLinkAnchor(context, out, link); 
         }
         
         out.write(Utils.buildImageTag(context, image, (String)link.getValue(), "absmiddle"));
         
         if (link.getShowLink() == false)
         {
            out.write("</a>");
         }
         else
         {
            if (padding != 0)
            {
               out.write("</td><td style=\"padding:");
               out.write(Integer.toString(padding));
               out.write("px\">");
            }
            
            // else the text is the clickable element
            renderActionLinkAnchor(context, out, link); 
            out.write(Utils.encode(link.getValue().toString()));
            out.write("</a>");
         }
         
         if (padding != 0)
         {
            out.write("</td></tr></table>");
         }
      }
      else
      {
         // no image, so text is the clickable element
         renderActionLinkAnchor(context, out, link); 
         out.write(Utils.encode(link.getValue().toString()));
         out.write("</a>");
      }
   }
   
   /**
    * Render ActionLink as plain link and image
    * 
    * @param context
    * @param link
    */
   private void renderActionLinkAnchor(FacesContext context, Writer out, UIActionLink link)
      throws IOException
   {
      Map attrs = link.getAttributes();
      
      // generate the href link - output later in the process depending on various rendering options
      if (link.getHref() == null)
      {
         out.write("<a href='#' onclick=\"");
         
         // if we have an overriden onclick add that
         if (link.getOnclick() != null)
         {
            out.write(link.getOnclick());
         }
         else
         {
            // generate JavaScript to set a hidden form field and submit
            // a form which request attributes that we can decode
            out.write(Utils.generateFormSubmit(context, link, Utils.getActionHiddenFieldName(context, link), link.getClientId(context), getParameterComponents(link)));
         }
         
         out.write('"');
      }
      else
      {
         String href = link.getHref();
         
         // prefix the web context path if required
         out.write("<a href=\"");
         if (href.startsWith("/"))
         {
            out.write(context.getExternalContext().getRequestContextPath());
         }
         out.write(href);
         
         // append the href params if any are present
         renderHrefParams(link, out, href);
         
         out.write('"');
         
         // output href 'target' attribute if supplied
         if (link.getTarget() != null)
         {
            out.write(" target=\"");
            out.write(link.getTarget());
            out.write("\"");
         }
      }
      
      // common link attributes
      if (attrs.get("id") != null)
      {
         out.write(" id=\"");
         out.write((String)attrs.get("id"));
         out.write("\"");
      }
      boolean appliedStyle = false;
      if (attrs.get("style") != null)
      {
         out.write(" style=\"");
         out.write((String)attrs.get("style"));
         out.write('"');
         appliedStyle = true;
      }
      if (attrs.get("styleClass") != null)
      {
         out.write(" class=");
         out.write((String)attrs.get("styleClass"));
         appliedStyle = true;
      }
      if (appliedStyle == false && link.getShowLink() == true && link.getImage() != null && link.getPadding() == 0)
      {
         // apply default alignment style if we have an image and no outer table padding
         out.write(" style='padding-left:2px;vertical-align:0%'");
      }
      if (link.getTooltip() != null)
      {
         out.write(" title=\"");
         out.write(Utils.encode(link.getTooltip()));
         out.write('"');
      }
      out.write('>');
   }

   /**
    * @param link
    * @param linkBuf
    * @param href
    */
   private void renderHrefParams(UIActionLink link, Writer out, String href)
      throws IOException
   {
      // append arguments if specified
      Map<String, String> actionParams = getParameterComponents(link);
      if (actionParams != null)
      {
         boolean first = (href.indexOf('?') == -1);
         for (String name : actionParams.keySet())
         {
            String paramValue = actionParams.get(name);
            if (first)
            {
               out.write('?');
               first = false;
            }
            else
            {
               out.write('&');
            }
            try
            {
               out.write(name);
               out.write("=");
               out.write(URLEncoder.encode(paramValue, "UTF-8"));
            }
            catch (UnsupportedEncodingException err)
            {
               // if this happens we have bigger problems than a missing URL parameter...!
            }
         }
      }
   }
   
   /**
    * Render ActionLink as menu image and item link
    * 
    * @param context
    * @param link
    */
   private void renderMenuAction(FacesContext context, Writer out, UIActionLink link, int padding)
      throws IOException
   {
      out.write("<tr><td>");
      
      // render image cell first for a menu
      if (link.getImage() != null)
      {
         out.write(Utils.buildImageTag(context, link.getImage(), (String)link.getValue()));
      }
      
      out.write("</td><td");
      if (padding != 0)
      {
         out.write(" style=\"padding:");
         out.write(Integer.toString(padding));
         out.write("px\">");
      }
      else
      {
         out.write(">");
      }
      
      // render text link cell for the menu
      if (link.getHref() == null)
      {
         out.write("<a href='#' onclick=\"");
         out.write(Utils.generateFormSubmit(context, link, Utils.getActionHiddenFieldName(context, link), link.getClientId(context), getParameterComponents(link)));
         out.write('"');
      }
      else
      {
         String href = link.getHref();
         if (href.startsWith("http") == false)
         {
            href = context.getExternalContext().getRequestContextPath() + href;
         }
         out.write("<a href=\"");
         out.write(href);
         
         // append the href params if any are present
         renderHrefParams(link, out, href);
         
         out.write('"');
         
         // output href 'target' attribute if supplied
         if (link.getTarget() != null)
         {
            out.write(" target=\"");
            out.write(link.getTarget());
            out.write("\"");
         }
      }
      
      Map attrs = link.getAttributes();
      if (attrs.get("style") != null)
      {
         out.write(" style=\"");
         out.write((String)attrs.get("style"));
         out.write('"');
      }
      if (attrs.get("styleClass") != null)
      {
         out.write(" class=");
         out.write((String)attrs.get("styleClass"));
      }
      out.write('>');
      out.write(Utils.encode(link.getValue().toString()));
      out.write("</a>");
      
      out.write("</td></tr>");
   }
   
   
   // ------------------------------------------------------------------------------
   // Private helpers
   
   /**
    * Return any vertically rendered container component the action link is present within 
    * 
    * @param link    The ActionLink to test
    * 
    * @return UIComponent vertically rendered component
    */
   private static UIComponent getVerticalContainer(UIActionLink link)
   {
      UIComponent parent = link.getParent();
      while (parent != null)
      {
         if (parent instanceof UIMenu ||
             (parent instanceof UIActions && ((UIActions)parent).getVerticalSpacing() != 0))
         {
            break;
         }
         parent = parent.getParent();
      }
      return parent;
   }
}
