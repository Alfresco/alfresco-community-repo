
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getContentStreamResponse element declaration.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;element name="getContentStreamResponse">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *         &lt;sequence>
 *           &lt;element ref="{http://www.cmis.org/ns/1.0}contentStream"/>
 *         &lt;/sequence>
 *       &lt;/restriction>
 *     &lt;/complexContent>
 *   &lt;/complexType>
 * &lt;/element>
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

    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected ContentStreamType contentStream;

    /**
     * Gets the value of the contentStream property.
     * 
     * @return
     *     possible object is
     *     {@link ContentStreamType }
     *     
     */
    public ContentStreamType getContentStream() {
        return contentStream;
    }

    /**
     * Sets the value of the contentStream property.
     * 
     * @param value
     *     allowed object is
     *     {@link ContentStreamType }
     *     
     */
    public void setContentStream(ContentStreamType value) {
        this.contentStream = value;
    }

}
