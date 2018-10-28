package ecgberht.Simulation;

import ecgberht.Util.MutablePair;
import java.util.ArrayList;
import java.util.Collection;
import org.bk.ass.Agent;
import org.openbw.bwapi4j.unit.Unit;

import java.util.Set;
import java.util.TreeSet;

public class SimInfo {

    public SimType type = SimType.MIX;
    public Set<Unit> allies = new TreeSet<>();
    public Set<Unit> enemies = new TreeSet<>();
    MutablePair<Integer, Integer> preSimScore;
    MutablePair<Integer, Integer> postSimScore;
    MutablePair<Collection<Agent>, Collection<Agent>> stateBefore = new MutablePair<>(new ArrayList<>(), new ArrayList<>());
    MutablePair<Collection<Agent>, Collection<Agent>> stateAfter = new MutablePair<>(new TreeSet<>(), new TreeSet<>());
    public boolean lose = false;

    public enum SimType {GROUND, AIR, MIX}
}
