package net.limit.cubliminal.world.placement;

import com.google.common.collect.SetMultimap;
import io.github.jdiemke.triangulation.Edge2D;
import io.github.jdiemke.triangulation.Triangle2D;
import io.github.jdiemke.triangulation.Vector2D;
import net.limit.cubliminal.world.room.Door.Instance;
import net.ludocrypt.limlib.api.world.maze.MazeComponent.Vec2i;
import net.minecraft.util.math.random.Random;

import java.util.*;

public class MSTree {

    public static List<Edge2D> buildCorridors(Collection<Vector2D> nodes, SetMultimap<Vec2i, Instance> doors,
                                              List<Triangle2D> triangleSoup, List<Vector2D> connections, Random random) {
        // Build unique edges
        List<Edge2D> edges = new ArrayList<>();
        for (Triangle2D triangle : triangleSoup) {
            Vector2D a = triangle.a;
            Vector2D b = triangle.b;
            Vector2D c = triangle.c;
            addEdge(a, b, edges, doors, connections, random);
            addEdge(b, c, edges, doors, connections, random);
            addEdge(c, a, edges, doors, connections, random);
        }
        // Sort edges by length
        edges.sort(Comparator.comparingDouble(MSTree::sqEdgeLength));

        List<Edge2D> mst = new ArrayList<>(nodes.size() - 1);
        UnionFind uf = new UnionFind(nodes);
        // Iterate from the shortest to the longest edges
        for (Edge2D edge : edges) {
            Vector2D u = edge.a;
            Vector2D v = edge.b;
            // Connect both sets if their root is different to form no loops
            if (!uf.find(u).equals(uf.find(v))) {
                mst.add(edge);
                uf.union(u, v);
            }
        }

        return mst;
    }

    public static void addEdge(Vector2D a, Vector2D b, List<Edge2D> edges, SetMultimap<Vec2i, Instance> doors,
                               List<Vector2D> connections, Random random) {
        Instance doorA = doors.get(new Vec2i((int) a.x, (int) a.y)).stream().toList().getFirst();
        Instance doorB = doors.get(new Vec2i((int) b.x, (int) b.y)).stream().toList().getFirst();
        if (!doorA.roomPos().equals(doorB.roomPos())) {
            add(a, b, edges);
        } else {
            add(a, connections.get(random.nextInt(connections.size())), edges);
            add(b, connections.get(random.nextInt(connections.size())), edges);
        }
    }

    public static void add(Vector2D a, Vector2D b, List<Edge2D> edges) {
        if (!edges.contains(new Edge2D(a, b))) {
            edges.add(new Edge2D(a, b));
        } else if (!edges.contains(new Edge2D(b, a))) {
            edges.add(new Edge2D(b, a));
        }
    }

    public static double sqEdgeLength(Edge2D edge) {
        Vector2D dif = edge.a.sub(edge.b);
        return dif.x * dif.x + dif.y * dif.y;
    }

    private static class UnionFind {

        private final Map<Vector2D, Vector2D> parent;

        public UnionFind(Collection<Vector2D> nodes) {
            this.parent = new HashMap<>(nodes.size());
            for (Vector2D vec : nodes) {
                this.parent.put(vec, vec);
            }
        }

        public Vector2D find(Vector2D vec) {
            Vector2D root = this.parent.get(vec);
            if (!vec.equals(root)) {
                root = this.find(root);
                this.parent.put(vec, root);
            }
            return root;
        }

        public void union(Vector2D a, Vector2D b) {
            Vector2D rA = this.find(a);
            Vector2D rB = this.find(b);
            if (!rA.equals(rB)) {
                this.parent.put(rA, rB);
            }
        }
    }
}
