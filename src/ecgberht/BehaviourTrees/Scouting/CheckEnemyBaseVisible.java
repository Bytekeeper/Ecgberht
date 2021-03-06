package ecgberht.BehaviourTrees.Scouting;

import ecgberht.EnemyBuilding;
import ecgberht.GameState;
import ecgberht.Util.BaseLocationComparator;
import ecgberht.Util.Util;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.unit.PlayerUnit;
import org.openbw.bwapi4j.unit.Worker;

import java.util.HashSet;
import java.util.List;

public class CheckEnemyBaseVisible extends Action {

    public CheckEnemyBaseVisible(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            List<PlayerUnit> enemies = ((GameState) this.handler).getGame().getUnits(((GameState) this.handler).getIH().enemy());
            if (!enemies.isEmpty()) {
                for (EnemyBuilding u : ((GameState) this.handler).enemyBuildingMemory.values()) {
                    if (Util.broodWarDistance(((GameState) this.handler).chosenScout.getPosition(), u.pos.toPosition()) <= 500) {
                        ((GameState) this.handler).enemyMainBase = Util.getClosestBaseLocation(u.pos.toPosition());
                        ((GameState) this.handler).scoutSLs = new HashSet<>();
                        if (!((GameState) this.handler).strat.name.equals("PlasmaWraithHell")) {
                            ((GameState) this.handler).chosenHarasser = (Worker) ((GameState) this.handler).chosenScout;
                        }
                        ((GameState) this.handler).chosenScout = null;
                        ((GameState) this.handler).getIH().sendText("!");
                        ((GameState) this.handler).playSound("gearthere.mp3");
                        ((GameState) this.handler).enemyBLs.clear();
                        ((GameState) this.handler).enemyBLs.addAll(((GameState) this.handler).BLs);
                        ((GameState) this.handler).enemyBLs.sort(new BaseLocationComparator(((GameState) this.handler).enemyMainBase));
                        if (((GameState) this.handler).firstScout) {
                            ((GameState) this.handler).enemyStartBase = ((GameState) this.handler).enemyMainBase;
                            ((GameState) this.handler).enemyMainArea = ((GameState) this.handler).enemyStartBase.getArea();
                            ((GameState) this.handler).enemyNaturalBase = ((GameState) this.handler).enemyBLs.get(1);
                            ((GameState) this.handler).enemyNaturalArea = ((GameState) this.handler).enemyNaturalBase.getArea();
                        }
                        return State.SUCCESS;
                    }
                }
            }
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}