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

package org.alfresco.repo.web.scripts.nodelocator;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.nodelocator.AncestorNodeLocator;
import org.alfresco.repo.nodelocator.CompanyHomeNodeLocator;
import org.alfresco.repo.nodelocator.SitesHomeNodeLocator;
import org.alfresco.repo.nodelocator.UserHomeNodeLocator;
import org.alfresco.repo.nodelocator.XPathNodeLocator;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.site.DocLibNodeLocator;
import org.alfresco.repo.site.SiteServiceInternal;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.extensions.surf.util.URLEncoder;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

/**
 * @author Nick Smith
 * @since 4.0
 *
 */
public class NodeLocatorWebScriptTest extends BaseWebScriptTest
{
    private static final String baseURL = "api/nodelocator/";
    private SiteServiceInternal siteService;
    private NodeService nodeService;
    private Repository repositoryHelper;
    private NodeRef companyHome;
    private NamespaceService namespaceService;

    public void testCompanyHomeNodeLocator() throws Exception
    {
        String url = baseURL + CompanyHomeNodeLocator.NAME;
        checkNodeLocator(url, companyHome);
    }
    
    public void testSitesHomeNodeLocator() throws Exception
    {
        String url = baseURL + SitesHomeNodeLocator.NAME;
        NodeRef sitesHome = siteService.getSiteRoot();
        checkNodeLocator(url, sitesHome);
    }

    public void testDocLibNodeLocator() throws Exception
    {
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        SiteInfo site = null;
        
        NodeRef companyChild = makeChildContentNode(companyHome);
        try
        {
            // Set up site
            String siteName = "TestSite" + GUID.generate();
            site = siteService.createSite("", siteName, "Title", "Description", SiteVisibility.PUBLIC);
            NodeRef fooFolder = makeContainer(siteName, "Foo");
            NodeRef docLib = makeContainer(siteName, SiteService.DOCUMENT_LIBRARY);
            NodeRef fooChild = makeChildContentNode(fooFolder);
            NodeRef docLibChild = makeChildContentNode(docLib);
            
            // Check returns company home if no source node specified.
            String noNodeUrl = baseURL + DocLibNodeLocator.NAME;
            checkNodeLocator(noNodeUrl, companyHome);
            
            // Check returns company home if source is not in a site.
            String noSiteUrl = makeUrl(companyChild, DocLibNodeLocator.NAME);
            checkNodeLocator(noSiteUrl, companyHome);
            
            // Check returns site doc lib if source is in site doc lib.
            String docLibUrl = makeUrl(docLibChild, DocLibNodeLocator.NAME);
            checkNodeLocator(docLibUrl, docLib);
            
            // Check returns site doc lib if source is in other site container.
            String fooUrl = makeUrl(fooChild, DocLibNodeLocator.NAME);
            checkNodeLocator(fooUrl, docLib);
        }
        finally
        {
            nodeService.deleteNode(companyChild);
            if(site != null)
            {
                siteService.deleteSite(site.getShortName());
            }
        }
    }

    public void testUserHomeNodeLocator() throws Exception
    {
        String url = baseURL + UserHomeNodeLocator.NAME;
        // Run as System User, no User Home.
        AuthenticationUtil.setRunAsUser(AuthenticationUtil.getSystemUserName());
        checkNodeLocator(url, null);
        
        //Run as Admin User
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        
        NodeRef admin = repositoryHelper.getPerson();
        NodeRef userHome = repositoryHelper.getUserHome(admin);
        
        checkNodeLocator(url, userHome);
    }
    
    public void testAncestorOfTypeNodeLocator() throws Exception
    {
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        NodeRef folder= makeChildFolderNode(companyHome);
        try
        {
            NodeRef sysFolder = makeChildSystemFolderNode(folder);
            NodeRef subFolder = makeChildFolderNode(sysFolder);
            NodeRef source = makeChildContentNode(subFolder);
            
            // No params so should find first parent.
            String ancestorUrl = makeUrl(source, AncestorNodeLocator.NAME);
            checkNodeLocator(ancestorUrl, subFolder);
            
            // Set type to cm:content. Should not match any node.
            String encodedContentType = URLEncoder.encode(ContentModel.TYPE_CONTENT.toPrefixString(namespaceService));
            String contentAncestorUrl = ancestorUrl + "?type=" + encodedContentType;
            checkNodeLocator(contentAncestorUrl, null);
            
            // Set type to cm:systemfolder. Should find the sysFolder node.
            String encodedSysFolderType = URLEncoder.encode(ContentModel.TYPE_SYSTEM_FOLDER.toPrefixString(namespaceService));
            String sysFolderAncestorUrl = ancestorUrl + "?type=" + encodedSysFolderType;
            checkNodeLocator(sysFolderAncestorUrl, sysFolder);
            
            // Set aspect to cm:ownable. Should not match any node.
            String encodedOwnableAspect= URLEncoder.encode(ContentModel.ASPECT_OWNABLE.toPrefixString(namespaceService));
            String ownableAncestorUrl = ancestorUrl + "?aspect=" + encodedOwnableAspect;
            checkNodeLocator(ownableAncestorUrl, null);
            
            // Add ownable aspect to folder node. Now that node should be found.
            nodeService.addAspect(folder, ContentModel.ASPECT_OWNABLE, null);
            checkNodeLocator(ownableAncestorUrl, folder);

            // Set aspect to cm:ownable and type to cm:systemfolder. Should not match any node.
            String ownableSysFolderAncestorUrl = sysFolderAncestorUrl + "&aspect=" + encodedOwnableAspect;
            checkNodeLocator(ownableSysFolderAncestorUrl, null);
            
            // Set aspect to cm:ownable and type to cm:folder. Should find folder node.
            String encodedFOlderType = URLEncoder.encode(ContentModel.TYPE_FOLDER.toPrefixString(namespaceService));
            String ownableFolderAncestorUrl = ownableAncestorUrl + "&type=" + encodedFOlderType;
            checkNodeLocator(ownableFolderAncestorUrl, folder);
        }
        finally
        {
            nodeService.deleteNode(folder);
        }
    }
    
    public void testXPathNodeLocator() throws Exception
    {
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        NodeRef first = makeChildFolderNode(companyHome);
        try
        {
            NodeRef second = makeChildFolderNode(first);
            NodeRef content = makeChildContentNode(second);
            
            // Check bad path returns null
            String badPath = URLEncoder.encode("cm:foo/cm:bar/cm:foobar");
            String badPathUrl = baseURL + XPathNodeLocator.NAME + "?query=" + badPath;
            checkNodeLocator(badPathUrl, null);
            
            String path  = nodeService.getPath(content).toPrefixString(namespaceService);
            String encodedPath = URLEncoder.encode(path);
            
            // Check default store ref works.
            String defaultStoreUrl = baseURL + XPathNodeLocator.NAME + "?query=" +encodedPath;
            checkNodeLocator(defaultStoreUrl, content);
            
            // Check specified store ref works.
            String storeIdUrl = defaultStoreUrl + "&store_type=workspace&store_id=SpacesStore";
            checkNodeLocator(storeIdUrl, content);
            
            // Check node store ref works.
            String nodePathUrl = makeUrl(companyHome, XPathNodeLocator.NAME) + "?query=" + encodedPath;
            checkNodeLocator(nodePathUrl, content);
        }
        finally
        {
            nodeService.deleteNode(first);
        }
    }
    
    private String makeUrl(NodeRef node, String locatorName)
    {
        StoreRef storeRef = node.getStoreRef();
        StringBuilder url = new StringBuilder("/api/");
        url.append(storeRef.getProtocol()).append("/")
        .append(storeRef.getIdentifier()).append("/")
        .append(node.getId()).append("/")
        .append("nodelocator").append("/")
        .append(locatorName);
        return url.toString();
    }

    private void checkNodeLocator(String url, NodeRef expNode) throws Exception
    {
        Response response = sendRequest(new GetRequest(url), 200);
        String jsonStr = response.getContentAsString();
        JSONObject json = new JSONObject(jsonStr);
        JSONObject result = json.getJSONObject("data");
        assertNotNull(result);
        String nodeRef = result.getString("nodeRef");
        String expResult = expNode == null ? "null" : expNode.toString();
        assertEquals(expResult, nodeRef);
    }

    private NodeRef makeChildContentNode(NodeRef parent)
    {
        return makeChildNode(parent, ContentModel.TYPE_CONTENT);
    }
    
    private NodeRef makeChildSystemFolderNode(NodeRef parent)
    {
        return makeChildNode(parent, ContentModel.TYPE_SYSTEM_FOLDER);
    }
    
    private NodeRef makeChildFolderNode(NodeRef parent)
    {
        return makeChildNode(parent, ContentModel.TYPE_FOLDER);
    }

    private NodeRef makeChildNode(NodeRef parent, QName type)
    {
        QName qName = QName.createQName("", GUID.generate());
        QName contains = ContentModel.ASSOC_CONTAINS;
        ChildAssociationRef result = nodeService.createNode(parent, contains, qName, type);
        return result.getChildRef();
    }

    private NodeRef makeContainer(String siteName, String containerId)
    {
        QName type = ContentModel.TYPE_FOLDER;
        return siteService.createContainer(siteName, containerId, type, null);
    }
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        ApplicationContext appContext = getServer().getApplicationContext();
        this.siteService = (SiteServiceInternal) appContext.getBean("SiteService");
        this.nodeService = (NodeService) appContext.getBean("NodeService");
        this.namespaceService= (NamespaceService) appContext.getBean("NamespaceService");
        this.repositoryHelper = (Repository) appContext.getBean("repositoryHelper");
        this.companyHome = repositoryHelper.getCompanyHome();
    }
}
