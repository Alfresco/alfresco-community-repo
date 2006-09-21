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
      if (component.isRendered() == true)
      {
         Writer out = context.getResponseWriter();
         
         UIActionLink link = (UIActionLink)component;
         
         UIComponent verticalContiner = getVerticalContainer(link);
         if (verticalContiner != null)
         {
            int padding = link.getPadding();
            
            if (verticalContiner instanceof UIActions)
            {
               padding = ((UIActions)verticalContiner).getVerticalSpacing();
            }
            // render as menu item style action link
            out.write( renderMenuAction(context, link, padding) );
         }
         else
         {
            // render as action link
            out.write( renderActionLink(context, link) );
         }
      }
   }
   
   /**
    * Render ActionLink as plain link and image
    * 
    * @param context
    * @param link
    * 
    * @return action link HTML
    */
   private String renderActionLink(FacesContext context, UIActionLink link)
   {
      // if there is no value for the link there will be no visible output
      // on the page so don't bother rendering anything
      String linkHtml = "";
      Object linkValue = link.getValue();
      
      if (linkValue != null)
      {
         Map attrs = link.getAttributes();
         StringBuilder linkBuf = new StringBuilder(256);
         
         if (link.getHref() == null)
         {
            linkBuf.append("<a href='#' onclick=\"");
            
            // if we have an overriden onclick add that
            if (link.getOnclick() != null)
            {
               linkBuf.append(link.getOnclick());
            }
            else
            {
               // generate JavaScript to set a hidden form field and submit
               // a form which request attributes that we can decode
               linkBuf.append(Utils.generateFormSubmit(context, link, Utils.getActionHiddenFieldName(context, link), link.getClientId(context), getParameterComponents(link)));
            }
            
            linkBuf.append('"');
         }
         else
         {
            String href = link.getHref();
            
            // prefix the web context path if required
            linkBuf.append("<a href=\"");
            if (href.startsWith("/"))
            {
               linkBuf.append(context.getExternalContext().getRequestContextPath());
            }
            linkBuf.append(href);
            
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
                     linkBuf.append('?');
                     first = false;
                  }
                  else
                  {
                     linkBuf.append('&');
                  }
                  try
                  {
                     linkBuf.append(name).append("=").append(URLEncoder.encode(paramValue, "UTF-8"));
                  }
                  catch (UnsupportedEncodingException err)
                  {
                     // if this happens we have bigger problems than a missing URL parameter...!
                  }
               }
            }
            
            linkBuf.append('"');
            
            // output href 'target' attribute if supplied
            if (link.getTarget() != null)
            {
               linkBuf.append(" target=\"")
                      .append(link.getTarget())
                      .append("\"");
            }
         }
         
         if (attrs.get("style") != null)
         {
            linkBuf.append(" style=\"")
                   .append(attrs.get("style"))
                   .append('"');
         }
         if (attrs.get("styleClass") != null)
         {
            linkBuf.append(" class=")
                   .append(attrs.get("styleClass"));
         }
         if (link.getTooltip() != null)
         {
            linkBuf.append(" title=\"")
                   .append(Utils.encode(link.getTooltip()))
                   .append('"');
         }
         linkBuf.append('>');
         
         StringBuilder buf = new StringBuilder(350);
         if (link.getImage() != null)
         {
            int padding = link.getPadding();
            if (padding != 0)
            {
               // TODO: make this width value a property!
               buf.append("<table cellspacing=0 cellpadding=0><tr><td width=16>");
            }
            
            if (link.getShowLink() == false)
            {
               buf.append(linkBuf.toString());
            }
            
            // TODO: allow configuring of alignment attribute
            buf.append(Utils.buildImageTag(context, link.getImage(), (String)link.getValue(), "absmiddle"));
            
            if (link.getShowLink() == false)
            {
               buf.append("</a>");
            }
            else
            {
               if (padding != 0)
               {
                  buf.append("</td><td style=\"padding:")
                     .append(padding)
                     .append("px\">");
               }
               else
               {
                  // TODO: add horizontal spacing as component property
                  buf.append("<span style='padding-left:2px");
                  
                  // text next to an image may need alignment
                  if (attrs.get("verticalAlign") != null)
                  {
                     buf.append(";vertical-align:")
                        .append(attrs.get("verticalAlign"));
                  }
                  
                  buf.append("'>");
               }
               
               buf.append(linkBuf.toString());
               buf.append(Utils.encode(link.getValue().toString()));
               buf.append("</a>");
               
               if (padding == 0)
               {
                  buf.append("</span>");
               }
            }
            
            if (padding != 0)
            {
               buf.append("</td></tr></table>");
            }
         }
         else
         {
            buf.append(linkBuf.toString());
            buf.append(Utils.encode(link.getValue().toString()));
            buf.append("</a>");
         }
         
         linkHtml = buf.toString();
      }
      
      return linkHtml;
   }
   
   /**
    * Render ActionLink as menu image and item link
    * 
    * @param context
    * @param link
    * 
    * @return action link HTML
    */
   private String renderMenuAction(FacesContext context, UIActionLink link, int padding)
   {
      // if there is no value for the link there will be no visible output
      // on the page so don't bother rendering anything
      String linkHtml = "";
      Object linkValue = link.getValue();
      
      if (linkValue != null)
      {
         StringBuilder buf = new StringBuilder(256);
         
         buf.append("<tr><td>");
         
         // render image cell first for a menu
         if (link.getImage() != null)
         {
            buf.append(Utils.buildImageTag(context, link.getImage(), (String)link.getValue()));
         }
         
         buf.append("</td><td");
         if (padding != 0)
         {
            buf.append(" style=\"padding:")
               .append(padding)
               .append("px\"");
         }
         buf.append(">");
         
         // render text link cell for the menu
         if (link.getHref() == null)
         {
            buf.append("<a href='#' onclick=\"");
            buf.append(Utils.generateFormSubmit(context, link, Utils.getActionHiddenFieldName(context, link), link.getClientId(context), getParameterComponents(link)));
            buf.append('"');
         }
         else
         {
            String href = link.getHref();
            if (href.startsWith("http") == false)
            {
               href = context.getExternalContext().getRequestContextPath() + href;
            }
            buf.append("<a href=\"")
               .append(href)
               .append('"');
            
            // output href 'target' attribute if supplied
            if (link.getTarget() != null)
            {
               buf.append(" target=\"")
                  .append(link.getTarget())
                  .append("\"");
            }
         }
         
         Map attrs = link.getAttributes();
         if (attrs.get("style") != null)
         {
            buf.append(" style=\"")
               .append(attrs.get("style"))
               .append('"');
         }
         if (attrs.get("styleClass") != null)
         {
            buf.append(" class=")
               .append(attrs.get("styleClass"));
         }
         buf.append('>');
         buf.append(Utils.encode(link.getValue().toString()));
         buf.append("</a>");
         
         buf.append("</td></tr>");
         
         linkHtml = buf.toString();
      }
      
      return linkHtml;
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
