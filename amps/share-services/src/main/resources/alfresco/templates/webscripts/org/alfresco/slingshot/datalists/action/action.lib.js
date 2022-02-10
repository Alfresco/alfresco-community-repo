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
 * Data List Component: action
 *
 * For a single-asset action, template paramters address the item.
 * For multi-item actions, optional template parameters address the source or destination node,
 * and a JSON body addresses the items involved in the action.
 *
 * @param uri {string} node/{store_type}/{store_id}/{id}
 */

/**
 * Main script entry point
 * @method main
 */
function main()
{
   var nodeRef = null,
      rootNode = null,
      params = {};
   
   if (url.templateArgs.store_type !== null)
   {
      /**
       * nodeRef input: store_type, store_id and id
       */
      var storeType = url.templateArgs.store_type,
         storeId = url.templateArgs.store_id,
         id = url.templateArgs.id;

      nodeRef = storeType + "://" + storeId + "/" + id;
      rootNode = ParseArgs.resolveNode(nodeRef);
      if (rootNode == null)
      {
         rootNode = search.findNode(nodeRef);
         if (rootNode === null)
         {
            status.setCode(status.STATUS_NOT_FOUND, "Not a valid nodeRef: '" + nodeRef + "'");
            return null;
         }
      }
      
      params.nodeRef = nodeRef;
      params.rootNode = rootNode;
   }

   // Multiple input files in the JSON body?
   var items = getMultipleInputValues("nodeRefs");
   if (typeof items != "string")
   {
      params.items = items;
   }
   
   // Check runAction function is provided the action's webscript
   if (typeof runAction != "function")
   {
      status.setCode(status.STATUS_BAD_REQUEST, "Action webscript must provide runAction() function.");
      return;
   }

   // Actually run the action
   var results = runAction(params);
   if (results)
   {
      if (typeof results == "string")
      {
         status.setCode(status.STATUS_INTERNAL_SERVER_ERROR, results);
      }
      else if (typeof results.status == "object")
      {
         // Status fields have been manually set
         status.redirect = true;
         for (var s in results.status)
         {
            status[s] = results.status[s];
         }
      }
      else
      {
         /**
          * NOTE: Webscripts run within one transaction only.
          * If a single operation fails, the transaction is marked for rollback and all
          * previous (successful) operations are also therefore rolled back.
          * We therefore need to scan the results for a failed operation and mark the entire
          * set of operations as failed.
          */
         var overallSuccess = true,
            successCount = 0,
            failureCount = 0;

         for (var i = 0, j = results.length; i < j; i++)
         {
            overallSuccess = overallSuccess && results[i].success;
            results[i].success ? ++successCount : ++failureCount;
         }
         model.overallSuccess = overallSuccess;
         model.successCount = successCount;
         model.failureCount = failureCount;
         model.results = results;
      }
   }
}

/**
 * Get multiple input values
 *
 * @method getMultipleInputValues
 * @return {array|string} Array containing multiple values, or string error
 */
function getMultipleInputValues(param)
{
   var values = [],
      error = null;
   
   try
   {
      // Was a JSON parameter list supplied?
      if (typeof json != "undefined")
      {
         if (!json.isNull(param))
         {
            var jsonValues = json.get(param);
            // Convert from JSONArray to JavaScript array
            for (var i = 0, j = jsonValues.length(); i < j; i++)
            {
               values.push(jsonValues.get(i));
            }
         }
      }
   }
   catch(e)
   {
      error = e.toString();
   }
   
   // Return the values array, or the error string if it was set
   return (error !== null ? error : values);
}