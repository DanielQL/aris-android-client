package edu.uoregon.casls.aris_android.models;

import android.location.Location;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.uoregon.casls.aris_android.GamePlayActivity;
import edu.uoregon.casls.aris_android.data_objects.Game;
import edu.uoregon.casls.aris_android.data_objects.ArisLog;

/**
 * Created by smorison on 8/20/15.
 */
public class LogsModel extends ARISModel {

	public Map<Long, ArisLog> logs = new LinkedHashMap<>();
	public long local_log_id; //starts at 1, no way it will ever catch up to actual logs

	public transient GamePlayActivity mGamePlayAct;
	public transient Game mGame;

	public void initContext(GamePlayActivity gamePlayAct) {
		mGamePlayAct = gamePlayAct; // todo: may need leak checking is activity gets recreated.
		mGame = mGamePlayAct.mGame; // convenience shortcut ref.
	}

	public void clearPlayerData() {
		logs.clear();
		local_log_id = 1;
		n_game_data_received = 0;
	}

	public long nPlayerDataToReceive() {
		return 1;
	}

	public void requestPlayerData() {
		this.requestPlayerLogs();
	}

	public void clearGameData() {
		this.clearPlayerData();
	}

	public void logsReceived(List<ArisLog> logs) {
		this.updateLogs(logs);
	}

	public void updateLogs(List<ArisLog> newLogs) {
		long newLogId;
		for (ArisLog newLog : newLogs) {
			newLogId = newLog.user_log_id;
			if (!logs.containsKey(newLogId))
				logs.put(newLogId, newLog); // setObject:newLog forKey:newLogId);
		}
		mGamePlayAct.mDispatch.model_logs_available(); //_ARIS_NOTIF_SEND_(@"MODEL_LOGS_AVAILABLE",nil,nil);
		n_player_data_received++;
		mGamePlayAct.mDispatch.player_piece_available(); //_ARIS_NOTIF_SEND_(@"PLAYER_PIECE_AVAILABLE",nil,nil);
	}

	public void addLogType(String type, long content_id, long qty) {
		ArisLog l = new ArisLog();
		l.user_log_id = local_log_id++;
		l.event_type = type;
		l.content_id = content_id;
		l.qty = qty;
		logs.put(l.user_log_id, l); // setObject:l forKey:[NSNumber numberWithLong:l.user_log_id]);
	}

	public void requestPlayerLogs() {
		mGamePlayAct.mAppServices.fetchLogsForPlayer();
	}

	public ArisLog logForId(long log_id) {
		return logs.get(log_id); // objectForKey:[NSNumber numberWithLong:user_log_id]);
	}

	public void playerEnteredGame() {
		if (!mGame.network_level.contentEquals("LOCAL"))
			mGamePlayAct.mAppServices.logPlayerEnteredGame();
		this.playerMoved(); //start off with a move to set location
	}

	public void playerMoved() {
		if (!mGame.network_level.contentEquals("LOCAL"))
			mGamePlayAct.mAppServices.logPlayerMoved();
		mGamePlayAct.mDispatch.user_moved(); //_ARIS_NOTIF_SEND_(@"USER_MOVED",nil,nil);
	}

	public void playerViewedTabId(long tab_id) {
		if (!mGame.network_level.contentEquals("LOCAL"))
			mGamePlayAct.mAppServices.logPlayerViewedTabId(tab_id);
		this.addLogType("VIEW_TAB", tab_id, 0);
	}

	public void playerViewedContent(String content, long content_id) {
		if (!mGame.network_level.contentEquals("LOCAL")) {
			if (content.contentEquals("PLAQUE"))
				mGamePlayAct.mAppServices.logPlayerViewedPlaqueId(content_id);
			if (content.contentEquals("ITEM"))
				mGamePlayAct.mAppServices.logPlayerViewedItemId(content_id);
			if (content.contentEquals("DIALOG"))
				mGamePlayAct.mAppServices.logPlayerViewedDialogId(content_id);
			if (content.contentEquals("DIALOG_SCRIPT"))
				mGamePlayAct.mAppServices.logPlayerViewedDialogScriptId(content_id);
			if (content.contentEquals("WEB_PAGE"))
				mGamePlayAct.mAppServices.logPlayerViewedWebPageId(content_id);
			if (content.contentEquals("NOTE"))
				mGamePlayAct.mAppServices.logPlayerViewedNoteId(content_id);
			if (content.contentEquals("SCENE"))
				mGamePlayAct.mAppServices.logPlayerViewedSceneId(content_id);
		}

		if (content.contentEquals("PLAQUE")) this.addLogType("VIEW_PLAQUE", content_id, 0);
		if (content.contentEquals("ITEM")) this.addLogType("VIEW_ITEM", content_id, 0);
		if (content.contentEquals("DIALOG")) this.addLogType("VIEW_DIALOG", content_id, 0);
		if (content.contentEquals("DIALOG_SCRIPT"))
			this.addLogType("VIEW_DIALOG_SCRIPT", content_id, 0);
		if (content.contentEquals("WEB_PAGE")) this.addLogType("VIEW_WEB_PAGE", content_id, 0);
		if (content.contentEquals("NOTE")) this.addLogType("VIEW_NOTE", content_id, 0);
		if (content.contentEquals("SCENE")) this.addLogType("CHANGE_SCENE", content_id, 0);

		mGame.questsModel.logAnyNewlyCompletedQuests();
	}

	public void playerViewedInstanceId(long instance_id) {
		if (!mGame.network_level.contentEquals("LOCAL"))
			mGamePlayAct.mAppServices.logPlayerViewedInstanceId(instance_id);
		this.addLogType("VIEW_INSTANCE", instance_id, 0);
	}

	public void playerTriggeredTriggerId(long trigger_id) {
		if (!mGame.network_level.contentEquals("LOCAL"))
			mGamePlayAct.mAppServices.logPlayerTriggeredTriggerId(trigger_id);
		this.addLogType("TRIGGER_TRIGGER", trigger_id, 0);
	}

	public void playerReceivedItemId(long item_id, long qty) {
		if (!mGame.network_level.contentEquals("LOCAL"))
			mGamePlayAct.mAppServices.logPlayerReceivedItemId(item_id, qty);
		this.addLogType("RECEIVE_ITEM", item_id, 0);
		mGame.questsModel.logAnyNewlyCompletedQuests();
	}

	public void playerLostItemId(long item_id, long qty) {
		if (!mGame.network_level.contentEquals("LOCAL"))
			mGamePlayAct.mAppServices.logPlayerLostItemId(item_id, qty);
		this.addLogType("LOSE_ITEM", item_id, 0);
		mGame.questsModel.logAnyNewlyCompletedQuests();
	}

	public void gameReceivedItemId(long item_id, long qty) {
		if (!mGame.network_level.contentEquals("LOCAL"))
			mGamePlayAct.mAppServices.logGameReceivedItemId(item_id, qty);
		this.addLogType("GAME_RECEIVE_ITEM", item_id, 0);
		mGame.questsModel.logAnyNewlyCompletedQuests();
	}

	public void gameLostItemId(long item_id, long qty) {
		if (!mGame.network_level.contentEquals("LOCAL"))
			mGamePlayAct.mAppServices.logGameLostItemId(item_id, qty);
		this.addLogType("GAME_LOSE_ITEM", item_id, 0);
		mGame.questsModel.logAnyNewlyCompletedQuests();
	}

	public void groupReceivedItemId(long item_id, long qty) {
		if (!mGame.network_level.contentEquals("LOCAL"))
			mGamePlayAct.mAppServices.logGroupReceivedItemId(item_id, qty);
		this.addLogType("GROUP_RECEIVE_ITEM", item_id, 0);
		mGame.questsModel.logAnyNewlyCompletedQuests();
	}

	public void groupLostItemId(long item_id, long qty) {
		if (!mGame.network_level.contentEquals("LOCAL"))
			mGamePlayAct.mAppServices.logGroupLostItemId(item_id, qty);
		this.addLogType("GROUP_LOSE_ITEM", item_id, 0);
		mGame.questsModel.logAnyNewlyCompletedQuests();
	}

	public void playerChangedSceneId(long scene_id) {
		if (!mGame.network_level.contentEquals("LOCAL"))
			mGamePlayAct.mAppServices.logPlayerSetSceneId(scene_id);
		this.addLogType("CHANGE_SCENE", scene_id, 0);
	}

	public void playerChangedGroupId(long group_id) {
		if (!mGame.network_level.contentEquals("LOCAL"))
			mGamePlayAct.mAppServices.logPlayerJoinedGroupId(group_id);
		this.addLogType("JOIN_GROUP", group_id, 0);
	}

	public void playerRanEventPackageId(long event_package_id) {
		if (!mGame.network_level.contentEquals("LOCAL"))
			mGamePlayAct.mAppServices.logPlayerRanEventPackageId(event_package_id);
		this.addLogType("RUN_EVENT_PACKAGE", event_package_id, 0);
		mGame.questsModel.logAnyNewlyCompletedQuests();
	}

	public void playerCompletedQuestId(long quest_id) {
		//let server figure it out on its own, for now
		//if(!mGame.network_level.contentEquals("LOCAL")
		//  mGamePlayAct.mServices.logPlayerCompletedQuestId:quest_id);
		this.addLogType("COMPLETE_QUEST", quest_id, 0);
		mGame.questsModel.logAnyNewlyCompletedQuests();
	}

	public boolean hasLogType(String type) {
		Collection<ArisLog> allLogs = logs.values();
		for (ArisLog l : allLogs) {
			if (l.event_type.contentEquals(type))
				return true;
		}
		return false;
	}

	public boolean hasLogType(String type, long content_id) {
		Collection<ArisLog> allLogs = logs.values();
		for (ArisLog l : allLogs) {
			if (l.event_type.contentEquals(type) && l.content_id == content_id)
				return true;
		}
		return false;
	}

	public boolean hasLogType(String type, long content_id, long qty) {
		Collection<ArisLog> allLogs = logs.values();
		for (ArisLog l : allLogs) {
			if (l.event_type.contentEquals(type) && l.content_id == content_id && l.qty == qty)
				return true;
		}
		return false;
	}

	public long countLogsOfType(String type) {
		Collection<ArisLog> allLogs = logs.values();
		long qty = 0;
		for (ArisLog l : allLogs) {
			if (type != null && !l.event_type.contentEquals(type))
				continue;
			qty++;
		}
		return qty;
	}

	public long countLogsOfType(String type, long withinDistance, double lat, double lng) {
		Collection<ArisLog> allLogs = logs.values();
		long qty = 0;
		Location targetLocation = new Location("0");
		targetLocation.setLatitude(lat);
		targetLocation.setLongitude(lng);

		for (ArisLog l : allLogs) {
			if (type != null && !l.event_type.contentEquals(type))
				continue;
			if (l.location.distanceTo(targetLocation) > withinDistance)
				continue;
			qty++;
		}
		return qty;
	}

}
