package com.bintage.pagemap.storage.domain.model;

import com.bintage.pagemap.auth.domain.account.Account;
import lombok.Builder;
import lombok.Getter;
import org.jmolecules.ddd.annotation.AggregateRoot;

import java.util.List;

@AggregateRoot
@Builder
@Getter
public class RootMap {

    private final Account.AccountId accountId;
    private final Map.MapId id;
    private List<Map> children;
    private List<WebPage> webPages;

    public void addChild(Map child) {
        if (children.contains(child)) {
            throw new StorageException.AlreadyItemExistException(StorageException.Item.MAP, child.getId().value().toString());
        }

        children.add(child);
    }

    public void removeChild(Map child) {
        if (!children.contains(child)) {
            throw new StorageException.NotExistContainItemException(StorageException.Item.MAP, child.getId().value().toString());
        }

        children.remove(child);
    }

    public void addPage(WebPage webPage) {
        if (webPages.contains(webPage)) {
            throw new StorageException.AlreadyContainItemException(StorageException.Item.PAGE, webPage.getId().value());
        }

        webPages.add(webPage);
    }

    public void removePage(WebPage webPage) {
        if (!webPages.contains(webPage)) {
            throw new StorageException.NotExistContainItemException(StorageException.Item.PAGE, webPage.getId().value());
        }

        webPages.remove(webPage);
    }
}
