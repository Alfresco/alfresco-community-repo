// arguments
var jsonString = args["json"];

model.status = null;

if(jsonString != null)
{
	// load arguments into object
	var json = eval('(' + jsonString + ')');

	// attributes
	var id = json.id;
	var title = json.title;
	var description = json.description;
	
	// TODO: create the web project
	
	// set back onto return
	model.webProjectId = id;
	model.storeId = id;
	model.sandboxId = id;

	model.status = 'ok';
}

if(model.status == null)
{
	model.status = 'error';
}
 