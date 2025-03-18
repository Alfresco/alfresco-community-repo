/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.acegisecurity.ConfigAttribute;
import net.sf.acegisecurity.ConfigAttributeDefinition;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.junit.experimental.categories.Category;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.framework.adapter.AdvisorAdapterRegistry;
import org.springframework.aop.framework.adapter.GlobalAdvisorAdapterRegistry;
import org.springframework.aop.target.SingletonTargetSource;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.search.results.ChildAssocRefResultSet;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.PermissionCheckCollection.PermissionCheckCollectionMixin;
import org.alfresco.repo.security.permissions.impl.AbstractPermissionTest;
import org.alfresco.repo.security.permissions.impl.SimplePermissionEntry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.Pair;

@Category(OwnJVMTestsCategory.class)
public class ACLEntryAfterInvocationTest extends AbstractPermissionTest
{

    public ACLEntryAfterInvocationTest()
    {
        super();
    }

    public void testBasicAllowNullNode() throws Exception
    {
        runAs("andy");

        Object o = new ClassWithMethods();
        Method method = o.getClass().getMethod("echoNodeRef", new Class[]{NodeRef.class});

        AdvisorAdapterRegistry advisorAdapterRegistry = GlobalAdvisorAdapterRegistry.getInstance();

        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.addAdvisor(advisorAdapterRegistry.wrap(new Interceptor("AFTER_ACL_NODE.sys:base.Read")));
        proxyFactory.setTargetSource(new SingletonTargetSource(o));
        Object proxy = proxyFactory.getProxy();

        Object answer = method.invoke(proxy, new Object[]{null});
        assertNull(answer);
    }

    public void testBasicAllowNullStore() throws Exception
    {
        runAs("andy");

        Object o = new ClassWithMethods();
        Method method = o.getClass().getMethod("echoStoreRef", new Class[]{StoreRef.class});

        AdvisorAdapterRegistry advisorAdapterRegistry = GlobalAdvisorAdapterRegistry.getInstance();

        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.addAdvisor(advisorAdapterRegistry.wrap(new Interceptor("AFTER_ACL_NODE.sys:base.Read")));
        proxyFactory.setTargetSource(new SingletonTargetSource(o));
        Object proxy = proxyFactory.getProxy();

        Object answer = method.invoke(proxy, new Object[]{null});
        assertNull(answer);
    }

    public void testBasicAllowUnrecognisedObject() throws Exception
    {
        runAs("andy");

        Object o = new ClassWithMethods();
        Method method = o.getClass().getMethod("echoObject", new Class[]{Object.class});

        AdvisorAdapterRegistry advisorAdapterRegistry = GlobalAdvisorAdapterRegistry.getInstance();

        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.addAdvisor(advisorAdapterRegistry.wrap(new Interceptor("AFTER_ACL_NODE.sys:base.Read")));
        proxyFactory.setTargetSource(new SingletonTargetSource(o));
        Object proxy = proxyFactory.getProxy();

        Object answer = method.invoke(proxy, new Object[]{"noodle"});
        assertNotNull(answer);
    }

    public void testBasicDenyInvalidNodeRef() throws Exception
    {
        runAs("andy");

        Object o = new ClassWithMethods();
        Method method = o.getClass().getMethod("echoNodeRef", new Class[]{NodeRef.class});

        AdvisorAdapterRegistry advisorAdapterRegistry = GlobalAdvisorAdapterRegistry.getInstance();

        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.addAdvisor(advisorAdapterRegistry.wrap(new Interceptor("AFTER_ACL_NODE.sys:base.Read")));
        proxyFactory.setTargetSource(new SingletonTargetSource(o));
        Object proxy = proxyFactory.getProxy();

        permissionService.setPermission(
                new SimplePermissionEntry(
                        rootNodeRef,
                        getPermission(PermissionService.READ),
                        "andy",
                        AccessStatus.ALLOWED));

        Object answer = method.invoke(proxy, new Object[]{rootNodeRef});
        assertEquals("Value passed out must be valid", rootNodeRef, answer);

        NodeRef invalidNodeRef = new NodeRef("workspace://SpacesStore/noodle");
        answer = method.invoke(proxy, new Object[]{invalidNodeRef});
        method.invoke(proxy, new Object[]{invalidNodeRef});
        assertEquals("Value passed out must be equal", invalidNodeRef, answer);
    }

    @SuppressWarnings("unchecked")
    public void testBasicFilterInvalidNodeRefs() throws Exception
    {
        runAs("andy");

        Object o = new ClassWithMethods();
        Method method = o.getClass().getMethod("echoCollection", new Class[]{Collection.class});

        AdvisorAdapterRegistry advisorAdapterRegistry = GlobalAdvisorAdapterRegistry.getInstance();

        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.addAdvisor(advisorAdapterRegistry.wrap(new Interceptor("AFTER_ACL_NODE.sys:base.Read")));
        proxyFactory.setTargetSource(new SingletonTargetSource(o));
        Object proxy = proxyFactory.getProxy();

        permissionService.setPermission(
                new SimplePermissionEntry(
                        rootNodeRef,
                        getPermission(PermissionService.READ),
                        "andy",
                        AccessStatus.ALLOWED));

        List<Object> answer = (List<Object>) method.invoke(proxy, new Object[]{Collections.singletonList(rootNodeRef)});
        assertEquals("Collection must be intact", 1, answer.size());

        NodeRef invalidNodeRef = new NodeRef("workspace://SpacesStore/noodle");
        answer = (List<Object>) method.invoke(proxy, new Object[]{Collections.singletonList(invalidNodeRef)});
        assertEquals("Invalid NodeRef should not have been filtered out", 1, answer.size());
    }

    public void testBasicDenyStore() throws Exception
    {
        runAs("andy");

        Object o = new ClassWithMethods();
        Method method = o.getClass().getMethod("echoStoreRef", new Class[]{StoreRef.class});

        AdvisorAdapterRegistry advisorAdapterRegistry = GlobalAdvisorAdapterRegistry.getInstance();

        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.addAdvisor(advisorAdapterRegistry.wrap(new Interceptor("AFTER_ACL_NODE.sys:base.Read")));
        proxyFactory.setTargetSource(new SingletonTargetSource(o));
        Object proxy = proxyFactory.getProxy();

        try
        {
            Object answer = method.invoke(proxy, new Object[]{rootNodeRef.getStoreRef()});
            assertNotNull(answer);
        }
        catch (InvocationTargetException e)
        {

        }
    }

    public void testBasicDenyNode() throws Exception
    {
        runAs("andy");

        Object o = new ClassWithMethods();
        Method method = o.getClass().getMethod("echoNodeRef", new Class[]{NodeRef.class});

        AdvisorAdapterRegistry advisorAdapterRegistry = GlobalAdvisorAdapterRegistry.getInstance();

        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.addAdvisor(advisorAdapterRegistry.wrap(new Interceptor("AFTER_ACL_NODE.sys:base.Read")));
        proxyFactory.setTargetSource(new SingletonTargetSource(o));
        Object proxy = proxyFactory.getProxy();

        try
        {
            Object answer = method.invoke(proxy, new Object[]{rootNodeRef});
            assertNotNull(answer);
        }
        catch (InvocationTargetException e)
        {

        }

    }

    public void testBasicAllowNode() throws Exception
    {
        runAs("andy");

        Object o = new ClassWithMethods();
        Method method = o.getClass().getMethod("echoNodeRef", new Class[]{NodeRef.class});

        AdvisorAdapterRegistry advisorAdapterRegistry = GlobalAdvisorAdapterRegistry.getInstance();

        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.addAdvisor(advisorAdapterRegistry.wrap(new Interceptor("AFTER_ACL_NODE.sys:base.Read")));
        proxyFactory.setTargetSource(new SingletonTargetSource(o));
        Object proxy = proxyFactory.getProxy();

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ), "andy", AccessStatus.ALLOWED));

        Object answer = method.invoke(proxy, new Object[]{rootNodeRef});
        assertEquals(answer, rootNodeRef);

    }

    public void testBasicAllowNodePair() throws Exception
    {
        runAs("andy");

        Object o = new ClassWithMethods();
        Method method = o.getClass().getMethod("echoNodePair", new Class[]{NodeRef.class});

        AdvisorAdapterRegistry advisorAdapterRegistry = GlobalAdvisorAdapterRegistry.getInstance();

        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.addAdvisor(advisorAdapterRegistry.wrap(new Interceptor("AFTER_ACL_NODE.sys:base.Read")));
        proxyFactory.setTargetSource(new SingletonTargetSource(o));
        Object proxy = proxyFactory.getProxy();

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ), "andy", AccessStatus.ALLOWED));

        Pair<Long, NodeRef> rootNodePair = new Pair<Long, NodeRef>(Long.valueOf(1), rootNodeRef);
        Object answer = method.invoke(proxy, new Object[]{rootNodeRef});
        assertEquals(rootNodePair, answer);
    }

    public void testBasicAllowStore() throws Exception
    {
        runAs("andy");

        Object o = new ClassWithMethods();
        Method method = o.getClass().getMethod("echoStoreRef", new Class[]{StoreRef.class});

        AdvisorAdapterRegistry advisorAdapterRegistry = GlobalAdvisorAdapterRegistry.getInstance();

        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.addAdvisor(advisorAdapterRegistry.wrap(new Interceptor("AFTER_ACL_NODE.sys:base.Read")));
        proxyFactory.setTargetSource(new SingletonTargetSource(o));
        Object proxy = proxyFactory.getProxy();

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ), "andy", AccessStatus.ALLOWED));

        Object answer = method.invoke(proxy, new Object[]{rootNodeRef.getStoreRef()});
        assertEquals(answer, rootNodeRef.getStoreRef());

    }

    public void testBasicAllowNodeParent() throws Exception
    {
        runAs("andy");

        Object o = new ClassWithMethods();
        Method method = o.getClass().getMethod("echoNodeRef", new Class[]{NodeRef.class});

        AdvisorAdapterRegistry advisorAdapterRegistry = GlobalAdvisorAdapterRegistry.getInstance();

        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.addAdvisor(advisorAdapterRegistry.wrap(new Interceptor("AFTER_ACL_PARENT.sys:base.Read")));
        proxyFactory.setTargetSource(new SingletonTargetSource(o));
        Object proxy = proxyFactory.getProxy();

        Object answer = method.invoke(proxy, new Object[]{rootNodeRef});
        assertEquals(answer, rootNodeRef);

        try
        {
            answer = method.invoke(proxy, new Object[]{systemNodeRef});
            assertNotNull(answer);
        }
        catch (InvocationTargetException e)
        {

        }

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ), "andy", AccessStatus.ALLOWED));

        answer = method.invoke(proxy, new Object[]{systemNodeRef});
        assertEquals(answer, systemNodeRef);
    }

    public void testBasicAllowNullChildAssociationRef1() throws Exception
    {
        runAs("andy");

        Object o = new ClassWithMethods();
        Method method = o.getClass().getMethod("echoChildAssocRef", new Class[]{ChildAssociationRef.class});

        AdvisorAdapterRegistry advisorAdapterRegistry = GlobalAdvisorAdapterRegistry.getInstance();

        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.addAdvisor(advisorAdapterRegistry.wrap(new Interceptor("AFTER_ACL_NODE.sys:base.Read")));
        proxyFactory.setTargetSource(new SingletonTargetSource(o));
        Object proxy = proxyFactory.getProxy();

        Object answer = method.invoke(proxy, new Object[]{null});
        assertNull(answer);
    }

    public void testBasicAllowNullChildAssociationRef2() throws Exception
    {
        runAs("andy");

        Object o = new ClassWithMethods();
        Method method = o.getClass().getMethod("echoChildAssocRef", new Class[]{ChildAssociationRef.class});

        AdvisorAdapterRegistry advisorAdapterRegistry = GlobalAdvisorAdapterRegistry.getInstance();

        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.addAdvisor(advisorAdapterRegistry.wrap(new Interceptor("AFTER_ACL_PARENT.sys:base.Read")));
        proxyFactory.setTargetSource(new SingletonTargetSource(o));
        Object proxy = proxyFactory.getProxy();

        Object answer = method.invoke(proxy, new Object[]{null});
        assertNull(answer);
    }

    public void testBasicDenyChildAssocRef1() throws Exception
    {
        runAs("andy");

        Object o = new ClassWithMethods();
        Method method = o.getClass().getMethod("echoChildAssocRef", new Class[]{ChildAssociationRef.class});

        AdvisorAdapterRegistry advisorAdapterRegistry = GlobalAdvisorAdapterRegistry.getInstance();

        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.addAdvisor(advisorAdapterRegistry.wrap(new Interceptor("AFTER_ACL_NODE.sys:base.Read")));
        proxyFactory.setTargetSource(new SingletonTargetSource(o));
        Object proxy = proxyFactory.getProxy();

        try
        {
            Object answer = method.invoke(proxy, new Object[]{nodeService.getPrimaryParent(rootNodeRef)});
            assertNotNull(answer);
        }
        catch (InvocationTargetException e)
        {

        }

        try
        {
            Object answer = method.invoke(proxy, new Object[]{nodeService.getPrimaryParent(systemNodeRef)});
            assertNotNull(answer);
        }
        catch (InvocationTargetException e)
        {

        }

    }

    public void testBasicDenyChildAssocRef2() throws Exception
    {
        runAs("andy");

        Object o = new ClassWithMethods();
        Method method = o.getClass().getMethod("echoChildAssocRef", new Class[]{ChildAssociationRef.class});

        AdvisorAdapterRegistry advisorAdapterRegistry = GlobalAdvisorAdapterRegistry.getInstance();

        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.addAdvisor(advisorAdapterRegistry.wrap(new Interceptor("AFTER_ACL_PARENT.sys:base.Read")));
        proxyFactory.setTargetSource(new SingletonTargetSource(o));
        Object proxy = proxyFactory.getProxy();

        Object answer = method.invoke(proxy, new Object[]{nodeService.getPrimaryParent(rootNodeRef)});
        assertNotNull(answer);

        try
        {
            answer = method.invoke(proxy, new Object[]{nodeService.getPrimaryParent(systemNodeRef)});
            assertNotNull(answer);
        }
        catch (InvocationTargetException e)
        {

        }

    }

    public void testBasicAllowChildAssociationRef1() throws Exception
    {
        runAs("andy");

        Object o = new ClassWithMethods();
        Method method = o.getClass().getMethod("echoChildAssocRef", new Class[]{ChildAssociationRef.class});

        AdvisorAdapterRegistry advisorAdapterRegistry = GlobalAdvisorAdapterRegistry.getInstance();

        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.addAdvisor(advisorAdapterRegistry.wrap(new Interceptor("AFTER_ACL_NODE.sys:base.Read")));
        proxyFactory.setTargetSource(new SingletonTargetSource(o));
        Object proxy = proxyFactory.getProxy();

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ), "andy", AccessStatus.ALLOWED));

        Object answer = method.invoke(proxy, new Object[]{nodeService.getPrimaryParent(rootNodeRef)});
        assertEquals(answer, nodeService.getPrimaryParent(rootNodeRef));

        answer = method.invoke(proxy, new Object[]{nodeService.getPrimaryParent(systemNodeRef)});
        assertEquals(answer, nodeService.getPrimaryParent(systemNodeRef));

    }

    public void testBasicAllowChildAssociationRef2() throws Exception
    {
        runAs("andy");

        Object o = new ClassWithMethods();
        Method method = o.getClass().getMethod("echoChildAssocRef", new Class[]{ChildAssociationRef.class});

        AdvisorAdapterRegistry advisorAdapterRegistry = GlobalAdvisorAdapterRegistry.getInstance();

        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.addAdvisor(advisorAdapterRegistry.wrap(new Interceptor("AFTER_ACL_PARENT.sys:base.Read")));
        proxyFactory.setTargetSource(new SingletonTargetSource(o));
        Object proxy = proxyFactory.getProxy();

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ), "andy", AccessStatus.ALLOWED));

        Object answer = method.invoke(proxy, new Object[]{nodeService.getPrimaryParent(rootNodeRef)});
        assertEquals(answer, nodeService.getPrimaryParent(rootNodeRef));

        answer = method.invoke(proxy, new Object[]{nodeService.getPrimaryParent(systemNodeRef)});
        assertEquals(answer, nodeService.getPrimaryParent(systemNodeRef));
    }

    public void testBasicAllowNullResultSet() throws Exception
    {
        runAs("andy");

        Object o = new ClassWithMethods();
        Method methodResultSet = o.getClass().getMethod("echoResultSet", new Class[]{ResultSet.class});
        Method methodCollection = o.getClass().getMethod("echoCollection", new Class[]{Collection.class});
        Method methodArray = o.getClass().getMethod("echoArray", new Class[]{Object[].class});

        AdvisorAdapterRegistry advisorAdapterRegistry = GlobalAdvisorAdapterRegistry.getInstance();

        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.addAdvisor(advisorAdapterRegistry.wrap(new Interceptor("AFTER_ACL_NODE.sys:base.Read")));
        proxyFactory.setTargetSource(new SingletonTargetSource(o));
        Object proxy = proxyFactory.getProxy();

        List<NodeRef> nodeRefList = new ArrayList<NodeRef>();
        NodeRef[] nodeRefArray = new NodeRef[0];

        Set<NodeRef> nodeRefSet = new HashSet<NodeRef>();

        List<ChildAssociationRef> carList = new ArrayList<ChildAssociationRef>();

        ChildAssociationRef[] carArray = new ChildAssociationRef[0];

        Set<ChildAssociationRef> carSet = new HashSet<ChildAssociationRef>();

        ChildAssocRefResultSet rsIn = new ChildAssocRefResultSet(nodeService, nodeRefList, false);

        assertEquals(0, rsIn.length());
        ResultSet answerResultSet = (ResultSet) methodResultSet.invoke(proxy, new Object[]{rsIn});
        assertEquals(0, answerResultSet.length());
        Collection answerCollection = (Collection) methodCollection.invoke(proxy, new Object[]{nodeRefList});
        assertEquals(0, answerCollection.size());
        answerCollection = (Collection) methodCollection.invoke(proxy, new Object[]{nodeRefSet});
        assertEquals(0, answerCollection.size());
        answerCollection = (Collection) methodCollection.invoke(proxy, new Object[]{carList});
        assertEquals(0, answerCollection.size());
        answerCollection = (Collection) methodCollection.invoke(proxy, new Object[]{carSet});
        assertEquals(0, answerCollection.size());
        Object[] answerArray = (Object[]) methodArray.invoke(proxy, new Object[]{nodeRefArray});
        assertEquals(0, answerArray.length);
        answerArray = (Object[]) methodArray.invoke(proxy, new Object[]{carArray});
        assertEquals(0, answerArray.length);

        assertEquals(0, rsIn.length());
        answerResultSet = (ResultSet) methodResultSet.invoke(proxy, new Object[]{null});
        assertNull(answerResultSet);
        answerCollection = (Collection) methodCollection.invoke(proxy, new Object[]{null});
        assertNull(answerCollection);
        answerArray = (Object[]) methodArray.invoke(proxy, new Object[]{null});
        assertNull(answerArray);
    }

    public void testResultSetFilterAll() throws Exception
    {
        runAs(AuthenticationUtil.getAdminUserName());

        NodeRef n1 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN,
                QName.createQName("{namespace}one"), ContentModel.TYPE_FOLDER).getChildRef();

        runAs("andy");

        Object o = new ClassWithMethods();
        Method methodResultSet = o.getClass().getMethod("echoResultSet", new Class[]{ResultSet.class});
        Method methodCollection = o.getClass().getMethod("echoCollection", new Class[]{Collection.class});
        Method methodArray = o.getClass().getMethod("echoArray", new Class[]{Object[].class});

        AdvisorAdapterRegistry advisorAdapterRegistry = GlobalAdvisorAdapterRegistry.getInstance();

        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.addAdvisor(advisorAdapterRegistry.wrap(new Interceptor("AFTER_ACL_NODE.sys:base.Read")));
        proxyFactory.setTargetSource(new SingletonTargetSource(o));
        Object proxy = proxyFactory.getProxy();

        List<NodeRef> nodeRefList = new ArrayList<NodeRef>();
        nodeRefList.add(rootNodeRef);
        nodeRefList.add(systemNodeRef);
        nodeRefList.add(n1);
        nodeRefList.add(n1);

        NodeRef[] nodeRefArray = nodeRefList.toArray(new NodeRef[]{});

        Set<NodeRef> nodeRefSet = new HashSet<NodeRef>();
        nodeRefSet.addAll(nodeRefList);

        List<ChildAssociationRef> carList = new ArrayList<ChildAssociationRef>();
        carList.add(nodeService.getPrimaryParent(rootNodeRef));
        carList.add(nodeService.getPrimaryParent(systemNodeRef));
        carList.add(nodeService.getPrimaryParent(n1));
        carList.add(nodeService.getPrimaryParent(n1));

        ChildAssociationRef[] carArray = carList.toArray(new ChildAssociationRef[]{});

        Set<ChildAssociationRef> carSet = new HashSet<ChildAssociationRef>();
        carSet.addAll(carList);

        ChildAssocRefResultSet rsIn = new ChildAssocRefResultSet(nodeService, nodeRefList, false);

        assertEquals(4, rsIn.length());
        ResultSet answerResultSet = (ResultSet) methodResultSet.invoke(proxy, new Object[]{rsIn});
        assertEquals(0, answerResultSet.length());
        Collection answerCollection = (Collection) methodCollection.invoke(proxy, new Object[]{nodeRefList});
        assertEquals(0, answerCollection.size());
        answerCollection = (Collection) methodCollection.invoke(proxy, new Object[]{nodeRefSet});
        assertEquals(0, answerCollection.size());
        answerCollection = (Collection) methodCollection.invoke(proxy, new Object[]{carList});
        assertEquals(0, answerCollection.size());
        answerCollection = (Collection) methodCollection.invoke(proxy, new Object[]{carSet});
        assertEquals(0, answerCollection.size());
        Object[] answerArray = (Object[]) methodArray.invoke(proxy, new Object[]{nodeRefArray});
        assertEquals(0, answerArray.length);
        answerArray = (Object[]) methodArray.invoke(proxy, new Object[]{carArray});
        assertEquals(0, answerArray.length);
    }

    public void testWhenNodesCheckedExceedsTargetResultCount() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, SecurityException, NoSuchMethodException
    {
        // ALF-11709: If 'count' nodes have been checked and that number exceeds targetResultCount
        // this should not stop further checks from happening, when the number of successful
        // security checks ('keepValues') is less then targetResultCount. If this does not hold true,
        // then in the case where the first targetResultCount checks are unsuccessful then the
        // method will not return any values that should be present. For example, if user_9999 looks
        // at User Homes, and there are folders user_0001 through to user_9998 being checked before user_9999's
        // folder - then if targetResultCount is only 1000, user_9999 will not see any folders in User Homes
        // however, they should see one folder - their own.
        runAs(AuthenticationUtil.getAdminUserName());

        NodeRef n1 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN,
                QName.createQName("{namespace}one"), ContentModel.TYPE_FOLDER).getChildRef();

        NodeRef n2 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN,
                QName.createQName("{namespace}one"), ContentModel.TYPE_FOLDER).getChildRef();

        NodeRef n3 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN,
                QName.createQName("{namespace}one"), ContentModel.TYPE_FOLDER).getChildRef();

        NodeRef n4 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN,
                QName.createQName("{namespace}one"), ContentModel.TYPE_FOLDER).getChildRef();

        // Set nodes n1..n3 to be unviewable by "andy" (using inherited permissions)
        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.ALL_PERMISSIONS), "andy", AccessStatus.DENIED));
        // The last node n4 can be seen (override the inherited permissions)
        permissionService.setPermission(new SimplePermissionEntry(n4, getPermission(PermissionService.ALL_PERMISSIONS), "andy", AccessStatus.ALLOWED));

        runAs("andy");

        Object o = new ClassWithMethods();
        Method methodCollection = o.getClass().getMethod("echoCollection", new Class[]{Collection.class});

        AdvisorAdapterRegistry advisorAdapterRegistry = GlobalAdvisorAdapterRegistry.getInstance();

        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.addAdvisor(advisorAdapterRegistry.wrap(new Interceptor("AFTER_ACL_NODE.sys:base.Read")));
        proxyFactory.setTargetSource(new SingletonTargetSource(o));
        Object proxy = proxyFactory.getProxy();

        List<NodeRef> nodeRefList = new ArrayList<NodeRef>(Arrays.asList(n1, n2, n3, n4));

        // targetResultCount = 3. The first three nodes are not visible by the user, so the logic
        // must not count those towards the targetResultCount cutoff.
        Collection<?> answerCollection = (Collection<?>) methodCollection.invoke(
                proxy, new Object[]{PermissionCheckCollectionMixin.create(nodeRefList, 3, 0, 0)});

        assertEquals(1, answerCollection.size());
        assertEquals(n4, answerCollection.iterator().next());
    }

    public void testResultSetFilterForNullParentOnly() throws Exception
    {
        runAs(AuthenticationUtil.getAdminUserName());
        NodeRef n1 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN,
                QName.createQName("{namespace}one"), ContentModel.TYPE_FOLDER).getChildRef();

        runAs("andy");

        Object o = new ClassWithMethods();
        Method methodResultSet = o.getClass().getMethod("echoResultSet", new Class[]{ResultSet.class});
        Method methodCollection = o.getClass().getMethod("echoCollection", new Class[]{Collection.class});
        Method methodArray = o.getClass().getMethod("echoArray", new Class[]{Object[].class});

        AdvisorAdapterRegistry advisorAdapterRegistry = GlobalAdvisorAdapterRegistry.getInstance();

        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.addAdvisor(advisorAdapterRegistry.wrap(new Interceptor("AFTER_ACL_PARENT.sys:base.Read")));
        proxyFactory.setTargetSource(new SingletonTargetSource(o));
        Object proxy = proxyFactory.getProxy();

        List<NodeRef> nodeRefList = new ArrayList<NodeRef>();
        nodeRefList.add(rootNodeRef);
        nodeRefList.add(systemNodeRef);
        nodeRefList.add(n1);
        nodeRefList.add(n1);

        NodeRef[] nodeRefArray = nodeRefList.toArray(new NodeRef[]{});

        Set<NodeRef> nodeRefSet = new HashSet<NodeRef>();
        nodeRefSet.addAll(nodeRefList);

        List<ChildAssociationRef> carList = new ArrayList<ChildAssociationRef>();
        carList.add(nodeService.getPrimaryParent(rootNodeRef));
        carList.add(nodeService.getPrimaryParent(systemNodeRef));
        carList.add(nodeService.getPrimaryParent(n1));
        carList.add(nodeService.getPrimaryParent(n1));

        ChildAssociationRef[] carArray = carList.toArray(new ChildAssociationRef[]{});

        Set<ChildAssociationRef> carSet = new HashSet<ChildAssociationRef>();
        carSet.addAll(carList);

        ChildAssocRefResultSet rsIn = new ChildAssocRefResultSet(nodeService, nodeRefList, false);

        assertEquals(4, rsIn.length());
        ResultSet answerResultSet = (ResultSet) methodResultSet.invoke(proxy, new Object[]{rsIn});
        assertEquals(1, answerResultSet.length());
        Collection answerCollection = (Collection) methodCollection.invoke(proxy, new Object[]{nodeRefList});
        assertEquals(1, answerCollection.size());
        answerCollection = (Collection) methodCollection.invoke(proxy, new Object[]{nodeRefSet});
        assertEquals(1, answerCollection.size());
        answerCollection = (Collection) methodCollection.invoke(proxy, new Object[]{carList});
        assertEquals(1, answerCollection.size());
        answerCollection = (Collection) methodCollection.invoke(proxy, new Object[]{carSet});
        assertEquals(1, answerCollection.size());
        Object[] answerArray = (Object[]) methodArray.invoke(proxy, new Object[]{nodeRefArray});
        assertEquals(1, answerArray.length);
        answerArray = (Object[]) methodArray.invoke(proxy, new Object[]{carArray});
        assertEquals(1, answerArray.length);
    }

    public void testResultSetFilterNone1() throws Exception
    {
        runAs(AuthenticationUtil.getAdminUserName());
        NodeRef n1 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN,
                QName.createQName("{namespace}one"), ContentModel.TYPE_FOLDER).getChildRef();

        runAs("andy");

        Object o = new ClassWithMethods();
        Method methodResultSet = o.getClass().getMethod("echoResultSet", new Class[]{ResultSet.class});
        Method methodCollection = o.getClass().getMethod("echoCollection", new Class[]{Collection.class});
        Method methodArray = o.getClass().getMethod("echoArray", new Class[]{Object[].class});

        AdvisorAdapterRegistry advisorAdapterRegistry = GlobalAdvisorAdapterRegistry.getInstance();

        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.addAdvisor(advisorAdapterRegistry.wrap(new Interceptor("AFTER_ACL_NODE.sys:base.Read")));
        proxyFactory.setTargetSource(new SingletonTargetSource(o));
        Object proxy = proxyFactory.getProxy();

        List<NodeRef> nodeRefList = new ArrayList<NodeRef>();
        nodeRefList.add(rootNodeRef);
        nodeRefList.add(systemNodeRef);
        nodeRefList.add(n1);
        nodeRefList.add(n1);

        List<Object> mixedRefList = new ArrayList<Object>();
        mixedRefList.add(rootNodeRef);
        mixedRefList.add(systemNodeRef);
        mixedRefList.add(n1);
        mixedRefList.add(n1);
        mixedRefList.add(rootNodeRef.getStoreRef());

        NodeRef[] nodeRefArray = nodeRefList.toArray(new NodeRef[]{});

        Set<NodeRef> nodeRefSet = new HashSet<NodeRef>();
        nodeRefSet.addAll(nodeRefList);

        Set<Object> mixedRefSet = new HashSet<Object>();
        mixedRefSet.addAll(mixedRefList);

        List<ChildAssociationRef> carList = new ArrayList<ChildAssociationRef>();
        carList.add(nodeService.getPrimaryParent(rootNodeRef));
        carList.add(nodeService.getPrimaryParent(systemNodeRef));
        carList.add(nodeService.getPrimaryParent(n1));
        carList.add(nodeService.getPrimaryParent(n1));

        ChildAssociationRef[] carArray = carList.toArray(new ChildAssociationRef[]{});

        Set<ChildAssociationRef> carSet = new HashSet<ChildAssociationRef>();
        carSet.addAll(carList);

        ChildAssocRefResultSet rsIn = new ChildAssocRefResultSet(nodeService, nodeRefList, false);

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ), "andy", AccessStatus.ALLOWED));

        assertEquals(4, rsIn.length());
        ResultSet answerResultSet = (ResultSet) methodResultSet.invoke(proxy, new Object[]{rsIn});
        assertEquals(4, answerResultSet.length());
        Collection answerCollection = (Collection) methodCollection.invoke(proxy, new Object[]{nodeRefList});
        assertEquals(4, answerCollection.size());
        answerCollection = (Collection) methodCollection.invoke(proxy, new Object[]{mixedRefList});
        assertEquals(5, answerCollection.size());
        answerCollection = (Collection) methodCollection.invoke(proxy, new Object[]{nodeRefSet});
        assertEquals(3, answerCollection.size());
        answerCollection = (Collection) methodCollection.invoke(proxy, new Object[]{mixedRefSet});
        assertEquals(4, answerCollection.size());
        answerCollection = (Collection) methodCollection.invoke(proxy, new Object[]{carList});
        assertEquals(4, answerCollection.size());
        answerCollection = (Collection) methodCollection.invoke(proxy, new Object[]{carSet});
        assertEquals(3, answerCollection.size());
        Object[] answerArray = (Object[]) methodArray.invoke(proxy, new Object[]{nodeRefArray});
        assertEquals(4, answerArray.length);
        answerArray = (Object[]) methodArray.invoke(proxy, new Object[]{carArray});
        assertEquals(4, answerArray.length);

        // Check cut-offs
        answerCollection = (Collection<?>) methodCollection.invoke(
                proxy, new Object[]{PermissionCheckCollectionMixin.create(new ArrayList<NodeRef>(nodeRefList), 1, 0, 0)});
        assertEquals(1, answerCollection.size());
        answerCollection = (Collection<?>) methodCollection.invoke(
                proxy, new Object[]{PermissionCheckCollectionMixin.create(new ArrayList<NodeRef>(nodeRefList), 5, 0, 2)});
        assertEquals(2, answerCollection.size());
        answerCollection = (Collection<?>) methodCollection.invoke(
                proxy, new Object[]{PermissionCheckCollectionMixin.create(new ArrayList<NodeRef>(nodeRefList), 5, 1, 0)});
        assertTrue("Too many results: " + answerCollection.size(), answerCollection.size() < 5); // Only 1ms to do the check

        permissionService.setPermission(new SimplePermissionEntry(n1, getPermission(PermissionService.READ), "andy", AccessStatus.DENIED));

        assertEquals(4, rsIn.length());
        answerResultSet = (ResultSet) methodResultSet.invoke(proxy, new Object[]{rsIn});
        assertEquals(2, answerResultSet.length());
        answerCollection = (Collection) methodCollection.invoke(proxy, new Object[]{nodeRefList});
        assertEquals(2, answerCollection.size());
        answerCollection = (Collection) methodCollection.invoke(proxy, new Object[]{mixedRefList});
        assertEquals(3, answerCollection.size());
        answerCollection = (Collection) methodCollection.invoke(proxy, new Object[]{nodeRefSet});
        assertEquals(2, answerCollection.size());
        answerCollection = (Collection) methodCollection.invoke(proxy, new Object[]{mixedRefSet});
        assertEquals(3, answerCollection.size());
        answerCollection = (Collection) methodCollection.invoke(proxy, new Object[]{carList});
        assertEquals(2, answerCollection.size());
        answerCollection = (Collection) methodCollection.invoke(proxy, new Object[]{carSet});
        assertEquals(2, answerCollection.size());
        answerArray = (Object[]) methodArray.invoke(proxy, new Object[]{nodeRefArray});
        assertEquals(2, answerArray.length);
        answerArray = (Object[]) methodArray.invoke(proxy, new Object[]{carArray});
        assertEquals(2, answerArray.length);

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ), "andy", AccessStatus.DENIED));

        assertEquals(4, rsIn.length());
        answerResultSet = (ResultSet) methodResultSet.invoke(proxy, new Object[]{rsIn});
        assertEquals(0, answerResultSet.length());
        answerCollection = (Collection) methodCollection.invoke(proxy, new Object[]{nodeRefList});
        assertEquals(0, answerCollection.size());
        answerCollection = (Collection) methodCollection.invoke(proxy, new Object[]{mixedRefList});
        assertEquals(0, answerCollection.size());
        answerCollection = (Collection) methodCollection.invoke(proxy, new Object[]{nodeRefSet});
        assertEquals(0, answerCollection.size());
        answerCollection = (Collection) methodCollection.invoke(proxy, new Object[]{mixedRefSet});
        assertEquals(0, answerCollection.size());
        answerCollection = (Collection) methodCollection.invoke(proxy, new Object[]{carList});
        assertEquals(0, answerCollection.size());
        answerCollection = (Collection) methodCollection.invoke(proxy, new Object[]{carSet});
        assertEquals(0, answerCollection.size());
        answerArray = (Object[]) methodArray.invoke(proxy, new Object[]{nodeRefArray});
        assertEquals(0, answerArray.length);
        answerArray = (Object[]) methodArray.invoke(proxy, new Object[]{carArray});
        assertEquals(0, answerArray.length);

    }

    public void testResultSetFilterNone2() throws Exception
    {
        runAs(AuthenticationUtil.getAdminUserName());

        NodeRef n1 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN,
                QName.createQName("{namespace}one"), ContentModel.TYPE_FOLDER).getChildRef();

        runAs("andy");

        Object o = new ClassWithMethods();
        Method methodResultSet = o.getClass().getMethod("echoResultSet", new Class[]{ResultSet.class});
        Method methodCollection = o.getClass().getMethod("echoCollection", new Class[]{Collection.class});
        Method methodArray = o.getClass().getMethod("echoArray", new Class[]{Object[].class});

        AdvisorAdapterRegistry advisorAdapterRegistry = GlobalAdvisorAdapterRegistry.getInstance();

        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.addAdvisor(advisorAdapterRegistry.wrap(new Interceptor("AFTER_ACL_PARENT.sys:base.Read")));
        proxyFactory.setTargetSource(new SingletonTargetSource(o));
        Object proxy = proxyFactory.getProxy();

        List<NodeRef> nodeRefList = new ArrayList<NodeRef>();
        nodeRefList.add(rootNodeRef);
        nodeRefList.add(systemNodeRef);
        nodeRefList.add(n1);
        nodeRefList.add(n1);

        List<Object> mixedRefList = new ArrayList<Object>();
        mixedRefList.add(rootNodeRef);
        mixedRefList.add(systemNodeRef);
        mixedRefList.add(n1);
        mixedRefList.add(n1);
        mixedRefList.add(rootNodeRef.getStoreRef());

        NodeRef[] nodeRefArray = nodeRefList.toArray(new NodeRef[]{});

        Set<NodeRef> nodeRefSet = new HashSet<NodeRef>();
        nodeRefSet.addAll(nodeRefList);

        Set<Object> mixedRefSet = new HashSet<Object>();
        mixedRefSet.addAll(mixedRefList);

        List<ChildAssociationRef> carList = new ArrayList<ChildAssociationRef>();
        carList.add(nodeService.getPrimaryParent(rootNodeRef));
        carList.add(nodeService.getPrimaryParent(systemNodeRef));
        carList.add(nodeService.getPrimaryParent(n1));
        carList.add(nodeService.getPrimaryParent(n1));

        ChildAssociationRef[] carArray = carList.toArray(new ChildAssociationRef[]{});

        Set<ChildAssociationRef> carSet = new HashSet<ChildAssociationRef>();
        carSet.addAll(carList);

        ChildAssocRefResultSet rsIn = new ChildAssocRefResultSet(nodeService, nodeRefList, false);

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ), "andy", AccessStatus.ALLOWED));

        assertEquals(4, rsIn.length());
        ResultSet answerResultSet = (ResultSet) methodResultSet.invoke(proxy, new Object[]{rsIn});
        assertEquals(4, answerResultSet.length());
        Collection answerCollection = (Collection) methodCollection.invoke(proxy, new Object[]{nodeRefList});
        assertEquals(4, answerCollection.size());
        answerCollection = (Collection) methodCollection.invoke(proxy, new Object[]{mixedRefList});
        assertEquals(5, answerCollection.size());
        answerCollection = (Collection) methodCollection.invoke(proxy, new Object[]{nodeRefSet});
        assertEquals(3, answerCollection.size());
        answerCollection = (Collection) methodCollection.invoke(proxy, new Object[]{mixedRefSet});
        assertEquals(4, answerCollection.size());
        answerCollection = (Collection) methodCollection.invoke(proxy, new Object[]{carList});
        assertEquals(4, answerCollection.size());
        answerCollection = (Collection) methodCollection.invoke(proxy, new Object[]{carSet});
        assertEquals(3, answerCollection.size());
        Object[] answerArray = (Object[]) methodArray.invoke(proxy, new Object[]{nodeRefArray});
        assertEquals(4, answerArray.length);
        answerArray = (Object[]) methodArray.invoke(proxy, new Object[]{carArray});
        assertEquals(4, answerArray.length);

        permissionService.setPermission(new SimplePermissionEntry(n1, getPermission(PermissionService.READ), "andy", AccessStatus.DENIED));

        assertEquals(4, rsIn.length());
        answerResultSet = (ResultSet) methodResultSet.invoke(proxy, new Object[]{rsIn});
        assertEquals(4, answerResultSet.length());
        answerCollection = (Collection) methodCollection.invoke(proxy, new Object[]{nodeRefList});
        assertEquals(4, answerCollection.size());
        answerCollection = (Collection) methodCollection.invoke(proxy, new Object[]{mixedRefList});
        assertEquals(5, answerCollection.size());
        answerCollection = (Collection) methodCollection.invoke(proxy, new Object[]{nodeRefSet});
        assertEquals(3, answerCollection.size());
        answerCollection = (Collection) methodCollection.invoke(proxy, new Object[]{mixedRefSet});
        assertEquals(4, answerCollection.size());
        answerCollection = (Collection) methodCollection.invoke(proxy, new Object[]{carList});
        assertEquals(4, answerCollection.size());
        answerCollection = (Collection) methodCollection.invoke(proxy, new Object[]{carSet});
        assertEquals(3, answerCollection.size());
        answerArray = (Object[]) methodArray.invoke(proxy, new Object[]{nodeRefArray});
        assertEquals(4, answerArray.length);
        answerArray = (Object[]) methodArray.invoke(proxy, new Object[]{carArray});
        assertEquals(4, answerArray.length);

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ), "andy", AccessStatus.DENIED));

        assertEquals(4, rsIn.length());
        answerResultSet = (ResultSet) methodResultSet.invoke(proxy, new Object[]{rsIn});
        assertEquals(1, answerResultSet.length());
        answerCollection = (Collection) methodCollection.invoke(proxy, new Object[]{nodeRefList});
        assertEquals(1, answerCollection.size());
        answerCollection = (Collection) methodCollection.invoke(proxy, new Object[]{mixedRefList});
        assertEquals(2, answerCollection.size());
        answerCollection = (Collection) methodCollection.invoke(proxy, new Object[]{nodeRefSet});
        assertEquals(1, answerCollection.size());
        answerCollection = (Collection) methodCollection.invoke(proxy, new Object[]{mixedRefSet});
        assertEquals(2, answerCollection.size());
        answerCollection = (Collection) methodCollection.invoke(proxy, new Object[]{carList});
        assertEquals(1, answerCollection.size());
        answerCollection = (Collection) methodCollection.invoke(proxy, new Object[]{carSet});
        assertEquals(1, answerCollection.size());
        answerArray = (Object[]) methodArray.invoke(proxy, new Object[]{nodeRefArray});
        assertEquals(1, answerArray.length);
        answerArray = (Object[]) methodArray.invoke(proxy, new Object[]{carArray});
        assertEquals(1, answerArray.length);

    }

    public static class ClassWithMethods
    {

        public Object echoObject(Object o)
        {
            return o;
        }

        public StoreRef echoStoreRef(StoreRef storeRef)
        {
            return storeRef;
        }

        public NodeRef echoNodeRef(NodeRef nodeRef)
        {
            return nodeRef;
        }

        public Pair<Long, NodeRef> echoNodePair(NodeRef nodeRef)
        {
            return new Pair<Long, NodeRef>(Long.valueOf(1), nodeRef);
        }

        public ChildAssociationRef echoChildAssocRef(ChildAssociationRef car)
        {
            return car;
        }

        public ResultSet echoResultSet(ResultSet rs)
        {
            return rs;
        }

        public <T> Collection<T> echoCollection(Collection<T> nrc)
        {
            return nrc;
        }

        public <T> T[] echoArray(T[] nra)
        {
            return nra;
        }

    }

    public class Interceptor implements MethodInterceptor
    {
        ConfigAttributeDefinition cad = new ConfigAttributeDefinition();

        Interceptor(final String config)
        {
            cad.addConfigAttribute(new ConfigAttribute() {

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
            ACLEntryAfterInvocationProvider after = new ACLEntryAfterInvocationProvider();
            after.setNamespacePrefixResolver(namespacePrefixResolver);
            after.setPermissionService(permissionService);
            after.setNodeService(nodeService);
            after.setUnfilteredFor(Collections.singleton("{ns}ln"));
            after.afterPropertiesSet();

            Object returnObject = invocation.proceed();
            return after.decide(null, invocation, cad, returnObject);
        }
    }
}
