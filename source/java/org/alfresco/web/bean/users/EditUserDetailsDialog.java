/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.web.bean.users;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;

/**
 * @author YanO
 *
 */
public class EditUserDetailsDialog extends BaseDialogBean
{
    private Node person;
    protected UsersBeanProperties properties;
    private NodeRef photoRef;

    /**
     * @param properties the properties to set
     */
    public void setProperties(UsersBeanProperties properties)
    {
        this.properties = properties;
    }

    @Override
    public void init(Map<String, String> parameters)
    {
        super.init(parameters);
        person = properties.getPerson();
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception
    {
        try
        {
            Map<QName, Serializable> props = this.nodeService.getProperties(getPerson().getNodeRef());
            //props.put(ContentModel.PROP_FIRSTNAME, getFirstName());
            //props.put(ContentModel.PROP_LASTNAME, getLastName());
            //props.put(ContentModel.PROP_EMAIL, getEmail());
            for (String key : getPerson().getProperties().keySet())
            {
                props.put(QName.createQName(key), (Serializable)getPerson().getProperties().get(key));
            }

            // crop person description to 1024 chars as HTML TextArea has no limiter 
            String personDesc = (String)props.get(ContentModel.PROP_PERSONDESC);
            if (personDesc != null && personDesc.length() > 1024)
            {
                personDesc = personDesc.substring(0, 1024);
                props.put(ContentModel.PROP_PERSONDESC, personDesc);
            }

            // persist all property changes
            NodeRef personRef = getPerson().getNodeRef();
            this.nodeService.setProperties(personRef, props);

            // setup user avatar association
            if (this.photoRef != null)
            {
                List<AssociationRef> refs = this.nodeService.getTargetAssocs(personRef, ContentModel.ASSOC_AVATAR);
                // remove old association if it exists
                if (refs.size() == 1)
                {
                    NodeRef existingRef = refs.get(0).getTargetRef();
                    this.nodeService.removeAssociation(
                            personRef, existingRef, ContentModel.ASSOC_AVATAR);
                }
                // setup new association
                this.nodeService.createAssociation(personRef, this.photoRef, ContentModel.ASSOC_AVATAR);
            }
            
            // if the above calls were successful, then reset Person Node in the session
            Application.getCurrentUser(context).reset();
        }
        catch (Throwable err)
        {
            Utils.addErrorMessage(MessageFormat.format(Application.getMessage(context, Repository.ERROR_GENERIC), err.getMessage()), err);
            outcome = null;
        }
        return outcome;
    }

    public Node getPerson()
    {
        return person;
    }

    public String getEmail()
    {
        return person.getProperties().get(ContentModel.PROP_EMAIL).toString();
    }

    public void setEmail(String email)
    {
        person.getProperties().put(ContentModel.PROP_EMAIL.toString(), email);
    }

    public String getFirstName()
    {
        return person.getProperties().get(ContentModel.PROP_FIRSTNAME).toString();
    }

    public void setFirstName(String firstName)
    {
        person.getProperties().put(ContentModel.PROP_FIRSTNAME.toString(), firstName);
    }

    public String getLastName()
    {
        return person.getProperties().get(ContentModel.PROP_LASTNAME).toString();
    }

    public void setLastName(String lastName)
    {
        person.getProperties().put(ContentModel.PROP_LASTNAME.toString(), lastName);
    }

    public Map<String, Object> getPersonProperties()
    {
        return person.getProperties();
    }
    
    public NodeRef getPersonPhotoRef()
    {
        if (this.photoRef == null)
        {
            List<AssociationRef> refs = this.nodeService.getTargetAssocs(person.getNodeRef(), ContentModel.ASSOC_AVATAR);
            if (refs.size() == 1)
            {
                this.photoRef = refs.get(0).getTargetRef();
            }
        }
        return this.photoRef;
    }
    
    public void setPersonPhotoRef(NodeRef ref)
    {
        this.photoRef = ref;
    }
}
