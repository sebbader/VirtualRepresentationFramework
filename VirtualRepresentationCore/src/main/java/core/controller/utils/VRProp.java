/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core.controller.utils;

import core.controller.virtualrepresentations.VirtualRepresentationManager;
import java.util.jar.Attributes;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 * Defines various {@link org.apache.jena.rdf.model.Property Properties}
 * that are handy for using this framework.
 * @author Jan-Peter.Schmidt
 */
public class VRProp {
    
    /**
     * Definition of various needed Namespaces.
     */
    public class Namespaces {
        
        public static final String OWL = "http://www.w3.org/2002/07/owl#";
        public static final String SSN = "http://www.w3.org/ns/ssn/";
        public static final String VRPROP = VirtualRepresentationManager.NS_AVA;    
        public static final String RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
        public static final String RDFS = "http://www.w3.org/2000/01/rdf-schema#";
        public static final String XSD = "http://www.w3.org/2001/XMLSchema#";
        public static final String SAREF = "https://w3id.org/saref/";
        public static final String SOSA = "http://www.w3.org/ns/sosa/";
        
    }
    
    public static final Property JAVA_TYPE = ResourceFactory.createProperty(Namespaces.VRPROP, "javaType");
    
    public static final Property IS_ROOT = ResourceFactory.createProperty(Namespaces.VRPROP, "isRoot");
    
    public static final Property HAS_PROPERTY = ResourceFactory.createProperty(Namespaces.SSN, "hasProperty");
    
    public static final Property HAS_DB_CONNECTION = ResourceFactory.createProperty(Namespaces.VRPROP, "hasDBConnection");
    
    public static final Property CONNECTION = ResourceFactory.createProperty(Namespaces.VRPROP, "connection");
    
    public static final Property SCHEMA = ResourceFactory.createProperty(Namespaces.VRPROP, "schema");
    
    public static final Property USER = ResourceFactory.createProperty(Namespaces.VRPROP, "user");
    
    public static final Property PASSWORD = ResourceFactory.createProperty(Namespaces.VRPROP, "password");
    
    public static final Property HAS_SQL_QUERY = ResourceFactory.createProperty(Namespaces.VRPROP, "hasSQLQuery");
    
    public static final Property HAS_VALUE = ResourceFactory.createProperty(Namespaces.OWL, "hasValue");
    
    public static final Property HAS_OPCUA_SERVER = ResourceFactory.createProperty(Namespaces.VRPROP, "hasOpcUaServer"); 
    
    public static final Property HAS_ODATA_KEYWORD = ResourceFactory.createProperty(Namespaces.VRPROP, "hasODataKeyword");
    
    public static final Property HAS_ODATA_SOURCE = ResourceFactory.createProperty(Namespaces.VRPROP, "hasODataSource");
    
    public static final Property HAS_ODATA_2_RDF_CONVERTER = ResourceFactory.createProperty(Namespaces.VRPROP, "hasOData2RDFConverter");
    
    public static final Property HAS_ODATA_2_RDF_CONFIG = ResourceFactory.createProperty(Namespaces.VRPROP, "hasOData2RDFConfig");
    
    public static final Property HOSTS = ResourceFactory.createProperty(Namespaces.SOSA, "hosts");
    
    public static final Property HOSTED_BY = ResourceFactory.createProperty(Namespaces.SOSA, "hostedBy");
    
    public static final Property REGISTERED = ResourceFactory.createProperty(Namespaces.VRPROP, "registered");
    
}