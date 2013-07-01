package org.alfresco.repo.cmis.ws;

import java.util.Arrays;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.ws.security.wss4j.WSS4JInInterceptor;
import org.apache.ws.security.WSConstants;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * Interceptor that by default adds PasswordText type to Password element
 * if it doesn't have Type attribute. It should be done before WSSecurityEngine
 * processes Security header in scope of WSS4JInInterceptor execution.
 * 
 * @author Viachaslau Tsikhanovich
 *
 */
public class PasswordTypeInterceptor extends AbstractSoapInterceptor
{

    private QName securityHeader = new QName(WSConstants.WSSE_NS, WSConstants.WSSE_LN);

    public PasswordTypeInterceptor()
    {
        super(Phase.PRE_PROTOCOL);
        addBefore(Arrays.asList(WSS4JInInterceptor.class.getName()));
    }

    @Override
    public void handleMessage(SoapMessage message) throws Fault
    {
        if (message.hasHeader(securityHeader))
        {
            SOAPMessage saaj = message.getContent(SOAPMessage.class);
            Document document = saaj.getSOAPPart();
            NodeList nodes = document.getElementsByTagNameNS("*", WSConstants.PASSWORD_LN);
            if (nodes.getLength() > 0)
            {
                Node passwordNode = nodes.item(0);
                NamedNodeMap atts = passwordNode.getAttributes();
                if (null == atts.getNamedItem(WSConstants.PASSWORD_TYPE_ATTR))
                {
                    Attr typeAttribute = document.createAttribute(WSConstants.PASSWORD_TYPE_ATTR);
                    typeAttribute.setValue(WSConstants.PASSWORD_TEXT);
                    atts.setNamedItem(typeAttribute);
                }
            }
        }
    }

}
