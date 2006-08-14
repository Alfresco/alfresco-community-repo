// create add action
var addAspectAction = actions.createAction("add-features");
addAspectAction.parameters["aspect-name"] = "cm:lockable";

// execute action against passed in node    
addAspectAction.execute(doc);

// return
true;
