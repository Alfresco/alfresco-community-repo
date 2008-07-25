// Get the store reference
var store = url.templateArgs.store_type + "://" + url.templateArgs.store_id;

var filter = args["tf"];
if (filter === null)
{
	// Get all the tags
	model.tags = taggingService.getTags(store);
}
else
{
	// Get a list of filtered tags
	model.tags = taggingService.getTags(store, filter);
}
	