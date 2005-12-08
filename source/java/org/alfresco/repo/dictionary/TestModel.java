/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.dictionary;

import java.util.ArrayList;
import java.util.List;


/**
 * Test Model Definitions
 */
public class TestModel
{

    public static void main(String[] args)
    {
        if (args != null && args.length > 0 && args[0].equals("-h"))
        {
            System.out.println("TestModel [model filename]*");
            System.exit(0);
        }
        
        System.out.println("Testing dictionary model definitions...");

        // construct list of models to test
        // include alfresco defaults
        List<String> bootstrapModels = new ArrayList<String>();
        bootstrapModels.add("alfresco/model/dictionaryModel.xml");
        bootstrapModels.add("alfresco/model/systemModel.xml");
        bootstrapModels.add("alfresco/model/contentModel.xml");
        bootstrapModels.add("alfresco/model/applicationModel.xml");

        // include models specified on command line
        for (String arg: args)
        {
            bootstrapModels.add(arg);
        }
        
        for (String model : bootstrapModels)
        {
            System.out.println(" " + model);
        }
        
        // construct dictionary dao        
        NamespaceDAO namespaceDAO = new NamespaceDAOImpl();
        DictionaryDAOImpl dictionaryDAO = new DictionaryDAOImpl(namespaceDAO);

        // bootstrap dao
        try
        {
            DictionaryBootstrap bootstrap = new DictionaryBootstrap();
            bootstrap.setModels(bootstrapModels);
            bootstrap.setDictionaryDAO(dictionaryDAO);
            bootstrap.bootstrap();
            System.out.println("Models are valid.");
        }
        catch(Exception e)
        {
            System.out.println("Found an invalid model...");
            Throwable t = e;
            while (t != null)
            {
                System.out.println(t.getMessage());
                t = t.getCause();
            }
        }
    }
}