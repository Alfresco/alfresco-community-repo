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

package org.alfresco.module.org_alfresco_module_rm.capability;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Iterator;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.capability.policy.ConfigAttributeDefinition;
import org.alfresco.module.org_alfresco_module_rm.capability.policy.Policy;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest;
import org.alfresco.service.cmr.repository.NodeRef;
import org.aopalliance.intercept.MethodInvocation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import net.sf.acegisecurity.Authentication;
import net.sf.acegisecurity.ConfigAttribute;
import net.sf.acegisecurity.vote.AccessDecisionVoter;

/**
 * RM entry voter unit test
 * 
 * @author Roy Wetherall
 * @since 2.3
 */
public class RMEntryVoterUnitTest extends BaseUnitTest
{
    private static final String POLICY_NAME = "myPolicy";
    
    /** RM Entry */
    private @InjectMocks RMEntryVoter entryVoter;
    
    /** mocked policy */
    private @Mock Policy mockedPolicy;
    
    /** mocked authentication */
    private @Mock Authentication mockedAuthentication;
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest#before()
     */
    @Before
    @Override
    public void before() throws Exception
    {
        super.before();
        
        // don't run as system
        when(mockedAuthenticationUtil.isRunAsUserTheSystemUser())
            .thenReturn(false);
        
        // indicate that "vote" transaction value is not set
        when(mockedTransactionalResourceHelper.isResourcePresent("voting"))
            .thenReturn(false);
    }
    
    /**
     * Given that the system is already voting 
     * When I vote
     * Then access granted
     */
    @Test
    public void alreadyVoting() throws Exception
    {
        // indicate already voting
        when(mockedTransactionalResourceHelper.isResourcePresent("voting"))
            .thenReturn(true);
        
        // given I am providing an invalid policy for a method
        MethodInvocation mockedMethodInvocation = createMethodInvoation("myTestMethod", NodeRef.class);
        net.sf.acegisecurity.ConfigAttributeDefinition mockedConfigDef = createConfigDefinition("RM.invalid");
        
        // call vote
        assertEquals(
                AccessDecisionVoter.ACCESS_GRANTED,
                entryVoter.vote(mockedAuthentication, mockedMethodInvocation, mockedConfigDef));    
    }
    
    /**
     * Given that I am running this as the system user
     * When I evaluate
     * Then access granted
     */
    @Test
    public void runAsSystem() throws Exception
    {
        // run as system
        when(mockedAuthenticationUtil.isRunAsUserTheSystemUser())
            .thenReturn(true);
        
        // given I am providing an invalid policy for a method
        MethodInvocation mockedMethodInvocation = createMethodInvoation("myTestMethod", NodeRef.class);
        net.sf.acegisecurity.ConfigAttributeDefinition mockedConfigDef = createConfigDefinition("RM.invalid");
        
        // call vote
        assertEquals(
                AccessDecisionVoter.ACCESS_GRANTED,
                entryVoter.vote(mockedAuthentication, mockedMethodInvocation, mockedConfigDef));    
    }
    
    /**
     * Given that we have provided an invalid policy
     * When I evaluate the voter
     * Then an AlfrescoRuntimeException is thrown
     */
    @Test
    public void invalidPolicy() throws Exception
    {   
        // given I am providing an invalid policy for a method
        MethodInvocation mockedMethodInvocation = createMethodInvoation("myTestMethod", NodeRef.class);
        net.sf.acegisecurity.ConfigAttributeDefinition mockedConfigDef = createConfigDefinition("RM.invalid");

        // I expect an Alfresco Runtime Exception
        exception.expect(AlfrescoRuntimeException.class);
        
        // call vote
        entryVoter.vote(mockedAuthentication, mockedMethodInvocation, mockedConfigDef);    
    }
    
    /**
     * Given that I have provided a valid policy
     * When I evaluate the voter
     * Then the corresponding policy will be evaluated
     */
    @Test
    public void validPolicy() throws Exception
    {
        // valid policy
        when(mockedPolicy.getName())
            .thenReturn(POLICY_NAME);
        entryVoter.registerPolicy(mockedPolicy);
        
        //  mock calling details
        MethodInvocation mockedMethodInvocation = createMethodInvoation("myTestMethod", NodeRef.class);
        net.sf.acegisecurity.ConfigAttributeDefinition mockedConfigDef = createConfigDefinition("RM." + POLICY_NAME);
        
        // call vote
        entryVoter.vote(mockedAuthentication, mockedMethodInvocation, mockedConfigDef);
        
        // verify that the policy was executed
        verify(mockedPolicy, times(1)).evaluate(eq(mockedMethodInvocation), any(Class[].class), any(ConfigAttributeDefinition.class));
    }
    
    /**
     * Helper method to create configuration object
     */
    @SuppressWarnings("rawtypes")
    private net.sf.acegisecurity.ConfigAttributeDefinition createConfigDefinition(String value)
    {
        net.sf.acegisecurity.ConfigAttributeDefinition mockedConfig = mock(net.sf.acegisecurity.ConfigAttributeDefinition.class);
        
        ConfigAttribute mockedConfigAttr = mock(ConfigAttribute.class);        
        when(mockedConfigAttr.getAttribute())
            .thenReturn(value);
        
        Iterator mockedIter = mock(Iterator.class);
        when(mockedIter.hasNext())
            .thenReturn(true)
            .thenReturn(false);
        when(mockedIter.next())
            .thenReturn(mockedConfigAttr);
        
        when(mockedConfig.getConfigAttributes())
            .thenReturn(mockedIter);
        
        return mockedConfig;        
    }
    
    /**
     * Helper method to create method invocation mock
     */
    private MethodInvocation createMethodInvoation(String methodName, Class<?> ... parameterTypes)
        throws Exception
    {
        // mock method invocation
        MethodInvocation mockedMethodInvocation = mock(MethodInvocation.class);
        
        // get method object .. assumed to be a method on this object
        Method method = RMEntryVoterUnitTest.class.getMethod(methodName, parameterTypes);
        when(mockedMethodInvocation.getMethod())
            .thenReturn(method);
        
        return mockedMethodInvocation;
    }
    
    /** ========= Test methods ======== */
    
    public void myTestMethod(NodeRef nodeRef)
    {
        // does nothing
    }
    
}
