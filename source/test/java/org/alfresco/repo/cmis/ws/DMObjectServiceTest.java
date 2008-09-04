package org.alfresco.repo.cmis.ws;


import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.alfresco.repo.cmis.ws.OIDUtils;

public class DMObjectServiceTest extends AbstractServiceTest
{

    public final static String SERVICE_WSDL_LOCATION = "http://localhost:8080/alfresco/cmis/ObjectService?wsdl";
    public final static QName SERVICE_NAME = new QName("http://www.cmis.org/ns/1.0", "ObjectService");

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
        return service.getPort(ObjectServicePort.class);
    }

    public void testGetDocumentProperties() throws Exception
    {
        GetProperties request = new GetProperties();
        request.setObjectId(OIDUtils.toOID(ALFRESCO_TUTORIAL_NODE_REF));

        GetPropertiesResponse response = ((ObjectServicePort) servicePort).getProperties(request);
        assertNotNull(response);
    }

    public void testGetPropertiesForInvalidOID() throws Exception
    {

        GetProperties request = new GetProperties();
        request.setObjectId("invalid OID");

        try
        {
            ((ObjectServicePort) servicePort).getProperties(request);
        }
        catch (InvalidArgumentException e)
        {
            return;
        }

        fail("Expects exception");
    }



    public void testGetContentStream() throws Exception
    {
        String documentId = OIDUtils.toOID(ALFRESCO_TUTORIAL_NODE_REF);

        for (int offset = 0; offset < 10; offset++)
        {
            for (int length = 0; length < 10; length++)
            {
                BigInteger offsetBig = new BigInteger(String.valueOf(offset));
                BigInteger lengthBig = new BigInteger(String.valueOf(length));

                byte[] result = ((ObjectServicePort) servicePort).getContentStream(documentId, offsetBig, lengthBig);

                assertNotNull(result);
                if (result.length > length)
                {
                    fail();
                }
            }

        }

        byte[] result = ((ObjectServicePort) servicePort).getContentStream(documentId, null, BigInteger.valueOf(20));
        assertNotNull(result);

        try
        {result = ((ObjectServicePort) servicePort).getContentStream("Invalid", null, null);}
        catch (InvalidArgumentException e) {}
        catch (Throwable e){fail();}

        try
        {result = ((ObjectServicePort) servicePort).getContentStream(documentId + "s", null, null);}
        catch (ObjectNotFoundException e) {}
        catch (Throwable e){fail();}

        try
        {result = ((ObjectServicePort) servicePort).getContentStream(OIDUtils.toOID(COMPANY_HOME_NODE_REF), null, null);}
        catch (StreamNotSupportedException e) {}
        catch (Throwable e){fail();}

    }
}
