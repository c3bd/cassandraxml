package imc.jettyserver;

import imc.disxmldb.XMLDBStore;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.mortbay.http.HttpContext;
import org.mortbay.http.HttpListener;
import org.mortbay.http.HttpServer;
import org.mortbay.http.SocketListener;
import org.mortbay.http.SslListener;
import org.mortbay.http.handler.ForwardHandler;
import org.mortbay.http.handler.NotFoundHandler;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.servlet.WebApplicationHandler;
import org.mortbay.util.ThreadedServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author:xiafan xiafan68@gmail.com
 * @version: Sep 8, 2011 0.1
 */

public class DisXMLDbServer{

	public interface ServletBootstrap {
		void bootstrap(Properties properties, WebApplicationHandler handler);
	}
	static {
		
	}

	private  static Logger logger = LoggerFactory.getLogger(DisXMLDbServer.class);

	// command-line options
	private final static int HELP_OPT = 'h';
	private final static int DEBUG_OPT = 'd';
	private final static int HTTP_PORT_OPT = 'p';
	private final static int THREADS_OPT = 't';

	private final static String DEFAULT_HTTP_LISTENER_PORT = "8080";

	private static Properties DEFAULT_PROPERTIES = new Properties();
	static {
		DEFAULT_PROPERTIES.setProperty("webdav.enabled", "no");
		DEFAULT_PROPERTIES.setProperty("rest.enabled", "no");
		DEFAULT_PROPERTIES.setProperty("xmlrpc.enabled", "yes");
		DEFAULT_PROPERTIES.setProperty("webdav.authentication", "basic");
		DEFAULT_PROPERTIES.setProperty("rest.form.encoding", "UTF-8");
		DEFAULT_PROPERTIES.setProperty("rest.container.encoding", "UTF-8");
		DEFAULT_PROPERTIES.setProperty("rest.param.dynamic-content-type", "no");
	}

	private HttpServer httpServer;

	private Map forwarding = new HashMap();
	private Map listeners = new HashMap();
	private Map filters = new HashMap();

	public DisXMLDbServer() {

	}

	public void run(String[] args) throws Exception {
		printNotice();

		// set default properties
		Properties props = new Properties(DEFAULT_PROPERTIES);

		// set default listener
		Properties defaultListener = new Properties();
		if (args.length == 2) {
			defaultListener.setProperty("port", args[1]);
			defaultListener.setProperty("host", args[0]);
			defaultListener.setProperty("address", args[0]);
		} else {
			defaultListener.setProperty("port", DEFAULT_HTTP_LISTENER_PORT);
		}
		listeners.put("http", defaultListener);

		int threads = 5;

		initXMLDB();
		List<String> servlets = new ArrayList<String>();
		servlets.add("xmlrpc");
		startHttpServer(servlets, props);

		logger.info("");
		logger.info("Server launched ...");
		logger.info("Installed services:");
		logger.info("-----------------------------------------------");
		Set listenerProtocols = listeners.keySet();
		for (int i = 0; i < servlets.size(); i++) {
			String name = (String) servlets.get(i);
			if (props.getProperty(name + ".enabled").equalsIgnoreCase("yes")) {
				for (Iterator itProtocol = listenerProtocols.iterator(); itProtocol
						.hasNext();) {
					String listenerProtocol = (String) itProtocol.next();
					Properties listenerProperties = (Properties) listeners
							.get(listenerProtocol);
					String host = listenerProperties.getProperty("host",
							"localhost");
					String port = listenerProperties.getProperty("port");
					logger.info(name + ":\t" + host + ":" + port
							+ props.getProperty(name + ".context"));
				}
			}
		}
	}

	public boolean isStarted() {
		if (httpServer == null)
			return false;
		return httpServer.isStarted();
	}

	/**
	     * 
	     */
	private void initXMLDB() throws Exception {
		XMLDBStore.instance();
	}

	/**
	 * Configures a minimal Jetty webserver (no webapplication support, no file
	 * system access) and registers the WebDAV and REST servlets.
	 * 
	 * @throws UnknownHostException
	 * @throws IllegalArgumentException
	 * @throws MultiException
	 */
	private void startHttpServer(List servlets, Properties props)
			throws Exception {
		httpServer = new HttpServer();

		// setup listeners
		Set listenerProtocols = listeners.keySet();
		for (Iterator itProtocol = listenerProtocols.iterator(); itProtocol
				.hasNext();) {
			String listenerProtocol = (String) itProtocol.next();
			Properties listenerProps = (Properties) listeners
					.get(listenerProtocol);
			HttpListener listener = null;

			/** currently support http and https listeners */
			if (listenerProtocol.equals("http")) {
				listener = new SocketListener();
			} else if (listenerProtocol.equals("https")) {
				listener = new SslListener();

				Properties params = (Properties) listenerProps.get("params");

				// set the keystore if specified
				if (params.containsKey("keystore")) {
					String keystore = params.getProperty("keystore");
					((SslListener) listener).setKeystore(keystore);
				}
			}

			if (listener != null) {
				// configure lisetener
				listener.setHost(listenerProps.getProperty("host", "localhost"));
				String port = (String) listenerProps.get("port");
				listener.setPort(Integer.parseInt(port));
				String address = (String) listenerProps.get("address");
				if (address != null) {
					InetAddress iaddress = InetAddress.getByName(address);
					((ThreadedServer) listener).setInetAddress(iaddress);
				}
				((ThreadedServer) listener).setMinThreads(5);
				((ThreadedServer) listener).setMaxThreads(50);

				httpServer.addListener(listener);
			}
		}

		HttpContext context = new HttpContext();
		context.setContextPath("/");

		// Setting up resourceBase, if it is possible
		// This one is needed by many Servlets which depend
		// on a not null context.getResourceBase() value
		//File eXistHome = ConfigurationHelper.getExistHome();
		//if (eXistHome != null)
		//	context.setResourceBase(eXistHome.getAbsolutePath());
		context.setResourceBase("/exist/");
		WebApplicationHandler webappHandler = new WebApplicationHandler();

		// TODO: this should be read from a configuration file
		Map bootstrappers = new HashMap();
		bootstrappers.put("xmlrpc", new ServletBootstrap() {
			public void bootstrap(Properties props,
					WebApplicationHandler webappHandler) {
				String path = props.getProperty("xmlrpc.context", "/xmlrpc/*");
				webappHandler.addServlet("RpcServlet", path,
						"org.exist.xmlrpc.RpcServlet");
			}
		});

		for (int i = 0; i < servlets.size(); i++) {
			String name = (String) servlets.get(i);
			ServletBootstrap bootstrapper = (ServletBootstrap) bootstrappers
					.get(name);
			if (bootstrapper != null) {
				bootstrapper.bootstrap(props, webappHandler);
			} else {
				String path = props.getProperty(name + ".context", "/" + name
						+ "/*");
				String sname = props.getProperty(name + ".name", name);
				ServletHolder servlet = webappHandler.addServlet(sname, path,
						props.getProperty(name + ".class"));
				String paramPrefix = name + ".param.";
				for (Enumeration pnames = props.propertyNames(); pnames
						.hasMoreElements();) {
					String pname = (String) pnames.nextElement();
					if (pname.startsWith(paramPrefix)) {
						String theName = pname.substring(paramPrefix.length());
						servlet.setInitParameter(theName,
								props.getProperty(pname));
					}
				}
			}
		}

		// setup filters
		Set filterClasses = filters.keySet();
		for (Iterator itFilterClass = filterClasses.iterator(); itFilterClass
				.hasNext();) {
			String filterClass = (String) itFilterClass.next();
			Properties filterProps = (Properties) filters.get(filterClass);

			org.mortbay.jetty.servlet.FilterHolder filterHolder = webappHandler
					.defineFilter(filterClass, filterClass);
			// TODO: putAll may be wrong??? check this
			filterHolder.putAll((Properties) filterProps.get("params"));

			// TODO: Dispatcher.__DEFAULY may be wrong??? check this
			webappHandler
					.addFilterPathMapping(filterProps.getProperty("path"),
							filterClass,
							org.mortbay.jetty.servlet.Dispatcher.__DEFAULT);
		}

		if (forwarding.size() > 0) {
			ForwardHandler forward = new ForwardHandler();
			// forward.setHandleQueries(true); //TODO needed if you wish to pass
			// querystring parameters - should maybe be a server.xml option?
			for (Iterator i = forwarding.keySet().iterator(); i.hasNext();) {
				String path = (String) i.next();
				String destination = (String) forwarding.get(path);
				if (path.length() == 0)
					forward.setRootForward(destination);
				else
					forward.addForward(path, destination);
			}

			context.addHandler(forward);
		}

		context.addHandler(webappHandler);
		context.addHandler(new NotFoundHandler());
		httpServer.addContext(context);
		httpServer.start();
	}

	private static void printHelp() {
		logger.info("Usage: java " + DisXMLDbServer.class.getName()
				+ " [options]");
	}

	public static void printNotice() {
	}

	public void shutdown() {
		//shutdown the database
		try {
			httpServer.stop(true);
			XMLDBStore.instance().shutdown();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	class ShutdownListener {

		public void shutdown(String dbname, int remainingInstances) {
			if (remainingInstances == 0) {
				// give the server a 1s chance to complete pending requests
				Timer timer = new Timer();
				timer.schedule(new TimerTask() {
					public void run() {
						logger.info("killing threads ...");
						try {
							httpServer.stop();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						System.exit(0);
					}
				}, 1000);
			}
		}
	}

	public static void main(String[] args) {
		DisXMLDbServer server = new DisXMLDbServer();
		try {
		
			server.run(args);
		} catch (Exception e) {
			logger.error(
					"An exception occurred while launching the server: "
							+ e.getMessage(), e);
		}
	}

}
