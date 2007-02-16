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
package org.alfresco.web.config;

/**
 * Represents the result of a navigation config result.
 * 
 * This object holds the string result which can either represent an outcome
 * or a view id. 
 * 
 * @author gavinc
 */
public class NavigationResult
{
   private String result;
   private boolean isOutcome = true;
   
   /**
    * Default constructor
    * 
    * @param viewId The to-view-id value
    * @param outcome The to-outcome value
    */
   public NavigationResult(String viewId, String outcome)
   {
      if (viewId != null && outcome != null)
      {
         throw new IllegalStateException("You can not have both a to-view-id and to-outcome");
      }
      
      if (outcome != null)
      {
         this.result = outcome;
      }
      else if (viewId != null)
      {
         this.result = viewId;
         this.isOutcome = false;
      }
   }
   
   /**
    * Returns the result
    * 
    * @return The result
    */
   public String getResult()
   {
      return this.result;
   }
   
   /**
    * Determines whether the result is an outcome
    * 
    * @return true if the result represents an outcome, 
    *         false if it represents a view id
    */
   public boolean isOutcome()
   {
      return this.isOutcome;
   }
   
   /**
    * @see java.lang.Object#toString()
    */
   public String toString()
   {
      StringBuilder buffer = new StringBuilder(super.toString());
      buffer.append(" (result=").append(this.result);
      buffer.append(" isOutcome=").append(this.isOutcome).append(")");
      return buffer.toString();
   }
}
