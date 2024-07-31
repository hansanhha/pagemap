package com.bintage.pagemap.storage.domain.model.bookmark;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.domain.model.folder.Folder;

import java.net.URI;
import java.util.List;

public class DefaultBookmarkProvider {

    public static List<Bookmark> create(Account.AccountId accountId, Folder.FolderId folderId) {
        return List.of(
                youtube(accountId, folderId),
                naver(accountId, folderId),
                instagram(accountId, folderId),
                twitter(accountId, folderId)
        );
    }

    private static Bookmark youtube(Account.AccountId accountId, Folder.FolderId folderId) {
        return Bookmark.builder()
                .accountId(accountId)
                .parentFolderId(folderId)
                .name("YouTube")
                .uri(URI.create("https://www.youtube.com/"))
                .build();
    }

    private static Bookmark naver(Account.AccountId accountId, Folder.FolderId folderId) {
        return Bookmark.builder()
                .accountId(accountId)
                .parentFolderId(folderId)
                .name("Naver")
                .uri(URI.create("https://www.naver.com/"))
                .build();
    }

    private static Bookmark instagram(Account.AccountId accountId, Folder.FolderId folderId) {
        return Bookmark.builder()
                .accountId(accountId)
                .parentFolderId(folderId)
                .name("Instagram")
                .uri(URI.create("https://www.instagram.com/"))
                .build();
    }

    private static Bookmark twitter(Account.AccountId accountId, Folder.FolderId folderId) {
        return Bookmark.builder()
                .accountId(accountId)
                .parentFolderId(folderId)
                .name("X (Twitter)")
                .uri(URI.create("https://www.twitter.com/"))
                .build();
    }

}
