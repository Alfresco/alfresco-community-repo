/**
 * User Status REST Update method
 * 
 * @method POST
 * @param json {string}
 *    {
 *       status: "value"
 *    }
 */

function main()
{
   model.success = false;
   
   if (json.has("status"))
   {
      var newStatus = json.get("status");
      if (newStatus != null)
      {
         person.properties["cm:userStatus"] = newStatus;
         person.properties["cm:userStatusTime"] = new Date();
      }

      person.save();
      model.success = true;
   }
}

main();