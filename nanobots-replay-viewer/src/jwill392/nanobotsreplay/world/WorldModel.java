package jwill392.nanobotsreplay.world;

import java.awt.Dimension;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import jwill392.nanobotsreplay.NBRV;

import replay.ReplayProto.Replay;
import replay.ReplayProto.Replay.TurnInfo;

public class WorldModel implements Iterable<EntityModel> {
	private int turn;
	private final Replay rep;
	private final Map<Integer, EntityModel> ents;
	private final Set<EntityModel> livingEnts;

	public WorldModel(Replay rep) {
		ents = new HashMap<>();
		livingEnts = new HashSet<>();

		this.rep = rep;
		turn = 0;

		// populate ents with every ent in replay
		for (TurnInfo turn : rep.getTurnsList()) {
			for (Replay.Entity ent : turn.getEntsList()) {
				int eid = ent.getEid();
				if (!ents.containsKey(eid)) {
					ents.put(eid, new EntityModel(rep, eid));
				}
			}
		}

		setTurn(0);
	}

	public void setTurn(int turn) {
		this.turn = turn;

		livingEnts.clear();
		for (EntityModel ent : ents.values()) {
			if (ent.getLifespan().contains(turn)) {
				livingEnts.add(ent);
			}
		}

		NBRV.eventBus.post(new ModelTurnChange(turn, this));
	}

	public boolean hasNextTurn() {
		return getTurn() < getEndTurn();
	}
	public void nextTurn() {
		setTurn(getTurn() + 1);
	}


	public boolean hasPrevTurn() {
		return getTurn() > 0;
	}
	public void prevTurn() {
		setTurn(getTurn() - 1);
	}

	public boolean hasTurn(int turn) {
		return 0 <= turn && turn <= getEndTurn();
	}

	public int getTurn() {
		return turn;
	}

	public int getEndTurn() {
		return rep.getTurnsCount() - 1;
	}
	@Override
	public Iterator<EntityModel> iterator() {
		return livingEnts.iterator();
	}
	public Dimension getSize() {
		return replay.Util.of(rep.getMapSize());
	}

	public static class ModelTurnChange {
		public final int newTurn;
		public final WorldModel model;
		public ModelTurnChange(int newTurn, WorldModel model) {
			this.newTurn = newTurn;
			this.model = model;
		}
	}
}
