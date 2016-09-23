/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
/**
 * Update the details of a value in an rm constraint
 */ 
function main()
{
   var urlElements = url.extension.split("/");
   var shortName = urlElements[0];
   
   var values = null;
   
   if (json.has("values"))
   {
      values = json.getJSONArray("values");
   }
   
   if (values == null)
   {
      status.setCode(status.STATUS_BAD_REQUEST, "Values missing");
      return;
   }
  
   // Get the constraint
   var constraint = caveatConfig.getConstraint(shortName);
   
   if (constraint != null)
   {
      constraint.updateValues(values); 
      model.constraint = caveatConfig.getConstraint(shortName);
      model.constraintName = shortName; 
   }
   else
   {
      // Return 404
      status.setCode(404, "Constraint List " + shortName + " does not exist");
      return;
   }
}

main();
