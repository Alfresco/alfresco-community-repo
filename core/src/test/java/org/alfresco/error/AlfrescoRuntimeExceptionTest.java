/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.error;

import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

import junit.framework.TestCase;

import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Alfresco runtime exception test
 * 
 * @author Roy Wetherall
 */
public class AlfrescoRuntimeExceptionTest extends TestCase
{
    private static final String BASE_RESOURCE_NAME = "org.alfresco.i18n.testMessages";
    private static final String PARAM_VALUE = "television";
    private static final String MSG_PARAMS = "msg_params";
    private static final String MSG_ERROR = "msg_error";
    private static final String VALUE_ERROR = "This is an error message. \n  This is on a new line.";
    private static final String VALUE_FR_ERROR = "C'est un message d'erreur. \n  C'est sur une nouvelle ligne.";
    private static final String VALUE_PARAMS = "What no " + PARAM_VALUE + "?";
    private static final String VALUE_FR_PARAMS = "Que non " + PARAM_VALUE + "?";
    private static final String NON_I18NED_MSG = "This is a non i18ned error message.";
    private static final String NON_EXISTING_MSG = "non.existing.msgId";
   
    @Override
    protected void setUp() throws Exception
    {
        // Re-set the current locale to be the default
        Locale.setDefault(Locale.ENGLISH);
        I18NUtil.setLocale(Locale.getDefault());
    }
    
    public void testI18NBehaviour()
    {
        // Ensure that the bundle is present on the classpath
        String baseResourceAsProperty = BASE_RESOURCE_NAME.replace('.', '/') + ".properties";
        URL baseResourceURL = AlfrescoRuntimeExceptionTest.class.getClassLoader().getResource(baseResourceAsProperty);
        assertNotNull(baseResourceURL);
        
        baseResourceAsProperty = BASE_RESOURCE_NAME.replace('.', '/') + "_fr_FR" + ".properties";
        baseResourceURL = AlfrescoRuntimeExceptionTest.class.getClassLoader().getResource(baseResourceAsProperty);
        assertNotNull(baseResourceURL);
        
        // Ensure we can load it as a resource bundle
        ResourceBundle properties = ResourceBundle.getBundle(BASE_RESOURCE_NAME);
        assertNotNull(properties);
        properties = ResourceBundle.getBundle(BASE_RESOURCE_NAME, new Locale("fr", "FR"));
        assertNotNull(properties);
       

        // From here on in, we use Spring
       
        // Register the bundle
        I18NUtil.registerResourceBundle(BASE_RESOURCE_NAME);
        
        AlfrescoRuntimeException exception1 = new AlfrescoRuntimeException(MSG_PARAMS, new Object[]{PARAM_VALUE});
        assertTrue(exception1.getMessage().contains(VALUE_PARAMS));
        AlfrescoRuntimeException exception3 = new AlfrescoRuntimeException(MSG_ERROR);
        assertTrue(exception3.getMessage().contains(VALUE_ERROR));
        
        // Change the locale and re-test
        I18NUtil.setLocale(new Locale("fr", "FR"));
        
        AlfrescoRuntimeException exception2 = new AlfrescoRuntimeException(MSG_PARAMS, new Object[]{PARAM_VALUE});
        assertTrue(exception2.getMessage().contains(VALUE_FR_PARAMS));   
        AlfrescoRuntimeException exception4 = new AlfrescoRuntimeException(MSG_ERROR);
        assertTrue(exception4.getMessage().contains(VALUE_FR_ERROR));  
        
        AlfrescoRuntimeException exception5 = new AlfrescoRuntimeException(NON_I18NED_MSG);
        assertTrue(exception5.getMessage().contains(NON_I18NED_MSG));
        
        // MNT-13028
        String param1 = PARAM_VALUE + "_1";
        String param2 = PARAM_VALUE + "_2";
        String param3 = PARAM_VALUE + "_3";
        AlfrescoRuntimeException exception6 = new AlfrescoRuntimeException(NON_EXISTING_MSG, new Object[]{param1, param2, param3});
        String message6 = exception6.getMessage();
        assertTrue(message6.contains(NON_EXISTING_MSG));
        assertTrue(message6.contains(param1));
        assertTrue(message6.contains(param2));
        assertTrue(message6.contains(param3));
    }
    
    public void testMakeRuntimeException()
    {
        Throwable e = new RuntimeException("sfsfs");
        RuntimeException ee = AlfrescoRuntimeException.makeRuntimeException(e, "Test");
        assertTrue("Exception should not have been changed", ee == e);
        
        e = new Exception();
        ee = AlfrescoRuntimeException.makeRuntimeException(e, "Test");
        assertTrue("Expected an AlfrescoRuntimeException instance", ee instanceof AlfrescoRuntimeException);
    }
}
