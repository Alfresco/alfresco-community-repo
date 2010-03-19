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

var Evaluator =
{
   /**
    * Cache for cm:person objects
    */
   PeopleObjectCache: {},

   /**
    * Gets / caches a person object
    *
    * @method getPersonObject
    * @param nodeRef {string} NodeRef of a cm:person object
    */
   getPersonObject: function Evaluator_getPersonObject(nodeRef)
   {
      if (nodeRef == null || nodeRef == "")
      {
         return null;
      }

      if (typeof Evaluator.PeopleObjectCache[nodeRef] == "undefined")
      {
         var person = search.findNode(nodeRef);
         Evaluator.PeopleObjectCache[nodeRef] =
         {
            userName: person.properties.userName,
            firstName: person.properties.firstName,
            lastName: person.properties.lastName,
            displayName: (person.properties.firstName + " " + person.properties.lastName).replace(/^\s+|\s+$/g, "")
         };
         if (person.assocs["cm:avatar"] != null)
         {
            Evaluator.PeopleObjectCache[nodeRef].avatar = person.assocs["cm:avatar"][0];
         }
      }
      return Evaluator.PeopleObjectCache[nodeRef];
   },

   /**
    * Cache for nodes that are subtypes of cm:cmobject
    */
   ContentObjectCache: {},

   /**
    * Gets / caches a content object
    *
    * @method getContentObject
    * @param nodeRef {string} NodeRef
    */
   getContentObject: function Evaluator_getContentObject(nodeRef)
   {
      if (nodeRef == null || nodeRef == "")
      {
         return null;
      }

      if (typeof Evaluator.ContentObjectCache[nodeRef] == "undefined")
      {
         var node = search.findNode(nodeRef);
         try
         {
            Evaluator.ContentObjectCache[nodeRef] = node;
         }
         catch(e)
         {
            // Possibly a stale indexed node
            return null;
         }
      }
      return Evaluator.ContentObjectCache[nodeRef];
   },

   /**
    * Generate displayValue and any extra metadata for this field
    */
   decorateFieldData: function Evaluator_decorateFieldData(objData, node)
   {
      var value = objData.value,
         type = objData.type,
         displayValue = value,
         obj;
      
      if (type == "cm:person")
      {
         obj = Evaluator.getPersonObject(value);
         if (obj != null)
         {
            displayValue = obj.displayName;
            objData.metadata = obj.userName;
         }
      }
      else if (type == "datetime" || type == "date")
      {
         objData.metadata = type;
      }
      else if (node.isSubType("cm:folder"))
      {
         obj = getContentObject(node.nodeRef);
         if (obj != null)
         {
            displayValue = obj.displayPath.substring(companyhome.name.length() + 1);
         }
      }
      else if (node.isSubType("cm:object"))
      {
         obj = getContentObject(node.nodeRef);
         if (obj != null)
         {
            displayValue = obj.properties["cm:name"];
         }
      }
      
      objData.displayValue = displayValue;
   },
   
   /**
    * Node Evaluator - main entrypoint
    */
   run: function Evaluator_run(node, fields)
   {
      var permissions = {},
         actionSet = "",
         actionLabels = {},
         createdBy = Common.getPerson(node.properties["cm:creator"]),
         modifiedBy = Common.getPerson(node.properties["cm:modifier"]),
         nodeData = {};

      /**
       * PERMISSIONS
       */
      permissions =
      {
         "create": node.hasPermission("CreateChildren"),
         "edit": node.hasPermission("Write"),
         "delete": node.hasPermission("Delete")
      };

      // Use the form service to parse the required properties
      scriptObj = formService.getForm("node", node.nodeRef, fields, fields);

      // Make sure we can quickly look-up the Field Definition within the formData loop...
      var objDefinitions = {};
      for each (formDef in scriptObj.fieldDefinitions)
      {
         objDefinitions[formDef.dataKeyName] = formDef;
      }

      // Populate the data model
      var formData = scriptObj.formData.data;
      for (var k in formData)
      {
         var isAssoc = k.indexOf("assoc") == 0,
            value = formData[k].value,
            type = isAssoc ? objDefinitions[k].endpointType : objDefinitions[k].dataType,
            objData =
            {
               type: type
            };

         if (value instanceof java.util.Date)
         {
            objData.value = utils.toISO8601(value);
         }
         else
         {
            // java.util.List instances are returned from ScriptFormData.java as Strings
            objData.value = value;
         }

         Evaluator.decorateFieldData(objData, node);
         nodeData[k] = objData;
      }

      return(
      {
         node: node,
         nodeData: nodeData,
         actionSet: actionSet,
         actionPermissions: permissions,
         createdBy: createdBy,
         modifiedBy: modifiedBy,
         tags: node.tags,
         actionLabels: actionLabels
      });
   }
};
