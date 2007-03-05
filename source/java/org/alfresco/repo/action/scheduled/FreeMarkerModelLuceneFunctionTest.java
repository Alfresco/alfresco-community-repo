/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
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
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.ISO8601DateFormat;
import org.springframework.context.ApplicationContext;

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

        authenticationComponent = (AuthenticationComponent) ctx.getBean("authenticationComponentImpl");
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
}