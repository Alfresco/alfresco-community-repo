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
         versionHistory, version, p;

      if (node != null)
      {
         var versionHistory = node.versionHistory;
         if (versionHistory != null)
         {
            for (i = 0; i < versionHistory.length; i++)
            {
               version = versionHistory[i];
               p = getPerson(version.creator);
               versions[versions.length] =
               {
                  nodeRef: version.node.nodeRef.toString(),
                  name: version.node.name,
                  label: version.label,
                  description: version.description,
                  createdDate: version.createdDate,
                  creator:
                  {
                     userName: p.userName,
                     firstName: p.firstName,
                     lastName: p.lastName
                  }
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
               }
            };
         }
      }
   }

   // store node onto model
   model.versions = versions;
}

main();