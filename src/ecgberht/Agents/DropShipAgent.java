package ecgberht.Agents;

import ecgberht.Squad;
import ecgberht.Util.Util;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.type.Color;
import org.openbw.bwapi4j.type.Order;
import org.openbw.bwapi4j.unit.Dropship;
import org.openbw.bwapi4j.unit.MobileUnit;
import org.openbw.bwapi4j.unit.Unit;
import org.openbw.bwapi4j.unit.Worker;

import java.util.*;
import java.util.stream.Collectors;

import static ecgberht.Ecgberht.getGs;

public class DropShipAgent extends Agent implements Comparable<Unit> {

    public Dropship unit;
    private Status status = Status.IDLE;
    private Set<Unit> airAttackers = new TreeSet<>();
    private Set<Unit> cargoLoaded = new TreeSet<>();
    private Set<Unit> cargoWanted = new TreeSet<>();
    private Position target;
    private Unit pickingUp;
    private boolean islandExpanding = false;
    private static List<Position> waypoints = new ArrayList<>();
    public static Set<Unit> loadedUnitsGlobally = new TreeSet<>();
    private Position currentWaypoint;
    private Position lastWaypoint;
    private Position finalWaypoint;

    public DropShipAgent(Unit unit) {
        super();
        this.unit = (Dropship) unit;
        this.myUnit = unit;
        if(waypoints.isEmpty()) initWaypoints();
    }

    private void initWaypoints() {
        // Tile coordinates.
        int minX = 32;
        int minY = 32;
        int maxX = (getGs().getGame().getBWMap().mapWidth() - 1) * 32;
        int maxY = (getGs().getGame().getBWMap().mapHeight() - 1) * 32;

        waypoints.add(new Position(minX, minY));
        waypoints.add(new Position(maxX, maxY));
        waypoints.add(new Position(minX, 32));
        waypoints.add(new Position(maxX, 32));

        waypoints.add(new Position(32, maxY/2));
        waypoints.add(new Position(maxX/2, 32));
        waypoints.add(new Position(maxX, maxY/2));
        waypoints.add(new Position(maxX/2, maxY));

    }

    public String statusToString() {
        switch (status) {
            case PICKING:
                return "PICKING";
            case MOVING:
                return "MOVING";
            case DROP:
                return "DROP";
            case RETREAT:
                return "RETREAT";
            case IDLE:
                return "IDLE";
        }
        return "None";
    }

    public void setCargo(Set<Unit> cargo) {
        this.cargoWanted = cargo;
        for (Unit u : this.cargoWanted) {
            loadedUnitsGlobally.add(u);
            if (u instanceof Worker && (((Worker) u).isCarryingMinerals() || ((Worker) u).isCarryingGas())) {
                ((Worker) u).returnCargo();
                ((MobileUnit) u).rightClick(unit, true);
            } else ((MobileUnit) u).rightClick(unit, false);
        }
    }

    public void setTarget(Position target, boolean islandExpanding) {
        this.target = target;
        this.islandExpanding = islandExpanding;
    }

    private void checkLoaded() {
        if (pickingUp == null) return;
        Unit transport = ((MobileUnit) pickingUp).getTransport();
        if (transport != null && transport.equals(unit)) {
            cargoLoaded.add(pickingUp);
            cargoWanted.remove(pickingUp);
            pickingUp = null;
        }
    }

    private void checkUnloaded() {
        for (Unit u : cargoLoaded) {
            Unit transport = ((MobileUnit) u).getTransport();
            if (transport == null) {
                cargoLoaded.remove(u);
                loadedUnitsGlobally.remove(u);
                //break;
            }
        }
    }

    @Override
    public boolean runAgent() {
        try {
            if (!unit.exists()) return true;
            actualFrame = getGs().frameCount;
            frameLastOrder = unit.getLastCommandFrame();
            if (actualFrame == frameLastOrder) return false;
            airAttackers.clear();
            cleanLoadLists();
            status = getNewStatus();
            switch (status) {
                case PICKING:
                    picking();
                    break;
                case MOVING:
                    moving();
                    break;
                case DROP:
                    drop();
                    break;
                case RETREAT:
                    retreat();
                    break;
                case IDLE:
                    idle();
                    break;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Exception DropShipAgent");
            e.printStackTrace();
        }
        return false;
    }

    private void cleanLoadLists() {
        cargoWanted.removeIf(unit1 -> !unit.exists());
        cargoLoaded.removeIf(unit1 -> !unit.exists()); // Exists if loaded??
        checkLoaded();
        checkUnloaded();
    }

    private void drop() {
        if (target == null) return;
        checkUnloaded();
        if (cargoLoaded.isEmpty()) return;
        if (unit.getOrder() == Order.MoveUnload || unit.getOrder() == Order.Unload) return;
        unit.unloadAll(target);
    }

    private void moving() {
        if (target == null) return;
        if (unit.getTargetPosition() != null && unit.getTargetPosition().equals(target)) return;
        if(islandExpanding) unit.move(target);
        else{
           if(lastWaypoint == null && currentWaypoint == null && finalWaypoint == null){
               double distMax = Double.MAX_VALUE;
               Position chosen = null;
               for(Position w : waypoints){
                   double dist = w.getDistance(target);
                   if(finalWaypoint == null || dist < distMax){
                       distMax = dist;
                       chosen = w;
                   }
               }
               //finalWaypoint = chosen;
               unit.move(target);
                // TODO complete
           }
        }
    }

    private void picking() {
        if (cargoWanted.isEmpty()) return;
        if (pickingUp == null) {
            double distB = Double.MAX_VALUE;
            for (Unit u : cargoWanted) {
                double distA = Util.broodWarDistance(unit.getPosition(), u.getPosition());
                if (pickingUp == null || distA < distB) {
                    pickingUp = u;
                    distB = distA;
                }
            }
            if (pickingUp != null) {
                unit.load((MobileUnit) pickingUp);
            }
        } else {
            if (unit.getOrderTarget() != null && unit.getOrderTarget().equals(pickingUp)) return;
            checkLoaded();
        }
    }

    private void idle() { // TODO knapsack // TODO fix??
        Optional<Map.Entry<Integer, Squad>> closest = getGs().sqManager.squads.entrySet().stream().filter(s -> !s.getValue().members.contains(unit) || (s.getValue().members.contains(unit) && s.getValue().members.stream().filter(u -> !u.getType().isFlyer()).count() > 1)).min(Comparator.comparing(u -> u.getValue().getSquadCenter().getDistance(unit.getPosition())));
        if(closest.isPresent()){
            getGs().getGame().getMapDrawer().drawCircleMap(closest.get().getValue().getSquadCenter(),200, Color.RED);
            Set<Unit> load = closest.get().getValue().members.stream().filter(u -> !unit.getType().isFlyer() && !getGs().agents.containsKey(u)).collect(Collectors.toSet());
            setCargo(load);
            closest.get().getValue().members.removeAll(load);
            if(closest.get().getValue().members.isEmpty()) getGs().sqManager.squads.remove(closest.get().getKey());
        }
    }

    @Override
    protected void retreat() {
        Position CC = getGs().getNearestCC(myUnit.getPosition());
        if (CC != null) target = CC;
        else target = getGs().getPlayer().getStartLocation().toPosition();
        ((MobileUnit) myUnit).move(target);
    }

    private Status getNewStatus() {
        if (status == Status.IDLE) {
            if (!cargoWanted.isEmpty()) return Status.PICKING;
            if (!cargoLoaded.isEmpty() && target != null) return Status.MOVING;
        }
        if (status == Status.PICKING) {
            if(canKeepPicking() && (cargoWanted.isEmpty() || unit.getOrder() == Order.PlayerGuard)) return Status.IDLE;
            if(!canKeepPicking() && target != null) return Status.MOVING;
            if (!cargoWanted.isEmpty()) return Status.PICKING;
            return Status.IDLE;

        }
        if (status == Status.MOVING) {
            if (target == null) return Status.IDLE;
            if (Util.broodWarDistance(unit.getPosition(), target) < 200) return Status.DROP;
            return Status.MOVING;
        }
        if (status == Status.DROP) {
            if (cargoLoaded.isEmpty()) return Status.RETREAT;
            else return Status.DROP;
        }
        if (status == Status.RETREAT) {
            if (target != null && Util.broodWarDistance(unit.getPosition(), target) <= 64) return Status.IDLE;
            else return Status.RETREAT;
        }
        return Status.IDLE;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this.unit) return true;
        if (!(o instanceof DropShipAgent)) return false;
        DropShipAgent dropship = (DropShipAgent) o;
        return unit.equals(dropship.unit);
    }

    private boolean canKeepPicking(){
        int stored = cargoLoaded.stream().mapToInt(unit -> unit.getType().spaceRequired()).sum();
        return unit.getType().spaceProvided() > stored;
    }
    @Override
    public int hashCode() {
        return Objects.hash(unit);
    }

    @Override
    public int compareTo(Unit v1) {
        return this.unit.getId() - v1.getId();
    }

    enum Status {PICKING, MOVING, DROP, RETREAT, IDLE}
}
