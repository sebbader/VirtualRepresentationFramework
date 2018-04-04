/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package web.controller.communication.opcua.client;

import core.controller.data.FileWrapperOPCUA;
import interfaces.OPCUAClient;
import java.io.File;
import java.util.concurrent.CompletableFuture;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.stack.core.types.OpcUaBinaryDataTypeDictionary;
import org.eclipse.milo.opcua.stack.core.types.OpcUaDataTypeManager;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.ExtensionObject;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;

/**
 * @author Jan-Peter.Schmidt
 */
public class UpdateClient implements OPCUAClient {

    private String name;
    private File file;
    private StatusCode statusCode;
    private NodeId binaryEncodingId;
    
    public UpdateClient(String name, File fileIn) {
        
        this.name = name;
        this.file = fileIn;
        
        // Create a dictionary, binaryEncodingId, and register the codec under that id
        OpcUaBinaryDataTypeDictionary dictionary = new OpcUaBinaryDataTypeDictionary(
            "urn:virtrepframework:net:file-wrapper"
        );

        binaryEncodingId = new NodeId(2, "DataType.FileWrapperOPCUA.BinaryEncoding");

        dictionary.registerStructCodec(
            new FileWrapperOPCUA.Codec().asBinaryCodec(),
            "FileWrapperOPCUA",
            binaryEncodingId
        );

        // Register dictionary with the shared DataTypeManager instance
        OpcUaDataTypeManager.getInstance().registerTypeDictionary(dictionary);            
        
    }

    @Override
    public void run(OpcUaClient client, CompletableFuture<OpcUaClient> future) throws Exception {
        
        try {
            client.connect().get();
            
            NodeId representation = new NodeId(2, name);

            System.out.println("Updateing for name=\"" + name + "\"..");

            DataValue value = null;
            
            if(file!=null) {               
                System.out.println("if");
                FileWrapperOPCUA wrapper = new FileWrapperOPCUA(file);
                ExtensionObject xo = ExtensionObject.encode(wrapper, binaryEncodingId);
                Variant variant = new Variant(xo);
                value = new DataValue(variant);
                System.out.println("Update write xo file..");              
            } else {
                System.out.println("File is null");
            }
            
            CompletableFuture<StatusCode> fr = client.writeValue(representation, value);
                       
            System.out.println("Statuscode Update: " + fr.get());
            statusCode = fr.get();

        } catch(Exception e) {
            
            e.printStackTrace();
            
        }
        
        System.out.println("Complete..");
        //Logger.getLogger(OPCUAClient.class.getName()).log(Level.SEVERE, "so far..");
        
        future.complete(client);
        System.out.println("Completed..");        
        
    }
    
    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public File getFile() {
        return null;
    }

    @Override
    public void setFile(File file) {
        return;
    }

    @Override
    public StatusCode getStatusCode() {
        return statusCode;
    }

    @Override
    public void setStatusCode(StatusCode statusCode) {
        this.statusCode = statusCode;
    }
    
}
