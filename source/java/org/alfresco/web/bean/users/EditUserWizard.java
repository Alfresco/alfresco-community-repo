/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.ReportedException;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author YanO
 *
 */
public class EditUserWizard extends CreateUserWizard
{
    private static final long serialVersionUID = 7529989488476324511L;

    private static Log logger = LogFactory.getLog(EditUserWizard.class);
   
    protected UsersBeanProperties properties;

    /**
     * @param properties the properties to set
     */
    public void setProperties(UsersBeanProperties properties)
    {
        this.properties = properties;
    }

    @Override
    public void init(Map<String, String> arg0)
    {
        super.init(arg0);

        // set values for edit mode
        Map<String, Object> props = properties.getPerson().getProperties();

        this.firstName = (String) props.get("firstName");
        this.lastName = (String) props.get("lastName");
        this.userName = (String) props.get("userName");
        this.email = (String) props.get("email");
        this.companyId = (String) props.get("organizationId");
        this.organisation = (String) props.get("organization");
        this.jobtitle = (String) props.get("jobtitle");
        this.location = (String) props.get("location");
        this.presenceProvider = (String) props.get("presenceProvider");
        this.presenceUsername = (String) props.get("presenceUsername");
        this.sizeQuota = (Long) props.get("sizeQuota");
        if (this.sizeQuota != null && this.sizeQuota == -1L)
        {
            this.sizeQuota = null;
        }
        
        if (this.sizeQuota != null)
        {
           Pair<Long, String> size = convertFromBytes(this.sizeQuota);
           this.sizeQuota = size.getFirst();
           this.sizeQuotaUnits = size.getSecond();
        }

        // calculate home space name and parent space Id from homeFolderId
        this.homeSpaceLocation = null; // default to Company root space
        NodeRef homeFolderRef = (NodeRef) props.get("homeFolder");
        if (homeFolderRef != null && this.getNodeService().exists(homeFolderRef) == true)
        {
            ChildAssociationRef childAssocRef = this.getNodeService().getPrimaryParent(homeFolderRef);
            NodeRef parentRef = childAssocRef.getParentRef();
            if (this.getNodeService().getRootNode(Repository.getStoreRef()).equals(parentRef) == false)
            {
                this.homeSpaceLocation = parentRef;
                this.homeSpaceName = Repository.getNameForNode(getNodeService(), homeFolderRef);
            }
            else
            {
                this.homeSpaceLocation = homeFolderRef;
            }
        }

        if (logger.isDebugEnabled())
            logger.debug("Edit user home space location: " + homeSpaceLocation + " home space name: " + homeSpaceName);

    }

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception
    {
        try
        {
            // update the existing node in the repository
            NodeRef nodeRef = properties.getPerson().getNodeRef();

            // Note: The previous approach was to try a move the home folder if changed,
            // but this was problematic as other users that shared the same folder
            // moved too. We cannot tell if it is shared. Decided it is better to just
            // create a new folder or reuse the folder pointed to just like the initial
            // creation in CreateUserWizard. It is always possible to move the contents
            // of the old home folder by hand later.
            Map<QName, Serializable> props = this.getNodeService().getProperties(nodeRef);
            setPersonPropertiesAndCreateHomeSpaceIfNeeded(props, context);

            // update the node that represents the Person
            this.getNodeService().setProperties(nodeRef, props);

            // TODO: RESET HomeSpace Ref found in top-level navigation bar!
            // NOTE: not need cos only admin can do this?
            
            if ((this.sizeQuota != null) && (this.sizeQuota < 0L))
            {
                Utils.addErrorMessage(MessageFormat.format(Application.getMessage(context, UsersDialog.ERROR_NEGATIVE_QUOTA), this.sizeQuota));
                outcome = null;
            }
            else
            {
            	putSizeQuotaProperty(this.userName, this.sizeQuota, this.sizeQuotaUnits);
            }
        }
        catch (Throwable e)
        {
            Utils.addErrorMessage(MessageFormat.format(Application.getMessage(FacesContext.getCurrentInstance(), ERROR), e.getMessage()), e);
            outcome = null;
            ReportedException.throwIfNecessary(e);
        }
        
        if (outcome == null)
        {
            this.isFinished = false;
        }
        
        return outcome;
    }
    
    @Override
    public Map getPersonPropertiesImmutability()
    {
        return this.properties.getImmutability();
    }
}
