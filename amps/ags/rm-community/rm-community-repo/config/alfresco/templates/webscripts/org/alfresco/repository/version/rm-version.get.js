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
var PeopleCache = {};

/**
 * Gets / caches a person object
 * @method getPerson
 * @param username {string} User name
 */
function getPerson(username)
{
   if (typeof PeopleCache[username] == "undefined")
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
      PeopleCache[username] =
      {
         userName: person.properties.userName,
         firstName: person.properties.firstName,
         lastName: person.properties.lastName,
         displayName: (person.properties.firstName + " " + person.properties.lastName).replace(/^\s+|\s+$/g, "")
      };
   }
   return PeopleCache[username];
}

function main()
{
   var json = "",
      versions = [];

   // allow for content to be loaded from id
   if (args["nodeRef"] != null)
   {
      var nodeRef = args["nodeRef"],
         node = search.findNode(nodeRef),
         versionHistory, version, p, recordNodeRef, isRecordedVersionDestroyed;

      if (node != null)
      {
         var versionHistory = node.versionHistory;
         if (versionHistory != null)
         {
            for (i = 0; i < versionHistory.length; i++)
            {
               version = versionHistory[i];
               p = getPerson(version.creator);
               
               recordNodeRef = version.getVersionProperty("recordNodeRef");
               isRecordedVersionDestroyed = version.getVersionProperty("RecordedVersionDestroyed");
               
               versions[versions.length] =
               {
                  nodeRef: version.node.nodeRef.toString(),
                  name: (isRecordedVersionDestroyed == true) ? "" : version.node.name,
                  label: version.label,
                  description: version.description,
                  createdDate: version.createdDate,
                  creator:
                  {
                     userName: p.userName,
                     firstName: p.firstName,
                     lastName: p.lastName
                  },
                  recordNodeRef: recordNodeRef ? recordNodeRef.toString() : "",
                  isRecordedVersionDestroyed: isRecordedVersionDestroyed
               };
            }
         }
         else
         {
            p = getPerson(node.properties.creator);
            versions[0] =
            {
               nodeRef: node.nodeRef.toString(),
               name: node.name,
               label: "1.0",
               description: "",
               createdDate: node.properties.created,
               creator:
               {
                  userName: p.userName,
                  firstName: p.firstName,
                  lastName: p.lastName
               },
               recordNodeRef: "",
               isRecordedVersionDestroyed: false
            };
         }
      }
   }

   // store node onto model
   model.versions = versions;
}

main();
