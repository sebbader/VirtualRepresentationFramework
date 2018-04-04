/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package web.controller.communication.opcua;

import core.controller.communication.ReadResponse;
import core.controller.data.FileWrapperOPCUA;
import core.controller.virtualrepresentations.VirtualRepresentationManager;
import interfaces.WebCommunication;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import org.eclipse.milo.opcua.sdk.server.services.NodeManagementServices;
import org.eclipse.milo.opcua.stack.core.StatusCodes;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.application.services.ServiceRequest;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.structured.AddNodesRequest;
import org.eclipse.milo.opcua.stack.core.types.structured.AddNodesResponse;
import org.eclipse.milo.opcua.stack.core.types.structured.AddNodesResult;
import org.eclipse.milo.opcua.stack.core.types.structured.AddReferencesRequest;
import org.eclipse.milo.opcua.stack.core.types.structured.AddReferencesResponse;
import org.eclipse.milo.opcua.stack.core.types.structured.DeleteNodesRequest;
import org.eclipse.milo.opcua.stack.core.types.structured.DeleteNodesResponse;
import org.eclipse.milo.opcua.stack.core.types.structured.DeleteReferencesRequest;
import org.eclipse.milo.opcua.stack.core.types.structured.DeleteReferencesResponse;
import org.eclipse.milo.opcua.stack.core.types.structured.ResponseHeader;

/**
 *
 * @author Jan-Peter.Schmidt
 */
public class NodeManager extends NodeManagementServices implements WebCommunication {
    
    public NodeManager() {
        
        super();
        System.out.println("NMS enabled");
        
    }
    
    @Override
    public void onAddNodes(ServiceRequest<AddNodesRequest, AddNodesResponse> service) throws UaException {
        try {
            System.out.println("onAdd");
            
            ArrayList<AddNodesResult> results = new ArrayList<>();
            
            Arrays.asList(service.getRequest().getNodesToAdd()).forEach((node) -> {
                
                File file = null;
                String name = String.valueOf(node.getRequestedNewNodeId().getIdentifier());
                StatusCode statusCode = new StatusCode(StatusCodes.Bad_UnexpectedError);
                System.out.println("Name is: " + name);
                
                if(node.getNodeAttributes()!=null) {
                    FileWrapperOPCUA wrapper = node.getNodeAttributes().decode();
                    file = wrapper.getFile();
                }
                
                switch(executeCreate(name, file)) {
                    
                    case 1:
                        statusCode = new StatusCode(StatusCodes.Good_NoData);
                        break;
                        
                    case 2:
                        statusCode = new StatusCode(StatusCodes.Good_EntryInserted);
                        break;
                        
                }
                
                System.out.println(statusCode + ": " + name + ", " + file);
                
                results.add(new AddNodesResult(statusCode, node.getTypeId()));
                
            });
            
            ResponseHeader responseHeader = service.createResponseHeader(StatusCode.GOOD);
            AddNodesResult[] resultsArray = new AddNodesResult[results.size()];
            results.toArray(resultsArray);
            AddNodesResponse response = new AddNodesResponse(responseHeader, resultsArray, null);
            service.setResponse(response);
            service.getFuture().get();
            if(service.getFuture().complete(response)) {
                System.out.println("Future completed with " + response.toString());
            } else {
                System.out.println("not completed");
            }
        } catch(Exception e)      {
            
            System.out.println("CATCH IN NMS");
            e.printStackTrace();
            
        }        
        
    }

    @Override
    public void onDeleteNodes(ServiceRequest<DeleteNodesRequest, DeleteNodesResponse> service) throws UaException {
        System.out.println("onDelete");
        
        try {
            ArrayList<StatusCode> results = new ArrayList<>();
            
            Arrays.asList(service.getRequest().getNodesToDelete()).forEach((node) -> {
                
                String name = String.valueOf(node.getNodeId().getIdentifier());
                StatusCode statusCode = new StatusCode(StatusCodes.Bad_UnexpectedError);
                System.out.println("Name is: " + name);
                
                switch(executeDelete(name)) {
                    
                    case -1:
                        statusCode = new StatusCode(StatusCodes.Bad_NodeIdUnknown);
                        break;
                        
                    case 1:
                        statusCode = new StatusCode(StatusCodes.Good_EntryReplaced);
                        break;                                                
                        
                }
                
                System.out.println(statusCode + ": " + name);
                
                results.add(statusCode);
                
            });
            
            StatusCode[] resultArray = new StatusCode[service.getRequest().getNodesToDelete().length];
            ResponseHeader responseHeader = service.createResponseHeader(StatusCode.GOOD);
            DeleteNodesResponse response = new DeleteNodesResponse(responseHeader, results.toArray(resultArray), null);
            service.setResponse(response);
            service.getFuture().get();
            if(service.getFuture().complete(response)) {
                System.out.println("Future completed with " + response.toString());
            } else {
                System.out.println("not completed");
            }
        } catch(Exception e) {
            
            System.out.println("CATCH IN NMS");
            e.printStackTrace();
            
        }
        
    }
    
    @Override
    public void onAddReferences(ServiceRequest<AddReferencesRequest, AddReferencesResponse> service) throws UaException {
        throw new UaException(new StatusCode(StatusCodes.Bad_ServiceUnsupported));
    }

    @Override
    public void onDeleteReferences(ServiceRequest<DeleteReferencesRequest, DeleteReferencesResponse> service) throws UaException {
        throw new UaException(new StatusCode(StatusCodes.Bad_ServiceUnsupported));
    }     

    @Override
    public int executeCreate(String name, File file) {
        return VirtualRepresentationManager.create(name, file);
    }

    @Override
    public ReadResponse executeRead(String name, String accept) {
        throw new UnsupportedOperationException("Use NameSpace manager for reading.");
    }

    @Override
    public int executeUpdate(String name, File file) {
        throw new UnsupportedOperationException("Use NameSpace manager for updating.");
    }

    @Override
    public int executeDelete(String name) {
        return VirtualRepresentationManager.delete(name);
    }
    
}
