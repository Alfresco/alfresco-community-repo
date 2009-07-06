function main()
{
    // Extract template args
    var itemKind = url.templateArgs["item_kind"];
    var itemId = url.templateArgs["item_id"];

    if (logger.isLoggingEnabled())
    {
        logger.log("itemKind = " + itemKind);
        logger.log("itemId = " + itemId);
    }

    try
    {
        // persist the submitted data using the most appropriate data set
        if (typeof formdata !== "undefined")
        {
            // Note: This formdata is org/alfresco/web/scripts/servlet/FormData.java
            if (logger.isLoggingEnabled())
            {
                logger.log("Saving form with formdata, " + formdata.fields.length + " fields.");
            }

            // N.B. This repoFormData is a different FormData class to that used above.
            var repoFormData = new Packages.org.alfresco.repo.forms.FormData();
            for (var i = 0; i < formdata.fields.length; i++)
            {
                repoFormData.addFieldData(formdata.fields[i].name, formdata.fields[i].value);
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

    model.message = "Successfully persisted form for item [" + itemKind + "]" + itemId;
}

main();