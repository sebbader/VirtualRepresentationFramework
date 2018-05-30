package core.controller.virtualrepresentations;

import core.controller.communication.ReadResponse;
import core.controller.mediatypes.LDMediaTypes;
import core.controller.model.writer.HTMLRegistry;
import core.controller.utils.Utilities;
import core.controller.utils.VRProp;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Iterator;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.ext.com.google.common.io.Files;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.SimpleSelector;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.impl.ResourceImpl;

/**
 * This class is responsible for managing interactions with all 
 * virtual representations. Incoming CRUD-Operations on uris 
 * are sent to this manager and exceuted by him
 * 
 * @author Jan-Peter.Schmidt
 */
public class VirtualRepresentationManager {
    
    /**
     * HashMap that contains all registered virtual representations
     */
    private static HashMap<String, VirtualRepresentation> registeredRepresentations = new HashMap<>();
    
    /**
     * RDF model that is shown in manager representation - also called "bookmark"
     */
    private static Model registry;
    
    /**
     * Path where temp file can be stored
     */
    public static final String TEMP_PATH = "virtrepframework\\temp\\";
    
    /**
     * Domain of the framework, used for defining own predicates for rdf graphs.
     */
    public static final String NS_VR = "http://www.virtualrepresentation-framework.edu/";
    
    /**
     * Individual domain of user
     */
    public static String domain = "";
    
    /**
     * Prefix of NS_VR namespace
     */
    public static String nsPrefixCustom = "virtrep";
    
    /**
     * Stuff that need to be done before Framework is used.
     * Manager representation is created.
     */
    static {
        
        try {
            String host = InetAddress.getLocalHost().getCanonicalHostName();
            String port = "9999";
            
            domain = "http://" + host + ":" + port + "/representations/";
            
            System.out.println("Domain is: " + domain);
            
            VirtualRepresentationManager.create("manager");
            Thread t = new Thread(() -> {
                
                VirtualRepresentation manager = null;
                                    
                while(true) {
                    
                    if(manager==null) {
                        manager = VirtualRepresentationManager.getRepresentation("manager");
                    }
                    
                    if(manager!=null) {
                        
                        final String MAN_NAME = manager.getName();
                        
                        registry = ModelFactory.createDefaultModel();
                        registeredRepresentations.forEach((name, representation) -> {
                            
                            Resource subject = new ResourceImpl(domain, MAN_NAME);
                            Property predicate = VRProp.REGISTERED;
                            Resource object = new ResourceImpl(domain, name);
                            
                            registry.add(subject, predicate, object);
                            
                        });
                        
                        manager.setDataAcquisition(registry);
                        
                    }
                    
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(VirtualRepresentationManager.class.getName()).log(Level.SEVERE, null, ex);
                        break;
                    }
                    
                }
                
            });
            
            t.setName("ManagerUpdater");
            t.setDaemon(true);
            t.start();
        } catch (UnknownHostException ex) {
            Logger.getLogger(VirtualRepresentationManager.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    /**
     * Function that returns a representation by its name. The name
     * is everything that is in uri after /representations
     * @param name name of representation. Name is part of the uri afte /representations
     * @return Instance of virtual representation with supplied name
     */
    
    public static VirtualRepresentation getRepresentation(String name) {
        
        System.out.println("Looking for name: " + name);               
        name = checkName(name);
        
        if(name!=null) {
            System.out.println(name);
            return registeredRepresentations.get(name);
        }
        
        return null;
        
    }
    
    /**
     * Function to register virtual representation. Registration is mandatory for newly created 
     * virtual representations.
     * @param representation Instance of VirtualRepresentaion that should be registered
     * @param name Name of representation under which it is stored.
     */
    
    public static void register(VirtualRepresentation representation, String name) {
        
        Logger.getAnonymousLogger().log(Level.INFO, "Registered Representation \"" + name + "\"");
        registeredRepresentations.put(name, representation);
        
    }
    
    /**
     * Function to deregister virtual representation. This is automatically done after deletion
     * 
     * @param name Name of representation that should be deregistered
     */
    public static void deregister(String name) {
        
        System.out.println("Deregister " + name + " - " + registeredRepresentations.size());
        registeredRepresentations.remove(name);
        
    }
    
    /**
     * Function to check if a representation exists
     * @param name Name of representation to check
     * @return true if registry of manager contains representation with supplied name, else false
     */
    public static boolean exists(String name) {
        
        name = checkName(name);
        return registeredRepresentations.containsKey(name);
        
    }
    
    /**
     * Function to check if a representation exists
     * @param representation instance of representation to check
     * @return true if registry of manager contains supplied instance, else false
     */    
    public static boolean exists(VirtualRepresentation representation) {
        
        return registeredRepresentations.containsValue(representation);
        
    }
    
    /**
     * This functions derives a parent from an uri. This is done by path splitting.
     * @param name Name of representation for that the parent will be derivced
     * @return Instance of parent representation, if one is found. Else null.
     */

    public static VirtualRepresentation getParentFromURI(String name) {
        
        name = checkName(name);
        
        System.out.println("getParentFromURI(" + name + ")");
        
        String[] ancestors = name.split("/");
        int i = ancestors.length-2;
        
        //Go backward through ancestor list and look for closests ancestor.
        while(i >= 0) {
            
            String ancestorURI = ancestors[i];
            int j = i-1;
            while(j >=0) {
                ancestorURI = ancestors[j] + "/" + ancestorURI;
                j--;
            }
            
            System.out.println("Check if " + ancestorURI + " exists.");
            if(getRepresentation(ancestorURI)!=null) {
                System.out.println("Return " + ancestorURI + " as parent.");
                return getRepresentation(ancestorURI);
            }
                    
            i--;
            
        }

        
        return null;
        
    }

    public static HashMap<String, VirtualRepresentation> getRegisteredRepresentations() {
        return registeredRepresentations;
    }
    
    /**
     * Checks if the name of the representation is valid. If so it returns the name.
     * It also appends an / if missing at the end of the uri.
     * 
     * @param name name to be checked.
     * @return null if an error occured. Else the name.
     */
    
    public static String checkName(String name) {

        if(name==null) {
            return null;
        }
        
        //Append / on Representationname if missing
        if(!name.endsWith("/")) name = name + "/"; 
        
        return name;
        
    }
    
    /**
     * Checks if a supplied name is a variable or a representation
     * @param name Name of possible variable
     * @return Instance of virtualRepresentation if name is not a variable, else null
     */
    
    public static VirtualRepresentation isVariable(String name) {
        
        name = checkName(name);
        
        System.out.println("isVariable " + name);
        
        if(name.lastIndexOf("/")==name.length()-1) {
            name = name.substring(0, name.length()-2);
        }
        
        if(StringUtils.countMatches(name, "/") > 0) {
            
            String avaName = name.substring(0, name.lastIndexOf("/"));
            
            System.out.println("Try to find -> " + avaName);
            return VirtualRepresentationManager.getRepresentation(avaName);
            
        }
        
        return null;
        
    }
    
    /**
     * Creates a new representation by name.
     * @param name Name of new representation 
     * @return 0: if an error occured (500); 
          1: VirtualRepresentation created, no content added (204); 
          2: VirtualRepresentation created, content added (201)
     */
    
    public static int create(String name) {
        
        return create(name, null);
        
    }
    
    /**
     * Creates an representation Instance and calls VirtualRepresentation.update() with file.
     * @param avaName Name of the representation (part of URI)
     * @param file File that contains functions or model.
     * @return 0: if an error occured (500); 
          1: VirtualRepresentation created, no content added (204); 
          2: VirtualRepresentation created, content added (201)
     * 
     */
    
    public static int create(String avaName, File file) {
        
        System.out.println("CREATE");
        
        avaName = VirtualRepresentationManager.checkName(avaName);
        
        boolean modelAdded = false;
        
        if(avaName!=null) {
            
            Class<? extends VirtualRepresentation> clazz = getClassFromModel(file);
            VirtualRepresentation representation = null;
            
            if(clazz!=null) {
                
                System.out.println("Create ava of type " + clazz.getName());
                try {
                    System.out.println("try");
                    Constructor<? extends VirtualRepresentation> constr = clazz.getConstructor(String.class, VirtualRepresentation.class);
                    representation = constr.newInstance(avaName, null);
                    
                    System.out.println(representation.getClass().getName() + "<- ClassName of ava");
                    
                } catch(Exception e) {
                    Logger.getLogger(VirtualRepresentationManager.class.getName()).log(Level.SEVERE, null, e);
                }
                
            } else {
                System.out.println("Normal creation.");
                representation = new VirtualRepresentation(avaName, null);            
            }

            System.out.println("Endung: " + Utilities.getEndung(file));
            
            if(VirtualRepresentationManager.update(representation.getName(), file)==1) {
                return 2;
            }

            return 1;

        }        
        
        return 0;
        
    }
    
    /**
     * 
     * @param name Name of virtual representation that should be read.
     * @param accept accepted media type.
     * @return -1: VirtualRepresentation not found (404), 0 any other error (500), 
     *   1 on success (representation), 2 on success (variable).
     */
       
    public static ReadResponse read(String name, String accept) {

        if(accept.contains(MediaType.TEXT_HTML)) {
            System.out.println("Register HTML Writer");
            HTMLRegistry.registerHTMLWriter();
        } else {
            System.out.println(accept + "<- accepttype");
        }
        
        File file = null;
        int status = 0;
        
        try {
            
            System.out.println("Looking for Representation with name \"" + name + "\"");
            
            VirtualRepresentation representation = VirtualRepresentationManager.getRepresentation(name);
            
            Model model = null;

            if(representation!=null) {
                
                System.out.println("Found representation -> build model.");

                model = representation.collectData();
                
                System.out.println(model.getGraph().size());
                
                status = 1;

            } else if(VirtualRepresentationManager.isVariable(name)!=null) {
                
                String variableName = name.substring(name.lastIndexOf("/")+1, name.length());
                
                System.out.println("Retrieving data for variable " + variableName);
                
                representation = VirtualRepresentationManager.isVariable(name);
                
                Model modelData = representation.collectData();
                model = ModelFactory.createDefaultModel();
                
                Resource subject = ResourceFactory.createProperty(
                                    modelData.getNsPrefixURI(nsPrefixCustom)+variableName);
                
                System.out.println("Subject --> " +subject.getURI());
                
                Iterator<Statement> iterator = modelData.listStatements(
                                            new SimpleSelector(subject, null, (RDFNode) null)
                                            ).toList().iterator();
                
                while(iterator.hasNext()) {
                    
                    model.add(iterator.next());
                    
                }
                
                System.out.println("Variable creates model with size: " + model.size());
                
                if(model.size()!=0) {
                
                    status = 2;
                
                } else {
                    
                    status = -1; //Not found
                    
                }
            
            } else {
                
                status = -1;
                
            }
            
            //Uri is variable or representation -> return data
            if(status==1 || status==2) {
                
                if(accept.contains(LDMediaTypes.APPLICATION_RDF_XML)) {
                    System.out.println("Return RDF");
                    file = createFile(model, "RDF/XML");
                } else {
                    System.out.println("Return HTML");
                    file = createFile(model, "HTML");
                }
                
                
            }
            
        } catch(Exception e) {
            e.printStackTrace();
        }
        
        System.out.println("Returned status=" + status);
        
        return new ReadResponse(status, file);
        
    }   
    
    /**
     * Updates the representation instance for @see(avaName) with information in @see(file)  
     * @param name Name of representation
     * @param file File that contains model.
     * @return -2: if VirtualRepresentation was not found (404); 
         -1: if File is null (204);
          0: if any other error occured (500); 
          1: if update was successful (200).
     */
    
    public static int update(String name, File file) {
        
        System.out.println("UPDATE");
        
        VirtualRepresentation representation = VirtualRepresentationManager.getRepresentation(name);
        
        //Representation nicht gefunden
        if(representation==null) {
            System.out.println("Update: Representation not found " + name);
            return -2;
        }
        
        if(file!=null && file.exists()) {
            
            System.out.println("if !=null");
            
            //Check if update changes java type of representation
            //if so, delete old representation and create new one with
            //correct java type
            try {
                Class<? extends VirtualRepresentation> clazz = getClassFromModel(file);

                if(clazz!=null && !clazz.getName().equals(representation.getClass().getName())) {
                    
                    System.out.println("Changed Representation type");
                    VirtualRepresentationManager.delete(name);                    
                    VirtualRepresentationManager.create(name, file);
                    representation = VirtualRepresentationManager.getRepresentation(name);

                }

            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }

            boolean modelAdded = representation.setModelAsFile(file);
            
            if(modelAdded) {
                System.out.println("Update: Model added");
                return 1;
            } 
        } else if(file==null) {
            System.out.println("Update: Update file is null");
            return -1;
        }

        System.out.println("Update: any Error");
        return 0;
        
    }
    
    /**
     * 
     * @param name Name of representation to delete.
     * @return -1: VirtualRepresentation not found (404), 0 on any other error (500
     *          1 on success (200)
     */
    
    public static int delete(String name) {
        
        VirtualRepresentation representation = VirtualRepresentationManager.getRepresentation(name);
        
        if(representation==null) {
            return -1;
        }
        
        if(representation.delete(false)) {
            return 1;
        }
        
        return 0;
    }
    
    private static File createFile(Model model, String type) throws IOException {
        
        String suffix = type.equals(MediaType.TEXT_HTML) ? ".html" : ".rdf";
        
        File file = File.createTempFile("out", suffix, Files.createTempDir());
        
        if(model!=null && model.getGraph().size()>0) {
            FileWriter writer = new FileWriter(file);

            System.out.println("abs Path = " + file.getAbsolutePath());

            if(model!=null) {
                BufferedWriter out =  new BufferedWriter(writer);      
                model.write(out, type);
                out.close();
            }      
        } else {
            System.out.println("model is empty or file null");
        }
        
        return file;
        
    }
    
    /**
     * This functions grabs the model of a virtual representation and searches
     * for the defined java class (defined by predicate virtrep:javaType)
     * @param file
     * @return 
     */

    private static Class<? extends VirtualRepresentation> getClassFromModel(File file) {
        
        Class<? extends VirtualRepresentation>[] clazz = new Class[1];
        try {
            if(file!=null && file.exists()) {

                Model model = ModelFactory.createDefaultModel();
                model.read(file.getAbsolutePath());
                
                Property isRoot = ResourceFactory.createProperty(NS_VR, "isRoot");
                Property javaType = ResourceFactory.createProperty(NS_VR, "javaType");
                
                model.listSubjectsWithProperty(isRoot).toList().forEach((subject) -> {
                    
                   model.listObjectsOfProperty(subject, javaType).toList().forEach((type) -> {
                       
                       try {
                           String className = type.asLiteral().getString();
                           clazz[0] = (Class<? extends VirtualRepresentation>) Class.forName(className);
                       } catch (ClassNotFoundException ex) {
                           Logger.getLogger(VirtualRepresentationManager.class.getName()).log(Level.SEVERE, null, ex);
                       }                       
                   });
                    
                });

            }
        } catch(Exception e) {
            //e.printStackTrace();
        }
        
        return clazz[0];
    }

    public static String getDomain() {
        return domain;
    }

    public static void setDomain(String domain) {
        VirtualRepresentationManager.domain = domain;
    }
    
    
    
}