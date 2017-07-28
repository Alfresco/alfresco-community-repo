logger.log("onAddAspect policy code for emailserver:aliasable");
var emailfolder = behaviour.args[0];
// search for an app:projectfolder parent node
var parent = emailfolder.parent;
while (parent != null && parent.hasPermission("Read"))
{
   if (parent.type == "{http://www.alfresco.org/model/application/1.0}projectfolder")
   {
      logger.log("Found parent app:project folder: " + parent.name);
      
      var alias = normalise(parent.name);
      parent.properties["emailserver:alias"] = alias;
      parent.save();
      logger.log("Applied email alias of: " + alias);
      
      break;
   }
   parent = parent.parent;
}

function normalise(s)
{
   // email alias has strict constraint
   return new String(s).toLowerCase().replace(/[^a-z^0-9^.]/g, "-");
}