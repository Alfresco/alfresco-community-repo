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
package org.alfresco.web.bean.users;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.ReportedException;
import org.alfresco.web.ui.common.Utils;

/**
 * @author YanO
 *
 */
public class EditUserDetailsDialog extends BaseDialogBean
{
    private static final long serialVersionUID = 8663254425262484L;
    
    private Node person;
    protected UsersBeanProperties properties;
    private NodeRef photoRef;
    private String personDescription;

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
        this.person = this.properties.getPerson();
        this.photoRef = null;
        this.personDescription = null;
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception
    {
        try
        {
            ServiceRegistry services = Repository.getServiceRegistry(FacesContext.getCurrentInstance());
            DictionaryService dd = services.getDictionaryService();
            Map<QName, Serializable> props = getNodeService().getProperties(getPerson().getNodeRef());
            for (String key : getPerson().getProperties().keySet())
            {
                QName propQName = QName.createQName(key);
                if (dd.getProperty(propQName) == null || dd.getProperty(propQName).isProtected() == false)
                {
                    props.put(propQName, (Serializable)getPerson().getProperties().get(key));
                }
            }

            // persist all property changes
            NodeRef personRef = getPerson().getNodeRef();
            this.getNodeService().setProperties(personRef, props);
            
            // save person description content field
            if (this.personDescription != null)
            {
                ContentService cs = services.getContentService();
                ContentWriter writer = cs.getWriter(personRef, ContentModel.PROP_PERSONDESC, true);
                writer.setMimetype(MimetypeMap.MIMETYPE_HTML);
                writer.putContent(this.personDescription);
            }

            // setup user avatar association
            if (this.photoRef != null)
            {
                List<AssociationRef> refs = this.getNodeService().getTargetAssocs(personRef, ContentModel.ASSOC_AVATAR);
                // remove old association if it exists
                if (refs.size() == 1)
                {
                    NodeRef existingRef = refs.get(0).getTargetRef();
                    this.getNodeService().removeAssociation(
                            personRef, existingRef, ContentModel.ASSOC_AVATAR);
                }
                // setup new association
                this.getNodeService().createAssociation(personRef, this.photoRef, ContentModel.ASSOC_AVATAR);
            }
            
            // if the above calls were successful, then reset Person Node in the session
            Application.getCurrentUser(context).reset();
        }
        catch (Throwable err)
        {
            Utils.addErrorMessage(MessageFormat.format(Application.getMessage(context, Repository.ERROR_GENERIC), err.getMessage()), err);
            outcome = null;
            ReportedException.throwIfNecessary(err);
        }
        return outcome;
    }

    public Node getPerson()
    {
        return person;
    }

    public String getEmail()
    {
        Object value = person.getProperties().get(ContentModel.PROP_EMAIL);
        return value == null ? null : value.toString();
    }

    public void setEmail(String email)
    {
        person.getProperties().put(ContentModel.PROP_EMAIL.toString(), email);
    }

    public String getFirstName()
    {
        Object value = person.getProperties().get(ContentModel.PROP_FIRSTNAME);
        return value == null ? null : value.toString();
    }

    public void setFirstName(String firstName)
    {
        person.getProperties().put(ContentModel.PROP_FIRSTNAME.toString(), firstName);
    }

    public String getLastName()
    {
        Object value = person.getProperties().get(ContentModel.PROP_LASTNAME);
        return value == null ? null : value.toString();
    }
    
    public String getPersonDescription()
    {
        if (personDescription == null)
        {
            ContentService cs = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getContentService();
            ContentReader reader = cs.getReader(this.person.getNodeRef(), ContentModel.PROP_PERSONDESC);
            if (reader != null && reader.exists())
            {
                personDescription = reader.getContentString();
            }
        }
        return personDescription;
    }
    
    public void setPersonDescription(String s)
    {
        this.personDescription = s;
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
            List<AssociationRef> refs = this.getNodeService().getTargetAssocs(person.getNodeRef(), ContentModel.ASSOC_AVATAR);
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
