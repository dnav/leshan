/*******************************************************************************
 * Copyright (c) 2020 Sierra Wireless and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * 
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *    http://www.eclipse.org/org/documents/edl-v10.html.
 * 
 * Contributors:
 *     Sierra Wireless - initial API and implementation
 *******************************************************************************/
package org.eclipse.leshan.server.bootstrap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.leshan.core.node.LwM2mNode;
import org.eclipse.leshan.core.node.LwM2mPath;
import org.eclipse.leshan.core.request.BootstrapDeleteRequest;
import org.eclipse.leshan.core.request.BootstrapDownlinkRequest;
import org.eclipse.leshan.core.request.BootstrapWriteRequest;
import org.eclipse.leshan.core.request.Identity;
import org.eclipse.leshan.core.response.LwM2mResponse;
import org.eclipse.leshan.server.bootstrap.BootstrapConfig.ACLConfig;
import org.eclipse.leshan.server.bootstrap.BootstrapConfig.ServerConfig;
import org.eclipse.leshan.server.bootstrap.BootstrapConfig.ServerSecurity;

@SuppressWarnings("deprecation")
public class BootstrapConfigurationStoreAdapter implements BootstrapConfigurationStore {

    private BootstrapConfigStore internalStore;

    public BootstrapConfigurationStoreAdapter(BootstrapConfigStore store) {
        this.internalStore = store;
    }

    @Override
    public BootstrapConfiguration get(String endpoint, Identity deviceIdentity, BootstrapSession session) {
        BootstrapConfig bootstrapConfig = internalStore.get(endpoint, deviceIdentity, session);
        if (bootstrapConfig == null)
            return null;

        List<BootstrapDownlinkRequest<? extends LwM2mResponse>> requests = new ArrayList<>();
        // handle delete
        for (String path : bootstrapConfig.toDelete) {
            requests.add(new BootstrapDeleteRequest(path));
        }
        // handle security
        for (Entry<Integer, ServerSecurity> security : bootstrapConfig.security.entrySet()) {
            LwM2mPath path = new LwM2mPath(0, security.getKey());
            final LwM2mNode securityInstance = BootstrapUtil.convertToSecurityInstance(security.getKey(),
                    security.getValue());
            final BootstrapWriteRequest writeBootstrapRequest = new BootstrapWriteRequest(path, securityInstance,
                    session.getContentFormat());
            requests.add(writeBootstrapRequest);
        }
        // handle server
        for (Entry<Integer, ServerConfig> server : bootstrapConfig.servers.entrySet()) {
            LwM2mPath path = new LwM2mPath(1, server.getKey());
            final LwM2mNode securityInstance = BootstrapUtil.convertToServerInstance(server.getKey(),
                    server.getValue());
            final BootstrapWriteRequest writeBootstrapRequest = new BootstrapWriteRequest(path, securityInstance,
                    session.getContentFormat());
            requests.add(writeBootstrapRequest);
        }
        // handle acl
        for (Entry<Integer, ACLConfig> acl : bootstrapConfig.acls.entrySet()) {
            LwM2mPath path = new LwM2mPath(2, acl.getKey());
            final LwM2mNode securityInstance = BootstrapUtil.convertToAclInstance(acl.getKey(), acl.getValue());
            final BootstrapWriteRequest writeBootstrapRequest = new BootstrapWriteRequest(path, securityInstance,
                    session.getContentFormat());
            requests.add(writeBootstrapRequest);
        }
        return new BootstrapConfiguration(requests);
    }
}
