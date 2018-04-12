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

import java.io.File;
import java.security.Security;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.google.common.collect.ImmutableList;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.eclipse.milo.opcua.sdk.server.api.config.OpcUaServerConfig;
import org.eclipse.milo.opcua.sdk.server.identity.CompositeValidator;
import org.eclipse.milo.opcua.sdk.server.identity.UsernameIdentityValidator;
import org.eclipse.milo.opcua.sdk.server.identity.X509IdentityValidator;
import org.eclipse.milo.opcua.sdk.server.util.HostnameUtil;
import org.eclipse.milo.opcua.stack.core.application.DefaultCertificateManager;
import org.eclipse.milo.opcua.stack.core.application.DirectoryCertificateValidator;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.types.builtin.DateTime;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.structured.BuildInfo;
import org.eclipse.milo.opcua.stack.core.util.CertificateUtil;
import org.eclipse.milo.opcua.stack.core.util.CryptoRestrictions;
import org.slf4j.LoggerFactory;

import static com.google.common.collect.Lists.newArrayList;
import org.eclipse.milo.opcua.sdk.server.OpcUaServer;
import static org.eclipse.milo.opcua.sdk.server.api.config.OpcUaServerConfig.USER_TOKEN_POLICY_ANONYMOUS;
import static org.eclipse.milo.opcua.sdk.server.api.config.OpcUaServerConfig.USER_TOKEN_POLICY_USERNAME;
import static org.eclipse.milo.opcua.sdk.server.api.config.OpcUaServerConfig.USER_TOKEN_POLICY_X509;

public class VirtualRepresenationOpcUaServer {

    static {
        CryptoRestrictions.remove();

        // Required for SecurityPolicy.Aes256_Sha256_RsaPss
        Security.addProvider(new BouncyCastleProvider());
    }

    private OpcUaServer server;

    public VirtualRepresenationOpcUaServer() throws Exception {
        
        File securityTempDir = new File(System.getProperty("java.io.tmpdir"), "security");
        if (!securityTempDir.exists() && !securityTempDir.mkdirs()) {
            throw new Exception("unable to create security temp dir: " + securityTempDir);
        }
        LoggerFactory.getLogger(getClass()).info("security temp dir: {}", securityTempDir.getAbsolutePath());

        KeyStoreLoader loader = new KeyStoreLoader().load(securityTempDir);

        DefaultCertificateManager certificateManager = new DefaultCertificateManager(
            loader.getServerKeyPair(),
            loader.getServerCertificateChain()
        );

        File pkiDir = securityTempDir.toPath().resolve("pki").toFile();
        DirectoryCertificateValidator certificateValidator = new DirectoryCertificateValidator(pkiDir);
        LoggerFactory.getLogger(getClass()).info("pki dir: {}", pkiDir.getAbsolutePath());

        UsernameIdentityValidator identityValidator = new UsernameIdentityValidator(
            true,
            authChallenge -> {
                String username = authChallenge.getUsername();
                String password = authChallenge.getPassword();

                boolean userOk = "user".equals(username) && "password1".equals(password);
                boolean adminOk = "admin".equals(username) && "password2".equals(password);

                return userOk || adminOk;
            }
        );

        X509IdentityValidator x509IdentityValidator = new X509IdentityValidator(c -> true);

        List<String> bindAddresses = newArrayList();
        bindAddresses.add("0.0.0.0");

        List<String> endpointAddresses = newArrayList();
        endpointAddresses.add(HostnameUtil.getHostname());
        endpointAddresses.addAll(HostnameUtil.getHostnames("0.0.0.0"));

        // The configured application URI must match the one in the certificate(s)
        try {
            String applicationUri = certificateManager.getCertificates().stream()
                .findFirst()
                .map(certificate ->
                    CertificateUtil.getSubjectAltNameField(certificate, CertificateUtil.SUBJECT_ALT_NAME_URI)
                        .map(Object::toString)
                        .<RuntimeException>orElseThrow(() -> 
                                new RuntimeException("certificate is missing the application URI")))
                .orElse("urn:virtrepframework:net:else:server:" + UUID.randomUUID());

            OpcUaServerConfig serverConfig = OpcUaServerConfig.builder()
                .setApplicationUri(applicationUri)
                .setApplicationName(LocalizedText.english("Virtual Representation Framework OPC UA Server"))
                .setBindPort(12686)
                .setBindAddresses(bindAddresses)
                .setEndpointAddresses(endpointAddresses)
                .setBuildInfo(
                    new BuildInfo(
                        "urn:virtrepframework:net:server:",
                        "KIT",
                        "virtrepframework opcua server",
                        OpcUaServer.SDK_VERSION,
                        "", DateTime.now()))
                .setCertificateManager(certificateManager)
                .setCertificateValidator(certificateValidator)
                .setIdentityValidator(new CompositeValidator(identityValidator, x509IdentityValidator))
                .setProductUri("urn:virtrepframework:net:server:")
                .setServerName("representations")
                .setSecurityPolicies(
                    EnumSet.of(
                        SecurityPolicy.None,
                        SecurityPolicy.Basic128Rsa15,
                        SecurityPolicy.Basic256,
                        SecurityPolicy.Basic256Sha256,
                        SecurityPolicy.Aes128_Sha256_RsaOaep,
                        SecurityPolicy.Aes256_Sha256_RsaPss))
                .setUserTokenPolicies(
                    ImmutableList.of(
                        USER_TOKEN_POLICY_ANONYMOUS,
                        USER_TOKEN_POLICY_USERNAME,
                        USER_TOKEN_POLICY_X509))
                .build();

            server = new OpcUaServer(serverConfig);
            server.getServer().addServiceSet(new NodeManager());

            server.getNamespaceManager().registerAndAdd(
                NamespaceManager.NAMESPACE_URI,
                idx -> new NamespaceManager(server, idx));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public OpcUaServer getServer() {
        return server;
    }

    public CompletableFuture<OpcUaServer> startup() {
        System.out.println("Server startup called");
        CompletableFuture<OpcUaServer> future =  server.startup();       
        
        return future;
    }

    public CompletableFuture<OpcUaServer> shutdown() {
        System.out.println("Server shutdown called");
        return server.shutdown();
    }   

}
