package com.bintage.pagemap.storage.domain.model.category;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.domain.model.map.Map;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jmolecules.ddd.types.Entity;
import org.jmolecules.ddd.types.Identifier;

import java.util.List;
import java.util.Set;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class Category implements Entity<Map, Category.CategoryId> {

    private static final String DEFAULT_BG_COLOR = "red";
    private static final String DEFAULT_FONT_COLOR = "white";

    private final CategoryId id;
    private final Account.AccountId accountId;
    private String name;
    private String bgColor;
    private String fontColor;

    public record CategoryId(Long value) implements Identifier {}

    public static Category toCategory(CategoryId id, Account.AccountId accountId, String name, String color, String fontColor) {
        return new Category(id, accountId, name, color, fontColor);
    }

    public static Category create(Account.AccountId accountId, String name, String color, String fontColor) {
        return new Category(null, accountId, name, color, fontColor);
    }

    public static Category create(Account.AccountId accountId, String name) {
        return create(accountId, name, DEFAULT_BG_COLOR, DEFAULT_FONT_COLOR);
    }

    public void update(String name, String bgColor, String fontColor) {
        if (name == null || name.isBlank()) {
            throw CategoryException.invalidName(accountId, name);
        }

        this.name = name;

        if (bgColor == null || bgColor.isBlank()) {
            this.bgColor = DEFAULT_BG_COLOR;
        } else {
            this.bgColor = bgColor;
        }

        if (fontColor == null || fontColor.isBlank()) {
            this.fontColor = DEFAULT_FONT_COLOR;
        } else {
            this.fontColor = fontColor;
        }
    }
}
