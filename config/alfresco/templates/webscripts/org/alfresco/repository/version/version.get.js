
function main()
{

   var json = "";

   // allow for content to be loaded from id
   if(args["nodeRef"] != null)
   {
      var nodeRef = args["nodeRef"];
      node = search.findNode(nodeRef);

      var versions = null;
      if (node != null)
      {
         versions = [];
         var versionHistory = node.versionHistory;
         if(versionHistory != null)
         {
            for (i = 0; i < versionHistory.length; i++)
            {
               var version = versionHistory[i];
               var p = people.getPerson(versionHistory[0].creator);
               versions[versions.length] = {
                  nodeRef: version.node.nodeRef.toString(),
                  name: version.node.name,
                  label: version.label,
                  description: version.description,
                  createdDate: version.createdDate,
                  creator: {
                     userName: p.properties.userName,
                     firstName: p.properties.firstName,
                     lastName: p.properties.lastName
                  }
               };
            }
         }
         else
         {
            var p = people.getPerson(node.properties.creator);
            versions[0] = {
               nodeRef: node.nodeRef.toString(),
               name: node.name,
               label: "1.0",
               description: "",
               createdDate: node.properties.created,
               creator: {
                  userName: p.properties.userName,
                  firstName: p.properties.firstName,
                  lastName: p.properties.lastName
               }
            };
         }
      }
   }

   // store node onto model
   model.versions = versions;
}

main();