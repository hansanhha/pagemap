import {createGlobalStyle} from "styled-components";
import reset from "./reset";
import init from "./init";

const GlobalStyle = createGlobalStyle`
    ${reset}
    ${init}
`;

export default GlobalStyle;