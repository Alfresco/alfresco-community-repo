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
package org.alfresco.repo.index.shard;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.List;

import org.alfresco.repo.cache.DefaultSimpleCache;
import org.alfresco.repo.index.shard.ShardRegistryImpl.ShardStateCollector;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.attributes.AttributeService;
import org.alfresco.service.cmr.attributes.AttributeService.AttributeQueryCallback;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.GUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.mockito.Matchers;

/**
 * @author Andy
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ShardRegistryTest
{
    private ShardRegistryImpl shardRegistry;
    
    private DefaultSimpleCache<ShardInstance, ShardState> shardStateCache = new DefaultSimpleCache<ShardInstance, ShardState>();
    
    private DefaultSimpleCache<ShardInstance, String> shardToGuidCache = new DefaultSimpleCache<ShardInstance, String>();
    
    private @Mock AttributeService attributeService;
    
    private @Mock TransactionService transactionService;
    
    private @Mock RetryingTransactionHelper retryingTransactionHelper;

    /**
     * 
     */
    public ShardRegistryTest()
    {
        // TODO Auto-generated constructor stub
    }

    @Before
    public void setUp() throws UnknownHostException
    {
        shardRegistry = new ShardRegistryImpl();
        shardRegistry.setAttributeService(attributeService);
        shardRegistry.setShardStateCache(shardStateCache);
        shardRegistry.setShardToGuidCache(shardToGuidCache);
        shardRegistry.setTransactionService(transactionService);
        shardRegistry.setPurgeOnInit(false);
        shardRegistry.setShardInstanceTimeoutInSeconds(30);
        shardRegistry.setMaxAllowedReplicaTxCountDifference(10);
        shardRegistry.init();
       
        when(transactionService.getRetryingTransactionHelper()).thenReturn(retryingTransactionHelper);
        
        final ArgumentCaptor<RetryingTransactionCallback> argument = ArgumentCaptor.forClass(RetryingTransactionCallback.class);
        when(retryingTransactionHelper.doInTransaction(argument.capture(), anyBoolean(), anyBoolean())).thenAnswer(
                new Answer() {
                    public Object answer(InvocationOnMock invocation) {
                        try
                        {
                            argument.getValue().execute();
                        }
                        catch (Throwable e)
                        {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        return null;
                    }
                });
        
    }
    
    @Test
    public void registerLocalShardState()
    {
    
        ShardState shardState1 = ShardStateBuilder.shardState().withMaster(true).withLastUpdated(System.currentTimeMillis())
           .withShardInstance().withBaseUrl("/solr4/shard1").withHostName("meep").withPort(1234)
               .withShard().withInstance(1)
                   .withFloc().withAddedStoreRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE).withHasContent(true).withNumberOfShards(2).withShardMethod(ShardMethodEnum.MOD_ACL_ID).withTemplate("default")
                   .endFloc().endShard().endShardInstance().build();
        
        shardRegistry.registerShardState(shardState1);
        
        assertEquals(1, shardToGuidCache.getKeys().size());
        assertEquals(1, shardStateCache.getKeys().size());
        
        assertEquals(shardState1, shardStateCache.get(shardState1.getShardInstance()));
        
        String guid1 = shardToGuidCache.get(shardState1.getShardInstance());
        verify(attributeService).setAttribute(shardState1, ".SHARD_STATE", guid1);
        
        
        ShardState shardState2 = ShardStateBuilder.shardState().withMaster(true).withLastUpdated(System.currentTimeMillis())
                .withShardInstance().withBaseUrl("/solr4/shard2").withHostName("meep").withPort(1234)
                    .withShard().withInstance(2)
                        .withFloc().withAddedStoreRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE).withHasContent(true).withNumberOfShards(2).withShardMethod(ShardMethodEnum.MOD_ACL_ID).withTemplate("default")
                        .endFloc().endShard().endShardInstance().build();

        shardRegistry.registerShardState(shardState2);

        assertEquals(2, shardToGuidCache.getKeys().size());
        assertEquals(2, shardStateCache.getKeys().size());

        assertEquals(shardState2, shardStateCache.get(shardState2.getShardInstance()));

        String guid2 = shardToGuidCache.get(shardState2.getShardInstance());
        verify(attributeService).setAttribute(shardState2, ".SHARD_STATE", guid2);
        
        // and again
        
        shardRegistry.registerShardState(shardState2);
        assertEquals(2, shardToGuidCache.getKeys().size());
        assertEquals(2, shardStateCache.getKeys().size());

        assertEquals(shardState2, shardStateCache.get(shardState2.getShardInstance()));
        verify(attributeService, times(2)).setAttribute(shardState2, ".SHARD_STATE", guid2);
        
        SearchParameters sp = new SearchParameters();
        sp.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        List<ShardInstance> slice = shardRegistry.getIndexSlice(sp);
        assertEquals(2, slice.size());
        assertTrue(slice.contains(shardState1.getShardInstance()));
        assertTrue(slice.contains(shardState2.getShardInstance()));        
    }
    
    @Test
    public void testShardsExpire()
    {
    
        ShardState shardState1 = ShardStateBuilder.shardState().withMaster(true).withLastUpdated(System.currentTimeMillis())
           .withShardInstance().withBaseUrl("/solr4/shard1").withHostName("meep").withPort(1234)
               .withShard().withInstance(1)
                   .withFloc().withAddedStoreRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE).withHasContent(true).withNumberOfShards(2).withShardMethod(ShardMethodEnum.MOD_ACL_ID).withTemplate("default")
                   .endFloc().endShard().endShardInstance().build();
        
        shardRegistry.registerShardState(shardState1);
        
        assertEquals(1, shardToGuidCache.getKeys().size());
        assertEquals(1, shardStateCache.getKeys().size());
        
        assertEquals(shardState1, shardStateCache.get(shardState1.getShardInstance()));
        
        String guid1 = shardToGuidCache.get(shardState1.getShardInstance());
        verify(attributeService).setAttribute(shardState1, ".SHARD_STATE", guid1);
        
        
        ShardState shardState2 = ShardStateBuilder.shardState().withMaster(true).withLastUpdated(System.currentTimeMillis())
                .withShardInstance().withBaseUrl("/solr4/shard2").withHostName("meep").withPort(1234)
                    .withShard().withInstance(2)
                        .withFloc().withAddedStoreRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE).withHasContent(true).withNumberOfShards(2).withShardMethod(ShardMethodEnum.MOD_ACL_ID).withTemplate("default")
                        .endFloc().endShard().endShardInstance().build();

        shardRegistry.registerShardState(shardState2);

        assertEquals(2, shardToGuidCache.getKeys().size());
        assertEquals(2, shardStateCache.getKeys().size());

        assertEquals(shardState2, shardStateCache.get(shardState2.getShardInstance()));

        String guid2 = shardToGuidCache.get(shardState2.getShardInstance());
        verify(attributeService).setAttribute(shardState2, ".SHARD_STATE", guid2);
        
        // and again
        
        shardRegistry.registerShardState(shardState2);
        assertEquals(2, shardToGuidCache.getKeys().size());
        assertEquals(2, shardStateCache.getKeys().size());

        assertEquals(shardState2, shardStateCache.get(shardState2.getShardInstance()));
        verify(attributeService, times(2)).setAttribute(shardState2, ".SHARD_STATE", guid2);
        
        SearchParameters sp = new SearchParameters();
        sp.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        List<ShardInstance> slice = shardRegistry.getIndexSlice(sp);
        assertEquals(2, slice.size());
        assertTrue(slice.contains(shardState1.getShardInstance()));
        assertTrue(slice.contains(shardState2.getShardInstance()));        
        
        
        // expire check
        
        synchronized(this)
        {
            try
            {
                wait(40000);
            }
            catch (InterruptedException e)
            {
                fail("Failed to wait");
            }
        }
        
        slice = shardRegistry.getIndexSlice(sp);
        assertEquals(0, slice.size());
         
        shardState1.setLastUpdated(System.currentTimeMillis());
        shardRegistry.registerShardState(shardState1);
        
        slice = shardRegistry.getIndexSlice(sp);
        assertEquals(1, slice.size());
        
        shardState2.setLastUpdated(System.currentTimeMillis());
        shardRegistry.registerShardState(shardState2);
        
        slice = shardRegistry.getIndexSlice(sp);
        assertEquals(2, slice.size());
        
    }
    
    @Test 
    public void registerHalfPersisted()
    {
        final String guid1 = GUID.generate();
        
        final ShardState shardState1 = ShardStateBuilder.shardState().withMaster(true).withLastUpdated(System.currentTimeMillis())
                .withShardInstance().withBaseUrl("/solr4/shard1").withHostName("meep").withPort(1234)
                    .withShard().withInstance(1)
                        .withFloc().withAddedStoreRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE).withHasContent(true).withNumberOfShards(2).withShardMethod(ShardMethodEnum.MOD_ACL_ID).withTemplate("default")
                        .endFloc().endShard().endShardInstance().build();
        
        doAnswer(new Answer<Object>()
                {
                    long id = 0;
                    ShardStateCollector callback;
                    void handle(Serializable value, String... keys)
                    {
                        callback.handleAttribute(id++, value, keys);
                    }
                    
                    @Override
                    public Object answer(InvocationOnMock invocation) throws Throwable
                    {
                        callback = (ShardStateCollector) invocation.getArguments()[0];
                        handle(shardState1, ".SHARD_STATE", guid1);
                        return null;
                    }
                }).when(attributeService).getAttributes(any(AttributeQueryCallback.class), eq(".SHARD_STATE"));
        
        ShardState shardState2 = ShardStateBuilder.shardState().withMaster(true).withLastUpdated(System.currentTimeMillis())
                .withShardInstance().withBaseUrl("/solr4/shard2").withHostName("meep").withPort(1234)
                    .withShard().withInstance(2)
                        .withFloc().withAddedStoreRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE).withHasContent(true).withNumberOfShards(2).withShardMethod(ShardMethodEnum.MOD_ACL_ID).withTemplate("default")
                        .endFloc().endShard().endShardInstance().build();

        shardRegistry.registerShardState(shardState2);

        assertEquals(2, shardToGuidCache.getKeys().size());
        assertEquals(1, shardStateCache.getKeys().size());

        assertEquals(shardState2, shardStateCache.get(shardState2.getShardInstance()));

        String guid2 = shardToGuidCache.get(shardState2.getShardInstance());
        verify(attributeService).setAttribute(shardState2, ".SHARD_STATE", guid2);
        
        // and again
        
        shardRegistry.registerShardState(shardState2);
        assertEquals(2, shardToGuidCache.getKeys().size());
        assertEquals(1, shardStateCache.getKeys().size());

        assertEquals(shardState2, shardStateCache.get(shardState2.getShardInstance()));
        verify(attributeService, times(2)).setAttribute(shardState2, ".SHARD_STATE", guid2);
        
        SearchParameters sp = new SearchParameters();
        sp.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        List<ShardInstance> slice = shardRegistry.getIndexSlice(sp);
        assertEquals(2, slice.size());
        assertTrue(slice.contains(shardState1.getShardInstance()));
        assertTrue(slice.contains(shardState2.getShardInstance()));
        
    }
    
    @Test 
    public void registerAllPersisted()
    {
        final String guid1 = GUID.generate();
        final String guid2 = GUID.generate();
        
        final ShardState shardState1 = ShardStateBuilder.shardState().withMaster(true).withLastUpdated(System.currentTimeMillis())
                .withShardInstance().withBaseUrl("/solr4/shard1").withHostName("meep").withPort(1234)
                    .withShard().withInstance(1)
                        .withFloc().withAddedStoreRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE).withHasContent(true).withNumberOfShards(2).withShardMethod(ShardMethodEnum.MOD_ACL_ID).withTemplate("default")
                        .endFloc().endShard().endShardInstance().build();
        
        final ShardState shardState2 = ShardStateBuilder.shardState().withMaster(true).withLastUpdated(System.currentTimeMillis())
                .withShardInstance().withBaseUrl("/solr4/shard2").withHostName("meep").withPort(1234)
                    .withShard().withInstance(2)
                        .withFloc().withAddedStoreRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE).withHasContent(true).withNumberOfShards(2).withShardMethod(ShardMethodEnum.MOD_ACL_ID).withTemplate("default")
                        .endFloc().endShard().endShardInstance().build();
        
        doAnswer(new Answer<Object>()
                {
                    long id = 0;
                    ShardStateCollector callback;
                    void handle(Serializable value, String... keys)
                    {
                        callback.handleAttribute(id++, value, keys);
                    }
                    
                    @Override
                    public Object answer(InvocationOnMock invocation) throws Throwable
                    {
                        callback = (ShardStateCollector) invocation.getArguments()[0];
                        handle(shardState1, ".SHARD_STATE", guid1);
                        handle(shardState2, ".SHARD_STATE", guid2);
                        return null;
                    }
                }).when(attributeService).getAttributes(any(AttributeQueryCallback.class), eq(".SHARD_STATE"));
        
        
      

        shardRegistry.registerShardState(shardState2);

        assertEquals(2, shardToGuidCache.getKeys().size());
        assertEquals(1, shardStateCache.getKeys().size());

        assertEquals(shardState2, shardStateCache.get(shardState2.getShardInstance()));

        verify(attributeService).setAttribute(shardState2, ".SHARD_STATE", guid2);
        
        // and again
        
        shardRegistry.registerShardState(shardState2);
        assertEquals(2, shardToGuidCache.getKeys().size());
        assertEquals(1, shardStateCache.getKeys().size());

        assertEquals(shardState2, shardStateCache.get(shardState2.getShardInstance()));
        verify(attributeService, times(2)).setAttribute(shardState2, ".SHARD_STATE", guid2);
        
        SearchParameters sp = new SearchParameters();
        sp.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        List<ShardInstance> slice = shardRegistry.getIndexSlice(sp);
        assertEquals(2, slice.size());
        assertTrue(slice.contains(shardState1.getShardInstance()));
        assertTrue(slice.contains(shardState2.getShardInstance()));
        
    }
    
    
 
    @Test
    public void testFromPersisted()
    {
        final String guid1 = GUID.generate();
        final String guid2 = GUID.generate();
        
        final ShardState shardState1 = ShardStateBuilder.shardState().withMaster(true).withLastUpdated(System.currentTimeMillis())
                .withShardInstance().withBaseUrl("/solr4/shard1").withHostName("meep").withPort(1234)
                    .withShard().withInstance(1)
                        .withFloc().withAddedStoreRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE).withHasContent(true).withNumberOfShards(2).withShardMethod(ShardMethodEnum.MOD_ACL_ID).withTemplate("default")
                        .endFloc().endShard().endShardInstance().build();
        
        final ShardState shardState2 = ShardStateBuilder.shardState().withMaster(true).withLastUpdated(System.currentTimeMillis())
                .withShardInstance().withBaseUrl("/solr4/shard2").withHostName("meep").withPort(1234)
                    .withShard().withInstance(2)
                        .withFloc().withAddedStoreRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE).withHasContent(true).withNumberOfShards(2).withShardMethod(ShardMethodEnum.MOD_ACL_ID).withTemplate("default")
                        .endFloc().endShard().endShardInstance().build();
        
        doAnswer(new Answer<Object>()
                {
                    long id = 0;
                    ShardStateCollector callback;
                    void handle(Serializable value, String... keys)
                    {
                        callback.handleAttribute(id++, value, keys);
                    }
                    
                    @Override
                    public Object answer(InvocationOnMock invocation) throws Throwable
                    {
                        callback = (ShardStateCollector) invocation.getArguments()[0];
                        handle(shardState1, ".SHARD_STATE", guid1);
                        handle(shardState2, ".SHARD_STATE", guid2);
                        return null;
                    }
                }).when(attributeService).getAttributes(any(AttributeQueryCallback.class), eq(".SHARD_STATE"));
        
        SearchParameters sp = new SearchParameters();
        sp.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        List<ShardInstance> slice = shardRegistry.getIndexSlice(sp);
        assertEquals(2, slice.size());
        assertTrue(slice.contains(shardState1.getShardInstance()));
        assertTrue(slice.contains(shardState2.getShardInstance()));
    }
    
    @Test
    public void testComplexFromPersisted()
    {
        final String guid1 = GUID.generate();
        final String guid2 = GUID.generate();
        final String guid3 = GUID.generate();
        final String guid4 = GUID.generate();
        final String guid5 = GUID.generate();
        final String guid6 = GUID.generate();
        final String guid7 = GUID.generate();
        
        final ShardState shardState1 = ShardStateBuilder.shardState().withMaster(true).withLastUpdated(System.currentTimeMillis())
                .withShardInstance().withBaseUrl("/solr4/shard1").withHostName("meep").withPort(1234)
                    .withShard().withInstance(1)
                        .withFloc().withAddedStoreRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE).withHasContent(true).withNumberOfShards(2).withShardMethod(ShardMethodEnum.MOD_ACL_ID).withTemplate("default")
                        .endFloc().endShard().endShardInstance().build();
        
        final ShardState shardState2 = ShardStateBuilder.shardState().withMaster(true).withLastUpdated(System.currentTimeMillis())
                .withShardInstance().withBaseUrl("/solr4/shard2").withHostName("meep").withPort(1234)
                    .withShard().withInstance(2)
                        .withFloc().withAddedStoreRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE).withHasContent(true).withNumberOfShards(2).withShardMethod(ShardMethodEnum.MOD_ACL_ID).withTemplate("default")
                        .endFloc().endShard().endShardInstance().build();
        
        final ShardState shardState3 = ShardStateBuilder.shardState().withMaster(true).withLastUpdated(System.currentTimeMillis())
                .withShardInstance().withBaseUrl("/solr4/rep1").withHostName("meep").withPort(1234)
                    .withShard().withInstance(1)
                        .withFloc().withAddedStoreRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE).withHasContent(true).withNumberOfShards(2).withShardMethod(ShardMethodEnum.MOD_ACL_ID).withTemplate("default")
                        .endFloc().endShard().endShardInstance().build();
        
        final ShardState shardState4 = ShardStateBuilder.shardState().withMaster(true).withLastUpdated(System.currentTimeMillis())
                .withShardInstance().withBaseUrl("/solr4/rep2").withHostName("meep").withPort(1234)
                    .withShard().withInstance(2)
                        .withFloc().withAddedStoreRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE).withHasContent(true).withNumberOfShards(2).withShardMethod(ShardMethodEnum.MOD_ACL_ID).withTemplate("default")
                        .endFloc().endShard().endShardInstance().build();
        
        final ShardState shardState5 = ShardStateBuilder.shardState().withMaster(true).withLastUpdated(System.currentTimeMillis())
                .withShardInstance().withBaseUrl("/solr4/shard1").withHostName("meep").withPort(1234)
                    .withShard().withInstance(1)
                        .withFloc().withAddedStoreRef(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE).withHasContent(true).withNumberOfShards(2).withShardMethod(ShardMethodEnum.MOD_ACL_ID).withTemplate("default")
                        .endFloc().endShard().endShardInstance().build();
        
        final ShardState shardState6 = ShardStateBuilder.shardState().withMaster(true).withLastUpdated(System.currentTimeMillis())
                .withShardInstance().withBaseUrl("/solr4/shard2").withHostName("meep").withPort(1234)
                    .withShard().withInstance(2)
                        .withFloc().withAddedStoreRef(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE).withHasContent(true).withNumberOfShards(2).withShardMethod(ShardMethodEnum.MOD_ACL_ID).withTemplate("default")
                        .endFloc().endShard().endShardInstance().build();
        
        final ShardState shardState7 = ShardStateBuilder.shardState().withMaster(true).withLastUpdated(System.currentTimeMillis())
                .withShardInstance().withBaseUrl("/solr4/shard2").withHostName("meep").withPort(1234)
                    .withShard().withInstance(1)
                        .withFloc().withAddedStoreRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE).withHasContent(true).withNumberOfShards(1).withShardMethod(ShardMethodEnum.MOD_ACL_ID).withTemplate("default")
                        .endFloc().endShard().endShardInstance().build();
        
        doAnswer(new Answer<Object>()
                {
                    long id = 0;
                    ShardStateCollector callback;
                    void handle(Serializable value, String... keys)
                    {
                        callback.handleAttribute(id++, value, keys);
                    }
                    
                    @Override
                    public Object answer(InvocationOnMock invocation) throws Throwable
                    {
                        callback = (ShardStateCollector) invocation.getArguments()[0];
                        handle(shardState1, ".SHARD_STATE", guid1);
                        handle(shardState2, ".SHARD_STATE", guid2);
                        handle(shardState3, ".SHARD_STATE", guid3);
                        handle(shardState4, ".SHARD_STATE", guid4);
                        handle(shardState5, ".SHARD_STATE", guid5);
                        handle(shardState6, ".SHARD_STATE", guid6);
                        return null;
                    }
                }).when(attributeService).getAttributes(any(AttributeQueryCallback.class), eq(".SHARD_STATE"));
        
        SearchParameters sp = new SearchParameters();
        sp.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        List<ShardInstance> slice = shardRegistry.getIndexSlice(sp);
        assertEquals(2, slice.size());
        assertTrue(slice.contains(shardState1.getShardInstance()) || slice.contains(shardState3.getShardInstance()));
        assertTrue(slice.contains(shardState2.getShardInstance()) || slice.contains(shardState4.getShardInstance()));
        
        
        sp = new SearchParameters();
        sp.addStore(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE);
        slice = shardRegistry.getIndexSlice(sp);
        assertEquals(2, slice.size());
        assertTrue(slice.contains(shardState5.getShardInstance()));
        assertTrue(slice.contains(shardState6.getShardInstance()));
    }
    
    @Test
    public void testSelectFlocBasedInTxCount1()
    {
        final String guid1 = GUID.generate();
        
        final String guid2 = GUID.generate();
        final String guid3 = GUID.generate();
        
        final String guid4 = GUID.generate();
        final String guid5 = GUID.generate();
        final String guid6 = GUID.generate();
        final String guid7 = GUID.generate();
        
        final ShardState shardState1 = ShardStateBuilder.shardState().withMaster(true).withLastUpdated(System.currentTimeMillis()).withLastIndexedTxId(10)
                .withShardInstance().withBaseUrl("/solr4/single").withHostName("meep").withPort(1234)
                    .withShard().withInstance(1)
                        .withFloc().withAddedStoreRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE).withHasContent(true).withNumberOfShards(1).withShardMethod(ShardMethodEnum.MOD_ACL_ID).withTemplate("default")
                        .endFloc().endShard().endShardInstance().build();
        
        
        final ShardState shardState2 = ShardStateBuilder.shardState().withMaster(true).withLastUpdated(System.currentTimeMillis()).withLastIndexedTxId(9)
                .withShardInstance().withBaseUrl("/solr4/2-1").withHostName("meep").withPort(1234)
                    .withShard().withInstance(1)
                        .withFloc().withAddedStoreRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE).withHasContent(true).withNumberOfShards(2).withShardMethod(ShardMethodEnum.MOD_ACL_ID).withTemplate("default")
                        .endFloc().endShard().endShardInstance().build();
        
        final ShardState shardState3 = ShardStateBuilder.shardState().withMaster(true).withLastUpdated(System.currentTimeMillis()).withLastIndexedTxId(9)
                .withShardInstance().withBaseUrl("/solr4/2-2").withHostName("meep").withPort(1234)
                    .withShard().withInstance(2)
                        .withFloc().withAddedStoreRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE).withHasContent(true).withNumberOfShards(2).withShardMethod(ShardMethodEnum.MOD_ACL_ID).withTemplate("default")
                        .endFloc().endShard().endShardInstance().build();
        
        final ShardState shardState4 = ShardStateBuilder.shardState().withMaster(true).withLastUpdated(System.currentTimeMillis()).withLastIndexedTxId(8)
                .withShardInstance().withBaseUrl("/solr4/4-1").withHostName("meep").withPort(1234)
                    .withShard().withInstance(1)
                        .withFloc().withAddedStoreRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE).withHasContent(true).withNumberOfShards(4).withShardMethod(ShardMethodEnum.MOD_ACL_ID).withTemplate("default")
                        .endFloc().endShard().endShardInstance().build();
        
        final ShardState shardState5 = ShardStateBuilder.shardState().withMaster(true).withLastUpdated(System.currentTimeMillis()).withLastIndexedTxId(8)
                .withShardInstance().withBaseUrl("/solr4/4-2").withHostName("meep").withPort(1234)
                    .withShard().withInstance(2)
                        .withFloc().withAddedStoreRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE).withHasContent(true).withNumberOfShards(4).withShardMethod(ShardMethodEnum.MOD_ACL_ID).withTemplate("default")
                        .endFloc().endShard().endShardInstance().build();
        
        final ShardState shardState6 = ShardStateBuilder.shardState().withMaster(true).withLastUpdated(System.currentTimeMillis()).withLastIndexedTxId(8)
                .withShardInstance().withBaseUrl("/solr4/4-3").withHostName("meep").withPort(1234)
                    .withShard().withInstance(3)
                        .withFloc().withAddedStoreRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE).withHasContent(true).withNumberOfShards(4).withShardMethod(ShardMethodEnum.MOD_ACL_ID).withTemplate("default")
                        .endFloc().endShard().endShardInstance().build();
        
        final ShardState shardState7 = ShardStateBuilder.shardState().withMaster(true).withLastUpdated(System.currentTimeMillis()).withLastIndexedTxId(8)
                .withShardInstance().withBaseUrl("/solr4/4-4").withHostName("meep").withPort(1234)
                    .withShard().withInstance(4)
                        .withFloc().withAddedStoreRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE).withHasContent(true).withNumberOfShards(4).withShardMethod(ShardMethodEnum.MOD_ACL_ID).withTemplate("default")
                        .endFloc().endShard().endShardInstance().build();
     
        
        doAnswer(new Answer<Object>()
                {
                    long id = 0;
                    ShardStateCollector callback;
                    void handle(Serializable value, String... keys)
                    {
                        callback.handleAttribute(id++, value, keys);
                    }
                    
                    @Override
                    public Object answer(InvocationOnMock invocation) throws Throwable
                    {
                        callback = (ShardStateCollector) invocation.getArguments()[0];
                        handle(shardState1, ".SHARD_STATE", guid1);
                        handle(shardState2, ".SHARD_STATE", guid2);
                        handle(shardState3, ".SHARD_STATE", guid3);
                        handle(shardState4, ".SHARD_STATE", guid4);
                        handle(shardState5, ".SHARD_STATE", guid5);
                        handle(shardState6, ".SHARD_STATE", guid6);
                        handle(shardState7, ".SHARD_STATE", guid7);
                        return null;
                    }
                }).when(attributeService).getAttributes(any(AttributeQueryCallback.class), eq(".SHARD_STATE"));
        
        SearchParameters sp = new SearchParameters();
        sp.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        List<ShardInstance> slice = shardRegistry.getIndexSlice(sp);
        assertEquals(1, slice.size());
        assertTrue(slice.contains(shardState1.getShardInstance()));
    }
    
    @Test
    public void testSelectFlocBasedInTxCount2()
    {
        final String guid1 = GUID.generate();
        
        final String guid2 = GUID.generate();
        final String guid3 = GUID.generate();
        
        final String guid4 = GUID.generate();
        final String guid5 = GUID.generate();
        final String guid6 = GUID.generate();
        final String guid7 = GUID.generate();
        
        final ShardState shardState1 = ShardStateBuilder.shardState().withMaster(true).withLastUpdated(System.currentTimeMillis()).withLastIndexedTxId(8)
                .withShardInstance().withBaseUrl("/solr4/single").withHostName("meep").withPort(1234)
                    .withShard().withInstance(1)
                        .withFloc().withAddedStoreRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE).withHasContent(true).withNumberOfShards(1).withShardMethod(ShardMethodEnum.MOD_ACL_ID).withTemplate("default")
                        .endFloc().endShard().endShardInstance().build();
        
        
        final ShardState shardState2 = ShardStateBuilder.shardState().withMaster(true).withLastUpdated(System.currentTimeMillis()).withLastIndexedTxId(10)
                .withShardInstance().withBaseUrl("/solr4/2-1").withHostName("meep").withPort(1234)
                    .withShard().withInstance(1)
                        .withFloc().withAddedStoreRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE).withHasContent(true).withNumberOfShards(2).withShardMethod(ShardMethodEnum.MOD_ACL_ID).withTemplate("default")
                        .endFloc().endShard().endShardInstance().build();
        
        final ShardState shardState3 = ShardStateBuilder.shardState().withMaster(true).withLastUpdated(System.currentTimeMillis()).withLastIndexedTxId(10)
                .withShardInstance().withBaseUrl("/solr4/2-2").withHostName("meep").withPort(1234)
                    .withShard().withInstance(2)
                        .withFloc().withAddedStoreRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE).withHasContent(true).withNumberOfShards(2).withShardMethod(ShardMethodEnum.MOD_ACL_ID).withTemplate("default")
                        .endFloc().endShard().endShardInstance().build();
        
        final ShardState shardState4 = ShardStateBuilder.shardState().withMaster(true).withLastUpdated(System.currentTimeMillis()).withLastIndexedTxId(9)
                .withShardInstance().withBaseUrl("/solr4/4-1").withHostName("meep").withPort(1234)
                    .withShard().withInstance(1)
                        .withFloc().withAddedStoreRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE).withHasContent(true).withNumberOfShards(4).withShardMethod(ShardMethodEnum.MOD_ACL_ID).withTemplate("default")
                        .endFloc().endShard().endShardInstance().build();
        
        final ShardState shardState5 = ShardStateBuilder.shardState().withMaster(true).withLastUpdated(System.currentTimeMillis()).withLastIndexedTxId(9)
                .withShardInstance().withBaseUrl("/solr4/4-2").withHostName("meep").withPort(1234)
                    .withShard().withInstance(2)
                        .withFloc().withAddedStoreRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE).withHasContent(true).withNumberOfShards(4).withShardMethod(ShardMethodEnum.MOD_ACL_ID).withTemplate("default")
                        .endFloc().endShard().endShardInstance().build();
        
        final ShardState shardState6 = ShardStateBuilder.shardState().withMaster(true).withLastUpdated(System.currentTimeMillis()).withLastIndexedTxId(9)
                .withShardInstance().withBaseUrl("/solr4/4-3").withHostName("meep").withPort(1234)
                    .withShard().withInstance(3)
                        .withFloc().withAddedStoreRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE).withHasContent(true).withNumberOfShards(4).withShardMethod(ShardMethodEnum.MOD_ACL_ID).withTemplate("default")
                        .endFloc().endShard().endShardInstance().build();
        
        final ShardState shardState7 = ShardStateBuilder.shardState().withMaster(true).withLastUpdated(System.currentTimeMillis()).withLastIndexedTxId(9)
                .withShardInstance().withBaseUrl("/solr4/4-4").withHostName("meep").withPort(1234)
                    .withShard().withInstance(4)
                        .withFloc().withAddedStoreRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE).withHasContent(true).withNumberOfShards(4).withShardMethod(ShardMethodEnum.MOD_ACL_ID).withTemplate("default")
                        .endFloc().endShard().endShardInstance().build();
     
        
        doAnswer(new Answer<Object>()
                {
                    long id = 0;
                    ShardStateCollector callback;
                    void handle(Serializable value, String... keys)
                    {
                        callback.handleAttribute(id++, value, keys);
                    }
                    
                    @Override
                    public Object answer(InvocationOnMock invocation) throws Throwable
                    {
                        callback = (ShardStateCollector) invocation.getArguments()[0];
                        handle(shardState1, ".SHARD_STATE", guid1);
                        handle(shardState2, ".SHARD_STATE", guid2);
                        handle(shardState3, ".SHARD_STATE", guid3);
                        handle(shardState4, ".SHARD_STATE", guid4);
                        handle(shardState5, ".SHARD_STATE", guid5);
                        handle(shardState6, ".SHARD_STATE", guid6);
                        handle(shardState7, ".SHARD_STATE", guid7);
                        return null;
                    }
                }).when(attributeService).getAttributes(any(AttributeQueryCallback.class), eq(".SHARD_STATE"));
        
        SearchParameters sp = new SearchParameters();
        sp.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        List<ShardInstance> slice = shardRegistry.getIndexSlice(sp);
        assertEquals(2, slice.size());
        assertTrue(slice.contains(shardState2.getShardInstance()));
        assertTrue(slice.contains(shardState3.getShardInstance()));
    }
    
    
    @Test
    public void testSelectFlocBasedInTxCount3()
    {
        final String guid1 = GUID.generate();
        
        final String guid2 = GUID.generate();
        final String guid3 = GUID.generate();
        
        final String guid4 = GUID.generate();
        final String guid5 = GUID.generate();
        final String guid6 = GUID.generate();
        final String guid7 = GUID.generate();
        
        final ShardState shardState1 = ShardStateBuilder.shardState().withMaster(true).withLastUpdated(System.currentTimeMillis()).withLastIndexedTxId(9)
                .withShardInstance().withBaseUrl("/solr4/single").withHostName("meep").withPort(1234)
                    .withShard().withInstance(1)
                        .withFloc().withAddedStoreRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE).withHasContent(true).withNumberOfShards(1).withShardMethod(ShardMethodEnum.MOD_ACL_ID).withTemplate("default")
                        .endFloc().endShard().endShardInstance().build();
        
        
        final ShardState shardState2 = ShardStateBuilder.shardState().withMaster(true).withLastUpdated(System.currentTimeMillis()).withLastIndexedTxId(8)
                .withShardInstance().withBaseUrl("/solr4/2-1").withHostName("meep").withPort(1234)
                    .withShard().withInstance(1)
                        .withFloc().withAddedStoreRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE).withHasContent(true).withNumberOfShards(2).withShardMethod(ShardMethodEnum.MOD_ACL_ID).withTemplate("default")
                        .endFloc().endShard().endShardInstance().build();
        
        final ShardState shardState3 = ShardStateBuilder.shardState().withMaster(true).withLastUpdated(System.currentTimeMillis()).withLastIndexedTxId(8)
                .withShardInstance().withBaseUrl("/solr4/2-2").withHostName("meep").withPort(1234)
                    .withShard().withInstance(2)
                        .withFloc().withAddedStoreRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE).withHasContent(true).withNumberOfShards(2).withShardMethod(ShardMethodEnum.MOD_ACL_ID).withTemplate("default")
                        .endFloc().endShard().endShardInstance().build();
        
        final ShardState shardState4 = ShardStateBuilder.shardState().withMaster(true).withLastUpdated(System.currentTimeMillis()).withLastIndexedTxId(10)
                .withShardInstance().withBaseUrl("/solr4/4-1").withHostName("meep").withPort(1234)
                    .withShard().withInstance(1)
                        .withFloc().withAddedStoreRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE).withHasContent(true).withNumberOfShards(4).withShardMethod(ShardMethodEnum.MOD_ACL_ID).withTemplate("default")
                        .endFloc().endShard().endShardInstance().build();
        
        final ShardState shardState5 = ShardStateBuilder.shardState().withMaster(true).withLastUpdated(System.currentTimeMillis()).withLastIndexedTxId(10)
                .withShardInstance().withBaseUrl("/solr4/4-2").withHostName("meep").withPort(1234)
                    .withShard().withInstance(2)
                        .withFloc().withAddedStoreRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE).withHasContent(true).withNumberOfShards(4).withShardMethod(ShardMethodEnum.MOD_ACL_ID).withTemplate("default")
                        .endFloc().endShard().endShardInstance().build();
        
        final ShardState shardState6 = ShardStateBuilder.shardState().withMaster(true).withLastUpdated(System.currentTimeMillis()).withLastIndexedTxId(10)
                .withShardInstance().withBaseUrl("/solr4/4-3").withHostName("meep").withPort(1234)
                    .withShard().withInstance(3)
                        .withFloc().withAddedStoreRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE).withHasContent(true).withNumberOfShards(4).withShardMethod(ShardMethodEnum.MOD_ACL_ID).withTemplate("default")
                        .endFloc().endShard().endShardInstance().build();
        
        final ShardState shardState7 = ShardStateBuilder.shardState().withMaster(true).withLastUpdated(System.currentTimeMillis()).withLastIndexedTxId(10)
                .withShardInstance().withBaseUrl("/solr4/4-4").withHostName("meep").withPort(1234)
                    .withShard().withInstance(4)
                        .withFloc().withAddedStoreRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE).withHasContent(true).withNumberOfShards(4).withShardMethod(ShardMethodEnum.MOD_ACL_ID).withTemplate("default")
                        .endFloc().endShard().endShardInstance().build();
     
        
        doAnswer(new Answer<Object>()
                {
                    long id = 0;
                    ShardStateCollector callback;
                    void handle(Serializable value, String... keys)
                    {
                        callback.handleAttribute(id++, value, keys);
                    }
                    
                    @Override
                    public Object answer(InvocationOnMock invocation) throws Throwable
                    {
                        callback = (ShardStateCollector) invocation.getArguments()[0];
                        handle(shardState1, ".SHARD_STATE", guid1);
                        handle(shardState2, ".SHARD_STATE", guid2);
                        handle(shardState3, ".SHARD_STATE", guid3);
                        handle(shardState4, ".SHARD_STATE", guid4);
                        handle(shardState5, ".SHARD_STATE", guid5);
                        handle(shardState6, ".SHARD_STATE", guid6);
                        handle(shardState7, ".SHARD_STATE", guid7);
                        return null;
                    }
                }).when(attributeService).getAttributes(any(AttributeQueryCallback.class), eq(".SHARD_STATE"));
        
        SearchParameters sp = new SearchParameters();
        sp.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        List<ShardInstance> slice = shardRegistry.getIndexSlice(sp);
        assertEquals(4, slice.size());
        assertTrue(slice.contains(shardState4.getShardInstance()));
        assertTrue(slice.contains(shardState5.getShardInstance()));
        assertTrue(slice.contains(shardState6.getShardInstance()));  
        assertTrue(slice.contains(shardState7.getShardInstance()));
    }
    
    @Test
    public void testReplicaSelection()
    {
        final String guid1 = GUID.generate();
        final String guid2 = GUID.generate();
        
        final String guid3 = GUID.generate();
        final String guid4 = GUID.generate();
        
        final String guid5 = GUID.generate();
        final String guid6 = GUID.generate();
        
        final ShardState shardState1 = ShardStateBuilder.shardState().withMaster(true).withLastUpdated(System.currentTimeMillis()).withLastIndexedTxId(100)
                .withShardInstance().withBaseUrl("/solr4/1-1").withHostName("meep").withPort(1234)
                    .withShard().withInstance(1)
                        .withFloc().withAddedStoreRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE).withHasContent(true).withNumberOfShards(2).withShardMethod(ShardMethodEnum.MOD_ACL_ID).withTemplate("default")
                        .endFloc().endShard().endShardInstance().build();
        
        final ShardState shardState2 = ShardStateBuilder.shardState().withMaster(true).withLastUpdated(System.currentTimeMillis()).withLastIndexedTxId(100)
                .withShardInstance().withBaseUrl("/solr4/1-2").withHostName("meep").withPort(1234)
                    .withShard().withInstance(2)
                        .withFloc().withAddedStoreRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE).withHasContent(true).withNumberOfShards(2).withShardMethod(ShardMethodEnum.MOD_ACL_ID).withTemplate("default")
                        .endFloc().endShard().endShardInstance().build();
        
        final ShardState shardState3 = ShardStateBuilder.shardState().withMaster(true).withLastUpdated(System.currentTimeMillis()).withLastIndexedTxId(90)
                .withShardInstance().withBaseUrl("/solr4/2-1").withHostName("meep").withPort(1234)
                    .withShard().withInstance(1)
                        .withFloc().withAddedStoreRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE).withHasContent(true).withNumberOfShards(2).withShardMethod(ShardMethodEnum.MOD_ACL_ID).withTemplate("default")
                        .endFloc().endShard().endShardInstance().build();
        
        final ShardState shardState4 = ShardStateBuilder.shardState().withMaster(true).withLastUpdated(System.currentTimeMillis()).withLastIndexedTxId(90)
                .withShardInstance().withBaseUrl("/solr4/2-2").withHostName("meep").withPort(1234)
                    .withShard().withInstance(2)
                        .withFloc().withAddedStoreRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE).withHasContent(true).withNumberOfShards(2).withShardMethod(ShardMethodEnum.MOD_ACL_ID).withTemplate("default")
                        .endFloc().endShard().endShardInstance().build();
        
        final ShardState shardState5 = ShardStateBuilder.shardState().withMaster(true).withLastUpdated(System.currentTimeMillis()).withLastIndexedTxId(80)
                .withShardInstance().withBaseUrl("/solr4/3-1").withHostName("meep").withPort(1234)
                    .withShard().withInstance(1)
                        .withFloc().withAddedStoreRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE).withHasContent(true).withNumberOfShards(2).withShardMethod(ShardMethodEnum.MOD_ACL_ID).withTemplate("default")
                        .endFloc().endShard().endShardInstance().build();
        
        final ShardState shardState6 = ShardStateBuilder.shardState().withMaster(true).withLastUpdated(System.currentTimeMillis()).withLastIndexedTxId(80)
                .withShardInstance().withBaseUrl("/solr4/3-2").withHostName("meep").withPort(1234)
                    .withShard().withInstance(2)
                        .withFloc().withAddedStoreRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE).withHasContent(true).withNumberOfShards(2).withShardMethod(ShardMethodEnum.MOD_ACL_ID).withTemplate("default")
                        .endFloc().endShard().endShardInstance().build();
        
        
        doAnswer(new Answer<Object>()
                {
                    long id = 0;
                    ShardStateCollector callback;
                    void handle(Serializable value, String... keys)
                    {
                        callback.handleAttribute(id++, value, keys);
                    }
                    
                    @Override
                    public Object answer(InvocationOnMock invocation) throws Throwable
                    {
                        callback = (ShardStateCollector) invocation.getArguments()[0];
                        handle(shardState1, ".SHARD_STATE", guid1);
                        handle(shardState2, ".SHARD_STATE", guid2);
                        handle(shardState3, ".SHARD_STATE", guid3);
                        handle(shardState4, ".SHARD_STATE", guid4);
                        handle(shardState5, ".SHARD_STATE", guid5);
                        handle(shardState6, ".SHARD_STATE", guid6);
                        return null;
                    }
                }).when(attributeService).getAttributes(any(AttributeQueryCallback.class), eq(".SHARD_STATE"));
        
        HashSet<ShardInstance> found = new HashSet<ShardInstance>();
        for(int i = 0; i < 1000; i++)
        {
            SearchParameters sp = new SearchParameters();
            sp.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
            List<ShardInstance> slice = shardRegistry.getIndexSlice(sp);
            assertEquals(2, slice.size());
            assertTrue(slice.contains(shardState1.getShardInstance()) || slice.contains(shardState3.getShardInstance()));
            assertTrue(slice.contains(shardState2.getShardInstance()) || slice.contains(shardState4.getShardInstance()));
            
            found.add(slice.get(0));
            found.add(slice.get(1));
        }
        
        assertEquals(4, found.size());
    }
}
