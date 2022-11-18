<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/documentlibrary/action/action.lib.js">

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
 * Copy multiple files action
 * @method POST
 */

/**
 * Entrypoint required by action.lib.js
 *
 * @method runAction
 * @param p_params {object} Object literal containing files array
 * @return {object|null} object representation of action results
 */
function runAction(p_params)
{
   var results = [],
      destNode = p_params.destNode,
      files = p_params.files,
      file, fileNode, result, nodeRef,
      fromSite, copiedNode;

   // Must have array of files
   if (!files || files.length == 0)
   {
      status.setCode(status.STATUS_BAD_REQUEST, "No files.");
      return;
   }

   for (file in files)
   {
      nodeRef = files[file];
      result =
      {
         nodeRef: nodeRef,
         action: "copyFile",
         success: false
      };

      try
      {
         fileNode = search.findNode(nodeRef);
         if (fileNode == null)
         {
            result.id = file;
            result.nodeRef = nodeRef;
            result.success = false;
            result.error = "Can't find source node.";
         }
         if (!rmService.getRecordsManagementNode(destNode).hasCapability("FillingPermissionOnly"))
         {
            result.name = fileNode.name;
            result.error = "You don't have filing permission on the destination or the destination is either frozen, closed or cut off!";
            results.push(result);
            continue;
         }
         else
         {
            result.id = fileNode.name;
            result.name = fileNode.name;
            result.type = fileNode.isContainer ? "folder" : "document"

            // Retain the name of the site the node is currently in. Null if it's not in a site.
            fromSite = String(fileNode.siteShortName);

            // copy the node (deep copy for containers)
            if (fileNode.isContainer)
            {
               copiedNode = fileNode.copy(destNode, true);
            }
            else
            {
               copiedNode = fileNode.copy(destNode);
            }

            result.nodeRef = copiedNode.nodeRef.toString();
            result.success = (result.nodeRef != null);

            if (result.success)
            {
               // If this was an inter-site copy, we'll need to clean up the permissions on the node
               if (fromSite != String(copiedNode.siteShortName))
               {
                  siteService.cleanSitePermissions(copiedNode);
               }
            }
         }
      }
      catch (e)
      {
         result.id = file;
         result.nodeRef = nodeRef;
         result.success = false;
         result.error = e.message;

         // log the error
         logger.error(e.message);
      }

      results.push(result);
   }

   return results;
}

/* Bootstrap action script */
main();
