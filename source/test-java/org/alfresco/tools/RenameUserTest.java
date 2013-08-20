/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.tools;

import static org.junit.Assert.assertEquals;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Iterator;

import org.alfresco.tools.RenameUser.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for RenameUser. {@link PersonTest} contains integration tests.
 * 
 * @author Alan Davis
 */
public class RenameUserTest
{
    private String[] args;
    private File file;
    private RenameUser renameUser = new RenameUser();

    @Before
    public void setUp() throws Exception
    {
        args = new String[6];
        args[0] = "-user";
        args[1] = "admin";
        args[2] = "-pwd";
        args[3] = "admin";
        args[4] = "oldUsername";
        args[5] = "newUsername";
    }

    @After
    public void tearDown()// throws Exception
    {
        if (file != null && file.exists())
        {
            file.delete();
        }
    }
    
    private void createFile(String content) throws Exception
    {
        file = File.createTempFile("RenameUserTest", ".txt");
        args[4] = "-file";
        args[5] = file.getPath();

        BufferedWriter out = new BufferedWriter(new FileWriter(file));
        out.write(content);
        out.close();
    }

    private void processArgsAndValidate()
    {
        renameUser.processArgs(args);
        renameUser.context.validate();
    }
    
    // Check that the expected (supplied) usernames are in the context 
    private void assertUsers(String... usernames)
    {
        int length = usernames.length/2;
        assertEquals("Must have an even number of usernames passed to assertUsers", usernames.length, length*2);
        
        assertEquals(length, renameUser.context.userCount());
        Iterator<User> iterator = renameUser.context.iterator();
        for (int i=0; i<length; i++)
        {
            User user = iterator.next();
            assertEquals(usernames[i*2], user.getOldUsername());
            assertEquals(usernames[i*2+1], user.getNewUsername());
        }
    }

    @Test
    public void testLoginUsernameAndPassword() throws Exception
    {
        // Reset the password to be sure we are picking up the correct value.
        args[3] = "password";
        
        renameUser.processArgs(args);
        
        assertEquals("admin", renameUser.context.getUsername());
        assertEquals("password", renameUser.context.getPassword());
    }

    @Test
    public void testCmdLineUsernames() throws Exception
    {
        renameUser.processArgs(args);
        
        assertUsers("oldUsername", "newUsername");
    }

    @Test
    public void testFileUsernames() throws Exception
    {
        createFile("oldUsername1,newUsername1\n");
        processArgsAndValidate();
        
        assertUsers("oldUsername1", "newUsername1");
    }

    @Test
    public void testFileUsernamesWithSpaces() throws Exception
    {
        createFile(" oldUsername1 , newUsername1 \n");
        processArgsAndValidate();
        
        assertUsers("oldUsername1", "newUsername1");
    }

    @Test
    public void testFileMultipleUsernames() throws Exception
    {
        createFile("oldUsername1,newUsername1\n" +
                   "oldUsername2,newUsername2\n");
        processArgsAndValidate();
        
        assertUsers("oldUsername1", "newUsername1",
                    "oldUsername2", "newUsername2");
    }

    @Test
    public void testFileNoNewlineAtEndOfFile() throws Exception
    {
        createFile("oldUsername1,newUsername1");
        processArgsAndValidate();
        
        assertUsers("oldUsername1", "newUsername1");
    }
    
    @Test
    public void testEmptyLines() throws Exception
    {
        createFile("\n\noldUsername1,newUsername1\n\n");
        processArgsAndValidate();
        
        assertUsers("oldUsername1", "newUsername1");
    }

    @Test
    public void testComments() throws Exception
    {
        createFile("#A header comment\noldUsername1,newUsername1  #end of line comment\n\n");
        processArgsAndValidate();
        
        assertUsers("oldUsername1", "newUsername1");
    }

    @Test(expected=ToolArgumentException.class)
    public void testBadFilename() throws Exception
    {
        createFile("oldUsername1,newUsername1\n");
        args[5] = "rubbish.txt";
        processArgsAndValidate();
    }

    @Test(expected=ToolArgumentException.class)
    public void testFileNoUsernames() throws Exception
    {
        createFile("#A comment\n");
        processArgsAndValidate();
    }

    @Test(expected=ToolArgumentException.class)
    public void testTooManyUsernamesOnALine() throws Exception
    {
        createFile("\nname1,name2,name3\n");
        processArgsAndValidate();
    }

    @Test(expected=ToolArgumentException.class)
    public void testDuplicateNewUsername() throws Exception
    {
        createFile("name1,name2\nname3,name1\n");
        processArgsAndValidate();
    }

    @Test(expected=ToolArgumentException.class)
    public void testDuplicateOldUsername() throws Exception
    {
        createFile("name1,name2\nname2,name3\n");
        processArgsAndValidate();
    }

    @Test(expected=ToolArgumentException.class)
    public void testSameUsernameCmdLine() throws Exception
    {
        args[5] = "oldUsername";
        processArgsAndValidate();
    }

    @Test(expected=ToolArgumentException.class)
    public void testSameUsernameFile() throws Exception
    {
        createFile("name1,name1");
        processArgsAndValidate();
    }
}
