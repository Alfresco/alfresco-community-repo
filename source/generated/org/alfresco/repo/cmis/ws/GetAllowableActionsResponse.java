
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getAllowableActionsResponse element declaration.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;element name="getAllowableActionsResponse">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *         &lt;sequence>
 *           &lt;element name="allowableActions" type="{http://www.cmis.org/ns/1.0}allowableActionsType"/>
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
    "allowableActions"
})
@XmlRootElement(name = "getAllowableActionsResponse")
public class GetAllowableActionsResponse {

    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected AllowableActionsType allowableActions;

    /**
     * Gets the value of the allowableActions property.
     * 
     * @return
     *     possible object is
     *     {@link AllowableActionsType }
     *     
     */
    public AllowableActionsType getAllowableActions() {
        return allowableActions;
    }

    /**
     * Sets the value of the allowableActions property.
     * 
     * @param value
     *     allowed object is
     *     {@link AllowableActionsType }
     *     
     */
    public void setAllowableActions(AllowableActionsType value) {
        this.allowableActions = value;
    }

}
