package org.alfresco.module.org_alfresco_module_rm.action.impl;

import static org.mockito.Mockito.verifyZeroInteractions;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.action.BaseActionUnitTest;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Test;
import org.mockito.InjectMocks;

/**
 * Unit test for file report action.
 *
 * @author Roy Wetherall
 * @since 2.2
 */
public class FileReportActionUnitTest extends BaseActionUnitTest
{
    /** actioned upon node reference */
    private NodeRef actionedUponNodeRef;

    /** file report action */
    private @InjectMocks FileReportAction fileReportAction;

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest#before()
     */
    @Override
    public void before() throws Exception
    {
        super.before();

        // actioned upon node reference
        actionedUponNodeRef = generateRecord();

        // mocked action
        fileReportAction.setAuditable(false);
    }

    /**
     * given the destination is not set, ensure that an exception is thrown
     */
    @Test
    public void destinationNotSet()
    {
        // == given ==

        // set action parameter values
        mockActionParameterValue(FileReportAction.MIMETYPE, MimetypeMap.MIMETYPE_HTML);
        mockActionParameterValue(FileReportAction.REPORT_TYPE, "rma:destructionReport");

        // expected exception
        exception.expect(AlfrescoRuntimeException.class);

        // == when ==

        // execute action
        fileReportAction.executeImpl(getMockedAction(), actionedUponNodeRef);

        // == then ==
        verifyZeroInteractions(mockedReportService, mockedNodeService);
    }

    /**
     * given no report type set, ensure that an exception is thrown
     */
    @Test
    public void reportTypeNotSet()
    {
        // == given ==

        // set action parameter values
        mockActionParameterValue(FileReportAction.MIMETYPE, MimetypeMap.MIMETYPE_HTML);
        mockActionParameterValue(FileReportAction.DESTINATION, generateNodeRef().toString());

        // expected exception
        exception.expect(AlfrescoRuntimeException.class);

        // == when ==

        // execute action
        fileReportAction.executeImpl(getMockedAction(), actionedUponNodeRef);

        // == then ==
        verifyZeroInteractions(mockedReportService, mockedNodeService);
    }
}
