// onCreateNode policy code for app:projectfolder
logger.log("onCreateNode policy code for app:projectfolder");
var project = behaviour.args[0].child;
if (project.children.length == 0)
{
   // perform deep copy of Project template contents into this node
   var templates = search.luceneSearch("+PATH:\"/app:company_home/app:dictionary/app:space_templates/*\" +TYPE:\"{http://www.alfresco.org/model/application/1.0}projectfolder\"");
   if (templates.length == 0)
   {
      logger.log("No app:project templates found to copy!");
   }
   else
   {
      logger.log("Copying app:project template into new project...");
      for each (var child in templates[0].children)
      {
         child.copy(project, true);
      }
      
      // search for the email archive child folder
      var results = search.luceneSearch("+PATH:\"" + project.qnamePath + "/*\" +ASPECT:\"{http://www.alfresco.org/model/emailserver/1.0}aliasable\"");
      if (results.length == 1)
      {
         results[0].properties["emailserver:alias"] = normalise(project.name);
         results[0].save();
         logger.log("Applied email alias of: " + normalise(project.name));
      }
      else
      {
         logger.log("No email archive folder found!");
      }
   }
}

function normalise(s)
{
   // email alias has strict constraint
   return new String(s).toLowerCase().replace(/[^a-z^0-9^.]/g, "-");
}