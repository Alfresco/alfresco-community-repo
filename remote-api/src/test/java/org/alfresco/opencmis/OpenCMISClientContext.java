/*
 * #%L
 * Alfresco Remote API
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
package org.alfresco.opencmis;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.tck.impl.JUnitHelper;
import org.springframework.context.ApplicationContext;

/**
 * Encapsulates Chemistry OpenCMIS client connection details and creates a parameters file for running the TCK tests.
 * 
 * @author steveglover
 *
 */
public class OpenCMISClientContext
{
    private Map<String, String> cmisParameters;

    public OpenCMISClientContext(BindingType bindingType, String url, String username, String password, Map<String, String> parameters, ApplicationContext ctx) throws IOException
    {
        cmisParameters = new HashMap<String, String>();
        cmisParameters.putAll(parameters);
        cmisParameters.put(SessionParameter.BINDING_TYPE, bindingType.value());
        if (bindingType.equals(BindingType.ATOMPUB))
        {
            cmisParameters.put(SessionParameter.ATOMPUB_URL, url);
        }
        else if (bindingType.equals(BindingType.BROWSER))
        {
            cmisParameters.put(SessionParameter.BROWSER_URL, url);
        }
        cmisParameters.put(SessionParameter.USER, username);
        cmisParameters.put(SessionParameter.PASSWORD, password);

        if (ctx != null)
        {
            Properties properties = (Properties) ctx.getBean("global-properties");
            cmisParameters.put(SessionParameter.CONNECT_TIMEOUT, properties.getProperty("opencmis.tck.connecttimeout"));
            cmisParameters.put(SessionParameter.READ_TIMEOUT, properties.getProperty("opencmis.tck.readtimeout"));
        }
        createCMISParametersFile();
    }

    public OpenCMISClientContext(BindingType bindingType, String url, String username, String password, Map<String, String> parameters) throws IOException
    {
        this(bindingType, url, username, password, parameters, null);
    }

    protected void createCMISParametersFile() throws IOException
    {
        File f = File.createTempFile("OpenCMISTCKContext", "" + System.currentTimeMillis(), new File(System.getProperty("java.io.tmpdir")));
        f.deleteOnExit();
        FileWriter fw = new FileWriter(f);
        for (String key : cmisParameters.keySet())
        {
            fw.append(key);
            fw.append("=");
            fw.append(cmisParameters.get(key));
            fw.append("\n");
        }
        fw.close();
        System.setProperty(JUnitHelper.JUNIT_PARAMETERS, f.getAbsolutePath());
        System.out.println("CMIS client parameters file: " + f.getAbsolutePath());
    }

    public Map<String, String> getCMISParameters()
    {
        return cmisParameters;
    }
}
