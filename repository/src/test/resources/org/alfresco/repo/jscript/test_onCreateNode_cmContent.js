var scriptFailed = false;

// Have a look at the behaviour object that should have been passed
if (behaviour == null)
{
    logger.log("The behaviour object has not been set.");
    scriptFailed = true;
}

// Check the name of the behaviour
if (behaviour.name == null && behaviour.name != "onCreateNode")
{
    logger.log("The behaviour name has not been set correctly.");    
    scriptFailed = true;
}
else
{
    logger.log("Behaviour name: " + behaviour.name);
}

// Check the arguments
if (behaviour.args == null)
{
    logger.log("The args have not been set.")
    scriptFailed = true;
}
else
{
    if (behaviour.args.length == 1)    
    {
        var childAssoc = behaviour.args[0];
        logger.log("Assoc type: " + childAssoc.type);
        logger.log("Assoc name: " + childAssoc.name);
        logger.log("Parent node: " + childAssoc.parent.id);
        logger.log("Child node: " + childAssoc.child.id);
        logger.log("Is primary: " + childAssoc.isPrimary());
        logger.log("Nth sibling: " + childAssoc.nthSibling);
    }
    else
    {
        logger.log("The number of arguments is incorrect.")
        scriptFailed = true;
    }
}

if (scriptFailed == false)
{
    childAssoc.child.addAspect("cm:titled");
    childAssoc.child.save();
}

