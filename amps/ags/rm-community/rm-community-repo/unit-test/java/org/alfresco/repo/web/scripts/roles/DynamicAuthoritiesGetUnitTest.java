/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.repo.web.scripts.roles;

import static java.util.Collections.emptyMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.security.ExtendedReaderDynamicAuthority;
import org.alfresco.module.org_alfresco_module_rm.security.ExtendedSecurityService;
import org.alfresco.module.org_alfresco_module_rm.security.ExtendedWriterDynamicAuthority;
import org.alfresco.module.org_alfresco_module_rm.test.util.AlfMock;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseWebScriptUnitTest;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.patch.PatchDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.web.scripts.content.ContentStreamer;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.Pair;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * DynamicAuthoritiesGet Unit Test
 *
 * @author Silviu Dinuta
 */
@SuppressWarnings("deprecation")
public class DynamicAuthoritiesGetUnitTest extends BaseWebScriptUnitTest implements RecordsManagementModel
{
    /** test data */
    private static final Long ASPECT_ID = 123l;
    private static final QName ASPECT = AlfMock.generateQName();

    /** mocks */
    @Mock
    private PatchDAO mockedPatchDAO;
    @Mock
    private NodeDAO mockedNodeDAO;
    @Mock
    private QNameDAO mockedQnameDAO;
    @Mock
    private NodeService mockedNodeService;
    @Mock
    private PermissionService mockedPermissionService;
    @Mock
    private ExtendedSecurityService mockedExtendedSecurityService;
    @Mock
    private TransactionService mockedTransactionService;
    @Mock
    private RetryingTransactionHelper mockedRetryingTransactionHelper;
    @Mock
    private ContentStreamer contentStreamer;
    @Mock
    private FileFolderService mockedFileFolderService;

    /** test component */
    @InjectMocks
    private DynamicAuthoritiesGet webScript;

    @Override
    protected AbstractWebScript getWebScript()
    {
        return webScript;
    }

    @Override
    protected String getWebScriptTemplate()
    {
        return "alfresco/templates/webscripts/org/alfresco/repository/roles/rm-dynamicauthorities.get.json.ftl";
    }

    /**
     * Before test
     */
    @SuppressWarnings("unchecked")
    @Before
    public void before()
    {
        MockitoAnnotations.initMocks(this);
        webScript.setNodeService(mockedNodeService);
        webScript.setPermissionService(mockedPermissionService);
        webScript.setExtendedSecurityService(mockedExtendedSecurityService);
        webScript.setFileFolderService(mockedFileFolderService);
        // setup retrying transaction helper
        Answer<Object> doInTransactionAnswer = new Answer<Object>()
        {
            @SuppressWarnings("rawtypes")
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable
            {
                RetryingTransactionCallback callback = (RetryingTransactionCallback) invocation.getArguments()[0];
                return callback.execute();
            }
        };

        doAnswer(doInTransactionAnswer).when(mockedRetryingTransactionHelper)
                    .<Object> doInTransaction(any(RetryingTransactionCallback.class), anyBoolean(), anyBoolean());

        when(mockedTransactionService.getRetryingTransactionHelper()).thenReturn(mockedRetryingTransactionHelper);

        // max node id
        when(mockedPatchDAO.getMaxAdmNodeID()).thenReturn(500000L);

        // aspect
        when(mockedQnameDAO.getQName(ASPECT_EXTENDED_SECURITY)).thenReturn(new Pair<>(ASPECT_ID, ASPECT));
    }

    /**
     * Given that there are no nodes with the extended security aspect
     * When the action is executed Nothing happens
     *
     * @throws Exception
     */
    @SuppressWarnings({ "unchecked" })
    @Test
    public void noNodesWithExtendedSecurity() throws Exception
    {
        when(mockedPatchDAO.getNodesByAspectQNameId(eq(ASPECT_ID), anyLong(), anyLong()))
                    .thenReturn(Collections.emptyList());

        // Set up parameters.
        Map<String, String> parameters = ImmutableMap.of("batchsize", "10", "maxProcessedRecords", "3");
        JSONObject json = executeJSONWebScript(parameters);
        assertNotNull(json);
        String actualJSONString = json.toString();

        // Check the JSON result using Jackson to allow easy equality testing.
        ObjectMapper mapper = new ObjectMapper();
        String expectedJSONString = "{\"responsestatus\":\"success\",\"message\":\"Processed 0 records.\"}";
        assertEquals(mapper.readTree(expectedJSONString), mapper.readTree(actualJSONString));

        verify(mockedNodeService, never()).getProperty(any(NodeRef.class), eq(PROP_READERS));
        verify(mockedNodeService, never()).getProperty(any(NodeRef.class), eq(PROP_WRITERS));
        verify(mockedNodeService, never()).removeAspect(any(NodeRef.class), eq(ASPECT_EXTENDED_SECURITY));
        verify(mockedPermissionService, never()).clearPermission(any(NodeRef.class),
                    eq(ExtendedReaderDynamicAuthority.EXTENDED_READER));
        verify(mockedPermissionService, never()).clearPermission(any(NodeRef.class),
                    eq(ExtendedWriterDynamicAuthority.EXTENDED_WRITER));
        verify(mockedExtendedSecurityService, never()).set(any(NodeRef.class), any(Set.class), any(Set.class));
    }

    /**
     * Given that there are records with the extended security aspect
     * When the action is executed
     * Then the aspect is removed
     * And the dynamic authorities permissions are cleared
     * And extended security is set via the updated API
     *
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @Test
    public void recordsWithExtendedSecurityAspect() throws Exception
    {
        List<Long> ids = Stream.of(1l, 2l, 3l).collect(Collectors.toList());

        when(mockedPatchDAO.getNodesByAspectQNameId(eq(ASPECT_ID), anyLong(), anyLong())).thenReturn(ids)
                    .thenReturn(Collections.emptyList());

        ids.stream().forEach((i) -> {
            NodeRef nodeRef = AlfMock.generateNodeRef(mockedNodeService);
            when(mockedNodeDAO.getNodePair(i)).thenReturn(new Pair<>(i, nodeRef));
            when(mockedNodeService.hasAspect(nodeRef, ASPECT_RECORD)).thenReturn(true);
            when(mockedNodeService.getProperty(nodeRef, PROP_READERS))
                        .thenReturn((Serializable) Collections.emptyMap());
            when(mockedNodeService.getProperty(nodeRef, PROP_WRITERS))
                        .thenReturn((Serializable) Collections.emptyMap());
        });

        // Set up parameters.
        Map<String, String> parameters = ImmutableMap.of("batchsize", "10", "maxProcessedRecords", "4");
        JSONObject json = executeJSONWebScript(parameters);
        assertNotNull(json);
        String actualJSONString = json.toString();
        ObjectMapper mapper = new ObjectMapper();
        String expectedJSONString = "{\"responsestatus\":\"success\",\"message\":\"Processed 3 records.\"}";
        assertEquals(mapper.readTree(expectedJSONString), mapper.readTree(actualJSONString));

        verify(mockedNodeService, times(3)).getProperty(any(NodeRef.class), eq(PROP_READERS));
        verify(mockedNodeService, times(3)).getProperty(any(NodeRef.class), eq(PROP_WRITERS));
        verify(mockedNodeService, times(3)).removeAspect(any(NodeRef.class), eq(ASPECT_EXTENDED_SECURITY));
        verify(mockedPermissionService, times(3)).clearPermission(any(NodeRef.class),
                    eq(ExtendedReaderDynamicAuthority.EXTENDED_READER));
        verify(mockedPermissionService, times(3)).clearPermission(any(NodeRef.class),
                    eq(ExtendedWriterDynamicAuthority.EXTENDED_WRITER));
        verify(mockedExtendedSecurityService, times(3)).set(any(NodeRef.class), any(Set.class), any(Set.class));

    }

    /**
     * Given that there are non-records with the extended security aspect
     * When the web script is executed
     * Then the aspect is removed And the dynamic authorities permissions are cleared
     *
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @Test
    public void nonRecordsWithExtendedSecurityAspect() throws Exception
    {
        List<Long> ids = Stream.of(1l, 2l, 3l).collect(Collectors.toList());

        when(mockedPatchDAO.getNodesByAspectQNameId(eq(ASPECT_ID), anyLong(), anyLong())).thenReturn(ids)
                           .thenReturn(Collections.emptyList());

        ids.stream().forEach((i) -> {
            NodeRef nodeRef = AlfMock.generateNodeRef(mockedNodeService);
            when(mockedNodeDAO.getNodePair(i)).thenReturn(new Pair<>(i, nodeRef));
            when(mockedNodeService.hasAspect(nodeRef, ASPECT_RECORD)).thenReturn(false);
            when(mockedNodeService.getProperty(nodeRef, PROP_READERS))
                        .thenReturn((Serializable) Collections.emptyMap());
            when(mockedNodeService.getProperty(nodeRef, PROP_WRITERS))
                        .thenReturn((Serializable) Collections.emptyMap());
        });

        // Set up parameters.
        Map<String, String> parameters = ImmutableMap.of("batchsize", "10", "maxProcessedRecords", "4");
        JSONObject json = executeJSONWebScript(parameters);
        assertNotNull(json);
        String actualJSONString = json.toString();
        ObjectMapper mapper = new ObjectMapper();
        String expectedJSONString = "{\"responsestatus\":\"success\",\"message\":\"Processed 3 records.\"}";
        assertEquals(mapper.readTree(expectedJSONString), mapper.readTree(actualJSONString));


        verify(mockedNodeService, times(3)).getProperty(any(NodeRef.class), eq(PROP_READERS));
        verify(mockedNodeService, times(3)).getProperty(any(NodeRef.class), eq(PROP_WRITERS));
        verify(mockedNodeService, times(3)).removeAspect(any(NodeRef.class), eq(ASPECT_EXTENDED_SECURITY));
        verify(mockedPermissionService, times(3)).clearPermission(any(NodeRef.class),
                    eq(ExtendedReaderDynamicAuthority.EXTENDED_READER));
        verify(mockedPermissionService, times(3)).clearPermission(any(NodeRef.class),
                    eq(ExtendedWriterDynamicAuthority.EXTENDED_WRITER));
        verify(mockedExtendedSecurityService, never()).set(any(NodeRef.class), any(Set.class), any(Set.class));
    }

    @Test
    public void missingBatchSizeParameter() throws Exception
    {
        try
        {
            executeJSONWebScript(emptyMap());
            fail("Expected exception as parameter batchsize is mandatory.");
        }
        catch (WebScriptException e)
        {
            assertEquals("If parameter batchsize is not provided then 'Bad request' should be returned.",
                        Status.STATUS_BAD_REQUEST, e.getStatus());
        }
    }

    @Test
    public void invalidBatchSizeParameter() throws Exception
    {
        try
        {
        // Set up parameters.
        Map<String, String> parameters = ImmutableMap.of("batchsize", "dd");
            executeJSONWebScript(parameters);
            fail("Expected exception as parameter batchsize is invalid.");
        }
        catch (WebScriptException e)
        {
            assertEquals("If parameter batchsize is invalid then 'Bad request' should be returned.",
                        Status.STATUS_BAD_REQUEST, e.getStatus());
        }
    }

    @Test
    public void batchSizeShouldBeGraterThanZero() throws Exception
    {
        try
        {
        // Set up parameters.
        Map<String, String> parameters = ImmutableMap.of("batchsize", "0");
            executeJSONWebScript(parameters);
            fail("Expected exception as parameter batchsize is not a number greater than 0.");
        }
        catch (WebScriptException e)
        {
            assertEquals("If parameter batchsize is not a number greater than 0 then 'Bad request' should be returned.",
                        Status.STATUS_BAD_REQUEST, e.getStatus());
        }
    }

    @Test
    public void extendedSecurityAspectNotCreated() throws Exception
    {
        when(mockedQnameDAO.getQName(ASPECT_EXTENDED_SECURITY)).thenReturn(null);
        // Set up parameters.
        Map<String, String> parameters = ImmutableMap.of("batchsize", "3");
        JSONObject json = executeJSONWebScript(parameters);
        assertNotNull(json);
        String actualJSONString = json.toString();
        ObjectMapper mapper = new ObjectMapper();
        String expectedJSONString = "{\"responsestatus\":\"success\",\"message\":\"There where no records to be processed.\"}";
        assertEquals(mapper.readTree(expectedJSONString), mapper.readTree(actualJSONString));
    }

    @Test
    public void processAllRecordsWhenMaxProcessedRecordsIsZero() throws Exception
    {
        List<Long> ids = Stream.of(1l, 2l, 3l,4l).collect(Collectors.toList());

        when(mockedPatchDAO.getNodesByAspectQNameId(eq(ASPECT_ID), anyLong(), anyLong())).thenReturn(ids)
                           .thenReturn(Collections.emptyList());

        ids.stream().forEach((i) -> {
            NodeRef nodeRef = AlfMock.generateNodeRef(mockedNodeService);
            when(mockedNodeDAO.getNodePair(i)).thenReturn(new Pair<>(i, nodeRef));
            when(mockedNodeService.hasAspect(nodeRef, ASPECT_RECORD)).thenReturn(false);
            when(mockedNodeService.getProperty(nodeRef, PROP_READERS))
                        .thenReturn((Serializable) Collections.emptyMap());
            when(mockedNodeService.getProperty(nodeRef, PROP_WRITERS))
                        .thenReturn((Serializable) Collections.emptyMap());
        });

        // Set up parameters.
        Map<String, String> parameters = ImmutableMap.of("batchsize", "10", "maxProcessedRecords", "0");
        JSONObject json = executeJSONWebScript(parameters);
        assertNotNull(json);
        String actualJSONString = json.toString();
        ObjectMapper mapper = new ObjectMapper();
        String expectedJSONString = "{\"responsestatus\":\"success\",\"message\":\"Processed 4 records.\"}";
        assertEquals(mapper.readTree(expectedJSONString), mapper.readTree(actualJSONString));
    }

    @Test
    public void whenMaxProcessedRecordsIsMissingItDefaultsToBatchSize() throws Exception
    {
        List<Long> ids = Stream.of(1l, 2l, 3l, 4l, 5l).collect(Collectors.toList());

        when(mockedPatchDAO.getNodesByAspectQNameId(eq(ASPECT_ID), anyLong(), anyLong())).thenReturn(ids)
                           .thenReturn(Collections.emptyList());

        ids.stream().forEach((i) -> {
            NodeRef nodeRef = AlfMock.generateNodeRef(mockedNodeService);
            when(mockedNodeDAO.getNodePair(i)).thenReturn(new Pair<>(i, nodeRef));
            when(mockedNodeService.hasAspect(nodeRef, ASPECT_RECORD)).thenReturn(false);
            when(mockedNodeService.getProperty(nodeRef, PROP_READERS))
                        .thenReturn((Serializable) Collections.emptyMap());
            when(mockedNodeService.getProperty(nodeRef, PROP_WRITERS))
                        .thenReturn((Serializable) Collections.emptyMap());
        });

        // Set up parameters.
        Map<String, String> parameters = ImmutableMap.of("batchsize", "4");
        JSONObject json = executeJSONWebScript(parameters);
        assertNotNull(json);
        String actualJSONString = json.toString();
        ObjectMapper mapper = new ObjectMapper();
        String expectedJSONString = "{\"responsestatus\":\"success\",\"message\":\"Processed first 4 records.\"}";
        assertEquals(mapper.readTree(expectedJSONString), mapper.readTree(actualJSONString));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void recordsWithExtendedSecurityAspectAndNullWritersAndReaders() throws Exception
    {
        List<Long> ids = Stream.of(1l, 2l, 3l).collect(Collectors.toList());

        when(mockedPatchDAO.getNodesByAspectQNameId(eq(ASPECT_ID), anyLong(), anyLong())).thenReturn(ids)
                    .thenReturn(Collections.emptyList());

        ids.stream().forEach((i) -> {
            NodeRef nodeRef = AlfMock.generateNodeRef(mockedNodeService);
            when(mockedNodeDAO.getNodePair(i)).thenReturn(new Pair<>(i, nodeRef));
            when(mockedNodeService.hasAspect(nodeRef, ASPECT_RECORD)).thenReturn(true);
            when(mockedNodeService.getProperty(nodeRef, PROP_READERS)).thenReturn(null);
            when(mockedNodeService.getProperty(nodeRef, PROP_WRITERS)).thenReturn(null);

        });

        // Set up parameters.
        Map<String, String> parameters = ImmutableMap.of("batchsize", "10", "maxProcessedRecords", "4");
        JSONObject json = executeJSONWebScript(parameters);
        assertNotNull(json);
        String actualJSONString = json.toString();
        ObjectMapper mapper = new ObjectMapper();
        String expectedJSONString = "{\"responsestatus\":\"success\",\"message\":\"Processed 3 records.\"}";
        assertEquals(mapper.readTree(expectedJSONString), mapper.readTree(actualJSONString));
        ArgumentCaptor<Set> readerKeysCaptor = ArgumentCaptor.forClass(Set.class);
        ArgumentCaptor<Set> writersKeysCaptor = ArgumentCaptor.forClass(Set.class);

        verify(mockedNodeService, times(3)).getProperty(any(NodeRef.class), eq(PROP_READERS));
        verify(mockedNodeService, times(3)).getProperty(any(NodeRef.class), eq(PROP_WRITERS));
        verify(mockedNodeService, times(3)).removeAspect(any(NodeRef.class), eq(ASPECT_EXTENDED_SECURITY));
        verify(mockedPermissionService, times(3)).clearPermission(any(NodeRef.class),
                    eq(ExtendedReaderDynamicAuthority.EXTENDED_READER));
        verify(mockedPermissionService, times(3)).clearPermission(any(NodeRef.class),
                    eq(ExtendedWriterDynamicAuthority.EXTENDED_WRITER));
        verify(mockedExtendedSecurityService, times(3)).set(any(NodeRef.class), readerKeysCaptor.capture(),
                    writersKeysCaptor.capture());
        List<Set> allReaderKeySets = readerKeysCaptor.getAllValues();
        List<Set> allWritersKeySets = writersKeysCaptor.getAllValues();
        for (Set keySet : allReaderKeySets)
        {
            assertNull(keySet);
        }
        for (Set keySet : allWritersKeySets)
        {
            assertNull(keySet);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void recordsWithExtendedSecurityAspectAndNullWriters() throws Exception
    {
        List<Long> ids = Stream.of(1l, 2l, 3l).collect(Collectors.toList());

        when(mockedPatchDAO.getNodesByAspectQNameId(eq(ASPECT_ID), anyLong(), anyLong())).thenReturn(ids)
                    .thenReturn(Collections.emptyList());

        ids.stream().forEach((i) -> {
            NodeRef nodeRef = AlfMock.generateNodeRef(mockedNodeService);
            when(mockedNodeDAO.getNodePair(i)).thenReturn(new Pair<>(i, nodeRef));
            when(mockedNodeService.hasAspect(nodeRef, ASPECT_RECORD)).thenReturn(true);
            when(mockedNodeService.getProperty(nodeRef, PROP_READERS))
                        .thenReturn((Serializable) Collections.emptyMap());
            when(mockedNodeService.getProperty(nodeRef, PROP_WRITERS)).thenReturn(null);

        });

        // Set up parameters.
        Map<String, String> parameters = ImmutableMap.of("batchsize", "10", "maxProcessedRecords", "4");
        JSONObject json = executeJSONWebScript(parameters);
        assertNotNull(json);
        String actualJSONString = json.toString();
        ObjectMapper mapper = new ObjectMapper();
        String expectedJSONString = "{\"responsestatus\":\"success\",\"message\":\"Processed 3 records.\"}";
        assertEquals(mapper.readTree(expectedJSONString), mapper.readTree(actualJSONString));
        ArgumentCaptor<Set> readerKeysCaptor = ArgumentCaptor.forClass(Set.class);
        ArgumentCaptor<Set> writersKeysCaptor = ArgumentCaptor.forClass(Set.class);

        verify(mockedNodeService, times(3)).getProperty(any(NodeRef.class), eq(PROP_READERS));
        verify(mockedNodeService, times(3)).getProperty(any(NodeRef.class), eq(PROP_WRITERS));
        verify(mockedNodeService, times(3)).removeAspect(any(NodeRef.class), eq(ASPECT_EXTENDED_SECURITY));
        verify(mockedPermissionService, times(3)).clearPermission(any(NodeRef.class),
                    eq(ExtendedReaderDynamicAuthority.EXTENDED_READER));
        verify(mockedPermissionService, times(3)).clearPermission(any(NodeRef.class),
                    eq(ExtendedWriterDynamicAuthority.EXTENDED_WRITER));
        verify(mockedExtendedSecurityService, times(3)).set(any(NodeRef.class), readerKeysCaptor.capture(),
                    writersKeysCaptor.capture());
        List<Set> allReaderKeySets = readerKeysCaptor.getAllValues();
        List<Set> allWritersKeySets = writersKeysCaptor.getAllValues();
        for (Set keySet : allReaderKeySets)
        {
            assertNotNull(keySet);
        }
        for (Set keySet : allWritersKeySets)
        {
            assertNull(keySet);
        }
    }

    /**
     * Given I have records that require migration
     * And I am interested in knowning which records are migrated
     * When I run the migration tool
     * Then I will be returned a CSV file containing the name and node reference of the record migrated
     *
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @Test
    public void processWithCSVFile() throws Exception
    {
        List<Long> ids = Stream.of(1l, 2l, 3l).collect(Collectors.toList());
        when(mockedPatchDAO.getNodesByAspectQNameId(eq(ASPECT_ID), anyLong(), anyLong())).thenReturn(ids)
                    .thenReturn(Collections.emptyList());

        ids.stream().forEach((i) -> {
            NodeRef nodeRef = AlfMock.generateNodeRef(mockedNodeService);
            when(mockedNodeDAO.getNodePair(i)).thenReturn(new Pair<>(i, nodeRef));
            when(mockedNodeService.hasAspect(nodeRef, ASPECT_RECORD)).thenReturn(true);
            when(mockedNodeService.getProperty(nodeRef, PROP_READERS))
                        .thenReturn((Serializable) Collections.emptyMap());
            when(mockedNodeService.getProperty(nodeRef, PROP_WRITERS))
                        .thenReturn((Serializable) Collections.emptyMap());
            String name = "name" + i;
            when(mockedNodeService.getProperty(nodeRef, ContentModel.PROP_NAME)).thenReturn((Serializable) name);
        });

        ArgumentCaptor<File> csvFileCaptor = ArgumentCaptor.forClass(File.class);
        // Set up parameters.
        Map<String, String> parameters = ImmutableMap.of("batchsize", "10", "maxProcessedRecords", "4", "export",
                    "true");
        executeWebScript(parameters);

        verify(contentStreamer, times(1)).streamContent(any(WebScriptRequest.class), any(WebScriptResponse.class),
                    csvFileCaptor.capture(), nullable(Long.class), any(Boolean.class), any(String.class), any(Map.class));

        File fileForDownload = csvFileCaptor.getValue();
        assertNotNull(fileForDownload);
    }

    /**
     * Given that I have record that require migration
     * And I'm not interested in knowing which records were migrated
     * When I run the migration tool
     * Then I will not be returned a CSV file of details.
     *
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @Test
    public void processedWithouthCSVFile() throws Exception
    {
        List<Long> ids = Stream.of(1l, 2l, 3l).collect(Collectors.toList());
        when(mockedPatchDAO.getNodesByAspectQNameId(eq(ASPECT_ID), anyLong(), anyLong())).thenReturn(ids)
                    .thenReturn(Collections.emptyList());

        ids.stream().forEach((i) -> {
            NodeRef nodeRef = AlfMock.generateNodeRef(mockedNodeService);
            when(mockedNodeDAO.getNodePair(i)).thenReturn(new Pair<>(i, nodeRef));
            when(mockedNodeService.hasAspect(nodeRef, ASPECT_RECORD)).thenReturn(true);
            when(mockedNodeService.getProperty(nodeRef, PROP_READERS))
                        .thenReturn((Serializable) Collections.emptyMap());
            when(mockedNodeService.getProperty(nodeRef, PROP_WRITERS))
                        .thenReturn((Serializable) Collections.emptyMap());
        });

        Map<String, String> parameters = ImmutableMap.of("batchsize", "10", "maxProcessedRecords", "4", "export",
                    "false");
        JSONObject json = executeJSONWebScript(parameters);
        assertNotNull(json);
        String actualJSONString = json.toString();
        ObjectMapper mapper = new ObjectMapper();
        String expectedJSONString = "{\"responsestatus\":\"success\",\"message\":\"Processed 3 records.\"}";
        assertEquals(mapper.readTree(expectedJSONString), mapper.readTree(actualJSONString));

        verify(contentStreamer, never()).streamContent(any(WebScriptRequest.class), any(WebScriptResponse.class),
                    any(File.class), any(Long.class), any(Boolean.class), any(String.class), any(Map.class));
    }

    @Test
    public void invalidParentNodeRefParameter() throws Exception
    {
        try
        {
            // Set up parameters.
            Map<String, String> parameters = ImmutableMap.of("batchsize", "10", "parentNodeRef", "invalidNodeRef");
            executeJSONWebScript(parameters);
            fail("Expected exception as parameter parentNodeRef is invalid.");
        }
        catch (WebScriptException e)
        {
            assertEquals("If parameter parentNodeRef is invalid then 'Internal server error' should be returned.",
                        Status.STATUS_INTERNAL_SERVER_ERROR, e.getStatus());
        }
    }

    @Test
    public void inexistentParentNodeRefParameter() throws Exception
    {
        try
        {
            NodeRef parentNodeRef = AlfMock.generateNodeRef(mockedNodeService);
            when(mockedNodeService.exists(parentNodeRef)).thenReturn(false);
            // Set up parameters.
            Map<String, String> parameters = ImmutableMap.of("batchsize", "10", "parentNodeRef",
                        parentNodeRef.toString());
            executeJSONWebScript(parameters);
            fail("Expected exception as parameter parentNodeRef does not exist.");
        }
        catch (WebScriptException e)
        {
            assertEquals("If parameter parentNodeRef is does not exist then 'Bad Reequest' should be returned.",
                        Status.STATUS_BAD_REQUEST, e.getStatus());
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void processedWithParentNodeRef() throws Exception
    {
        List<Long> ids = Stream.of(1l, 2l, 3l).collect(Collectors.toList());
        NodeRef parentNodeRef = AlfMock.generateNodeRef(mockedNodeService);
        List<FileInfo> children = new ArrayList<>();
        ids.stream().forEach((i) -> {
            NodeRef nodeRef = AlfMock.generateNodeRef(mockedNodeService);
            when(mockedNodeService.hasAspect(nodeRef, ASPECT_RECORD)).thenReturn(true);
            when(mockedNodeService.hasAspect(nodeRef, ASPECT)).thenReturn(true);
            when(mockedNodeService.getProperty(nodeRef, PROP_READERS))
                        .thenReturn((Serializable) Collections.emptyMap());
            when(mockedNodeService.getProperty(nodeRef, PROP_WRITERS))
                        .thenReturn((Serializable) Collections.emptyMap());
            String name = "name" + i;
            when(mockedNodeService.getProperty(nodeRef, ContentModel.PROP_NAME)).thenReturn((Serializable) name);
            FileInfo mockedFileInfo = mock(FileInfo.class);
            when(mockedFileInfo.getNodeRef()).thenReturn(nodeRef);
            children.add(mockedFileInfo);
        });
        when(mockedFileFolderService.search(eq(parentNodeRef), eq("*"), eq(true), eq(true), eq(true)))
                    .thenReturn(children);

        Map<String, String> parameters = ImmutableMap.of("batchsize", "3", "maxProcessedRecords", "4", "export",
                    "false", "parentNodeRef", parentNodeRef.toString());
        JSONObject json = executeJSONWebScript(parameters);
        assertNotNull(json);
        String actualJSONString = json.toString();
        ObjectMapper mapper = new ObjectMapper();
        String expectedJSONString = "{\"responsestatus\":\"success\",\"message\":\"Processed 3 records.\"}";
        assertEquals(mapper.readTree(expectedJSONString), mapper.readTree(actualJSONString));

        verify(contentStreamer, never()).streamContent(any(WebScriptRequest.class), any(WebScriptResponse.class),
                    any(File.class), any(Long.class), any(Boolean.class), any(String.class), any(Map.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void processedWithParentNodeRefWithFirstTwoBatchesAlreadyProcessed() throws Exception
    {
        List<Long> ids = Stream.of(1l, 2l, 3l, 4l, 5l, 6l, 7l, 8l).collect(Collectors.toList());
        NodeRef parentNodeRef = AlfMock.generateNodeRef(mockedNodeService);
        List<FileInfo> children = new ArrayList<>();
        ids.stream().forEach((i) -> {
            NodeRef nodeRef = AlfMock.generateNodeRef(mockedNodeService);
            when(mockedNodeService.hasAspect(nodeRef, ASPECT_RECORD)).thenReturn(true);
            if (i <= 6l)
            {
                when(mockedNodeService.hasAspect(nodeRef, ASPECT)).thenReturn(false);
            }
            else
            {
                when(mockedNodeService.hasAspect(nodeRef, ASPECT)).thenReturn(true);
            }
            when(mockedNodeService.getProperty(nodeRef, PROP_READERS))
                        .thenReturn((Serializable) Collections.emptyMap());
            when(mockedNodeService.getProperty(nodeRef, PROP_WRITERS))
                        .thenReturn((Serializable) Collections.emptyMap());
            String name = "name" + i;
            when(mockedNodeService.getProperty(nodeRef, ContentModel.PROP_NAME)).thenReturn((Serializable) name);
            FileInfo mockedFileInfo = mock(FileInfo.class);
            when(mockedFileInfo.getNodeRef()).thenReturn(nodeRef);
            children.add(mockedFileInfo);
        });
        when(mockedFileFolderService.search(eq(parentNodeRef), eq("*"), eq(true), eq(true), eq(true)))
                    .thenReturn(children);

        Map<String, String> parameters = ImmutableMap.of("batchsize", "3", "parentNodeRef", parentNodeRef.toString());
        JSONObject json = executeJSONWebScript(parameters);
        assertNotNull(json);
        String actualJSONString = json.toString();
        ObjectMapper mapper = new ObjectMapper();
        String expectedJSONString = "{\"responsestatus\":\"success\",\"message\":\"Processed 2 records.\"}";
        assertEquals(mapper.readTree(expectedJSONString), mapper.readTree(actualJSONString));

        verify(contentStreamer, never()).streamContent(any(WebScriptRequest.class), any(WebScriptResponse.class),
                    any(File.class), any(Long.class), any(Boolean.class), any(String.class), any(Map.class));
    }
}
