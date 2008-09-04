package org.alfresco.repo.cmis.ws;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

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
        request.setObjectId(ALFRESCO_TUTORIAL_NODE_REF.toString());

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
        String documentId = ALFRESCO_TUTORIAL_NODE_REF.toString();

        GetContentStream contStream = new GetContentStream();
        contStream.setDocumentId(documentId);
        GetContentStreamResponse result = ((ObjectServicePort) servicePort).getContentStream(contStream);
        if (result.getContentStream().length.intValue() == 0)
        {
            fail();
        }

        try
        {
            contStream.setDocumentId(documentId + "s");
            {result = ((ObjectServicePort) servicePort).getContentStream(contStream);}
        }
        catch (ObjectNotFoundException e)
        {
        }
        catch (Throwable e)
        {
            fail();
        }
    }
}
