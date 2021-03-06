package ecgberht.BehaviourTrees.Harass;

import ecgberht.GameState;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Conditional;
import org.iaie.btree.util.GameHandler;

public class CheckExplorer extends Conditional {

    public CheckExplorer(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (!((GameState) this.handler).EI.defendHarass) {
                return State.FAILURE;
            } else {
                ((GameState) this.handler).chosenUnitToHarass = null;
                return State.SUCCESS;
            }
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}
