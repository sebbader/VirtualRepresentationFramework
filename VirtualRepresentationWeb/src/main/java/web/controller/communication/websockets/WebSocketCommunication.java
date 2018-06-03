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
 * This class defines the WebSocket Endpoint. This implementation
 * recognizes WS-Calls on /representation path and is defined for
 * four subprotocols namely CREATE, UPDATE, READ and DELETE.
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
    
    /**
     * After a session is opened the uri is saved for later 
     * communications.
     * @param session
     * @throws IOException 
     */
    @OnOpen
    public void onOpen(Session session) throws IOException {
        
        if(session.getRequestParameterMap().get("uri")!=null && 
                session.getRequestParameterMap().get("uri").get(0)!=null) {
            
            uri = session.getRequestParameterMap().get("uri").get(0);
                        
        }
        
        System.out.println("Session opened for " + uri + " with subprotocol " + session.getNegotiatedSubprotocol());
                       
    }
    
    /**
     * This method is used for clients that want to upload files to
     * the framework while using a operation. Files are converted to a @link{java.io.File, File} and
     * afterwards sent to the framework by calling the selected operation.
     * 
     * @param msg Current part of upload
     * @param last Flag, if end of datastream
     * @param session Current session.
     */
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
    
    /**
     * This method is called when a client sents a message. 
     * By checking the agreeed subprotocol the method knows which
     * operation the user has excecuted and calls the corresponding method.
     * @param session Current session
     * @param message Message that the user has sent.
     * @throws IOException 
     */
    
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
    
    /**
     * This method is called on session close.
     * @param session Current Session
     * @throws IOException 
     */

    @OnClose
    public void onClose(Session session) {
        
        System.out.println("Session closed.");
        
    }
    
    /**
     * This method is called on error.
     * @param session Current session.
     * @param throwable Thrown error.
     */

    @OnError
    public void onError(Session session, Throwable throwable) {
        
        throwable.printStackTrace();
        
        System.out.println("Error occured. -> " + throwable.getMessage());
        
    }
    
    /**
     * Executes and returns value of {@link core.controller.virtualrepresentations.VirtualRepresentationManager#create(String name, File file)}
     * @param name Name of representation that should be created
     * @param file File that should be uploaded to representation
     * @return 
     */

    @Override
    public int executeCreate(String name, File file) {
        return VirtualRepresentationManager.create(name, file);
    }

    /**
     * Executes and returns value of {@link core.controller.virtualrepresentations.VirtualRepresentationManager#read(String name, String accept)}
     * @param name Name of representation that should be created
     * @param accept Accepted Mediy Type
     * @return 
     */    
    @Override
    public ReadResponse executeRead(String name, String accept) {
        return VirtualRepresentationManager.read(name, accept);
    }

    /**
     * Executes and returns value of {@link core.controller.virtualrepresentations.VirtualRepresentationManager#update(String name, File file)}
     * @param name Name of representation that should be created
     * @param file File that should be uploaded to representation
     * @return 
     */    
    
    @Override
    public int executeUpdate(String name, File file) {
        return VirtualRepresentationManager.update(name, file);
    }

    /**
     * Executes and returns value of {@link core.controller.virtualrepresentations.VirtualRepresentationManager#delete(String name)}
     * @param name Name of representation that should be created
     * @return 
     */    
    @Override
    public int executeDelete(String name) {
        return VirtualRepresentationManager.delete(name);
    }
    
    /**
     * Converts the internal statuscodes of create operation to http statuscodes.
     * @param file File to upload for creation
     * @return HTTP statuscode
     */

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
    
    /**
     * Converts the internal statuscodes of read operation to http statuscodes.
     * @param msg Mesasage that the user has sent
     * @return HTTP statuscode
     */
    
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
    
    /**
     * Converts the internal statuscode of update operation to http statuscodes.
     * @param file File that is uploaded to the virtual representation
     * @return HTTP statuscode
     */

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
    
    /**
     * Converts the internal statuscode of delete operation to http statuscode.
     * @return HTTP statuscode
     */
    
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
    
    /**
     * This method can be used to send a java file to the client.
     * @param file
     * @param session 
     */

    private void sendFileToUser(File file, Session session) {
        
        String fileString = Utilities.readFile(file.getPath(), Charset.defaultCharset());
                    
        session.getAsyncRemote().sendBinary(ByteBuffer.wrap(fileString.getBytes()));

    }
    
}