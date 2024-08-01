package com.bintage.pagemap.storage.application;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.application.dto.BookmarkDto;
import com.bintage.pagemap.storage.application.dto.FolderDto;
import com.bintage.pagemap.storage.domain.model.bookmark.Bookmark;
import com.bintage.pagemap.storage.domain.model.bookmark.BookmarkRepository;
import com.bintage.pagemap.storage.domain.model.folder.Folder;
import com.bintage.pagemap.storage.domain.model.folder.FolderRepository;
import com.bintage.pagemap.storage.domain.model.validation.ArchiveCounter;
import com.bintage.pagemap.storage.domain.model.validation.ArchiveCounterException;
import com.bintage.pagemap.storage.domain.model.validation.ArchiveCounterRepository;
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
    private final ArchiveCounterRepository archiveCounterRepository;

    public Map<String, Object> getDeleteScheduledArchives(String accountIdStr) {
        var accountId = new Account.AccountId(accountIdStr);

        List<Folder> scheduledFolders = folderRepository.findAllDeleteScheduledByAccountId(accountId);
        List<Bookmark> scheduledBookmarks = bookmarkRepository.findAllDeleteScheduledByAccountId(accountId);

        var folderIds = scheduledFolders.stream().map(Folder::getId).toList();
        var parentFolder = scheduledFolders.stream().filter(folder -> !folderIds.contains(folder.getParentFolderId())).toList();
        var noParentBookmark = scheduledBookmarks.stream().filter(bookmark -> !folderIds.contains(bookmark.getParentFolderId())).toList();
        var scheduledFolderDtos = parentFolder.stream().map(FolderDto::from).toList();
        var scheduledBookmarkDtos = noParentBookmark.stream().map(BookmarkDto::from).toList();

        return Map.of(
                "folders", scheduledFolderDtos,
                "bookmarks", scheduledBookmarkDtos
        );
    }

    public void empty(String accountIdStr) {
        var accountId = new Account.AccountId(accountIdStr);

        List<Folder> scheduledFolders = folderRepository.findAllDeleteScheduledByAccountId(accountId);
        List<Bookmark> scheduledBookmarks = bookmarkRepository.findAllDeleteScheduledByAccountId(accountId);
        var archiveCounter = archiveCounterRepository.findByAccountId(accountId)
                .orElseThrow(() -> ArchiveCounterException.notFound(accountId));

        archiveCounter.decrease(ArchiveCounter.CountType.BOOKMARK, scheduledBookmarks.size());
        archiveCounter.decrease(ArchiveCounter.CountType.FOLDER, scheduledFolders.size());

        folderRepository.deleteAll(accountId, scheduledFolders);
        bookmarkRepository.deleteAll(accountId, scheduledBookmarks);
        archiveCounterRepository.update(archiveCounter);
    }

}
