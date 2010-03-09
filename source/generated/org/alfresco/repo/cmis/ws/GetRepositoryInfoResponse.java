
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
 *         &lt;element name="repositoryInfo" type="{http://docs.oasis-open.org/ns/cmis/core/200908/}cmisRepositoryInfoType"/>
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
    "repositoryInfo"
})
@XmlRootElement(name = "getRepositoryInfoResponse")
public class GetRepositoryInfoResponse {

    @XmlElement(required = true)
    protected CmisRepositoryInfoType repositoryInfo;

    /**
     * Gets the value of the repositoryInfo property.
     * 
     * @return
     *     possible object is
     *     {@link CmisRepositoryInfoType }
     *     
     */
    public CmisRepositoryInfoType getRepositoryInfo() {
        return repositoryInfo;
    }

    /**
     * Sets the value of the repositoryInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link CmisRepositoryInfoType }
     *     
     */
    public void setRepositoryInfo(CmisRepositoryInfoType value) {
        this.repositoryInfo = value;
    }

}
