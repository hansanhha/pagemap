import {useState} from "react";

export const useLocalStorage = (keyName, defaultValue) => {
    const [value, setValue] = useState(() => {
        try {
            const value = window.localStorage.getItem(keyName);

            if (value) {
                return JSON.parse(value);
            }

            window.localStorage.setItem(keyName, JSON.stringify(defaultValue));
            return defaultValue;
        } catch (err) {
            return defaultValue;
        }
    });

    const setNewValue = (value) => {
        try {
            window.localStorage.setItem(keyName, JSON.stringify(value));
        } catch (err) {
        }

        setValue(value);
    };

    return [value, setNewValue];
}