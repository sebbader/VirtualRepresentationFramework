/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package web.controller.communication.http;

import core.controller.communication.ReadResponse;
import core.controller.mediatypes.LDMediaTypes;
import core.controller.utils.Utilities;
import core.controller.virtualrepresentations.VirtualRepresentationManager;
import interfaces.WebCommunication;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.POST;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;


/**
 * @author Jan-Peter.Schmidt
 */
//@Consumes({LDMediaTypes.APPLICATION_RDF_XML, LDMediaTypes.APPLICATION_TURTLE, LDMediaTypes.N3, LDMediaTypes.NTRIPLES, MediaType.MULTIPART_FORM_DATA})
@Path("{uri:.+}")
public class HTTPCommunication implements WebCommunication {
    
    @POST
    @Produces(MediaType.TEXT_HTML)
    public Response recognizeCreate(@PathParam("uri") String name,
                                @FormDataParam("file") InputStream uploadedInputStream,
                                @FormDataParam("file") FormDataContentDisposition fileDetail) {
        
        try {        
            System.out.println("convertCreate");
            
            File file = null;
            Boolean modelAdded = false;

            //Check if information about file are exisiting and store as File.
            if(uploadedInputStream!=null && fileDetail!=null) {
                    file = Utilities.saveToFile(uploadedInputStream, fileDetail);
            }
            
            URI uri = null;
            
            switch(executeCreate(name, file)) {
                
                case 0:
                    return Response.serverError().build();
                    
                case 1:
                    uri = VirtualRepresentationManager.getURIFromName(name);
                    return Response.noContent().location(uri).build();
                    
                case 2:
                    uri = VirtualRepresentationManager.getURIFromName(name);
                    return Response.created(uri).build();
                
            }
            
        } catch (Exception ex) {
            Logger.getLogger(HTTPCommunication.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return Response.serverError().build();
    }

    @GET
    @Produces({MediaType.TEXT_HTML, LDMediaTypes.APPLICATION_RDF_XML})
    public Response recognizeRead(@Context HttpHeaders headers, @PathParam("uri") String name) {
        
        System.out.println("GET");
        
        String accept = headers.getHeaderString("accept")==null ? 
                    (MediaType.TEXT_HTML) : (headers.getHeaderString("accept"));
        
        ReadResponse readResponse = executeRead(name, accept);
        
        System.out.println(readResponse.getStatus() + "<- RR");
        
        switch(readResponse.getStatus()) {
            
            case -1:
                return Response.status(Response.Status.NOT_FOUND).build();
                
            case 0:
                return Response.serverError().build();            
                
            case 1: //do same as in two
            case 2:
                URI uri = VirtualRepresentationManager.getURIFromName(name);
                
                System.out.println("URI --> " + uri);
                
                if(readResponse.getFile()!=null && readResponse.getFile().exists()) {
                    if(accept.contains(MediaType.TEXT_HTML)) {
                        System.out.println("NO CONTENT DISPOSITION");
                        return Response.ok(readResponse.getFile(), MediaType.TEXT_HTML).contentLocation(uri).build();
                    } else {
                        System.out.println("CONTENT DISPOSITION");

                        String fileName = readResponse.getFile().getName();
                        return Response.ok(readResponse.getFile(), LDMediaTypes.APPLICATION_RDF_XML).header("Content-Disposition", "attachment; filename="+ fileName).contentLocation(uri).build();
                    }
                }
                
                return Response.noContent().build();
                        
        }
        
        return Response.serverError().build();
    }
    

    @PUT
    @Produces(MediaType.TEXT_HTML)
    public Response recognizeUpdate(@PathParam("uri") String name,
                @FormDataParam("file") InputStream uploadedInputStream,
                @FormDataParam("file") FormDataContentDisposition fileDetail) {

        try {        
            System.out.println("convertUpdate");
            
            File file = null;
            Boolean modelAdded = false;

            //Check if information about file are exisiting and store as File.
            if(uploadedInputStream!=null && fileDetail!=null) {
                    file = Utilities.saveToFile(uploadedInputStream, fileDetail);
            }
            
            URI uri = null;
            
            switch(executeUpdate(name, file)) {
                
                case -2:
                    return Response.status(404).build();
                    
                case -1:
                    return Response.status(204).build();
                    
                case 0:
                    return Response.serverError().build();
                    
                case 1:
                    uri = VirtualRepresentationManager.getURIFromName(name);
                    return Response.ok(uri.getPath()).build();
                
            }
            
        } catch (Exception ex) {
            Logger.getLogger(HTTPCommunication.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return Response.serverError().build();        

    }

    @DELETE
    public Response recognizeDelete(@PathParam("uri") String name) {
        System.out.println("convertDelete");
        
        switch(executeDelete(name)) {
            
            case -1:
                return Response.status(Response.Status.NOT_FOUND).build();
            
            case 0:
                return Response.serverError().build();
                
            case 1:
                return Response.ok().build();
        }
        
        return Response.serverError().build();
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

}
