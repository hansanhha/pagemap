package com.bintage.pagemap.storage.application;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.application.dto.BookmarkDto;
import com.bintage.pagemap.storage.application.dto.FolderDto;
import com.bintage.pagemap.storage.domain.model.bookmark.Bookmark;
import com.bintage.pagemap.storage.domain.model.bookmark.BookmarkRepository;
import com.bintage.pagemap.storage.domain.model.folder.Folder;
import com.bintage.pagemap.storage.domain.model.folder.FolderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.hexagonal.PrimaryPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@PrimaryPort
@Service
@Transactional
@RequiredArgsConstructor
public class ArchiveTrash {

    private final FolderRepository folderRepository;
    private final BookmarkRepository bookmarkRepository;

    public Map<String, Object> getDeleteScheduledArchives(String accountIdStr) {
        var accountId = new Account.AccountId(accountIdStr);

        List<Folder> scheduledFolders = folderRepository.findAllDeleteScheduledByAccountId(accountId);
        List<Bookmark> scheduledBookmarks = bookmarkRepository.findAllDeleteScheduledByAccountId(accountId);

        var scheduledFolderDtos = scheduledFolders.stream().map(FolderDto::from).toList();
        var scheduledBookmarkDtos = scheduledBookmarks.stream().map(BookmarkDto::from).toList();

        return Map.of(
                "folders", scheduledFolderDtos,
                "bookmarks", scheduledBookmarkDtos
        );
    }

    public void empty(String accountIdStr) {
        var accountId = new Account.AccountId(accountIdStr);

        List<Folder> scheduledFolders = folderRepository.findAllDeleteScheduledByAccountId(accountId);
        List<Bookmark> scheduledBookmarks = bookmarkRepository.findAllDeleteScheduledByAccountId(accountId);

        folderRepository.deleteAll(accountId, scheduledFolders);
        bookmarkRepository.deleteAll(accountId, scheduledBookmarks);
    }

}
