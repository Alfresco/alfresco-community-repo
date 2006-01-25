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
    private int fixesFromSchema;
    private int fixesToSchema;
    private int targetSchema;
    private String description;
    /** a list of patches that this one depends on */
    private List<Patch> dependsOn;
    /** flag indicating if the patch was successfully applied */
    private boolean applied;
    /** the service to register ourselves with */
    private PatchService patchService;
    private TransactionService transactionService;

    public AbstractPatch()
    {
        this.fixesFromSchema = -1;
        this.fixesToSchema = -1;
        this.targetSchema = -1;
        this.applied = false;
        this.dependsOn = Collections.emptyList();
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(256);
        sb.append("Patch")
          .append("[ id=").append(id)
          .append(", description=").append(description)
          .append(", fixesFromSchema=").append(fixesFromSchema)
          .append(", fixesToSchema=").append(fixesToSchema)
          .append(", targetSchema=").append(targetSchema)
          .append("]");
        return sb.toString();
    }

    /**
     * Set the service that this patch will register with for execution.
     */
    public void setPatchService(PatchService patchService)
    {
        this.patchService = patchService;
    }

    /**
     * Set the transaction provider so that each execution can be performed within a transaction
     */
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    /**
     * This ensures that this bean gets registered with the appropriate {@link PatchService service}. 
     */
    public void init()
    {
        if (patchService == null)
        {
            throw new AlfrescoRuntimeException("Mandatory property not set: patchService");
        }
        patchService.registerPatch(this);
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

    public int getFixesFromSchema()
    {
        return fixesFromSchema;
    }

    /**
     * Set the smallest schema number that this patch may be applied to.
     * 
     * @param version a schema number not smaller than 0
     */
    public void setFixesFromSchema(int version)
    {
        if (version < 0)
        {
            throw new IllegalArgumentException("The 'fixesFromSchema' property may not be less than 0");
        }
        this.fixesFromSchema = version;
        // auto-adjust the to version
        if (fixesToSchema < fixesFromSchema)
        {
            setFixesToSchema(this.fixesFromSchema);
        }
    }

    public int getFixesToSchema()
    {
        return fixesToSchema;
    }

    /**
     * Set the largest schema version number that this patch may be applied to.
     * 
     * @param version a schema version number not smaller than the
     *      {@link #setFixesFromSchema(int) from version} number.
     */
    public void setFixesToSchema(int version)
    {
        if (version < fixesFromSchema)
        {
            throw new IllegalArgumentException("'fixesToSchema' must be greater than or equal to 'fixesFromSchema'");
        }
        this.fixesToSchema = version;
    }

    public int getTargetSchema()
    {
        return targetSchema;
    }

    /**
     * Set the schema version that this patch attempts to take the existing schema to.
     * This is for informational purposes only, acting as an indicator of intention rather
     * than having any specific effect.
     * 
     * @param version a schema version number that must be greater than the
     *      {@link #fixesToSchema max fix schema number}
     */
    public void setTargetSchema(int version)
    {
        if (version <= fixesToSchema)
        {
            throw new IllegalArgumentException("'targetSchema' must be greater than 'fixesToSchema'");
        }
        this.targetSchema = version;
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

    public boolean applies(int version)
    {
        return ((this.fixesFromSchema <= version) && (version <= fixesToSchema));
    }

    /**
     * Check that the schema version properties have been set appropriately
     */
    private void checkProperties()
    {
        // check that the necessary properties have been set
        if (id == null || description == null)
        {
            throw new AlfrescoRuntimeException(
                    "Patch properties 'id', 'fixesFromSchema' and 'description' have not all been set on this patch: \n" +
                    "   patch: " + this);
        }
        else if (fixesFromSchema == -1 || fixesToSchema == -1 || targetSchema == -1)
        {
            throw new AlfrescoRuntimeException(
                    "Patch properties 'fixesFromSchema', 'fixesToSchema' and 'targetSchema' have not all been set on this patch: \n" +
                    "   patch: " + this);
        }
        else if (transactionService == null)
        {
            throw new AlfrescoRuntimeException("'transactionService' property has not been set: \n" +
                    "   patch: " + this);
        }
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
        // check properties
        checkProperties();
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
            String report = TransactionUtil.executeInNonPropagatingUserTransaction(transactionService, patchWork);
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
