/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package web.controller.communication.opcua.client;

import interfaces.OPCUAClient;
import core.controller.data.FileWrapperOPCUA;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.UaClient;
import org.eclipse.milo.opcua.stack.core.StatusCodes;
import org.eclipse.milo.opcua.stack.core.types.OpcUaBinaryDataTypeDictionary;
import org.eclipse.milo.opcua.stack.core.types.OpcUaDataTypeManager;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.ExpandedNodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.ExtensionObject;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.NodeClass;
import org.eclipse.milo.opcua.stack.core.types.structured.AddNodesItem;
import org.eclipse.milo.opcua.stack.core.types.structured.AddNodesResponse;

/**
 * OPC UA Client that sends an addNodeRequest to the server.
 * @author Jan-Peter.Schmidt
 */
public class CreateClient implements OPCUAClient {
    
    private String name;
    private File file;
    private StatusCode statusCode;
    private NodeId binaryEncodingId;
    

    public CreateClient(String name, File fileIn) {
        
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
            UaClient uaClient = client.connect().get();
            
            System.out.println("Writing for name=\"" + name + "\"..");

            DataValue value = null;
            ExtensionObject xo = null;
            
            if(file!=null) {               
                System.out.println("if");
                FileWrapperOPCUA wrapper = new FileWrapperOPCUA(file);
                xo = ExtensionObject.encode(wrapper, binaryEncodingId);
                /*Variant variant = new Variant(xo);
                value = new DataValue(variant);*/
                System.out.println("Wrote xo file..");              
            } else {
                System.out.println("File is null");
            }
            System.out.println("TEST");
            List<AddNodesItem> list = new ArrayList();
            AddNodesItem item = new AddNodesItem(ExpandedNodeId.NULL_VALUE,
                    NodeId.NULL_NUMERIC, new ExpandedNodeId(new NodeId(1, name)), 
                    new QualifiedName(1, name), NodeClass.ObjectType,
                    xo, ExpandedNodeId.NULL_VALUE);
            
            list.add(item);
            
            CompletableFuture<AddNodesResponse> fr = uaClient.addNodes(list);
            
            if(fr.get().getResults().length > 0) {
                statusCode = fr.get().getResults()[0].getStatusCode();
            }

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
