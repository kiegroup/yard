import * as React from 'react';
import '@patternfly/react-core/dist/styles/base.css';
import { BrowserRouter as Router } from 'react-router-dom';
import { AppLayout } from '@app/AppLayout/AppLayout';
import { AppRoutes } from '@app/routes';
import '@app/app.css';

interface IMyEditorContext {
  yaml: string;
  setYaml?: (content: string) => void;
}

const defaultState = {
  yaml: "hello: world from context",
};

export const MyEditorContext = React.createContext<IMyEditorContext>(defaultState);

const App: React.FunctionComponent = () => {
  const [yaml, setYaml] = React.useState(defaultState.yaml);
  return (
  <MyEditorContext.Provider value={{yaml, setYaml}}>
  <Router>
    <AppLayout>
      <AppRoutes />
    </AppLayout>
  </Router>
  </MyEditorContext.Provider>
)};

export default App;
