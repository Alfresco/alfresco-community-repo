/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.filesys.auth.cifs;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.filesys.alfresco.AlfrescoClientInfo;
import org.alfresco.filesys.auth.PassthruServerFactory;
import org.alfresco.jlan.debug.Debug;
import org.alfresco.jlan.server.SessionListener;
import org.alfresco.jlan.server.SrvSession;
import org.alfresco.jlan.server.auth.AuthContext;
import org.alfresco.jlan.server.auth.AuthenticatorException;
import org.alfresco.jlan.server.auth.ClientInfo;
import org.alfresco.jlan.server.auth.ICifsAuthenticator;
import org.alfresco.jlan.server.auth.NTLanManAuthContext;
import org.alfresco.jlan.server.auth.ntlm.NTLM;
import org.alfresco.jlan.server.auth.ntlm.NTLMMessage;
import org.alfresco.jlan.server.auth.ntlm.TargetInfo;
import org.alfresco.jlan.server.auth.ntlm.Type1NTLMMessage;
import org.alfresco.jlan.server.auth.ntlm.Type2NTLMMessage;
import org.alfresco.jlan.server.auth.ntlm.Type3NTLMMessage;
import org.alfresco.jlan.server.auth.passthru.AuthenticateSession;
import org.alfresco.jlan.server.auth.passthru.PassthruDetails;
import org.alfresco.jlan.server.auth.passthru.PassthruServers;
import org.alfresco.jlan.server.config.InvalidConfigurationException;
import org.alfresco.jlan.server.config.ServerConfiguration;
import org.alfresco.jlan.server.core.SharedDevice;
import org.alfresco.jlan.smb.Capability;
import org.alfresco.jlan.smb.SMBStatus;
import org.alfresco.jlan.smb.dcerpc.UUID;
import org.alfresco.jlan.smb.server.SMBServer;
import org.alfresco.jlan.smb.server.SMBSrvException;
import org.alfresco.jlan.smb.server.SMBSrvPacket;
import org.alfresco.jlan.smb.server.SMBSrvSession;
import org.alfresco.jlan.smb.server.VirtualCircuit;
import org.alfresco.jlan.util.DataPacker;
import org.alfresco.jlan.util.HexDump;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.NTLMMode;
import org.alfresco.repo.security.authentication.ntlm.NLTMAuthenticator;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.config.ConfigElement;

/**
 * Passthru Authenticator Class
 * <p>
 * Authenticate users accessing the CIFS server by validating the user against a domain controller
 * or other server on the network.
 * 
 * @author GKSpencer
 */
public class PassthruCifsAuthenticator extends CifsAuthenticatorBase implements SessionListener
{
    // Debug logging

    private static final Log logger = LogFactory.getLog(PassthruCifsAuthenticator.class);

    // Constants

    public final static int DefaultSessionTmo = 5000;   // 5 seconds
    public final static int MinSessionTmo = 2000;       // 2 seconds
    public final static int MaxSessionTmo = 30000;      // 30 seconds

    public final static int MinCheckInterval = 10;		// 10 seconds
    public final static int MaxCheckInterval = 15 * 60; // 15 minutes
    
    // Passthru keep alive interval
    
    public final static long PassthruKeepAliveInterval  = 60000L;   // 60 seconds
    
    // NTLM flags mask, used to mask out features that are not supported
    
    private static final int NTLM_FLAGS = NTLM.Flag56Bit +
                                          NTLM.Flag128Bit +
                                          NTLM.FlagLanManKey +
                                          NTLM.FlagNegotiateNTLM +
                                          NTLM.FlagNegotiateUnicode;
    
    // Passthru servers used to authenticate users

    private PassthruServers m_passthruServers;
    private boolean m_localPassThruServers;

    // Sessions that are currently in the negotiate/session setup state

    private Hashtable<String, PassthruDetails> m_sessions;
    
    /**
     * Passthru Authenticator Constructor
     * <p>
     * Default to user mode security with encrypted password support.
     */
    public PassthruCifsAuthenticator()
    {
        // Allocate the session table

        m_sessions = new Hashtable<String, PassthruDetails>();
    }        

    public void setPassthruServers(PassthruServers servers)
    {
        m_passthruServers = servers;
    }

    /**
     * Authenticate the connection to a particular share, called when the SMB server is in share
     * security mode
     * 
     * @param client ClientInfo
     * @param share SharedDevice
     * @param sharePwd String
     * @param sess SrvSession
     * @return int
     */
    public int authenticateShareConnect(ClientInfo client, SharedDevice share, String sharePwd, SrvSession sess)
    {
        return ICifsAuthenticator.Writeable;
    }

    /**
     * Authenticate a session setup by a user
     * 
     * @param client ClientInfo
     * @param sess SrvSession
     * @param alg int
     * @return int
     */
    public int authenticateUser(final ClientInfo client, final SrvSession sess, int alg)
    {
        // Check that the client is an Alfresco client
      
        if ( client instanceof AlfrescoClientInfo == false)
            return ICifsAuthenticator.AUTH_DISALLOW;
        
        final AlfrescoClientInfo alfClient = (AlfrescoClientInfo) client;
        
        // The null session will only be allowed to connect to the IPC$ named pipe share.

        if (client.isNullSession())
        {
            // Debug
            
            if ( logger.isDebugEnabled())
                logger.debug("Null CIFS logon allowed, sess = " + sess.getUniqueId());

            return ICifsAuthenticator.AUTH_ALLOW;
        }

        // Start a transaction
        
        return doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Integer>(){

            public Integer execute() throws Throwable
            {
                int authSts = AUTH_DISALLOW;
                
                try
                {
                    // Check if the client is already authenticated, and it is not a null logon
                    
                    if ( alfClient.hasAuthenticationTicket() && client.getLogonType() != ClientInfo.LogonNull)
                    {
                        // Use the existing authentication token
                        
                        getAuthenticationService().validate(alfClient.getAuthenticationTicket());
            
                        // Debug
                        
                        if ( logger.isDebugEnabled())
                            logger.debug("Re-using existing authentication token");
                        
                        // Return the authentication status
                        
                        return client.getLogonType() != ClientInfo.LogonGuest ? AUTH_ALLOW : AUTH_GUEST; 
                    }
                    
                    // Check if this is a guest logon
                    
                    if ( client.isGuest() || client.getUserName().equalsIgnoreCase(getGuestUserName()))
                    {
                        // Check if guest logons are allowed
                        
                        if ( allowGuest() == false)
                            return AUTH_DISALLOW;
                        
                        //  Get a guest authentication token
                        
                        doGuestLogon( client, sess);
                        
                        // Indicate logged on as guest
                        
                        authSts = AUTH_GUEST;
                        
                        // DEBUG
                        
                        if ( logger.isDebugEnabled())
                            logger.debug("Authenticated user " + client.getUserName() + " sts=" + getStatusAsString(authSts));
                        
                        // Return the guest status
                        
                        return authSts;
                    }
            
                    // Find the active authentication session details for the server session
            
                    PassthruDetails passDetails = m_sessions.get(sess.getUniqueId());
            
                    if (passDetails != null)
                    {
            
                        try
                        {
            
                            // Authenticate the user by passing the hashed password to the authentication server
                            // using the session that has already been setup.
            
                            AuthenticateSession authSess = passDetails.getAuthenticateSession();
                            authSess.doSessionSetup(client.getDomain(), client.getUserName(), null, client.getANSIPassword(), client.getPassword(), 0);
            
                            // Check if the user has been logged on as a guest
            
                            if (authSess.isGuest())
                            {
            
                                // Check if the local server allows guest access
            
                                if (allowGuest() == true)
                                {
                                    //  Get a guest authentication token
                                    
                                    doGuestLogon( client, sess);
                                    
                                    // Allow the user access as a guest
            
                                    authSts = ICifsAuthenticator.AUTH_GUEST;
            
                                    // Debug
            
                                    if (logger.isDebugEnabled())
                                        logger.debug("Passthru authenticate user=" + client.getUserName() + ", GUEST");
                                }
                            }
                            else
                            {
                                // Map the passthru username to an Alfresco person
            
                                String username = client.getUserName();

                                // Use the person name as the current user
                                
                                getAuthenticationComponent().setCurrentUser(username);
                                alfClient.setAuthenticationTicket(getAuthenticationService().getCurrentTicket());
                                
                                // DEBUG
                                
                                if ( logger.isDebugEnabled())
                                    logger.debug("Setting current user using person " + getAuthenticationComponent().getCurrentUserName() + " (username " + username + ")");
        
                                // Allow the user full access to the server
        
                                authSts = ICifsAuthenticator.AUTH_ALLOW;
        
                                // Debug
        
                                if (logger.isDebugEnabled())
                                    logger.debug("Passthru authenticate user=" + client.getUserName() + ", FULL");
                            }
                        }
                        catch (AuthenticationException e)
                        {
                            throw e;
                        }
                        catch (Exception ex)
                        {
            
                            // Debug
            
                            logger.error(ex);
                        }
            
                        // Keep the authentication session if the user session is an SMB session, else close the
                        // session now
            
                        if ((sess instanceof SMBSrvSession) == false)
                        {
            
                            // Remove the passthru session from the active list
            
                            m_sessions.remove(sess.getUniqueId());
            
                            // Close the passthru authentication session
            
                            try
                            {
            
                                // Close the authentication session
            
                                AuthenticateSession authSess = passDetails.getAuthenticateSession();
                                authSess.CloseSession();
            
                                // DEBUG
            
                                if (logger.isDebugEnabled())
                                    logger.debug("Closed auth session, sessId=" + authSess.getSessionId());
                            }
                            catch (Exception ex)
                            {
            
                                // Debug
            
                                logger.error("Passthru error closing session (auth user)", ex);
                            }
                        }
                    }
                    else
                    {
            
                        // DEBUG
            
                        if (logger.isDebugEnabled())
                            logger.debug("  No PassthruDetails for " + sess.getUniqueId());
                    }
                }
                catch ( Exception ex)
                {
                    //  DEBUG
                  
                    if ( logger.isDebugEnabled())
                        logger.debug( ex);
                    
                    // Return an access denied status
                    
                    return AUTH_DISALLOW;
                }
                
                // Return the authentication status

                return authSts;
            }});
    }

    /**
     * Return an authentication context for the new session
     * 
     * @return AuthContext
     */
    public AuthContext getAuthContext( SMBSrvSession sess)
    {
        // Open a connection to the authentication server, use normal session setup

        AuthContext authCtx = null;
        
        try
        {
            // Try and map the client address to a domain
            
            String domain = mapClientAddressToDomain( sess.getRemoteAddress());
            
            // DEBUG
            
            if ( logger.isDebugEnabled())
                logger.debug("Mapped client " + sess.getRemoteAddress() + " to domain " + domain);
            
            AuthenticateSession authSess = m_passthruServers.openSession( false, domain);
            if (authSess != null)
            {
    
                // Create an entry in the active sessions table for the new session
    
                PassthruDetails passDetails = new PassthruDetails(sess, authSess);
                m_sessions.put(sess.getUniqueId(), passDetails);
    
                // Use the challenge key returned from the authentication server
    
                authCtx = new NTLanManAuthContext( authSess.getEncryptionKey());
                sess.setAuthenticationContext( authCtx);
    
                // DEBUG
    
                if (logger.isDebugEnabled())
                    logger.debug("Passthru sessId=" + authSess.getSessionId() + ", auth ctx=" + authCtx);
            }
            else if ( logger.isDebugEnabled())
                logger.debug("Failed to open a passthru session, mapped domain = " + domain);
        }
        catch (Exception ex)
        {

            // Debug

            logger.error("Passthru error getting challenge", ex);
        }

        // Return the authentication context

        return authCtx;
    }

    /**
     * Generate the CIFS negotiate response packet, the authenticator should add authentication specific fields
     * to the response.
     * 
     * @param sess SMBSrvSession
     * @param respPkt SMBSrvPacket
     * @param extendedSecurity boolean
     * @exception AuthenticatorException
     */
    public void generateNegotiateResponse(SMBSrvSession sess, SMBSrvPacket respPkt, boolean extendedSecurity)
        throws AuthenticatorException
    {
        // If the client does not support extended security then return a standard negotiate response
        // with an 8 byte challenge
        
        if ( extendedSecurity == false)
        {
            super.generateNegotiateResponse( sess, respPkt, extendedSecurity);
            return;
        }
        
        // Make sure the extended security negotiation flag is set
        
        if (( respPkt.getFlags2() & SMBSrvPacket.FLG2_EXTENDEDSECURITY) == 0)
            respPkt.setFlags2( respPkt.getFlags2() + SMBSrvPacket.FLG2_EXTENDEDSECURITY);
        
        // Get the negotiate response byte area position
        
        int pos = respPkt.getByteOffset();
        byte[] buf = respPkt.getBuffer();
        
        // Pack the CIFS server GUID into the negotiate response

        UUID serverGUID = sess.getSMBServer().getServerGUID();
        
        System.arraycopy( serverGUID.getBytes(), 0, buf, pos, 16);
        pos += 16;
        
        // Set the negotiate response length
        
        respPkt.setByteCount(pos - respPkt.getByteOffset());
    }
    
    /**
     * Process the CIFS session setup request packet and build the session setup response.
     * <p>
     * This is the boundary between alfresco and JLAN.  So is responsible for logging and 
     * ensuring that the exceptions are correct for JLAN.
     * <p>
     * @param sess SMBSrvSession
     * @param reqPkt SMBSrvPacket
     * @exception SMBSrvException
     */
    public void processSessionSetup(final SMBSrvSession sess, final SMBSrvPacket reqPkt)
    throws SMBSrvException
    {
        try
        {
            processAlfrescoSessionSetup(sess, reqPkt);
        }
        catch (SMBSrvException e)
        {
            /*
             * A JLAN SMBSrvException is not human readable so we need to log the 
             * error before throwing, rather than logging the error here.
             */ 
            if(logger.isDebugEnabled())
            {
                logger.debug("Returning SMBSrvException to JLAN", e);
            }
            throw e;
        }
        catch (AlfrescoRuntimeException a)
        {
            Throwable c = a.getCause();
            
            if(c != null)
            {
                if(a.getCause() instanceof SMBSrvException)
                {
                    logger.error(c.getMessage(), c);
                    throw (SMBSrvException)c;
                }
            }
            logger.error(a.getMessage(), a);
            throw new SMBSrvException( SMBStatus.NTAccessDenied, SMBStatus.ErrDos, SMBStatus.DOSAccessDenied);

        }
        catch (Throwable t)
        {
            logger.error(t.getMessage(), t);
            throw new SMBSrvException( SMBStatus.NTAccessDenied, SMBStatus.ErrDos, SMBStatus.DOSAccessDenied);
        }
    }
    
    /**
     * Process the CIFS session setup request packet and build the session setup response
     * 
     * @param sess SMBSrvSession
     * @param reqPkt SMBSrvPacket
     * @exception SMBSrvException
     */
    public void processAlfrescoSessionSetup(SMBSrvSession sess, SMBSrvPacket reqPkt)
        throws SMBSrvException
    {
        //  Check that the received packet looks like a valid NT session setup andX request

        if (reqPkt.checkPacketIsValid(12, 0) == false)
        {
            if(logger.isErrorEnabled())
            {
                logger.error("Invalid packet received, return SMBStatus.NTInvalidParameter");
            }
            throw new SMBSrvException(SMBStatus.NTInvalidParameter, SMBStatus.ErrSrv, SMBStatus.SRVNonSpecificError);
        }
        
        //  Check if the request is using security blobs or the older hashed password format
        
        if ( reqPkt.getParameterCount() == 13)
        {
            //  Process the standard password session setup
                
            super.processSessionSetup( sess, reqPkt);
            return;
        }
        
        //  Extract the session details

        int maxBufSize = reqPkt.getParameter(2);
        int maxMpx     = reqPkt.getParameter(3);
        int vcNum      = reqPkt.getParameter(4);
        int secBlobLen = reqPkt.getParameter(7);
        int capabs     = reqPkt.getParameterLong(10);

        //  Extract the client details from the session setup request

        int dataPos = reqPkt.getByteOffset();
        byte[] buf = reqPkt.getBuffer();

        //  Determine if ASCII or unicode strings are being used
            
        boolean isUni = reqPkt.isUnicode();

        //  Make a note of the security blob position
        
        int secBlobPos = dataPos;
        
        //  Extract the clients primary domain name string

        dataPos += secBlobLen;
        reqPkt.setPosition( dataPos);
        
        String domain = "";

        if (reqPkt.hasMoreData()) 
        {

            //    Extract the callers domain name

            domain = reqPkt.unpackString(isUni);
            
            if (domain == null)
            {
                if(logger.isErrorEnabled())
                {
                    logger.error("Invalid packet received, domain is null");
                }
                throw new SMBSrvException(SMBStatus.NTInvalidParameter, SMBStatus.ErrSrv, SMBStatus.SRVNonSpecificError);
            }
        }

        //  Extract the clients native operating system

        String clientOS = "";

        if (reqPkt.hasMoreData()) 
        {

          //    Extract the callers operating system name

            clientOS = reqPkt.unpackString(isUni);
            
          if (clientOS == null)
          {
              if(logger.isErrorEnabled())
              {
                  logger.error("Invalid packet received, client OS is null");
              }
              throw new SMBSrvException( SMBStatus.NTInvalidParameter, SMBStatus.ErrSrv, SMBStatus.SRVNonSpecificError);
          }
        }

        //  Store the client maximum buffer size, maximum multiplexed requests count and client capability flags
            
        sess.setClientMaximumBufferSize(maxBufSize != 0 ? maxBufSize : SMBSrvSession.DefaultBufferSize);
        sess.setClientMaximumMultiplex(maxMpx);
        sess.setClientCapabilities(capabs);

        //  Create the client information and store in the session

        ClientInfo client = new AlfrescoClientInfo();
        client.setDomain(domain);
        client.setOperatingSystem(clientOS);
        
        client.setLogonType( ClientInfo.LogonNormal);

        // Set the remote address, if available
        
        if ( sess.hasRemoteAddress())
        {
            client.setClientAddress(sess.getRemoteAddress().getHostAddress());
        }
        //  Set the process id for this client, for multi-stage logons
        
        client.setProcessId( reqPkt.getProcessId());
        
        // Get the current sesion setup object, or null
        
        Object setupObj = sess.getSetupObject( client.getProcessId());
        
        //  Process the security blob
        
        byte[] respBlob = null;
        boolean isNTLMSSP = false;
        
        try
        {
            
            // Check if the blob has the NTLMSSP signature
            
            if ( secBlobLen >= NTLM.Signature.length) {
              
              // Check for the NTLMSSP signature
              
              int idx = 0;
              while ( idx < NTLM.Signature.length && buf[secBlobPos + idx] == NTLM.Signature[ idx])
                idx++;
              
              if ( idx == NTLM.Signature.length)
                isNTLMSSP = true;
            }
            
            // Process the security blob
            
            if ( isNTLMSSP == true)
            {
                //  DEBUG

                if (logger.isDebugEnabled())
                {
                    logger.debug("NT Session setup NTLMSSP, MID=" + reqPkt.getMultiplexId() + ", UID=" + reqPkt.getUserId() + ", PID=" + reqPkt.getProcessId());
                }
                //  Process an NTLMSSP security blob
    
                respBlob = doNtlmsspSessionSetup( sess, client, buf, secBlobPos, secBlobLen, isUni);
            }
            else
            {
                // Invalid blob type
                
                if(logger.isErrorEnabled())
                {
                    logger.error("Invalid packet received for Passthru Cifs Autenticator, not of type NTLMSSP");
                }
                
                throw new SMBSrvException( SMBStatus.NTInvalidParameter, SMBStatus.ErrSrv, SMBStatus.SRVNonSpecificError);
            }
        }
        catch (SMBSrvException ex)
        {
            //  Cleanup any stored context
            
            sess.removeSetupObject( client.getProcessId());
            
            //  Rethrow the exception
            
            throw ex;
        }

        // Debug
        
        if ( logger.isDebugEnabled())
        {
            logger.debug("User " + client.getUserName() + " logged on " + (client != null ? " (type " + client.getLogonTypeString() + ")" : ""));
        }
        
        //  Update the client information if not already set
            
        if ( sess.getClientInformation() == null ||
             sess.getClientInformation().getUserName().length() == 0) {
                      
            //  Set the client details for the session
                    
            sess.setClientInformation(client);
        }

        //  Get the response blob length, it can be null
        
        int respLen = respBlob != null ? respBlob.length : 0;
        
        //  Check if there is/was a session setup object stored in the session, this indicates a multi-stage session
        //  setup so set the status code accordingly
        
        boolean loggedOn = false;
        
        if ( isNTLMSSP == true || sess.hasSetupObject( client.getProcessId()) || setupObj != null)
        {
            //  NTLMSSP has two stages, if there is a stored setup object then indicate more processing
            //  required
            
            if ( sess.hasSetupObject( client.getProcessId()))
                reqPkt.setLongErrorCode( SMBStatus.NTMoreProcessingRequired);
            else
            {
                reqPkt.setLongErrorCode( SMBStatus.NTSuccess);
                
                // Indicate that the user is logged on
                
                loggedOn = true;
            }

            reqPkt.setParameterCount(4);
            reqPkt.setParameter(0, 0xFF);      //  No chained response
            reqPkt.setParameter(1, 0);         //  Offset to chained response
            
            reqPkt.setParameter(2, 0);         //  Action
            reqPkt.setParameter(3, respLen);
        }
        else
        {
            //  Build a completed session setup response
            
            reqPkt.setLongErrorCode( SMBStatus.NTSuccess);
            
            //  Build the session setup response SMB
    
            reqPkt.setParameterCount(12);
            reqPkt.setParameter(0, 0xFF);      //  No chained response
            reqPkt.setParameter(1, 0);         //  Offset to chained response
    
            reqPkt.setParameter(2, SMBSrvSession.DefaultBufferSize);
            reqPkt.setParameter(3, SMBSrvSession.NTMaxMultiplexed);
            reqPkt.setParameter(4, 0);         //  virtual circuit number
            reqPkt.setParameterLong(5, 0);     //  session key
            reqPkt.setParameter(7, respLen);
                                                //  security blob length
            reqPkt.setParameterLong(8, 0);     //  reserved
            reqPkt.setParameterLong(10, getServerCapabilities());
            
            // Indicate that the user is logged on
            
            loggedOn = true;
        }
        
        // If the user is logged on then allocate a virtual circuit

        int uid = 0;
        
        if ( loggedOn == true) {

			// Check for virtual circuit zero, disconnect any other sessions from this client
			
			if ( vcNum == 0 && hasSessionCleanup()) {
			
				// Disconnect other sessions from this client, cleanup any open files/locks/oplocks
				
				int discCnt = sess.disconnectClientSessions();

				// DEBUG

				if ( discCnt > 0 && Debug.EnableInfo && sess.hasDebug(SMBSrvSession.DBG_NEGOTIATE))
					Debug.println("[SMB] Disconnected " + discCnt + " existing sessions from client, sess=" + sess);
			}
			
          // Clear any stored session setup object for the logon
          
          sess.removeSetupObject( client.getProcessId());
          
          // Check if the user is an administrator
          
          checkForAdminUserName( client);
          
          // Get the users home folder node, if available
          
          getHomeFolderForUser( client);
          
          // Create a virtual circuit for the new logon
          
          VirtualCircuit vc = new VirtualCircuit( vcNum, client);
          uid = sess.addVirtualCircuit( vc);
          
          if ( uid == VirtualCircuit.InvalidUID)
          {            
              logger.error("Failed to allocate UID for virtual circuit, " + vc);
            
              // Failed to allocate a UID
              throw new SMBSrvException(SMBStatus.NTLogonFailure, SMBStatus.ErrDos, SMBStatus.DOSAccessDenied);
          }
          else if ( logger.isDebugEnabled()) 
          {
            
              // DEBUG
            
              logger.debug("Allocated UID=" + uid + " for VC=" + vc);
          }
        }
        
        // Common session setup response
        
        reqPkt.setCommand( reqPkt.getCommand());
        reqPkt.setByteCount(0);

        reqPkt.setTreeId( 0);
        reqPkt.setUserId( uid);

        //  Set the various flags

        int flags = reqPkt.getFlags();
        flags &= ~SMBSrvPacket.FLG_CASELESS;
        reqPkt.setFlags(flags);
        
        int flags2 = SMBSrvPacket.FLG2_LONGFILENAMES + SMBSrvPacket.FLG2_EXTENDEDSECURITY + SMBSrvPacket.FLG2_LONGERRORCODE;
        if ( isUni)
          flags2 += SMBSrvPacket.FLG2_UNICODE;
        reqPkt.setFlags2( flags2);
        
        //  Pack the security blob

        int pos = reqPkt.getByteOffset();
        buf = reqPkt.getBuffer();

        if ( respBlob != null)
        {
            System.arraycopy( respBlob, 0, buf, pos, respBlob.length);
            pos += respBlob.length;
        }
        
        // Pack the OS, dialect and domain name strings
        
        if ( isUni)
            pos = DataPacker.wordAlign(pos);

        pos = DataPacker.putString("Java", buf, pos, true, isUni);
        pos = DataPacker.putString("Alfresco CIFS Server " + sess.getServer().isVersion(), buf, pos, true, isUni);
        pos = DataPacker.putString(getCIFSConfig().getDomainName(), buf, pos, true, isUni);
        
        reqPkt.setByteCount(pos - reqPkt.getByteOffset());
    }

    /**
     * Process an NTLMSSP security blob
     * 
     * @param sess SMBSrvSession
     * @param client ClientInfo
     * @param secbuf byte[]
     * @param secpos int
     * @param seclen int
     * @param unicode boolean
     * @exception SMBSrvException
     */
    private final byte[] doNtlmsspSessionSetup( SMBSrvSession sess, ClientInfo client,
            byte[] secbuf, int secpos, int seclen, boolean unicode) throws SMBSrvException
    {
        // Determine the NTLmSSP message type
        
        int msgType = NTLMMessage.isNTLMType( secbuf, secpos);
        byte[] respBlob = null;
        
        if ( msgType == -1)
        {
            if ( logger.isErrorEnabled())
            {
                logger.error("Invalid NTLMSSP token received, Token=" + HexDump.hexString( secbuf, secpos, seclen, " ") );
            }

            // Return a logon failure status
            
            throw new SMBSrvException( SMBStatus.NTLogonFailure, SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
        }
        
        // Check for a type 1 NTLMSSP message
        
        else if ( msgType ==  NTLM.Type1)
        {
            // Create the type 1 NTLM message from the token
            
            Type1NTLMMessage type1Msg = new Type1NTLMMessage( secbuf, secpos, seclen);
            
            //  Build the type 2 NTLM response message
            //
            //  Get the flags from the client request and mask out unsupported features
            
            int ntlmFlags = type1Msg.getFlags() & NTLM_FLAGS;
            
            //  Generate a challenge for the response
            
            NTLanManAuthContext ntlmCtx = (NTLanManAuthContext) getAuthContext( sess);
            
            //  Build a type2 message to send back to the client, containing the challenge

            String domain = sess.getSMBServer().getServerName();
            
            List<TargetInfo> tList = new ArrayList<TargetInfo>();
            
            tList.add(new TargetInfo(NTLM.TargetDomain, domain));
            tList.add(new TargetInfo(NTLM.TargetServer, sess.getServerName()));
            tList.add(new TargetInfo(NTLM.TargetDNSDomain, domain));
            tList.add(new TargetInfo(NTLM.TargetFullDNS, domain));
            
            ntlmFlags = NTLM.FlagChallengeAccept + NTLM.FlagRequestTarget +
                        NTLM.FlagNegotiateNTLM + NTLM.FlagNegotiateUnicode +
                        NTLM.FlagKeyExchange + NTLM.FlagTargetInfo + NTLM.Flag56Bit;
            
            // NTLM.FlagAlwaysSign + NTLM.FlagNegotiateSign +
            
            Type2NTLMMessage type2Msg = new Type2NTLMMessage();
            
            type2Msg.buildType2(ntlmFlags, domain, ntlmCtx.getChallenge(), null, tList);

            //  Store the type 2 message in the session until the session setup is complete
            
            sess.setSetupObject( client.getProcessId(), type2Msg);
            
            // Set the response blob using the type 2 message
            
            respBlob = type2Msg.getBytes();
        }
        else if ( msgType == NTLM.Type3)
        {
            //  Create the type 3 NTLM message from the token
            
            Type3NTLMMessage type3Msg = new Type3NTLMMessage( secbuf, secpos, seclen, unicode);
            
            //  Make sure a type 2 message was stored in the first stage of the session setup
            
            if ( sess.hasSetupObject( client.getProcessId()) == false || sess.getSetupObject( client.getProcessId()) instanceof Type2NTLMMessage == false)
            {
                //  Clear the setup object
                
                sess.removeSetupObject( client.getProcessId());
                
                //  Return a logon failure
                logger.error("NTLMSSP Logon failure - type 2 message not found");
                
                throw new SMBSrvException( SMBStatus.NTLogonFailure, SMBStatus.ErrDos, SMBStatus.DOSAccessDenied);
            }

            //  Determine if the client sent us NTLMv1 or NTLMv2
            
            if ( type3Msg.hasFlag( NTLM.Flag128Bit) && type3Msg.hasFlag( NTLM.FlagNTLM2Key))
            {
                    logger.error("Received NTLMSSP/NTLMv2, not supported");
                
                //  Return a logon failure

                throw new SMBSrvException( SMBStatus.NTLogonFailure, SMBStatus.ErrDos, SMBStatus.DOSAccessDenied);
            }
            else
            {
                //  Looks like an NTLMv1 blob
                
                doNTLMv1Logon( sess, client, type3Msg);

                //  Debug
                
                if ( logger.isDebugEnabled())
                    logger.debug("Logged on using NTLMSSP/NTLMv1");
            }
        }
        
        // Return the response blob
        
        return respBlob;
    }

    /**
     * Perform an NTLMv1 logon using the NTLMSSP type3 message
     * 
     * @param sess SMBSrvSession
     * @param client ClientInfo
     * @param type3Msg Type3NTLMMessage
     * @exception SMBSrvException 
     */
    private final void doNTLMv1Logon(SMBSrvSession sess, final ClientInfo client, Type3NTLMMessage type3Msg)
        throws SMBSrvException
    {
        //  Get the type 2 message that contains the challenge sent to the client
        
        sess.removeSetupObject( client.getProcessId());
        
        // Get the NTLM logon details
        
        final String userName = type3Msg.getUserName();
        
        //  Check for a null logon
        
        if ( userName.length() == 0)
        {
            //  DEBUG
            
            if ( logger.isDebugEnabled())
                logger.debug("Null logon");
            
            //  Indicate a null logon in the client information
            
            client.setLogonType( ClientInfo.LogonNull);
            return;
        }

        // Find the active authentication session details for the server session

        PassthruDetails passDetails = m_sessions.get(sess.getUniqueId());

        if (passDetails != null)
        {
            try
            {
                // Authenticate the user by passing the hashed password to the authentication server
                // using the session that has already been setup.

                AuthenticateSession authSess = passDetails.getAuthenticateSession();
                authSess.doSessionSetup( type3Msg.getDomain(), userName, null, type3Msg.getLMHash(), type3Msg.getNTLMHash(), 0);

                // Check if the user has been logged on as a guest

                if (authSess.isGuest())
                {
                    // Check if the local server allows guest access

                    if (allowGuest() == true)
                    {
                        //  Get a guest authentication token
                        
                        doGuestLogon( client, sess);
                        
                        // Debug

                        if (logger.isDebugEnabled())
                            logger.debug("Passthru authenticate user=" + userName + ", GUEST");
                    }
                }
                else
                {
                    // Wrap the service calls in a transaction
                    
                    doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
                    {

                        public Object execute() throws Throwable
                        {
                            // Map the passthru username to an Alfresco person

                            NodeRef userNode = getPersonService().getPerson(userName);
                            if (userNode != null)
                            {
                                // Get the person name and use that as the current user to line up with permission
                                // checks

                                String personName = (String) getNodeService().getProperty(userNode,
                                        ContentModel.PROP_USERNAME);
                                getAuthenticationComponent().setCurrentUser(personName);

                                // DEBUG

                                if (logger.isDebugEnabled())
                                    logger.debug("Setting current user using person " + personName + " (username "
                                            + userName + ")");
                            }
                            else
                            {
                                // Set using the user name

                                getAuthenticationComponent().setCurrentUser(userName);

                                // DEBUG

                                if (logger.isDebugEnabled())
                                    logger.debug("Setting current user using username " + userName);
                            }

                            // Get the authentication token and store

                            AlfrescoClientInfo alfClient = (AlfrescoClientInfo) client;
                            alfClient.setAuthenticationTicket(getAuthenticationService().getCurrentTicket());

                            // Indicate that the client is logged on

                            client.setLogonType(ClientInfo.LogonNormal);

                            // Debug

                            if (logger.isDebugEnabled())
                                logger.debug("Passthru authenticate user=" + userName + ", FULL");

                            return null;
                        }
                    });

                }
                
                // Update the client details
                
                client.setDomain( type3Msg.getDomain());
                client.setUserName( userName);
            }
            catch (Exception ex)
            {
                if(ex instanceof SMBSrvException)
                {
                    throw (SMBSrvException)ex;
                }

                logger.error("unable to log on "+  ex.getMessage(), ex);
                
                // Indicate logon failure
                
                throw new SMBSrvException( SMBStatus.NTErr, SMBStatus.NTLogonFailure);
            }
            finally
            {
                // Remove the passthru session from the active list

                m_sessions.remove(sess.getUniqueId());

                // Close the passthru authentication session

                try
                {

                    // Close the authentication session

                    AuthenticateSession authSess = passDetails.getAuthenticateSession();
                    authSess.CloseSession();

                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Closed auth session, sessId=" + authSess.getSessionId());
                    }
                }
                catch (Exception ex)
                {

                    logger.error("Passthru error closing session (auth user)", ex);
                }
            }
        }
        else
        {
            logger.error("  No PassthruDetails for " + sess.getUniqueId() + ", check server list/domain mappings");
            
            // Indicate logon failure
            
            throw new SMBSrvException( SMBStatus.NTErr, SMBStatus.NTLogonFailure);
        }
    }

    @Override
    protected boolean validateAuthenticationMode()
    {
        // Check if the appropriate authentication component type is configured
        AuthenticationComponent authenticationComponent = getAuthenticationComponent();
        return !(authenticationComponent instanceof NLTMAuthenticator)
                || ((NLTMAuthenticator) authenticationComponent).getNTLMMode() != NTLMMode.MD4_PROVIDER;
    }

    /**
     * Initialize the authenticator via the config service
     * 
     * @param config
     *            ServerConfiguration
     * @param params
     *            ConfigElement
     * @exception InvalidConfigurationException
     */
    public void initialize(ServerConfiguration config, ConfigElement params) throws InvalidConfigurationException
    {
        // Manually construct our own passthru server list
        PassthruServerFactory factory = new PassthruServerFactory();
        
        // Check if the offline check interval has been specified
        ConfigElement checkInterval = params.getChild("offlineCheckInterval");
        if ( checkInterval != null)
        {
            try
            {
                // Validate the check interval value

                factory.setOfflineCheckInterval(Integer.parseInt(checkInterval.getValue()));
                
            }
            catch (NumberFormatException ex)
            {
                throw new InvalidConfigurationException("Invalid offline check interval specified");
            }
        }

        // Check if the session timeout has been specified

        ConfigElement sessTmoElem = params.getChild("Timeout");
        if (sessTmoElem != null)
        {

            try
            {

                // Validate the session timeout value

                factory.setTimeout(Integer.parseInt(sessTmoElem.getValue()));
            }
            catch (NumberFormatException ex)
            {
                throw new InvalidConfigurationException("Invalid timeout value specified");
            }
        }

        // Check if the local server should be used

        // Note that this option is not supported for singleton bean initialization, because the local server name is
        // part of the file server subsystem rather than the authentication subsystem

        if (params.getChild("LocalServer") != null)
        {

            // Get the local server name, trim the domain name
            String server = getCIFSConfig().getServerName();
            if(server == null)
                throw new AlfrescoRuntimeException("Passthru authenticator failed to get local server name");
            factory.setServer(server);
        }

        // Check if a server name has been specified

        ConfigElement srvNamesElem = params.getChild("Server");

        if (srvNamesElem != null && srvNamesElem.getValue().length() > 0)
        {
            factory.setServer(srvNamesElem.getValue());
        }

        // Check if the local domain/workgroup should be used

        if (params.getChild("LocalDomain") != null)
        {
            // Get the local domain/workgroup name
            
            factory.setDomain(getCIFSConfig().getDomainName());            
        }

        // Check if a domain name has been specified

        ConfigElement domNameElem = params.getChild("Domain");

        if (domNameElem != null && domNameElem.getValue().length() > 0)
        {
            factory.setDomain(domNameElem.getValue());
        }

        // Check if a protocol order has been set

        ConfigElement protoOrderElem = params.getChild("ProtocolOrder");

        if (protoOrderElem != null && protoOrderElem.getValue().length() > 0)
        {
            factory.setProtocolOrder(protoOrderElem.getValue());
        }
        
        // Complete initialization
        factory.afterPropertiesSet();
        setPassthruServers((PassthruServers) factory.getObject());
        // Remember that we have to shut down the servers
        m_localPassThruServers = true;
        
        // Call the base class
        super.initialize(config, params);
        
        // Install the SMB server listener so we receive callbacks when sessions are
        // opened/closed on the SMB server

        SMBServer smbServer = (SMBServer) config.findServer( "SMB");
        if ( smbServer != null)
            smbServer.addSessionListener(this);
        
        // Note that for container-based initialization, session listeners can be registered directly on CIFSSeverBean
    }

    /**
     * Return the server capability flags
     * 
     * @return int
     */
    public int getServerCapabilities()
    {
        return Capability.Unicode + Capability.RemoteAPIs + Capability.NTSMBs + Capability.NTFind +
               Capability.NTStatus + Capability.LargeFiles + Capability.LargeRead + Capability.LargeWrite +
               Capability.ExtendedSecurity + Capability.InfoPassthru + Capability.Level2Oplocks;
    }
    
    /**
     * Close the authenticator, perform cleanup
     */
    public void closeAuthenticator()
    {
        // Close the passthru authentication server list
        
        if ( m_localPassThruServers && m_passthruServers != null)
            m_passthruServers.shutdown();
    }

    /**
     * SMB server session closed notification
     * 
     * @param sess SrvSession
     */
    public void sessionClosed(SrvSession sess)
    {

        // Check if there is an active session to the authentication server for this local
        // session

        PassthruDetails passDetails = m_sessions.get(sess.getUniqueId());

        if (passDetails != null)
        {

            // Remove the passthru session from the active list

            m_sessions.remove(sess.getUniqueId());

            // Close the passthru authentication session

            try
            {

                // Close the authentication session

                AuthenticateSession authSess = passDetails.getAuthenticateSession();
                authSess.CloseSession();

                // DEBUG

                if (logger.isDebugEnabled())
                    logger.debug("Closed auth session, sessId=" + authSess.getSessionId());
            }
            catch (Exception ex)
            {

                // Debug

                logger.error("Passthru error closing session (closed)", ex);
            }
        }
    }

    /**
     * SMB server session created notification
     * 
     * @param sess SrvSession
     */
    public void sessionCreated(SrvSession sess)
    {
    }

    /**
     * User successfully logged on notification
     * 
     * @param sess SrvSession
     */
    public void sessionLoggedOn(SrvSession sess)
    {
        // Check the client information for the session
        
        ClientInfo cInfo = sess.getClientInformation();
        
        if ( cInfo == null || cInfo.isNullSession())
            return;
        
        // Check if there is an active session to the authentication server for this local
        // session

        PassthruDetails passDetails = m_sessions.get(sess.getUniqueId());

        if (passDetails != null)
        {
            // Remove the passthru session from the active list

            m_sessions.remove(sess.getUniqueId());

            // Close the passthru authentication session

            try
            {
                // Close the authentication session

                AuthenticateSession authSess = passDetails.getAuthenticateSession();
                authSess.CloseSession();

                // DEBUG

                if (logger.isDebugEnabled())
                    logger.debug("Closed auth session, sessId=" + authSess.getSessionId());
            }
            catch (Exception ex)
            {

                // Debug

                logger.error("Passthru error closing session (logon)", ex);
            }
        }
    }
}
