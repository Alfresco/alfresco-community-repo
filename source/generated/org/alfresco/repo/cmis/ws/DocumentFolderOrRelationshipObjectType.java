
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for documentFolderOrRelationshipObjectType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="documentFolderOrRelationshipObjectType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.cmis.org/ns/1.0}documentOrFolderObjectType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.cmis.org/ns/1.0}sourceOID" minOccurs="0"/>
 *         &lt;element ref="{http://www.cmis.org/ns/1.0}targetOID" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "documentFolderOrRelationshipObjectType", propOrder = {
    "sourceOID",
    "targetOID"
})
public class DocumentFolderOrRelationshipObjectType
    extends DocumentOrFolderObjectType
{

    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected String sourceOID;
    @XmlElement(namespace = "http://www.cmis.org/ns/1.0")
    protected String targetOID;

    /**
     * Gets the value of the sourceOID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSourceOID() {
        return sourceOID;
    }

    /**
     * Sets the value of the sourceOID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSourceOID(String value) {
        this.sourceOID = value;
    }

    /**
     * Gets the value of the targetOID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTargetOID() {
        return targetOID;
    }

    /**
     * Sets the value of the targetOID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTargetOID(String value) {
        this.targetOID = value;
    }

}
