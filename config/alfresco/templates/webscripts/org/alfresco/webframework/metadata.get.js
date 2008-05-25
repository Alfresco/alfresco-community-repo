var object = null;

// allow for content to be loaded from id
if(args["id"] != null)
{
	var id = args["id"];
	object = search.findNode(id);
}

// if not by id, then allow for user id
if(object == null && args["user"] != null)
{
	var userId = args["user"];
	object = people.getPerson(userId);
}

// load content by relative path
if(object == null)
{
	var path = args["path"];
	if(path == null || path == "" || path == "/")
		path = "/Company Home";
	else
		path = "/Company Home" + path;
	
	// look up the content by path
	object = roothome.childByNamePath(path);
}

// store onto model
model.object = object;
model.mimetype = object.mimetype;
model.includeChildren = true;
model.includeContent = false;