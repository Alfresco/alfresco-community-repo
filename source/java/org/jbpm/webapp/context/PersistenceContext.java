/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jbpm.webapp.context;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.jbpm.db.JbpmSession;
import org.jbpm.db.JbpmSessionFactory;
import org.jbpm.identity.hibernate.IdentitySession;

public class PersistenceContext {

  static String jndiName = "java:/jbpm/SessionFactory";
  static JbpmSessionFactory jbpmSessionFactory = null;
  static {
    try {
      InitialContext initialContext = new InitialContext();
      Object o = initialContext.lookup(jndiName);
      SessionFactory sessionFactory = (SessionFactory) PortableRemoteObject.narrow(o, SessionFactory.class);
      jbpmSessionFactory = new JbpmSessionFactory(null, sessionFactory);
    } catch (Exception e) {
      throw new RuntimeException("couldn't get the hibernate session factory from jndi entry '"+jndiName+"'", e);
    }
  }

  boolean isRollbackOnly;
  JbpmSession jbpmSession;
  IdentitySession identitySession;
  
  public void beginTransaction() {
    isRollbackOnly = false;
    log.debug("beginning transaction");
    jbpmSession = jbpmSessionFactory.openJbpmSessionAndBeginTransaction();
    identitySession = new IdentitySession(jbpmSession.getSession());
  }

  public void endTransaction() {
    if (isRollbackOnly) {
      log.debug("rolling back transaction");
      jbpmSession.rollbackTransactionAndClose();
    } else {
      log.debug("committing transaction");
      jbpmSession.commitTransactionAndClose();
    }
  }

  public void setRollbackOnly() {
    isRollbackOnly = true;
  }

  public IdentitySession getIdentitySession() {
    return identitySession;
  }

  public JbpmSession getJbpmSession() {
    return jbpmSession;
  }
  
  private static final Log log = LogFactory.getLog(PersistenceContext.class);
}
