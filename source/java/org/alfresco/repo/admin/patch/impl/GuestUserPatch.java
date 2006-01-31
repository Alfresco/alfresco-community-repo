/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.admin.patch.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.importer.ImporterBootstrap;
import org.alfresco.service.cmr.admin.PatchException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;

/**
 * Ensures that the <b>guest</b> user homespace exists.<br/> A guest user homespace is now created during bootstrap. It is required for guest user access, but in older databases
 * will not exist.
 * 
 * @author Andy Hind
 */
public class GuestUserPatch extends AbstractPatch
{
    private static final String MSG_SUCCESS = "patch.guestUser.result";

    private static final String COMPANY_HOME_CHILD_NAME = "spaces.company_home.childname";

    private static final String GUEST_HOME_CHILD_NAME = "spaces.guest_home.childname";

    private static final String GUEST_HOME_NAME = "spaces.guest_home.name";

    private static final String GUEST_HOME_DESCRIPTION = "spaces.guest_home.description";

    private PersonService personService;

    private NodeService nodeService;

    private SearchService searchService;

    private PermissionService permissionService;

    private ImporterBootstrap importerBootstrap;

    private NamespaceService namespaceService;

    private String guestId = "guest";

    private MessageSource messageSource;

    public GuestUserPatch()
    {
        super();
    }

    public void setGuestId(String guestId)
    {
        this.guestId = guestId;
    }

    public void setImporterBootstrap(ImporterBootstrap importerBootstrap)
    {
        this.importerBootstrap = importerBootstrap;
    }

    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }

    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    public void setMessageSource(MessageSource messageSource)
    {
        this.messageSource = messageSource;
    }

    @Override
    protected String applyInternal() throws Exception
    {
        // Config

        StoreRef storeRef = importerBootstrap.getStoreRef();
        if (storeRef == null)
        {
            throw new PatchException("Bootstrap store has not been set");
        }

        NodeRef storeRootNodeRef = nodeService.getRootNode(storeRef);
        Properties configuration = importerBootstrap.getConfiguration();

        String companyHomeChildName = configuration.getProperty(COMPANY_HOME_CHILD_NAME);
        if (companyHomeChildName == null || companyHomeChildName.length() == 0)
        {
            throw new PatchException("Bootstrap property '" + COMPANY_HOME_CHILD_NAME + "' is not present");
        }

        String guestHomeChildName = configuration.getProperty(GUEST_HOME_CHILD_NAME);
        if (guestHomeChildName == null || guestHomeChildName.length() == 0)
        {
            throw new PatchException("Bootstrap property '" + GUEST_HOME_CHILD_NAME + "' is not present");
        }

        // Set company home space permissions

        NodeRef companyHomeRef = setCompanyHomeSpacePermissions(storeRootNodeRef, companyHomeChildName);

        // Add the guest home space

        NodeRef guestHomeRef = addGuestHomeSpace(storeRootNodeRef, configuration, companyHomeChildName,
                guestHomeChildName, companyHomeRef);

        // Add the guest user and fix read access to the created person

        addGuestUser(guestHomeRef);

        return I18NUtil.getMessage(MSG_SUCCESS);
    }

    private void addGuestUser(NodeRef guestHomeRef)
    {
        if (!personService.personExists(guestId))
        {
            HashMap<QName, Serializable> properties = new HashMap<QName, Serializable>();
            properties.put(ContentModel.PROP_USERNAME, guestId);
            properties.put(ContentModel.PROP_HOMEFOLDER, guestHomeRef);
            properties.put(ContentModel.PROP_FIRSTNAME, "Guest");
            properties.put(ContentModel.PROP_LASTNAME, "");
            properties.put(ContentModel.PROP_EMAIL, "");
            properties.put(ContentModel.PROP_ORGID, "");

            personService.createPerson(properties);

        }

        NodeRef personRef = personService.getPerson(guestId);

        permissionService.setInheritParentPermissions(personRef, false);
        permissionService.setPermission(personRef, guestId, PermissionService.READ, true);

    }

    private NodeRef addGuestHomeSpace(NodeRef storeRootNodeRef, Properties configuration, String companyHomeChildName,
            String guestHomeChildName, NodeRef companyHomeRef)
    {
        List<NodeRef> nodeRefs = searchService.selectNodes(companyHomeRef, guestHomeChildName, null, namespaceService,
                false);
        if (nodeRefs.size() == 0)
        {
            // create

            String guestHomeName = messageSource.getMessage(GUEST_HOME_NAME, null, I18NUtil.getLocale());
            if (guestHomeName == null || guestHomeName.length() == 0)
            {
                throw new PatchException("Bootstrap property '" + GUEST_HOME_NAME + "' is not present");
            }

            String guestHomeDescription = messageSource.getMessage(GUEST_HOME_DESCRIPTION, null, I18NUtil.getLocale());
            if (guestHomeDescription == null || guestHomeDescription.length() == 0)
            {
                throw new PatchException("Bootstrap property '" + GUEST_HOME_DESCRIPTION + "' is not present");
            }

            HashMap<QName, Serializable> properties = new HashMap<QName, Serializable>();
            properties.put(ContentModel.PROP_NAME, guestHomeName);
            properties.put(ContentModel.PROP_TITLE, guestHomeName);
            properties.put(ContentModel.PROP_DESCRIPTION, guestHomeDescription);
            properties.put(ContentModel.PROP_ICON, "space-icon-default");

            ChildAssociationRef childAssocRef = nodeService.createNode(companyHomeRef, ContentModel.ASSOC_CONTAINS,
                    QName.createQName(guestHomeChildName, namespaceService), ContentModel.TYPE_FOLDER, properties);

            NodeRef nodeRef = childAssocRef.getChildRef();
            // add the required aspects
            nodeService.addAspect(nodeRef, ContentModel.ASPECT_UIFACETS, null);

            setGuestHomePermissions(nodeRef);

            return nodeRef;

        }
        else if (nodeRefs.size() == 1)
        {
            NodeRef nodeRef = nodeRefs.get(0);
            setGuestHomePermissions(nodeRef);
            return nodeRef;
        }
        else
        {
            throw new PatchException("XPath returned too many results: \n"
                    + "   root: " + storeRootNodeRef + "\n" + "   xpath: " + companyHomeChildName + "\n"
                    + "   results: " + nodeRefs);
        }
    }

    private void setGuestHomePermissions(NodeRef nodeRef)
    {
        permissionService.setInheritParentPermissions(nodeRef, false);
        permissionService.setPermission(nodeRef, PermissionService.ALL_AUTHORITIES, PermissionService.GUEST, true);
        permissionService.setPermission(nodeRef, guestId, PermissionService.GUEST, true);
    }

    private NodeRef setCompanyHomeSpacePermissions(NodeRef storeRootNodeRef, String companyHomeChildName)
    {
        List<NodeRef> nodeRefs = searchService.selectNodes(storeRootNodeRef, companyHomeChildName, null,
                namespaceService, false);
        if (nodeRefs.size() == 0)
        {
            throw new PatchException("XPath didn't return any results: \n"
                    + "   root: " + storeRootNodeRef + "\n" + "   xpath: " + companyHomeChildName);
        }
        else if (nodeRefs.size() > 1)
        {
            throw new PatchException("XPath returned too many results: \n"
                    + "   root: " + storeRootNodeRef + "\n" + "   xpath: " + companyHomeChildName + "\n"
                    + "   results: " + nodeRefs);
        }
        NodeRef companyHomeRef = nodeRefs.get(0);

        permissionService.setInheritParentPermissions(companyHomeRef, false);
        permissionService.setPermission(companyHomeRef, PermissionService.ALL_AUTHORITIES, PermissionService.GUEST,
                true);
        return companyHomeRef;
    }

}
