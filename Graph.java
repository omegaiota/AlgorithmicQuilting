package jackiequiltpatterndeterminaiton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by JacquelineLi on 6/22/17.
 */
public class Graph {
    private ArrayList<Vertex<Point>> mVertices;
    private int V = 0,E = 0;
    private double disLen = 0;

    public Graph(double disLen) {
        mVertices = new ArrayList<>();
    }

    /**
     * Perturbation
     */
    public void pointPerturb() {
        for (Vertex<Point> p : mVertices) {
            p.setData(jackiequiltpatterndeterminaiton.Point.randomNearby(p.getData(), disLen * 0.05));
        }
    }


    /**
     * Generate a spanning tree by taking the first vertex as root and traverse it
     *
     * @return
     */
    public TreeNode<Point> generateSpanningTree() {
        pointPerturb();
        boolean flag = true;
        HashSet<Vertex<Point>> toInclude = new HashSet<>();
        HashMap<Vertex<Point>, TreeNode<Point>> vertexTreeNodeHashMap = new HashMap<>();
        TreeNode<Point> root = new TreeNode<>(mVertices.get(0).getData(), mVertices.get(0).getNeighbors());
        vertexTreeNodeHashMap.put(mVertices.get(0), root);
        toInclude.addAll(mVertices);
        toInclude.remove(mVertices.get(0));

        while (!toInclude.isEmpty() && flag) {
            //System.out.println("Remaining:" + toInclude.size());
         int minCost = 10000;
            Vertex<Point> minVertex = null, parentVertex = null;
            for (Vertex<Point> key : vertexTreeNodeHashMap.keySet()) {
             assert(toInclude.contains(key) == false);
             for (int i = 0; i < key.neighborSize(); i++) {
                 if ((key.getWeight().get(i) < minCost) && toInclude.contains(key.getNeighbors().get(i))) {
                     minCost = key.getWeight().get(i);
                     minVertex = key.getNeighbors().get(i);
                     parentVertex = key;
                 }
             }
         }
         assert(toInclude.size() + vertexTreeNodeHashMap.size() == mVertices.size());
         /* Some vertices might not be connected  */
         if (minVertex != null) {
             TreeNode<Point> newNode = new TreeNode<>(minVertex.getData(), minVertex.getNeighbors());
             newNode.setParent(vertexTreeNodeHashMap.get(parentVertex));
             vertexTreeNodeHashMap.get(parentVertex).addChild(newNode);
             vertexTreeNodeHashMap.put(minVertex, newNode);
             toInclude.remove(minVertex);
         } else {
             flag = false;
         }
        }
        return root;
    }

    public void addVertex(Vertex<Point> vertex) {
        mVertices.add(vertex);
        V++;
    }


    public ArrayList<Vertex<Point>> getVertices() {
        return mVertices;
    }

    public int getV() {
        return V;
    }

    public int getE() {
        return E;
    }
}
