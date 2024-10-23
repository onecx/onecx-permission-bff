package org.tkit.onecx.permission.bff.rs;

import io.quarkus.runtime.annotations.ConfigDocFilename;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

/**
 * Permission bff client configuration
 */
@ConfigDocFilename("onecx-permission-bff.adoc")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
@ConfigMapping(prefix = "onecx.permission")
public interface PermissionConfig {

    /**
     * rest-client configurations
     */
    @WithName("rest-client")
    RestClientsConfig restClients();

    /**
     * Rest-clients config
     */
    interface RestClientsConfig {
        /**
         * Config for iam_kc_svc client
         */
        @WithName("onecx_iam_svc")
        RestClientConfig iam();
    }

    /**
     * Rest-client config
     */
    interface RestClientConfig {

        /**
         * Enable or disable client
         */
        @WithDefault("true")
        @WithName("enabled")
        boolean enabled();
    }
}
