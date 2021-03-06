package ecgberht.BehaviourTrees.Build;

import ecgberht.GameState;
import ecgberht.Util.MutablePair;
import ecgberht.Util.Util;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Armory;
import org.openbw.bwapi4j.unit.Building;

public class ChooseArmory extends Action {

    public ChooseArmory(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (((GameState) this.handler).Fs.isEmpty()) return State.FAILURE;

            if (((GameState) this.handler).getArmySize() < ((GameState) this.handler).strat.facForArmory) {
                return State.FAILURE;
            }
            if (Util.countUnitTypeSelf(UnitType.Terran_Armory) < ((GameState) this.handler).strat.numArmories) {
                for (MutablePair<UnitType, TilePosition> w : ((GameState) this.handler).workerBuild.values()) {
                    if (w.first == UnitType.Terran_Armory) {
                        return State.FAILURE;
                    }
                }
                for (Building w : ((GameState) this.handler).workerTask.values()) {
                    if (w instanceof Armory) return State.FAILURE;
                }
                ((GameState) this.handler).chosenToBuild = UnitType.Terran_Armory;
                return State.SUCCESS;
            }
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}
