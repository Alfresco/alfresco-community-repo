// onCreateNode policy code for app:projectfolder
var project = behaviour.args[0];
var results = search.luceneSearch("+PATH:\"" + project.qnamePath + "/*\" +ASPECT:\"{http://www.alfresco.org/model/emailserver/1.0}aliasable\"");
if (results.length == 1)
{
   results[0].properties["emailserver:alias"] = normalise(project.name);
   results[0].save();
   logger.log("Applied email alias of: " + normalise(project.name));
}
else
{
   logger.log("No email folder found!");
}

function normalise(s)
{
   // email alias has strict constraint
   return new String(s).toLowerCase().replace(/[^a-z^0-9^.]/g, "-");
}