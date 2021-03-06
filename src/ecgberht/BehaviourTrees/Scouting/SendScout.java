package ecgberht.BehaviourTrees.Scouting;

import bwem.Base;
import ecgberht.GameState;
import ecgberht.Util.Util;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.unit.MobileUnit;
import org.openbw.bwapi4j.unit.Worker;

import java.util.ArrayList;
import java.util.List;

public class SendScout extends Action {

    public SendScout(String name, GameHandler gh) {
        super(name, gh);

    }

    @Override
    public State execute() {
        try {
            if (((GameState) this.handler).enemyMainBase == null) {
                if (!((GameState) this.handler).scoutSLs.isEmpty()) {
                    List<Base> aux = new ArrayList<>();
                    for (Base b : ((GameState) this.handler).scoutSLs) {
                        if (((GameState) this.handler).fortressSpecialBLs.containsKey(b)) continue;
                        if (((GameState) this.handler).strat.name.equals("PlasmaWraithHell")) {
                            if (((MobileUnit) ((GameState) this.handler).chosenScout).move(b.getLocation().toPosition())) {
                                return State.SUCCESS;
                            }
                        } else if (Util.isConnected(b.getLocation(), ((GameState) this.handler).chosenScout.getTilePosition())) {
                            if (((MobileUnit) ((GameState) this.handler).chosenScout).move(b.getLocation().toPosition())) {
                                return State.SUCCESS;
                            }
                        } else aux.add(b);
                    }
                    ((GameState) this.handler).scoutSLs.removeAll(aux);
                }
            }
            if (((GameState) this.handler).strat.name.equals("PlasmaWraithHell")) {
                ((MobileUnit) ((GameState) this.handler).chosenScout).stop(false);
                ((GameState) this.handler).chosenScout = null;
                return State.FAILURE;
            }
            ((GameState) this.handler).workerIdle.add((Worker) ((GameState) this.handler).chosenScout);
            ((MobileUnit) ((GameState) this.handler).chosenScout).stop(false);
            ((GameState) this.handler).chosenScout = null;
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}
