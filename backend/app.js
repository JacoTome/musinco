const express = require("express");
const SparqlClient = require("sparql-http-client");
const http = require("http");
const fs = require("fs");
const cors = require("cors");
const app = express();
const port = 3002;
const jenaFusekiServer = "http://204.216.223.231:3030";
const graphDBServer = "http://localhost:7200/repositories/musinco";

const client = new SparqlClient({
  endpointUrl: jenaFusekiServer + "/musico",
  updateUrl: jenaFusekiServer + "/musico/update",
});

const GDBclient = new SparqlClient({
  endpointUrl: graphDBServer,
});

// Read the contents of the query file
const SPquery = fs.readFileSync("./queries/query1.sparql", "utf8");
const SPquery2 = fs.readFileSync("./queries/query2.sparql", "utf8");
const SPquery3 = fs.readFileSync("./queries/query3.sparql", "utf8");
// Use the query variable in your SPARQL client code

app.use(cors());

async function queryGDB(query) {
  try {
    const result = await GDBclient.query.select(query);

    // Stampa i risultati
    result.bindingsStream.on("data", (row) => {
      console.log(row.toObject());
    });

    // Gestisci eventuali errori
    result.bindingsStream.on("error", (error) => {
      console.error(error);
    });

    // Gestisci la chiusura della query
    result.bindingsStream.on("end", () => {
      console.log("Query completata");
    });
  } catch (error) {
    console.error(error);
  }
}

async function queryData(query) {
  const stream = await client.query.select(query);
  return new Promise((resolve, reject) => {
    var data = [];
    stream.on("data", (row) => {
      data.push(row);
    });
    stream.on("end", () => {
      resolve(data);
    });
    stream.on("error", (err) => {
      reject(err);
    });
  });
}

async function insertData(query) {
  await client.query.update(query);
}

app.get("/", (req, res) => {
  // Redirect to the query page 1
  res.redirect("/1");
});
app.get("/1", async (req, res) => {
  res.send(await queryData(SPquery));
});

app.get("/2", async (req, res) => {
  res.send(await queryData(SPquery2));
});

app.get("/3", async (req, res) => {
  res.send(await queryData(SPquery3));
});

app.get("/4", async (req, res) => {
  const query = `select * where { ?s ?p ?o } limit 10`;
  const encodedQuery = encodeURIComponent(query);
  const response = await http.get(
    graphDBServer + "?query=" + encodedQuery,
    {
      headers: {
        Accept: "application/sparql-results+json",
      },
    },
    (resp) => {
      try {
        let data = "";
        resp.on("data", (chunk) => {
          data += chunk;
        });
        resp.on("end", () => {
          res.send(JSON.parse(data));
        });
      } catch (err) {
        console.log(err);
      }
    }
  );

  console.log(response);
});

app.get("/5", async (req, res) => {
  res.send(await queryGDB("select * where { ?s ?p ?o } limit 10"));
});

// Route to add a user to the database
app.get("/add", async (req, res) => {
  const query = `INSERT DATA { <http://example.org/John> <http://example.org/age> 30 . }`;
  res.send(await insertData(query));
});

app.listen(port, () => console.log(`Example app listening on port ${port}`));
