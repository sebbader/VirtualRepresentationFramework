/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package web.controller.communication.opcua.client;

import interfaces.OPCUAClient;
import com.github.andrewoma.dexx.collection.ArrayLists;
import core.controller.data.FileWrapperOPCUA;
import core.controller.utils.Utilities;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.milo.opcua.stack.core.StatusCodes;
import org.eclipse.milo.opcua.stack.core.types.OpcUaBinaryDataTypeDictionary;
import org.eclipse.milo.opcua.stack.core.types.OpcUaDataTypeManager;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;

/**
 * Adapted from:
 * https://www.tutorialspoint.com/servlets/servlets-first-example.htm
 * @author Jan-Peter.Schmidt
 */

@WebServlet(asyncSupported = true, name ="opcuaclient",  displayName = "OPCUAClient", urlPatterns = {"/opcuaclient"})
@MultipartConfig
public class OPCUAClientServlet extends HttpServlet {
    
    @Override
    public void init() {
        
        /**
         * Register file decoder/encoder on server.
         */
        OpcUaBinaryDataTypeDictionary dictionary = new OpcUaBinaryDataTypeDictionary(
            "urn:virtrepframework:net:file-wrapper"
        );
        
        NodeId binaryEncodingId = new NodeId(2, "DataType.FileWrapperOPCUA.BinaryEncoding");
        
        dictionary.registerStructCodec(
            new FileWrapperOPCUA.Codec().asBinaryCodec(),
            "FileWrapperOPCUA",
            binaryEncodingId
        );

        OpcUaDataTypeManager.getInstance().registerTypeDictionary(dictionary);        
        
    }
    
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        
        try {
            
        System.out.println("Multipart-->xx:\n"+request.getPathInfo() + "\n" + request.getPart("name"));            
            System.out.println(request.getParts().size() + "<- Part size");
            request.getParts().forEach((part)-> {
                System.out.println("PART: " + part.getName());
            });            
            
            System.out.println("newS1-" + request.getParameterMap().size());
            request.getParameterMap().forEach((key, valueArray) -> {
            
                System.out.println("Key: " + key + " -> { ");
                
                ArrayLists.of(valueArray).forEach((value) -> {
                    System.out.println("  " + value);
                });
                
                System.out.println("}");
            
            });
            
            String name = StringUtils.substringAfter(request.getParameter("name"), "/representations/");
            String method = request.getParameter("method");
            File fileIn = null;

            if(request.getPart("file")!=null && request.getPart("file").getInputStream()!=null) {
                
                fileIn = File.createTempFile("upload", ".n3");
                InputStream inStream = request.getPart("file").getInputStream();
                FileUtils.copyInputStreamToFile(inStream, fileIn);   
                System.out.println("Created File with: \n\n" + Utilities.readFile(fileIn.getAbsolutePath(), Charset.defaultCharset()));
                
            }
            
            response.setHeader("Content-Location", "http://" + request.getHeader("host") + "/representations/");
            
            OPCUAClient client = null;
            StatusCode statusCode = new StatusCode(StatusCodes.Bad_UnexpectedError);
            File fileOut = null;
            
            System.out.println("doPost(" + method + ") for name=" + name);
            
            switch(method) {
                case "GET":
                    System.out.println("GET");
                    client = new ReadClient(name);                 
                    break;
                    
                case "POST":
                    client = new CreateClient(name, fileIn);
                    break;
                    
                case "PUT":
                    client = new UpdateClient(name, fileIn);
                    break;
                    
                case "DELETE":
                    client = new DeleteClient(name);
                    break;
                    
            }
            
            if(client!=null) {
                
                new ClientRunner(client).run();
                statusCode = client.getStatusCode()!=null ? client.getStatusCode() : statusCode;
                System.out.println(statusCode);
                fileOut = client.getFile();        
                
            }
            
            if(fileOut!=null && 
                    Utilities.readFile(fileOut.getAbsolutePath(), Charset.defaultCharset()).length() > 0 && 
                    statusCode.isGood()) {
                System.out.println("File !=null");
                //Taken from: https://stackoverflow.com/a/29308866
                response.setContentType("application/octet-stream");
                response.setContentLength((int) fileOut.length());
                response.setHeader( "Content-Disposition",
                         String.format("attachment; filename=\"%s\"", fileOut.getName()));

                OutputStream out = response.getOutputStream();
                try (FileInputStream in = new FileInputStream(fileOut)) {
                    byte[] buffer = new byte[4096];
                    int length;
                    while ((length = in.read(buffer)) > 0) {
                        out.write(buffer, 0, length);
                    }
                }
                out.flush();                
                
            } else if(statusCode.isGood()) {
            
                String success = "OPC UA StatusCode is good. OPC UA StatusCode is: <br><br>" + statusCode.toString();
                PrintWriter out = response.getWriter();
                out.println(success);
                response.setStatus(200);
            
            } else {
                
                String error = "OPC UA StatusCode is bad. OPC UA StatusCode is: <br><br>" + statusCode.toString();
                PrintWriter out = response.getWriter();
                out.println(error);
                response.setStatus(466);
                
            }
            
            System.out.println("Operation done." + method);
            
        } catch (Exception ex) {
            Logger.getLogger(OPCUAClientServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
        
}
