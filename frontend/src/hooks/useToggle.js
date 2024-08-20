import {useState} from "react";

const useToggle = (initialValue = false) => {
    const [value, setValue] = useState(initialValue);

    const toggle = () => {
        setValue(!value);
    }

    const setTrue = () => {
        setValue(true);
    }

    const setFalse = () => {
        setValue(false);
    }

    return [value, setTrue, setFalse];
}

export default useToggle;