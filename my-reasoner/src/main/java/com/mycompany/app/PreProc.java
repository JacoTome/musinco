package com.mycompany.app;

import org.apache.jena.graph.*;
import org.apache.jena.reasoner.Finder;
import org.apache.jena.reasoner.TriplePattern;
import org.apache.jena.reasoner.rulesys.FBRuleInfGraph;
import org.apache.jena.reasoner.rulesys.RulePreprocessHook;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.util.NodeFactoryExtra;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.util.List;

public class PreProc implements RulePreprocessHook {
    @Override
    public boolean needsRerun(FBRuleInfGraph fbRuleInfGraph, Triple triple) {
        return false;
    }

    @Override
    public void run(FBRuleInfGraph fbRuleInfGraph, Finder finder, Graph graph) {
        PrefixMapping mapping = graph.getPrefixMapping();
        String musico = "<http://purl.org/ontology/musico/";
        Node p = NodeFactory.createVariable("?p");
        Node type = NodeFactoryExtra.parseNode("rdf:type");
        Node day = NodeFactoryExtra.parseNode("time:day");
        Node hour = NodeFactoryExtra.parseNode("time:hour");

        Node sameAs = NodeFactoryExtra.parseNode("owl:sameAs");
        Node sl = NodeFactoryExtra.parseNode(musico + "SelfLearning>");

        List<Triple> iterat = finder.find(new TriplePattern(p, type, sl)).toList();

        for (Triple triple :
                iterat) {
            System.out.println(triple);
        }


    }
}