import Style from "./styles/Main.module.css";
import Header from "../../components/header/Header";
import Categories from "../../components/category/Categories";
import ArchiveUtil from "../../components/archive_util/ArchiveUtil";
import ArchiveDetails from "../../components/archives/ArchiveDetails";
import {ArchiveService} from "../../service/ArchiveService";
import React, {createContext, useEffect, useRef, useState} from "react";
import {BookmarkService} from "../../service/BookmarkService";
import {FolderService} from "../../service/FolderService";
import ArchiveOverview from "../../components/archives/ArchiveOverview";

export const entireCategoryId = 0;
export const CategoryContext = createContext();

function ArchivePage() {
    const [overviewFolders, setOverviewFolders] = useState([]);
    const [overviewBookmarks, setOverviewBookmarks] = useState([]);
    const [selectedOverviewFolder, setSelectedOverviewFolder] = useState(null);
    const [categories, setCategories] = useState([]);
    const [currentCategoryId, setCurrentCategoryId] = useState(entireCategoryId);
    const bodyRef = useRef(null);

    useEffect(() => {
        ArchiveService.getMainArchives()
            .then(([folders, bookmarks]) => {
                setOverviewFolders(folders.slice());
                setOverviewBookmarks(bookmarks.slice());
            });

        const handleDragOver = (e) => {
            e.preventDefault();
        }

        const handleDrop = async (e) => {
            e.preventDefault();

            const types = e.dataTransfer.types;
            console.log(e.dataTransfer.getData("text/plain"));
            console.log(e.dataTransfer.getData("text/uri-list"));
            console.log(e.dataTransfer.getData("text/title"));

            // 북마크 파일 업로드 처리 필요
            if (types.includes("Files")) {
                if (e.dataTransfer.items) {
                    [...e.dataTransfer.items].forEach((item, i) => {
                        item = item.webkitGetAsEntry();
                        if (item.isFile) {
                            console.log('File has been dropped');
                        } else if (item.isDirectory) {
                            console.log('Directory has been dropped');
                        }
                    });
                } else {
                    [...e.dataTransfer.files].forEach((file, i) => {
                        console.log(`… file[${i}].name = ${file.name}`);
                    });
                }
                return;
            }

            // html 요소를 드래그앤드롭한 경우
            if (types.includes("text/html")) {
                const html = e.dataTransfer.getData("text/html");

                const parser = new DOMParser();
                const document = parser.parseFromString(html, "text/html");
                const a = document.querySelectorAll("a")[0];

                if (a) {
                    handleCreateBookmark(a.innerText, entireCategoryId, a.href, null);
                }
            }

            // 사용자가 브라우저의 북마크를 드래그앤드롭한 경우 등 (북마크 폴더 자체를 인식하는 로직 필요)
            if (types.includes("text/uri-list") || types.includes("text/plain")) {
                const url = e.dataTransfer.getData("text/plain");
                const uriList = e.dataTransfer.getData("text/uri-list");

                const splitUrls = url.split("\n");
                const splitUriList = uriList.split("\n");
                const bookmarkUrls = splitUrls.length > splitUriList.length ? splitUrls : splitUriList;

                let completeBookmarkUrls = bookmarkUrls.map(bookmarkUrl => BookmarkService.completeUrl(bookmarkUrl));

                if (completeBookmarkUrls.some(completeBookmarkUrl => !BookmarkService.validateUrl(completeBookmarkUrl))) {
                    return;
                }

                const createdBookmarks = await handleDroppedNonTitleBookmark(completeBookmarkUrls);
                if (createdBookmarks && createdBookmarks.length > 0) {
                    setOverviewBookmarks(prevBookmarks => [...prevBookmarks, ...createdBookmarks]);
                }
            }

        }

        const handlePaste = async (e) => {
            e.preventDefault();

            const text = e.clipboardData.getData("text");
            const url = BookmarkService.completeUrl(text);
            if (BookmarkService.validateUrl(url)) {
                const createdBookmark = (await BookmarkService.requiredAutoCreate([url]))[0];
                if (createdBookmark) {
                    setOverviewBookmarks(prevBookmarks => [...prevBookmarks, createdBookmark]);
                }
            }
        }

        const bodyElement = bodyRef.current;

        bodyElement.addEventListener("dragover", handleDragOver);
        bodyElement.addEventListener("drop", handleDrop);
        bodyElement.addEventListener("paste", handlePaste);

        return () => {
            bodyElement.removeEventListener("dragover", handleDragOver);
            bodyElement.removeEventListener("drop", handleDrop);
            bodyElement.removeEventListener("paste", handlePaste);
        };
    }, []);

    async function handleDroppedNonTitleBookmark(urls) {
        return await BookmarkService.requiredAutoCreate(urls);
    }

    function handleSelectedOverviewFolder(folder) {
        if (folder.id === selectedOverviewFolder?.id) {
            setSelectedOverviewFolder(null);
            return;
        }

        FolderService.getById(folder.id)
            .then(folder => setSelectedOverviewFolder(folder));
    }

    function handleCurrentCategory(clickedCategoryId) {
        if (clickedCategoryId === entireCategoryId && currentCategoryId === entireCategoryId) {
            return;
        }

        if (clickedCategoryId === currentCategoryId || clickedCategoryId === entireCategoryId) {
            setSelectedOverviewFolder(null);
            setCurrentCategoryId(entireCategoryId);
            ArchiveService.getMainArchives()
                .then(([folders, bookmarks]) => {
                    setOverviewFolders(folders.slice());
                    setOverviewBookmarks(bookmarks.slice());
                });
            return;
        }

        if (clickedCategoryId > 0) {
            setSelectedOverviewFolder(null);
            setCurrentCategoryId(clickedCategoryId);
            ArchiveService.getArchivesByCategoryId(clickedCategoryId)
                .then(([folders, bookmarks]) => {
                    setOverviewFolders(folders.slice());
                    setOverviewBookmarks(bookmarks.slice());
                });
        }
    }

    function handleCreateBookmark(name, parentFolderId, url, categoryIds) {
        BookmarkService.create(name, parentFolderId, url, categoryIds)
            .then((createdBookmarkId) => {
                if (isRequiredOverviewArchiveReRendering(currentCategoryId, parentFolderId, categoryIds)) {
                    BookmarkService.getById(createdBookmarkId)
                        .then(createdBookmark => {
                            setOverviewBookmarks(prevBookmarks => [...prevBookmarks, createdBookmark]);
                        });
                }
            });
    }

    function handleCreateFolder(name, parentFolderId, categoryIds) {
        FolderService.create(name, parentFolderId, categoryIds)
            .then((createdFolderId) => {
                if (isRequiredOverviewArchiveReRendering(currentCategoryId, parentFolderId, categoryIds)) {
                    FolderService.getById(createdFolderId)
                        .then(createdFolder => {
                            setOverviewFolders(prevFolders => [...prevFolders, createdFolder]);
                        });
                }
            });
    }

    return (
        <>
            <Header/>
            <div className={Style.body} ref={bodyRef}>
                <CategoryContext.Provider value={{categories, setCategories}}>
                    <div className={Style.main_container}>
                        <div className={Style.category_container}>
                            <Categories currentCategory={currentCategoryId}
                                        onChangeCurrentCategory={handleCurrentCategory}/>
                        </div>
                        <div className={Style.archive_util_container}>
                            <ArchiveUtil bookmarks={overviewBookmarks}
                                         folders={overviewFolders}
                                         categories={categories}
                                         onCreateBookmark={handleCreateBookmark}
                                         onCreateFolder={handleCreateFolder}
                            />
                        </div>
                        <div className={Style.archive_contents_container}>
                            <div className={Style.archive_overview_container}>
                                <ArchiveOverview folders={overviewFolders}
                                                 bookmarks={overviewBookmarks}
                                                 setSelectedOverviewFolder={handleSelectedOverviewFolder}
                                                 setOverviewFolders={setOverviewFolders}
                                                 setOverviewBookmarks={setOverviewBookmarks}
                                />
                            </div>
                            <div className={Style.archive_details_container}>
                                {
                                    selectedOverviewFolder &&
                                    (selectedOverviewFolder.childrenFolder.length > 0 ||
                                        selectedOverviewFolder.childrenBookmark.length > 0) &&
                                    <ArchiveDetails selectedOverviewFolder={selectedOverviewFolder}
                                                    childrenFolders={selectedOverviewFolder.childrenFolder}
                                                    childrenBookmarks={selectedOverviewFolder.childrenBookmark}
                                    />
                                }
                            </div>
                        </div>
                    </div>
                </CategoryContext.Provider>
            </div>
            <div className={Style.bottom}>
                <div>@Copyright Pagemap</div>
            </div>
        </>
    );
}


function isRequiredOverviewArchiveReRendering(currentCategoryId, parentId, categoryIds) {
    if (categoryIds && (categoryIds.includes(currentCategoryId.toString())
        || (currentCategoryId === entireCategoryId && parentId !== entireCategoryId))) {
        return false;
    } else if (!categoryIds && currentCategoryId !== entireCategoryId && parentId !== currentCategoryId) {
        return false;
    } else if (!categoryIds && currentCategoryId === entireCategoryId && parentId !== entireCategoryId) {
        return false;
    }
    return true;
}

export default ArchivePage;