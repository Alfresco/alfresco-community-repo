logger.log("The counatble aspect has been added");

var scriptFailed = false;

// Have a look at the behaviour object that should have been passed
if (behaviour == null)
{
    logger.log("The behaviour object has not been set.");
    scriptFailed = true;
}

// Check the name of the behaviour
if (behaviour.name == null && behaviour.name != "onAddAspect")
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
    if (behaviour.args.length == 2)    
    {
        var nodeRef = behaviour.args[0];
        var aspectType = behaviour.args[1];
        logger.log("NodeRef: " + nodeRef.id);
        logger.log("Type: " + aspectType);
        if (aspectType != "{http://www.alfresco.org/model/content/1.0}countable")
        {
            logger.log("Aspect type is incorrect");
            scriptFailed = true;
        }
    }
    else
    {
        logger.log("The number of arguments is incorrect.")
        scriptFailed = true;
    }
}

if (scriptFailed == false)
{
    nodeRef.addAspect("cm:titled");
    nodeRef.save();
}

