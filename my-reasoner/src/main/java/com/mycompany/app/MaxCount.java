package com.mycompany.app;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.reasoner.rulesys.RuleContext;
import org.apache.jena.reasoner.rulesys.builtins.BaseBuiltin;


import org.apache.jena.graph.Node;
import org.apache.jena.reasoner.rulesys.builtins.CountLiteralValues;
import org.apache.jena.sparql.util.NodeFactoryExtra;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MaxCount extends BaseBuiltin {
    public static HashMap<Node, Boolean> nodesVisited = new HashMap<Node, Boolean>();
    public static HashMap<Node, Integer> nodesCount;


    @Override
    public String getName() {
        return "MaxCount";
    }

    @Override
    public int getArgLength() {
        return 3;
    }

    @Override
    public void headAction(Node[] args, int length, RuleContext context) {

        doUserRequiredAction(args, length, context);
    }

    @Override
    public boolean bodyCall(Node[] args, int length, RuleContext context) {
        return doUserRequiredAction(args, length, context);
    }


    private boolean doUserRequiredAction(Node[] args,
                                         int length, RuleContext context) {

        Graph graph = context.getGraph().getRawGraph();
        checkArgs(length, context);
        if (!nodesVisited.containsKey(args[2])) {
            nodesVisited.put(args[2], true);
        }

        ExtendedIterator<Triple> relativeNodes = graph.find(
                NodeFactory.createVariable("?sub"),
                NodeFactoryExtra.parseNode("<http://purl.org/ontology/musico/displayed_mood>"),
                args[2]);

        for (ExtendedIterator<Triple> it = relativeNodes; it.hasNext(); ) {
            Triple triple = it.next();
            Node musParticipation = triple.getSubject();
            ExtendedIterator<Triple> event = graph.find(musParticipation,
                    NodeFactoryExtra.parseNode("<http://purl.org/ontology/musico/involved_event>"),
                    NodeFactory.createVariable("?event "));
            while (event.hasNext()) {
                Node ev = event.removeNext().getObject();
                ExtendedIterator<Triple> sl = graph.find(
                        NodeFactory.createVariable("?sl"),
                        NodeFactoryExtra.parseNode("owl:sameAs"),
                        ev
                );
                while (sl.hasNext()) {
                    System.out.println(sl.next());
                }
            }
        }

        return true;

    }
}