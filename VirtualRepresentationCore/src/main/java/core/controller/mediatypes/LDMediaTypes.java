package core.controller.mediatypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.MediaType;

/**
 * Extends the {@link javax.ws.rs.core.MediaType MediaType} with prefdefined media types
 * that are neccessary for LD-Communication. See w3.org for further information.
 * https://www.w3.org/2008/01/rdf-media-types
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
    
    /**
     * Converts Jena media type string to official media type string.
     * @param mediaType common mediaType to convert
     * @return String of MediaType in Jena media type format
     */
    
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
    
    /**
     * Returns the suffix for a file of a certain media type
     * @param mediaType MediaType for that suffix is needed.
     * @return Suffix for mediatype
     */
    
    public static final String getEndungen(String mediaType) {
        
        return typesEndungen.get(mediaType);
        
    }
    
    /**
     * Returns MediaType by given suffix.
     * @param endung Suffix for that MediaType is needed.
     * @return MediaType for suffix
     */
    public static final String getMediaTypeByEndung(String endung) {
        
        for(Map.Entry<String,String> entry : typesEndungen.entrySet()) {
            
            if(entry.getValue().equals(endung)) {
                return entry.getKey();
            }
            
        }
        
        return null;
        
    }    
    
    /**
     * checks if given string is valid media type
     * @param contentType MediaTypeString to check
     * @return true if given String is registered in {@link LDMediaTypes LDMediaTypes} or {@link MediaType MediaType}
     */
    
    public static boolean isRDFMediaType(String contentType) {
        
        ArrayList<String> rdfMediaTypes = new ArrayList<>();
        
        rdfMediaTypes.add(LDMediaTypes.APPLICATION_RDF_XML);
        rdfMediaTypes.add(LDMediaTypes.APPLICATION_TURTLE);
        rdfMediaTypes.add(LDMediaTypes.N3);
        rdfMediaTypes.add(LDMediaTypes.NTRIPLES);
        
        return rdfMediaTypes.contains(contentType);
        
    }    
    
}
