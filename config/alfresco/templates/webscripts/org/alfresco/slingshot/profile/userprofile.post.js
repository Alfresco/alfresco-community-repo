/**
 * User Profile Update method
 * 
 * @method POST
 * @param json {string}
 *    {
 *       username : "username",
 *       properties : {
 *          "cm:propname" : "value"
 *          ...
 *       },
 *       content : {
 *          "cm:contentpropname" : "contentstringvalue"
 *          ...
 *       }
 *    }
 */

model.success = false;
var username = json.get("username");
if (username != null)
{
   var person = people.getPerson(username);
   // ensure we found a valid person and that it is the current user or an admin
   if (person != null && (username == person.properties.userName || people.isAdmin(person)))
   {
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
               person.properties[propname] = propval;
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
               person.properties[propname].content = propval;
            }
         }
      }
      
      // TODO: AVATAR?
      
      person.save();
      model.success = true;
   }
}