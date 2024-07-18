export class CategoryDto {
    id;
    name;
    backgroundColor;
    fontColor;

    constructor(id, name, backgroundColor, fontColor) {
        this.id = id;
        this.name = name;
        this.backgroundColor = backgroundColor;
        this.fontColor = fontColor;
    }
}