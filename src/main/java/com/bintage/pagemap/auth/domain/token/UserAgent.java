package com.bintage.pagemap.auth.domain.token;

import com.bintage.pagemap.auth.domain.account.Account;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.jmolecules.ddd.annotation.Entity;
import org.jmolecules.ddd.types.Identifier;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Entity
@Builder
@Getter
public class UserAgent {

    private final UserAgentId id;
    @Setter
    private Account.AccountId accountId;
    @Setter(value = AccessLevel.PRIVATE)
    private boolean signedIn;
    private Instant lastSignedIn;
    private Instant lastSignedOut;
    private Instant lastModifiedAt;
    private final Type type;
    private final OS os;
    private final Device device;
    private final Application application;
    private final Set<Token> tokens;

    public void signIn() {
        Instant now = Instant.now();
        signedIn = true;
        lastSignedIn = now;
        lastModifiedAt = now;
    }

    public void signOut() {
        Instant now = Instant.now();
        lastSignedOut = now;
        lastModifiedAt = now;
        signedIn = false;
        tokens.forEach(token -> token.expire(now));
    }

    public static boolean isSame(UserAgent u1, UserAgent u2) {
        return u1.getOs().equals(u2.getOs()) &&
                u1.getDevice().equals(u2.getDevice()) &&
                u1.getApplication().equals(u2.getApplication());
    }

    public boolean isSame(Type type, OS os, Device device, Application application) {
        return this.type.equals(type) && this.os.equals(os) && this.device.equals(device) && this.application.equals(application);
    }

    public record UserAgentId(UUID value) implements Identifier {
    }

    public enum Type {
        DESKTOP,
        MOBILE;
    }

    public enum OS {
        ANDROID,
        IOS,
        WINDOWS,
        MAC_OS_X,
        LINUX,
        UNIX,;
    }

    public enum Application {
        CHROME,
        FIREFOX,
        SAFARI,
        EDGE,
        IE,
        BRAVE,
        OPERA,
        ARC,
        MIN,
        VIA,
        KIWI,
        DUCKDUCKGO,
        SAMSUNG,
        VIVALDI,
        WHALE,
        PUFFIN;
    }

    public enum Device {
        WINDOWS,
        MAC,
        IPHONE,
        IPAD,
        GALAXY,
        PIXEL,
        XPERIA,
        MOTO,
        NEXUS;
    }
}
