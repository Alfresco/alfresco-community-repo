
package org.alfresco.repo.cmis.ws;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.alfresco.repo.cmis.ws.GetAllowableActionsResponse.AllowableActionCollection;


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
 *           &lt;element name="allowableActionCollection">
 *             &lt;complexType>
 *               &lt;complexContent>
 *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                   &lt;sequence>
 *                     &lt;element name="action" type="{http://www.cmis.org/ns/1.0}allowableActionsEnum" maxOccurs="unbounded" minOccurs="0"/>
 *                   &lt;/sequence>
 *                 &lt;/restriction>
 *               &lt;/complexContent>
 *             &lt;/complexType>
 *           &lt;/element>
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
    "allowableActionCollection"
})
@XmlRootElement(name = "getAllowableActionsResponse")
public class GetAllowableActionsResponse {

    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected AllowableActionCollection allowableActionCollection;

    /**
     * Gets the value of the allowableActionCollection property.
     * 
     * @return
     *     possible object is
     *     {@link AllowableActionCollection }
     *     
     */
    public AllowableActionCollection getAllowableActionCollection() {
        return allowableActionCollection;
    }

    /**
     * Sets the value of the allowableActionCollection property.
     * 
     * @param value
     *     allowed object is
     *     {@link AllowableActionCollection }
     *     
     */
    public void setAllowableActionCollection(AllowableActionCollection value) {
        this.allowableActionCollection = value;
    }


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
     *         &lt;element name="action" type="{http://www.cmis.org/ns/1.0}allowableActionsEnum" maxOccurs="unbounded" minOccurs="0"/>
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
        "action"
    })
    public static class AllowableActionCollection {

        @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
        protected List<AllowableActionsEnum> action;

        /**
         * Gets the value of the action property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the action property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getAction().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link AllowableActionsEnum }
         * 
         * 
         */
        public List<AllowableActionsEnum> getAction() {
            if (action == null) {
                action = new ArrayList<AllowableActionsEnum>();
            }
            return this.action;
        }

    }

}
