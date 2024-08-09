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

import java.util.*;

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

    public CurrentFolderResponse getFolder(String accountIdStr, long folderIdVal, ArchiveFetchType type) {
        var accountId = new Account.AccountId(accountIdStr);

        var folderId = new Folder.FolderId(folderIdVal);
        var folder = folderRepository.findFamilyById(accountId, folderId)
                .orElseThrow(() -> FolderException.notFound(accountId, folderId));

        List<Folder> sortedChildrenFolder = new LinkedList<>();
        List<Bookmark> sortedChildrenBookmark = new LinkedList<>();

        if (type.equals(ArchiveFetchType.BOTH) || type.equals(ArchiveFetchType.FOLDER)) {
            sortedChildrenFolder.addAll(
                    folder.getChildrenFolder()
                            .stream()
                            .sorted(Comparator.comparing(Folder::getOrder))
                            .toList());
        }

        if (type.equals(ArchiveFetchType.BOTH) || type.equals(ArchiveFetchType.BOOKMARK)) {
            sortedChildrenBookmark.addAll(
                    folder.getChildrenBookmark()
                            .stream()
                            .sorted(Comparator.comparing(Bookmark::getOrder))
                            .toList());
        }

        return CurrentFolderResponse.from(folder, sortedChildrenFolder, sortedChildrenBookmark);
    }

    public List<CurrentFolderResponse> getFolders(String accountIdStr, List<Long> folderIdVals, ArchiveFetchType type) {
        return folderIdVals.stream()
                .map(id -> getFolder(accountIdStr, id, type))
                .toList();
    }

    public SpecificArchiveResponse getAllOnTheTopLevel(String accountIdStr, ArchiveFetchType type) {
        var accountId = new Account.AccountId(accountIdStr);

        List<Folder> sortedTopFolders = new LinkedList<>();
        List<Bookmark> sortedTopBookmarks = new LinkedList<>();

        if (type.equals(ArchiveFetchType.BOTH) || type.equals(ArchiveFetchType.FOLDER)) {
            var topFolders = folderRepository.findAllByParentId(accountId, Folder.TOP_LEVEL);
            sortedTopFolders.addAll(topFolders.stream().sorted(Comparator.comparing(Folder::getOrder)).toList());
        }
        if (type.equals(ArchiveFetchType.BOTH) || type.equals(ArchiveFetchType.BOOKMARK)) {
            var topBookmarks = bookmarkRepository.findAllByParentFolderId(accountId, Bookmark.TOP_LEVEL);
            sortedTopBookmarks.addAll(topBookmarks.stream().sorted(Comparator.comparing(Bookmark::getOrder)).toList());
        }

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

    public enum ArchiveFetchType {
        BOTH,
        FOLDER,
        BOOKMARK;

        public static ArchiveFetchType of(String type) {
            if (type == null || type.isBlank()) {
                return BOTH;
            }

            type = type.trim();
            type = type.toLowerCase();

            if (type.equals("bookmark") || type.equals("bookmarks")) {
                return BOOKMARK;
            }

            if (type.equals("folder") || type.equals("folders")) {
                return FOLDER;
            }

            return BOTH;
        }
    }
}
