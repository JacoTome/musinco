const express = require("express");
const SparqlClient = require("sparql-http-client");
const http = require("http");
const fs = require("fs");
const cors = require("cors");
const app = express();
const port = 3002;
const jenaFusekiServer = "http://204.216.223.231:3030";

const client = new SparqlClient({
  endpointUrl: jenaFusekiServer + "/musico",
  updateUrl: jenaFusekiServer + "/musico/update",
});
// Read the contents of the query file
const SPquery = fs.readFileSync("./queries/query1.sparql", "utf8");
const SPquery2 = fs.readFileSync("./queries/query2.sparql", "utf8");
const SPquery3 = fs.readFileSync("./queries/query3.sparql", "utf8");
// Use the query variable in your SPARQL client code

app.use(cors());

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

// Route to add a user to the database
app.get("/add", async (req, res) => {
  const query = `INSERT DATA { <http://example.org/John> <http://example.org/age> 30 . }`;
  res.send(await insertData(query));
});

app.listen(port, () => console.log(`Example app listening on port ${port}`));
