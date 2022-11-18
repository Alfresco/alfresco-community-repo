<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/documentlibrary/permissions.get.js">

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

function getRmPermissions()
{
   /**
    * nodeRef input: store_type, store_id and id
    */
   var storeType = url.templateArgs.store_type,
      storeId = url.templateArgs.store_id,
      id = url.templateArgs.id,
      nodeRef = storeType + "://" + storeId + "/" + id,
      node = ParseArgs.resolveNode(nodeRef);

   if (node == null)
   {
      node = search.findNode(nodeRef);
      if (node === null)
      {
         status.setCode(status.STATUS_NOT_FOUND, "Not a valid nodeRef: '" + nodeRef + "'");
         return null;
      }
   }

   var permissionData = model.data,
      settable = node.getSettablePermissions(),
      canReadInherited = true;

   if (node.parent.hasPermission("ReadRecords"))
   {
      permissionData["inherited"] = parsePermissions(node.parent.getPermissions(), settable);
   }
   else
   {
      canReadInherited = false;
   }

   permissionData["canReadInherited"] = canReadInherited;

   model.data = permissionData;
}

getRmPermissions();
