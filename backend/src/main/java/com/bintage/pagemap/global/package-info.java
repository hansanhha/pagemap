@ApplicationModule(displayName = "global",
        allowedDependencies = {
                "storage::exception",
                "storage::exceptionCode",
                "auth::exception",
                "auth::exceptionCode"})

package com.bintage.pagemap.global;

import org.springframework.modulith.ApplicationModule;