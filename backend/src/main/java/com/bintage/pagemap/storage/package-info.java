@ApplicationModule(displayName = "storage",
    allowedDependencies = {"auth::accountId", "auth::authenticatedAccount", "auth::event"})
package com.bintage.pagemap.storage;

import org.springframework.modulith.ApplicationModule;