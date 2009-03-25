var contentList = null;

if (args["storeid"] == null) {

	logger.log("ERROR: 'store' argument not specified.");

} else { 

	// Get the store id
	var storeId = args["storeid"];
	
	var store = avm.lookupStore(storeId);

	if ( store != null ) {
	
		// Get the form name
		var formName = args["form"];
		
		if ( formName != null ) {
		
			
			var method = (args.method == null) ? "lucene" : args.method;
			
			if ( method == "lucene" ) {
			
				// Run the lucene query
				var results = store.luceneSearch('@\\{http\\://www.alfresco.org/model/wcmappmodel/1.0\\}parentformname:"'+formName+'"');

				contentList = results;
				
			} else {
			
				var path = (args.path == null) ? "/ROOT" : args.path;
				
				var storeRootNode = avm.lookupStoreRoot(storeId);
				
				// try xpath???
				
				var results = avm.lookupNode(storeRootNode.path + path);
				
				var contentList = new Array();
				
				if ( results.children != null ) {
				
					for ( var i = 0 ; i < results.children.length ; i ++ ) {
					
						var item = results.children[i];
						
						if ( item.properties["{http://www.alfresco.org/model/wcmappmodel/1.0}parentformname"] == formName ) {
						
							contentList.push(item);
						
						}
					
					}
				
				}
			
			}
			
		}	

	} else {
	
		
	}
	
}

model.results= contentList;