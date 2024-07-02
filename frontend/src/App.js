import RootRoute from "./routes/RootRoute";
import {BrowserRouter as Router} from "react-router-dom";
import {AppContext} from "./service/AppContext";

function App() {
  return (
      <AppContext>
          <Router>
            <RootRoute />
          </Router>
      </AppContext>
  );
}


export default App;
