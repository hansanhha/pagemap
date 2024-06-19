package com.bintage.pagemap.auth.domain.account;

import com.bintage.pagemap.auth.domain.exception.AccountDomainModelException;
import lombok.Builder;
import lombok.Getter;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.types.Identifier;
import org.jmolecules.ddd.types.ValueObject;
import org.springframework.modulith.NamedInterface;

import java.time.Instant;

@AggregateRoot
@Getter
@Builder
public class Account{

    private static final long NICKNAME_MODIFY_INTERVAL = 30 * 24 * 60 * 60;

    private final AccountId id;
    private String nickname;
    private final Role role;
    private final OAuth2Provider oAuth2Provider;
    private final OAuth2MemberIdentifier oAuth2MemberIdentifier;
    private final Instant createdAt;
    private Instant lastNicknameModifiedAt;
    private Instant lastModifiedAt;

    public static Account toAccount(AccountId id, String nickname, Role role, OAuth2Provider oAuth2Provider, OAuth2MemberIdentifier oAuth2MemberIdentifier, Instant createdAt, Instant lastNicknameModifiedAt, Instant lastModifiedAt) {
        return Account.builder()
                .id(id)
                .nickname(nickname)
                .role(role)
                .oAuth2Provider(oAuth2Provider)
                .oAuth2MemberIdentifier(oAuth2MemberIdentifier)
                .createdAt(createdAt)
                .lastNicknameModifiedAt(lastNicknameModifiedAt)
                .lastModifiedAt(lastModifiedAt)
                .build();
    }

    public void updateNickname(String updateNickname) {
        Instant now = Instant.now();



        if (lastNicknameModifiedAt != null) {
            Instant allowedUpdateNicknameDate = lastNicknameModifiedAt.plusSeconds(NICKNAME_MODIFY_INTERVAL);
            if (now.isBefore(allowedUpdateNicknameDate)) {
                throw new AccountDomainModelException.IsNotUpdatablePeriodAccountNickname(id, allowedUpdateNicknameDate);
            }
        }

        nickname = updateNickname;
        lastNicknameModifiedAt = now;
        lastModifiedAt = now;
    }

    @NamedInterface(name = "accountId")
    public record AccountId(String value) implements Identifier {
    }

    public enum Role implements ValueObject {
        ADMIN,
        USER,
    }

    public record OAuth2MemberIdentifier(String value) implements ValueObject {
    }

    public enum OAuth2Provider {
        KAKAO,
        NAVER,
        GOOGLE
    }
}
