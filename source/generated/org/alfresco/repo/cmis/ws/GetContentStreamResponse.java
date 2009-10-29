
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="contentStream" type="{http://docs.oasis-open.org/ns/cmis/messaging/200908/}cmisContentStreamType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "contentStream"
})
@XmlRootElement(name = "getContentStreamResponse")
public class GetContentStreamResponse {

    @XmlElement(required = true)
    protected CmisContentStreamType contentStream;

    /**
     * Gets the value of the contentStream property.
     * 
     * @return
     *     possible object is
     *     {@link CmisContentStreamType }
     *     
     */
    public CmisContentStreamType getContentStream() {
        return contentStream;
    }

    /**
     * Sets the value of the contentStream property.
     * 
     * @param value
     *     allowed object is
     *     {@link CmisContentStreamType }
     *     
     */
    public void setContentStream(CmisContentStreamType value) {
        this.contentStream = value;
    }

}
