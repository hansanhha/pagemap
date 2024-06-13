package com.bintage.pagemap.storage.domain.model;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.domain.exception.DomainModelException;
import com.bintage.pagemap.storage.domain.exception.StorageException;
import lombok.Builder;
import lombok.Getter;
import org.jmolecules.ddd.annotation.AggregateRoot;

import java.util.List;

import static com.bintage.pagemap.storage.domain.exception.StorageException.*;

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
            throw DomainModelException.AlreadyContainChildException.hideParentId(Item.ROOT_MAP, Item.MAP, child.getId().value());
        }

        children.add(child);
    }

    public void removeChild(Map child) {
        if (!children.contains(child)) {
            throw DomainModelException.NotContainChildException.hideParentId(Item.ROOT_MAP, Item.MAP, child.getId().value());
        }

        children.remove(child);
    }

    public void addWebPage(WebPage webPage) {
        if (webPages.contains(webPage)) {
            throw DomainModelException.AlreadyContainChildException.hideParentId(Item.ROOT_MAP, Item.WEB_PAGE, webPage.getId().value());
        }

        webPages.add(webPage);
    }

    public void removeWebPage(WebPage webPage) {
        if (!webPages.contains(webPage)) {
            throw DomainModelException.NotContainChildException.hideParentId(Item.ROOT_MAP, Item.WEB_PAGE, webPage.getId().value());
        }

        webPages.remove(webPage);
    }
}
