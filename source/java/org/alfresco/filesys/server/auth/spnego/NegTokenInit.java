/*
 * Copyright (C) 2005-2006 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.filesys.server.auth.spnego;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.DERApplicationSpecific;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DERGeneralString;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DEROutputStream;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.DERTags;
import org.bouncycastle.asn1.DERUnknownTag;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.Oid;

/**
 * NegTokenInit Class
 * 
 * <p>Contains the details of an SPNEGO NegTokenInit blob for use with CIFS.
 * 
 * @author gkspencer
 */
public class NegTokenInit
{
    // Mechtypes list
    
    private Oid[] m_mechTypes;
    
    // Context flags
    
    private int m_contextFlags = -1;
    
    // Mechtoken
    
    private byte[] m_mechToken;
    
    // MectListMIC principal
    
    private String m_mecListMICPrincipal;

    /**
     * Class constructor for decoding
     */
    public NegTokenInit()
    {
    }
    
    /**
     * Class constructor for encoding
     * 
     * @param mechTypes Oid[]
     * @param mechPrinciple String
     */
    public NegTokenInit( Oid[] mechTypes, String mechPrinciple)
    {
        m_mechTypes = mechTypes;
        m_mecListMICPrincipal = mechPrinciple;
    }
    
    /**
     * Class constructor for encoding
     * 
     * @param mechTypes Vector<Oid>
     * @param mechPrinciple String
     */
    public NegTokenInit( Vector<Oid> mechTypes, String mechPrinciple)
    {
        // Create the mechTypes array
        
        m_mechTypes = new Oid[ mechTypes.size()];
        for ( int i = 0; i < mechTypes.size(); i++)
            m_mechTypes[i] = mechTypes.get(i);

        m_mecListMICPrincipal = mechPrinciple;
    }
    
    /**
     * Return the mechTypes OID list
     * 
     * @return Oid[]
     */
    public final Oid[] getOids()
    {
        return m_mechTypes;
    }
    
    /**
     * Return the context flags
     * 
     *  @return int
     */
    public final int getContextFlags()
    {
        return m_contextFlags;
    }
    
    /**
     * Return the mechToken
     * 
     * @return byte[]
     */
    public final byte[] getMechtoken()
    {
        return m_mechToken;
    }

    /**
     * Return the mechListMIC principal
     * 
     * @return String
     */
    public final String getPrincipal()
    {
        return m_mecListMICPrincipal;
    }
    
    /**
     * Check if the OID list contains the specified OID
     * 
     * @param oid Oid
     * @return boolean
     */
    public final boolean hasOid( Oid oid)
    {
        boolean foundOid = false;
        
        if ( m_mechTypes != null)
        {
            foundOid = oid.containedIn( m_mechTypes);
        }
        
        return foundOid;
    }
    
    /**
     * Return the count of OIDs
     * 
     * @return int
     */
    public final int numberOfOids()
    {
        return m_mechTypes != null ? m_mechTypes.length : 0;
    }
    
    /**
     * Return the specified OID
     * 
     * @param idx int
     * @return OID
     */
    public final Oid getOidAt(int idx)
    {
        if ( m_mechTypes != null && idx >= 0 && idx < m_mechTypes.length)
            return m_mechTypes[idx];
        return null;
    }
    
    /**
     * Decode an SPNEGO NegTokenInit blob
     * 
     * @param buf byte[]
     * @param off int
     * @param len int
     * @exception IOException
     */
    public void decode(byte[] buf, int off, int len) throws IOException
    {
        // Create a stream around the security blob
        
        ByteArrayInputStream bytStream = new ByteArrayInputStream( buf, off, len);
        ASN1InputStream asnStream = new ASN1InputStream( bytStream);
        
        // Read the top level object from the security blob
        
        DERObject derObj = asnStream.readObject();
        
        if ( derObj instanceof DERApplicationSpecific == false)
            throw new IOException("Bad blob format (AppSpec)");

        // Access the application specific contents
            
        DERApplicationSpecific derApp = (DERApplicationSpecific) derObj;
            
        ByteArrayInputStream appStream = new ByteArrayInputStream( derApp.getContents());
        ASN1InputStream asnAppStream = new ASN1InputStream( appStream);
            
        // First object should be an OID, make sure it is the SPNEGO OID
            
        derObj = asnAppStream.readObject();
        if ( derObj instanceof DERObjectIdentifier == false)
            throw new IOException("Bad blob format (SPNEGO OID)");
        
        DERObjectIdentifier derOid = (DERObjectIdentifier) derObj;
        if ( derOid.getId().equals( OID.ID_SPNEGO) == false)
            throw new IOException("Not an SPNEGO blob");

        // Next object should be a tagged object with a sequence
        
        derObj = asnAppStream.readObject();
        if ( derObj instanceof DERTaggedObject == false)
            throw new IOException("Bad blob format, tagged object missing");

        DERTaggedObject derTagSeq = (DERTaggedObject) derObj;
        if ( derTagSeq.getTagNo() != 0 || derTagSeq.getObject() instanceof DERSequence == false)
            throw new IOException("Bad blob format, sequence missing");
        
        // Enumerate the main NegTokenInit sequence

        DERSequence negTokInitSeq = (DERSequence) derTagSeq.getObject();
        Enumeration seqEnum = negTokInitSeq.getObjects();
        
        while ( seqEnum.hasMoreElements())
        {
            // Read an object from the sequence
            
            derObj = (DERObject) seqEnum.nextElement();
            if ( derObj instanceof DERTaggedObject)
            {
                // Tag 0 should be a sequence of object identifiers
                
                DERTaggedObject derTag = (DERTaggedObject) derObj;
                if ( derTag.getTagNo() == 0 && derTag.getObject() instanceof DERSequence)
                {
                    DERSequence derSeq = (DERSequence) derTag.getObject();
                    Enumeration typesEnum = derSeq.getObjects();
                    
                    // Allocate the OID list
                    
                    m_mechTypes = new Oid[derSeq.size()];
                    int idx = 0;
                    
                    while( typesEnum.hasMoreElements())
                    {
                        derObj = (DERObject) typesEnum.nextElement();
                        if ( derObj instanceof DERObjectIdentifier)
                        {
                            derOid = (DERObjectIdentifier) derObj;
                            try
                            {
                                m_mechTypes[idx++] = new Oid( derOid.getId());
                            }
                            catch (GSSException ex)
                            {
                                throw new IOException("Bad mechType OID");
                            }
                        }
                    }
                }
                else if ( derTag.getTagNo() == 1 && derTag.getObject() instanceof DERBitString)
                {
                    // Context flags
                    
                }
                else if ( derTag.getTagNo() == 2 && derTag.getObject() instanceof DEROctetString)
                {
                    // Unpack the mechToken
                    
                    DEROctetString derStr = (DEROctetString) derTag.getObject();
                    m_mechToken = derStr.getOctets();
                }
                else if ( derTag.getTagNo() == 3 &&derTag.getObject() instanceof DEROctetString)
                {
                    // mechListMIC
                    
                }
                else if ( derTag.getTagNo() == 3 && derTag.getObject() instanceof DERSequence)
                {
                    // mechListMIC (Microsoft)
                    
                    DERSequence derSeq = (DERSequence) derTag.getObject();
                    
                    Enumeration subEnum = derSeq.getObjects();
                    while( subEnum.hasMoreElements())
                    {
                        derObj = (DERObject) subEnum.nextElement();
                        System.out.println("mechListMIC Seq: " + derObj);
                    }
                }
                else
                    throw new IOException("Bad format, unexpected type");
            }
            else
                throw new IOException("Bad format, untagged type");
        }
    }
    
    /**
     * Encode an SPNEGO NegTokenInit blob
     * 
     * @return byte[]
     * @exception IOException
     */
    public byte[] encode() throws IOException
    {
        ByteArrayOutputStream tokStream = new ByteArrayOutputStream();
        
        // Create an SPNEGO NegTokenInit token
        
        DEROutputStream derOut = new DEROutputStream( tokStream);

        derOut.writeObject( new DERObjectIdentifier( OID.ID_SPNEGO));
        ASN1EncodableVector asnList = new ASN1EncodableVector();

        // Build the mechTypes sequence

        ASN1EncodableVector mechTypesList = new ASN1EncodableVector();

        for ( Oid mechType : m_mechTypes)
        {
            mechTypesList.add( new DERObjectIdentifier( mechType.toString()));
        }
        
        asnList.add( new DERTaggedObject( true, 0, new DERSequence( mechTypesList)));
        
        // Build the mechListMIC
        //
        // Note: This field is not as specified
        
        if ( m_mecListMICPrincipal != null)
        {
            ASN1EncodableVector micList = new ASN1EncodableVector();
            
            micList.add( new DERTaggedObject( true, 0, new DERGeneralString( m_mecListMICPrincipal)));
            asnList.add( new DERTaggedObject( true, 3, new DERSequence( micList)));
        }
        
        // Generate the SPNEGO NegTokenInit blob
        
        derOut.writeObject( new DERTaggedObject( true, 0, new DERSequence( asnList)));
        DERObject token = new DERUnknownTag( DERTags.CONSTRUCTED | DERTags.APPLICATION, tokStream.toByteArray());
        
        tokStream = new ByteArrayOutputStream();
        derOut = new DEROutputStream( tokStream);
        derOut.writeObject( token);
        
        return tokStream.toByteArray();
    }
    
    /**
     * Return the NegTokenInit object as a string
     * 
     * @return String
     */
    public String toString()
    {
        StringBuilder str = new StringBuilder();
        
        str.append("[NegTokenInit ");
        
        if ( m_mechTypes != null)
        {
            str.append("mechTypes=");
            for ( Oid oid : m_mechTypes)
            {
                str.append(oid.toString());
                str.append(",");
            }
        }
        
        if ( m_contextFlags != -1)
        {
            str.append(" context=0x");
            str.append(Integer.toHexString(m_contextFlags));
        }
        
        if ( m_mechToken != null)
        {
            str.append(" token=");
            str.append(m_mechToken.length);
            str.append(" bytes");
        }
        
        if ( m_mecListMICPrincipal != null)
        {
            str.append(" principal=");
            str.append(m_mecListMICPrincipal);
        }
        str.append("]");
        
        return str.toString();
    }
}
