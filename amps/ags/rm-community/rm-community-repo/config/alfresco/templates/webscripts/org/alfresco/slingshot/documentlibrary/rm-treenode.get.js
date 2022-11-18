<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/documentlibrary/parse-args.lib.js">

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
 * Document List Component: treenode
 */
model.treenode = getTreenode();

/* Create collection of folders in the given space */
function getTreenode()
{
   try
   {
      var items = new Array(),
         hasSubfolders = true,
         ignoredTypes =
         {
            "{http://www.alfresco.org/model/forum/1.0}forum": true,
            "{http://www.alfresco.org/model/forum/1.0}topic": true,
            "{http://www.alfresco.org/model/content/1.0}systemfolder": true,
            "{http://www.alfresco.org/model/recordsmanagement/1.0}unfiledRecordContainer":true
         },
         skipPermissionCheck = args["perms"] == "false",
         evalChildFolders = false,
         item, rmNode, capabilities, cap;

      // Use helper function to get the arguments
      var parsedArgs = ParseArgs.getParsedArgs();
      if (parsedArgs === null)
      {
         return;
      }

      // Quick version if "skipPermissionCheck" flag set
      if (skipPermissionCheck)
      {
         for each (item in parsedArgs.pathNode.children)
         {
            if (itemIsAllowed(item) && !(item.type in ignoredTypes))
            {
               if (evalChildFolders)
               {
                  hasSubfolders = item.childFileFolders(false, true, "fm:forum").length > 0;
               }

               items.push(
               {
                  node: item,
                  hasSubfolders: hasSubfolders
               });
            }
         }
      }
      else
      {
         for each (item in parsedArgs.pathNode.children)
         {
            if (itemIsAllowed(item) && !(item.type in ignoredTypes))
            {
               //capabilities = {};
               rmNode = rmService.getRecordsManagementNode(item);

               //for each (cap in rmNode.capabilitiesSet("Create"))
               //{
               //   capabilities[cap.name] = true;
               //}

               //

               hasCreateCapability = rmNode.hasCapability("Create");

               if (evalChildFolders)
               {
                  hasSubfolders = item.childFileFolders(false, true, "fm:forum").length > 0;
               }

               items.push(
               {
                  node: item,
                  hasSubfolders: hasSubfolders,
                  permissions:
                  {
                     create: hasCreateCapability
                  }
               });
            }
         }
      }

      items.sort(sortByName);

      return (
      {
         parent: parsedArgs.pathNode,
         resultsTrimmed: false,
         items: items
      });
   }
   catch(e)
   {
      status.setCode(status.STATUS_INTERNAL_SERVER_ERROR, e.toString());
      return;
   }
}


/* Sort the results by case-insensitive name */
function sortByName(a, b)
{
   return (b.node.name.toLowerCase() > a.node.name.toLowerCase() ? -1 : 1);
}

/* Filter allowed types, etc. */
function itemIsAllowed(item)
{
   // Must be a subtype of cm:folder
   if (!item.isSubType("cm:folder"))
   {
      return false;
   }

   var typeShort = String(item.typeShort);

   // Don't show Hold and Transfer top-level containers
   if (typeShort == "rma:holdContainer" || typeShort == "rma:transferContainer" || typeShort == "rma:unfiledRecordContainer")
   {
      return false;
   }

   // Must be a "dod:" or "rma:" namespaced type
   if (typeShort.indexOf("dod:") !== 0 && typeShort.indexOf("rma") !== 0)
   {
      return false;
   }

   return true;
}
