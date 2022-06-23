import React from 'react';
import '@patternfly/react-core/dist/styles/base.css';
import { TextArea, Button } from '@patternfly/react-core';
import { Grid, GridItem, Stack, StackItem } from '@patternfly/react-core';
import { Card, CardTitle, CardBody, CardFooter } from '@patternfly/react-core';
import { Panel, PanelMain, PanelMainBody, PageSection, PageSectionVariants } from '@patternfly/react-core';
import { CodeBlock, CodeBlockCode } from '@patternfly/react-core';
import { MyEditorContext } from '@app/index';

import Ajv from 'ajv';
const metaSchemaDraft04 = require("ajv/lib/refs/json-schema-draft-04.json");
import { JSONSchemaBridge } from 'uniforms-bridge-json-schema';
import { AutoForm } from "uniforms-patternfly/dist/es6";

let ajv = new Ajv({
  schemaId: "auto",
  meta: false, // optional, to prevent adding draft-06 meta-schema
  extendRefs: true, // optional, current default is to 'fail', spec behaviour is to 'ignore'
  unknownFormats: 'ignore',  // optional, current default is true (fail)
  // ...
});
ajv.addMetaSchema(metaSchemaDraft04);
function createValidator(schema: object) {
  const validator = ajv.compile(schema);

  return (model: object) => {
    validator(model);
    return validator.errors?.length ? { details: validator.errors } : null;
  };
}
const SCHEMA_DRAFT4 = "http://json-schema.org/draft-04/schema#";
const emptySchema = {
  $schema: SCHEMA_DRAFT4,
  type: 'object'
};
const emptyBridge = new JSONSchemaBridge(emptySchema, createValidator(emptySchema));

const SimpleTextArea = () => {
  const { yaml, setYaml } = React.useContext(MyEditorContext);
  const [response, setResponse] = React.useState({});
  const [bridge, setBridge] = React.useState(emptyBridge);
  const [requestPayload, setRequestPayload] = React.useState({});
  const [responsePayload, setResponsePayload] = React.useState({});
  function setPayload(payload: any) {
    console.log(payload);
    setRequestPayload({"a":1});
    setRequestPayload(payload);
  }
  function fetchSchemaForm(modelXML: string) {
    const other_params: RequestInit = {
      headers: {
        'Content-Type': 'application/x-yml',
        'Accept': 'application/json'
      }, 
      body: modelXML, 
      method: "POST",
      mode: "cors" 
    };
    fetch("http://localhost:8080/jitdmn/schema/form/", other_params)
    .then(function(response) {
      console.log(response);
      return response.json();
    }).then((data) => {
      console.log(data);
      setRequestPayload({});
      setResponse(data);
      const schemaValidator = createValidator(data);
      setBridge(new JSONSchemaBridge(data, schemaValidator));
    });
  }

  function submitFormUsingRequestPayload() {
    const formData = {"model": yaml, "context": requestPayload};
    const other_params: RequestInit = {
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      }, 
      body: JSON.stringify(formData), 
      method: "POST",
      mode: "cors" 
    };
    fetch("http://localhost:8080/jitdmn/yml/", other_params)
    .then(function(response) {
      console.log(response);
      return response.json();
    }).then((data) => {
      console.log(data);
      setResponsePayload(data);
    });
  }


  return (
    <PageSection variant={PageSectionVariants.light}>
    <Stack hasGutter>
    <StackItem>

      {/* <TextArea readOnly={true} value={yaml} onChange={setYaml} aria-label="text area yaml" /> */}
      <Button variant="secondary" isSmall onClick={() => fetchSchemaForm(yaml)}><i className="bi bi-arrow-clockwise"></i> Deploy</Button>



    </StackItem>
    <StackItem isFilled>

    <Grid hasGutter>
    <GridItem span={6}>

    {/* <TextArea readOnly={true} value={JSON.stringify(response)} aria-label="text area response" /> */}
    <Card>
      <CardTitle>Input values</CardTitle>
      <CardBody><AutoForm model={requestPayload} onChangeModel={setPayload} schema={bridge} onSubmit={submitFormUsingRequestPayload} /></CardBody>
      {/* <CardFooter>Footer</CardFooter> */}
    </Card>

    </GridItem>
    <GridItem span={6}>
      
    <Card>
      <CardTitle>Request</CardTitle>
      <CardBody>
      <CodeBlock>
        <CodeBlockCode>{JSON.stringify(requestPayload)}</CodeBlockCode>
      </CodeBlock>
      </CardBody>
    </Card>
    &nbsp;
    <Card>
      <CardTitle>Response</CardTitle>
      <CardBody>
      <CodeBlock>
        <CodeBlockCode>{JSON.stringify(responsePayload, null, 2)}</CodeBlockCode>
      </CodeBlock>
      </CardBody>
    </Card>
    </GridItem>
    </Grid>
    &nbsp;
    </StackItem>
    </Stack>
    </PageSection>
  );
}

export { SimpleTextArea }