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
