/**
 * Node Metadata Retrieval Service GET method
 */
function main()
{
   var json = "{}";
   
   // allow for content to be loaded from id
   if (args["nodeRef"] != null)
   {
   	var nodeRef = args["nodeRef"];
   	node = search.findNode(nodeRef);
   	
   	if (node != null)
   	{
   	   // if the node was found get JSON representation
   		if (args["shortQNames"] != null)
   		{
   			json = node.toJSON(true);
   		}
   		else
   		{
   			json = node.toJSON();
   		}
   	}
   }
   
   // store node onto model
   model.json = json;
}

main();