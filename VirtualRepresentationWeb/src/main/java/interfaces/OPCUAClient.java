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

package interfaces;

import java.io.File;
import java.util.concurrent.CompletableFuture;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.identity.AnonymousProvider;
import org.eclipse.milo.opcua.sdk.client.api.identity.IdentityProvider;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;

/**
 * This interface is taken from the OPCUAClient for milo client examples. 
 * It provides neccesary information to run a client.
 * @author Jan-Peter.Schmidt
 */
public interface OPCUAClient {

    default String getEndpointUrl() {
        return "opc.tcp://localhost:12686/representations";
    }

    default SecurityPolicy getSecurityPolicy() {
        return SecurityPolicy.None;
    }

    default IdentityProvider getIdentityProvider() {
        return new AnonymousProvider();
    }

    void run(OpcUaClient client, CompletableFuture<OpcUaClient> future) throws Exception;
    
    public String getName();

    public void setName(String name);

    public StatusCode getStatusCode();

    public void setStatusCode(StatusCode statusCode);

    public File getFile();

    public void setFile(File file); 

}
