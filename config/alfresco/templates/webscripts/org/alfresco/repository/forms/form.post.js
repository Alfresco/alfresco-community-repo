function main()
{
    // Extract template args
    var itemKind = url.templateArgs['item_kind'];
    var itemId = url.templateArgs['item_id'];

    if (logger.isLoggingEnabled())
    {
        logger.log("itemKind = " + itemKind);
        logger.log("itemId = " + itemId);
    }

    // TODO: Return error if item kind and/or id is missing?

    try
    {
        // persist the submitted data using the most appropriate data set
        if (typeof formdata !== "undefined")
        {
            // The model.data is set here to allow the rendering of a simple result page.
            // TODO This should be removed when the POST is working.
            model.data = formdata;

            // Note: This formdata is org/alfresco/web/scripts/servlet/FormData.java
            if (logger.isLoggingEnabled())
            {
                logger.log("Saving form with formdata, " + formdata.fields.length + " fields.");
            }

            // N.B. This repoFormData is a different FormData class to that used above.
            var repoFormData = new Packages.org.alfresco.repo.forms.FormData();
            for (var i = 0; i < formdata.fields.length; i++)
            {
                // Replace the first 2 underscores with colons.
                var alteredName = formdata.fields[i].name.replaceFirst("_", ":").replaceFirst("_", ":");
                repoFormData.addData(alteredName, formdata.fields[i].value);
            }

            formService.saveForm(itemKind, itemId, repoFormData);
        }
        else
        {
            if (logger.isLoggingEnabled())
            {
                logger.log("Saving form with args = " + args);
            }

            formService.saveForm(itemKind, itemId, args);
        }
    }
    catch (error)
    {
        var msg = error.message;
       
        if (logger.isLoggingEnabled())
            logger.log(msg);
       
        // determine if the exception was a FormNotFoundException, if so return
        // 404 status code otherwise return 500
        if (msg.indexOf("FormNotFoundException") != -1)
        {
            status.setCode(404, msg);
          
            if (logger.isLoggingEnabled())
                logger.log("Returning 404 status code");
        }
        else
        {
            status.setCode(500, msg);
          
            if (logger.isLoggingEnabled())
                logger.log("Returning 500 status code");
        }
       
        return;
    }

    model.message = "Successfully updated item [" + itemKind + "]" + itemId;
}

main();