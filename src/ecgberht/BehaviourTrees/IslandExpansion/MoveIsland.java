package ecgberht.BehaviourTrees.IslandExpansion;

import ecgberht.GameState;
import ecgberht.Util.MutablePair;
import ecgberht.Util.Util;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.SCV;
import org.openbw.bwapi4j.unit.Worker;

public class MoveIsland extends Action {

    public MoveIsland(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            Worker chosen = ((GameState) this.handler).chosenWorkerDrop;
            UnitType chosenType = UnitType.Terran_Command_Center;
            TilePosition chosenTile = ((GameState) this.handler).chosenIsland.getLocation();
            Position realEnd = Util.getUnitCenterPosition(chosenTile.toPosition(), chosenType);
            if (chosen.move(realEnd)) {
                ((GameState) this.handler).workerBuild.put((SCV) chosen, new MutablePair<>(chosenType, chosenTile));
                ((GameState) this.handler).deltaCash.first += chosenType.mineralPrice();
                ((GameState) this.handler).deltaCash.second += chosenType.gasPrice();
                ((GameState) this.handler).chosenWorkerDrop = null;
                ((GameState) this.handler).chosenIsland = null;
                ((GameState) this.handler).islandExpand = false;
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
