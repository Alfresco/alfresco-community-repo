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
package org.jbpm.webapp.bean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.model.SelectItem;

import org.hibernate.Session;
import org.jbpm.JbpmContext;
import org.jbpm.identity.User;
import org.jbpm.identity.hibernate.IdentitySession;

public class UserBean {

  String userName;

  public String getUserName() {
    return userName;
  }

  public void setUserName(String name) {
    this.userName = name;
  }
  
  public String login() {
    JbpmContext.getCurrentJbpmContext().setActorId(userName);
    return "home";
  }

  public List getUsers() {
    Session session = JbpmContext.getCurrentJbpmContext().getSession();
    IdentitySession identitySession = new IdentitySession(session);
    return identitySession.getUsers();
  }

  public List getUserSelectItems() {
    List userSelectItems = new ArrayList();

    Iterator iter = getUsers().iterator();
    while (iter.hasNext()) {
      User user = (User) iter.next();
      userSelectItems.add(new UserSelectItem(user));
    }
    
    return userSelectItems;
  }
  
  public static class UserSelectItem extends SelectItem {
    private static final long serialVersionUID = 1L;
    public UserSelectItem(User user) {
      setValue(user.getName());
      setLabel(user.getName());
    }
  }
}
