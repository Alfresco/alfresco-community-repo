/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.admin.patch;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.transaction.TransactionUtil;
import org.alfresco.repo.transaction.TransactionUtil.TransactionWork;
import org.alfresco.service.cmr.admin.PatchException;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Base implementation of the patch.  This class ensures that the patch is
 * thread- and transaction-safe.
 * 
 * @author Derek Hulley
 */
public abstract class AbstractPatch implements Patch
{
    private static Log logger = LogFactory.getLog(AbstractPatch.class);
    
    private String id;
    private String applyToVersion;
    private String description;
    /** a list of patches that this one depends on */
    private List<Patch> dependsOn;
    /** flag indicating if the patch was successfully applied */
    private boolean applied;
    private TransactionService transactionService;

    public AbstractPatch()
    {
        this.applied = false;
        this.dependsOn = Collections.emptyList();
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(56);
        sb.append("Patch")
          .append("[id=").append(getId())
          .append(", after=").append(getApplyToVersion())
          .append(", description=").append(getDescription())
          .append("]");
        return sb.toString();
    }
    
    /**
     * Set the transaction provider so that each execution can be performed within a transaction
     */
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }
    
    public String getId()
    {
        return id;
    }

    /**
     * 
     * @param id the unique ID of the patch.  This dictates the order in which patches are applied.
     */
    public void setId(String id)
    {
        this.id = id;
    }

    public String getApplyToVersion()
    {
        return applyToVersion;
    }

    /**
     * 
     * @param applyAfterVersion the version of the repository after which this patch must be applied.
     */
    public void setApplyToVersion(String applyAfterVersion)
    {
        this.applyToVersion = applyAfterVersion;
    }

    public String getDescription()
    {
        return description;
    }

    /**
     * @param description a thorough description of the patch
     */
    public void setDescription(String description)
    {
        this.description = description;
    }

    public List<Patch> getDependsOn()
    {
        return this.dependsOn;
    }
    /**
     * Set all the dependencies for this patch.  It should not be executed
     * before all the dependencies have been applied.
     * 
     * @param dependsOn a list of dependencies
     */
    public void setDependsOn(List<Patch> dependsOn)
    {
        this.dependsOn = dependsOn;
    }

    /**
     * Sets up the transaction and ensures thread-safety.
     * 
     * @see #applyInternal()
     */
    public synchronized String apply() throws PatchException
    {
        // ensure that this has not been executed already
        if (applied)
        {
            throw new AlfrescoRuntimeException("The patch has already been executed: \n" +
                    "   patch: " + this);
        }
        // check that the necessary properties have been set
        if (id == null || applyToVersion == null || description == null)
        {
            throw new AlfrescoRuntimeException(
                    "Patch properties 'id', 'applyToVersion' and 'description' have not all been set on this patch: \n" +
                    "   patch: " + this);
        }
        else if (transactionService == null)
        {
            throw new AlfrescoRuntimeException("'transactionService' property has not been set: \n" +
                    "   patch: " + this);
        }
        // execute in a transaction
        try
        {
            TransactionWork<String> patchWork = new TransactionWork<String>()
            {
                public String doWork() throws Exception
                {
                    String report = applyInternal();
                    // done
                    return report;
                };
            };
            String report = TransactionUtil.executeInUserTransaction(transactionService, patchWork);
            // the patch was successfully applied
            applied = true;
            // done
            if (logger.isDebugEnabled())
            {
                logger.debug("Patch successfully applied: \n" +
                        "   patch: " + this + "\n" +
                        "   report: " + report);
            }
            return report;
        }
        catch (PatchException e)
        {
            // no need to extract the exception
            throw e;
        }
        catch (Throwable e)
        {
            // check whether there is an embedded patch exception
            Throwable cause = e.getCause();
            if (cause != null && cause instanceof PatchException)
            {
                throw (PatchException) cause;
            }
            // need to generate a message from the exception
            String report = makeReport(e);
            // generate the correct exception
            throw new PatchException(report);
        }
    }
    
    /**
     * Dumps the error's full message and trace to the String
     * 
     * @param e the throwable
     * @return Returns a String representative of the printStackTrace method
     */
    private String makeReport(Throwable e)
    {
        StringWriter stringWriter = new StringWriter(1024);
        PrintWriter printWriter = new PrintWriter(stringWriter, true);
        try
        {
            e.printStackTrace(printWriter);
            return stringWriter.toString();
        }
        finally
        {
            printWriter.close();
        }
    }

    /**
     * This method does the work.  All transactions and thread-safety will be taken care of by this class.
     * Any exception will result in the transaction being rolled back.
     * 
     * @return Returns the report (only success messages).
     * @see #apply()
     * @throws Exception anything can be thrown.  This must be used for all failures.
     */
    protected abstract String applyInternal() throws Exception;
}
