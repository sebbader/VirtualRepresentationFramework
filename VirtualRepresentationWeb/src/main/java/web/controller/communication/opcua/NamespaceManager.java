/*
 * Copyright (c) 2016 Kevin Herron
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *   http://www.eclipse.org/org/documents/edl-v10.html.
 */

package web.controller.communication.opcua;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.google.common.collect.Lists;
import core.controller.communication.ReadResponse;
import core.controller.data.FileWrapperOPCUA;
import core.controller.utils.Utilities;
import core.controller.virtualrepresentations.VirtualRepresentationManager;
import interfaces.WebCommunication;
import java.io.File;
import java.nio.charset.Charset;
import org.eclipse.milo.opcua.sdk.core.Reference;
import org.eclipse.milo.opcua.sdk.server.OpcUaServer;
import org.eclipse.milo.opcua.sdk.server.api.AccessContext;
import org.eclipse.milo.opcua.sdk.server.api.DataItem;
import org.eclipse.milo.opcua.sdk.server.api.MethodInvocationHandler;
import org.eclipse.milo.opcua.sdk.server.api.MonitoredItem;
import org.eclipse.milo.opcua.sdk.server.api.Namespace;
import org.eclipse.milo.opcua.sdk.server.nodes.ServerNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaMethodNode;
import org.eclipse.milo.opcua.sdk.server.util.SubscriptionModel;
import org.eclipse.milo.opcua.stack.core.StatusCodes;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.OpcUaBinaryDataTypeDictionary;
import org.eclipse.milo.opcua.stack.core.types.OpcUaDataTypeManager;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.ExtensionObject;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UShort;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.eclipse.milo.opcua.stack.core.types.structured.ReadValueId;
import org.eclipse.milo.opcua.stack.core.types.structured.WriteValue;
import org.eclipse.milo.opcua.stack.core.util.FutureUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Change class from Milo example. This class forwards the read and write (update)
 * operations from a client to the virtual representation framework. Afterwards the
 * answer from the manager is converted to a opc ua answer.
 * 
 */

public class NamespaceManager implements Namespace, WebCommunication {

    public static final String NAMESPACE_URI = "urn:virtrepframework:net";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final SubscriptionModel subscriptionModel;

    private final OpcUaServer server;
    private final UShort namespaceIndex;

    public NamespaceManager(OpcUaServer server, UShort namespaceIndex) {
        this.server = server;
        this.namespaceIndex = namespaceIndex;

        subscriptionModel = new SubscriptionModel(server, this);

    }

    @Override
    public UShort getNamespaceIndex() {
        return namespaceIndex;
    }

    @Override
    public String getNamespaceUri() {
        return NAMESPACE_URI;
    }

    @Override
    public CompletableFuture<List<Reference>> browse(AccessContext context, NodeId nodeId) {
        ServerNode node = server.getNodeMap().get(nodeId);

        if (node != null) {
            return CompletableFuture.completedFuture(node.getReferences());
        } else {
            return FutureUtils.failedFuture(new UaException(StatusCodes.Bad_NodeIdUnknown));
        }
    }

    /**
     * This method receives a read request from a opc ua client. Afterwards
     * it is converted to a read operation for a VirtualRepresentationManager.
     * The answer of him is reconverted to opc ua and sent back to the client.
     */
    
    @Override
    public void read(ReadContext context, Double maxAge, 
        TimestampsToReturn timestamps, List<ReadValueId> readValueIds) {
        
        System.out.println("READ Recognized");
        System.out.println("Context " + context + ", maxAge=" + maxAge);
        
        List<DataValue> results = Lists.newArrayListWithCapacity(readValueIds.size());
        
        // Create a dictionary, binaryEncodingId, and register the codec under that id
        OpcUaBinaryDataTypeDictionary dictionary = new OpcUaBinaryDataTypeDictionary(
            "urn:virtrepframework:net:file-wrapper"
        );

        NodeId binaryEncodingId = new NodeId(namespaceIndex, "DataType.FileWrapperOPCUA.BinaryEncoding");

        dictionary.registerStructCodec(
            new FileWrapperOPCUA.Codec().asBinaryCodec(),
            "FileWrapperOPCUA",
            binaryEncodingId
        );

        // Register dictionary with the shared DataTypeManager instance
        OpcUaDataTypeManager.getInstance().registerTypeDictionary(dictionary);        
        
        readValueIds.forEach((rvi) -> {
            
            String name = String.valueOf(rvi.getNodeId().getIdentifier());
            
            System.out.println("Read for name=" + name);
            
            ReadResponse readResponse = executeRead(name, "application/rdf+xml");
            
            if(readResponse.getFile()==null) {
                
                System.out.println("RR: File is null");
                
            } else {
                
                System.out.println("RR: File is not null");
                
            }
            
            FileWrapperOPCUA wrapper = new FileWrapperOPCUA(readResponse.getFile());
            
            System.out.println("ReadValue: " + rvi.getNodeId().getIdentifier());
            System.out.println("STATUS READ: " + readResponse.getStatus());
            
            DataValue value = null;
            
            switch (readResponse.getStatus()) {
                case 2: //do same as in case 1
                case 1:
                    ExtensionObject xo = ExtensionObject.encode(wrapper, binaryEncodingId);
                    value = new DataValue(new Variant(xo), StatusCode.GOOD);
                    break;
                case -1:
                    value = new DataValue(StatusCodes.Bad_NodeIdUnknown);
                    break;
                default:
                    value = new DataValue(StatusCodes.Bad_InternalError);
                    break;
            }
            
            results.add(value);
            
        });                
        
        System.out.println("Read complete.");
        context.complete(results);
        
    }

    /*
     * This method receives a write (update) request from a opc ua client. Afterwards
     * it is converted to a write operation for a VirtualRepresentationManager.
     * The answer of him is reconverted to opc ua and sent back to the client.
    */    
    @Override
    public void write(WriteContext context, List<WriteValue> writeValues) {
                
        System.out.println("WRITING INCOMING\n\nContext " + context.toString());
        
        List<StatusCode> results = Lists.newArrayListWithCapacity(writeValues.size());
        
        writeValues.forEach((wv) -> {
            
            StatusCode statusCode = new StatusCode(StatusCodes.Bad_UnexpectedError);
            File file = null;
            String name = String.valueOf(wv.getNodeId().getIdentifier());
            
            System.out.println("writeValue: " + wv.getNodeId());
            if(wv.getValue()!=null && 
                wv.getValue().getValue().isNotNull() && 
                wv.getValue().getValue().getValue() !=null &&
                wv.getValue().getValue().getValue() instanceof ExtensionObject) {
                
                ExtensionObject xo = (ExtensionObject) wv.getValue().getValue().getValue();
                FileWrapperOPCUA wrapper = xo.decode();
                file = wrapper.getFile();
                System.out.println("READ FILE SERVER\n\n" + Utilities.readFile(file.getAbsolutePath(), Charset.defaultCharset()));
                
            }
            
            switch(VirtualRepresentationManager.update(name, file)) {
                
                case -2:
                    statusCode = new StatusCode(StatusCodes.Bad_NodeIdUnknown);
                    break;
                    
                case -1:
                    statusCode = new StatusCode(StatusCodes.Bad_NoData);
                    break;
                
                case 1:
                    statusCode = new StatusCode(StatusCodes.Good_Edited);
                    break;
                
            }
            
            System.out.println("StatusCode is: " + statusCode);
            results.add(statusCode);
            
        });
        
        System.out.println("Write complete");
        context.complete(results);

    }

    @Override
    public void onDataItemsCreated(List<DataItem> dataItems) {
        subscriptionModel.onDataItemsCreated(dataItems);
    }

    @Override
    public void onDataItemsModified(List<DataItem> dataItems) {
        subscriptionModel.onDataItemsModified(dataItems);
    }

    @Override
    public void onDataItemsDeleted(List<DataItem> dataItems) {
        subscriptionModel.onDataItemsDeleted(dataItems);
    }

    @Override
    public void onMonitoringModeChanged(List<MonitoredItem> monitoredItems) {
        subscriptionModel.onMonitoringModeChanged(monitoredItems);
    }

    @Override
    public Optional<MethodInvocationHandler> getInvocationHandler(NodeId methodId) {
        Optional<ServerNode> node = server.getNodeMap().getNode(methodId);

        return node.flatMap(n -> {
            if (n instanceof UaMethodNode) {
                return ((UaMethodNode) n).getInvocationHandler();
            } else {
                return Optional.empty();
            }
        });
    }
    
    @Override
    public int executeCreate(String name, File file) {
        throw new UnsupportedOperationException("Use NodeManagerService manager for creating.");
    }

    @Override
    public ReadResponse executeRead(String name, String accept) {
        return VirtualRepresentationManager.read(name, accept);
    }

    @Override
    public int executeUpdate(String name, File file) {
        return VirtualRepresentationManager.update(name, file);
    }

    @Override
    public int executeDelete(String name) {
        throw new UnsupportedOperationException("Use NodeManagerService manager for deleting.");
    }

}
