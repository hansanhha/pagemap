class ShortcutDto {
    id;
    logo;
    title;
    url;
    order;

    constructor(webPage) {
        this.id = webPage.id;
        this.logo = null;
        this.title = webPage.title;
        this.url = webPage.url;
        this.order = 0;
    }
}

export default ShortcutDto;