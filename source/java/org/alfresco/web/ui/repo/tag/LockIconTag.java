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
package org.alfresco.web.ui.repo.tag;

import javax.faces.component.UIComponent;

import org.alfresco.web.ui.common.tag.HtmlComponentTag;

/**
 * @author Kevin Roast
 */
public class LockIconTag extends HtmlComponentTag
{
   /**
    * @see javax.faces.webapp.UIComponentTag#getComponentType()
    */
   public String getComponentType()
   {
      return "org.alfresco.faces.LockIcon";
   }

   /**
    * @see javax.faces.webapp.UIComponentTag#getRendererType()
    */
   public String getRendererType()
   {
      return null;
   }
   
   /**
    * @see javax.faces.webapp.UIComponentTag#setProperties(javax.faces.component.UIComponent)
    */
   protected void setProperties(UIComponent component)
   {
      super.setProperties(component);
      setStringProperty(component, "lockImage", this.lockImage);
      setStringProperty(component, "lockOwnerImage", this.lockOwnerImage);
      setStringProperty(component, "align", this.align);
      setIntProperty(component, "width", this.width);
      setIntProperty(component, "height", this.height);
      setStringProperty(component, "lockedOwnerTooltip", this.lockedOwnerTooltip);
      setStringProperty(component, "lockedUserTooltip", this.lockedUserTooltip);
      setStringBindingProperty(component, "value", this.value);
   }
   
   /**
    * @see org.alfresco.web.ui.common.tag.HtmlComponentTag#release()
    */
   public void release()
   {
      super.release();
      this.lockImage = null;
      this.lockOwnerImage = null;
      this.align = null;
      this.width = null;
      this.height = null;
      this.lockedOwnerTooltip = null;
      this.lockedUserTooltip = null;
      this.value = null;
   }
   
   /**
    * Set the lockImage
    *
    * @param lockImage     the lockImage
    */
   public void setLockImage(String lockImage)
   {
      this.lockImage = lockImage;
   }

   /**
    * Set the lockOwnerImage
    *
    * @param lockOwnerImage     the lockOwnerImage
    */
   public void setLockOwnerImage(String lockOwnerImage)
   {
      this.lockOwnerImage = lockOwnerImage;
   }

   /**
    * Set the align
    *
    * @param align     the align
    */
   public void setAlign(String align)
   {
      this.align = align;
   }

   /**
    * Set the width
    *
    * @param width     the width
    */
   public void setWidth(String width)
   {
      this.width = width;
   }

   /**
    * Set the height
    *
    * @param height     the height
    */
   public void setHeight(String height)
   {
      this.height = height;
   }

   /**
    * Set the value
    *
    * @param value     the value
    */
   public void setValue(String value)
   {
      this.value = value;
   }
   
   /**
    * Set the lockedOwnerTooltip
    *
    * @param lockedOwnerTooltip     the lockedOwnerTooltip
    */
   public void setLockedOwnerTooltip(String lockedOwnerTooltip)
   {
      this.lockedOwnerTooltip = lockedOwnerTooltip;
   }

   /**
    * Set the lockedUserTooltip
    *
    * @param lockedUserTooltip     the lockedUserTooltip
    */
   public void setLockedUserTooltip(String lockedUserTooltip)
   {
      this.lockedUserTooltip = lockedUserTooltip;
   }


   /** the lockedOwnerTooltip */
   private String lockedOwnerTooltip;

   /** the lockedUserTooltip */
   private String lockedUserTooltip;
   
   /** the lockImage */
   private String lockImage;

   /** the lockOwnerImage */
   private String lockOwnerImage;

   /** the align */
   private String align;

   /** the width */
   private String width;

   /** the height */
   private String height;

   /** the value */
   private String value;
}
