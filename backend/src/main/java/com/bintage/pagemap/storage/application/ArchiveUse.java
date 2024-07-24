package com.bintage.pagemap.storage.application;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.application.dto.*;
import com.bintage.pagemap.storage.domain.model.bookmark.Bookmark;
import com.bintage.pagemap.storage.domain.model.bookmark.BookmarkException;
import com.bintage.pagemap.storage.domain.model.bookmark.BookmarkRepository;
import com.bintage.pagemap.storage.domain.model.folder.Folder;
import com.bintage.pagemap.storage.domain.model.folder.FolderException;
import com.bintage.pagemap.storage.domain.model.folder.FolderRepository;
import com.bintage.pagemap.storage.domain.model.validation.ArchiveCounter;
import com.bintage.pagemap.storage.domain.model.validation.ArchiveCounterException;
import com.bintage.pagemap.storage.domain.model.validation.ArchiveCounterRepository;
import com.bintage.pagemap.storage.domain.model.validation.DefaultArchiveCounter;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.hexagonal.PrimaryPort;
import org.springframework.modulith.NamedInterface;
import org.springframework.stereotype.Service;

import java.util.Optional;

@NamedInterface("readOnly")
@PrimaryPort
@Service
@Transactional
@RequiredArgsConstructor
public class ArchiveUse {

    private final ArchiveCounterRepository archiveCounterRepository;
    private final FolderRepository folderRepository;
    private final BookmarkRepository bookmarkRepository;

    public void visitWebPage(long webPageIdLong) {
        bookmarkRepository.findById(new Bookmark.BookmarkId(webPageIdLong))
                .ifPresent(Bookmark::visit);
    }

    public CurrentMapResponse getMap(String accountIdStr, long mapIdLong) {
        var accountId = new Account.AccountId(accountIdStr);

        var mapId = new Folder.FolderId(mapIdLong);
        var map = folderRepository.findFamilyById(accountId, mapId)
                .orElseThrow(() -> FolderException.notFound(accountId, mapId));

        return CurrentMapResponse.from(map);
    }

    public SpecificArchiveResponse getAllOnTheTopLevel(String accountIdStr) {
        var accountId = new Account.AccountId(accountIdStr);

        var topFolders = folderRepository.findAllByParentId(accountId, Folder.TOP_LEVEL);
        var topBookmarks = bookmarkRepository.findByParentFolderId(accountId, Bookmark.TOP_LEVEL);

        return SpecificArchiveResponse.from(topFolders, topBookmarks);
    }

    public BookmarkDto getWebPage(String accountIdStr, Long webPageIdStr) {
        var accountId = new Account.AccountId(accountIdStr);
        var webPageId = new Bookmark.BookmarkId(webPageIdStr);

        var webPage = bookmarkRepository.findById(webPageId)
                .orElseThrow(() -> BookmarkException.notFound(accountId, webPageId));

        return BookmarkDto.from(webPage);
    }

    public ArchiveCountDto getArchiveCount(String accountIdStr) {
        var accountId = new Account.AccountId(accountIdStr);
        var archiveCounter = archiveCounterRepository.findByAccountId(accountId)
                .or(() -> Optional.of(archiveCounterRepository.save(DefaultArchiveCounter.create(accountId))))
                .orElseThrow(() -> ArchiveCounterException.notFound(accountId));

        return ArchiveCountDto.of(accountId,
                archiveCounter.getCurrentCount(ArchiveCounter.CountType.FOLDER),
                archiveCounter.getCurrentCount(ArchiveCounter.CountType.BOOKMARK));
    }
}
