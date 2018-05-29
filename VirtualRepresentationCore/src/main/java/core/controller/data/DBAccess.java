/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core.controller.data;

import core.controller.virtualrepresentations.VirtualRepresentationManager;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 * This class stores credentials for a database.
 * @author Jan-Peter.Schmidt
 */
public class DBAccess {
    
    private String connection;
    private String schema;
    private String user;
    private String password;
    
    /**
     * Default contstructor
     */
    public DBAccess() {
        
        
    }
    
    /**
     * This function takes needed credentials for database connection
     * from a supplied {@link org.apache.jena.rdf.model.Model Model}.
     * Therefore it uses predefined rdf predicates from {@link core.controller.utils.VRProp VRProp}.
     * After derivation credentials are stored.
     * @param model Model that contains information for a connection.
     * @return Connection from derived credentials.
     */
    public Connection deriveFromModel(Model model) {
        
        System.out.println("Derive form model " + model.getGraph().size());
        
        Connection conn = null;
        
        try {
            
            String ns = VirtualRepresentationManager.NS_AVA;
            
            Property propConnection = ResourceFactory.createProperty(ns, "connection");
            Property propSchema = ResourceFactory.createProperty(ns, "schema");
            Property propUser = ResourceFactory.createProperty(ns, "user");
            Property propPassword = ResourceFactory.createProperty(ns, "password");
            
            System.out.println(propConnection);

            connection = model.listObjectsOfProperty(propConnection).next().toString();
            schema = model.listObjectsOfProperty(propSchema).next().toString();
            user = model.listObjectsOfProperty(propUser).next().toString();
            password = model.listObjectsOfProperty(propPassword).next().toString();            
            
            System.out.println(connection + ", " + schema +", " + user + ", " + password);
            
            Properties props = new Properties();
            props.put("user", user);
            props.put("password", password);
            
            conn = DriverManager.getConnection(connection + "/" + schema, props);
            
            System.out.println("Connection to " + connection + "/" + schema);
            
        } catch(Exception e) {
           //Logger.getLogger(VirtualRepresentationManager.class.getName()).log(Level.INFO, null, e);
        }
        
        return conn;
    }
    
}
