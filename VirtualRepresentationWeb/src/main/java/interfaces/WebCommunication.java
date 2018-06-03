package interfaces;

import core.controller.communication.ReadResponse;
import java.io.File;

/**
 * This interface is used to standardize different communication protocols
 * to crud operations that are supported by the representation manager.
 * @author Jan-Peter.Schmidt
 */
public interface WebCommunication {
    
    /**
     * Performs a create operation and returns internal status code
     * @param name Name of representation that should be created
     * @param file File that contains RDF data or SPARQL Query for representation<br>
     * @return Internal Statuscode <br>0: unknown error,<br> 
     *                  1: created, no file, <br>
     *                  2: created, with file<br>
     */
    int executeCreate(String name, File file);
    
    /**
     * Performs a read operation and returns ReadResponse containing
     * the status and a file with rdf oder html data.
     * @param name Name of virtual representation 
     * @param accept MediaType of accepdtedRepresentation
     * @return An instance of ReadResponse containing an Integer 
     *         statusCode and a file with rdf or html data<br>
     *         -1 object not found;<br>
     *         0 unknown error;<br>
     *         1 found, representation;<br>
     *         2 found, variable<br>
     */
    ReadResponse executeRead(String name, String accept);
    
    /**
     * Performs an update operation and returns internal status code.
     * @param name Name of virtual representation
     * @param file File to store in virtual representation
     * @return Internal statusCode.
     *      -1 object not found;<br>
     *       0 unknown error;<br>
     *       1 found, representation;<br>
     *       2 found, variable<br>
     */
    int executeUpdate(String name, File file);
    
    /**
     * Performs delete operation and returns internal status code.
     * @param name Name of representation
     * @return Internal statucode.<br>
     *        -1 object not found;<br>
     *         0 unknown error;<br>
     *         1 found, representation;<br>
     *         2 found, variable<br>
     */
    int executeDelete(String name);
    
}