/*-----------------------------------------------------------------------------
*  Copyright 2007 Alfresco Inc.
*  
*  Licensed under the Mozilla Public License version 1.1
*  with a permitted attribution clause. You may obtain a
*  copy of the License at:
*  
*      http://www.alfresco.org/legal/license.txt
*  
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
*  either express or implied. See the License for the specific
*  language governing permissions and limitations under the
*  License.
*  
*  
*  Author  Jon Cox  <jcox@alfresco.com>
*  File    AVMSubmitTransactionListener.java
*----------------------------------------------------------------------------*/

package org.alfresco.repo.avm.wf;

import java.util.List;

import org.alfresco.mbeans.VirtServerRegistry;
import org.alfresco.repo.avm.util.RawServices;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.service.cmr.avmsync.AVMDifference;
import org.alfresco.util.VirtServerUtils;
import org.springframework.context.ApplicationContext;

/**
*  Gets callbacks at critical moments within a transaction
*  (commit, rollback, etc.) to perform JMX update notifications
*  to the virtualization server.
*/
public class AVMSubmitTransactionListener extends TransactionListenerAdapter
{
    public AVMSubmitTransactionListener() { }
   

    /**
    *  Notify virtualization server that webapps in workflow sandbox
    *  are not longer needed, and possibly trigger a notification 
    *  instrucing the virtualization server to reload staging
    *  and every virtual webapp that depends on it.
    */
    @Override
    public void afterCommit()
    {
        List<AVMDifference> stagingDiffs = 
            (List<AVMDifference>)
            AlfrescoTransactionSupport.getResource("staging_diffs");

        if ( stagingDiffs == null) { return; }   // TODO: log this?

        AVMDifference requiresUpdate = null;
         
        for (AVMDifference diff : stagingDiffs)
        {
            // Example values:
            //
            // diff.getSourceVersion() == -1; diff.getSourcePath() ==
            //   mysite--workflow-21edf548-b17e-11db-bd90-35dd2ee4a5c6:/www/avm_webapps/ROOT/x.txt
            //
            // diff.getDestinationVersion() == -1;  diff.getDestinationPath() ==
            //   mysite:/www/avm_webapps/ROOT/x.txt

            if ( requiresUpdate == null )
            {
                if ( VirtServerUtils.requiresUpdateNotification(  diff.getDestinationPath() ) )
                {
                    requiresUpdate = diff;
                }
            }
        }

        ApplicationContext springContext   = RawServices.Instance().getContext();
        VirtServerRegistry vServerRegistry = (VirtServerRegistry) 
                                             springContext.getBean("VirtServerRegistry");



        // TODO: In the future, we might want to allow a single submit to
        //       update multiple staging areas & versions.  If so, 
        //       the logic above will have to look for each unique 
        //       version/webapp tuple, rather than assume everything
        //       is going into the same version and into the same
        //       store/webapp.


        // Only update staging if necessary
        if ( requiresUpdate != null )
        {
            vServerRegistry.updateAllWebapps( requiresUpdate.getDestinationVersion(),
                                              requiresUpdate.getDestinationPath(),
                                              true
                                             );
        }

        // Remove virtual weapps from workflow sandbox

        if ( ! stagingDiffs.isEmpty() )
        {
            // All the files are from the same workflow sandbox;
            // so to remove all the webapps, you just need to
            // look at the 1st difference

            AVMDifference d = stagingDiffs.iterator().next();
            vServerRegistry.removeAllWebapps( d.getSourceVersion(), d.getSourcePath(), true );
        }


        AlfrescoTransactionSupport.unbindResource("staging_diffs");
    }


    /**
    *  Handle failed transaction.
    */
    @Override
    public void afterRollback()
    {
        AlfrescoTransactionSupport.unbindResource("staging_diffs");
    }
}
