/*
 * Copyright (C) 2005-2012
 Alfresco Software Limited.
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
package org.alfresco.util.test.junitrules;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.rules.ExternalResource;
import org.springframework.context.ApplicationContext;

/**
 * A JUnit rule designed to help with the automatic cleanup of temporary models and to make it easier to
 * create common test models with JUnit code.
 * 
 * @author Alex Miller
 * @since 4.2
 */
public class TemporaryModels extends ExternalResource
{
    private static final Log logger = LogFactory.getLog(TemporaryModels.class);
    
    private final ApplicationContextInit appContextRule;
    
    private final Set<QName> loadedModels = new HashSet<QName>();

    /**
     * Constructs the rule with a reference to a {@link ApplicationContextInit rule} which can be used to retrieve the ApplicationContext.
     * 
     * @param appContextRule a rule which can be used to retrieve the spring app context.
     */
    public TemporaryModels(ApplicationContextInit appContextRule)
    {
        this.appContextRule = appContextRule;
    }
    
    
    @Override protected void before() throws Throwable
    {
        // Intentionally empty
    }
    
    @Override protected void after()
    {
        final RetryingTransactionHelper transactionHelper = getTransactionHelper();
        final DictionaryDAO dictionaryDAO = getDictionaryDAO();
        
        // Run as system to ensure all non-system nodes can be deleted irrespective of which user created them.
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            @Override public Void doWork() throws Exception
            {
                transactionHelper.doInTransaction(new RetryingTransactionCallback<Void>()
                {
                    @Override public Void execute() throws Throwable
                    {
                    	for (QName model : loadedModels)
                    	{
                    		dictionaryDAO.removeModel(model);
                    	}
                    	return null;
                    }
                });
                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
    }


	private RetryingTransactionHelper getTransactionHelper() {
		final ApplicationContext springContext = appContextRule.getApplicationContext();
        
        final RetryingTransactionHelper transactionHelper = springContext.getBean("retryingTransactionHelper", RetryingTransactionHelper.class);
		return transactionHelper;
	}
    
    public QName loadModel(String modelPath, ClassLoader classLoader)
    {
        InputStream modelStream = classLoader.getResourceAsStream(modelPath);
        if (modelStream == null)
        {
            throw new DictionaryException("Could not find bootstrap model " + modelPath);
        }
    	try
    	{
    		return loadModel(modelStream); 
    	}
        finally
        {
            try
            {
                modelStream.close();
            } 
            catch (IOException ioe)
            {
                logger.warn("Failed to close model input stream for '"+modelPath+"': "+ioe);
            }
        }
    }

    public QName loadModel(InputStream modelStream) 
    {
        try
        {
            final M2Model model = M2Model.createModel(modelStream);
            
            return loadModel(model);
        }
        catch(DictionaryException e)
        {
            throw new DictionaryException("Could not import model", e);
        }
	
    }


	private QName loadModel(final M2Model model) {
        if (logger.isDebugEnabled())
        {
            logger.debug("Loading model: "+model.getName());
        }
        
        final DictionaryDAO dictionaryDAO = getDictionaryDAO();
		QName modelQName = dictionaryDAO.putModel(model);
		loadedModels.add(modelQName);
		return modelQName;
	}


	private DictionaryDAO getDictionaryDAO() {
		final ApplicationContext springContext = appContextRule.getApplicationContext();

        DictionaryDAO dictionaryDAO = springContext.getBean("dictionaryDAO", DictionaryDAO.class);
		return dictionaryDAO;
	}
}
