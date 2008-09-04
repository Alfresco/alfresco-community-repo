
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getRepositoryInfoResponse element declaration.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;element name="getRepositoryInfoResponse">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *         &lt;sequence>
 *           &lt;element name="repositoryInfo" type="{http://www.cmis.org/ns/1.0}repositoryInfoType"/>
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
    "repositoryInfo"
})
@XmlRootElement(name = "getRepositoryInfoResponse")
public class GetRepositoryInfoResponse {

    @XmlElement(namespace = "http://www.cmis.org/ns/1.0", required = true)
    protected RepositoryInfoType repositoryInfo;

    /**
     * Gets the value of the repositoryInfo property.
     * 
     * @return
     *     possible object is
     *     {@link RepositoryInfoType }
     *     
     */
    public RepositoryInfoType getRepositoryInfo() {
        return repositoryInfo;
    }

    /**
     * Sets the value of the repositoryInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link RepositoryInfoType }
     *     
     */
    public void setRepositoryInfo(RepositoryInfoType value) {
        this.repositoryInfo = value;
    }

}
