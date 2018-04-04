/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package web.controller.communication.websockets;

import core.controller.communication.ReadResponse;
import core.controller.utils.Utilities;
import core.controller.virtualrepresentations.VirtualRepresentation;
import core.controller.virtualrepresentations.VirtualRepresentationManager;
import interfaces.WebCommunication;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import javax.ws.rs.core.Response;
/**
 *
 * @author Jan-Peter.Schmidt
 */
@ServerEndpoint(value="/representations", subprotocols = {"CREATE", "UPDATE", "READ", "DELETE"})
public class WebSocketCommunication implements WebCommunication {
    
    private String uri = null;
    private VirtualRepresentation representation = null;
    
    private static final String CREATE = "CREATE";
    private static final String UPDATE = "UPDATE";
    private static final String READ = "READ";
    private static final String DELETE = "DELETE";
    
    final static String filePath="c:/virtrepframework/";   
    
    private File file;
    private boolean append = false;
    
    public Response redirect() {
         
        return Response.status(101).build();
        
    }
    
    @OnOpen
    public void onOpen(Session session) throws IOException {
        
        if(session.getRequestParameterMap().get("uri")!=null && 
                session.getRequestParameterMap().get("uri").get(0)!=null) {
            
            uri = session.getRequestParameterMap().get("uri").get(0);
                        
        }
        
        System.out.println("Session opened for " + uri + " with subprotocol " + session.getNegotiatedSubprotocol());
                       
    }
    
    @OnMessage
    public void processUpload(ByteBuffer msg, boolean last, Session session) {

        System.out.println("Binary Data - is last? " + last);     
        
        int statusCode = 500;
        String suffix = ".rq";
        
        
        if(session.getNegotiatedSubprotocol().equals("CREATE")) {
            suffix = ".n3";
        }
        
        //Adapted from: http://www.java2s.com/Tutorial/Java/0180__File/WritingandAppendingaByteBuffertoaFile.htm
        //Create file from file parts
        if(!append) {
            
            try {
                file = File.createTempFile("ava", ".n3");
                System.out.println("BBUF-File: " + file.getAbsolutePath());
                FileChannel wChannel = new FileOutputStream(file, append).getChannel();
                wChannel.write(msg);
                wChannel.close();                  
            } catch (IOException ex) {
                Logger.getLogger(WebSocketCommunication.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
        
        //if it end of file work on with file
        if(last) {
            
            append=false; //Create new file next time method is called
            
            if(session.getNegotiatedSubprotocol().equals("CREATE")) {
                statusCode = convertCreate(file);
            } else if(session.getNegotiatedSubprotocol().equals("UPDATE")) {
                statusCode = convertUpdate(file);
            }

        }
        
        session.getAsyncRemote().sendText(String.valueOf(statusCode));
        

    }    
    
    @OnMessage
    public void onMessage(Session session, String message) throws IOException {
        
        System.out.println("Channel " + session.getNegotiatedSubprotocol() + ": Message received. -> " +message);  
        
        String subprotocol = session.getNegotiatedSubprotocol();
        
        int statusCode = 500;
        
       switch(subprotocol) {
        
            case CREATE:
               statusCode = convertCreate(null);
               break;
               
            case READ:
                ReadResponse response = convertRead(message);
                statusCode = response.getStatus();
                //Send file to user
                if(statusCode==200 && response.getFile()!=null) {  
                    sendFileToUser(response.getFile(), session);
                    return;
                }
                break;
                       
            case UPDATE:
                statusCode = convertUpdate(null);
                break;
                
            case DELETE:
                statusCode = convertDelete();
                break;
        
       }
       
       System.out.println("StatusCode=" + statusCode);
       
       session.getAsyncRemote().sendText(String.valueOf(statusCode));
        
    }

    @OnClose
    public void onClose(Session session) throws IOException {
        
        System.out.println("Session closed.");
        
    }

    @OnError
    public void onError(Session session, Throwable throwable) throws IOException {
        
        throwable.printStackTrace();
        
        System.out.println("Error occured. -> " + throwable.getMessage());
        
    }

    @Override
    public int executeCreate(String name, File file) {
        return VirtualRepresentationManager.create(name, file);
    }

    @Override
    public ReadResponse executeRead(String name, String accept) {
        return VirtualRepresentationManager.read(name, accept);
    }

    @Override
    public int executeUpdate(String name, File file) {
        return VirtualRepresentationManager.update(name, file);
    }

    @Override
    public int executeDelete(String name) {
        return VirtualRepresentationManager.delete(name);
    }    

    private int convertCreate(File file) {
        
        int statusCode = 500;
               
        switch(executeCreate(uri, file)) {
            
            case 0:
                return 500;
                
            case 1:
                return 204;
                
            case 2:
                return 201;
            
        }
        
        return statusCode;
        
    } 
    
    private ReadResponse convertRead(String msg) {
        
        int statusCode = 500;
        
        ReadResponse readResponse = executeRead(uri, msg);
        
        switch(readResponse.getStatus()) {
            
            case -1:
                statusCode = 404;
                break;
                
            case 0:
                statusCode = 500;
                break;
                
            case 1: //do same as in case 2
            case 2:
                statusCode = 200;
                break;
            
        }
        
        System.out.println("StatusCode " + statusCode);
        
        readResponse.setStatus(statusCode);
        
        return readResponse;
        
    }    

    private int convertUpdate(File file) {
        
        int statusCode = 500;
        
        switch(executeUpdate(uri, file)) {
            
            case -2:
                statusCode = 400;
                break;
                
            case -1:
                statusCode = 204;
                break;
                
            case 0: 
                statusCode = 500;
                break;
                
            case 1:
                statusCode = 200;
                break;
            
        }
        
        return statusCode;
    }
    
    private int convertDelete() {
       
        int statusCode = 500;
        
        switch(executeDelete(uri)) {
            
            case -1:
                statusCode = 404;
                break;
                
            case 0:
                statusCode = 500;
                break;
                
            case 1:
                statusCode = 200;
                break;                
            
        }
        
        return statusCode;        
        
    }

    private void sendFileToUser(File file, Session session) {
        
        String fileString = Utilities.readFile(file.getPath(), Charset.defaultCharset());
                    
        session.getAsyncRemote().sendBinary(ByteBuffer.wrap(fileString.getBytes()));

    }
    
}