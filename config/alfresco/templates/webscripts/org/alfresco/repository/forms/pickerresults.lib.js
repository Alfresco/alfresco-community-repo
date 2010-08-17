/**
 * Creates an Object representing the given node.
 * 
 * Also determines whether the node is located within a site, if it
 * is a "site" property is provided where the value is the site short
 * name.
 * 
 * @method createNodeResult
 * @param node
 * @return Object representing the node
 */
function createNodeResult(node)
{
   var nodeObject = 
   {
      typeShort: node.typeShort,
      isContainer: node.isContainer,
      children: node.children,
      properties: {},
      displayPath: node.displayPath,
      nodeRef: "" + node.nodeRef,
   }
   
   // add required properties
   nodeObject.properties.name = node.properties.name;
   nodeObject.properties.title = node.properties.title;
   nodeObject.properties.description = node.properties.description;
   nodeObject.properties.modified = node.properties.modified;
   nodeObject.properties.modifier = node.properties.modifier;
   
   // determine if the node is in a site and if so, which one
   if (node.qnamePath != null)
   {
      var paths = node.qnamePath.split("/");
      for (var i = 0, ii = paths.length; i < ii; i++)
      {
         if (paths[i] == "st:sites")
         {
            // we now know the node is in a site, find
            // the next element in the array (if there 
            // is one) to get the site name
            
            if ((i+1) < paths.length)
            {
               var siteName = paths[i+1];
               
               // remove the "cm:" prefix and add to result object
               nodeObject.site = siteName.substring(3);
            }
            
            break;
         }
      }
   }
   
   return nodeObject;
}

/**
 * Creates an Object representing the given person node.
 * 
 * @method createPersonResult
 * @param node
 * @return Object representing the person
 */
function createPersonResult(node)
{
   var personObject = 
   {
      typeShort: node.typeShort,
      isContainer: false,
      children: [],
      properties: {},
      displayPath: node.displayPath,
      nodeRef: "" + node.nodeRef
   }
   
   // define properties for person
   personObject.properties.userName = node.properties.userName;
   personObject.properties.name = (node.properties.firstName ? node.properties.firstName + " " : "") + 
      (node.properties.lastName ? node.properties.lastName : "") +
         " (" + node.properties.userName + ")";
   personObject.properties.jobtitle = (node.properties.jobtitle ? node.properties.jobtitle  : "");
   
   return personObject;
}

/**
 * Creates an Object representing the given group node.
 * 
 * @method createGroupResult
 * @param node
 * @return Object representing the group
 */
function createGroupResult(node)
{
   var groupObject = 
   {
      typeShort: node.typeShort,
      isContainer: false,
      children: [],
      properties: {},
      displayPath: node.displayPath,
      nodeRef: "" + node.nodeRef
   }
   
   // find most appropriate name for the group
   var name = node.properties.name;
   if (node.properties.authorityDisplayName != null && node.properties.authorityDisplayName.length > 0)
   {
      name = node.properties.authorityDisplayName;
   }
   else if (node.properties.authorityName != null && node.properties.authorityName.length > 0)
   {
      var authName = node.properties.authorityName;
      if (authName.indexOf("GROUP_") == 0)
      {
         name = authName.substring(6);
      }
      else
      {
         name = authName;
      }
   }
   
   // set the name
   groupObject.properties.name = name;
   
   return groupObject;
}
