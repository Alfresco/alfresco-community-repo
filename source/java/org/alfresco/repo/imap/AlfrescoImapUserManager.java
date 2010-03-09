/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.imap;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.user.UserException;
import com.icegreen.greenmail.user.UserManager;

/**
 * @author Arseny Kovalchuk
 */
public class AlfrescoImapUserManager extends UserManager
{
    private Log logger = LogFactory.getLog(AlfrescoImapUserManager.class);

    protected Map<String, GreenMailUser> userMap = Collections.synchronizedMap(new HashMap<String, GreenMailUser>());

    protected AuthenticationService authenticationService;
    protected PersonService personService;
    protected NodeService nodeService;

    public AlfrescoImapUserManager()
    {
        super(null);
    }

    public GreenMailUser createUser(String email, String login, String password) throws UserException
    {
        // TODO: User creation/addition code should be implemented here (in the AlfrescoImapUserManager).
        // Following code is not need and not used in the current implementation.
        GreenMailUser user = new AlfrescoImapUser(email, login, password);
        user.create();
        addUser(user);
        return user;
    }

    protected void addUser(GreenMailUser user)
    {
        userMap.put(user.getLogin(), user);
    }

    public GreenMailUser getUser(String login)
    {
        return (GreenMailUser) userMap.get(login);
    }

    public GreenMailUser getUserByEmail(String email)
    {
        GreenMailUser ret = getUser(email);
        if (null == ret)
        {
            for (GreenMailUser user : userMap.values())
            {
                // TODO: NPE!
                if (user.getEmail().trim().equalsIgnoreCase(email.trim()))
                {
                    return user;
                }
            }
        }
        return ret;
    }

    public void deleteUser(GreenMailUser user) throws UserException
    {
        user = (GreenMailUser) userMap.remove(user.getLogin());
        if (user != null)
        {
            user.delete();
        }
    }

    /**
     * The login method.
     * 
     * @see com.icegreen.greenmail.imap.commands.LoginCommand#doProcess()
     */
    public boolean test(String userid, String password)
    {
        try
        {
            authenticationService.authenticate(userid, password.toCharArray());
            String email = null;
            if (personService.personExists(userid))
            {
                NodeRef personNodeRef = personService.getPerson(userid);
                email = (String) nodeService.getProperty(personNodeRef, ContentModel.PROP_EMAIL);
            }
            GreenMailUser user = new AlfrescoImapUser(email, userid, password);
            addUser(user);
        }
        catch (AuthenticationException ex)
        {
            logger.error("IMAP authentication failed for userid: " + userid);
            return false;
        }
        return true;
    }

    public void setAuthenticationService(AuthenticationService authenticationService)
    {
        this.authenticationService = authenticationService;
    }

    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

}
