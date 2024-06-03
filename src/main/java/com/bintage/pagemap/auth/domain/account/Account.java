package com.bintage.pagemap.auth.domain.account;

import lombok.Builder;
import lombok.Getter;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.types.Identifier;
import org.jmolecules.ddd.types.ValueObject;

import java.time.Instant;

@AggregateRoot
@Getter
@Builder
public class Account{

    private final AccountId id;
    private String nickname;
    private final Role role;
    private final OAuth2Provider oAuth2Provider;
    private final OAuth2MemberIdentifier oAuth2MemberIdentifier;
    private final Instant createdAt;
    private Instant lastModifiedAt;

    public static Account toAccount(AccountId id, String nickname, Role role, OAuth2Provider oAuth2Provider, OAuth2MemberIdentifier oAuth2MemberIdentifier, Instant createdAt, Instant lastModifiedAt) {
        return Account.builder()
                .id(id)
                .nickname(nickname)
                .role(role)
                .oAuth2Provider(oAuth2Provider)
                .oAuth2MemberIdentifier(oAuth2MemberIdentifier)
                .createdAt(createdAt)
                .lastModifiedAt(lastModifiedAt)
                .build();
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
        this.lastModifiedAt = Instant.now();
    }

    public record AccountId(String value) implements Identifier {
    }

    public enum Role implements ValueObject {
        ADMIN,
        USER,
        USER_RESTRICTED_CHANGE_NICKNAME
    }

    public record OAuth2MemberIdentifier(String value) implements ValueObject {
    }

    public enum OAuth2Provider {
        KAKAO,
        NAVER,
        GOOGLE
    }
}
