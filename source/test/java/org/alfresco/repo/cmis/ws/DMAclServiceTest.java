/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
package org.alfresco.repo.cmis.ws;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.alfresco.service.cmr.security.PermissionService;

/**
 * @author Dmitry Velichkevich
 */
public class DMAclServiceTest extends AbstractServiceTest
{
    private static final String CONSUMER_PERMISSION = "{http://www.alfresco.org/model/content/1.0}cmobject.Consumer";

    public final static String SERVICE_WSDL_LOCATION = CmisServiceTestHelper.ALFRESCO_URL + "/cmis/ACLService?wsdl";
    public final static QName SERVICE_NAME = new QName("http://docs.oasis-open.org/ns/cmis/ws/200908/", "ACLService");

    @Override
    protected Object getServicePort()
    {
        URL serviceWsdlURL;
        try
        {
            serviceWsdlURL = new URL(SERVICE_WSDL_LOCATION);
        }
        catch (MalformedURLException e)
        {
            throw new java.lang.RuntimeException("Cannot get service Wsdl URL", e);
        }

        Service service = Service.create(serviceWsdlURL, SERVICE_NAME);
        ACLServicePort port = service.getPort(ACLServicePort.class);
        helper.authenticateServicePort(port, CmisServiceTestHelper.USERNAME_ADMIN, CmisServiceTestHelper.PASSWORD_ADMIN);
        return port;
    }

    public void testAclApplying() throws Exception
    {
        ACLServicePort port = (ACLServicePort) getServicePort();
        CmisACLType appliedAcl = port.applyACL(repositoryId, documentId, createAceListType(null, CmisServiceTestHelper.USERNAME_ADMIN, true, "cmis:all"), null,
                EnumACLPropagation.PROPAGATE, null);
        assertAclResponseBasically(appliedAcl);
        assertFalse(appliedAcl.getACL().getPermission().isEmpty());
        assertPermissionInResponse(appliedAcl, true, CmisServiceTestHelper.USERNAME_ADMIN, "cmis:all");
    }

    private void assertAclResponseBasically(CmisACLType acl)
    {
        assertNotNull(acl);
        assertNotNull(acl.getACL());
        assertNotNull(acl.getACL().getPermission());
    }

    public void testAclRemoving() throws Exception
    {
        ACLServicePort port = (ACLServicePort) getServicePort();
        CmisACLType appliedACL = port.applyACL(repositoryId, documentId, null, createAceListType(null, PermissionService.ALL_AUTHORITIES, false, CONSUMER_PERMISSION),
                EnumACLPropagation.PROPAGATE, null);
        assertAclResponseBasically(appliedACL);
        assertTrue(appliedACL.getACL().getPermission().isEmpty());
    }

    public void testChangeFromSomeToWrite() throws Exception
    {
        ACLServicePort port = (ACLServicePort) getServicePort();
        CmisACLType appliedACL = port.applyACL(repositoryId, documentId, createAceListType(null, PermissionService.ALL_AUTHORITIES, false, "cmis:read"), null,
                EnumACLPropagation.PROPAGATE, null);
        assertAclResponseBasically(appliedACL);
        assertFalse(appliedACL.getACL().getPermission().isEmpty());
        appliedACL = port.applyACL(repositoryId, documentId, createAceListType(null, PermissionService.ALL_AUTHORITIES, false, "cmis:write"), null, EnumACLPropagation.PROPAGATE,
                null);
        assertAclResponseBasically(appliedACL);
        assertFalse(appliedACL.getACL().getPermission().isEmpty());
        assertPermissionInResponse(appliedACL, true, CmisServiceTestHelper.USERNAME_ADMIN, "cmis:write");
        appliedACL = port.applyACL(repositoryId, documentId, createAceListType(null, PermissionService.ALL_AUTHORITIES, false, "cmis:all"), null, EnumACLPropagation.PROPAGATE,
                null);
        assertAclResponseBasically(appliedACL);
        assertFalse(appliedACL.getACL().getPermission().isEmpty());
        appliedACL = port.applyACL(repositoryId, documentId, createAceListType(null, PermissionService.ALL_AUTHORITIES, false, "cmis:write"), null, EnumACLPropagation.PROPAGATE,
                null);
        assertAclResponseBasically(appliedACL);
        assertFalse(appliedACL.getACL().getPermission().isEmpty());
        assertPermissionInResponse(appliedACL, true, CmisServiceTestHelper.USERNAME_ADMIN, "cmis:write");
    }

    public void testChangeFromReadToWriteWithRemoving() throws Exception
    {
        ACLServicePort port = (ACLServicePort) getServicePort();
        CmisACLType appliedACL = port.applyACL(repositoryId, documentId, createAceListType(null, CmisServiceTestHelper.USERNAME_ADMIN, false, "cmis:read"), null,
                EnumACLPropagation.PROPAGATE, null);
        assertAclResponseBasically(appliedACL);
        assertFalse(appliedACL.getACL().getPermission().isEmpty());
        appliedACL = port.applyACL(repositoryId, documentId, null, createAceListType(null, CmisServiceTestHelper.USERNAME_ADMIN, false, "cmis:read"), EnumACLPropagation.PROPAGATE,
                null);
        assertAclResponseBasically(appliedACL);
        assertFalse(appliedACL.getACL().getPermission().isEmpty());
        appliedACL = port.applyACL(repositoryId, documentId, createAceListType(null, CmisServiceTestHelper.USERNAME_ADMIN, false, "cmis:write"), null,
                EnumACLPropagation.PROPAGATE, null);
        assertAclResponseBasically(appliedACL);
        assertFalse(appliedACL.getACL().getPermission().isEmpty());
        assertPermissionInResponse(appliedACL, true, CmisServiceTestHelper.USERNAME_ADMIN, "cmis:write");
    }

    // TODO: Maybe this test will be invalid!!!
    public void testChangeFromAllToWriteWithRemoving() throws Exception
    {
        ACLServicePort port = (ACLServicePort) getServicePort();
        CmisACLType appliedACL = port.applyACL(repositoryId, documentId, createAceListType(null, CmisServiceTestHelper.USERNAME_ADMIN, false, "cmis:all"), null,
                EnumACLPropagation.PROPAGATE, null);
        assertAclResponseBasically(appliedACL);
        assertFalse(appliedACL.getACL().getPermission().isEmpty());
        appliedACL = port.applyACL(repositoryId, documentId, createAceListType(null, CmisServiceTestHelper.USERNAME_ADMIN, false, "cmis:read"), null, EnumACLPropagation.PROPAGATE,
                null);
        assertAclResponseBasically(appliedACL);
        assertFalse(appliedACL.getACL().getPermission().isEmpty());
        appliedACL = port.applyACL(repositoryId, documentId, null, createAceListType(null, CmisServiceTestHelper.USERNAME_ADMIN, false, "cmis:all"), EnumACLPropagation.PROPAGATE,
                null);
        assertAclResponseBasically(appliedACL);
        assertFalse(appliedACL.getACL().getPermission().isEmpty());
        appliedACL = port.applyACL(repositoryId, documentId, createAceListType(null, CmisServiceTestHelper.USERNAME_ADMIN, false, "cmis:write"), null,
                EnumACLPropagation.PROPAGATE, null);
        assertAclResponseBasically(appliedACL);
        assertFalse(appliedACL.getACL().getPermission().isEmpty());
        assertPermissionInResponse(appliedACL, true, CmisServiceTestHelper.USERNAME_ADMIN, "cmis:write");
    }

    public void testUpdatingAclOnFolder() throws Exception
    {
        String name = "TestFolder (" + System.currentTimeMillis() + ")";
        String document = helper.createDocument(name, folderId);
        ACLServicePort port = (ACLServicePort) getServicePort();
        CmisACLType appliedACL = port.applyACL(repositoryId, folderId, createAceListType(null, CmisServiceTestHelper.USERNAME_ADMIN, false, "cmis:write"), null,
                EnumACLPropagation.PROPAGATE, null);
        assertAclResponseBasically(appliedACL);
        assertFalse(appliedACL.getACL().getPermission().isEmpty());
        assertPermissionInResponse(appliedACL, true, CmisServiceTestHelper.USERNAME_ADMIN, "cmis:write");
        appliedACL = port.getACL(repositoryId, document, false, null);
        assertAclResponseBasically(appliedACL);
        assertFalse(appliedACL.getACL().getPermission().isEmpty());
        assertPermissionInResponse(appliedACL, true, CmisServiceTestHelper.USERNAME_ADMIN, "cmis:write");
    }

    public void testGettingAcl() throws Exception
    {
        ACLServicePort port = (ACLServicePort) getServicePort();
        CmisACLType acl = port.getACL(repositoryId, documentId, false, null);
        assertAclResponseBasically(acl);
        assertFalse(acl.getACL().getPermission().isEmpty());
        assertPermissionInResponse(acl, true, CmisServiceTestHelper.USERNAME_ADMIN, CONSUMER_PERMISSION);
        acl = port.getACL(repositoryId, folderId, false, null);
        assertAclResponseBasically(acl);
        assertFalse(acl.getACL().getPermission().isEmpty());
        assertPermissionInResponse(acl, true, CmisServiceTestHelper.USERNAME_ADMIN, CONSUMER_PERMISSION);
    }

    private void assertPermissionInResponse(CmisACLType aclRepsonse, boolean mustContain, String principalId, String permission)
    {
        boolean found = false;
        for (CmisAccessControlEntryType entry : aclRepsonse.getACL().getPermission())
        {
            assertNotNull(entry);
            assertNotNull(entry.getPermission());
            assertFalse(entry.getPermission().isEmpty());
            found = entry.getPermission().contains(permission);
            if (found)
            {
                break;
            }
        }
        if (mustContain)
        {
            assertTrue(("'" + permission + "' Permission was not found for '" + principalId + "' Principal Id"), found);
        }
        else
        {
            assertFalse(("'" + permission + "' Permission was found for '" + principalId + "' Principal Id"), found);
        }
    }

    private CmisAccessControlListType createAceListType(CmisAccessControlListType list, String principalId, boolean direct, String permission)
    {
        CmisAccessControlListType result = (null == list) ? (new CmisAccessControlListType()) : (list);
        CmisAccessControlEntryType entry = new CmisAccessControlEntryType();
        CmisAccessControlPrincipalType principal = new CmisAccessControlPrincipalType();
        principal.setPrincipalId(principalId);
        entry.setPrincipal(principal);
        entry.setDirect(direct);
        entry.getPermission().add(permission);
        result.getPermission().add(entry);
        return result;
    }

    @Override
    protected void setUp() throws Exception
    {
        createInitialContent();
    }

    @Override
    protected void tearDown() throws Exception
    {
        deleteInitialContent();
    }
}
