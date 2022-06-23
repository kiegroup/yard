import '@patternfly/react-core/dist/styles/base.css';
import React from 'react';
import { CodeEditor, Language } from '@patternfly/react-code-editor';
import { MyEditorContext } from '@app/index';


const Editor = () => {
  const { yaml, setYaml } = React.useContext(MyEditorContext);
  const onEditorDidMount = (editor, monaco) => {
    console.log(editor.getValue());
    editor.layout();
    editor.focus();
    monaco.editor.getModels()[0].updateOptions({ tabSize: 5 });
  };
  return <CodeEditor
        isDarkTheme={true}
        isLineNumbersVisible={false}
        isReadOnly={false}
        isMinimapVisible={false}
        isLanguageLabelVisible
        code={yaml}
        onChange={setYaml}
        language={Language.yaml}
        onEditorDidMount={onEditorDidMount}
        height='600px'
      />
  ;
}

export { Editor };