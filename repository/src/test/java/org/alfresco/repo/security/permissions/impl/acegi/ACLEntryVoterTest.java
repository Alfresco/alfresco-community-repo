/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.security.permissions.impl.acegi;

import static java.util.Collections.singletonList;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.acegisecurity.ConfigAttribute;
import net.sf.acegisecurity.ConfigAttributeDefinition;
import net.sf.acegisecurity.vote.AccessDecisionVoter;

import org.alfresco.repo.security.permissions.impl.AbstractPermissionTest;
import org.alfresco.repo.security.permissions.impl.SimplePermissionEntry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.junit.experimental.categories.Category;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.framework.adapter.AdvisorAdapterRegistry;
import org.springframework.aop.framework.adapter.GlobalAdvisorAdapterRegistry;
import org.springframework.aop.target.SingletonTargetSource;

@Category(OwnJVMTestsCategory.class)
public class ACLEntryVoterTest extends AbstractPermissionTest
{
    private static final String ANDY = "andy";
    private static final String ACL_NODE_0_SYS_BASE_READ = "ACL_NODE.0.sys:base.Read";
    private static final String TEST_LIST_OF_NODE_REFS = "testListOfNodeRefs";
    private static final String ABSTAIN = "ABSTAIN";
    private static final String DENIED = "Access denied";
    private static final String SHOULD_FAIL_DENIED = "Should fail because node is DENIED";
    public static final String SHOULD_FAIL_ABSTAINED = "Should fail because node is ABSTAINED";

    public ACLEntryVoterTest()
    {
        super();
    }

    public void testBasicDenyNode() throws Exception
    {
        runAs(ANDY);

        Object o = new ClassWithMethods();
        Method method = o.getClass().getMethod("testOneNodeRef", new Class[] { NodeRef.class });

        Object proxy = getProxy(o, ACL_NODE_0_SYS_BASE_READ);

        try
        {
            method.invoke(proxy, new Object[] { rootNodeRef });
            fail(SHOULD_FAIL_DENIED);
        }
        catch (InvocationTargetException e)
        {
            verifyAccessDenied(e);
        }

        try
        {
            method.invoke(proxy, new Object[] { systemNodeRef });
            fail(SHOULD_FAIL_DENIED);
        }
        catch (InvocationTargetException e)
        {
            verifyAccessDenied(e);
        }

        // Check we are allowed access to deleted nodes ..

        nodeService.deleteNode(systemNodeRef);

        assertNull(method.invoke(proxy, new Object[] { systemNodeRef }));

    }

    public void testBasicDenyStore() throws Exception
    {
        runAs(ANDY);

        Object o = new ClassWithMethods();
        Method method = o.getClass().getMethod("testOneStoreRef", new Class[] { StoreRef.class });

        Object proxy = getProxy(o, ACL_NODE_0_SYS_BASE_READ);

        try
        {
            method.invoke(proxy, new Object[] { rootNodeRef.getStoreRef() });
            fail(SHOULD_FAIL_DENIED);
        }
        catch (InvocationTargetException e)
        {
            verifyAccessDenied(e);
        }

    }

    public void testAllowNullNode() throws Exception
    {
        runAs(ANDY);

        Object o = new ClassWithMethods();
        Method method = o.getClass().getMethod("testOneNodeRef", new Class[] { NodeRef.class });

        Object proxy = getProxy(o, ACL_NODE_0_SYS_BASE_READ);

        method.invoke(proxy, new Object[] { null });

    }

    public void testAllowNullStore() throws Exception
    {
        runAs(ANDY);

        Object o = new ClassWithMethods();
        Method method = o.getClass().getMethod("testOneStoreRef", new Class[] { StoreRef.class });

        Object proxy = getProxy(o, ACL_NODE_0_SYS_BASE_READ);

        method.invoke(proxy, new Object[] { null });

    }

    public void testAllowNullParentOnRealChildAssoc() throws Exception
    {
        runAs(ANDY);

        Object o = new ClassWithMethods();
        Method method = o.getClass().getMethod("testOneChildAssociationRef", new Class[] { ChildAssociationRef.class });

        Object proxy = getProxy(o, "ACL_PARENT.0.sys:base.Read");

        method.invoke(proxy, new Object[] { nodeService.getPrimaryParent(rootNodeRef) });

    }

    public void testAllowNullParent() throws Exception
    {
        runAs(ANDY);

        Object o = new ClassWithMethods();
        Method method = o.getClass().getMethod("testOneChildAssociationRef", new Class[] { ChildAssociationRef.class });

        Object proxy = getProxy(o, "ACL_PARENT.0.sys:base.Read");

        method.invoke(proxy, new Object[] { null });

    }

    public void testAllowNullChild() throws Exception
    {
        runAs(ANDY);

        Object o = new ClassWithMethods();
        Method method = o.getClass().getMethod("testOneChildAssociationRef", new Class[] { ChildAssociationRef.class });

        Object proxy = getProxy(o, ACL_NODE_0_SYS_BASE_READ);

        method.invoke(proxy, new Object[] { null });

    }

    public void testBasicDenyChildAssocNode() throws Exception
    {
        runAs(ANDY);

        Object o = new ClassWithMethods();
        Method method = o.getClass().getMethod("testOneChildAssociationRef", new Class[] { ChildAssociationRef.class });

        Object proxy = getProxy(o, ACL_NODE_0_SYS_BASE_READ);

        try
        {
            method.invoke(proxy, new Object[] { nodeService.getPrimaryParent(rootNodeRef) });
            fail(SHOULD_FAIL_DENIED);
        }
        catch (InvocationTargetException e)
        {
            verifyAccessDenied(e);
        }
    }

    public void testBasicDenyParentAssocNode() throws Exception
    {
        runAs(ANDY);

        Object o = new ClassWithMethods();
        Method method = o.getClass().getMethod("testOneChildAssociationRef", new Class[] { ChildAssociationRef.class });

        Object proxy = getProxy(o, "ACL_PARENT.0.sys:base.Read");

        try
        {
            method.invoke(proxy, new Object[] { nodeService.getPrimaryParent(systemNodeRef) });
            fail(SHOULD_FAIL_DENIED);
        }
        catch (InvocationTargetException e)
        {
            verifyAccessDenied(e);
        }
    }

    public void testBasicAllowNode() throws Exception
    {
        runAs(ANDY);

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ),
                ANDY, AccessStatus.ALLOWED));

        Object o = new ClassWithMethods();
        Method method = o.getClass().getMethod("testOneNodeRef", new Class[] { NodeRef.class });

        Object proxy = getProxy(o, ACL_NODE_0_SYS_BASE_READ);

        method.invoke(proxy, new Object[] { rootNodeRef });
    }

    public void testBasicAllow() throws Exception
    {
        runAs(ANDY);

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ),
                ANDY, AccessStatus.ALLOWED));

        Object o = new ClassWithMethods();
        Method method = o.getClass().getMethod("testOneNodeRef", new Class[] { NodeRef.class });

        Object proxy = getProxy(o, "ACL_ALLOW");

        method.invoke(proxy, new Object[] { rootNodeRef });
    }

    public void testBasicAllowStore() throws Exception
    {
        runAs(ANDY);

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ),
                ANDY, AccessStatus.ALLOWED));

        Object o = new ClassWithMethods();
        Method method = o.getClass().getMethod("testOneStoreRef", new Class[] { StoreRef.class });

        Object proxy = getProxy(o, ACL_NODE_0_SYS_BASE_READ);

        method.invoke(proxy, new Object[] { rootNodeRef.getStoreRef() });
    }

    public void testBasicAllowChildAssocNode() throws Exception
    {
        runAs(ANDY);

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ),
                ANDY, AccessStatus.ALLOWED));

        Object o = new ClassWithMethods();
        Method method = o.getClass().getMethod("testOneChildAssociationRef", new Class[] { ChildAssociationRef.class });

        Object proxy = getProxy(o, ACL_NODE_0_SYS_BASE_READ);

        method.invoke(proxy, new Object[] { nodeService.getPrimaryParent(rootNodeRef) });
    }

    public void testBasicAllowParentAssocNode() throws Exception
    {
        runAs(ANDY);

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ),
                ANDY, AccessStatus.ALLOWED));

        Object o = new ClassWithMethods();
        Method method = o.getClass().getMethod("testOneChildAssociationRef", new Class[] { ChildAssociationRef.class });

        Object proxy = getProxy(o, "ACL_PARENT.0.sys:base.Read");

        method.invoke(proxy, new Object[] { nodeService.getPrimaryParent(systemNodeRef) });
    }

    public void testDenyParentAssocNode() throws Exception
    {
        runAs(ANDY);

        permissionService.setPermission(new SimplePermissionEntry(systemNodeRef, getPermission(PermissionService.READ),
                ANDY, AccessStatus.ALLOWED));

        Object o = new ClassWithMethods();
        Method method = o.getClass().getMethod("testOneChildAssociationRef", new Class[] { ChildAssociationRef.class });

        Object proxy = getProxy(o, "ACL_PARENT.0.sys:base.Read");

        try
        {
            method.invoke(proxy, new Object[] { nodeService.getPrimaryParent(systemNodeRef) });
            fail(SHOULD_FAIL_DENIED);
        }
        catch (InvocationTargetException e)
        {
            verifyAccessDenied(e);
        }
    }

    public void testAllowChildAssocNode() throws Exception
    {
        runAs(ANDY);

        permissionService.setPermission(new SimplePermissionEntry(systemNodeRef, getPermission(PermissionService.READ),
                ANDY, AccessStatus.ALLOWED));
        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef,
                getPermission(PermissionService.READ_CHILDREN), ANDY, AccessStatus.ALLOWED));

        Object o = new ClassWithMethods();
        Method method = o.getClass().getMethod("testOneChildAssociationRef", new Class[] { ChildAssociationRef.class });

        Object proxy = getProxy(o, ACL_NODE_0_SYS_BASE_READ);

        method.invoke(proxy, new Object[] { nodeService.getPrimaryParent(systemNodeRef) });

    }

    public void testMultiNodeMethodsArg0() throws Exception
    {
        runAs(ANDY);

        Object o = new ClassWithMethods();
        Method method = o.getClass().getMethod("testManyNodeRef",
                new Class[] { NodeRef.class, NodeRef.class, NodeRef.class, NodeRef.class });

        Object proxy = getProxy(o, ACL_NODE_0_SYS_BASE_READ);

        method.invoke(proxy, new Object[] { null, null, null, null });

        try
        {
            method.invoke(proxy, new Object[] { rootNodeRef, null, null, null });
            fail(SHOULD_FAIL_DENIED);
        }
        catch (InvocationTargetException e)
        {
            verifyAccessDenied(e);
        }

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ),
                ANDY, AccessStatus.ALLOWED));
        method.invoke(proxy, new Object[] { rootNodeRef, null, null, null });
    }

    public void testMultiNodeMethodsArg1() throws Exception
    {
        runAs(ANDY);

        Object o = new ClassWithMethods();
        Method method = o.getClass().getMethod("testManyNodeRef",
                new Class[] { NodeRef.class, NodeRef.class, NodeRef.class, NodeRef.class });

        Object proxy = getProxy(o, "ACL_NODE.1.sys:base.Read");

        method.invoke(proxy, new Object[] { null, null, null, null });

        try
        {
            method.invoke(proxy, new Object[] { null, rootNodeRef, null, null });
            fail(SHOULD_FAIL_DENIED);
        }
        catch (InvocationTargetException e)
        {
            verifyAccessDenied(e);
        }

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ),
                ANDY, AccessStatus.ALLOWED));
        method.invoke(proxy, new Object[] { null, rootNodeRef, null, null });
    }

    public void testMultiNodeMethodsArg2() throws Exception
    {
        runAs(ANDY);

        Object o = new ClassWithMethods();
        Method method = o.getClass().getMethod("testManyNodeRef",
                new Class[] { NodeRef.class, NodeRef.class, NodeRef.class, NodeRef.class });

        Object proxy = getProxy(o, "ACL_NODE.2.sys:base.Read");

        method.invoke(proxy, new Object[] { null, null, null, null });

        try
        {
            method.invoke(proxy, new Object[] { null, null, rootNodeRef, null });
            fail(SHOULD_FAIL_DENIED);
        }
        catch (InvocationTargetException e)
        {
            verifyAccessDenied(e);
        }

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ),
                ANDY, AccessStatus.ALLOWED));
        method.invoke(proxy, new Object[] { null, null, rootNodeRef, null });
    }

    public void testMultiNodeMethodsArg3() throws Exception
    {
        runAs(ANDY);

        Object o = new ClassWithMethods();
        Method method = o.getClass().getMethod("testManyNodeRef",
                new Class[] { NodeRef.class, NodeRef.class, NodeRef.class, NodeRef.class });

        Object proxy = getProxy(o, "ACL_NODE.3.sys:base.Read");

        method.invoke(proxy, new Object[] { null, null, null, null });

        try
        {
            method.invoke(proxy, new Object[] { null, null, null, rootNodeRef });
            fail(SHOULD_FAIL_DENIED);
        }
        catch (InvocationTargetException e)
        {
            verifyAccessDenied(e);
        }

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ),
                ANDY, AccessStatus.ALLOWED));
        method.invoke(proxy, new Object[] { null, null, null, rootNodeRef });
    }

    public void testMultiChildAssocRefMethodsArg0() throws Exception
    {
        runAs(ANDY);

        Object o = new ClassWithMethods();
        Method method = o.getClass().getMethod(
                "testManyChildAssociationRef",
                new Class[] { ChildAssociationRef.class, ChildAssociationRef.class, ChildAssociationRef.class,
                        ChildAssociationRef.class });

        Object proxy = getProxy(o, ACL_NODE_0_SYS_BASE_READ);

        method.invoke(proxy, new Object[] { null, null, null, null });

        try
        {
            method.invoke(proxy, new Object[] { nodeService.getPrimaryParent(rootNodeRef), null, null, null });
            fail(SHOULD_FAIL_DENIED);
        }
        catch (InvocationTargetException e)
        {
            verifyAccessDenied(e);
        }

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ),
                ANDY, AccessStatus.ALLOWED));
        method.invoke(proxy, new Object[] { nodeService.getPrimaryParent(rootNodeRef), null, null, null });
    }

    public void testMultiChildAssocRefMethodsArg1() throws Exception
    {
        runAs(ANDY);

        Object o = new ClassWithMethods();
        Method method = o.getClass().getMethod(
                "testManyChildAssociationRef",
                new Class[] { ChildAssociationRef.class, ChildAssociationRef.class, ChildAssociationRef.class,
                        ChildAssociationRef.class });

        Object proxy = getProxy(o, "ACL_NODE.1.sys:base.Read");

        method.invoke(proxy, new Object[] { null, null, null, null });

        try
        {
            method.invoke(proxy, new Object[] { null, nodeService.getPrimaryParent(rootNodeRef), null, null });
            fail(SHOULD_FAIL_DENIED);
        }
        catch (InvocationTargetException e)
        {
            verifyAccessDenied(e);
        }

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ),
                ANDY, AccessStatus.ALLOWED));
        method.invoke(proxy, new Object[] { null, nodeService.getPrimaryParent(rootNodeRef), null, null });
    }

    public void testMultiChildAssocRefMethodsArg2() throws Exception
    {
        runAs(ANDY);

        Object o = new ClassWithMethods();
        Method method = o.getClass().getMethod(
                "testManyChildAssociationRef",
                new Class[] { ChildAssociationRef.class, ChildAssociationRef.class, ChildAssociationRef.class,
                        ChildAssociationRef.class });

        Object proxy = getProxy(o, "ACL_NODE.2.sys:base.Read");

        method.invoke(proxy, new Object[] { null, null, null, null });

        try
        {
            method.invoke(proxy, new Object[] { null, null, nodeService.getPrimaryParent(rootNodeRef), null });
            fail(SHOULD_FAIL_DENIED);
        }
        catch (InvocationTargetException e)
        {
            verifyAccessDenied(e);
        }

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ),
                ANDY, AccessStatus.ALLOWED));
        method.invoke(proxy, new Object[] { null, null, nodeService.getPrimaryParent(rootNodeRef), null });
    }

    public void testMultiChildAssocRefMethodsArg3() throws Exception
    {
        runAs(ANDY);

        Object o = new ClassWithMethods();
        Method method = o.getClass().getMethod(
                "testManyChildAssociationRef",
                new Class[] { ChildAssociationRef.class, ChildAssociationRef.class, ChildAssociationRef.class,
                        ChildAssociationRef.class });

        Object proxy = getProxy(o, "ACL_NODE.3.sys:base.Read");

        method.invoke(proxy, new Object[] { null, null, null, null });

        try
        {
            method.invoke(proxy, new Object[] { null, null, null, nodeService.getPrimaryParent(rootNodeRef) });
            fail(SHOULD_FAIL_DENIED);
        }
        catch (InvocationTargetException e)
        {
            verifyAccessDenied(e);
        }

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ),
                ANDY, AccessStatus.ALLOWED));
        method.invoke(proxy, new Object[] { null, null, null, nodeService.getPrimaryParent(rootNodeRef) });
    }

    public void testMethodACL() throws Exception
    {
        runAs(ANDY);

        Object o = new ClassWithMethods();
        Method method = o.getClass().getMethod("testMethod", new Class[] {});

        AdvisorAdapterRegistry advisorAdapterRegistry = GlobalAdvisorAdapterRegistry.getInstance();

        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.addAdvisor(advisorAdapterRegistry.wrap(new Interceptor("ACL_METHOD.andy", "ACL_METHOD.BANANA")));
        proxyFactory.setTargetSource(new SingletonTargetSource(o));
        Object proxy = proxyFactory.getProxy();

        method.invoke(proxy, new Object[] {});
    }

    public void testMethodACL2() throws Exception
    {
        runAs(ANDY);

        Object o = new ClassWithMethods();
        Method method = o.getClass().getMethod("testMethod", new Class[] {});

        AdvisorAdapterRegistry advisorAdapterRegistry = GlobalAdvisorAdapterRegistry.getInstance();

        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.addAdvisor(advisorAdapterRegistry.wrap(new Interceptor("ACL_METHOD.BANANA", "ACL_METHOD."
                + PermissionService.ALL_AUTHORITIES)));
        proxyFactory.setTargetSource(new SingletonTargetSource(o));
        Object proxy = proxyFactory.getProxy();

        method.invoke(proxy, new Object[] {});
    }

    public void testMethodACL3() throws Exception
    {
        runAs(ANDY);

        Object o = new ClassWithMethods();
        Method method = o.getClass().getMethod("testMethod", new Class[] {});

        AdvisorAdapterRegistry advisorAdapterRegistry = GlobalAdvisorAdapterRegistry.getInstance();

        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.addAdvisor(advisorAdapterRegistry.wrap(new Interceptor("ACL_METHOD.andy", "ACL_METHOD."
                + PermissionService.ALL_AUTHORITIES)));
        proxyFactory.setTargetSource(new SingletonTargetSource(o));
        Object proxy = proxyFactory.getProxy();

        method.invoke(proxy, new Object[] {});

    }

    public void testMethodACL4() throws Exception
    {
        runAs(ANDY);

        Object o = new ClassWithMethods();
        Method method = o.getClass().getMethod("testMethod", new Class[] {});

        AdvisorAdapterRegistry advisorAdapterRegistry = GlobalAdvisorAdapterRegistry.getInstance();

        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.addAdvisor(advisorAdapterRegistry.wrap(new Interceptor("ACL_METHOD.woof", "ACL_METHOD.BOO")));
        proxyFactory.setTargetSource(new SingletonTargetSource(o));
        Object proxy = proxyFactory.getProxy();

        try
        {
            method.invoke(proxy, new Object[] {});
        }
        catch (InvocationTargetException e)
        {
            verifyAccessDenied(e);
        }
    }

    public void testBasicAllowNodeCollection() throws Exception
    {
        runAs(ANDY);
        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ), ANDY, AccessStatus.ALLOWED));

        Object o = new ClassWithMethods();
        Method method = o.getClass().getMethod(TEST_LIST_OF_NODE_REFS, List.class);
        Object proxy = getProxy(o, ACL_NODE_0_SYS_BASE_READ);

        method.invoke(proxy, singletonList(rootNodeRef));
    }

    public void testBasicDenyNodeCollection() throws Exception
    {
        runAs(ANDY);

        Object o = new ClassWithMethods();
        Method method = o.getClass().getMethod(TEST_LIST_OF_NODE_REFS, List.class);
        Object proxy = getProxy(o, ACL_NODE_0_SYS_BASE_READ);

        try
        {
            method.invoke(proxy, singletonList(rootNodeRef));
            fail(SHOULD_FAIL_DENIED);
        } catch (InvocationTargetException e)
        {
            verifyAccessDenied(e);
        }
    }

    public void testAllowNullCollection() throws Exception
    {
        runAs(ANDY);

        Object o = new ClassWithMethods();
        Method method = o.getClass().getMethod(TEST_LIST_OF_NODE_REFS, List.class);
        Object proxy = getProxy(o, ACL_NODE_0_SYS_BASE_READ);

        method.invoke(proxy, singletonList(null));
    }

    public void testAllowNodeCollection() throws Exception
    {
        runAs(ANDY);
        permissionService.setPermission(new SimplePermissionEntry(systemNodeRef, getPermission(PermissionService.READ), ANDY, AccessStatus.ALLOWED));

        Object o = new ClassWithMethods();
        Method method = o.getClass().getMethod(TEST_LIST_OF_NODE_REFS, List.class);
        Object proxy = getProxy(o, ACL_NODE_0_SYS_BASE_READ);

        method.invoke(proxy, Arrays.asList(systemNodeRef, systemNodeRef, systemNodeRef));
    }

    public void testDenyNodeCollectionWhenOneElementShouldBeDenied() throws Exception
    {
        runAs(ANDY);
        permissionService.setPermission(new SimplePermissionEntry(systemNodeRef, getPermission(PermissionService.READ), ANDY, AccessStatus.ALLOWED));

        Object o = new ClassWithMethods();
        Method method = o.getClass().getMethod(TEST_LIST_OF_NODE_REFS, List.class);
        Object proxy = getProxy(o, ACL_NODE_0_SYS_BASE_READ);

        try
        {
            method.invoke(proxy, Arrays.asList(systemNodeRef, rootNodeRef, systemNodeRef));
            fail(SHOULD_FAIL_DENIED);
        } catch (InvocationTargetException e)
        {
            verifyAccessDenied(e);
        }
    }

    public void testSimpleAbstain() throws Exception
    {
        runAs(ANDY);
        permissionService.setPermission(new SimplePermissionEntry(abstainedNode, getPermission(PermissionService.READ), ANDY, AccessStatus.ALLOWED));

        Object o = new ClassWithMethods();
        Method method = o.getClass().getMethod(TEST_LIST_OF_NODE_REFS, List.class);
        Object proxy = getProxy(o, ACL_NODE_0_SYS_BASE_READ);

        try
        {
            method.invoke(proxy, Collections.singletonList(abstainedNode));
            fail(SHOULD_FAIL_ABSTAINED);
        } catch (InvocationTargetException e)
        {
            verifyAccessAbstain(e);
        }
    }

    public void testAbstainHasAPriorityOverDeny() throws Exception
    {
        runAs(ANDY);
        permissionService.setPermission(new SimplePermissionEntry(abstainedNode, getPermission(PermissionService.READ), ANDY, AccessStatus.DENIED));

        Object o = new ClassWithMethods();
        Method method = o.getClass().getMethod(TEST_LIST_OF_NODE_REFS, List.class);
        Object proxy = getProxy(o, ACL_NODE_0_SYS_BASE_READ);

        try
        {
            method.invoke(proxy, Collections.singletonList(abstainedNode));
            fail(SHOULD_FAIL_ABSTAINED);
        } catch (InvocationTargetException e)
        {
            verifyAccessAbstain(e);
        }
    }

    public void testSimpleAbstainList() throws Exception
    {
        runAs(ANDY);
        permissionService.setPermission(new SimplePermissionEntry(abstainedNode, getPermission(PermissionService.READ), ANDY, AccessStatus.ALLOWED));

        Object o = new ClassWithMethods();
        Method method = o.getClass().getMethod(TEST_LIST_OF_NODE_REFS, List.class);
        Object proxy = getProxy(o, ACL_NODE_0_SYS_BASE_READ);

        try
        {
            method.invoke(proxy, Collections.singletonList(abstainedNode));
            fail(SHOULD_FAIL_ABSTAINED);
        } catch (InvocationTargetException e)
        {
            verifyAccessAbstain(e);
        }
    }

    public void testAbstainNodeCollectionWhenOneElementShouldBeAbstained() throws Exception
    {
        runAs(ANDY);
        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ), ANDY, AccessStatus.ALLOWED));
        permissionService.setPermission(new SimplePermissionEntry(abstainedNode, getPermission(PermissionService.READ), ANDY, AccessStatus.ALLOWED));

        Object o = new ClassWithMethods();
        Method method = o.getClass().getMethod(TEST_LIST_OF_NODE_REFS, List.class);
        Object proxy = getProxy(o, ACL_NODE_0_SYS_BASE_READ);

        try
        {
            method.invoke(proxy, Arrays.asList(rootNodeRef, abstainedNode, rootNodeRef));
            fail(SHOULD_FAIL_ABSTAINED);
        } catch (InvocationTargetException e)
        {
            verifyAccessAbstain(e);
        }
    }

    public void testDenyNodeCollectionWhenOneElementShouldBeDeniedAndThereAreAlsoAbstained() throws Exception
    {
        runAs(ANDY);
        permissionService.setPermission(new SimplePermissionEntry(abstainedNode, getPermission(PermissionService.READ), ANDY, AccessStatus.ALLOWED));

        Object o = new ClassWithMethods();
        Method method = o.getClass().getMethod(TEST_LIST_OF_NODE_REFS, List.class);
        Object proxy = getProxy(o, ACL_NODE_0_SYS_BASE_READ);

        try
        {
            method.invoke(proxy, Arrays.asList(abstainedNode, systemNodeRef, abstainedNode));
            fail(SHOULD_FAIL_DENIED);
        } catch (InvocationTargetException e)
        {
            verifyAccessDenied(e);
        }
    }


    private void verifyAccessAbstain(InvocationTargetException e)
    {
        assertEquals(ABSTAIN, e.getCause().getMessage());
    }

    private void verifyAccessDenied(InvocationTargetException e)
    {
        String causeMessage = e.getCause().getMessage();
        assertEquals(DENIED, causeMessage.substring(causeMessage.length() - 13));
    }

    public static class ClassWithMethods
    {
        public void testMethod()
        {

        }

        public void testOneStoreRef(StoreRef storeRef)
        {

        }

        public void testOneNodeRef(NodeRef nodeRef)
        {

        }

        public void testManyNodeRef(NodeRef nodeRef1, NodeRef nodeRef2, NodeRef nodeRef3, NodeRef nodeRef4)
        {

        }

        public void testOneChildAssociationRef(ChildAssociationRef car)
        {

        }

        public void testManyChildAssociationRef(ChildAssociationRef car1, ChildAssociationRef car2,
                ChildAssociationRef car3, ChildAssociationRef car4)
        {

        }

        public void testListOfNodeRefs(List listOfNodeRefs)
        {
        }

    }

    public class Interceptor implements MethodInterceptor
    {
        ConfigAttributeDefinition cad = new ConfigAttributeDefinition();

        Interceptor(final String config1, final String config2)
        {
            cad.addConfigAttribute(new ConfigAttribute()
            {

                /**
                 * Comment for <code>serialVersionUID</code>
                 */
                private static final long serialVersionUID = 1L;

                public String getAttribute()
                {
                    return config1;
                }

            });
            cad.addConfigAttribute(new ConfigAttribute()
            {

                /**
                 * Comment for <code>serialVersionUID</code>
                 */
                private static final long serialVersionUID = 1L;

                public String getAttribute()
                {
                    return config2;
                }

            });
        }

        Interceptor(final String config)
        {
            cad.addConfigAttribute(new ConfigAttribute()
            {

                /**
                 * Comment for <code>serialVersionUID</code>
                 */
                private static final long serialVersionUID = 1L;

                public String getAttribute()
                {
                    return config;
                }

            });
        }

        public Object invoke(MethodInvocation invocation) throws Throwable
        {
            ACLEntryVoter voter = new ACLEntryVoter();
            voter.setNamespacePrefixResolver(namespacePrefixResolver);
            voter.setPermissionService(permissionService);
            voter.setNodeService(nodeService);
            voter.setAuthenticationService(authenticationService);
            voter.setAuthorityService(authorityService);

            Set<String> abstainFor = new HashSet<>(2);
            abstainFor.add("{http://www.alfresco.org/model/content/1.0}emailed");
            abstainFor.add("{http://www.alfresco.org/model/content/1.0}failedThumbnail");
            voter.setAbstainFor(abstainFor);
            voter.afterPropertiesSet();

            int voteResult = voter.vote(null, invocation, cad);
            if (voteResult == AccessDecisionVoter.ACCESS_DENIED)
            {
                throw new ACLEntryVoterException(DENIED);
            }
            if (voteResult == AccessDecisionVoter.ACCESS_ABSTAIN)
            {
                throw new RuntimeException(ABSTAIN);
            }
            return invocation.proceed();
        }
    }

    private Object getProxy(Object o, String s)
    {
        AdvisorAdapterRegistry advisorAdapterRegistry = GlobalAdvisorAdapterRegistry.getInstance();
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.addAdvisor(advisorAdapterRegistry.wrap(new Interceptor(s)));
        proxyFactory.setTargetSource(new SingletonTargetSource(o));
        return proxyFactory.getProxy();
    }

}
