package web.controller.communication.http;

import core.controller.communication.ReadResponse;
import core.controller.mediatypes.LDMediaTypes;
import core.controller.utils.Utilities;
import core.controller.virtualrepresentations.VirtualRepresentationManager;
import interfaces.WebCommunication;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
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
 * This class defines the HTTP Endpoint. As implementation of the websocket
 * interface it is responsible for converting CRUD request from HTTP specific
 * format to CRUD calls for representation manager. Also te reconversion of
 * internal statuscodes and answer file to HTTP specific Responses is done here.
 * 
 * @author Jan-Peter.Schmidt
 */
//@Consumes({LDMediaTypes.APPLICATION_RDF_XML, LDMediaTypes.APPLICATION_TURTLE, LDMediaTypes.N3, LDMediaTypes.NTRIPLES, MediaType.MULTIPART_FORM_DATA})
@Path("{uri:.+}")
public class HTTPCommunication implements WebCommunication {
    
    /**
     * Recognizes a create request, by listening to incoming POST-calls.
     * Incoming stuff is prepared and delegated to 
     * {@link web.controller.communication.http.HTTPCommunication#executeCreate(String name, File file)}
     * @param name Name of new representation
     * @param uploadedInputStream InputStream of uploaded file
     * @param fileDetail Details of uploaded file
     * @param httpHeaders HttpHeaders sent by this request
     * @return Response for user with statuscode and some other specific header information.
     */
    @POST
    @Produces(MediaType.TEXT_HTML)
    public Response recognizeCreate(@PathParam("uri") String name,
                                @FormDataParam("file") InputStream uploadedInputStream,
                                @FormDataParam("file") FormDataContentDisposition fileDetail,
                                @Context HttpHeaders httpHeaders) {
        
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
                    uri = getURIFromName(name, httpHeaders.getRequestHeader("Host"));;
                    return Response.noContent().location(uri).build();
                    
                case 2:
                    uri = getURIFromName(name, httpHeaders.getRequestHeader("Host"));
                    return Response.created(uri).build();
                
            }
            
        } catch (Exception ex) {
            Logger.getLogger(HTTPCommunication.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return Response.serverError().build();
    }
    
    /**
     * Recognizes a read request by listening to incoming GET-calls. Incoming stuff
     * is prepared and delegated to {@link web.controller.communication.http.HTTPCommunication#executeRead(String name, String accept)}.
     * @param headers Headers sent by request.
     * @param name Name of vritual representation that should be read.
     * @return Returns Response with Datafile (HTML or RDF) and specific header options.
     */

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
                URI uri = getURIFromName(name, headers.getRequestHeader("Host"));
                
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
    
    /**
     * Recognizes a update request, by listening to incoming PUT-calls. Incoming stuff
     * is prepared and delegated to {@link web.controller.communication.http.HTTPCommunication#executeUpdate(String name, File file)}.
     * @param name Name of new representation
     * @param uploadedInputStream InputStream of uploaded file
     * @param fileDetail Details of uploaded file
     * @param httpHeaders HttpHeaders sent by this request
     * @return Response for user with statuscode and some other specific header information.
     */    

    @PUT
    @Produces(MediaType.TEXT_HTML)
    public Response recognizeUpdate(@PathParam("uri") String name,
                @FormDataParam("file") InputStream uploadedInputStream,
                @FormDataParam("file") FormDataContentDisposition fileDetail, 
                @Context HttpHeaders httpHeaders) {

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
                    uri = getURIFromName(name, httpHeaders.getRequestHeader("Host"));
                    return Response.ok(uri.getPath()).build();
                
            }
            
        } catch (Exception ex) {
            Logger.getLogger(HTTPCommunication.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return Response.serverError().build();        

    }
    
    /**
     * Recognizes delete operation and delegates to 
     * {@link web.controller.communication.http.HTTPCommunication#executeDelete(String name)}
     * @param name Name of virtual representation.
     * @return HTTP-Response for user with corresponding statuscode.
     */

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
    
    /**
     * Executes and returns value of {@link core.controller.virtualrepresentations.VirtualRepresentationManager#create(String name, File file)}
     * @param name Name of representation that should be created
     * @param file File that should be uploaded to representation
     * @return Internal statuscode
     */

    @Override
    public int executeCreate(String name, File file) {
        return VirtualRepresentationManager.create(name, file);
    }

    /**
     * Executes and returns value of {@link core.controller.virtualrepresentations.VirtualRepresentationManager#read(String name, String accept)}
     * @param name Name of representation that should be created
     * @param accept Accepted Mediy Type
     * @return Returns a ReadResponse that contains statuscode and a file.
     */    
    @Override
    public ReadResponse executeRead(String name, String accept) {
        return VirtualRepresentationManager.read(name, accept);
    }

    /**
     * Executes and returns value of {@link core.controller.virtualrepresentations.VirtualRepresentationManager#update(String name, File file)}
     * @param name Name of representation that should be created
     * @param file File that should be uploaded to representation
     * @return Returns an integer which is an internal statuscode.
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
     * Returns URI from name and from header field
     * @param name Name of URI
     * @param hostList List of Strings contained in Host-Header
     * @return URI for representation with name.
     */
    
    public URI getURIFromName(String name, List<String> hostList) {
        
        URI uri=null;
        String host = "";
        if(hostList.size()>0) {
            host = hostList.get(0);
        }
        
        try {
            uri = new URI("http://www." + host + "/representations/"+name);
        } catch (URISyntaxException ex) {
            Logger.getLogger(HTTPCommunication.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return uri;
        
    }

}
