package ecgberht.Upgrade;

import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import ecgberht.GameState;

import bwapi.Unit;
import bwapi.UpgradeType;

public class ChooseWeaponInfUp extends Action {

	public ChooseWeaponInfUp(String name, GameHandler gh) {
		super(name, gh);
	}

	@Override
	public State execute() {
		try {
			if(((GameState)this.handler).UBs.isEmpty()) {
				return State.FAILURE;
			}
			for(Unit u : ((GameState)this.handler).UBs) {
				if(u.canUpgrade(UpgradeType.Terran_Infantry_Weapons) && !u.isResearching() && !u.isUpgrading()) {
					((GameState)this.handler).chosenUnitUpgrader = u;
					((GameState)this.handler).chosenUpgrade = UpgradeType.Terran_Infantry_Weapons;
					return State.SUCCESS;
				}
			}
			return State.FAILURE;
		} catch(Exception e) {
			System.err.println(this.getClass().getSimpleName());
			System.err.println(e);
			return State.ERROR;
		}
	}
}