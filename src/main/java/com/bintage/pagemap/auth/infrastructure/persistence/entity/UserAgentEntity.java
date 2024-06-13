package com.bintage.pagemap.auth.infrastructure.persistence.entity;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.auth.domain.token.Token;
import com.bintage.pagemap.auth.domain.token.UserAgent;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class UserAgentEntity {

    @Id
    private UUID id;

    @Embedded
    @AttributeOverride(name = "account", column = @Column(name = "account_id"))
    private AccountEntity accountEntity;

    private boolean signedIn;

    @Enumerated(EnumType.STRING)
    private UserAgent.OS os;

    @Enumerated(EnumType.STRING)
    private UserAgent.Application application;

    @Enumerated(EnumType.STRING)
    private UserAgent.Device device;

    @Enumerated(EnumType.STRING)
    private UserAgent.Type type;

    private Timestamp lastSignedInAt;

    private Timestamp lastSignedOutAt;

    private Timestamp lastModifiedAt;

    public static UserAgentEntity fromDomainModel(UserAgent userAgent) {
        var entity = new UserAgentEntity();
        if (userAgent.getAccountId() == null) {
            entity.accountEntity = new AccountEntity(null);
            entity.lastSignedInAt = null;
            entity.lastSignedOutAt = null;
            entity.lastModifiedAt = null;
        } else {
            entity.accountEntity = new AccountEntity(userAgent.getAccountId().value());
            entity.lastModifiedAt = Timestamp.from(userAgent.getLastModifiedAt());
            entity.lastSignedInAt = Timestamp.from(userAgent.getLastSignedIn());
            entity.lastSignedOutAt= Timestamp.from(userAgent.getLastSignedOut());
        }
        entity.id = userAgent.getId().value();
        entity.signedIn = userAgent.isSignedIn();
        entity.os = userAgent.getOs();
        entity.application = userAgent.getApplication();
        entity.device = userAgent.getDevice();
        entity.type = userAgent.getType();
        return entity;
    }

    public static UserAgent toDomainModel(UserAgentEntity entity) {
        Account.AccountId accountId = null;
        Instant lastSignedIn = null;
        Instant lastSignedOut = null;
        Instant lastModifiedAt = null;
        if (entity.getAccountEntity() != null) {
            accountId = new Account.AccountId(entity.getAccountEntity().accountId());
            lastSignedIn = entity.getLastSignedInAt().toInstant();
            lastModifiedAt = entity.getLastModifiedAt().toInstant();
        }

        if (entity.getLastSignedOutAt() != null) {
            lastSignedOut = entity.getLastSignedOutAt().toInstant();
        }

        return UserAgent.builder()
                .id(new UserAgent.UserAgentId((entity.getId())))
                .accountId(accountId)
                .signedIn(entity.isSignedIn())
                .os(entity.getOs())
                .application(entity.getApplication())
                .device(entity.getDevice())
                .type(entity.getType())
                .tokens(null)
                .lastSignedIn(lastSignedIn)
                .lastSignedOut(lastSignedOut)
                .lastModifiedAt(lastModifiedAt)
                .build();
    }

    public static UserAgent toDomainModel(UserAgentEntity entity, Set<Token> tokens) {
        Instant lastSignedOut = null;
        if (entity.getLastSignedOutAt() != null) {
            lastSignedOut = entity.getLastSignedOutAt().toInstant();
        }

        return UserAgent.builder()
                .id(new UserAgent.UserAgentId((entity.getId())))
                .accountId(new Account.AccountId(entity.getAccountEntity().accountId()))
                .signedIn(entity.isSignedIn())
                .os(entity.getOs())
                .application(entity.getApplication())
                .device(entity.getDevice())
                .type(entity.getType())
                .tokens(tokens)
                .lastSignedIn(entity.getLastSignedInAt().toInstant())
                .lastSignedOut(lastSignedOut)
                .lastModifiedAt(entity.getLastModifiedAt().toInstant())
                .build();
    }

    public void updateSignIn(UserAgent userAgent) {
        this.signedIn = userAgent.isSignedIn();
        this.lastSignedInAt = Timestamp.from(userAgent.getLastSignedIn());
        this.lastModifiedAt = Timestamp.from(userAgent.getLastModifiedAt());
    }

    public void updateSignOut(UserAgent userAgent) {
        this.signedIn = false;
        this.lastSignedOutAt = Timestamp.from(userAgent.getLastSignedOut());
        this.lastModifiedAt = Timestamp.from(userAgent.getLastModifiedAt());
    }

    public void updateAccount(String accountId) {
        Assert.isNull(this.accountEntity, "account accountId already exists");
        this.accountEntity =  new AccountEntity(accountId);
    }

    public record AccountEntity(String accountId) {
    }
}
