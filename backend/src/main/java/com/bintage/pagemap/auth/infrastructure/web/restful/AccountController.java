package com.bintage.pagemap.auth.infrastructure.web.restful;

import com.bintage.pagemap.auth.application.AccountAuth;
import com.bintage.pagemap.auth.application.AccountInfo;
import com.bintage.pagemap.auth.infrastructure.security.AuthenticatedAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountInfo accountInfo;
    private final AccountAuth accountAuth;

    @GetMapping("/me")
    public Map<String, Object> getUser(@AuthenticationPrincipal AuthenticatedAccount authenticatedAccount) {
        var response = accountInfo.getAccountInfo(authenticatedAccount.getName());
        return Map.of("nickname", response.nickname(),
                "mapCount", response.mapCount(),
                "webPageCount", response.webPageCount());
    }

    @DeleteMapping("/me")
    public Map<String, String> signOut(@AuthenticationPrincipal AuthenticatedAccount account) {
        accountAuth.deleteAccount(account.getName());
        return Map.of("message", "deleted account");
    }

    @PutMapping("/me")
    public Map<String, String> changeNickname(@AuthenticationPrincipal AuthenticatedAccount account, @RequestBody String nickname) {
        var changedNickname = accountInfo.changeNickname(account.getName(), nickname);
        return Map.of("message", "success", "changedNickname", changedNickname);
    }
}
