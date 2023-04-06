const express = require('express');
const SparqlClient = require('sparql-http-client');
const http = require('http');
const app = express();
const port = 3002
const jenaFusekiServer = 'http://localhost:3030/';


app.get('/sendQuery', async(req, res) =>  {
  // const query = 'PREFIX%20owl%3A%20%3Chttp%3A%2F%2Fwww.w3.org%2F2002%2F07%2Fowl%23%3E%0APREFIX%20foaf%3A%20%3Chttp%3A%2F%2Fxmlns.com%2Ffoaf%2F0.1%2F%3E%0APREFIX%20rdf%3A%20%3Chttp%3A%2F%2Fwww.w3.org%2F1999%2F02%2F22-rdf-syntax-ns%23%3E%0APREFIX%20rdfs%3A%20%3Chttp%3A%2F%2Fwww.w3.org%2F2000%2F01%2Frdf-schema%23%3E%0APREFIX%20musico%3A%20%3Chttp%3A%2F%2Fpurl.org%2Fontology%2Fmusico%23%3E%0APREFIX%20smi%3A%20%3Chttp%3A%2F%2Fpurl.org%2Fontology%2Fiomust%2Fsmi%3E%0APREFIX%20mo%3A%20%3Chttp%3A%2F%2Fpurl.org%2Fontology%2Fmo%2F%3E%0APREFIX%20iomust%3A%20%3Chttp%3A%2F%2Fpurl.org%2Fontology%2Fiomust%2Finternet_of_things%3E%0APREFIX%20ex%3A%20%3Chttp%3A%2F%2Fwww.example.audio%2F%3E%0ASELECT%20DISTINCT%20%3Fentity1%20%3Fpred%20%3Fobj%0AWHERE%20%7B%0A%20%20%3Fentity1%20%3Fpred%20%3Fobj%3B%0A%20%20%20%20%20%20%20%20%20%20%20musico%3Aplays_genre%20ex%3APop%20%3B%0A%20%20%20%20%20%20%20%20%20%20%20foaf%3AfirstName%20%22Atai%22%20.%0A%7D%20%0A%0AORDER%20BY%20%3Finstrument%0ALIMIT%20100';
  // const options = {
  //   hostname: 'localhost',
  //   port: 3030,
  //   path: '/Musico/sparql' ,
  //   method: 'POST',
  //   body: {
  //     payload: query,
  //   },
  //   headers: {
  //     'Accept': 'application/sparql-results+json',
  //     'Connection': 'keep-alive',
  //     'Content-Type': 'application/x-www-form-urlencoded',
  //     'Content-Length': query.length
  //     },
  //   }
  //
  // const request = http.request(options, (response) => {
  //   console.log(`STATUS: ${response.statusCode}`);
  //   console.log(`HEADERS: ${JSON.stringify(response.headers)}`);
  //   response.setEncoding('utf8');
  //   response.on('data', (chunk) => {
  //     console.log(`BODY: ${chunk}`);
  //     res.send(chunk);
  //   });
  //   });
  // request.on('error', (e) => {
  //   console.log(`problem with request: ${e.message}`);
  //   res.send(e.message);
  // });
  // request.end();

  const client = new SparqlClient({ endpointUrl: jenaFusekiServer + 'Musico/sparql' });
  const query = `PREFIX owl: <http://www.w3.org/2002/07/owl#>
  PREFIX foaf: <http://xmlns.com/foaf/0.1/>
  PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
  PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
  PREFIX musico: <http://purl.org/ontology/musico#>
  PREFIX smi: <http://purl.org/ontology/iomust/smi>
  PREFIX mo: <http://purl.org/ontology/mo/>
  PREFIX iomust: <http://purl.org/ontology/iomust/internet_of_things>
  PREFIX ex: <http://www.example.audio/>

  SELECT DISTINCT ?entity1 ?pred ?obj
  WHERE {
    ?entity1 ?pred ?obj;
      musico:plays_genre ex:Pop ;
      foaf:firstName "Atai" .
  }
  ORDER BY ?instrument
  LIMIT 100
`;
  let dataToSend = '';
  const stream = await  client.query.select(query)
  stream.on('data', (data) => {
    Object.keys(data).forEach((key) => {
      dataToSend += `${key}: ${data[key].value} `;
    });
    console.log('---');
  });
  stream.on('end', () => {
    res.send(dataToSend);
    console.log('Done');
  }


  );
  });


app.get('/', (req, res) => {
  res.send('Hello World!')
})

app.listen(port, () => {
  console.log(`Example app listening on port ${port}`)
})

