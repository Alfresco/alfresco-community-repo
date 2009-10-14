model.includeChildren = true;
model.includeContent = false;
model.isUser = false;

var object = null;

// allow for content to be loaded from id
if (args["id"] != null)
{
	var id = args["id"];
	object = search.findNode(id);
}

// if not by id, then allow for user id
else if (args["user"] != null)
{
   var userId = args["user"];
   object = people.getPerson(userId);
   model.isAdmin = people.isAdmin(object);
   model.isGuest = people.isGuest(object);
   model.isUser = true;
   model.includeChildren = false;
}

// load content by relative path
else
{
	var path = args["path"];
	if (path == null || path == "" || path == "/")
	{
		path = "/Company Home";
	}
	
	// look up the content by path
	object = roothome.childByNamePath(path);
}

model.object = object;