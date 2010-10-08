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
package org.alfresco.repo.audit;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.audit.extractor.DataExtractor;
import org.alfresco.repo.audit.generator.DataGenerator;
import org.alfresco.repo.audit.model.AuditApplication;
import org.alfresco.repo.audit.model.AuditModelRegistry;
import org.alfresco.repo.audit.model.AuditModelRegistryImpl;
import org.alfresco.repo.audit.model.AuditApplication.DataExtractorDefinition;
import org.alfresco.repo.domain.audit.AuditDAO;
import org.alfresco.repo.domain.propval.PropertyValueDAO;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.audit.AuditQueryParameters;
import org.alfresco.service.cmr.audit.AuditService.AuditQueryCallback;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.PathMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.ParameterCheck;

/**
 * Component that records audit values as well as providing the query implementation.
 * <p>
 * To turn on logging of all <i>potentially auditable</i> data, turn on logging for:<br/>
 * <strong>{@link #INBOUND_LOGGER org.alfresco.repo.audit.inbound}</strong>.
 * <p/>
 * TODO: Respect audit internal - at the moment audit internal is fixed to false.
 * 
 * @author Derek Hulley
 * @since 3.2 (in its current form)
 */
public class AuditComponentImpl implements AuditComponent
{
    private static final String INBOUND_LOGGER = "org.alfresco.repo.audit.inbound";
    
    private static Log logger = LogFactory.getLog(AuditComponentImpl.class);
    private static Log loggerInbound = LogFactory.getLog(INBOUND_LOGGER);

    private AuditModelRegistryImpl auditModelRegistry;
    private PropertyValueDAO propertyValueDAO;
    private AuditDAO auditDAO;
    private TransactionService transactionService;
    
    /**
     * Default constructor
     */
    public AuditComponentImpl()
    {
    }
    
    /**
     * Set the registry holding the audit models
     * @since 3.2
     */
    public void setAuditModelRegistry(AuditModelRegistryImpl auditModelRegistry)
    {
        this.auditModelRegistry = auditModelRegistry;
    }

    /**
     * Set the DAO for manipulating property values
     * @since 3.2
     */
    public void setPropertyValueDAO(PropertyValueDAO propertyValueDAO)
    {
        this.propertyValueDAO = propertyValueDAO;
    }
    
    /**
     * Set the DAO for accessing audit data
     * @since 3.2
     */
    public void setAuditDAO(AuditDAO auditDAO)
    {
        this.auditDAO = auditDAO;
    }

    /**
     * Set the service used to start new transactions
     */
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    /**
     * {@inheritDoc}
     * @since 3.2
     */
    public int deleteAuditEntries(String applicationName, Long fromTime, Long toTime)
    {
        ParameterCheck.mandatory("applicationName", applicationName);
        AlfrescoTransactionSupport.checkTransactionReadState(true);
        
        AuditApplication application = auditModelRegistry.getAuditApplicationByName(applicationName);
        if (application == null)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("No audit application named '" + applicationName + "' has been registered.");
            }
            return 0;
        }
        
        Long applicationId = application.getApplicationId();
        
        int deleted = auditDAO.deleteAuditEntries(applicationId, fromTime, toTime);
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug(
                    "Delete audit " + deleted + " entries for " + applicationName + 
                    " (" + fromTime + " to " + toTime);
        }
        return deleted;
    }

    /**
     * {@inheritDoc}
     * @since 3.2
     */
    @Override
    public int deleteAuditEntries(List<Long> auditEntryIds)
    {
        // Shortcut, if necessary
        if (auditEntryIds.size() == 0)
        {
            return 0;
        }
        return auditDAO.deleteAuditEntries(auditEntryIds);
    }

    /**
     * @param application       the audit application object
     * @return                  Returns a copy of the set of disabled paths associated with the application
     */
    @SuppressWarnings("unchecked")
    private Set<String> getDisabledPaths(AuditApplication application)
    {
        try
        {
            Long disabledPathsId = application.getDisabledPathsId();
            Set<String> disabledPaths = (Set<String>) propertyValueDAO.getPropertyById(disabledPathsId);
            return new HashSet<String>(disabledPaths);
        }
        catch (Throwable e)
        {
            // Might be an invalid ID, somehow
            auditModelRegistry.loadAuditModels();
            throw new AlfrescoRuntimeException("Unabled to get AuditApplication disabled paths: " + application, e);
        }
    }

    /**
     * {@inheritDoc}
     * @since 3.2
     */
    public boolean isAuditEnabled()
    {
        return auditModelRegistry.isAuditEnabled();                
    }

    /**
     * {@inheritDoc}
     * @since 3.4
     */
    @Override
    public void setAuditEnabled(boolean enable)
    {
        boolean alreadyEnabled = auditModelRegistry.isAuditEnabled();
        if (alreadyEnabled != enable)
        {
            // It is changing
            auditModelRegistry.stop();
            auditModelRegistry.setProperty(
                        AuditModelRegistry.AUDIT_PROPERTY_AUDIT_ENABLED,
                        Boolean.toString(enable).toLowerCase());
            auditModelRegistry.start();
        }
    }

    /**
     * {@inheritDoc}
     * @since 3.4
     */
    public Map<String, AuditApplication> getAuditApplications()
    {
        return auditModelRegistry.getAuditApplications();
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Note that if DEBUG is on for the the {@link #INBOUND_LOGGER}, then <tt>true</tt>
     * will always be returned.
     * 
     * @since 3.2
     */
    public boolean areAuditValuesRequired()
    {
        return
            (loggerInbound.isDebugEnabled()) ||
            (isAuditEnabled() && !auditModelRegistry.getAuditPathMapper().isEmpty());
    }
    
    /**
     * {@inheritDoc}
     * @since 3.2
     */
    public boolean isAuditPathEnabled(String applicationName, String path)
    {
        ParameterCheck.mandatory("applicationName", applicationName);
        AlfrescoTransactionSupport.checkTransactionReadState(false);
        
        AuditApplication application = auditModelRegistry.getAuditApplicationByName(applicationName);
        if (application == null)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("No audit application named '" + applicationName + "' has been registered.");
            }
            return false;
        }
        // Ensure that the path gets a valid value
        if (path == null)
        {
            path = AuditApplication.AUDIT_PATH_SEPARATOR + application.getApplicationKey();
        }
        else
        {
            // Check the path against the application
            application.checkPath(path);
        }

        Set<String> disabledPaths = getDisabledPaths(application);
        
        // Check if there are any entries that match or supercede the given path
        String disablingPath = null;;
        for (String disabledPath : disabledPaths)
        {
            if (path.startsWith(disabledPath))
            {
                disablingPath = disabledPath;
                break;
            }
        }
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug(
                    "Audit path enabled check: \n" +
                    "   Application:    " + applicationName + "\n" +
                    "   Path:           " + path + "\n" +
                    "   Disabling Path: " + disablingPath);
        }
        return disablingPath == null;
    }

    /**
     * {@inheritDoc}
     * @since 3.2
     */
    public void enableAudit(String applicationName, String path)
    {
        ParameterCheck.mandatory("applicationName", applicationName);
        AlfrescoTransactionSupport.checkTransactionReadState(true);
        
        AuditApplication application = auditModelRegistry.getAuditApplicationByName(applicationName);
        if (application == null)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("No audit application named '" + applicationName + "' has been registered.");
            }
            return;
        }
        // Ensure that the path gets a valid value
        if (path == null)
        {
            path = AuditApplication.AUDIT_PATH_SEPARATOR + application.getApplicationKey();
        }
        else
        {
            // Check the path against the application
            application.checkPath(path);
        }

        Long disabledPathsId = application.getDisabledPathsId();
        Set<String> disabledPaths = getDisabledPaths(application);
        
        // Remove any paths that start with the given path
        boolean changed = false;
        Iterator<String> iterateDisabledPaths = disabledPaths.iterator();
        while (iterateDisabledPaths.hasNext())
        {
            String disabledPath = iterateDisabledPaths.next();
            if (disabledPath.startsWith(path))
            {
                iterateDisabledPaths.remove();
                changed = true;
            }
        }
        // Persist, if necessary
        if (changed)
        {
            propertyValueDAO.updateProperty(disabledPathsId, (Serializable) disabledPaths);
            if (logger.isDebugEnabled())
            {
                logger.debug(
                        "Audit disabled paths updated: \n" +
                        "   Application: " + applicationName + "\n" +
                        "   Disabled:    " + disabledPaths);
            }
        }
        // Done
    }

    /**
     * {@inheritDoc}
     * @since 3.2
     */
    public void disableAudit(String applicationName, String path)
    {
        ParameterCheck.mandatory("applicationName", applicationName);
        AlfrescoTransactionSupport.checkTransactionReadState(true);
        
        AuditApplication application = auditModelRegistry.getAuditApplicationByName(applicationName);
        if (application == null)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("No audit application named '" + applicationName + "' has been registered.");
            }
            return;
        }
        // Ensure that the path gets a valid value
        if (path == null)
        {
            path = AuditApplication.AUDIT_PATH_SEPARATOR + application.getApplicationKey();
        }
        else
        {
            // Check the path against the application
            application.checkPath(path);
        }
        
        Long disabledPathsId = application.getDisabledPathsId();
        Set<String> disabledPaths = getDisabledPaths(application);
        
        // Shortcut if the disabled paths contain the exact path
        if (disabledPaths.contains(path))
        {
            if (logger.isDebugEnabled())
            {
                logger.debug(
                        "Audit disable path already present: \n" +
                        "   Path:       " + path);
            }
            return;
        }
        
        // Bring the set up to date by stripping out unwanted paths
        Iterator<String> iterateDisabledPaths = disabledPaths.iterator();
        while (iterateDisabledPaths.hasNext())
        {
            String disabledPath = iterateDisabledPaths.next();
            if (disabledPath.startsWith(path))
            {
                // We will be superceding this
                iterateDisabledPaths.remove();
            }
            else if (path.startsWith(disabledPath))
            {
                // There is already a superceding path
                if (logger.isDebugEnabled())
                {
                    logger.debug(
                            "Audit disable path superceded: \n" +
                            "   Path:          " + path + "\n" +
                            "   Superceded by: " + disabledPath);
                }
                return;
            }
        }
        // Add our path in
        disabledPaths.add(path);
        // Upload the new set
        propertyValueDAO.updateProperty(disabledPathsId, (Serializable) disabledPaths);
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug(
                    "Audit disabled paths updated: \n" +
                    "   Application: " + applicationName + "\n" +
                    "   Disabled:    " + disabledPaths);
        }
    }

    /**
     * {@inheritDoc}
     * @since 3.2
     */
    public void resetDisabledPaths(String applicationName)
    {
        ParameterCheck.mandatory("applicationName", applicationName);
        AlfrescoTransactionSupport.checkTransactionReadState(true);
        
        AuditApplication application = auditModelRegistry.getAuditApplicationByName(applicationName);
        if (application == null)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("No audit application named '" + applicationName + "' has been registered.");
            }
            return;
        }
        Long disabledPathsId = application.getDisabledPathsId();
        propertyValueDAO.updateProperty(disabledPathsId, (Serializable) Collections.emptySet());
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug("Removed all disabled paths for application " + applicationName);
        }
    }

    /**
     * {@inheritDoc}
     * @since 3.2
     */
    public Map<String, Serializable> recordAuditValues(String rootPath, Map<String, Serializable> values)
    {
        ParameterCheck.mandatory("rootPath", rootPath);
        AuditApplication.checkPathFormat(rootPath);
        
        if (values == null || values.isEmpty() || !areAuditValuesRequired())
        {
            return Collections.emptyMap();
        }
        
        // Log inbound values
        if (loggerInbound.isDebugEnabled())
        {
            StringBuilder sb = new StringBuilder(values.size()*64);
            sb.append("\n")
              .append("Inbound audit values:");
            for (Map.Entry<String, Serializable> entry : values.entrySet())
            {
                String pathElement = entry.getKey();
                String path = AuditApplication.buildPath(rootPath, pathElement);
                Serializable value = entry.getValue();
                sb.append("\n\t").append(path).append("=").append(value);
            }
            loggerInbound.debug(sb.toString());
        }

        // Build the key paths using the session root path
        Map<String, Serializable> pathedValues = new HashMap<String, Serializable>(values.size() * 2);
        for (Map.Entry<String, Serializable> entry : values.entrySet())
        {
            String pathElement = entry.getKey();
            String path = AuditApplication.buildPath(rootPath, pathElement);
            pathedValues.put(path, entry.getValue());
        }
        
        // Translate the values map
        PathMapper pathMapper = auditModelRegistry.getAuditPathMapper();
        final Map<String, Serializable> mappedValues = pathMapper.convertMap(pathedValues);
        if (mappedValues.isEmpty())
        {
            return mappedValues;
        }
        
        // We have something to record.  Start a transaction, if necessary
        TxnReadState txnState = AlfrescoTransactionSupport.getTransactionReadState();
        switch (txnState)
        {
        case TXN_NONE:
        case TXN_READ_ONLY:
            // New transaction
            RetryingTransactionCallback<Map<String, Serializable>> callback =
                    new RetryingTransactionCallback<Map<String,Serializable>>()
            {
                public Map<String, Serializable> execute() throws Throwable
                {
                    return recordAuditValuesImpl(mappedValues);
                }
            };
            return transactionService.getRetryingTransactionHelper().doInTransaction(callback, false, true);
        case TXN_READ_WRITE:
            return recordAuditValuesImpl(mappedValues);
        default:
            throw new IllegalStateException("Unknown txn state: " + txnState);
        }
    }
    
    /**
     * {@inheritDoc}
     * @since 3.2
     */
    public Map<String, Serializable> recordAuditValuesImpl(Map<String, Serializable> mappedValues)
    {
        // Group the values by root path
        Map<String, Map<String, Serializable>> mappedValuesByRootKey = new HashMap<String, Map<String,Serializable>>();
        for (Map.Entry<String, Serializable> entry : mappedValues.entrySet())
        {
            String path = entry.getKey();
            String rootKey = AuditApplication.getRootKey(path);
            Map<String, Serializable> rootKeyMappedValues = mappedValuesByRootKey.get(rootKey);
            if (rootKeyMappedValues == null)
            {
                rootKeyMappedValues = new HashMap<String, Serializable>(7);
                mappedValuesByRootKey.put(rootKey, rootKeyMappedValues);
            }
            rootKeyMappedValues.put(path, entry.getValue());
        }

        Map<String, Serializable> allAuditedValues = new HashMap<String, Serializable>(mappedValues.size()*2+1);
        // Now audit for each of the root keys
        for (Map.Entry<String, Map<String, Serializable>> entry : mappedValuesByRootKey.entrySet())
        {
            String rootKey = entry.getKey();
            Map<String, Serializable> rootKeyMappedValues = entry.getValue();
            // Get the application
            AuditApplication application = auditModelRegistry.getAuditApplicationByKey(rootKey);
            if (application == null)
            {
                // There is no application that uses the root key
                logger.debug(
                        "There is no application for root key: " + rootKey);
                continue;
            }
            // Get the disabled paths
            Set<String> disabledPaths = getDisabledPaths(application);
            // Do a quick elimination if the root path is disabled
            if (disabledPaths.contains(AuditApplication.buildPath(rootKey)))
            {
                // The root key has been disabled for this application
                if (logger.isDebugEnabled())
                {
                    logger.debug(
                            "Audit values root path has been excluded by disabled paths: \n" +
                            "   Application: " + application + "\n" +
                            "   Root Path:   " + AuditApplication.buildPath(rootKey));
                }
                continue;
            }
            // Do the audit
            Map<String, Serializable> rootKeyAuditValues = audit(application, disabledPaths, rootKeyMappedValues);
            allAuditedValues.putAll(rootKeyAuditValues);
        }
        // Done
        return allAuditedValues;
    }

    /**
     * Audit values for a given application.  No path checking is done.
     * 
     * @param application           the audit application to audit to
     * @param disabledPaths         the application's disabled paths
     * @param values                the values to store keyed by <b>full paths</b>.
     * @return                      Returns all values as audited
     */
    private Map<String, Serializable> audit(
            final AuditApplication application,
            Set<String> disabledPaths,
            final Map<String, Serializable> values)
    {
        // Get the model ID for the application
        Long applicationId = application.getApplicationId();
        if (applicationId == null)
        {
            throw new AuditException("No persisted instance exists for audit application: " + application);
        }

        // Check if there is anything to audit
        if (values.size() == 0)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug(
                        "Audit values have all been excluded by disabled paths: \n" +
                        "   Application: " + application + "\n" +
                        "   Values:      " + values);
            }
            return Collections.emptyMap();
        }

        Set<String> generatorKeys = values.keySet();
        // Eliminate any paths that have been disabled
        Iterator<String> generatorKeysIterator = generatorKeys.iterator();
        while(generatorKeysIterator.hasNext())
        {
            String generatorKey = generatorKeysIterator.next();
            for (String disabledPath : disabledPaths)
            {
                if (generatorKey.startsWith(disabledPath))
                {
                    // The pathed value is excluded
                    generatorKeysIterator.remove();
                }
            }
        }
        
        // Generate data
        Map<String, DataGenerator> generators = application.getDataGenerators(generatorKeys);
        Map<String, Serializable> auditData = generateData(generators);
        
        // Now extract values
        Map<String, Serializable> extractedData = AuthenticationUtil.runAs(new RunAsWork<Map<String, Serializable>>()
        {
            public Map<String, Serializable> doWork() throws Exception
            {
                return extractData(application, values);
            }
        }, AuthenticationUtil.getSystemUserName());
        
        // Combine extracted and generated values (extracted data takes precedence)
        auditData.putAll(extractedData);

        // Time and username are intrinsic
        long time = System.currentTimeMillis();
        String username = AuthenticationUtil.getFullyAuthenticatedUser();
        
        Long entryId = null;
        if (!auditData.isEmpty())
        {
            // Persist the values
            entryId = auditDAO.createAuditEntry(applicationId, time, username, auditData);
            // Done
            if (logger.isDebugEnabled())
            {
                StringBuilder sb = new StringBuilder();
                sb.append(
                        "\nNew audit entry: \n" +
                        "\tApplication ID: " + applicationId + "\n" +
                        "\tEntry ID:       " + entryId + "\n" +
                        "\tValues:         " + "\n");
                for (Map.Entry<String, Serializable> entry : values.entrySet())
                {
                    sb.append("\t\t").append(entry).append("\n");
                }
                sb.append("\n\tAudit Data: \n");
                for (Map.Entry<String, Serializable> entry : auditData.entrySet())
                {
                    sb.append("\t\t").append(entry).append("\n");
                }
                logger.debug(sb.toString());
            }
        }
        else
        {
            // Done ... nothing
            if (logger.isDebugEnabled())
            {
                StringBuilder sb = new StringBuilder();
                sb.append(
                        "\nNothing audited: \n" +
                        "\tApplication ID: " + applicationId + "\n" +
                        "\tEntry ID:       " + entryId + "\n" +
                        "\tValues:         " + "\n");
                for (Map.Entry<String, Serializable> entry : values.entrySet())
                {
                    sb.append("\t\t").append(entry).append("\n");
                }
                logger.debug(sb.toString());
            }
        }
        
        return auditData;
    }
    
    /**
     * Extracts data from a given map using data extractors from the given application.
     * 
     * @param application           the application providing the data extractors
     * @param values                the data values from which to generate data
     * @return                      Returns a map of derived data keyed by full path
     * 
     * @since 3.2
     */
    private Map<String, Serializable> extractData(
            AuditApplication application,
            Map<String, Serializable> values)
    {
        Map<String, Serializable> newData = new HashMap<String, Serializable>(values.size());
        
        List<DataExtractorDefinition> extractors = application.getDataExtractors();
        for (DataExtractorDefinition extractorDef : extractors)
        {
            DataExtractor extractor = extractorDef.getDataExtractor();
            String triggerPath = extractorDef.getDataTrigger();
            String sourcePath = extractorDef.getDataSource();
            String targetPath = extractorDef.getDataTarget();
            
            // Check if it is triggered
            if (!values.containsKey(triggerPath))
            {
                continue;               // It is not triggered
            }
            
            // We observe the key, not the actual value
            if (!values.containsKey(sourcePath))
            {
                continue;               // There is no data to extract
            }
            
            Serializable value = values.get(sourcePath);
            
            // Check if the extraction is supported
            if (!extractor.isSupported(value))
            {
                continue;
            }
            // Use the extractor to pull the value out
            final Serializable data;
            try
            {
                data = extractor.extractData(value);
            }
            catch (Throwable e)
            {
                throw new AlfrescoRuntimeException(
                        "Failed to extract audit data: \n" +
                        "   Path:      " + sourcePath + "\n" +
                        "   Raw value: " + value + "\n" +
                        "   Extractor: " + extractor,
                        e);
            }
            // Add it to the map
            newData.put(targetPath, data);
        }
        // Done
        if (logger.isDebugEnabled())
        {
            StringBuilder sb = new StringBuilder();
            sb.append(
                    "\nExtracted audit data: \n" +
                    "\tApplication:    " + application + "\n" +
                    "\tValues:         " + "\n");
            for (Map.Entry<String, Serializable> entry : values.entrySet())
            {
                sb.append("\t\t").append(entry).append("\n");
            }
            sb.append("\n\tNew Data: \n");
            for (Map.Entry<String, Serializable> entry : newData.entrySet())
            {
                sb.append("\t\t").append(entry).append("\n");
            }
            logger.debug(sb.toString());
        }
        return newData;
    }
    
    /**
     * @param generators            the data generators
     * @return                      Returns a map of generated data keyed by full path
     * 
     * @since 3.2
     */
    private Map<String, Serializable> generateData(Map<String, DataGenerator> generators)
    {
        Map<String, Serializable> newData = new HashMap<String, Serializable>(generators.size() + 5);
        for (Map.Entry<String, DataGenerator> entry : generators.entrySet())
        {
            String path = entry.getKey();
            DataGenerator generator = entry.getValue();
            final Serializable data;
            try
            {
                data = generator.getData();
            }
            catch (Throwable e)
            {
                throw new AlfrescoRuntimeException(
                        "Failed to generate audit data: \n" +
                        "   Path:      " + path + "\n" +
                        "   Generator: " + generator,
                        e);
            }
            // Add it to the map
            newData.put(path, data);
        }
        // Done
        return newData;
    }

    /**
     * {@inheritDoc}
     */
    public void auditQuery(AuditQueryCallback callback, AuditQueryParameters parameters, int maxResults)
    {
        ParameterCheck.mandatory("callback", callback);
        ParameterCheck.mandatory("parameters", parameters);
        
        // Shortcuts
        if (parameters.isZeroResultQuery())
        {
            return;
        }
        
        auditDAO.findAuditEntries(callback, parameters, maxResults);
    }
}
