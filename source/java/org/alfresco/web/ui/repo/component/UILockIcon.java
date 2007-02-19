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
package org.alfresco.web.ui.repo.component;

import java.io.IOException;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.SelfRenderingComponent;
import org.alfresco.web.ui.repo.WebResources;

/**
 * @author Kevin Roast
 */
public class UILockIcon extends SelfRenderingComponent
{
   private static final String MSG_LOCKED_YOU  = "locked_you";
   private static final String MSG_LOCKED_USER = "locked_user";
   
   // ------------------------------------------------------------------------------
   // Component implementation
   
   /**
    * @see javax.faces.component.UIComponent#getFamily()
    */
   public String getFamily()
   {
      return "org.alfresco.faces.LockIcon";
   }
   
   /**
    * @see javax.faces.component.StateHolder#restoreState(javax.faces.context.FacesContext, java.lang.Object)
    */
   public void restoreState(FacesContext context, Object state)
   {
      Object values[] = (Object[])state;
      // standard component attributes are restored by the super class
      super.restoreState(context, values[0]);
      this.lockImage = (String)values[1];
      this.lockOwnerImage = (String)values[2];
      this.align = (String)values[3];
      this.width = ((Integer)values[4]).intValue();
      this.height = ((Integer)values[5]).intValue();
      this.value = values[6];
   }
   
   /**
    * @see javax.faces.component.StateHolder#saveState(javax.faces.context.FacesContext)
    */
   public Object saveState(FacesContext context)
   {
      Object values[] = new Object[7];
      // standard component attributes are saved by the super class
      values[0] = super.saveState(context);
      values[1] = this.lockImage;
      values[2] = this.lockOwnerImage;
      values[3] = this.align;
      values[4] = this.width;
      values[5] = this.height;
      values[6] = this.value;
      return (values);
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
      
      // get the value and see if the image is locked
      NodeService nodeService = getNodeService(context);
      boolean locked = false;
      boolean lockedOwner = false;
      
      Object val = getValue();
      NodeRef ref = null;
      if (val instanceof NodeRef)
      {
         ref = (NodeRef)val;
         if (nodeService.exists(ref) && nodeService.hasAspect(ref, ContentModel.ASPECT_LOCKABLE) == true)
         {
            LockStatus lockStatus = getLockService(context).getLockStatus(ref);
            locked = (lockStatus == LockStatus.LOCKED || lockStatus == LockStatus.LOCK_OWNER);
            lockedOwner = (lockStatus == LockStatus.LOCK_OWNER);
         }
      }
      
      String msg = null;
      
      if (locked == true)
      {
         out.write("&nbsp;<img");
         
         outputAttribute(out, getAttributes().get("style"), "style");
         outputAttribute(out, getAttributes().get("styleClass"), "class");
         
         outputAttribute(out, getAlign(), "align");
         outputAttribute(out, getWidth(), "width");
         outputAttribute(out, getHeight(), "height");
         
         out.write("src=\"");
         out.write(context.getExternalContext().getRequestContextPath());
         String lockImage = getLockImage();
         if (lockedOwner == true && getLockOwnerImage() != null)
         {
            lockImage = getLockOwnerImage();
         }
         out.write(lockImage);
         out.write("\" border=0");
         
         if (lockedOwner == true)
         {
            msg = Application.getMessage(context, MSG_LOCKED_YOU);
            if (getLockedOwnerTooltip() != null)
            {
               msg = getLockedOwnerTooltip();
            }
         }
         else
         {
            String lockingUser = (String)nodeService.getProperty(ref, ContentModel.PROP_LOCK_OWNER);
            msg = Application.getMessage(context, MSG_LOCKED_USER);
            if (getLockedUserTooltip() != null)
            {
               msg = getLockedUserTooltip();
            }
            StringBuilder buf = new StringBuilder(32);
            msg = buf.append(msg).append(" '")
                     .append(lockingUser)
                     .append("'").toString();
         }
         
         msg = Utils.encode(msg);
         out.write(" alt=\"");
         out.write(msg);
         out.write("\" title=\"");
         out.write(msg);
         out.write("\">");
      }
   }
   
   /**
    * Use Spring JSF integration to return the Node Service bean instance
    * 
    * @param context    FacesContext
    * 
    * @return Node Service bean instance or throws exception if not found
    */
   private static NodeService getNodeService(FacesContext context)
   {
      NodeService service = Repository.getServiceRegistry(context).getNodeService();
      if (service == null)
      {
         throw new IllegalStateException("Unable to obtain NodeService bean reference.");
      }
      
      return service;
   }
   
   /**
    * Use Spring JSF integration to return the Lock Service bean instance
    * 
    * @param context    FacesContext
    * 
    * @return Lock Service bean instance or throws exception if not found
    */
   private static LockService getLockService(FacesContext context)
   {
      LockService service = Repository.getServiceRegistry(context).getLockService();
      if (service == null)
      {
         throw new IllegalStateException("Unable to obtain LockService bean reference.");
      }
      
      return service;
   }
   
   
   // ------------------------------------------------------------------------------
   // Strongly typed component property accessors 
   
   /**
    * @return the image to display as the lock icon. A default is provided if none is set.
    */
   public String getLockImage()
   {
      ValueBinding vb = getValueBinding("lockImage");
      if (vb != null)
      {
         this.lockImage = (String)vb.getValue(getFacesContext());
      }
      
      return this.lockImage;
   }
   
   /**
    * @param lockImage     the image to display as the lock icon. A default is provided if none is set.
    */
   public void setLockImage(String lockImage)
   {
      this.lockImage = lockImage;
   }
   
   /**
    * @return Returns the image to display if the owner has the lock.
    */
   public String getLockOwnerImage()
   {
      ValueBinding vb = getValueBinding("lockOwnerImage");
      if (vb != null)
      {
         this.lockOwnerImage = (String)vb.getValue(getFacesContext());
      }
      
      return this.lockOwnerImage;
   }

   /**
    * @param lockOwnerImage     the image to display if the owner has the lock.
    */
   public void setLockOwnerImage(String lockOwnerImage)
   {
      this.lockOwnerImage = lockOwnerImage;
   }
   
   /**
    * @return Returns the image alignment value.
    */
   public String getAlign()
   {
      ValueBinding vb = getValueBinding("align");
      if (vb != null)
      {
         this.align = (String)vb.getValue(getFacesContext());
      }
      
      return this.align;
   }

   /**
    * @param align      The image alignment value to set.
    */
   public void setAlign(String align)
   {
      this.align = align;
   }

   /**
    * @return Returns the icon height.
    */
   public int getHeight()
   {
      ValueBinding vb = getValueBinding("height");
      if (vb != null)
      {
         Integer value = (Integer)vb.getValue(getFacesContext());
         if (value != null)
         {
            this.height = value.intValue();
         }
      }
      
      return this.height;
   }

   /**
    * @param height         The icon height to set.
    */
   public void setHeight(int height)
   {
      this.height = height;
   }
   
   /**
    * @return Returns the icon width.
    */
   public int getWidth()
   {
      ValueBinding vb = getValueBinding("width");
      if (vb != null)
      {
         Integer value = (Integer)vb.getValue(getFacesContext());
         if (value != null)
         {
            this.width = value.intValue();
         }
      }
      
      return this.width;
   }

   /**
    * @param width      The iconwidth to set.
    */
   public void setWidth(int width)
   {
      this.width = width;
   }

   /**
    * @return Returns the lockedOwnerTooltip.
    */
   public String getLockedOwnerTooltip()
   {
      ValueBinding vb = getValueBinding("lockedOwnerTooltip");
      if (vb != null)
      {
         this.lockedOwnerTooltip = (String)vb.getValue(getFacesContext());
      }
      
      return this.lockedOwnerTooltip;
   }

   /**
    * @param lockedOwnerTooltip The lockedOwnerTooltip to set.
    */
   public void setLockedOwnerTooltip(String lockedOwnerTooltip)
   {
      this.lockedOwnerTooltip = lockedOwnerTooltip;
   }

   /**
    * @return Returns the lockedUserTooltip.
    */
   public String getLockedUserTooltip()
   {
      ValueBinding vb = getValueBinding("lockedUserTooltip");
      if (vb != null)
      {
         this.lockedUserTooltip = (String)vb.getValue(getFacesContext());
      }
      
      return this.lockedUserTooltip;
   }

   /**
    * @param lockedUserTooltip The lockedUserTooltip to set.
    */
   public void setLockedUserTooltip(String lockedUserTooltip)
   {
      this.lockedUserTooltip = lockedUserTooltip;
   }
   
   /**
    * @return Returns the value (Node or NodeRef)
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
    * @param value The Node or NodeRef value to set.
    */
   public void setValue(Object value)
   {
      this.value = value;
   }
   
   
   private String lockImage = WebResources.IMAGE_LOCK;
   private String lockOwnerImage = WebResources.IMAGE_LOCK_OWNER;
   private String align = null;
   private int width = 16;
   private int height = 16;
   private String lockedOwnerTooltip = null;
   private String lockedUserTooltip = null;
   private Object value = null;
}
