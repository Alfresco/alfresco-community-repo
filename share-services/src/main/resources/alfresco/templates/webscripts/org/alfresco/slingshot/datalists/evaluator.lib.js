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
    *
    * @method decorateFieldData
    * @param objData {object} Object literal containing this field's data
    * @param node {ScriptNode} The list item node for this field
    * @return {Boolean} false to prevent this field being added to the output stream.
    */
   decorateFieldData: function Evaluator_decorateFieldData(objData, node)
   {
      var value = objData.value,
         type = objData.type,
         obj;
      
      if (type == "cm:person")
      {
         obj = Evaluator.getPersonObject(value);
         if (obj == null)
         {
            return false;
         }
         objData.displayValue = obj.displayName;
         objData.metadata = obj.userName;
      }
      else if (type == "cm:folder")
      {
         obj = Evaluator.getContentObject(value);
         if (obj == null)
         {
            return false;
         }
         objData.displayValue = obj.displayPath.substring(companyhome.name.length() + 1);
         objData.metadata = "container";
      }
      else if (type.indexOf(":") > 0 && node.isSubType("cm:cmobject"))
      {
         obj = Evaluator.getContentObject(value);
         if (obj == null || !obj.hasPermission("Read"))
         {
            return false;
         }
         objData.displayValue = obj.properties["cm:name"];
         objData.metadata = obj.isContainer ? "container" : "document";
      }
      return true;
   },
   
   /**
    * Translates a List fieldDefinition
    *
    * @method translateField
    * @param objDef {FieldDefinition} objDef
    * @param value {String} default value
    */
   translateField: function Evaluator_translateField(objDef, value)
   {
      if (objDef == null || objDef == "")
      {
         return null;
      }
      if (objDef.constraints != null) 
      {
         for ( var i=0, len= objDef.constraints.size(); i<len; ++i )
         {          
             if ("LIST" == objDef.constraints.get(i).type) 
             {
                var allowedV = objDef.constraints.get(i).parameters.allowedValues;
                for (var j=0; j<allowedV.size(); ++j )
                {   
                    var allowedVasString = "" + allowedV.get(j);
                    var allValSplit = allowedVasString.split("|");
                    if (value == allValSplit[0]) {
                       return allValSplit[1];
                    }
                }
              }
          }
      }
      return value;
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
            values,
            type = isAssoc ? objDefinitions[k].endpointType : objDefinitions[k].dataType,
            endpointMany = isAssoc ? objDefinitions[k].endpointMany : false,
            objData =
            {
               type: type
            };

         if (value instanceof java.util.Date)
         {
            objData.value = utils.toISO8601(value);
            objData.displayValue = objData.value;
            nodeData[k] = objData;
         }
         else if (endpointMany)
         {
            if (value.length() > 0)
            {
               values = value.split(",");
               nodeData[k] = [];
               for each (value in values)
               {
                  var objLoop =
                  {
                     type: objData.type,
                     value: value,
                     displayValue: value
                  };

                  if (Evaluator.decorateFieldData(objLoop, node))
                  {
                     nodeData[k].push(objLoop);
                  }
               }
            }
         }
         else
         {
            objData.value = value;
            objData.displayValue = isAssoc ? value : Evaluator.translateField(objDefinitions[k],value);
            
            if (Evaluator.decorateFieldData(objData, node))
            {
               nodeData[k] = objData;
            }
         }
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
