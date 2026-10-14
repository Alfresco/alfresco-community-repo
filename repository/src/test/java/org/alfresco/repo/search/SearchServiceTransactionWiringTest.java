/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2026 Alfresco Software Limited
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
package org.alfresco.repo.search;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Properties;

import org.junit.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.interceptor.TransactionAttribute;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;

/**
 * Pins the rollback rule on the {@code SearchService_transaction} interceptor.
 *
 * <p>
 * {@link SearchService} is read-only but joins the caller's transaction, so without the rule any failure it throws marks that transaction rollback-only and the caller's own work is silently discarded at commit.
 * </p>
 */
public class SearchServiceTransactionWiringTest
{
    private static final String CONTEXT_XML = "alfresco/public-services-context.xml";
    private static final String TRANSACTION_PROPERTIES = "alfresco/domain/transaction.properties";
    private static final String INTERCEPTOR = "SearchService_transaction";

    @Test
    public void searchFailuresMustNotRollBackTheCallersTransaction() throws Exception
    {
        TransactionAttribute attribute = searchServiceTransactionAttribute();

        assertNoRollback(attribute, "Elasticsearch unreachable",
                new RuntimeException(new IOException("elasticsearch: Name or service not known")));
        assertNoRollback(attribute, "unknown query language",
                new SearcherException("Unknown query language: chupacabra"));
        assertNoRollback(attribute, "Solr unreachable",
                new QueryParserException("", new IOException("solr: Name or service not known")));
        assertNoRollback(attribute, "unsupported search feature",
                new UnsupportedOperationException());
    }

    /** A failed read leaves the caller's work valid whether it failed with an Exception or an Error, and {@code catch (Throwable ...)} callers are equally exposed. */
    @Test
    public void errorsMustNotRollBackTheCallersTransactionEither() throws Exception
    {
        TransactionAttribute attribute = searchServiceTransactionAttribute();

        assertNoRollback(attribute, "Error raised during a search", new OutOfMemoryError());
        assertNoRollback(attribute, "linkage failure during a search", new LinkageError());
    }

    @Test
    public void searchMustKeepParticipatingInTheCallersTransaction() throws Exception
    {
        TransactionAttribute attribute = searchServiceTransactionAttribute();

        for (int suspending : new int[]{
                TransactionDefinition.PROPAGATION_REQUIRES_NEW,
                TransactionDefinition.PROPAGATION_NOT_SUPPORTED,
                TransactionDefinition.PROPAGATION_NEVER})
        {
            assertNotEquals("Transactional metadata queries run on the caller's connection, so"
                    + " suspending the transaction would silently stop them seeing the caller's"
                    + " uncommitted changes",
                    suspending, attribute.getPropagationBehavior());
        }
    }

    @Test
    public void bothPublicSearchBeansUseTheInterceptor()
    {
        DefaultListableBeanFactory factory = loadFactory();

        for (String bean : new String[]{"SearchService", "ADMSearchService"})
        {
            Object interceptorNames = factory.getBeanDefinition(bean)
                    .getPropertyValues()
                    .getPropertyValue("interceptorNames")
                    .getValue();
            assertTrue(bean + " must be advised by " + INTERCEPTOR,
                    String.valueOf(interceptorNames).contains(INTERCEPTOR));
        }
    }

    private static void assertNoRollback(TransactionAttribute attribute, String what, Throwable failure)
    {
        assertFalse("A search that fails with " + what + " (" + failure.getClass().getName() + ")"
                + " must not mark the caller's transaction rollback-only; the caller's own work"
                + " would be silently discarded at commit. Check the rollback rule on "
                + INTERCEPTOR + " in " + CONTEXT_XML,
                attribute.rollbackOn(failure));
    }

    private static TransactionAttribute searchServiceTransactionAttribute() throws Exception
    {
        DefaultListableBeanFactory factory = loadFactory();
        resolvePlaceholders(factory);

        TransactionInterceptor interceptor = factory.getBean(INTERCEPTOR, TransactionInterceptor.class);
        Method query = SearchService.class.getMethod("query", SearchParameters.class);

        return interceptor.getTransactionAttributeSource().getTransactionAttribute(query, SearchService.class);
    }

    /** Only the interceptor is ever instantiated, so its transaction manager is the one dependency needing a stub. */
    private static DefaultListableBeanFactory loadFactory()
    {
        DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
        new XmlBeanDefinitionReader(factory).loadBeanDefinitions(new ClassPathResource(CONTEXT_XML));
        factory.registerSingleton("transactionManager", mock(PlatformTransactionManager.class));
        return factory;
    }

    private static void resolvePlaceholders(DefaultListableBeanFactory factory) throws IOException
    {
        Properties properties = new Properties();
        try (var stream = new ClassPathResource(TRANSACTION_PROPERTIES).getInputStream())
        {
            properties.load(stream);
        }

        PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
        configurer.setIgnoreUnresolvablePlaceholders(true);
        configurer.setProperties(properties);
        configurer.postProcessBeanFactory(factory);
    }
}
