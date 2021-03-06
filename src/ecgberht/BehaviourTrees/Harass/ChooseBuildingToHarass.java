package ecgberht.BehaviourTrees.Harass;

import ecgberht.EnemyBuilding;
import ecgberht.GameState;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;

public class ChooseBuildingToHarass extends Action {

    public ChooseBuildingToHarass(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (((GameState) this.handler).chosenUnitToHarass != null) {
                return State.FAILURE;
            }
            for (EnemyBuilding u : ((GameState) this.handler).enemyBuildingMemory.values()) {
                if (((GameState) this.handler).enemyMainBase != null) {
                    if (u.type.isBuilding()) {
                        if (((GameState) this.handler).bwem.getMap().getArea(u.pos).equals(((GameState) this.handler).bwem.getMap().getArea(((GameState) this.handler).enemyMainBase.getLocation()))) {
                            ((GameState) this.handler).chosenUnitToHarass = u.unit;
                            return State.SUCCESS;
                        }
                    }
                }
            }
            if (((GameState) this.handler).chosenHarasser.isIdle()) {
                ((GameState) this.handler).workerIdle.add(((GameState) this.handler).chosenHarasser);
                ((GameState) this.handler).chosenHarasser.stop(false);
                ((GameState) this.handler).chosenHarasser = null;
                ((GameState) this.handler).chosenUnitToHarass = null;
            }
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}
