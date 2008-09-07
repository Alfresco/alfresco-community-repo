
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
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
 *         &lt;element ref="{http://www.cmis.org/2008/05}type" minOccurs="0"/>
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
    "type"
})
@XmlRootElement(name = "getTypeDefinitionResponse")
public class GetTypeDefinitionResponse {

    @XmlElementRef(name = "type", namespace = "http://www.cmis.org/2008/05", type = JAXBElement.class)
    protected JAXBElement<? extends CmisTypeDefinitionType> type;

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link CmisTypeFolderDefinitionType }{@code >}
     *     {@link JAXBElement }{@code <}{@link CmisTypePolicyDefinitionType }{@code >}
     *     {@link JAXBElement }{@code <}{@link CmisTypeDocumentDefinitionType }{@code >}
     *     {@link JAXBElement }{@code <}{@link CmisTypeRelationshipDefinitionType }{@code >}
     *     {@link JAXBElement }{@code <}{@link CmisTypeDefinitionType }{@code >}
     *     
     */
    public JAXBElement<? extends CmisTypeDefinitionType> getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link CmisTypeFolderDefinitionType }{@code >}
     *     {@link JAXBElement }{@code <}{@link CmisTypePolicyDefinitionType }{@code >}
     *     {@link JAXBElement }{@code <}{@link CmisTypeDocumentDefinitionType }{@code >}
     *     {@link JAXBElement }{@code <}{@link CmisTypeRelationshipDefinitionType }{@code >}
     *     {@link JAXBElement }{@code <}{@link CmisTypeDefinitionType }{@code >}
     *     
     */
    public void setType(JAXBElement<? extends CmisTypeDefinitionType> value) {
        this.type = ((JAXBElement<? extends CmisTypeDefinitionType> ) value);
    }

}
