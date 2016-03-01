 
package org.alfresco.module.org_alfresco_module_rm.test.integration.disposition;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.action.RMDispositionActionExecuterAbstractBase;
import org.alfresco.module.org_alfresco_module_rm.action.impl.CompleteEventAction;
import org.alfresco.module.org_alfresco_module_rm.action.impl.CutOffAction;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.module.org_alfresco_module_rm.test.util.CommonRMTestUtils;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.GUID;

/**
 * Cut off integration tests.
 *
 * @author Roy Wetherall
 * @since 2.2
 */
public class CutOffTest extends BaseRMTestCase
{
    /**
     * given we have a record folder that is eligible for cutoff ensure that the
     * record can be cut off successfully.
     */
    public void testCutOffRecordFolder()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            NodeRef recordFolder = null;

            @Override
            public void given()
            {
                //create record folder
                recordFolder = recordFolderService.createRecordFolder(rmContainer, GUID.generate());

                // TODO add some records

                // make eligible for cutoff
                Map<String, Serializable> params = new HashMap<String, Serializable>(1);
                params.put(CompleteEventAction.PARAM_EVENT_NAME, CommonRMTestUtils.DEFAULT_EVENT_NAME);
                rmActionService.executeRecordsManagementAction(recordFolder, CompleteEventAction.NAME, params);
            }

            @Override
            public void when()
            {
                // complete event
                rmActionService.executeRecordsManagementAction(recordFolder, CutOffAction.NAME, null);
            }

            @Override
            public void then()
            {
                // ensure the record folder is cut off
                assertTrue(dispositionService.isDisposableItemCutoff(recordFolder));
            }
        });

    }

    /**
     * given that we have a closed record folder eligible for cut off ensure that it can
     * be cut off.
     * <p>
     * relates to https://issues.alfresco.com/jira/browse/RM-1340
     */
    public void testCutOffClosedRecordFolder()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            NodeRef recordFolder = null;

            @Override
            public void given()
            {
                //create record folder
                recordFolder = recordFolderService.createRecordFolder(rmContainer, GUID.generate());

                // TODO add some records

                // make eligible for cutoff
                Map<String, Serializable> params = new HashMap<String, Serializable>(1);
                params.put(CompleteEventAction.PARAM_EVENT_NAME, CommonRMTestUtils.DEFAULT_EVENT_NAME);
                rmActionService.executeRecordsManagementAction(recordFolder, CompleteEventAction.NAME, params);

                // close the record folder
                recordFolderService.closeRecordFolder(recordFolder);
            }

            @Override
            public void when()
            {
                // complete event
                rmActionService.executeRecordsManagementAction(recordFolder, CutOffAction.NAME, null);
            }

            @Override
            public void then()
            {
                // ensure the record folder is cut off
                assertTrue(dispositionService.isDisposableItemCutoff(recordFolder));
            }
        });
    }

    /**
     * given we have a record folder that is eligible for cutoff ensure that the
     * record can be cut off successfully.
     */
    public void testCutOffUncutOffRecordFolder()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            NodeRef recordFolder = null;

            @Override
            public void given()
            {
                //create record folder
                recordFolder = recordFolderService.createRecordFolder(rmContainer, GUID.generate());
                nodeService.addAspect(recordFolder, ASPECT_UNCUT_OFF, null);

                // TODO add some records

                // make eligible for cutoff
                Map<String, Serializable> params = new HashMap<String, Serializable>(1);
                params.put(CompleteEventAction.PARAM_EVENT_NAME, CommonRMTestUtils.DEFAULT_EVENT_NAME);
                rmActionService.executeRecordsManagementAction(recordFolder, CompleteEventAction.NAME, params);
            }

            @Override
            public void when()
            {
                // complete event
                rmActionService.executeRecordsManagementAction(recordFolder, CutOffAction.NAME, null);
            }

            @Override
            public void then()
            {
                // ensure the record folder is cut off
                assertTrue(dispositionService.isDisposableItemCutoff(recordFolder));
            }
        });

    }

    /**
     * given we have a record folder that is eligible for cutoff ensure that the
     * record can be cut off successfully.
     */
    public void testCutOffUncutOffRecordFolderFromSchedule()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            NodeRef recordFolder = null;

            @Override
            public void given()
            {
                //create record folder
                recordFolder = recordFolderService.createRecordFolder(rmContainer, GUID.generate());
                nodeService.addAspect(recordFolder, ASPECT_UNCUT_OFF, null);

                // TODO add some records

                // make eligible for cutoff
                Map<String, Serializable> params = new HashMap<String, Serializable>(1);
                params.put(CompleteEventAction.PARAM_EVENT_NAME, CommonRMTestUtils.DEFAULT_EVENT_NAME);
                rmActionService.executeRecordsManagementAction(recordFolder, CompleteEventAction.NAME, params);
            }

            @Override
            public void when()
            {
                // complete event
                Map<String, Serializable> params = new HashMap<String, Serializable>(1);
                params.put(RMDispositionActionExecuterAbstractBase.PARAM_NO_ERROR_CHECK, Boolean.FALSE);
                try
                {
                    rmActionService.executeRecordsManagementAction(recordFolder, CutOffAction.NAME, params);
                }
                catch(AlfrescoRuntimeException e) { } // expected
            }

            @Override
            public void then()
            {
                // ensure the record folder is cut off
                assertFalse(dispositionService.isDisposableItemCutoff(recordFolder));
            }
        });

    }

}
