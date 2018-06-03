/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package web.controller.context;

import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.eclipse.milo.opcua.stack.core.Stack;
import web.controller.communication.opcua.VirtualRepresenationOpcUaServer;

/**
 * This class is called on server startup and starts the OPC UA Server.
 * @author Jan-Peter.Schmidt
 */
@WebListener("Starts OPC UA Server for receiving OPC UA calls")
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
                server.shutdown().get();                                      
                Stack.releaseSharedResources();
            } catch (InterruptedException ex) {
                Logger.getLogger(ServerRunner.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ExecutionException ex) {
                Logger.getLogger(ServerRunner.class.getName()).log(Level.SEVERE, null, ex);
            }
        }        
    }
    
    private void startOPCUAServer() {
        
        System.out.println("Works fine..");
        
        Thread thread = new Thread(() -> {
            try {
                
                System.out.println("Test");
                
                server = new VirtualRepresenationOpcUaServer();
                
                server.startup().get();
                
                System.out.println("Server is running");
                System.out.println("Server url is: " + server.getServer().getConfig().getBindPort());
                
                /*final CompletableFuture<Void> future = new CompletableFuture<>();
                
                Runtime.getRuntime().addShutdownHook(new Thread(() -> future.complete(null)));*/
                
                //future.get();
                
            } catch (Exception ex) {
                Logger.getLogger(ServerRunner.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        
        thread.setName("OPC UA Server");
        thread.start();
        
        
    }
    
}
