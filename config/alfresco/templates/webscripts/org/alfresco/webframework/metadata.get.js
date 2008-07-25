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

// if not by id, then allow for user id
else if (args["user"] != null)
{
	var userId = args["user"];
	// TODO: only return "unprotected" properties if the user is not current or admin
	//       if (userId == person.properties.userName || people.isAdmin(person))
   object = people.getPerson(userId);
   model.isUser = true;
   model.includeChildren = false;
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