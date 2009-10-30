
package org.alfresco.repo.cmis.ws;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
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
 *         &lt;element name="repositoryId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="objectId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="major" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="properties" type="{http://docs.oasis-open.org/ns/cmis/core/200908/}cmisPropertiesType" minOccurs="0"/>
 *         &lt;element name="contentStream" type="{http://docs.oasis-open.org/ns/cmis/messaging/200908/}cmisContentStreamType" minOccurs="0"/>
 *         &lt;element name="checkinComment" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="policies" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="addACEs" type="{http://docs.oasis-open.org/ns/cmis/core/200908/}cmisAccessControlListType" minOccurs="0"/>
 *         &lt;element name="removeACEs" type="{http://docs.oasis-open.org/ns/cmis/core/200908/}cmisAccessControlListType" minOccurs="0"/>
 *         &lt;element name="extension" type="{http://docs.oasis-open.org/ns/cmis/messaging/200908/}cmisExtensionType" minOccurs="0"/>
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
    "repositoryId",
    "objectId",
    "major",
    "properties",
    "contentStream",
    "checkinComment",
    "policies",
    "addACEs",
    "removeACEs",
    "extension"
})
@XmlRootElement(name = "checkIn")
public class CheckIn {

    @XmlElement(required = true)
    protected String repositoryId;
    @XmlElement(required = true)
    protected String objectId;
    @XmlElementRef(name = "major", namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", type = JAXBElement.class)
    protected JAXBElement<Boolean> major;
    @XmlElementRef(name = "properties", namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", type = JAXBElement.class)
    protected JAXBElement<CmisPropertiesType> properties;
    @XmlElementRef(name = "contentStream", namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", type = JAXBElement.class)
    protected JAXBElement<CmisContentStreamType> contentStream;
    @XmlElementRef(name = "checkinComment", namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", type = JAXBElement.class)
    protected JAXBElement<String> checkinComment;
    @XmlElement(nillable = true)
    protected List<String> policies;
    @XmlElementRef(name = "addACEs", namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", type = JAXBElement.class)
    protected JAXBElement<CmisAccessControlListType> addACEs;
    @XmlElementRef(name = "removeACEs", namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", type = JAXBElement.class)
    protected JAXBElement<CmisAccessControlListType> removeACEs;
    @XmlElementRef(name = "extension", namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", type = JAXBElement.class)
    protected JAXBElement<CmisExtensionType> extension;

    /**
     * Gets the value of the repositoryId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRepositoryId() {
        return repositoryId;
    }

    /**
     * Sets the value of the repositoryId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRepositoryId(String value) {
        this.repositoryId = value;
    }

    /**
     * Gets the value of the objectId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getObjectId() {
        return objectId;
    }

    /**
     * Sets the value of the objectId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setObjectId(String value) {
        this.objectId = value;
    }

    /**
     * Gets the value of the major property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public JAXBElement<Boolean> getMajor() {
        return major;
    }

    /**
     * Sets the value of the major property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public void setMajor(JAXBElement<Boolean> value) {
        this.major = ((JAXBElement<Boolean> ) value);
    }

    /**
     * Gets the value of the properties property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link CmisPropertiesType }{@code >}
     *     
     */
    public JAXBElement<CmisPropertiesType> getProperties() {
        return properties;
    }

    /**
     * Sets the value of the properties property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link CmisPropertiesType }{@code >}
     *     
     */
    public void setProperties(JAXBElement<CmisPropertiesType> value) {
        this.properties = ((JAXBElement<CmisPropertiesType> ) value);
    }

    /**
     * Gets the value of the contentStream property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link CmisContentStreamType }{@code >}
     *     
     */
    public JAXBElement<CmisContentStreamType> getContentStream() {
        return contentStream;
    }

    /**
     * Sets the value of the contentStream property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link CmisContentStreamType }{@code >}
     *     
     */
    public void setContentStream(JAXBElement<CmisContentStreamType> value) {
        this.contentStream = ((JAXBElement<CmisContentStreamType> ) value);
    }

    /**
     * Gets the value of the checkinComment property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getCheckinComment() {
        return checkinComment;
    }

    /**
     * Sets the value of the checkinComment property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setCheckinComment(JAXBElement<String> value) {
        this.checkinComment = ((JAXBElement<String> ) value);
    }

    /**
     * Gets the value of the policies property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the policies property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPolicies().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getPolicies() {
        if (policies == null) {
            policies = new ArrayList<String>();
        }
        return this.policies;
    }

    /**
     * Gets the value of the addACEs property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link CmisAccessControlListType }{@code >}
     *     
     */
    public JAXBElement<CmisAccessControlListType> getAddACEs() {
        return addACEs;
    }

    /**
     * Sets the value of the addACEs property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link CmisAccessControlListType }{@code >}
     *     
     */
    public void setAddACEs(JAXBElement<CmisAccessControlListType> value) {
        this.addACEs = ((JAXBElement<CmisAccessControlListType> ) value);
    }

    /**
     * Gets the value of the removeACEs property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link CmisAccessControlListType }{@code >}
     *     
     */
    public JAXBElement<CmisAccessControlListType> getRemoveACEs() {
        return removeACEs;
    }

    /**
     * Sets the value of the removeACEs property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link CmisAccessControlListType }{@code >}
     *     
     */
    public void setRemoveACEs(JAXBElement<CmisAccessControlListType> value) {
        this.removeACEs = ((JAXBElement<CmisAccessControlListType> ) value);
    }

    /**
     * Gets the value of the extension property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link CmisExtensionType }{@code >}
     *     
     */
    public JAXBElement<CmisExtensionType> getExtension() {
        return extension;
    }

    /**
     * Sets the value of the extension property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link CmisExtensionType }{@code >}
     *     
     */
    public void setExtension(JAXBElement<CmisExtensionType> value) {
        this.extension = ((JAXBElement<CmisExtensionType> ) value);
    }

}
