package core.controller.virtualrepresentations;

import core.controller.data.DBAccess;
import core.controller.utils.Utilities;
import java.io.File;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.SimpleSelector;

/**
 * This class provides all methods that are needed to manipulate virtual 
 * representations. It provides the usual getter and setter methods but 
 * also methods to aggregate and share data.
 * 
 * @author Jan-Peter.Schmidt
 */
public class VirtualRepresentation {

    /**
     * Definition of possible VirtualRepresentation types. It is neccessary 
     * to have representations for non-physical things, e.g. jobdefintions or orders. 
     * Therefore are representations of type META introduced. All representations that belong 
     * to physical things should use the REPRESENTATION type.
     */
    public enum TYPE {
        
        META, REPRESENTATION;
        
    }
    
    /**
     * Defines the name of the representation. It is reachable under his name, e.g. 
     * {@link http://localhost:8080/maschine123/saegeband} with name 
     * "maschine123/saegeband".
     */
    private String name;
    
    /**
     * Defines another representation that is parent of this rperesentation. Can be null.
     */
    private VirtualRepresentation parent;
    
    /**
     * Map that keeps all children of this representation instance.
     */
    private HashMap<String, VirtualRepresentation> children = new HashMap<>();
    
    /**
     * RDF triple store that contains all data of this representation
     */
    private Model dataAcquisition;
    
    /**
     * Defines the type of a representation. See enum defintion of type for further
     * information. {@link TYPE}
     */
    private TYPE type;    
    
    /**
     * A query that constructs the model that is returned by collectData.
     */
    private Query dataAggregation;
    
    /**
     * Flag whether VirtualRepresentation can use SQL Database to aggregate data.
     * Default value is true.
     */
    private boolean collectSQL = true;   
    
    
    /**
     * Constructor for representations.
     * @param name Name of the representation. Name should only contain path of URI, not
     * URI itself. E.g. /machine/sensor1 and not http://example.com/machine/sensor1
     * @param parent Representation that contains this representation. E.g. machines contains
     * sensor1. -> Machine is parent of sensor1. Can be null.
     */
    
    protected VirtualRepresentation(String name, VirtualRepresentation parent) {
        
        Logger.getAnonymousLogger().log(Level.INFO, "Erzeuge Representation \"" +name + "\""); 

        this.name = name;
        this.parent = parent;
        this.type = TYPE.REPRESENTATION;
        
        dataAcquisition = ModelFactory.createDefaultModel();
        
        VirtualRepresentationManager.register(this, name);        
        
        //Try to find parent via uri
        if(parent==null) {
            
            this.parent = VirtualRepresentationManager.getParentFromURI(name);
            System.out.println("Parent: " + parent);
                    
        }
        //If parent found, set this as child.
        if(this.parent!=null) {
            
            System.out.println(name + "<" + this.parent.getName());
            this.parent.addChild(this);
            
        }
        
    }
   
    /**
     * Deletes a representation.
     * @param isAutomaticChildDeletion
     * @return returns true if operation was done sucessfully.
     */
    protected boolean delete(boolean isAutomaticChildDeletion) {
        System.out.println("DELETE");
        
        //Force automatic child deltion if neccessary
        getChildren().forEach((name, representation) -> {
            System.out.println("DELETE " + name + "-> " + representation);
            representation.delete(true);
        });
        
        if(parent!=null && !isAutomaticChildDeletion) {
            parent.getChildren().remove(name);  
        }
        
        VirtualRepresentationManager.deregister(name);
        
        return !VirtualRepresentationManager.exists(name);
        
    }  
    
    /**
     * Method adds an representation to child list.
     * @param representation VirtualRepresentation that should be added.
     */
    
    protected void addChild(VirtualRepresentation representation) {
        
        getChildren().put(representation.getName(), representation);
        
    }
    
    
    /**
     * Method tries to create an model from delivered file and append it to this
     * representation. Both dataaggregation and dataacquisition files can be uploaded
     * that way. This method derives from filename suffix which one it is.
     * @param file File to be stored in this representation. .rq files are stored as
     * aggregation file anything else that can be parsed by Model.read()
     * is stored as data acquisition file. See also 
     * @see org.apache.jena.rdf.model.Model#read(java.lang.String) Model.read() 
     * 
     * @return true if file is valid and converted. False if file is corrupt.
     */
    protected boolean setModelAsFile(File file) {

        try {
            
            System.out.println(file.getAbsolutePath());

            Model model = ModelFactory.createDefaultModel();           
            
            //It's a function of the representation
            if(file.getAbsolutePath().endsWith(".rq")) {
                
                String query = Utilities.readFile(file.getPath(), Charset.defaultCharset());
                dataAggregation = QueryFactory.create(query);
                
                
            } else {
                
                dataAcquisition.add(model.read(file.getAbsolutePath()));            
                
            }

            System.out.println(model.getGraph().size());
            return true;
            
        } catch(Exception e) {
            e.printStackTrace();
        }
        
        return false;
        
    }
    
    /**
     * This method reads the file dataAcquisition and collects data that are
     * described in that file. These data are stored in a model and returned.
     * Datacollection starts with transferring static data from acuqisition file.
     * Afterwards SQL-Queries are executed and results converted to rdf and stored.
     * @return Model with all available data that are specified in data acquistion
     * file.
     */
    
    public Model collectData() {
        
        Model model = ModelFactory.createDefaultModel();
        
        //Check if dataAcquisition file exists.
        if(dataAcquisition!=null && dataAcquisition.getGraph().size() > 0) {
            
            /**
             * Copy static data from dataacquisition file to model.
             */
            
            //Copy Namespaces
            model.setNsPrefixes(dataAcquisition.getNsPrefixMap());            
            
            //Define properties and Namespaces
            String nsAF = VirtualRepresentationManager.NS_AVA;
            String nsCustom = model.getNsURIPrefix(VirtualRepresentationManager.nsPrefixCustom);

            //Define needed properties
            Property hasProperty = ResourceFactory.createProperty(nsAF, "hasProperty");
            Property hasSQLQuery = ResourceFactory.createProperty(nsAF, "hasSQLQuery");
            Property hasValue = ResourceFactory.createProperty(nsAF, "hasValue");            
            Property isRoot = ResourceFactory.createProperty(nsAF,"isRoot");
            
            dataAcquisition.listSubjectsWithProperty(isRoot).toList().forEach((subject) -> {
                
                //Import all information of the root element from dataacquisition to model
                dataAcquisition.listStatements(new SimpleSelector(subject, null, (RDFNode) null)).toList().forEach((stmt) -> {
                   
                    model.add(stmt);
                    
                });

                //Import all values of properties from dataacquisition to model
                dataAcquisition.listObjectsOfProperty(subject, hasProperty).toList().forEach((objects) -> {
                    dataAcquisition.listStatements(new SimpleSelector(objects.asResource(), null, (RDFNode) null)).toList().forEach((stmt) -> {
                        
                        model.add(stmt);                        
                        
                    });                    
                });            
            });
            
            //If data should be collected from SQL database
            if(collectSQL) {

                //Create access with defined credentials
                DBAccess dbAccess = new DBAccess();
                Connection conn = dbAccess.deriveFromModel(dataAcquisition);

                //If connection is established
                if(conn!=null) {

                    try {

                        dataAcquisition.listObjectsOfProperty(hasProperty).toList().forEach((node) -> {

                            //If any property has an SQLQuery
                            if(dataAcquisition.contains(node.asResource(), hasSQLQuery)) {
                                try {

                                    //Get first String (query) from rdf file.
                                    RDFNode query = dataAcquisition.listObjectsOfProperty(node.asResource(), hasSQLQuery).next();

                                    String value = "";

                                    ResultSet results = conn.createStatement().executeQuery(query.asLiteral().getString());
                                    //If result was found add value found to model
                                    while(results.next()) {

                                        value = results.getString(1);
                                        //model.add(node.asResource(), hasValue, value);

                                    }
                                    
                                    //Add found value to model
                                    Literal literalValue = ResourceFactory.createStringLiteral(value);
                                    model.add(node.asResource(), hasValue, literalValue);                                

                                } catch (Exception ex) {
                                    Logger.getLogger(VirtualRepresentation.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        });

                        conn.close();

                    } catch(SQLException sqle) {
                        sqle.printStackTrace();
                    } finally {
                        try {
                            if(conn!=null && !conn.isClosed()) {
                                conn.close();
                            }
                        } catch (SQLException ex) {
                            Logger.getLogger(VirtualRepresentation.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                        
                } else {
                    System.out.println("Con err");
                }
            }        
        }

        //System.out.println("CollcetedModelSize: " + model.size());
        
        if(dataAggregation != null) {
            
            try {
                
                return QueryExecutionFactory.create(dataAggregation, model).execConstruct();
                
                
            } catch(Exception e) {
                Logger.getLogger(VirtualRepresentation.class.getName()).log(Level.SEVERE, e.getMessage());
            }
            
            
            
        }

        return model;
        
    }
    
    /*
     * GETTER UND SETTER METHODEN
     */
    protected String getName() {
        return name;
    }

    protected void setName(String name) {
        this.name = name;
    }

    protected VirtualRepresentation getParent() {
        return parent;
    }

    protected void setParent(VirtualRepresentation parent) {
        this.parent = parent;
    }

    protected HashMap<String, VirtualRepresentation> getChildren() {
        return children;
    }

    protected void setChildren(HashMap<String, VirtualRepresentation> children) {
        this.children = children;
    }

    protected TYPE getType() {
        return type;
    }

    protected void setType(TYPE type) {
        this.type = type;
    }  

    public Model getDataAcquisition() {
        return dataAcquisition;
    }

    protected void setDataAcquisition(Model dataAcquisition) {
        this.dataAcquisition = dataAcquisition;
    }

    protected boolean isCollectSQL() {
        return collectSQL;
    }

    protected void setCollectSQL(boolean collectSQL) {
        this.collectSQL = collectSQL;
    } 

    public Query getDataAggregation() {
        return dataAggregation;
    }

    protected void setDataAggregation(Query dataAggregation) {
        this.dataAggregation = dataAggregation;
    }
    
    @Override
    public String toString() {
        return "Representation{" + "name=" + name + ", parent=" + ((parent!=null) ? (parent.getName()) : ("NULL")) + ", children=" + 
                children.size() + ", dataAcquisition=" + dataAcquisition.size() + ", type=" + 
                type + ", dataAggregationQuery=" + ((dataAggregation!=null) ? (dataAggregation.getHavingExprs().size()) : ("NULL")) + ", collectSQL=" + 
                collectSQL + '}';
    }
    
    
        
}
