 
package org.alfresco.repo.security.permissions.impl;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertEquals;

import org.alfresco.module.org_alfresco_module_rm.test.util.AlfMock;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest;
import org.alfresco.repo.security.permissions.processor.PermissionPostProcessor;
import org.alfresco.repo.security.permissions.processor.PermissionPreProcessor;
import org.alfresco.repo.security.permissions.processor.PermissionProcessorRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

/**
 * Extended permission service implementation unit test
 * 
 * @author Roy Wetherall
 * @since 2.4.a
 */
public class ExtendedPermissionServiceImplUnitTest extends BaseUnitTest 
{
	private @InjectMocks @Spy ExtendedPermissionServiceImpl extendedPermissionServiceImpl = new ExtendedPermissionServiceImpl()
	{
		protected AccessStatus hasPermissionImpl(NodeRef nodeRef, String perm) { return AccessStatus.UNDETERMINED; };
	};
	
	private @Mock PermissionProcessorRegistry mockedPermissionProcessorRegistry;
	private @Mock PermissionPreProcessor mockedPermissionPreProcessor;
	private @Mock PermissionPostProcessor mockedPermissionPostProcessor;
	
	/**
	 * Given a permission pre-processor has been registered
	 * And does not DENY
	 * When hasPermission is called
	 * Then the pre-processor is executed
	 * And the ACL's are evaluated as normal
	 */
	@Test
	public void preProcessorDoesNotDeny()
	{
		NodeRef nodeRef = generateCmContent("anyname");
		String perm = AlfMock.generateText();
		when(mockedPermissionProcessorRegistry.getPermissionPreProcessors())
			.thenReturn(asList(mockedPermissionPreProcessor));
		when(mockedPermissionPreProcessor.process(nodeRef, perm))
		    .thenReturn(AccessStatus.UNDETERMINED);
		
		AccessStatus result = extendedPermissionServiceImpl.hasPermission(nodeRef, perm);

		assertEquals(AccessStatus.UNDETERMINED, result);
		verify(mockedPermissionPreProcessor).process(nodeRef, perm);
		verify(extendedPermissionServiceImpl).hasPermissionImpl(nodeRef, perm);
	}
	
	/**
	 * Given a permission pre-processor has been registered
	 * And DENY's
	 * When hasPermission is called
	 * Then the pre-processor is executed
	 * And the remaining permission evaluations do not take place
	 */
	@Test
	public void preProcessorDenys()
	{
		NodeRef nodeRef = generateCmContent("anyname");
		String perm = AlfMock.generateText();
		when(mockedPermissionProcessorRegistry.getPermissionPreProcessors())
			.thenReturn(asList(mockedPermissionPreProcessor));
		when(mockedPermissionPreProcessor.process(nodeRef, perm))
		    .thenReturn(AccessStatus.DENIED);
		
		AccessStatus result = extendedPermissionServiceImpl.hasPermission(nodeRef, perm);

		assertEquals(AccessStatus.DENIED, result);
		verify(mockedPermissionPreProcessor).process(nodeRef, perm);
		verify(extendedPermissionServiceImpl, never()).hasPermissionImpl(nodeRef, perm);
	}

	/**
	 * Given a permission post-processor has been registered
	 * When hasPermission is called
	 * Then the permission post-processor is called
	 */
	@Test
	public void postProcessorRegistered()
	{
		NodeRef nodeRef = generateCmContent("anyname");
		String perm = AlfMock.generateText();
		when(mockedPermissionProcessorRegistry.getPermissionPostProcessors())
			.thenReturn(asList(mockedPermissionPostProcessor));
		when(mockedPermissionPostProcessor.process(AccessStatus.UNDETERMINED, nodeRef, perm))
	    	.thenReturn(AccessStatus.ALLOWED);
	
		AccessStatus result = extendedPermissionServiceImpl.hasPermission(nodeRef, perm);		

		assertEquals(AccessStatus.ALLOWED, result);
		verify(mockedPermissionPostProcessor).process(AccessStatus.UNDETERMINED, nodeRef, perm);
		verify(extendedPermissionServiceImpl).hasPermissionImpl(nodeRef, perm);
	}
}
