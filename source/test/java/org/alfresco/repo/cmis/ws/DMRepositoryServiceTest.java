package org.alfresco.repo.cmis.ws;


import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

/**
 * @author Michael Shavnev
 */
public class DMRepositoryServiceTest extends AbstractServiceTest
{

    public final static String SERVICE_WSDL_LOCATION = "http://localhost:8080/alfresco/cmis/RepositoryService?wsdl";
    public final static QName SERVICE_NAME = new QName("http://www.cmis.org/ns/1.0", "RepositoryService");

    protected Object getServicePort()
    {
        URL serviceWsdlURL;
        try
        {
            serviceWsdlURL = new URL(SERVICE_WSDL_LOCATION);
        }
        catch (MalformedURLException e)
        {
            throw new java.lang.RuntimeException("Cannot get service Wsdl URL", e);
        }
        Service service = Service.create(serviceWsdlURL, SERVICE_NAME);
        return service.getPort(RepositoryServicePort.class);
    }

    public void testGetRootFolder()
    {
        GetRootFolder getRootFolder = new GetRootFolder();
        try
        {
            getRootFolder.setFilter("*");
            ((RepositoryServicePort) servicePort).getRootFolder(getRootFolder);
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            fail();
        }

        try
        {
            getRootFolder.setFilter("name");
            ((RepositoryServicePort) servicePort).getRootFolder(getRootFolder);
            fail();
        }
        catch (FilterNotValidException e)
        {
            // expected
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            fail();
        }
    }

}
