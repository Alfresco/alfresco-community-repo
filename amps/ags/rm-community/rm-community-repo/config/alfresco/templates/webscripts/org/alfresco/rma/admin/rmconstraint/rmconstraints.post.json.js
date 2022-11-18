<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/rma/admin/rmconstraint/rmconstraint-utils.js">

/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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
 * Create a new RM Constraint List
 */
function main()
{
   // Parse the passed in details
   var title = null,
      name = null,
      allowedValues = {};

   if (json.has("constraintName"))
   {
      name = json.get("constraintName");
   }

   if (json.has("constraintTitle"))
   {
      title = json.get("constraintTitle");
   }
   else
   {
      title = name;
   }

   if (existsTitle(caveatConfig, title))
   {
      status.code = 400;
      model.errorMessage = "rm.admin.list-already-exists";
      model.title = title;
      return;
   }

   if (json.has("allowedValues"))
   {
      values = json.getJSONArray("allowedValues");

      var i = 0;
      allowedValues = new Array();

      if (values != null)
      {
         for (var x = 0; x < values.length(); x++)
         {
            allowedValues[i++] = values.get(x);
         }
      }
   }

   model.constraint = caveatConfig.createConstraint(name, title, allowedValues);
}

main();
