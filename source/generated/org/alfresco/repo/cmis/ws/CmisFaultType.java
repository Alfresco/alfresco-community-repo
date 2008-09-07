
package org.alfresco.repo.cmis.ws;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for cmisFaultType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="cmisFaultType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="errorCode" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="errorMessage" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "cmisFaultType", propOrder = {
    "errorCode",
    "errorMessage"
})
@XmlSeeAlso({
    OperationNotSupportedExceptionType.class,
    PermissionDeniedExceptionType.class,
    NotInFolderExceptionType.class,
    StorageExceptionType.class,
    OffsetExceptionType.class,
    ConstraintViolationExceptionType.class,
    VersioningExceptionType.class,
    InvalidArgumentExceptionType.class,
    FilterNotValidExceptionType.class,
    TypeNotFoundExceptionType.class,
    UpdateConflictExceptionType.class,
    FolderNotValidExceptionType.class,
    ObjectNotFoundExceptionType.class,
    StreamNotSupportedExceptionType.class,
    RuntimeExceptionType.class,
    ContentAlreadyExistsExceptionType.class
})
public class CmisFaultType {

    @XmlElement(required = true)
    protected BigInteger errorCode;
    @XmlElement(required = true)
    protected String errorMessage;

    /**
     * Gets the value of the errorCode property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getErrorCode() {
        return errorCode;
    }

    /**
     * Sets the value of the errorCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setErrorCode(BigInteger value) {
        this.errorCode = value;
    }

    /**
     * Gets the value of the errorMessage property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Sets the value of the errorMessage property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setErrorMessage(String value) {
        this.errorMessage = value;
    }

}
