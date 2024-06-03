package com.bintage.pagemap.auth.infrastructure.web.restful;

import com.bintage.pagemap.auth.application.AccountInfo;
import com.bintage.pagemap.auth.application.AuthPort;
import com.bintage.pagemap.auth.infrastructure.security.AuthenticatedAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountInfo accountInfo;
    private final AuthPort authPort;

    @GetMapping("/me")
    public Map<String, String> getUser(@AuthenticationPrincipal AuthenticatedAccount authenticatedAccount) {
        var response = accountInfo.getAccountInfo(authenticatedAccount.getName());
        return Map.of("nickname", response.nickname(),
                "email", authenticatedAccount.getName(),
                "permission", authenticatedAccount.getAuthorities().toString());
    }

    @DeleteMapping("/me")
    public Map<String, String> signOut(@AuthenticationPrincipal AuthenticatedAccount account) {
        authPort.deleteAccount(account.getName());
        return Map.of("message", "deleted account");
    }

    @GetMapping("/me/devices")
    public Map<String, Object> getUserDevice(@AuthenticationPrincipal AuthenticatedAccount account) {
        var accountDevice = accountInfo.getAccountDevice(account.getName(), account.getTokenId());
        return Map.of("devices", accountDevice.devices(), "currentDeviceId", accountDevice.currentDeviceId());
    }

    @DeleteMapping("/me/devices/{deviceId}")
    public Map<String, String> signOutDevice(@AuthenticationPrincipal AuthenticatedAccount account, @PathVariable String deviceId) {
        authPort.signOutForOtherDevice(deviceId);
        return Map.of("message", "success");
    }

    @PutMapping("/me")
    public Map<String, String> changeNickname(@AuthenticationPrincipal AuthenticatedAccount account, @RequestBody String nickname) {
        var changedNickname = accountInfo.changeNickname(account.getName(), nickname);
        return Map.of("message", "success", "changedNickname", changedNickname);
    }
}
