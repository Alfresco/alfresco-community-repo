model.includeChildren = true;
model.includeContent = false;
model.isUser = false;

var object = null;

// allow for content to be loaded from id
if(args["id"] != null)
{
	var id = args["id"];
	object = search.findNode(id);
}

// if not by id, then allow for user id - but only if current user is the user!
else if(args["user"] != null)
{
	var userId = args["user"];
	if (userId == person.properties.userName || people.isAdmin(person))
	{
	   object = person;
	   model.isUser = true;
	   model.includeChildren = false;
	}
}

// load content by relative path
else
{
	var path = args["path"];
	if(path == null || path == "" || path == "/")
	{
		path = "/Company Home";
	}
	else
	{
		path = "/Company Home" + path;
	}
	
	// look up the content by path
	object = roothome.childByNamePath(path);
}

model.object = object;
model.mimetype = object.mimetype;