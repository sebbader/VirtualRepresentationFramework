/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core.controller.mediatypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.MediaType;

/**
 * Extends the @see(javax.ws.rs.core.MediaType) with prefdefined media types
 * that are neccessary for LD-Communication. See w3.org for further information.
 * @link(https://www.w3.org/2008/01/rdf-media-types)
 * 
 * @author Jan-Peter.Schmidt
 */
public class LDMediaTypes extends MediaType {
    
    public static final String APPLICATION_RDF_XML = "application/rdf+xml";
    
    public static final MediaType APPLICATION_RDF_XML_TYPE = new MediaType("application", "rdf+xml");
    
    public static final String APPLICATION_TURTLE = "application/x-turtle";
    
    public static final MediaType APPLICATION_TURTLE_TYPE = new MediaType("application", "x-turtle");
    
    public static final String NTRIPLES = "text/plain";
    
    public static final MediaType APPLICATION_NTRIPLES_TYPE = new MediaType("text", "plain");
    
    /**
     * NOTE: Not registered as official media type.
     */
    public static final String N3 = "text/rdf+n3";
    
    public static final MediaType N3_TYPE = new MediaType("text","rdf+n3");
    
    private static final HashMap<String, String> typesEndungen = new HashMap<>();
    static {
        
        typesEndungen.put(APPLICATION_RDF_XML, "rdf");
        typesEndungen.put(APPLICATION_TURTLE, "ttl");
        typesEndungen.put(NTRIPLES, "nt");
        typesEndungen.put(N3, "n3");
        
    }    
    
    public LDMediaTypes() {
        
        super();
        
    }
    
    public static final String convertToJenaMediaType(String mediaType) {
        
        switch(mediaType) {
            
            case APPLICATION_RDF_XML:
                return "RDF/XML";
                
            case APPLICATION_TURTLE:
                return "TURTLE";
                
            case NTRIPLES:
                return "N-TRIPLES";
                
            case N3:
                return "N3";                    
                
            default:
                return null;
            
        }
        
    }
    
    public static final String getEndungen(String mediaType) {
        
        return typesEndungen.get(mediaType);
        
    }
    
    public static final String getMediaTypeByEndung(String endung) {
        
        for(Map.Entry<String,String> entry : typesEndungen.entrySet()) {
            
            if(entry.getValue().equals(endung)) {
                return entry.getKey();
            }
            
        }
        
        return null;
        
    }    
    
    public static boolean isRDFMediaType(String contentType) {
        
        ArrayList<String> rdfMediaTypes = new ArrayList<>();
        
        rdfMediaTypes.add(LDMediaTypes.APPLICATION_RDF_XML);
        rdfMediaTypes.add(LDMediaTypes.APPLICATION_TURTLE);
        rdfMediaTypes.add(LDMediaTypes.N3);
        rdfMediaTypes.add(LDMediaTypes.NTRIPLES);
        
        return rdfMediaTypes.contains(contentType);
        
    }    
    
}
