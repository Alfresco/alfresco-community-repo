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

var Common =
{
   /**
    * Cache for person objects
    */
   PeopleCache: {},

   /**
    * Gets / caches a person object
    *
    * @method getPerson
    * @param username {string} User name
    */
   getPerson: function Common_getPerson(username)
   {
      if (username == null || username == "")
      {
         return null;
      }

      if (typeof Common.PeopleCache[username] != "object")
      {
         var person = people.getPerson(username);
         if (person == null)
         {
            if (username == "System" || username.match("^System@") == "System@")
            {
               // special case for the System users
               person =
               {
                  properties:
                  {
                     userName: "System",
                     firstName: "System",
                     lastName: "User"
                  },
                  assocs: {}
               };
            }
            else
            {
               // missing person - may have been deleted from the database
               person =
               {
                  properties:
                  {
                     userName: username,
                     firstName: "",
                     lastName: ""
                  },
                  assocs: {}
               };
            }
         }
         Common.PeopleCache[username] =
         {
            userName: person.properties.userName,
            firstName: person.properties.firstName,
            lastName: person.properties.lastName,
            displayName: (person.properties.firstName + " " + person.properties.lastName).replace(/^\s+|\s+$/g, "")
         };
         if (person.assocs["cm:avatar"] != null)
         {
            Common.PeopleCache[username].avatar = person.assocs["cm:avatar"][0];
         }
      }
      return Common.PeopleCache[username];
   }
};

var ParseArgs =
{
   /**
    * Get and parse arguments
    *
    * @method getParsedArgs
    * @param containerType {string} Optional: Node Type of container to create if it doesn't exist, defaults to "cm:folder"
    * @return {array|null} Array containing the validated input parameters
    */
   getParsedArgs: function ParseArgs_getParsedArgs(containerType)
   {
      var rootNode = null,
         nodeRef = null,
         listNode = null;

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
         
         listNode = rootNode;
      }
      else
      {
         /**
          * Site and container input
          */
         var siteId = url.templateArgs.site,
            containerId = url.templateArgs.container,
            listId = url.templateArgs.list,
            siteNode = siteService.getSite(siteId);

         if (siteNode === null)
         {
            status.setCode(status.STATUS_NOT_FOUND, "Site not found: '" + siteId + "'");
            return null;
         }

         rootNode = siteNode.getContainer(containerId);
         if (rootNode === null)
         {
            rootNode = siteNode.createAndSaveContainer(containerId, containerType || "cm:folder", "Data Lists");
            if (rootNode === null)
            {
               status.setCode(status.STATUS_NOT_FOUND, "Data Lists container '" + containerId + "' not found in '" + siteId + "'. (No permission?)");
               return null;
            }
         }
         listNode = rootNode;
         
         if (listId !== null)
         {
            listNode = rootNode.childByNamePath(listId);
            if (listNode === null)
            {
               status.setCode(status.STATUS_NOT_FOUND, "List not found: '" + listId + "'");
               return null;
            }
         }
      }
      
      // Filter
      var filter = null;
      if (args.filter)
      {
         filter =
         {
            filterId: args.filter,
            filterData: args.filterData
         }
      }
      else if (typeof json !== "undefined" && json.has("filter"))
      {
         var filterJSON = json.get("filter");
         if (filterJSON != null)
         {
            filter =
            {
               filterId: filterJSON.get("filterId"),
               filterData: filterJSON.get("filterData")
            }
         }
         else
         {
            filter =
            {
               filterId: "all"
            }
         }
      }

      var objRet =
      {
         rootNode: rootNode,
         listNode: listNode,
         nodeRef: String(listNode.nodeRef),
         filter: filter
      };

      return objRet;
   },

   /**
    * Resolve "virtual" nodeRefs into nodes
    *
    * @method resolveVirtualNodeRef
    * @deprecated for ParseArgs.resolveNode
    */
   resolveVirtualNodeRef: function ParseArgs_resolveVirtualNodeRef(nodeRef)
   {
      if (logger.isLoggingEnabled())
      {
         logger.log("WARNING: ParseArgs.resolveVirtualNodeRef is deprecated for ParseArgs.resolveNode");
      }
      return ParseArgs.resolveNode(nodeRef);
   },

   /**
    * Resolve "virtual" nodeRefs, nodeRefs and xpath expressions into nodes
    *
    * @method resolveNode
    * @param reference {string} "virtual" nodeRef, nodeRef or xpath expressions
    * @return {ScriptNode|null} Node corresponding to supplied expression. Returns null if node cannot be resolved.
    */
   resolveNode: function ParseArgs_resolveNode(reference)
   {
      var node = null;
      try
      {
         if (reference == "alfresco://company/home")
         {
            node = companyhome;
         }
         else if (reference == "alfresco://user/home")
         {
            node = userhome;
         }
         else if (reference == "alfresco://sites/home")
         {
            node = companyhome.childrenByXPath("st:sites")[0];
         }
         else if (reference == "alfresco://shared")
         {
            node = companyhome.childrenByXPath("app:shared")[0];
         }
         else if (reference.indexOf("://") > 0)
         {
            node = search.findNode(reference);
         }
         else if (reference.substring(0, 1) == "/")
         {
            node = search.xpathSearch(reference)[0];
         }
      }
      catch (e)
      {
         return null;
      }
      return node;
   }
};
