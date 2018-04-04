/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package web.controller.communication.opcua.client;

import interfaces.OPCUAClient;
import com.google.common.collect.ImmutableList;
import core.controller.data.FileWrapperOPCUA;
import core.controller.utils.Utilities;
import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.UaClient;
import org.eclipse.milo.opcua.stack.core.StatusCodes;
import org.eclipse.milo.opcua.stack.core.UaSerializationException;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.ExpandedNodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.ExtensionObject;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.enumerated.NodeClass;
import org.eclipse.milo.opcua.stack.core.types.structured.AddNodesItem;
import org.eclipse.milo.opcua.stack.core.types.structured.AddNodesResponse;
import org.eclipse.milo.opcua.stack.core.types.structured.DeleteNodesItem;
import org.eclipse.milo.opcua.stack.core.types.structured.DeleteNodesResponse;

/**
 *
 * @author Jan-Peter.Schmidt
 */
public class DeleteClient implements OPCUAClient {
    
    private String name;
    private StatusCode statusCode;
    private File file;
    
    
    public DeleteClient(String name) {
        
        this.name = name;
        
    }

    @Override
    public void run(OpcUaClient client, CompletableFuture<OpcUaClient> future) {
        System.out.println("Run client");
        
        try {
            UaClient uaClient = client.connect().get();
            
            NodeId representation = new NodeId(2, name);

            System.out.println("Deleting for name=\"" + name + "\"..");
            
            List<DeleteNodesItem> list = new ArrayList();
            DeleteNodesItem item = new DeleteNodesItem(representation, true);
            
            list.add(item);
            
            CompletableFuture<DeleteNodesResponse> fr = uaClient.deleteNodes(list);
            
            if(fr.get().getResults().length > 0) {
                statusCode = fr.get().getResults()[0];
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
    public StatusCode getStatusCode() {
        return statusCode;
    }

    @Override
    public void setStatusCode(StatusCode statusCode) {
        this.statusCode = statusCode;
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public void setFile(File file) {
        this.file = file;
    }
    
}
