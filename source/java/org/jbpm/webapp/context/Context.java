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

import java.util.HashMap;
import java.util.Map;

public class Context {

  static ThreadLocal contextsThreadLocal = new ThreadLocal();
  
  public static void create() {
    contextsThreadLocal.set(new HashMap());
  }
  
  public static void destroy() {
    contextsThreadLocal.set(null);
  }
  
  public static Object getContext(Class clazz) {
    Map contexts = (Map) contextsThreadLocal.get();
    Object context = contexts.get(clazz);
    if (context==null) {
      try {
        context = clazz.newInstance();
        contexts.put(clazz, context);
      } catch (Exception e) {
        throw new RuntimeException("couldn't instantiate context '"+clazz.getName()+"'");
      }
    }
    return context;
  }
  
  public static PersistenceContext getPersistenceContext() {
    return (PersistenceContext) getContext(PersistenceContext.class);
  }

  public static BpmContext getBpmContext() {
    return (BpmContext) getContext(BpmContext.class);
  }
}
