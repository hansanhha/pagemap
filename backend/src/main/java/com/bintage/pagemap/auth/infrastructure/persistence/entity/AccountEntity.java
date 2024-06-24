package com.bintage.pagemap.auth.infrastructure.persistence.entity;

import com.bintage.pagemap.auth.domain.account.Account;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.time.Instant;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AccountEntity {

    @Id
    private String id;

    @Setter
    @Column(unique = true)
    private String nickname;

    @Setter
    @Enumerated(EnumType.STRING)
    private Account.Role role;

    @Enumerated(EnumType.STRING)
    private Account.OAuth2Provider oAuth2Provider;

    private String oAuth2MemberIdentifier;

    private Timestamp createdAt;

    @Setter
    private Timestamp lastNicknameModifiedAt;

    @Setter
    private Timestamp lastModifiedAt;

    public static AccountEntity fromDomainModel(Account account) {
        return new AccountEntity(account.getId().value(),
                account.getNickname(),
                account.getRole(),
                account.getOAuth2Provider(),
                account.getOAuth2MemberIdentifier().value(),
                Timestamp.from(account.getCreatedAt()),
                Timestamp.from(account.getLastNicknameModifiedAt()),
                Timestamp.from(account.getLastModifiedAt()));
    }

    public static Account toDomainModel(AccountEntity accountEntity) {
        return Account.toAccount(
                new Account.AccountId(accountEntity.id),
                accountEntity.nickname,
                accountEntity.role,
                accountEntity.oAuth2Provider,
                new Account.OAuth2MemberIdentifier(accountEntity.oAuth2MemberIdentifier),
                accountEntity.createdAt.toInstant(),
                accountEntity.lastNicknameModifiedAt.toInstant(),
                accountEntity.lastModifiedAt.toInstant());
    }
}
