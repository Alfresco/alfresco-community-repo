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
 * Entry point for rmpermissions POST data webscript.
 * Applies supplied RM permissions to an RM node.
 *
 * @method main
 */
function main()
{
   // Get the node from the URL
   var pathSegments = url.match.split("/");
   var reference = [ url.templateArgs.store_type, url.templateArgs.store_id ].concat(url.templateArgs.id.split("/"));
   var node = search.findNode(pathSegments[2], reference);

   // 404 if the node is not found
   if (node == null)
   {
      status.setCode(status.STATUS_NOT_FOUND, "The node could not be found");
      return;
   }

   if (json.has("permissions") == false)
   {
      status.setCode(status.STATUS_BAD_REQUEST, "Permissions value missing from request.");
   }

   if (json.has("isInherited"))
   {
      node.setInheritsPermissions(json.getBoolean("isInherited"));
   }

   var permissions = json.getJSONArray("permissions");
   for (var i=0; i<permissions.length(); i++)
   {
      var p = permissions.getJSONObject(i);

      // collect values for the permission setting
      var role = p.getString("role");
      var authority = p.getString("authority");
      var remove = false;
      if (p.has("remove"))
      {
         remove = p.getBoolean("remove");
      }

      // apply or remove permission
      if (remove)
      {
         rmService.deletePermission(node, role, authority);
      }
      else
      {
         rmService.setPermission(node, role, authority);
      }
   }
}

main();
