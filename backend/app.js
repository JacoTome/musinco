const express = require("express");
const SparqlClient = require("sparql-http-client");
const http = require("http");
const fs = require("fs");
const app = express();
const port = 3002;
const jenaFusekiServer = "http://localhost:3030/";

const client = new SparqlClient({
  endpointUrl: jenaFusekiServer + "Musico/sparql",
});
// Read the contents of the query file
const SPquery = fs.readFileSync("./queries/query1.sparql", "utf8");
const SPquery2 = fs.readFileSync("./queries/query2.sparql", "utf8");
// Use the query variable in your SPARQL client code

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

app.listen(port, () => console.log(`Example app listening on port ${port}`));
