/*
 *  eXist Open Source Native XML Database
 *  Copyright (C) 2001-07 The eXist Project
 *  http://exist-db.org
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * $Id$
 */
package org.exist.xmlrpc;

import imc.disxmldb.security.User;
import imc.disxmldb.security.XMLDBSecurityManager;

import org.slf4j.*;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.common.XmlRpcHttpRequestConfig;
import org.apache.xmlrpc.server.RequestProcessorFactoryFactory;
//import org.exist.EXistException;
//import org.exist.security.User;
//import org.exist.storage.BrokerPool;
//import org.exist.util.XMLDBException;

/**
 * Factory creates a new handler for each XMLRPC request. For eXist, the handler
 * is implemented by class {@link org.sheepdog.xmlrpc.RpcConnection}. The
 * factory is needed to make sure that each RpcConnection is properly
 * initialized.
 */
public class XmldbRequestProcessorFactory implements
		RequestProcessorFactoryFactory.RequestProcessorFactory {

	private final static Logger LOG = LoggerFactory
			.getLogger(XmldbRequestProcessorFactory.class);

	public final static int CHECK_INTERVAL = 2000;

	// protected BrokerPool brokerPool;

	protected int connections = 0;

	protected long lastCheck = System.currentTimeMillis();

	/** id of the database registred against the BrokerPool */
	protected String databaseid = null;// BrokerPool.DEFAULT_INSTANCE_NAME;

	/**
	 * 
	 * @param databaseid
	 * @throws EXistException
	 */
	public XmldbRequestProcessorFactory(String databaseid) {
		if (databaseid != null && !"".equals(databaseid))
			this.databaseid = databaseid;

	}

	public Object getRequestProcessor(XmlRpcRequest pRequest)
			throws XmlRpcException {
		checkResultSets();
		XmlRpcHttpRequestConfig config = (XmlRpcHttpRequestConfig) pRequest
				.getConfig();
		User user = authenticate(config.getBasicUserName(),
				config.getBasicPassword());
		return new RpcConnection(this, user);
	}

	protected User authenticate(String username, String password)
			throws XmlRpcException {
		// assume guest user if no user is specified
		// set a password for admin to permit this
		if (username == null) {
			username = "guest";
			password = "guest";
		}
		// TODO check user
		// User u = new User(username, password);
		User user = XMLDBSecurityManager.getInstance().fetchUser(username);
		if (user == null || !user.validate(password)) {
			throw new XmlRpcException("user or password is wrong");
		}
		return user;
	}

	protected void checkResultSets() {
		if (System.currentTimeMillis() - lastCheck > CHECK_INTERVAL) {
			// resultSets.checkTimestamps();
			lastCheck = System.currentTimeMillis();
		}
	}

	public synchronized void shutdown() {
		LOG.info("shutdown");
	}
}
