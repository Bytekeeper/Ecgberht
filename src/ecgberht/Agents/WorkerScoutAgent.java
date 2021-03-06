package ecgberht.Agents;

import bwem.Base;
import bwem.area.Area;
import ecgberht.BuildingMap;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.unit.SCV;
import org.openbw.bwapi4j.unit.Unit;

import java.util.ArrayList;
import java.util.List;

import static ecgberht.Ecgberht.getGs;

// Based on SteamHammer worker scout management, props to @JayScott
public class WorkerScoutAgent {
    private SCV unit;
    private int currentVertex;
    private List<Position> enemyBaseBorders = new ArrayList<>();
    private Base enemyBase;

    public WorkerScoutAgent(Unit unit, Base enemyBase) {
        this.unit = (SCV) unit;
        this.enemyBase = enemyBase;
    }

    public boolean runAgent() {
        if (unit == null || !unit.exists()) return true;
        if (enemyBaseBorders.isEmpty()) {
            updateBorders();
        }
        followPerimeter();
        return false;
    }

    private Position getFleePosition() {

        // if this is the first flee, we will not have a previous perimeter index
        if (currentVertex == -1) {
            // so return the closest position in the polygon
            int closestPolygonIndex = getClosestVertexIndex();

            if (closestPolygonIndex == -1) {
                return getGs().getPlayer().getStartLocation().toPosition();
            }

            // set the current index so we know how to iterate if we are still fleeing later
            currentVertex = closestPolygonIndex;
            return enemyBaseBorders.get(closestPolygonIndex);
        }

        // if we are still fleeing from the previous frame, get the next location if we are close enough
        double distanceFromCurrentVertex = enemyBaseBorders.get(currentVertex).getDistance(unit.getPosition());

        // keep going to the next vertex in the perimeter until we get to one we're far enough from to issue another move command
        while (distanceFromCurrentVertex < 128) {
            currentVertex = (currentVertex + 1) % enemyBaseBorders.size();

            distanceFromCurrentVertex = enemyBaseBorders.get(currentVertex).getDistance(unit.getPosition());
        }

        return enemyBaseBorders.get(currentVertex);
    }

    private int getClosestVertexIndex() {
        int chosen = -1;
        double distMax = Double.MAX_VALUE;
        for (int ii = 0; ii < enemyBaseBorders.size(); ii++) {
            double dist = enemyBaseBorders.get(ii).getDistance(unit.getPosition());
            if (dist < distMax) {
                chosen = ii;
                distMax = dist;
            }
        }
        return chosen;
    }

    private void followPerimeter() {
        Position fleeTo = getFleePosition();
        Position lastPos = unit.getOrderTargetPosition();
        if (lastPos != null && lastPos.equals(fleeTo)) return;
        unit.move(fleeTo);
    }

    private void updateBorders() {

        final Area enemyRegion = enemyBase.getArea();

        if (enemyRegion == null) return;


        final Position enemyCenter = enemyBase.getLocation().toPosition().add(new Position(64, 48));

        final List<TilePosition> closestTobase = new ArrayList<>(BuildingMap.tilesArea.get(enemyRegion));

        List<Position> unsortedVertices = new ArrayList<>();

        // check each tile position
        for (TilePosition tp : closestTobase) {

            if (getGs().bwem.getMap().getArea(tp) != enemyRegion) continue;

            // a tile is 'on an edge' unless
            // 1) in all 4 directions there's a tile position in the current region
            // 2) in all 4 directions there's a buildable tile

            TilePosition right = new TilePosition(tp.getX() + 1, tp.getY());
            TilePosition bottom = new TilePosition(tp.getX(), tp.getY() + 1);
            TilePosition left = new TilePosition(tp.getX() - 1, tp.getY());
            TilePosition up = new TilePosition(tp.getX(), tp.getY() - 1);
            final boolean edge =
                    (!getGs().getGame().getBWMap().isValidPosition(right) || (getGs().bwem.getMap().getArea(right) != enemyRegion || !getGs().getGame().getBWMap().isBuildable(right)))
                            || (!getGs().getGame().getBWMap().isValidPosition(bottom) || (getGs().bwem.getMap().getArea(bottom) != enemyRegion || !getGs().getGame().getBWMap().isBuildable(bottom)))
                            || (!getGs().getGame().getBWMap().isValidPosition(left) || (getGs().bwem.getMap().getArea(left) != enemyRegion || !getGs().getGame().getBWMap().isBuildable(left)))
                            || (!getGs().getGame().getBWMap().isValidPosition(up) || (getGs().bwem.getMap().getArea(up) != enemyRegion || !getGs().getGame().getBWMap().isBuildable(up)));

            // push the tiles that aren't surrounded
            if (edge && getGs().getGame().getBWMap().isBuildable(tp)) {

                Position vertex = tp.toPosition().add(new Position(16, 16));

                // Pull the vertex towards the enemy base center, unless it is already within 12 tiles
                double dist = enemyCenter.getDistance(vertex);
                if (dist > 384.0) {

                    double pullBy = Math.min(dist - 384.0, 120.0);

                    // Special case where the slope is infinite
                    if (vertex.getX() == enemyCenter.getX()) {
                        vertex = vertex.add(new Position(0, vertex.getY() > enemyCenter.getY() ? (int) (-pullBy) : (int) pullBy));
                    } else {
                        // First get the slope, m = (y1 - y0)/(x1 - x0)
                        double m = (double) (enemyCenter.getY() - vertex.getY()) / (double) (enemyCenter.getX() - vertex.getX());

                        // Now the equation for a new x is x0 +- d/sqrt(1 + m^2)
                        double x = vertex.getX() + (vertex.getX() > enemyCenter.getX() ? -1.0 : 1.0) * pullBy / (Math.sqrt(1 + m * m));

                        // And y is m(x - x0) + y0
                        double y = m * (x - vertex.getX()) + vertex.getY();

                        vertex = new Position((int) x, (int) y);
                    }
                }

                unsortedVertices.add(vertex);
            }
        }

        List<Position> sortedVertices = new ArrayList<>();
        Position current = unsortedVertices.get(0);

        enemyBaseBorders.add(current);
        unsortedVertices.remove(current);

        // while we still have unsorted vertices left, find the closest one remaining to current
        while (!unsortedVertices.isEmpty()) {
            double bestDist = 1000000;
            Position bestPos = null;

            for (final Position pos : unsortedVertices) {
                double dist = pos.getDistance(current);

                if (dist < bestDist) {
                    bestDist = dist;
                    bestPos = pos;
                }
            }

            current = bestPos;
            sortedVertices.add(sortedVertices.size(), bestPos);
            unsortedVertices.remove(bestPos);
        }

        // let's close loops on a threshold, eliminating death grooves
        int distanceThreshold = 100;

        while (true) {
            // find the largest index difference whose distance is less than the threshold
            int maxFarthest = 0;
            int maxFarthestStart = 0;
            int maxFarthestEnd = 0;

            // for each starting vertex
            for (int i = 0; i < sortedVertices.size(); ++i) {
                int farthest = 0;
                int farthestIndex = 0;

                // only test half way around because we'll find the other one on the way back
                for (int j = 1; j < sortedVertices.size() / 2; ++j) {
                    int jindex = (i + j) % sortedVertices.size();

                    if (sortedVertices.get(i).getDistance(sortedVertices.get(jindex)) < distanceThreshold) {
                        farthest = j;
                        farthestIndex = jindex;
                    }
                }
                if (farthest > maxFarthest) {
                    maxFarthest = farthest;
                    maxFarthestStart = i;
                    maxFarthestEnd = farthestIndex;
                }
            }

            // stop when we have no long chains within the threshold
            if (maxFarthest < 4) break;
            List<Position> temp = new ArrayList<>();

            for (int s = maxFarthestEnd; s != maxFarthestStart; s = (s + 1) % sortedVertices.size()) {
                temp.add(temp.size(), sortedVertices.get(s));
            }

            sortedVertices = temp;
        }
        enemyBaseBorders = sortedVertices;
        // Set the initial index to the vertex closest to the enemy main, so we get scouting information as soon as possible
        double bestDist = 1000000;
        for (int i = 0; i < sortedVertices.size(); i++) {
            double dist = sortedVertices.get(i).getDistance(enemyCenter);
            if (dist < bestDist) {
                bestDist = dist;
                currentVertex = i;
            }
        }
    }
}
