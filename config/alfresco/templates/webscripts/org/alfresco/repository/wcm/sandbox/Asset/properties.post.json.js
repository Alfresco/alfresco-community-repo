/*
 *  Post -update a asset file/folder
 *  optionally updates simple content
 */  
function main()
{
	var urlElements = url.extension.split("/");
	var shortName = urlElements[0];
	var boxName = urlElements[2];
	var pathArray = urlElements.slice(5);
	var path = pathArray.join("/");

	var webproject = webprojects.getWebProject(shortName);
	if (null == webproject)
	{
		// Site cannot be found
		status.setCode(status.STATUS_NOT_FOUND, "The webproject, " + shortName + ", does not exist.");
		return;
	}
	var sandbox = webproject.getSandbox(boxName);
	if (null == sandbox)
	{
		// Site cannot be found
		status.setCode(status.STATUS_NOT_FOUND, "The sandbox, " + boxName + ", in webproject, "  +  shortName + ", does not exist.");
		return;
	}
	var asset = sandbox.getAsset(path); 

    if (null == asset)
    {
	    status.setCode(status.STATUS_NOT_FOUND, "The asset, " + path + ", in webproject, "  +  shortName + ", does not exist.");
	    return;
    }

    // Now read the values from the json form
//    if(!json.has("properties"))
//    {
//    	status.setCode(status.BAD_REQUEST, "JSON property 'properties' must specified");
//  	    return;
//    }

    var properties = null;
    if (json.has("properties"))
    {
    	properties = json.get("properties");
    }
    var content = null;

    if(json.has("content"))
    {
    	content = json.get("content");
    }

    if (null != properties)
    {
    	asset.setProperties(properties);
    	asset.save();
    }
    if (null != content)
    {
		asset.writeContent(content);
	}

	// set model properties
	model.sandbox = sandbox;
	model.webproject = webproject;
	model.asset = asset;

	status.code = status.STATUS_ACCEPTED;
}

main();
