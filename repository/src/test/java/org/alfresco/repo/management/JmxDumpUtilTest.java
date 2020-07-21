/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.repo.management;

import static org.junit.Assert.assertArrayEquals;

import java.util.regex.Pattern;

import org.alfresco.util.ApplicationContextHelper;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import junit.framework.TestCase;

public class JmxDumpUtilTest extends TestCase
{
    public void testUpdateOSNameAttribute() throws Exception
    {
        ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
        String osName = System.getProperty("os.name");
        if (osName.toLowerCase().startsWith("linux"))
        {
            String attr = JmxDumpUtil.updateOSNameAttributeForLinux(osName);
            assertTrue(attr.toLowerCase().startsWith("linux ("));
        }
    }

    @Test
    public void testCleanPasswordsFromInputArgument() throws Exception
    {
        Pattern pattern = Pattern.compile("(?i)(.*(password=|pwd=|token=))((?<=password=|pwd=|token=).*+)");
        String passwordArg = "-Ddb.password=I should be stars \"£$%^&*()@";
        String expected = "-Ddb.password="+JmxDumpUtil.PROTECTED_VALUE;
        String actual = JmxDumpUtil.cleanPasswordFromInputArgument(passwordArg,pattern);
        assertEquals("Expectected output: "+ expected +" Actual output: "+actual, expected, actual);

        passwordArg = "-Ddb.paSsword=@";
        expected = "-Ddb.paSsword="+JmxDumpUtil.PROTECTED_VALUE;
        actual = JmxDumpUtil.cleanPasswordFromInputArgument(passwordArg, pattern);
        assertEquals("Expectected output: "+ expected +" Actual output: "+actual, expected, actual);

        passwordArg = "somePrefix.token=\"If i'm not replaced, something has gone very wrong\"";
        expected = "somePrefix.token="+JmxDumpUtil.PROTECTED_VALUE;
        actual = JmxDumpUtil.cleanPasswordFromInputArgument(passwordArg, pattern);
        assertEquals("Expectected output: "+ expected +" Actual output: "+actual, expected, actual);

        passwordArg = "yetanotherpwd=";
        expected = "yetanotherpwd="+JmxDumpUtil.PROTECTED_VALUE;
        actual = JmxDumpUtil.cleanPasswordFromInputArgument(passwordArg, pattern);
        assertEquals("Expectected output: "+ expected +" Actual output: "+actual, expected, actual);

        passwordArg = "AnyOtherArgument=\"I should still be here\"";
        expected = "AnyOtherArgument=\"I should still be here\"";
        actual = JmxDumpUtil.cleanPasswordFromInputArgument(passwordArg, pattern);
        assertEquals("Expectected output :"+ expected +" Actual output :"+actual, expected, actual);

    }
    @Test
    public void testCleanPasswordsFromInputArguments() throws Exception
    {
        String[] argEndingsTypical = {"password", "token","pwd"};
        String[] args = {"-Ddb.password=alfresco", "-Ddb.user=alfresco", "-DtestToken=asdoij3ifiej22244ojpgkmkfpsi3j55643pojpdjoismvi4563625mposvsd"};
        String[] expectedArray = {"-Ddb.password="+JmxDumpUtil.PROTECTED_VALUE, "-Ddb.user=alfresco", "-DtestToken="+JmxDumpUtil.PROTECTED_VALUE};
        String[] actualArray = JmxDumpUtil.cleanPasswordsFromInputArguments(args, argEndingsTypical);
        assertArrayEquals("Expectected output:"+expectedArray+" Actual output:"+actualArray,expectedArray,actualArray);

        args = new String[]{"-Ddb.port=1234", "-Ddb.user=alfresco", "-DtestArg=Test1234password"};
        expectedArray = new String[]{"-Ddb.port=1234", "-Ddb.user=alfresco", "-DtestArg=Test1234password"};
        actualArray = JmxDumpUtil.cleanPasswordsFromInputArguments(args, argEndingsTypical);
        assertArrayEquals("Expectected output:"+expectedArray+" Actual output:"+actualArray, expectedArray, actualArray);
        
        

    }
    @Test
    public void testCreatePasswordFindRegexString() throws Exception
    {
        String[] argEndings = {"password", "any old ending :D", "token"};
        String expected = "(?i)(.*(password=|any old ending :D=|token=))((?<=password=|any old ending :D=|token=).*+)";
        String actual = JmxDumpUtil.createPasswordFindRegexString(argEndings);
        assertEquals("Expectected output :"+expected+" Actual output :"+actual,expected, actual);

        String[] argEndings2 = {"?", "\"£$%^&*"};
        expected = "(?i)(.*(\\?=|\"£$%^&\\*=))((?<=\\?=|\"£$%^&\\*=).*+)";
        actual = JmxDumpUtil.createPasswordFindRegexString(argEndings2);
        assertEquals("Expectected output :"+expected+" Actual output :"+actual,expected, actual);   
        
        String[] emptyArgs = {};
        try 
        {
            JmxDumpUtil.createPasswordFindRegexString(emptyArgs);
            fail("expected exception was not occured.");
        } catch(IllegalArgumentException e) 
        {
            
        }
    }
    @Test
    public void testEscapeRegexMetaChars()
    {
        String input = "|?*+.";
        String expected = "\\|\\?\\*\\+\\.";
        String actual = JmxDumpUtil.escapeRegexMetaChars(input);
        assertEquals("Expectected output :" + expected + " Actual output :" + actual, expected, actual);

        input = "Let's.Add++,*complexity?!\"";
        expected = "Let's\\.Add\\+\\+,\\*complexity\\?!\"";
        actual = JmxDumpUtil.escapeRegexMetaChars(input);
        assertEquals("Expectected output :" + expected + " Actual output :" + actual, expected, actual);
    }
}
