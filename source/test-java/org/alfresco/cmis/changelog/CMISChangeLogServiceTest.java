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
package org.alfresco.cmis.changelog;

import java.io.Serializable;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.transaction.Status;
import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.cmis.CMISCapabilityChanges;
import org.alfresco.cmis.CMISChangeEvent;
import org.alfresco.cmis.CMISChangeLog;
import org.alfresco.cmis.CMISChangeLogService;
import org.alfresco.cmis.CMISChangeType;
import org.alfresco.cmis.CMISInvalidArgumentException;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.audit.model.AuditModelRegistryImpl;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;
import org.alfresco.util.Pair;


/**
 * Base tests for {@link CMISChangeLogServiceImpl}
 * 
 * @author Dmitry Velichkevich
 */
public class CMISChangeLogServiceTest extends TestCase
{
    private static final String CMIS_AUTHORITY = "cmis";
    private static final String CHANGE_PREFIX = "Changed";
    private static final String INVALID_CHANGE_TOKEN = "<Invalid Change Token>";
    private static final String[] NAME_PARTS = new String[] { "TestDocument (", ").txt", "TestFolder (", ")" };

    private static int TOTAL_AMOUNT = 31;
    private static int CREATED_AMOUNT = 18;
    private static final int THE_HALFT_OF_CREATED_AMOUNT = CREATED_AMOUNT / 2;
    private static final Map<CMISChangeType, Integer> EXPECTED_AMOUNTS = new HashMap<CMISChangeType, Integer>();
    static
    {
        EXPECTED_AMOUNTS.put(CMISChangeType.CREATED, 5);
        EXPECTED_AMOUNTS.put(CMISChangeType.DELETED, 3);
        EXPECTED_AMOUNTS.put(CMISChangeType.SECURITY, 4);
        EXPECTED_AMOUNTS.put(CMISChangeType.UPDATED, 6);
    }

    private AuditModelRegistryImpl auditSubsystem;
    private CMISChangeLogService changeLogService;
    private NodeService nodeService;
    private FileFolderService fileFolderService;
    private PermissionService permissionService;
    private TransactionService transactionService;
    private AuthenticationComponent authenticationComponent;
    private RetryingTransactionHelper retryingTransactionHelper;
    private UserTransaction testTX;

    private int actualCount = 0;
    private Map<CMISChangeType, Integer> actualAmounts = new HashMap<CMISChangeType, Integer>();
    private List<NodeRef> created = null;
    private List<NodeRef> deleted = null;

    private void disableAudit()
    {
        auditSubsystem.stop();
        auditSubsystem.setProperty("audit.enabled", "true");
        auditSubsystem.setProperty("audit.cmischangelog.enabled", "false");
    }

    private void enableAudit()
    {
        auditSubsystem.stop();
        auditSubsystem.setProperty("audit.enabled", "true");
        auditSubsystem.setProperty("audit.cmischangelog.enabled", "true");
    }

    /**
     * Tests {@link CMISChangeLogServiceImpl} with disabled Auditing feature
     * 
     * @throws Exception
     */
    public void testServiceWithDisabledAuditing() throws Exception
    {
        disableAudit();
        String lastChangeLogToken = changeLogService.getLastChangeLogToken();
        createTestData(EXPECTED_AMOUNTS, false);
        assertEquals(CMISCapabilityChanges.NONE, changeLogService.getCapability());
        try
        {
            changeLogService.getChangeLogEvents(lastChangeLogToken, null);
            fail("Changes Logging was not enabled but no one Change Log Service method thrown exception");
        }
        catch (Exception e)
        {
            assertTrue("Invalid exception type from Change Log Service method call with desabled Changes Logging", e instanceof AlfrescoRuntimeException);
        }
    }

    /**
     * Tests {@link CMISChangeLogServiceImpl} with enabled Auditing feature
     * 
     * @throws Exception
     */
    public void testEnabledAuditing() throws Exception
    {
        enableAudit();
        String logToken = changeLogService.getLastChangeLogToken();
        createTestData(EXPECTED_AMOUNTS, false);
        assertEquals(CMISCapabilityChanges.OBJECTIDSONLY, changeLogService.getCapability());
        CMISChangeLog changeLog = changeLogService.getChangeLogEvents(logToken, null);
        assertChangeLog(logToken, changeLog);
        assertChangeEvents(logToken, changeLog, null, FoldersAppearing.NOT_EXPECTED);
    }

    /**
     * Validates Change Log descriptor that was returned for some Change Log Token
     * 
     * @param logToken {@link String} value that represents last Change Log Token
     * @param changeLog {@link CMISChangeLog} instance that represents Change Log descriptor
     */
    private void assertChangeLog(String logToken, CMISChangeLog changeLog)
    {
        assertNotNull(("'" + logToken + "' Change Log Token has no descriptor"), changeLog);
        assertNotNull(("Event Etries for '" + logToken + "' Change Log Token are undefined"), changeLog.getChangeEvents());
        assertFalse(("Descriptor for '" + logToken + "' Change Log Token has no any Event Entry"), changeLog.getChangeEvents().isEmpty());
    }

    /**
     * Creates test data which will represent Change Events of all possible types
     * 
     * @param requiredAmounts {@link Map}&lt;{@link CMISChangeType}, {@link Integer}&gt; container instance that determines amount of Change Event for each Change Type
     * @return pair containing list of created node refs, and list of deleted node refs
     * @see CMISChangeType
     */
    private Pair<List<NodeRef>, List<NodeRef>> createTestData(Map<CMISChangeType, Integer> requiredAmounts, boolean withFolders)
    {
        changeLogService.getLastChangeLogToken();
        created = new LinkedList<NodeRef>();
        deleted = new LinkedList<NodeRef>();
        Pair<List<NodeRef>, List<NodeRef>> result = new Pair<List<NodeRef>, List<NodeRef>>(created, deleted);
        NodeRef parentNodeRef = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        SecureRandom randomizer = new SecureRandom();
        for (CMISChangeType key : requiredAmounts.keySet())
        {
            Integer amount = requiredAmounts.get(key);
            for (int i = 0; i < amount; i++)
            {
                boolean folder = withFolders && (0 == ((Math.abs(randomizer.nextInt()) % amount) % 2));
                QName objectType = (folder) ? (ContentModel.TYPE_FOLDER) : (ContentModel.TYPE_CONTENT);
                FileInfo object = fileFolderService.create(parentNodeRef, generateName(randomizer, folder), objectType);
                created.add(object.getNodeRef());
                addOneToAmount(actualAmounts, CMISChangeType.CREATED);
                switch (key)
                {
                case DELETED:
                {
                    nodeService.deleteNode(object.getNodeRef());
                    deleted.add(object.getNodeRef());
                    addOneToAmount(actualAmounts, CMISChangeType.DELETED);
                    break;
                }
                case SECURITY:
                {
                    permissionService.setPermission(object.getNodeRef(), CMIS_AUTHORITY, PermissionService.EXECUTE_CONTENT, true);
                    addOneToAmount(actualAmounts, CMISChangeType.SECURITY);
                    break;
                }
                case UPDATED:
                {
                    StringBuilder nameBuilder = new StringBuilder(CHANGE_PREFIX);
                    nameBuilder.append(nodeService.getProperty(object.getNodeRef(), ContentModel.PROP_NAME));
                    nodeService.setProperty(object.getNodeRef(), ContentModel.PROP_NAME, nameBuilder.toString());
                    addOneToAmount(actualAmounts, CMISChangeType.UPDATED);
                }
                }
                actualCount++;
            }
        }
        return result;
    }

    /**
     * Deletes each element of created test data if element exist and current user has appropriate rights
     * 
     * @param testData {@link Map}&lt;{@link NodeRef}, {@link Map}&lt;{@link QName}, {@link Serializable}&gt;&gt; container instance that contains test data
     */
    private void deleteTestData()
    {
        if (created != null)
        {
            for (NodeRef object : created)
            {
                if (nodeService.exists(object) && (AccessStatus.ALLOWED == permissionService.hasPermission(object, PermissionService.DELETE)))
                {
                    nodeService.deleteNode(object);
                }
            }
        }
    }

    /**
     * @param folder {@link Boolean} value that determines which name should be generated (for Folder or Document Object)
     * @return {@link String} value that represents generated uniquely name for Document Object
     */
    private String generateName(SecureRandom randomizer, boolean folder)
    {
        StringBuilder nameBuilder = new StringBuilder();
        int i = (folder) ? (2) : (0);
        nameBuilder.append(NAME_PARTS[i++]).append(Math.abs(randomizer.nextInt())).append(NAME_PARTS[i++]);
        return nameBuilder.toString();
    }

    /**
     * This method validates Change Event entries according to created earlier Objects. According to <b>assertProperties</b> parameter this method may and may not check properties
     * of Change Event entry according to appropriate expected Object against Change Type
     * 
     * @param expectedObjects {@link Map}&lt;{@link NodeRef}, {@link Map}&lt;{@link QName}, {@link Serializable}&gt;&gt; container instance that contains Ids and properties of
     *        expected Objects
     * @param logToken {@link String} value that represents last Change Log Token
     * @param changeLog {@link CMISChangeLog} instance that represents Change Log descriptor for last Change Log Token
     * @param maxItems {@link Integer} value that determines high bound of Change Events amount
     * @see CMISChangeType
     */
    private void assertChangeEvents(String logToken, CMISChangeLog changeLog, Integer maxItems, FoldersAppearing foldersAppearing)
    {
        Map<CMISChangeType, Integer> logAmounts = new HashMap<CMISChangeType, Integer>();
        boolean folderWasFound = false;
        int idx = 0;
        for (CMISChangeEvent event : changeLog.getChangeEvents())
        {
            // skip first change log entry if a log token has been specified, as the CMIS spec expects
            // the change entry to be returned for the specified log token
            idx++;
            if (logToken != null && idx == 1)
            {
                continue;
            }
            
            assertNotNull(("One of the Change Log Event Enries is undefined for '" + logToken + "' Change Log Token"), event);
            assertNotNull(("Change Event Entry Id of one of the Change Entries is undefined for '" + logToken + "' Change Log Token"), event.getChangedNode());
            assertNotNull(("Change Event Change Type of one of the Change Entries is undefined for '" + logToken + "' Change Log Token"), event.getChangeType());
            assertTrue("Unexpected Object Id='" + event.getChangedNode().toString() + "' from Change Log Token Entries list for '" + logToken + "' Change Log Token", created
                    .contains(event.getChangedNode()));
            if (!deleted.contains(event.getChangedNode()))
            {
                folderWasFound = folderWasFound || fileFolderService.getFileInfo(event.getChangedNode()).isFolder();
                assertTrue(
                        ("Object from Change Event Entries list is marked as '" + event.getChangeType().toString() + "' but does not exist for '" + logToken + "' Change Log Token"),
                        nodeService.exists(event.getChangedNode()));
            }
            else
            {
                assertTrue("Object has been deleted", deleted.contains(event.getChangedNode()));
                assertFalse(("Object from Change Event Entries list is marked as 'DELETED' but it still exist for '" + logToken + "' Change Log Token"), nodeService.exists(event
                        .getChangedNode()));
            }
            addOneToAmount(logAmounts, event.getChangeType());
        }
        if (FoldersAppearing.MUST_APPEAR == foldersAppearing)
        {
            assertTrue("No one Folder Object was returned", folderWasFound);
        }
        else
        {
            if (FoldersAppearing.NOT_EXPECTED == foldersAppearing)
            {
                assertFalse("Some Folder Object was found", folderWasFound);
            }
        }
        if ((null == maxItems) || (maxItems >= TOTAL_AMOUNT))
        {
            for (CMISChangeType key : actualAmounts.keySet())
            {
                Integer actualAmount = actualAmounts.get(key);
                Integer logAmount = logAmounts.get(key);
                assertTrue(("Invalid Entries amount for '" + key.toString() + "' Change Type. Actual amount: " + actualAmount + ", but log amount: " + logAmount), actualAmount
                        .equals(logAmount));
            }
        }
    }

    private enum FoldersAppearing
    {
        NOT_EXPECTED, MAY_APPEAR, MUST_APPEAR
    }

    /**
     * Determines which kind of Change was handled and increments appropriate amount to 1
     * 
     * @param mappedAmounts {@link Map}&gt;{@link CMISChangeType}, {@link Integer}&lt; container instance that contains all accumulated amounts for each kind of Change
     * @param changeType {@link CMISChangeType} enum value that determines kind of Change
     */
    private void addOneToAmount(Map<CMISChangeType, Integer> mappedAmounts, CMISChangeType changeType)
    {
        Integer amount = mappedAmounts.get(changeType);
        amount = (null == amount) ? (Integer.valueOf(1)) : (Integer.valueOf(amount.intValue() + 1));
        mappedAmounts.put(changeType, amount);
    }

    /**
     * Test {@link CMISChangeLogServiceImpl} with enabled Auditing feature for Max Items parameter
     * 
     * @throws Exception
     */
    public void testEnabledAuditingForMaxItems() throws Exception
    {
        enableAudit();
        String logToken = changeLogService.getLastChangeLogToken();
        createTestData(EXPECTED_AMOUNTS, false);
        assertEquals(CMISCapabilityChanges.OBJECTIDSONLY, changeLogService.getCapability());
        CMISChangeLog changeLog = changeLogService.getChangeLogEvents(logToken, THE_HALFT_OF_CREATED_AMOUNT);
        assertChangeLog(logToken, changeLog);
        assertChangeEvents(logToken, changeLog, THE_HALFT_OF_CREATED_AMOUNT, FoldersAppearing.NOT_EXPECTED);
        assertEquals(THE_HALFT_OF_CREATED_AMOUNT, changeLog.getChangeEvents().size());
        assertTrue("Not all Change Log Entries were requested but result set is indicating that no one more Entry is avilable", changeLog.hasMoreItems());
        changeLog = changeLogService.getChangeLogEvents(logToken, TOTAL_AMOUNT + (logToken == null ? 0 : 1));
        assertChangeEvents(logToken, changeLog, TOTAL_AMOUNT, FoldersAppearing.NOT_EXPECTED);
        assertFalse("All Change Log Entries were requested but result set is indicating that some more Entry(s) are available", changeLog.hasMoreItems());
    }

    /**
     * This method tests {@link CMISChangeLogServiceImpl} on receiving Change Event Entries for Invalid Change Log Token with enable and disabled Changes Logging
     * 
     * @throws Exception
     */
    public void testReceivingChangeEventsForInvalidChangeToken() throws Exception
    {
        enableAudit();
        try
        {
            changeLogService.getChangeLogEvents(INVALID_CHANGE_TOKEN, null);
            fail("Change Events were received normally for Invalid Change Log Token");
        }
        catch (Exception e)
        {
            assertTrue("Invalid exception type from Change Log Service method call with enabled Changes Logging", e instanceof CMISInvalidArgumentException);
        }
        disableAudit();
        try
        {
            changeLogService.getChangeLogEvents(INVALID_CHANGE_TOKEN, null);
            fail("Changes Logging was not enabled but not one Change Log Service method thrown exception");
        }
        catch (Exception e)
        {
            assertTrue("Invalid exception type from Change Log Service method call with desabled Changes Logging", e instanceof AlfrescoRuntimeException);
        }
    }

    /**
     * This method tests {@link CMISChangeLogServiceImpl} on working with Change Event entries which could contain Folder Objects
     * 
     * @throws Exception
     */
    public void testReceivingOfChangeEventsExpectingFolders() throws Exception
    {
        enableAudit();
        String changeToken = changeLogService.getLastChangeLogToken();
        createTestData(EXPECTED_AMOUNTS, true);
        CMISChangeLog changeLogEvents = changeLogService.getChangeLogEvents(changeToken, null);
        assertChangeLog(changeToken, changeLogEvents);
        assertChangeEvents(changeToken, changeLogEvents, null, FoldersAppearing.MUST_APPEAR);
    }

    /**
     * This method tests {@link CMISChangeLogServiceImpl} on working with Change Event entries which could contain Folder Objects. Also this method tests behavior of Max Items
     * parameter for Folder Objects
     * 
     * @throws Exception
     */
    public void testReceivingOfChangeEventsExpectingFoldersAndBoundedByMaxItems() throws Exception
    {
        enableAudit();
        String changeToken = changeLogService.getLastChangeLogToken();
        createTestData(EXPECTED_AMOUNTS, true);
        CMISChangeLog changeLogEvents = changeLogService.getChangeLogEvents(changeToken, 15);
        assertTrue("Not all Change Event Entries were requested but result set indicates that no more Entry(s) available", changeLogEvents.hasMoreItems());
        assertChangeLog(changeToken, changeLogEvents);
        assertChangeEvents(changeToken, changeLogEvents, 15, FoldersAppearing.MAY_APPEAR);
        changeLogEvents = changeLogService.getChangeLogEvents(changeToken, TOTAL_AMOUNT + (changeToken == null ? 0 : 1));
        assertChangeLog(changeToken, changeLogEvents);
        assertChangeEvents(changeToken, changeLogEvents, TOTAL_AMOUNT, FoldersAppearing.MUST_APPEAR);
        assertFalse("All Change Event Entries were requested but results indicating that some more Entry(s) available", changeLogEvents.hasMoreItems());
    }

    @Override
    public void setUp() throws Exception
    {
        ApplicationContext applicationContext = ApplicationContextHelper.getApplicationContext();
        changeLogService = (CMISChangeLogService) applicationContext.getBean("CMISChangeLogService");
        nodeService = (NodeService) applicationContext.getBean("NodeService");
        permissionService = (PermissionService) applicationContext.getBean("PermissionService");
        fileFolderService = (FileFolderService) applicationContext.getBean("FileFolderService");
        transactionService = (TransactionService) applicationContext.getBean("transactionComponent");
        authenticationComponent = (AuthenticationComponent) applicationContext.getBean("authenticationComponent");
        retryingTransactionHelper = (RetryingTransactionHelper) applicationContext.getBean("retryingTransactionHelper");
        auditSubsystem = (AuditModelRegistryImpl) applicationContext.getBean("Audit");
        
        // initialise audit subsystem
        RetryingTransactionCallback<Void> initAudit = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Exception
            {
                auditSubsystem.stop();
                auditSubsystem.setProperty("audit.enabled", "true");
                auditSubsystem.setProperty("audit.cmischangelog.enabled", "true");
                auditSubsystem.start();
                return null;
            }
        };
        retryingTransactionHelper.doInTransaction(initAudit, false, true); 

        // start test transaction
        testTX = transactionService.getUserTransaction();
        testTX.begin();
        this.authenticationComponent.setSystemUserAsCurrentUser();
    }

    @Override
    protected void tearDown() throws Exception
    {
        deleteTestData();
        
        if (testTX.getStatus() == Status.STATUS_ACTIVE)
        {
            testTX.rollback();
        }
        AuthenticationUtil.clearCurrentSecurityContext();
        
        auditSubsystem.destroy();
    }
}
