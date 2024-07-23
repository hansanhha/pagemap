import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';
import GlobalStyle from "./style/GlobalStyle";
import BrowserContextMenuBlock from "./layout/BrowserContextMenuBlock";

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
    <React.Fragment>
        <GlobalStyle/>
        <BrowserContextMenuBlock>
            <App/>
        </BrowserContextMenuBlock>
    </React.Fragment>
);


