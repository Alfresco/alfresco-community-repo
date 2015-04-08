/**
 * User Profile REST Update method
 * 
 * @method POST
 * @param json {string}
 *    {
 *       username: "username",
 *       properties:
 *       {
 *          "cm:propname": "value"
 *          ...
 *       },
 *       content:
 *       {
 *          "cm:contentpropname": "contentstringvalue"
 *          ...
 *       }
 *    }
 */

function main()
{
   model.success = false;
   var username = json.get("username");
   if (username == null)
   {
      status.code = 400;
      status.message = "Username parameter not supplied.";
      status.redirect = true;
      return;
   }
   
   var user = people.getPerson(username);
   // ensure we found a valid user and that it is the current user or we are an admin
   if (user == null ||
       (people.isAdmin(person) == false && user.properties.userName != person.properties.userName))
   {
      status.code = 500;
      status.message = "Failed to locate user to modify or permission denied.";
      status.redirect = true;
      return;
   }
   
   if (json.has("properties"))
   {
      var props = json.get("properties");
      if (props != null)
      {
         var names = props.names();
         for (var i=0; i<props.length(); i++)
         {
            var propname = names.get(i);
            var propval = props.get(propname);
            
            // set simple text properties
            user.properties[propname] = propval;
            
            // update userStatusTime if updating userStatus
            if (propname.toLowerCase() == "cm:userstatus")
            {
               user.properties["cm:userStatusTime"] = new Date();
            }
         }
      }
   }
   
   if (json.has("content"))
   {
      var props = json.get("content");
      if (props != null)
      {
         var names = props.names();
         for (var i=0; i<props.length(); i++)
         {
            var propname = names.get(i);
            var propval = props.get(propname);
            
            // set content property
            user.properties[propname].content = propval;
         }
      }
   }

   user.save();
   model.success = true;
}

main();