/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
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
package org.alfresco.web.forms;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import junit.framework.TestCase;

/**
 * Simple XMLUtil test
 */
public class XMLUtilTest extends TestCase
{
    public static final String SOME_XML = 
        " <model name='test1:testModelOne' xmlns='http://www.alfresco.org/model/dictionary/1.0'>" +
        "   <description>Test model one</description>" +
        "   <author>Alfresco</author>" +
        "   <published>2008-01-01</published>" +
        "   <version>1.0</version>" +
        "   <imports>" +
        "      <import uri='http://www.alfresco.org/model/dictionary/1.0' prefix='d'/>" +
        "   </imports>" +
        "   <namespaces>" +
        "      <namespace uri='http://www.alfresco.org/test/testmodel1/1.0' prefix='test1'/>" +
        "   </namespaces>" +
        "   <types>" +
        "      <type name='test1:base'>" +
        "        <title>Base</title>" +
        "        <description>The Base Type</description>" +
        "        <properties>" +
        "           <property name='test1:prop1'>" +
        "              <type>d:text</type>" +
        "           </property>" +
        "        </properties>" +
        "      </type>" +
        "   </types>" +
        " </model>";
    
    
    private final static int threadCount = 5;
    
    private final static int loopCount = 50;
    private final static int randomNextInt = 100;
    
    private Map<String, Throwable> errors = new HashMap<String, Throwable>();
    

    protected void setUp() throws Exception
    {
    }

    // https://issues.alfresco.com/browse/ETWOONE-241
    public void testConcurrentParse()
    {
        ThreadGroup threadGroup = new ThreadGroup(getName());
        Thread[] threads = new Thread[threadCount];
        
        for (int i = 0; i < threadCount; i++)
        {
            threads[i] = new Thread(threadGroup, new TestRun(""+i), String.format("XMLUtilTest-%02d", i));
            threads[i].start();
        }
        
        // join each thread so that we wait for them all to finish
        for (int i = 0; i < threads.length; i++)
        {
            try
            {
                threads[i].join();
            }
            catch (InterruptedException e)
            {
                // ignore
            }
        }
        
        if (errors.size() != 0)
        {
           fail();
        }
    }
    
    class TestRun extends Thread
    {
        private String arg;
        
        public TestRun(String arg)
        {
            this.arg = arg;
        }
        
        public String getArg()
        {
            return arg;
        }
        
        public void run()
        {
            Random random = new Random(System.currentTimeMillis());
            
            for (int i = 0; i < loopCount; i++)
            {
               try
               {
                   XMLUtil.parse(SOME_XML); // ignore returned doc
               }
               catch (Throwable t)
               {
                   t.printStackTrace();
                   errors.put(arg, t);
                   break;
               }
               
               // random delay ...
               if (randomNextInt != 0)
               {
                  int msecs = random.nextInt(randomNextInt);
                  try {Thread.sleep(msecs);} catch (Exception exception){};
               }
            }
        }
    }
}
