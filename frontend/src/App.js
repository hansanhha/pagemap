import Router from "./routes/Router";
import {LoginProvider} from "./hooks/useLogin";

function App() {
    return (
        <LoginProvider>
            <Router/>
        </LoginProvider>
    );
}


export default App;
