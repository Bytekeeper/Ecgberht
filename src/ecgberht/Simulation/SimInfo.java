package ecgberht.Simulation;

import jfap.JFAPUnit;
import jfap.MutablePair;
import org.openbw.bwapi4j.unit.Unit;

import java.util.Set;
import java.util.TreeSet;

public class SimInfo {

    public SimType type = SimType.MIX;
    public Set<Unit> allies = new TreeSet<>();
    public Set<Unit> enemies = new TreeSet<>();
    public MutablePair<Integer, Integer> preSimScore;
    public MutablePair<Integer, Integer> postSimScore;
    public MutablePair<Set<JFAPUnit>, Set<JFAPUnit>> stateBefore = new MutablePair<>(new TreeSet<>(), new TreeSet<>());
    public MutablePair<Set<JFAPUnit>, Set<JFAPUnit>> stateAfter = new MutablePair<>(new TreeSet<>(), new TreeSet<>());
    public boolean lose = false;

    public enum SimType {GROUND, AIR, MIX}
}
