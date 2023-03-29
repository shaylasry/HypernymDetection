package org.example;

import java.util.*;


public class Graph {
    HashMap<Integer ,Node> nodes = new HashMap<>();
    int len = 0;
    String path = "";

    public Graph(){
    };

    public void addNode (Integer index, Node node) {
        nodes.put(index, node);
    }

    public HashMap<Integer, Node> getNodes() {
        return nodes;
    }

    public String shortestPath (int indexWord1, int indexWord2) {
        this.path = "";
        this.len = Integer.MAX_VALUE;
        Node source = this.nodes.get(indexWord1);
        Node target = this.nodes.get(indexWord2);

        if (target == source) {
            return "noPath";
        }
        HashSet<Node> visited = new HashSet<>();
        helper(true, source, target, visited, 0 , "");
        if (this.path.equals("")) {
            return "noPath";
        }
        return this.path;
    }

    public void helper (boolean isFirst, Node curNode, Node target, HashSet visited, int curLen, String curPath) {

        HashMap<Node, String> neighbors = curNode.getNeighbors();
        Set<Node> neighborsSet = neighbors.keySet();
        for (Node neighbor : neighborsSet) {
            if (visited.contains(neighbor)) {
                continue;
            }
            if (neighbor == target && curLen <= this.len) {
                visited.add(neighbor);
                this.path = curPath + neighbors.get(neighbor);
                this.len = curLen;
                return;
            }
        }
        for (Node neighbor : neighborsSet) {
            if(!visited.contains(neighbor)) {
                visited.add(neighbor);

                String newPath = curPath;
                if(!isFirst) {
                    newPath += curNode.getLabel() + "/";
                }
                newPath += neighbors.get(neighbor) + "/" + neighbor.getLabel() + "/";
                helper(false, neighbor, target, visited, curLen + 1, newPath);
            }
        }



    }





}

