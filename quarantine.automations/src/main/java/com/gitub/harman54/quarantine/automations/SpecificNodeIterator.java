package com.gitub.harman54.quarantine.automations;

import com.github.javaparser.ast.Node;

public class SpecificNodeIterator<T> {
	private Class<T> type;
	private NodeHandler<T> nodeHandler;
	
    interface NodeHandler<T> {
        public Boolean handle(T node);
    }
    
    public void explore(Node node) {
        if (type.isInstance(node)) {
            if (!nodeHandler.handle(type.cast(node))) {
                return;
            }
        }
        for (Node child : node.getChildNodes()) {
            explore(child);
        }
    }

}
