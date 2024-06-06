package com.bintage.pagemap.storage.application;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.application.dto.MapStoreRequest;
import com.bintage.pagemap.storage.application.dto.WebPageStoreRequest;
import com.bintage.pagemap.storage.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class ArchiveStoreTest {

    @InjectMocks
    private ArchiveStore archiveStore;

    @Mock
    private MapRepository mapRepository;

    @Mock
    private WebPageRepository webPageRepository;

    @Mock
    private CategoriesRepository categoriesRepository;

    private static final Account.AccountId ACCOUNT_ID = new Account.AccountId("test id");
    private final ArgumentCaptor<Map> mapArgumentCaptor = ArgumentCaptor.forClass(Map.class);
    private final ArgumentCaptor<WebPage> webPageArgumentCaptor = ArgumentCaptor.forClass(WebPage.class);
    private final ArgumentCaptor<Categories> categoriesArgumentCaptor = ArgumentCaptor.forClass(Categories.class);

    private Map rootMap;
    private Categories categories;
    private Tags tags;

    @BeforeEach
    void setUp() {
        rootMap = Map.builder()
                .id(new Map.MapId(UUID.randomUUID()))
                .accountId(ACCOUNT_ID)
                .build();

        categories = Categories.builder()
                .id(new Categories.CategoriesId(UUID.randomUUID()))
                .accountId(ACCOUNT_ID)
                .registeredCategories(Set.of(new Categories.Category("ca1"), new Categories.Category("ca2")))
                .build();

        tags = Tags.of(Set.of("tag1", "tag2"));
    }


    @Test
    void shouldStoreMapWhenValidRequest() {
        var usedTitle = "test map title";
        var usedDescription = "test map description";
        var parentId = rootMap.getId().value().toString();
        var usedCategories = categories.getRegisteredCategories().stream().map(Categories.Category::name).collect(Collectors.toSet());
        var usedTags = tags.getNames();
        var mapStoreRequest = new MapStoreRequest(ACCOUNT_ID.value(),
                parentId,
                usedTitle,
                usedDescription,
                usedCategories,
                usedTags);

        given(categoriesRepository.findByAccountId(any(Account.AccountId.class)))
                .willReturn(Optional.of(categories));

        willReturn(null)
                .given(mapRepository)
                .save(any(Map.class));

        var mapStoreResponse = archiveStore.storeMap(mapStoreRequest);

        then(categoriesRepository).should(times(1)).findByAccountId(any(Account.AccountId.class));
        then(mapRepository).should(times(1)).save(mapArgumentCaptor.capture());

        var savedMap = mapArgumentCaptor.getValue();
        assertThat(mapStoreResponse).isNotNull();
        assertThat(savedMap).isNotNull();
        assertThat(savedMap.getId().value().toString()).isEqualTo(mapStoreResponse.storedMapId());
        assertThat(savedMap.getParent().getId().value().toString()).isEqualTo(parentId);
        assertThat(savedMap.getChildren().size()).isZero();
        assertThat(savedMap.getTitle()).isEqualTo(usedTitle);
        assertThat(savedMap.getDescription()).isEqualTo(usedDescription);
        assertThat(savedMap.getCategories().size()).isEqualTo(usedCategories.size());
        assertThat(savedMap.getTags().getNames().size()).isEqualTo(usedTags.size());
        assertThat(savedMap.getWebPageIds().size()).isZero();
        assertThat(savedMap.getDeleted()).isEqualTo(Trash.Delete.notScheduled());
    }

    @Test
    void shouldStoreWebPageWhenValidRequest() {
        var usedTitle = "test webpage title";
        var usedDescription = "test webpage description";
        var usedMapId = rootMap.getId().value().toString();
        var usedUri = URI.create("http://www.testuri.com");
        var usedCategories = categories.getRegisteredCategories().stream().map(Categories.Category::name).collect(Collectors.toSet());
        var usedTags = tags.getNames();
        var webPageStoreRequest = new WebPageStoreRequest(ACCOUNT_ID.value(),
                usedMapId,
                usedTitle,
                usedUri,
                usedDescription,
                usedCategories,
                usedTags);

        given(categoriesRepository.findByAccountId(any(Account.AccountId.class)))
                .willReturn(Optional.of(categories));

        willReturn(null)
                .given(webPageRepository)
                .save(any(WebPage.class));

        var webPageStoreResponse = archiveStore.storeWebPage(webPageStoreRequest);

        then(categoriesRepository).should(times(1)).findByAccountId(any(Account.AccountId.class));
        then(webPageRepository).should(times(1)).save(webPageArgumentCaptor.capture());

        var savedWebPage = webPageArgumentCaptor.getValue();
        assertThat(webPageStoreResponse).isNotNull();
        assertThat(savedWebPage).isNotNull();
        assertThat(savedWebPage.getId().value().toString()).isEqualTo(webPageStoreResponse.storedWebPageId());
        assertThat(savedWebPage.getParentId().value().toString()).isEqualTo(usedMapId);
        assertThat(savedWebPage.getTitle()).isEqualTo(usedTitle);
        assertThat(savedWebPage.getUrl()).isEqualTo(usedUri);
        assertThat(savedWebPage.getDescription()).isEqualTo(usedDescription);
        assertThat(savedWebPage.getCategories().size()).isEqualTo(usedCategories.size());
        assertThat(savedWebPage.getTags().getNames().size()).isEqualTo(usedTags.size());
        assertThat(savedWebPage.getDeleted()).isEqualTo(Trash.Delete.notScheduled());
    }

}