package com.bintage.pagemap.storage.application;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.application.dto.ArchiveCountDto;
import com.bintage.pagemap.storage.application.dto.BookmarkDto;
import com.bintage.pagemap.storage.application.dto.CurrentFolderResponse;
import com.bintage.pagemap.storage.application.dto.SpecificArchiveResponse;
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

import java.util.Comparator;
import java.util.List;
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

    public CurrentFolderResponse getFolder(String accountIdStr, long mapIdLong) {
        var accountId = new Account.AccountId(accountIdStr);

        var folderId = new Folder.FolderId(mapIdLong);
        var folder = folderRepository.findFamilyById(accountId, folderId)
                .orElseThrow(() -> FolderException.notFound(accountId, folderId));

        List<Folder> sortedChildrenFolder = folder.getChildrenFolder().stream()
                .sorted(Comparator.comparing(Folder::getCreatedAt))
                .toList();

        List<Bookmark> sortedChildrenBookmark = folder.getChildrenBookmark().stream()
                .sorted(Comparator.comparing(Bookmark::getCreatedAt))
                .toList();

        return CurrentFolderResponse.from(folder, sortedChildrenFolder, sortedChildrenBookmark);
    }

    public SpecificArchiveResponse getAllOnTheTopLevel(String accountIdStr) {
        var accountId = new Account.AccountId(accountIdStr);

        var topFolders = folderRepository.findAllByParentId(accountId, Folder.TOP_LEVEL);
        var topBookmarks = bookmarkRepository.findByParentFolderId(accountId, Bookmark.TOP_LEVEL);

        var sortedTopFolders = topFolders.stream().sorted(Comparator.comparing(Folder::getCreatedAt)).toList();
        var sortedTopBookmarks = topBookmarks.stream().sorted(Comparator.comparing(Bookmark::getCreatedAt)).toList();

        return SpecificArchiveResponse.from(sortedTopFolders, sortedTopBookmarks);
    }

    public BookmarkDto getBookmark(String accountIdStr, Long webPageIdStr) {
        var accountId = new Account.AccountId(accountIdStr);
        var bookmarkId = new Bookmark.BookmarkId(webPageIdStr);

        var bookmark = bookmarkRepository.findById(bookmarkId)
                .orElseThrow(() -> BookmarkException.notFound(accountId, bookmarkId));

        return BookmarkDto.from(bookmark);
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
