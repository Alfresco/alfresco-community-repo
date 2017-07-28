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
