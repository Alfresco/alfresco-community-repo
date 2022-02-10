/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.test.integration.issue;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

/**
 * See https://issues.alfresco.com/jira/browse/RM-981.
 * 
 * Instance of the repository needs to be running on localhost:8080 before executing this
 * system test.
 * 
 * @author Roy Wetherall
 */
public class RM981SystemTest extends TestCase
{
    public void testRM981() throws Exception 
    {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        SecureRandom rnd = new SecureRandom();
        List<String> data = new ArrayList<>();
        for (int i = 0; i < 100; i++) 
        {
            String definitionname = "test_" + i + "_" + rnd.nextInt(Integer.MAX_VALUE);
            data.add(definitionname);
        }

        for (String definitionname : data) 
        {
            pool.submit(new SendRequest(definitionname));
        }
        pool.shutdown();
        pool.awaitTermination(60L, TimeUnit.SECONDS);
    }


    class SendRequest implements Runnable 
    {

        private String definitionname;
    
        public SendRequest(String definitionname) 
        {
            this.definitionname = definitionname;
        }
    
        @Override
        public void run() 
        {
            try 
            {
                URL url = new URL("http://localhost:8080/alfresco/service/api/rma/admin/customreferencedefinitions");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setRequestProperty("Authorization", "Basic YWRtaW46YWRtaW4=");
                String body = "{\"referenceType\":\"bidirectional\",\"label\":\"" + definitionname + "\"}";
                OutputStream out = conn.getOutputStream();
    
                out.write(body.getBytes("UTF-8"));
                out.flush();
                out.close();
    
                int status = conn.getResponseCode();
    
                if (status != 200) 
                {
                    System.out.println("Reproduced");
                    System.out.println("---------------------------------");
                    String line;
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    while ((line = reader.readLine()) != null)
                    {
                        System.out.println(line);
                    }
                    reader.close();
                    System.exit(0);
                }
            } 
            catch (Exception ex) 
            {
                ex.printStackTrace();
    
            }
        }
    }
}
