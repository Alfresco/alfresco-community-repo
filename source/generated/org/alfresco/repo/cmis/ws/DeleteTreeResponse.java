
package org.alfresco.repo.cmis.ws;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.alfresco.repo.cmis.ws.DeleteTreeResponse.FailedToDelete;


/**
 * <p>Java class for deleteTreeResponse element declaration.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;element name="deleteTreeResponse">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *         &lt;sequence>
 *           &lt;element name="failedToDelete">
 *             &lt;complexType>
 *               &lt;complexContent>
 *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                   &lt;sequence>
 *                     &lt;element name="objectId" type="{http://www.cmis.org/ns/1.0}ID" maxOccurs="unbounded" minOccurs="0"/>
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
    "failedToDelete"
})
@XmlRootElement(name = "deleteTreeResponse")
public class DeleteTreeResponse {

    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected FailedToDelete failedToDelete;

    /**
     * Gets the value of the failedToDelete property.
     * 
     * @return
     *     possible object is
     *     {@link FailedToDelete }
     *     
     */
    public FailedToDelete getFailedToDelete() {
        return failedToDelete;
    }

    /**
     * Sets the value of the failedToDelete property.
     * 
     * @param value
     *     allowed object is
     *     {@link FailedToDelete }
     *     
     */
    public void setFailedToDelete(FailedToDelete value) {
        this.failedToDelete = value;
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
     *         &lt;element name="objectId" type="{http://www.cmis.org/ns/1.0}ID" maxOccurs="unbounded" minOccurs="0"/>
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
        "objectId"
    })
    public static class FailedToDelete {

        @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
        protected List<String> objectId;

        /**
         * Gets the value of the objectId property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the objectId property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getObjectId().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link String }
         * 
         * 
         */
        public List<String> getObjectId() {
            if (objectId == null) {
                objectId = new ArrayList<String>();
            }
            return this.objectId;
        }

    }

}
