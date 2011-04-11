<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/datalists/evaluator.lib.js">
<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/datalists/filters.lib.js">
<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/datalists/parse-args.lib.js">

/**
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Main entry point: Return data list with properties being supplied in POSTed arguments
 *
 * @method getData
 */
function getData()
{
   // Use helper function to get the arguments
   var parsedArgs = ParseArgs.getParsedArgs();
   if (parsedArgs === null)
   {
      return;
   }

   var fields = null;
   // Extract fields (if given)
   if (json.has("fields"))
   {
      // Convert the JSONArray object into a native JavaScript array
      fields = [];
      var jsonFields = json.get("fields"),
         numFields = jsonFields.length();
      
      for (count = 0; count < numFields; count++)
      {
         fields.push(jsonFields.get(count).replaceFirst("_", ":"));
      }
   }

   // Try to find a filter query based on the passed-in arguments
   var filter = parsedArgs.filter,
      allNodes = [], node,
      items = [];

   var filterParams = Filters.getFilterParams(filter, parsedArgs)
      query = filterParams.query;

   // Query the nodes - passing in default sort and result limit parameters
   if (query !== "")
   {
      allNodes = search.query(
      {
         query: query,
         language: filterParams.language,
         page:
         {
            maxItems: (filterParams.limitResults ? parseInt(filterParams.limitResults, 10) : 0)
         },
         sort: filterParams.sort,
         templates: filterParams.templates,
         namespace: (filterParams.namespace ? filterParams.namespace : null)
      });
      
      for each (node in allNodes)
      {
         try
         {
             items.push(Evaluator.run(node, fields));
         }
         catch(e) {}
      }
   }

   return (
   {
      fields: fields,
      luceneQuery: query,
      paging:
      {
         totalRecords: items.length,
         startIndex: 0
      },
      parent:
      {
         node: parsedArgs.listNode,
         userAccess:
         {
            create: parsedArgs.listNode.hasPermission("CreateChildren")
         }
      },
      items: items
   });
}

model.data = getData();