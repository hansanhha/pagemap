class ShortcutDto {
    id;
    logo;
    title;
    url;
    order;

    constructor(Shortcut) {
        this.id = Shortcut.id;
        this.logo = null;
        this.title = Shortcut.title;
        this.url = Shortcut.url;
        this.order = 0;
    }
}

export default ShortcutDto;