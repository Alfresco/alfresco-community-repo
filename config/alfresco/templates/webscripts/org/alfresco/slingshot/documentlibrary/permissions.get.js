<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/documentlibrary/parse-args.lib.js">

/**
 * Main entry point: Retrieve permissions and associated metadata for given node
 *
 * @method getPermissions
 */
function getPermissions()
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

   // Get array of settable permissions
   var settable = node.getSettablePermissions(),
      location = Common.getLocation(node);
   
   // If this node lives within a Site, then append the Site-specific roles
   if (location.siteNode != null)
   {
      settable = settable.concat(location.siteNode.getNode().getSettablePermissions());
   }

   // Get full permission set, including inherited
   // [ALLOWED|DENIED];[USERNAME|GROUPNAME|ROLE];PERMISSION;[INHERITED|DIRECT]
   var isInherited = node.inheritsPermissions(),
      nodePermissions = parsePermissions(node.getDirectPermissions(), settable),
      inheritedPermissions = [],
      canReadInherited = true;
   
   if (node.parent.hasPermission("ReadPermissions"))
   {
      inheritedPermissions = parsePermissions(node.parent.getPermissions(), settable);
   }
   else
   {
      canReadInherited = false;
   }

   return (
   {
      inherited: inheritedPermissions,
      isInherited: isInherited,
      canReadInherited: canReadInherited,
      direct: nodePermissions,
      settable: settable
   });
}

function parsePermissions(p_permissions, p_settable)
{
   var results = [],
      settable = {},
      tokens, authority, authorityId, role, i, ii;

   // Settable array into object for "x in y" style operations
   for (i = 0, ii = p_settable.length; i < ii; i++)
   {
      if (p_settable[i] !== undefined)
      {
         settable[p_settable[i]] = true;
      }
   }

   for (i = 0, ii = p_permissions.length; i < ii; i++)
   {
      tokens = p_permissions[i].split(";");
      authorityId = tokens[1];
      role = tokens[2];

      // Only return ALLOWED permissions
      if (tokens[0] == "ALLOWED")
      {
         // Resolve to group or user as appropriate
         if (authorityId.indexOf("GROUP_") === 0)
         {
            authority = Common.getGroup(authorityId);
         }
         else if (authorityId.indexOf("ROLE_") === 0)
         {
            authority =
            {
               avatar: null,
               name: authorityId,
               displayName: null
            };
            nameProperty = "name";
         }
         else
         {
            authority = Common.getPerson(authorityId);
         }
      
         if (authority != null)
         {
            results.push(
            {
               authority:
               {
                  avatar: authority.avatar || null,
                  name: authorityId,
                  displayName: authority["displayName"]
               },
               role: role
            });
         }
      }
   }
   return results;
}

/**
 * Document List Component: permissions
 */
model.data = getPermissions();
