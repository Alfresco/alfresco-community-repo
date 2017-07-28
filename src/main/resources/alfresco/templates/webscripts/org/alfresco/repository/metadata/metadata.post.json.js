main();

function main()
{
   if (url.templateArgs.store_type === null)
   {
      status.setCode(status.STATUS_BAD_REQUEST, "NodeRef missing");
      return;
   }

   // nodeRef input
   var storeType = url.templateArgs.store_type;
   var storeId = url.templateArgs.store_id;
   var id = url.templateArgs.id;
   var nodeRef = storeType + "://" + storeId + "/" + id;
   var node = search.findNode(nodeRef);
   if (node === null)
   {
      status.setCode(status.STATUS_NOT_FOUND, "Not a valid nodeRef: '" + nodeRef + "'");
      return null;
   }

   // Set passed-in properties
   if (json.has("properties"))
   {
      var properties = jsonToObject("properties");
      for (property in properties)
      {
         node.properties[property] = properties[property];
      }
   }

   // Set passed-in mimetype
   if (json.has("mimetype"))
   {
      node.mimetype = json.get("mimetype");
   }
   
   // Set passed-in tags
   if (json.has("tags"))
   {
      // Get the tags JSONArray and copy it to a native JavaScript array
      var jsonTags = json.get("tags"),
         tags = [];
      
      for (var t = 0; t < jsonTags.length(); t++)
      {
         tags.push(jsonTags.get(t));
      }

      if (tags.length > 0)
      {
         node.tags = tags;
      }
      else
      {
         node.clearTags();
      }
   }
   node.save();
}

function jsonToObject(p_name)
{
   var object = {};
   var jsonObj = json.get(p_name);
   var names = jsonObj.names();
   var name;
   for (var i = 0, j = names.length(); i < j; i++)
   {
      name = names.get(i);
      object[name] = jsonObj.get(name);
   }
   return object;
}

function jsonToArray(p_name)
{
   var array = [];
   var jsonArray = json.get(p_name);
   for (var i = 0, j = jsonArray.length(); i < j; i++)
   {
      array.push(jsonArray.get(i));
   }
   return array;
}