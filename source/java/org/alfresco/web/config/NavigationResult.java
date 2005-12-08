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
