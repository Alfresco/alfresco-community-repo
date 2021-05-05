<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/datalists/action/action.lib.js">

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
 * Duplicate multiple items action
 * @method POST
 */

/**
 * Entrypoint required by action.lib.js
 *
 * @method runAction
 * @param p_params {object} Object literal containing items array
 * @return {object|null} object representation of action results
 */
function runAction(p_params)
{
   var results = [],
      parentNode = p_params.rootNode,
      items = p_params.items,
      index, itemNode, result, nodeRef;

   // Must have parent node and array of items
   if (!parentNode)
   {
      status.setCode(status.STATUS_BAD_REQUEST, "No parent node supplied on URL.");
      return;
   }
   if (!items || items.length === 0)
   {
      status.setCode(status.STATUS_BAD_REQUEST, "No items supplied in JSON body.");
      return;
   }
   
   // Properties to skip when duplicating
   var propertiesToSkip =
   {
      "cm:name": true,
      "cm:content": true,
      "cm:created": true,
      "cm:creator": true,
      "cm:modified": true,
      "cm:modifier": true
   };
   
   for (index in items)
   {
      nodeRef = items[index];
      result =
      {
         nodeRef: nodeRef,
         action: "duplicateItem",
         success: false
      };
      
      try
      {
         itemNode = search.findNode(nodeRef);
         if (itemNode !== null)
         {
            var duplicateProperties = new Array(),
               propNames = itemNode.getPropertyNames(true),
               propName;
            
            // Copy selected properties from the original node
            for (var i = 0, ii = propNames.length; i < ii; i++)
            {
               propName = propNames[i];
               if (propName in propertiesToSkip || propName.indexOf("sys:") == 0)
               {
                  continue;
               }
               duplicateProperties[propName] = itemNode.properties[propName];
            }

            // Duplicate the node with a new GUID cm:name
            var newNode = parentNode.createNode(null, itemNode.type, duplicateProperties);
            if (newNode !== null)
            {
               // Now copy any associations
               for (var idxAssoc in itemNode.assocs)
               {
                  var assocs = itemNode.assocs[idxAssoc];
                  for (var j = 0, jj = assocs.length; j < jj; j++)
                  {
                     newNode.createAssociation(assocs[j], idxAssoc);
                  }
               }
               result.nodeRef = newNode.nodeRef.toString();
               result.success = true;
            }
         }
      }
      catch (e)
      {
         result.success = false;
      }
      
      results.push(result);
   }

   return results;
}

/* Bootstrap action script */
main();
