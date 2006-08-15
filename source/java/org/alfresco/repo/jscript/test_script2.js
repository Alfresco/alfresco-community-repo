// create add action
var addAspectAction = actions.create("add-features");
addAspectAction.parameters["aspect-name"] = "cm:lockable";

// execute action against passed in node    
addAspectAction.execute(doc);

// return
true;
