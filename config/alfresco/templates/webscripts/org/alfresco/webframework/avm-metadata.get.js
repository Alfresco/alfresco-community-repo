model.includeChildren = true;
model.includeContent = false;

var object = null;

var storeId = url.templateArgs["storeId"];
var webappId = url.templateArgs["webappId"];
var path = url.templateArgs["path"];

var storeRootNode = avm.lookupStoreRoot(storeId);
if (storeRootNode != null)
{
	var path = storeRootNode.path + "/" + webappId + "/" + path;
	object = avm.lookupNode(path);
}

model.object = object;
