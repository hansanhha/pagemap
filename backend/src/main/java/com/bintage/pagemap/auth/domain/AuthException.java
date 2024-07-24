package com.bintage.pagemap.auth.domain;

import com.bintage.pagemap.auth.domain.account.Account;
import lombok.Getter;
import org.springframework.http.HttpHeaders;
import org.springframework.modulith.NamedInterface;

import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@NamedInterface("exception")
public abstract class AuthException extends RuntimeException {

    protected final Account.AccountId accountId;
    protected final Instant occurAt;
    protected final Map<String, Object> properties;
    protected final AuthExceptionCode authExceptionCode;
    private final HttpHeaders headers;

    public AuthException(Account.AccountId accountId, AuthExceptionCode authExceptionCode, Map<String, Object> properties, Instant occurAt) {
        super("raised AuthException [accountId : ".concat(accountId.value())
                .concat("] [detail: ").concat(authExceptionCode.getDetailCode()).concat(", ").concat(authExceptionCode.getTitle())
                .concat("] [time:").concat(occurAt.toString()).concat("]")
                .concat(properties == null ? "" : "[properties: "
                        .concat(properties.entrySet().stream()
                                .map(entry -> " [" + entry.getKey() + ": " + entry.getValue() + "]")
                                .collect(Collectors.joining())).concat("]"))
        );

        this.accountId = accountId;
        this.authExceptionCode = authExceptionCode;
        this.properties = properties;
        this.occurAt = occurAt;
        headers = initHeaders();
    }

    private HttpHeaders initHeaders() {
        return new HttpHeaders();
    }

    public abstract String getProblemDetailTitle();
    public abstract String getProblemDetailDetail();
    public abstract URI getProblemDetailInstance();


}
