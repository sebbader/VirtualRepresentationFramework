/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package web.controller.context;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.eclipse.milo.opcua.sdk.server.OpcUaServer;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import web.controller.communication.opcua.VirtualRepresenationOpcUaServer;

/**
 *
 * @author Jan-Peter.Schmidt
 */
public class ServerRunner implements ServletContextListener {
    
    public static void main(String args[]) {
        
        ServerRunner sr = new ServerRunner();
        sr.startOPCUAServer();
        
    }

    private VirtualRepresenationOpcUaServer server;
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("Start server...");
        startOPCUAServer();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("Shutdown server...");
        if(server!=null) {
            try {
                final CompletableFuture<OpcUaServer> future = server.shutdown();
                
                System.out.println("Shutted down.");
                
                future.get();
                System.out.println("After future.");
                
            } catch (InterruptedException ex) {
                Logger.getLogger(ServerRunner.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ExecutionException ex) {
                Logger.getLogger(ServerRunner.class.getName()).log(Level.SEVERE, null, ex);
            }                        
        }
        
    }
    
    private void startOPCUAServer() {
        
        System.out.println("Works fine..");
        
        new Thread(() -> {
            try {
                
                System.out.println("Test");
                
                server = new VirtualRepresenationOpcUaServer();
                
                server.startup().get();
                
                System.out.println("Server is running");
                System.out.println("Server url is: " + server.getServer().getConfig().getBindPort());
                server.getServer().getConfig().getEndpointAddresses().forEach((address) -> {
                    System.out.println(address);
                });
                
                final CompletableFuture<Void> future = new CompletableFuture<>();
                
                Runtime.getRuntime().addShutdownHook(new Thread(() -> future.complete(null)));
                
                future.get();
                
            } catch (Exception ex) {
                Logger.getLogger(ServerRunner.class.getName()).log(Level.SEVERE, null, ex);
            }
        }).start();
        
        
    }
    
}
