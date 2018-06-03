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
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.stack.core.UaSerializationException;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.ExtensionObject;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;

/**
 * OPC UA Client that sends a read request to the opc ua server.
 * @author Jan-Peter.Schmidt
 */
public class ReadClient implements OPCUAClient {
    
    private String name;
    private StatusCode statusCode;
    private File file;
    
    
    public ReadClient(String name) {
        
        this.name = name;
        
    }

    @Override
    public void run(OpcUaClient client, CompletableFuture<OpcUaClient> future) {
        System.out.println("Run client");
        
        try {
            client.connect().get();
            
            List<NodeId> nodeIds = ImmutableList.of(new NodeId(2, name));

            System.out.println("Reading for name=\"" + name + "\"..");
            // write asynchronously....
            CompletableFuture<DataValue> fr =
                client.readValue(Double.POSITIVE_INFINITY, null, nodeIds.get(0));

            if(fr.get().getStatusCode().isGood() && fr.get().getValue().getValue() instanceof ExtensionObject) {
                System.out.println("if");
                ExtensionObject xo = (ExtensionObject) fr.get().getValue().getValue();
                System.out.println("xo=" + xo);
                FileWrapperOPCUA wrapper = xo.decode();
                System.out.println("Returned File:\n\n" + Utilities.readFile(wrapper.getFile().getAbsolutePath(), Charset.defaultCharset()));
                file = wrapper.getFile();
                
            }
                       
            System.out.println("Statuscode Read: " + fr.get().getStatusCode());
            statusCode = fr.get().getStatusCode();

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
