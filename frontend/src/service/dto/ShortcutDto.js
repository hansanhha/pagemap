class ShortcutDto {
    id;
    logo;
    name;
    uri;
    order;

    constructor(Shortcut) {
        this.id = Shortcut.id;
        this.logo = null;
        this.name = Shortcut.name;
        this.uri = Shortcut.uri;
        this.order = 0;
    }

    static isShortcut(archive) {
        return archive instanceof ShortcutDto;
    }
}

export default ShortcutDto;