package com.netflix.karyon.example.jetty;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.archaius.guice.ArchaiusModule;
import com.netflix.governator.DefaultLifecycleListener;
import com.netflix.governator.guice.jetty.JettyModule;
import com.netflix.karyon.Karyon;
import com.netflix.karyon.admin.rest.AdminServerModule;
import com.netflix.karyon.admin.ui.AdminUIServerModule;
import com.netflix.karyon.archaius.ArchaiusKaryonConfiguration;
import com.netflix.karyon.log4j.ArchaiusLog4J2ConfigurationModule;
import com.netflix.karyon.rxnetty.ShutdownServerModule;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

@Path("/hello")
public class HelloWorldApp extends DefaultLifecycleListener {
    private static final Logger LOG = LoggerFactory.getLogger(HelloWorldApp.class);
    
    public static void main(String[] args) throws InterruptedException {

        Karyon.createInjector(
            ArchaiusKaryonConfiguration.builder()
                .withConfigName("helloworld")
                .build(),
            new ArchaiusLog4J2ConfigurationModule(),
            new JettyModule(),
            new AdminServerModule(),
            new AdminUIServerModule(),
            new ArchaiusModule(),
            new ShutdownServerModule(),
            new JerseyServletModule() {
               @Override
               protected void configureServlets() {
                   serve("/*").with(GuiceContainer.class);
                   
                   bind(GuiceContainer.class).asEagerSingleton();
                   bind(ArchaiusEndpoint.class).asEagerSingleton();
                   
                   bind(HelloWorldApp.class).asEagerSingleton();
               }
               
               @Override
               public String toString() {
                   return "JerseyServletModule";
               }
           }
           )
           .awaitTermination();
    }
    
    @GET
    public String sayHello() {
        return "hello world";
    }

    @Override
    public void onStarted() {
        LOG.info("HelloWorldApp started");;
    }
}