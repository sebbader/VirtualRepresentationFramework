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
    
    public static final Property javaType = ResourceFactory.createProperty(NAMESPACE, "javaType");
    
    public static final Property isRoot = ResourceFactory.createProperty(NAMESPACE, "isRoot");
    
    public static final Property hasProperty = ResourceFactory.createProperty(NAMESPACE, "hasProperty");
    
    public static final Property hasDBConnection = ResourceFactory.createProperty(NAMESPACE, "hasDBConnection");
    
    public static final Property connection = ResourceFactory.createProperty(NAMESPACE, "connection");
    
    public static final Property schema = ResourceFactory.createProperty(NAMESPACE, "schema");
    
    public static final Property user = ResourceFactory.createProperty(NAMESPACE, "user");
    
    public static final Property password = ResourceFactory.createProperty(NAMESPACE, "password");
    
    public static final Property hasSQLQuery = ResourceFactory.createProperty(NAMESPACE, "hasSQLQuery");
    
    public static final Property hasValue = ResourceFactory.createProperty(NAMESPACE, "hasValue");
    
    public static final Property hasOpcUaServer = ResourceFactory.createProperty(NAMESPACE, "hasOpcUaServer");        
    
}