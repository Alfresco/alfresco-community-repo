/**
 * Create / Post / Invitation
 */
function main()
{
    var invitation = null;
   
   // Get the web site site 
    var shortName = url.extension.split("/")[0];
    var site = siteService.getSite(shortName);
    if (site == null)
    {
        // Site cannot be found
        status.setCode(status.STATUS_NOT_FOUND, "The site " + shortName + " does not exist.");
        return;
    }
   
    if (!json.has("invitationType"))
    {
        status.setCode(status.STATUS_BAD_REQUEST, "The invitationType has not been set.");
        return;
    }
   
   // Get the role 
    var invitationType = json.get("invitationType");
    if (invitationType == null || invitationType.length == 0)
    {
        status.setCode(status.STATUS_BAD_REQUEST, "The invitationType is null or empty.");
        return;
    }
   
    if (!invitationType.match("[MODERATED]|[NOMINATED]"))
    {
        status.setCode(status.STATUS_BAD_REQUEST, "The invitationType has does not have a correct value.");
        return;
    }

    try
    {
        if (invitationType == "MODERATED")
        {
            // Check mandatory parameters and values
            if (isNotDefinedOrEmpty(json, "inviteeRoleName"))
            {
                status.setCode(status.STATUS_BAD_REQUEST, "The inviteeRoleName has not been set.");
                return;
            }
            if (isNotDefinedOrEmpty(json, "inviteeUserName"))
            {
                status.setCode(status.STATUS_BAD_REQUEST, "The inviteeUserName has not been set.");
                return;
            }
            var inviteeComments = json.get("inviteeComments");
            if (inviteeComments == null)
            {
                status.setCode(status.STATUS_BAD_REQUEST, "The inviteeComments has not been set.");
                return;
            }

            var inviteeRoleName = json.get("inviteeRoleName");
            var inviteeUserName = json.get("inviteeUserName");
            var inviteeComments = json.get("inviteeComments");

            invitation = site.inviteModerated(inviteeComments, inviteeUserName, inviteeRoleName);
        }

        if (invitationType == "NOMINATED")
        {
            // Check mandatory parameters and values
            if (isNotDefinedOrEmpty(json, "inviteeRoleName"))
            {
                status.setCode(status.STATUS_BAD_REQUEST, "The inviteeRoleName has not been set.");
                return;
            }
            var inviteeRoleName = json.get("inviteeRoleName");
            var acceptUrl = json.get("acceptURL");
            var rejectUrl = json.get("rejectURL");

            // Get the optional properties
            if (json.has("inviteeUserName") && json.get("inviteeUserName") && json.get("inviteeUserName").trim() != "")
            {
                invitation = site.inviteNominated(json.get("inviteeUserName"), inviteeRoleName, acceptUrl, rejectUrl);
            } else
            {
                // Get mandatory properties
                if (isNotDefinedOrEmpty(json, "inviteeFirstName"))
                {
                    status.setCode(status.STATUS_BAD_REQUEST, "The inviteeFirstName has not been set.");
                    return;
                }
                if (isNotDefinedOrEmpty(json, "inviteeLastName"))
                {
                    status.setCode(status.STATUS_BAD_REQUEST, "The inviteeLastName has not been set.");
                    return;
                }
                if (isNotDefinedOrEmpty(json, "inviteeEmail"))
                {
                    status.setCode(status.STATUS_BAD_REQUEST, "The inviteeEmail has not been set.");
                    return;
                }

                var inviteeFirstName = json.get("inviteeFirstName");
                var inviteeLastName = json.get("inviteeLastName");
                var inviteeEmail = json.get("inviteeEmail");
                invitation = site.inviteNominated(inviteeFirstName, inviteeLastName, inviteeEmail, inviteeRoleName, acceptUrl,
                        rejectUrl);
            }
        }
   
        // Pass the model to the results template
        model.site = site;
        model.invitation = invitation;

        status.code = status.STATUS_CREATED;
    } catch (e)
    {
        if (e.message && e.message.indexOf("org.alfresco.service.cmr.invitation.InvitationExceptionUserError") == 0)
        {
            e.code = status.STATUS_CONFLICT;
        } else if (e.message && e.message.indexOf("org.alfresco.service.cmr.invitation.InvitationExceptionForbidden") == 0)
        {
            e.code = status.STATUS_FORBIDDEN;
        } else
        {
            e.code = 500;
            e.message = e.message + "Unexpected error occurred during starting invitation";
        }
        throw e;
    }
}

function isNotDefinedOrEmpty(json, key)
{
    return (!json.has(key) || (json.get(key) == null || json.get(key).trim().length() == 0))
}

main();