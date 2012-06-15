/* ***********************************************************************
 * 
 * ARINI CONFIDENTIAL
 * __________________
 * 
 *  Copyright Arini Software Inc. 
 *  All Rights Reserved.
 * 
 * NOTICE:  All information contained herein is, and remains
 * the property of Arini Software Inc. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Arini Software Inc.
 * and its suppliers and may be covered by Canadian and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Arini Software Inc.
 *
 *************************************************************************/
package ca.arini.d3_generator.launch;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.session.HashSessionManager;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.google.inject.Guice;
import com.google.inject.servlet.GuiceFilter;

public class D3GeneratorLauncher {

    private static final int DEFAULT_PORT = 8080;

    private static final String WEB_APP_DIRECTORY = "src/main/webapp/";

    private static final String PORT = "PORT";

    private static int getPort() {
        String port = System.getenv(PORT);

        if (port == null || port.isEmpty()) {
            return DEFAULT_PORT;
        }

        return Integer.parseInt(port);
    }

    public static void main(String[] args) throws Exception {
        start(new D3GeneratorConfiguration(getPort()));
    }

    public static void start(D3GeneratorConfiguration configuration) throws Exception {
        Guice.createInjector(configuration);
        startServer(configuration.getPort());
    }

    private static void startServer(int port) throws Exception {
        // http://wiki.eclipse.org/Jetty/Howto/Deal_with_Locked_Windows_Files
        // http://stackoverflow.com/questions/184312/how-to-make-jetty-dynamically-load-static-pages
        ServletContextHandler contextHandler = new ServletContextHandler();
        contextHandler.setSessionHandler(new SessionHandler(
                new HashSessionManager()));
        contextHandler.setContextPath("/");
        contextHandler.setResourceBase(WEB_APP_DIRECTORY);

        ServletHolder holder = new ServletHolder(new DefaultServlet());
        holder.setInitParameter("useFileMappedBuffer", "false");
        holder.setInitOrder(0);
        contextHandler.getServletHandler().addServletWithMapping(holder, "/");

        // Guice - reroute all requests
        contextHandler.addFilter(GuiceFilter.class, "/*", 0);

        Server server = new Server(port);
        server.setHandler(contextHandler);
        server.start();
        server.join();
    }

}