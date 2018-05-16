/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core.controller.utils;

import core.controller.virtualrepresentations.VirtualRepresentationManager;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 * Defines various @(link org.apache.jena.rdf.model.Property) Properties
 * that are handy for using this framework.
 * @author Jan-Peter.Schmidt
 */
public class VRProp {
    
    public static final String NAMESPACE = VirtualRepresentationManager.NS_AVA;
    
    public static final Property JAVA_TYPE = ResourceFactory.createProperty(NAMESPACE, "javaType");
    
    public static final Property IS_ROOT = ResourceFactory.createProperty(NAMESPACE, "isRoot");
    
    public static final Property HAS_PROPERTY = ResourceFactory.createProperty(NAMESPACE, "hasProperty");
    
    public static final Property HAS_DB_CONNECTION = ResourceFactory.createProperty(NAMESPACE, "hasDBConnection");
    
    public static final Property CONNECTION = ResourceFactory.createProperty(NAMESPACE, "connection");
    
    public static final Property SCHEMA = ResourceFactory.createProperty(NAMESPACE, "schema");
    
    public static final Property USER = ResourceFactory.createProperty(NAMESPACE, "user");
    
    public static final Property PASSWORD = ResourceFactory.createProperty(NAMESPACE, "password");
    
    public static final Property HAS_SQL_QUERY = ResourceFactory.createProperty(NAMESPACE, "hasSQLQuery");
    
    public static final Property HAS_VALUE = ResourceFactory.createProperty(NAMESPACE, "hasValue");
    
    public static final Property HAS_OPCUA_SERVER = ResourceFactory.createProperty(NAMESPACE, "hasOpcUaServer"); 
    
    public static final Property HAS_ODATA_KEYWORD = ResourceFactory.createProperty(NAMESPACE, "hasODataKeyword");
    
    public static final Property HAS_ODATA_SOURCE = ResourceFactory.createProperty(NAMESPACE, "hasODataSource");
    
    public static final Property HAS_ODATA_2_RDF_CONVERTER = ResourceFactory.createProperty(NAMESPACE, "hasOData2RDFConverter");
    
    public static final Property HAS_ODATA_2_RDF_CONFIG = ResourceFactory.createProperty(NAMESPACE, "hasOData2RDFConfig");
    
    public static final Property HAS_CHILDREN = ResourceFactory.createProperty(NAMESPACE, "hasChild");
    
    public static final Property HAS_PARENT = ResourceFactory.createProperty(NAMESPACE, "hasParent");
    
}