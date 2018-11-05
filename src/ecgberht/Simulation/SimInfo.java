package ecgberht.Simulation;

import ecgberht.Clustering.Cluster;
import ecgberht.Clustering.ClusterInfo;
import ecgberht.Util.MutablePair;
import java.util.ArrayList;
import java.util.Collection;
import org.bk.ass.Agent;
import org.bk.ass.Agent;
import org.openbw.bwapi4j.unit.Unit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

public class SimInfo {

    public SimType type = SimType.MIX;
    public Set<Unit> allies = new TreeSet<>();
    public Set<Unit> enemies = new TreeSet<>();
    public ClusterInfo allyCluster;
    MutablePair<Integer, Integer> preSimScore;
    MutablePair<Integer, Integer> postSimScore;
    ecgberht.Util.MutablePair<Integer, Integer> preSimScoreASS;
    ecgberht.Util.MutablePair<Integer, Integer> postSimScoreASS;
    MutablePair<Collection<Agent>, Collection<Agent>> stateBeforeJFAP = new MutablePair<>(new ArrayList<>(), new ArrayList<>());
    MutablePair<Collection<Agent>, Collection<Agent>> stateAfterJFAP = new MutablePair<>(new TreeSet<>(), new TreeSet<>());
    ecgberht.Util.MutablePair<Collection<Agent>, Collection<Agent>> stateBeforeASS = new ecgberht.Util.MutablePair<>(new ArrayList<>(), new ArrayList<>());
    ecgberht.Util.MutablePair<Collection<Agent>, Collection<Agent>> stateAfterASS = new ecgberht.Util.MutablePair<>(new ArrayList<>(), new ArrayList<>());
    public boolean lose = false;

    SimInfo(ClusterInfo friend) {
        this.allyCluster = friend;
    }

    SimInfo() {
    }

    public enum SimType {GROUND, AIR, MIX}
}
