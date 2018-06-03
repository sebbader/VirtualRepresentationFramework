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

package web.controller.communication.opcua.client;

import interfaces.OPCUAClient;
import com.github.andrewoma.dexx.collection.ArrayLists;
import java.io.File;
import java.security.Security;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfig;
import org.eclipse.milo.opcua.stack.client.UaTcpStackClient;
import org.eclipse.milo.opcua.stack.core.Stack;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.eclipse.milo.opcua.stack.core.util.CryptoRestrictions;
import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;

/**
 * This class is an updated version of the ClientRunner example of 
 * the Milo Example.
 * 
 * This class is able to run different implementations of the opc
 * ua client. It is possible to run a create, read, update or delete
 * client.
 * 
 */

public class ClientRunner {

    static {
        CryptoRestrictions.remove();

        // Required for SecurityPolicy.Aes256_Sha256_RsaPss
        Security.addProvider(new BouncyCastleProvider());
    }

    private final Logger logger = Logger.getLogger(ClientRunner.class.getName());

    private final CompletableFuture<OpcUaClient> future = new CompletableFuture<>();

    //private ExampleServer exampleServer;

    private final OPCUAClient clientExample;
    private final boolean serverRequired;

    public ClientRunner(OPCUAClient clientExample) throws Exception {
        this(clientExample, true);
    }

    public ClientRunner(OPCUAClient clientExample, boolean serverRequired) throws Exception {
        this.clientExample = clientExample;
        this.serverRequired = serverRequired;
    }

    private OpcUaClient createClient() throws Exception {
        File securityTempDir = new File(System.getProperty("java.io.tmpdir"), "security");
        if (!securityTempDir.exists() && !securityTempDir.mkdirs()) {
            throw new Exception("unable to create security dir: " + securityTempDir);
        }
        logger.log(Level.INFO, "security temp dir: {" + securityTempDir.getAbsolutePath() + "}");

        KeyStoreLoader loader = new KeyStoreLoader().load(securityTempDir);

        SecurityPolicy securityPolicy = clientExample.getSecurityPolicy();

        EndpointDescription[] endpoints = null;

        try {
            endpoints = UaTcpStackClient
                .getEndpoints(clientExample.getEndpointUrl())
                .get();
            
            logger.log(Level.TRACE, "Endpoints are: ");
            Arrays.asList(endpoints).forEach(ep -> {
                logger.log(Level.TRACE, ep);
            });

        } catch (Throwable ex) {
            ex.printStackTrace();
        }

        if(endpoints!=null) {
            EndpointDescription endpoint = Arrays.stream(endpoints)
                .filter(e -> e.getSecurityPolicyUri().equals(securityPolicy.getSecurityPolicyUri()))
                .findFirst().orElseThrow(() -> new Exception("no desired endpoints returned"));

            logger.log(Level.INFO, "Using endpoint: " + endpoint.getEndpointUrl());

            OpcUaClientConfig config = OpcUaClientConfig.builder()
                .setApplicationName(LocalizedText.english("virtrepframework opc-ua client"))
                .setApplicationUri("urn:virtrepframework:net:client")
                .setCertificate(loader.getClientCertificate())
                .setKeyPair(loader.getClientKeyPair())
                .setEndpoint(endpoint)
                .setIdentityProvider(clientExample.getIdentityProvider())
                .setRequestTimeout(uint(5000))
                .build();

            return new OpcUaClient(config);
        
        } else {
            
            System.out.println("No endpoints found.");
            return null;
            
        }
    }

    public void run() {
        try {
            OpcUaClient client = createClient();
            
            System.out.println("Created client.");

            future.whenComplete((c, ex) -> {
                System.out.println("Future ist complete");
                if (ex != null) {
                    ex.printStackTrace();
                    logger.error(ex.getMessage());
                }
                /**
                 * If Client disconnect a reconnect is not possible. -> Connection refused.
                 */
                //                try {
                //                    System.out.println("Try to disconnect");
                //                    client.disconnect().get();
                //                    System.out.println("Disconnected");
                //                    /*if (serverRequired && exampleServer != null) {
                //                        exampleServer.shutdown().get();
                //                    }*/
                //                    Stack.releaseSharedResources();
                //                    System.out.println("Released shared resources");
                //                } catch (InterruptedException | ExecutionException e) {
                //                    e.printStackTrace();
                //                    logger.log(Level.ERROR, "Error disconnecting" + e.getMessage());
                //                }

                    System.out.println("Ready when done.");
            });

            try {
                System.out.println("Client example run..");
                clientExample.run(client, future);
                System.out.println("Finished with client run.");
                future.get(15, TimeUnit.SECONDS);
                System.out.println("after get client run.");
            } catch (Throwable t) {
                logger.log(Level.ERROR, "Error running client example: { " + t.getMessage() + ", " + t);
                future.completeExceptionally(t);
            }
        } catch (Throwable t) {
            logger.log(Level.ERROR, "Error getting client: { "+  t.getMessage() + ", " + t);

            future.completeExceptionally(t);

        }
    }

}
