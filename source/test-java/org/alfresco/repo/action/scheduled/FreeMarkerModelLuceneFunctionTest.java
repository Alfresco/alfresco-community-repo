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
package org.alfresco.repo.action.scheduled;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.TemplateException;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;
import org.springframework.extensions.surf.util.ISO8601DateFormat;

/**
 * Test that the correct date ranges are generated for lucene 
 * 
 * @author Andy Hind
 */
public class FreeMarkerModelLuceneFunctionTest extends TestCase
{
    //private static SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sssZ");
    private static SimpleDateFormat SDF2 = new SimpleDateFormat("yyyy-MM-dd");
    
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    private AuthenticationComponent authenticationComponent;
    private ServiceRegistry serviceRegistry;
    private UserTransaction tx;

    /**
     * 
     *
     */
    public FreeMarkerModelLuceneFunctionTest()
    {
        super();
    }

    /**
     * 
     * @param arg0
     */
    public FreeMarkerModelLuceneFunctionTest(String arg0)
    {
        super(arg0);
    }

    public void setUp() throws Exception
    {

        authenticationComponent = (AuthenticationComponent) ctx.getBean("authenticationComponent");
        serviceRegistry = (ServiceRegistry) ctx.getBean("ServiceRegistry");

        this.authenticationComponent.setSystemUserAsCurrentUser();

        TransactionService transactionService = (TransactionService) ctx.getBean(ServiceRegistry.TRANSACTION_SERVICE
                .getLocalName());
        tx = transactionService.getUserTransaction();
        tx.begin();
    }

    @Override
    protected void tearDown() throws Exception
    {
        authenticationComponent.clearCurrentSecurityContext();
        tx.rollback();
        super.tearDown();
    }

    /**
     * Test date formatting
     *
     */
    public void testDate()
    {
        String template = "${date?date?string(\"yyyy-MM-dd\")}";
        FreeMarkerWithLuceneExtensionsModelFactory mf = new FreeMarkerWithLuceneExtensionsModelFactory();
        mf.setServiceRegistry(serviceRegistry);
        String result = serviceRegistry.getTemplateService().processTemplateString("freemarker", template, mf.getModel());
        assertEquals(result, SDF2.format(new Date()));
    }
    
    /**
     * Test generation of lucene date ranges
     *
     */
    public void testLuceneDateRangeFunction()
    {
        GregorianCalendar cal = new GregorianCalendar();
        cal.set(Calendar.YEAR, 2001);
        cal.set(Calendar.MONTH, 1);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        String isoStartDate = ISO8601DateFormat.format(cal.getTime());
        cal.add(Calendar.DAY_OF_MONTH, 1);
        String isoEndDate = ISO8601DateFormat.format(cal.getTime());
        String template = "${luceneDateRange(\""+isoStartDate+"\", \"P1D\")}";
        FreeMarkerWithLuceneExtensionsModelFactory mf = new FreeMarkerWithLuceneExtensionsModelFactory();
        mf.setServiceRegistry(serviceRegistry);
        String result = serviceRegistry.getTemplateService().processTemplateString("freemarker", template, mf.getModel());
        assertEquals(result, "["+isoStartDate+" TO "+isoEndDate+"]");
    }
    
    /**
     * Test generation of lucene date ranges
     *
     */
    public void testLuceneDateRangeFunctionToDate()
    {
        GregorianCalendar cal = new GregorianCalendar();
        cal.set(Calendar.YEAR, 2001);
        cal.set(Calendar.MONTH, 1);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        String isoStartDate = ISO8601DateFormat.format(cal.getTime());
        cal.add(Calendar.DAY_OF_MONTH, 4);
        String isoEndDate = ISO8601DateFormat.format(cal.getTime());
        String template = "${luceneDateRange(\""+isoStartDate+"\", \""+isoEndDate+"\")}";
        FreeMarkerWithLuceneExtensionsModelFactory mf = new FreeMarkerWithLuceneExtensionsModelFactory();
        mf.setServiceRegistry(serviceRegistry);
        String result = serviceRegistry.getTemplateService().processTemplateString("freemarker", template, mf.getModel());
        assertEquals(result, "["+isoStartDate+" TO "+isoEndDate+"]");
    }
    
    /**
     * Test generation of lucene date ranges
     *
     */
    public void testLuceneDateRangeFunctionTodayPlus4()
    {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(new Date());
        //cal.set(Calendar.AM_PM, Calendar.AM);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        String isoStartDate = ISO8601DateFormat.format(cal.getTime());
        cal.add(Calendar.DAY_OF_MONTH, 4);
        String isoEndDate = ISO8601DateFormat.format(cal.getTime());
        String template = "${luceneDateRange(today, \"P4D\")}";
        FreeMarkerWithLuceneExtensionsModelFactory mf = new FreeMarkerWithLuceneExtensionsModelFactory();
        mf.setServiceRegistry(serviceRegistry);
        String result = serviceRegistry.getTemplateService().processTemplateString("freemarker", template, mf.getModel());
        assertNotNull(result);
        assertEquals(result, "["+isoStartDate+" TO "+isoEndDate+"]");
    }
    
    /**
     * Test generation of lucene date ranges
     *
     */
    public void testLuceneDateRangeFunctionTodayMinus4()
    {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(new Date());
        //cal.set(Calendar.AM_PM, Calendar.AM);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        String isoStartDate = ISO8601DateFormat.format(cal.getTime());
        cal.add(Calendar.DAY_OF_MONTH, -4);
        String isoEndDate = ISO8601DateFormat.format(cal.getTime());
        String template = "${luceneDateRange(today, \"-P4D\")}";
        FreeMarkerWithLuceneExtensionsModelFactory mf = new FreeMarkerWithLuceneExtensionsModelFactory();
        mf.setServiceRegistry(serviceRegistry);
        String result = serviceRegistry.getTemplateService().processTemplateString("freemarker", template, mf.getModel());
        assertEquals(result, "["+isoEndDate+" TO "+isoStartDate+"]");
    }
    
    /**
     * Test generation of lucene date ranges
     *
     */
    public void testLuceneDateRangeFunctionTodayToday()
    {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(new Date());
        //cal.set(Calendar.AM_PM, Calendar.AM);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        String isoStartDate = ISO8601DateFormat.format(cal.getTime());
        String template = "${luceneDateRange(today, today)}";
        FreeMarkerWithLuceneExtensionsModelFactory mf = new FreeMarkerWithLuceneExtensionsModelFactory();
        mf.setServiceRegistry(serviceRegistry);
        String result = serviceRegistry.getTemplateService().processTemplateString("freemarker", template, mf.getModel());
        assertEquals(result, "["+isoStartDate+" TO "+isoStartDate+"]");
    }
    
    /**
     * Tests finding single nodes by a lucene query.
     * Uses a node with a special, known noderef
     */
    public void testSelectSingleNode()
    {
       String renderingSpaceNodeRef = "workspace://SpacesStore/rendering_actions_space";
       
       // Build a selectSingleNode to get the rendering actions space by path
       String rendering_space = "rendering_actions";
       String path = "'PATH:\"/app:company_home/app:dictionary/app:" + rendering_space + "\"'";
       String template = "${selectSingleNode('workspace://SpacesStore', 'lucene', "+path+" )}";
       
       // Evaluate the script
       FreeMarkerWithLuceneExtensionsModelFactory mf = new FreeMarkerWithLuceneExtensionsModelFactory();
       mf.setServiceRegistry(serviceRegistry);
       String result = serviceRegistry.getTemplateService().processTemplateString("freemarker", template, mf.getModel());
       
       // Check we got the magic, known noderef back
       assertEquals(result, renderingSpaceNodeRef);
       
       
       // Now check for a node that doesn't exist
       String invalidSpace = "DOESnotEXISTspace";
       path = "'PATH:\"/app:company_home/app:dictionary/app:" + invalidSpace + "\"'";
       template = "${selectSingleNode('workspace://SpacesStore', 'lucene', "+path+" )}";
       
       // Will fail with "No Nodes Selected"
       try
       {
          result = serviceRegistry.getTemplateService().processTemplateString("freemarker", template, mf.getModel());
          fail("Shouldn't find anything, but got" + result);
       }
       catch(TemplateException e) {}
    }
}