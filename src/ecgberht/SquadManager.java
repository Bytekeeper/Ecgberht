package ecgberht;

import ecgberht.Clustering.ClusterInfo;
import ecgberht.Simulation.SimInfo;
import java.util.Collection;
import java.util.TreeSet;
import org.bk.ass.cluster.Cluster;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.unit.Bunker;
import org.openbw.bwapi4j.unit.Marine;
import org.openbw.bwapi4j.unit.Unit;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static ecgberht.Ecgberht.getGs;

public class SquadManager {
    public Map<Integer, Squad> squads = new TreeMap<>();

    public void createSquads(Collection<Cluster<Unit>> friendly) {
        squads.clear();
        int counter = 0;
        for (Cluster c : friendly) {
            ClusterInfo ci = (ClusterInfo) c.getUserObject();
            Squad s = new Squad(counter, new TreeSet<>(c.getElements()), new Position((int) ci.modeX, (int) ci.modeY));
            squads.put(counter, s);
            counter++;
        }
    }

    void updateSquadOrderAndMicro() {
        for (Squad u : squads.values()) {
            if (u.members.isEmpty()) continue;
            u.microUpdateOrder();
        }
    }

    void updateBunkers(){
        for (Map.Entry<Bunker, Set<Unit>> bunker : getGs().DBs.entrySet()) {
            SimInfo bunkerSim = getGs().sim.getSimulation(bunker.getKey(), SimInfo.SimType.MIX);
            if (!bunkerSim.enemies.isEmpty() && !getGs().learningManager.isNaughty()) {
                if (bunker.getValue().size() < 4) {
                    Marine closest = null;
                    double bestDist = Double.MAX_VALUE;
                    for(Unit u : bunkerSim.allies){
                        if(!(u instanceof Marine)) continue;
                        double dist = u.getDistance(bunker.getKey());
                        if(dist < bestDist){
                            closest = (Marine) u;
                            bestDist = dist;
                        }
                    }
                    if(closest != null){
                        bunker.getValue().add(closest);
                        bunkerSim.allies.remove(closest);
                        closest.rightClick(bunker.getKey(),false);
                    }
                }
            }
            else {
                bunker.getKey().unloadAll();
                bunkerSim.allies.addAll(bunker.getValue());
                bunker.getValue().clear();
            }

        }
    }
}
