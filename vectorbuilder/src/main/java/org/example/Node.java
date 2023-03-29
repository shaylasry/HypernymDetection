package org.example;

import java.util.HashMap;

public class Node {
        String label;
        //only going out negihbors
        HashMap<Node, String> neighbors = new HashMap<>();

        public Node (String label) {
            this.label = label;
        }

        public void addNeighbor (Node neighbor, String edge) {
            this.neighbors.put(neighbor, edge);
        }

    public String getLabel() {
        return label;
    }

    public HashMap<Node, String> getNeighbors() {
        return neighbors;
    }
}
