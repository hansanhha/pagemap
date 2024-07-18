import {UserService} from "./UserService";
import {CategoryDto} from "./dto/CategoryDto";

export class CategoryService {

    static async getAllCategories() {
        return fetch(`${process.env.REACT_APP_SERVER}/storage/categories`, {
            method: "GET",
            headers: {
                "Content-Type": "application/problem+json",
                "Authorization": "Bearer " + UserService.getToken()
            },
        })
            .then(response => response.json())
            .then(data => {
                return data.categories.map(category => new CategoryDto(category.id, category.name, category.bgColor, category.fontColor));
            })
            .catch(error => console.log(error));
    }

    static createCategory(name, backgroundColor, fontColor) {
        return fetch(`${process.env.REACT_APP_SERVER}/storage/categories`, {
            method: "POST",
            headers: {
                "Content-Type": "application/problem+json",
                "Authorization": "Bearer " + UserService.getToken()
            },
            body: JSON.stringify({
                name: name,
                bgColor: backgroundColor,
                fontColor: fontColor
            })
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                return response.json();
            })
            .catch(error => console.log(error));
    }

    static updateCategory(id, name, backgroundColor, fontColor) {
        return fetch(`${process.env.REACT_APP_SERVER}/storage/categories/${id}`, {
            method: "PUT",
            headers: {
                "Content-Type": "application/problem+json",
                "Authorization": "Bearer " + UserService.getToken()
            },
            body: JSON.stringify({
                name: name,
                bgColor: backgroundColor,
                fontColor: fontColor
            })
        }).then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json();
        }).catch(error => console.log(error));
    }

    static deleteCategory(id) {
        return fetch(`${process.env.REACT_APP_SERVER}/storage/categories/${id}`, {
            method: "DELETE",
            headers: {
                "Content-Type": "application/problem+json",
                "Authorization": "Bearer " + UserService.getToken()
            },
        }).then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json();
        }).catch(error => console.log(error));
    }
}