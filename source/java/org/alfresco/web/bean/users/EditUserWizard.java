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
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;
import org.apache.log4j.Logger;

/**
 * @author YanO
 *
 */
public class EditUserWizard extends CreateUserWizard
{
    private static Logger logger = Logger.getLogger(EditUserWizard.class);
   
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
        if (this.nodeService.exists(homeFolderRef) == true)
        {
            ChildAssociationRef childAssocRef = this.nodeService.getPrimaryParent(homeFolderRef);
            NodeRef parentRef = childAssocRef.getParentRef();
            if (this.nodeService.getRootNode(Repository.getStoreRef()).equals(parentRef) == false)
            {
                this.homeSpaceLocation = parentRef;
                this.homeSpaceName = Repository.getNameForNode(nodeService, homeFolderRef);
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

            Map<QName, Serializable> props = this.nodeService.getProperties(nodeRef);
            props.put(ContentModel.PROP_USERNAME, this.userName);
            props.put(ContentModel.PROP_FIRSTNAME, this.firstName);
            props.put(ContentModel.PROP_LASTNAME, this.lastName);

            // calculate whether we need to move the old home space or create new
            NodeRef newHomeFolderRef;
            NodeRef oldHomeFolderRef = (NodeRef) this.nodeService.getProperty(nodeRef, ContentModel.PROP_HOMEFOLDER);
            boolean moveHomeSpace = false;
            boolean renameHomeSpace = false;
            if (oldHomeFolderRef != null && this.nodeService.exists(oldHomeFolderRef) == true)
            {
                // the original home folder ref exists so may need moving if it has been changed
                ChildAssociationRef childAssocRef = this.nodeService.getPrimaryParent(oldHomeFolderRef);
                NodeRef currentHomeSpaceLocation = childAssocRef.getParentRef();
                if (this.homeSpaceName.length() != 0)
                {
                    if (currentHomeSpaceLocation.equals(this.homeSpaceLocation) == false && oldHomeFolderRef.equals(this.homeSpaceLocation) == false
                            && currentHomeSpaceLocation.equals(getCompanyHomeSpace()) == false && currentHomeSpaceLocation.equals(getDefaultHomeSpace()) == false)
                    {
                        moveHomeSpace = true;
                    }

                    String oldHomeSpaceName = Repository.getNameForNode(nodeService, oldHomeFolderRef);
                    if (oldHomeSpaceName.equals(this.homeSpaceName) == false && oldHomeFolderRef.equals(this.homeSpaceLocation) == false)
                    {
                        renameHomeSpace = true;
                    }
                }
            }

            if (logger.isDebugEnabled())
                logger.debug("Moving space: " + moveHomeSpace + "  and renaming space: " + renameHomeSpace);

            if (moveHomeSpace == false && renameHomeSpace == false)
            {
                if (this.homeSpaceLocation != null && this.homeSpaceName.length() != 0)
                {
                    newHomeFolderRef = createHomeSpace(this.homeSpaceLocation.getId(), this.homeSpaceName, false);
                }
                else if (this.homeSpaceLocation != null)
                {
                    // location selected but no home space name entered,
                    // so the home ref should be set to the newly selected space
                    newHomeFolderRef = this.homeSpaceLocation;

                    // set the permissions for this space so the user can access it

                }
                else
                {
                    // nothing selected - use Company Home by default
                    newHomeFolderRef = getCompanyHomeSpace();
                }
            }
            else
            {
                // either move, rename or both required
                if (moveHomeSpace == true)
                {
                    this.nodeService
                            .moveNode(oldHomeFolderRef, this.homeSpaceLocation, ContentModel.ASSOC_CONTAINS, this.nodeService.getPrimaryParent(oldHomeFolderRef).getQName());
                }
                newHomeFolderRef = oldHomeFolderRef; // ref ID doesn't change

                if (renameHomeSpace == true)
                {
                    // change HomeSpace node name
                    this.nodeService.setProperty(newHomeFolderRef, ContentModel.PROP_NAME, this.homeSpaceName);
                }
            }

            props.put(ContentModel.PROP_HOMEFOLDER, newHomeFolderRef);
            props.put(ContentModel.PROP_EMAIL, this.email);
            props.put(ContentModel.PROP_ORGID, this.companyId);
            props.put(ContentModel.PROP_ORGANIZATION, this.organisation);
            props.put(ContentModel.PROP_JOBTITLE, this.jobtitle);
            props.put(ContentModel.PROP_LOCATION, this.location);
            props.put(ContentModel.PROP_PRESENCEPROVIDER, this.presenceProvider);
            props.put(ContentModel.PROP_PRESENCEUSERNAME, this.presenceUsername);
            this.nodeService.setProperties(nodeRef, props);

            // TODO: RESET HomeSpace Ref found in top-level navigation bar!
            // NOTE: not need cos only admin can do this?
            
            putSizeQuotaProperty(this.userName, this.sizeQuota, this.sizeQuotaUnits);
            
        }
        catch (Throwable e)
        {
            Utils.addErrorMessage(MessageFormat.format(Application.getMessage(FacesContext.getCurrentInstance(), ERROR), e.getMessage()), e);
            outcome = null;
        }
        return outcome;
    }
}
