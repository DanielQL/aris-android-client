package edu.uoregon.casls.aris_android.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.uoregon.casls.aris_android.GamePlayActivity;
import edu.uoregon.casls.aris_android.data_objects.Instance;
import edu.uoregon.casls.aris_android.tab_controllers.InventoryViewFragment;

/**
 * Created by smorison on 8/20/15.
 */
public class InstancesModel extends ARISModel {

	public Map<Long, Instance> instances = new LinkedHashMap<>();
	public Map<Long, String> blacklist = new LinkedHashMap<>(); //list of ids attempting / attempted and failed to load
	public transient GamePlayActivity mGamePlayAct;

	public void initContext(GamePlayActivity gamePlayAct) {
		mGamePlayAct = gamePlayAct; // todo: may need leak checking is activity gets recreated.
	}

	public void clearGameData() {
		n_game_data_received = 0;
	}

	public long nGameDataToReceive() {
		return 1;
	}

	public void requestPlayerData() {
		this.requestPlayerInstances();
	}

	public void clearPlayerData() {
		Collection<Instance> insts = instances.values();
		for (Instance inst : insts) {
			if (inst.owner_id == Long.parseLong(mGamePlayAct.mPlayer.user_id))
				instances.remove(inst.instance_id);
		}
		n_player_data_received = 0;
	}

	public long nPlayerDataToReceive() {
		return 1;
	}

	//only difference at this point is notification sent- all other functionality same (merge into all known insts)
	public void playerInstancesReceived(List<Instance> instances) {
		this.updateInstances(instances);
		n_player_data_received++;

		mGamePlayAct.mDispatch.player_piece_available(); // _ARIS_NOTIF_SEND_(@"PLAYER_PIECE_AVAILABLE",nil,nil);

		InventoryViewFragment inv = mGamePlayAct.inventoryViewFragment;
		if (inv != null && mGamePlayAct.mCurrentFragVisible == inv.getTag()) {
			inv.updateList();
		}
	}

	public void gameInstancesReceived(List<Instance> instances) {
		this.updateInstances(instances);
		n_game_data_received++;

		mGamePlayAct.mDispatch.game_piece_available(); // _ARIS_NOTIF_SEND_(@"GAME_PIECE_AVAILABLE",nil,nil);
	}

	public void instanceReceived(Instance inst) {
		List<Instance> instances = new ArrayList<>();
		instances.add(inst);
		this.updateInstances(instances);
	}

	public void updateInstances(List<Instance> newInstances) {
		long newInstanceId;

//		NSDictionary playerDeltas = @{@"added":NSMutableArray alloc init,@"lost":NSMutableArray alloc init}; // orig iOS hashmap (NSDict)
//		NSDictionary gameDeltas   = @{@"added":NSMutableArray alloc init,@"lost":NSMutableArray alloc init}; // orig iOS hashmap (NSDict)

		// Hashmaps of->[HashMap of variable object types]
		//  e.g. playerDeltas = [ "added"->["instance"->(Instance obj), "delta"->(Long obj)]], "lost"->["instance"->(Instance obj), "delta"->(Long obj)]] ]
//		Map<String, Object> playerDeltas = new HashMap<>();
//		Map<String, Object> gameDeltas = new HashMap<>();
//		playerDeltas.put("added", new ArrayList<Object>());
//		playerDeltas.put("lost", new ArrayList<Object>());
//		gameDeltas.put("added", new ArrayList<Object>());
//		gameDeltas.put("lost", new ArrayList<Object>());

		Map<String, List<Map<String, Object>>> playerDeltas = new HashMap<>();
		Map<String, List<Map<String, Object>>> gameDeltas = new HashMap<>();
		playerDeltas.put("added", new ArrayList<Map<String, Object>>());
		playerDeltas.put("lost", new ArrayList<Map<String, Object>>());
		gameDeltas.put("added", new ArrayList<Map<String, Object>>());
		gameDeltas.put("lost", new ArrayList<Map<String, Object>>());

		for (Instance newInstance : newInstances) {
			newInstance.initContext(mGamePlayAct);
			newInstanceId = newInstance.instance_id;
			if (!instances.containsKey(newInstanceId)) {
				//No instance exists- give player instance with 0 qty and let it be updated like all the others
				Instance fakeExistingInstance = new Instance();
				fakeExistingInstance.initContext(mGamePlayAct);
				fakeExistingInstance.mergeDataFromInstance(newInstance);
				fakeExistingInstance.qty = 0;
				instances.put(newInstanceId, fakeExistingInstance); // fixme: instances is not getting set correctly missing elements compared to iOS
				blacklist.remove(newInstanceId);
			}

			Instance existingInstance = instances.get(newInstanceId);
			long delta = newInstance.qty - existingInstance.qty;
			existingInstance.mergeDataFromInstance(newInstance);

//			NSDictionary d = @{@"instance":existingInstance,@"delta":NSNumber numberWithLong:delta};
			Map<String, Object> d = new HashMap<>();
			d.put("instance", existingInstance);
			d.put("delta", delta);

			if (existingInstance.owner_id == Long.parseLong(mGamePlayAct.mPlayer.user_id)) {
				if (!this.playerDataReceived() || mGamePlayAct.mGame.network_level.contentEquals("REMOTE")) { //only local should be making changes to player. fixes race cond (+1, -1, +1 notifs)
					if (delta > 0) playerDeltas.get("added").add(d); //) addObject:d;
					if (delta < 0) playerDeltas.get("lost").add(d);
				}
			}
			else {
				//race cond (above) still applies here, but notifs oughtn't be a problem, and fixes this.over time
				if (delta > 0) gameDeltas.get("added").add(d);
				if (delta < 0) gameDeltas.get("lost").add(d);
			}
		}

		this.sendNotifsForGameDeltas(gameDeltas, playerDeltas);
	}

	public void sendNotifsForGameDeltas(Map<String, List<Map<String, Object>>> gameDeltas, Map<String, List<Map<String, Object>>> playerDeltas) {
		if (playerDeltas != null && playerDeltas.size() > 0) {
			if (playerDeltas.containsKey("added"))
				mGamePlayAct.mDispatch.model_instances_player_gained(playerDeltas); // _ARIS_NOTIF_SEND_(@"MODEL_INSTANCES_PLAYER_GAINED",nil,playerDeltas);
			if (playerDeltas.containsKey("lost"))
				mGamePlayAct.mDispatch.model_instances_player_lost(playerDeltas); // _ARIS_NOTIF_SEND_(@"MODEL_INSTANCES_PLAYER_LOST",  nil,playerDeltas);
			mGamePlayAct.mDispatch.model_instances_player_available(playerDeltas); // _ARIS_NOTIF_SEND_(@"MODEL_INSTANCES_PLAYER_AVAILABLE",nil,playerDeltas);
		}

		if (gameDeltas != null && gameDeltas.size() > 0) {
			if (gameDeltas.containsKey("added"))
				mGamePlayAct.mDispatch.model_instances_gained(gameDeltas); // _ARIS_NOTIF_SEND_(@"MODEL_INSTANCES_GAINED",nil,gameDeltas);
			if (gameDeltas.containsKey("lost"))
				mGamePlayAct.mDispatch.model_instances_lost(gameDeltas); // _ARIS_NOTIF_SEND_(@"MODEL_INSTANCES_LOST",  nil,gameDeltas);
			mGamePlayAct.mDispatch.model_instances_available(gameDeltas); // _ARIS_NOTIF_SEND_(@"MODEL_INSTANCES_AVAILABLE",nil,gameDeltas);
		}
	}

	public void requestGameData() {
		this.requestInstances();
	}

	public void requestInstances() {
		mGamePlayAct.mAppServices.fetchInstances();
	}

	public void requestInstance(long i) {
		mGamePlayAct.mAppServices.fetchInstanceById(i);
	}// _SERVICES_ fetchInstanceById(i);

	public void requestPlayerInstances() {
		if (this.playerDataReceived() && !mGamePlayAct.mGame.network_level.contentEquals("REMOTE")) {
			Collection<Instance> pinsts = instances.values();
			mGamePlayAct.mDispatch.services_player_instances_received(pinsts); // ARIS_NOTIF_SEND_(@"SERVICES_PLAYER_INSTANCES_RECEIVED",nil,@{@"instances":pinsts});
		}
		// for now treat every game as HYBRID, since that's really all we need to worry about in v1.0 of Android
		if (!this.playerDataReceived() ||
				mGamePlayAct.mGame.network_level.contentEquals("HYBRID") ||
				mGamePlayAct.mGame.network_level.contentEquals("REMOTE"))
			mGamePlayAct.mAppServices.fetchInstancesForPlayer(); // _SERVICES_ fetchInstancesForPlayer;
	}

	public long setQtyForInstanceId(long instance_id, long qty) {
		Instance i = this.instanceForId(instance_id);
		if (i == null) return 0;
		if (qty < 0) qty = 0;

		if (!mGamePlayAct.mGame.network_level.contentEquals("REMOTE")) {
			long oldQty = i.qty;
			i.qty = qty;
			Map<String, List<Map<String, Object>>> deltas = new HashMap<>();
			Map<String, Object> d = new HashMap<>();
			d.put("instance", i);
			d.put("delta", qty - oldQty);
			ArrayList<Map<String, Object>> list = new ArrayList<>();
			list.add(d);

			if (qty > oldQty) {
				deltas.put("added", list); //deltas = @{@"lost":@,@"added":@@{@"instance":i,@"delta":NSNumber numberWithLong:qty-oldQty}};
				deltas.put("lost", null);
			}
			if (qty < oldQty) {
				deltas.put("added", null);
				deltas.put("lost", list);  //deltas = @{@"added":@,@"lost":@@{@"instance":i,@"delta":NSNumber numberWithLong:qty-oldQty}};
			}

			if (deltas != null && !deltas.isEmpty()) {
				if (i.owner_type.contentEquals("USER") && i.owner_id == Long.parseLong(mGamePlayAct.mPlayer.user_id))
					this.sendNotifsForGameDeltas(null, deltas);
				else if (i.owner_type.contentEquals("GAME_CONTENT"))
					this.sendNotifsForGameDeltas(deltas, null);
			}
		}

		if (!mGamePlayAct.mGame.network_level.contentEquals("LOCAL"))
			mGamePlayAct.mAppServices.setQtyForInstanceId(instance_id, qty); // _SERVICES_ setQtyForInstanceId:instance_id qty:qty;

		this.requestPlayerData();

		return qty;
	}

	// null instance (id == 0) NOT flyweight!!! (to allow for temporary customization safety)
	public Instance instanceForId(long instance_id) {
		if (instance_id == 0) return new Instance();
		Instance i = instances.get(instance_id);
		if (i == null) {
			blacklist.put(instance_id, "true"); //setObject:@"true" forKey:NSNumber numberWithLong:instance_id;
			this.requestInstance(instance_id);
			return new Instance();
		}
		return i;
	}

	public List<Instance> instancesForType(String object_type, long object_id) {
		List<Instance> a = new ArrayList<>();
		Collection<Instance> allInstances = instances.values();
		for (Instance inst : allInstances) {
//			Instance inst = instances allValuesi; // ??
			if (inst.object_id == object_id && inst.object_type.contentEquals(object_type))
				a.add(inst);// addObject:inst;
		}
		return a;
	}

	public List<Instance> playerInstances() {
		List<Instance> pInstances = new ArrayList<>();
		Collection<Instance> allInstances = instances.values();
		for (Instance inst : allInstances) {
			if (inst.owner_type.contentEquals("USER") &&
					inst.owner_id == Long.parseLong(mGamePlayAct.mPlayer.user_id))
				pInstances.add(inst);
		}
		return pInstances;
	}

	public List<Instance> gameOwnedInstances() {
		List<Instance> gInstances = new ArrayList<>();
		Collection<Instance> allInstances = instances.values();
		for (Instance inst : allInstances) {
			if (inst.owner_type.contentEquals("GAME"))
				gInstances.add(inst);
		}
		return gInstances;
	}

	public List<Instance> groupOwnedInstances() {
		List<Instance> gInstances = new ArrayList<>();
		Collection<Instance> allInstances = instances.values();
		for (Instance inst : allInstances) {
			if (inst.owner_type.contentEquals("GROUP"))
					if (mGamePlayAct.mGame.groupsModel.playerGroup != null // todo: null check is band-aid over the NPE that happens below. Need to fix underlying issue
							&& inst.owner_id == mGamePlayAct.mGame.groupsModel.playerGroup.group_id) // fixme: playerGroup is not getting set (ie: null)
				gInstances.add(inst);
		}
		return gInstances;
	}

}
