package edu.uoregon.casls.aris_android.Utilities;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.uoregon.casls.aris_android.GamePlayActivity;
import edu.uoregon.casls.aris_android.data_objects.ArisLog;
import edu.uoregon.casls.aris_android.data_objects.Dialog;
import edu.uoregon.casls.aris_android.data_objects.DialogCharacter;
import edu.uoregon.casls.aris_android.data_objects.DialogOption;
import edu.uoregon.casls.aris_android.data_objects.DialogScript;
import edu.uoregon.casls.aris_android.data_objects.Event;
import edu.uoregon.casls.aris_android.data_objects.Factory;
import edu.uoregon.casls.aris_android.data_objects.Group;
import edu.uoregon.casls.aris_android.data_objects.Instance;
import edu.uoregon.casls.aris_android.data_objects.Item;
import edu.uoregon.casls.aris_android.data_objects.Note;
import edu.uoregon.casls.aris_android.data_objects.NoteComment;
import edu.uoregon.casls.aris_android.data_objects.ObjectTag;
import edu.uoregon.casls.aris_android.data_objects.Overlay;
import edu.uoregon.casls.aris_android.data_objects.Plaque;
import edu.uoregon.casls.aris_android.data_objects.Quest;
import edu.uoregon.casls.aris_android.data_objects.RequirementAndPackage;
import edu.uoregon.casls.aris_android.data_objects.RequirementAtom;
import edu.uoregon.casls.aris_android.data_objects.RequirementRootPackage;
import edu.uoregon.casls.aris_android.data_objects.Scene;
import edu.uoregon.casls.aris_android.data_objects.Tab;
import edu.uoregon.casls.aris_android.data_objects.Tag;
import edu.uoregon.casls.aris_android.data_objects.Trigger;
import edu.uoregon.casls.aris_android.data_objects.User;
import edu.uoregon.casls.aris_android.data_objects.WebPage;

/**
 * Created by smorison on 11/3/15.
 *
 * Centralized hub for most game play model HTTP call server responses
 * Called asynchronously as server responses are received.
 */
public class ResponseHandler { // for now only handles responses with respect to GamePlayActivity.
	
	public transient GamePlayActivity mGamePlayAct;

	public void initContext(GamePlayActivity gamePlayActivity) {
		// reference to GamePlayActivity
		mGamePlayAct = gamePlayActivity;
	}
	
	public void processJsonHttpResponse(String callingReq, String returnStatus, JSONObject jsonReturn) throws JSONException {
		Log.d(AppConfig.LOGTAG, getClass().getSimpleName() + " Server response to Req: " + callingReq + "; data: " + jsonReturn.toString());
		if ((jsonReturn.has("returnCode") && jsonReturn.getLong("returnCode") == 0) && !jsonReturn.has("faultCode")) {
			/*****
			 *  For all the calls for which a response is not fielded for data
			 *****/
			if (Calls.FIRE_AND_FORGET_CALLS.contains(callingReq)) {
				// do nothing.
			}
			/* All calls that will trigger an immediate game update. */
			else if (Calls.TRIGGER_GAME_UPDATE_CALLS.contains(callingReq)) {
				// cause game to update.
				mGamePlayAct.mGame.requestPlayerData();
			}
			/* parseCreateNote*/
			else if (callingReq.equals(Calls.HTTP_CREATE_NOTE)) { // todo: may add update note here handler here too
				if (jsonReturn.has("data")) {
					JSONObject jsonData = jsonReturn.getJSONObject("data");
					Gson gson = new Gson();
					Note note;
					if (jsonData != null)
						note = gson.fromJson(jsonData.toString(), Note.class);
					else
						note = new Note();
					List<Note> newNote = new ArrayList<>(1);
					newNote.add(note); // subsequent calls want the new item to be in a list already. So be it.
					mGamePlayAct.mDispatch.services_note_received(newNote);  //    _ARIS_NOTIF_SEND_(@"SERVICES_NOTE_COMMENT_RECEIVED", nil, @{@"note_comment":noteComment});
				}
			}
			/* parseCreateNoteComment or parseUpdateNoteComment*/
			else if (callingReq.equals(Calls.HTTP_CREATE_NOTE_COMMENT)
					|| callingReq.equals(Calls.HTTP_UPDATE_NOTE_COMMENT)) {
				if (jsonReturn.has("data")) {
					JSONObject jsonData = jsonReturn.getJSONObject("data");
					Gson gson = new Gson();
					NoteComment noteComment;
					if (jsonData != null)
						noteComment = gson.fromJson(jsonData.toString(), NoteComment.class);
					else
						noteComment = new NoteComment();
					List<NoteComment> newNoteComments = new ArrayList<>(1);
					newNoteComments.add(noteComment); // subsequent calls want the new item to be in a list already. So be it.
					mGamePlayAct.mDispatch.services_note_comment_received(newNoteComments);  //    _ARIS_NOTIF_SEND_(@"SERVICES_NOTE_COMMENT_RECEIVED", nil, @{@"note_comment":noteComment});
				}
			}
			/* parseDialogCharacters */
			else if (callingReq.equals(Calls.HTTP_GET_DIALOG_CHARS_4_GAME)) {
				if (jsonReturn.has("data")) {
					JSONArray jsonData = jsonReturn.getJSONArray("data");
					Gson gson = new Gson();
					List<DialogCharacter> dialogCharacters = new LinkedList<>();
					for (int i = 0; i < jsonData.length(); i++) {
						String dataStr = jsonData.getJSONObject(i).toString();
						DialogCharacter dialogChar = gson.fromJson(dataStr, DialogCharacter.class);
						//populate hashmap as dialogChars_id, DialogCharacter Obj>
						dialogCharacters.add(dialogChar);
					}
					mGamePlayAct.mDispatch.services_dialog_characters_received(dialogCharacters); // _ARIS_NOTIF_SEND_(@"SERVICES_DIALOG_CHARACTERS_RECEIVED", nil, @{@"dialogCharacters":dialogCharacters});
				}
			}
			/* parseDialogScripts */
			else if (callingReq.equals(Calls.HTTP_GET_DIALOG_SCRIPTS_4_GAME)) {
				if (jsonReturn.has("data")) {
					JSONArray jsonData = jsonReturn.getJSONArray("data");
					Gson gson = new Gson();
					List<DialogScript> dialogScripts = new LinkedList<>();
					for (int i = 0; i < jsonData.length(); i++) {
						String dataStr = jsonData.getJSONObject(i).toString();
						DialogScript dialogScript = gson.fromJson(dataStr, DialogScript.class);
						//populate hashmap as dialogScript_id, DialogScript Obj>
						dialogScripts.add(dialogScript);
					}
					mGamePlayAct.mDispatch.services_dialog_scipts_received(dialogScripts); // _ARIS_NOTIF_SEND_(@"SERVICES_DIALOG_SCRIPTS_RECEIVED", nil, @{@"dialogScripts":dialogScripts});
				}
			}
			/* parseDialogOptions */
			else if (callingReq.equals(Calls.HTTP_GET_DIALOG_OPTNS_4_GAME)) {
				if (jsonReturn.has("data")) {
					JSONArray jsonData = jsonReturn.getJSONArray("data");
					Gson gson = new Gson();
					List<DialogOption> dialogOptions = new LinkedList<>();
					for (int i = 0; i < jsonData.length(); i++) {
						String dataStr = jsonData.getJSONObject(i).toString();
						DialogOption dialogOption = gson.fromJson(dataStr, DialogOption.class);
						//populate hashmap as dialog_option_id, DialogOption Obj>
						dialogOptions.add(dialogOption);
					}
					mGamePlayAct.mDispatch.services_dialog_options_received(dialogOptions);
				}
			}
			/* parseDialogs */
			else if (callingReq.equals(Calls.HTTP_GET_DIALOGS_4_GAME)) {
				if (jsonReturn.has("data")) {
					JSONArray jsonData = jsonReturn.getJSONArray("data");
					Gson gson = new Gson();
					List<Dialog> dialogs = new ArrayList<>();
					for (int i = 0; i < jsonData.length(); i++) {
						String dataStr = jsonData.getJSONObject(i).toString();
						Dialog dialog = gson.fromJson(dataStr, Dialog.class);
						//populate hashmap as dialog_id, Dialog Obj>
						dialogs.add(dialog);
					}
					mGamePlayAct.mDispatch.services_dialog_received(dialogs); //_ARIS_NOTIF_SEND_(@"SERVICES_DIALOGS_RECEIVED", nil, @{@"dialogs":dialogs});
				}
			}
			/* parseEvents */
			else if (callingReq.equals(Calls.HTTP_GET_EVENTS_4_GAME)) {
				if (jsonReturn.has("data")) {
					JSONArray jsonData = jsonReturn.getJSONArray("data");
					Gson gson = new Gson();
					List<Event> events = new LinkedList<>();
					for (int i = 0; i < jsonData.length(); i++) {
						String dataStr = jsonData.getJSONObject(i).toString();
						Event event = gson.fromJson(dataStr, Event.class);
						//populate hashmap as event_id, Event Obj>
						events.add(event);
					}
					mGamePlayAct.mDispatch.services_events_received(events);
				}
			}
			/* parseFactories */
			else if (callingReq.equals(Calls.HTTP_GET_FACTORIES_4_GAME)) {
				if (jsonReturn.has("data")) {
					JSONArray jsonData = jsonReturn.getJSONArray("data");
					Gson gson = new Gson();
					List<Factory> factories = new LinkedList<>();
					for (int i = 0; i < jsonData.length(); i++) {
						String dataStr = jsonData.getJSONObject(i).toString();
						Factory factory = gson.fromJson(dataStr, Factory.class);
						//populate hashmap as factory_id, Factory Obj>
						factories.add(factory);
					}
					mGamePlayAct.mDispatch.services_factories_received(factories);
				}
			}
			/* parsePlayerGroup */
			else if (callingReq.equals(Calls.HTTP_GET_GROUP_4_PLAYER)) {
				if (jsonReturn.has("data")) {
					Group group;
					if (!jsonReturn.isNull("data")) {
						JSONObject jsonData = jsonReturn.getJSONObject("data");
						group = new Gson().fromJson(jsonData.toString(), Group.class);
					}
					else
						group = new Group(); // set attribs to empty values

					mGamePlayAct.mDispatch.services_player_group_received(group);
//					mGamePlayAct.mGame.groupsModel.groupsReceived(group);
				}
			}
			/* parseGroups */
			else if (callingReq.equals(Calls.HTTP_GET_GROUPS_4_GAME)) {
				if (jsonReturn.has("data")) {
					JSONArray jsonData = jsonReturn.getJSONArray("data");
					Gson gson = new Gson();
					List<Group> groups = new ArrayList<>();
					for (int i = 0; i < jsonData.length(); i++) {
						String dataStr = jsonData.getJSONObject(i).toString();
						Group group = gson.fromJson(dataStr, Group.class);
						groups.add(group);
					}
					mGamePlayAct.mDispatch.services_groups_received(groups);
				}
			}
			/* parseInstances */
			else if (callingReq.equals(Calls.HTTP_GET_INSTANCES_4_GAME)) {
				if (jsonReturn.has("data")) {
					JSONArray jsonData = jsonReturn.getJSONArray("data");
					Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").create();
					List<Instance> instances = new LinkedList<>();
					for (int i = 0; i < jsonData.length(); i++) {
						String dataStr = jsonData.getJSONObject(i).toString();
						Instance instance = gson.fromJson(dataStr, Instance.class);
						//populate hashmap as instances_id, Instance Obj>
						instances.add(instance);
					}
					mGamePlayAct.mDispatch.services_instances_received(instances);
				}
			}
			/* parsePlayerInstances */
			else if (callingReq.equals(Calls.HTTP_GET_INSTANCES_4_PLAYER)) {
				if (jsonReturn.has("data")) {
					JSONArray jsonData = jsonReturn.getJSONArray("data");
					Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").create();
					List<Instance> instances = new LinkedList<>();
					for (int i = 0; i < jsonData.length(); i++) {
						String dataStr = jsonData.getJSONObject(i).toString();
						Instance instance = gson.fromJson(dataStr, Instance.class);
						//populate hashmap as instances_id, Instance Obj>
						instances.add(instance);
					}
					mGamePlayAct.mDispatch.services_player_instances_received(instances);
				}
			}
			/* parseItems */
			else if (callingReq.equals(Calls.HTTP_GET_ITEMS_4_GAME)) {
				if (jsonReturn.has("data")) {
					JSONArray jsonData = jsonReturn.getJSONArray("data");
					Gson gson = new Gson();
					List<Item> newItems = new ArrayList<>();
					for (int i = 0; i < jsonData.length(); i++) {
						String dataStr = jsonData.getJSONObject(i).toString();
						Item item = gson.fromJson(dataStr, Item.class);
						newItems.add(item);
					}
					mGamePlayAct.mGame.itemsModel.itemsReceived(newItems);
				}
			}
			/* parsePlayerLogs */
			else if (callingReq.equals(Calls.HTTP_GET_LOGS_4_PLAYER)) {
				if (jsonReturn.has("data")) {
					JSONArray jsonData = jsonReturn.getJSONArray("data");
					Gson gson = new Gson();
					List<ArisLog> newLogs = new ArrayList<>();
					for (int i = 0; i < jsonData.length(); i++) {
						String dataStr = jsonData.getJSONObject(i).toString();
						ArisLog log = gson.fromJson(dataStr, ArisLog.class);
						log.initGeos();
						newLogs.add(log);
					}
					mGamePlayAct.mDispatch.services_player_logs_received(newLogs); //_ARIS_NOTIF_SEND_(@"SERVICES_PLAYER_LOGS_RECEIVED", nil, @{@"logs":logs});
				}
			}
			/* parseMedia */
			else if (callingReq.equals(Calls.HTTP_GET_MEDIA)) {
				if (jsonReturn.has("data")) {
					JSONObject jsonData = jsonReturn.getJSONObject("data");
					Gson gson = new Gson();
					List<Map<String, String>> rawMediaArr = new LinkedList<>(); // List will hold only one element, but facilitates downstream handling of data
					String dataStr = jsonData.toString();
					Map<String, String> aMediaRec = gson.fromJson(dataStr, new TypeToken<HashMap<String, Object>>() {}.getType());
					rawMediaArr.add(aMediaRec);
					mGamePlayAct.mDispatch.services_media_received(rawMediaArr); //_ARIS_NOTIF_SEND_(@"SERVICES_MEDIA_RECEIVED", nil, @{@"media":mediaDict}); // fakes an entire list and does same as fetching all media
				}
			}
			/* parseMedias */
			else if (callingReq.equals(Calls.HTTP_GET_MEDIA_4_GAME)) {
				if (jsonReturn.has("data")) {
					JSONArray jsonData = jsonReturn.getJSONArray("data");
					Gson gson = new Gson();
					List<Map<String, String>> rawMediaArr = new LinkedList<>();
					for (int i = 0; i < jsonData.length(); i++) {
						String dataStr = jsonData.getJSONObject(i).toString();
						Map<String, String> aMediaRec = gson.fromJson(dataStr, new TypeToken<HashMap<String, Object>>() {}.getType());
						//populate List with this Key->Val pair set. (Map)
						rawMediaArr.add(aMediaRec);
					}
					//note that this intentionally only sends the dictionaries, not fully populated Media objects
					mGamePlayAct.mDispatch.services_medias_received(rawMediaArr); // _ARIS_NOTIF_SEND_(@"SERVICES_MEDIAS_RECEIVED", nil, @{@"medias":mediaDicts}); // fakes an entire list and does same as fetching all media
				}
			}
			/* parseNoteComments */
			else if (callingReq.equals(Calls.HTTP_GET_NOTE_COMMENTS_4_GAME)) {
				if (jsonReturn.has("data")) {
					JSONArray jsonData = jsonReturn.getJSONArray("data");
					Gson gson = new Gson();
					List<NoteComment> noteComments = new LinkedList<>();
					for (int i = 0; i < jsonData.length(); i++) {
						String dataStr = jsonData.getJSONObject(i).toString();
						NoteComment noteCmnt = gson.fromJson(dataStr, NoteComment.class);
						//populate hashmap as Note_comment_id, NoteComment Obj>
						noteComments.add(noteCmnt);
					}
					mGamePlayAct.mDispatch.services_note_comments_received(noteComments);
				}
			}
			/* parseNotes */
			else if (callingReq.equals(Calls.HTTP_GET_NOTES_4_GAME)) {
				if (jsonReturn.has("data")) {
					JSONArray jsonData = jsonReturn.getJSONArray("data");
					Gson gson = new Gson();
					List<Note> notes = new LinkedList<>();
					for (int i = 0; i < jsonData.length(); i++) {
						String dataStr = jsonData.getJSONObject(i).toString();
						Note note = gson.fromJson(dataStr, Note.class);
						//populate hashmap as Note_comment_id, NoteComment Obj>
						notes.add(note);
					}
					mGamePlayAct.mDispatch.services_notes_received(notes);
				}
			}
			/* parseObjectTags */
			else if (callingReq.equals(Calls.HTTP_GET_OBJ_TAGS_4_GAME)) {
				if (jsonReturn.has("data")) {
					JSONArray jsonData = jsonReturn.getJSONArray("data");
					Gson gson = new Gson();
					List<ObjectTag> objectTags = new LinkedList<>();
					for (int i = 0; i < jsonData.length(); i++) {
						String dataStr = jsonData.getJSONObject(i).toString();
						ObjectTag objTag = gson.fromJson(dataStr, ObjectTag.class);
						//populate list
						objectTags.add(objTag);
					}
					mGamePlayAct.mDispatch.services_object_tags_received(objectTags);
				}
			}
			/* parseOverlays */
			else if (callingReq.equals(Calls.HTTP_GET_OVERLAYS_4_GAME)) {
				if (jsonReturn.has("data")) {
					JSONArray jsonData = jsonReturn.getJSONArray("data");
					Gson gson = new Gson();
					List<Overlay> overlays = new LinkedList<>();
					for (int i = 0; i < jsonData.length(); i++) {
						String dataStr = jsonData.getJSONObject(i).toString();
						Overlay overlay = gson.fromJson(dataStr, Overlay.class);
						//populate list>
						overlays.add(overlay);
					}
					mGamePlayAct.mDispatch.services_overlays_received(overlays);
				}
			}
			/* parsePlayerOverlays */
			else if (callingReq.equals(Calls.HTTP_GET_OVERLAYS_4_PLAYER)) {
				if (jsonReturn.has("data")) {
					JSONArray jsonData = jsonReturn.getJSONArray("data");
					Gson gson = new Gson();
					List<Overlay> overlays = new LinkedList<>();
					for (int i = 0; i < jsonData.length(); i++) {
						String dataStr = jsonData.getJSONObject(i).toString();
						Overlay overlay = gson.fromJson(dataStr, Overlay.class);
						//populate list
						overlays.add(overlay);
					}
					mGamePlayAct.mDispatch.services_player_overlays_received(overlays); //_ARIS_NOTIF_SEND_(@"SERVICES_PLAYER_OVERLAYS_RECEIVED", nil, @{@"overlays":overlays});
				}
			}
			/* parsePlaques */
			else if (callingReq.equals(Calls.HTTP_GET_PLAQUES_4_GAME)) {
				if (jsonReturn.has("data")) {
					JSONArray jsonData = jsonReturn.getJSONArray("data");
					Gson gson = new Gson();
					List<Plaque> plaques = new ArrayList<>();
					for (int i = 0; i < jsonData.length(); i++) {
						String dataStr = jsonData.getJSONObject(i).toString();
						Plaque plaque = gson.fromJson(dataStr, Plaque.class);
						//populate list
						plaques.add(plaque);
						mGamePlayAct.mGame.plaquesModel.plaques.put(plaque.plaque_id, plaque);
					}
					mGamePlayAct.mGame.plaquesModel.plaquesReceived(plaques);
				}
			}
			/* parsePlayerPlayedGame <-- this is handled in GameCoverPageActivity with discrete call. May need to unify this if call elsewhere.*/
//			else if (callingReq.equals(Calls.HTTP_GET_PLAYER_PLAYED_GAME)) {
//				if (jsonReturn.has("data")) {
//					Game g; // I don't think this call returns an entire Game object. I think it just has a "has_played" json field. See ResponceHander in GameCoverPageActivity
//					if (!jsonReturn.isNull("data")) {
//						JSONObject jsonData = jsonReturn.getJSONObject("data");
//						g = new Gson().fromJson(jsonData.toString(), Game.class);
//					}
//					else
//						g = new Game(); // set attribs to empty values
//
//					mGamePlayAct.mDispatch.services_player_played_game_received(g);
//				}
//			}
			/* parseQuests */
			else if (callingReq.equals(Calls.HTTP_GET_QUESTS_4_GAME)) {
				if (jsonReturn.has("data")) {
					JSONArray jsonData = jsonReturn.getJSONArray("data");
					Gson gson = new Gson();
					List<Quest> quests = new LinkedList<>();
					for (int i = 0; i < jsonData.length(); i++) {
						String dataStr = jsonData.getJSONObject(i).toString();
						Quest quest = gson.fromJson(dataStr, Quest.class);
						//populate hashmap as quest_id, Quest Obj>
						quests.add(quest);
					}
					mGamePlayAct.mDispatch.services_quests_received(quests);
				}
			}
			/* parsePlayerQuests */
			else if (callingReq.equals(Calls.HTTP_GET_QUESTS_4_PLAYER)) {
				if (jsonReturn.has("data")) {
					JSONObject jsonData = jsonReturn.getJSONObject("data");
					Map<String, List<Quest>> playerQuests = new LinkedHashMap<>();
					List<Quest> activeQuests = new ArrayList<>();
					playerQuests.put("active", activeQuests);
					List<Quest> completeQuests = new ArrayList<>();
					playerQuests.put("complete", completeQuests);
					Gson gson = new Gson();

					JSONArray jsonActive = jsonData.getJSONArray("active");
					for (int i = 0; i < jsonActive.length(); i++) {
						String dataStr = jsonActive.getJSONObject(i).toString();
						Quest quest = gson.fromJson(dataStr, Quest.class);
						//populate hashmap as "active", Quest Obj>
						playerQuests.get("active").add(quest);
					}
					JSONArray jsonComplete = jsonData.getJSONArray("complete");
					for (int i = 0; i < jsonComplete.length(); i++) {
						String dataStr = jsonComplete.getJSONObject(i).toString();
						Quest quest = gson.fromJson(dataStr, Quest.class);
						//populate hashmap as "active", Quest Obj>
						playerQuests.get("complete").add(quest);
					}
					mGamePlayAct.mDispatch.services_player_quests_received(playerQuests);
				}
			}
			/* parseRequirementAndPackages */
			else if (callingReq.equals(Calls.HTTP_GET_REQMNT_AND_PKGS_4_GAME)) {
				if (jsonReturn.has("data")) {
					JSONArray jsonData = jsonReturn.getJSONArray("data");
					Gson gson = new Gson();
					List<RequirementAndPackage> reqAnds = new LinkedList<>();
					for (int i = 0; i < jsonData.length(); i++) {
						String dataStr = jsonData.getJSONObject(i).toString();
						RequirementAndPackage reqAnd = gson.fromJson(dataStr, RequirementAndPackage.class);
						//populate hashmap as quest_id, Quest Obj>
						reqAnds.add(reqAnd);
					}
					mGamePlayAct.mDispatch.services_requirement_and_packages_received(reqAnds);
				}
			}
			/* parseRequirementAtoms */
			else if (callingReq.equals(Calls.HTTP_GET_REQMNT_ATOMS_4_GAME)) {
				if (jsonReturn.has("data")) {
					JSONArray jsonData = jsonReturn.getJSONArray("data");
					Gson gson = new Gson();
					List<RequirementAtom> reqAtoms = new LinkedList<>();
					for (int i = 0; i < jsonData.length(); i++) {
						String dataStr = jsonData.getJSONObject(i).toString();
						RequirementAtom reqAtom = gson.fromJson(dataStr, RequirementAtom.class);
						//populate hashmap as quest_id, Quest Obj>
						reqAtoms.add(reqAtom);
					}
					mGamePlayAct.mDispatch.services_requirement_atoms_received(reqAtoms);
				}
			}
			/* parseRequirementRootPackages */
			else if (callingReq.equals(Calls.HTTP_GET_REQMNT_ROOT_PKGS_4_GAME)) {
				if (jsonReturn.has("data")) {
					JSONArray jsonData = jsonReturn.getJSONArray("data");
					Gson gson = new Gson();
					List<RequirementRootPackage> reqRoots = new LinkedList<>();
					for (int i = 0; i < jsonData.length(); i++) {
						String dataStr = jsonData.getJSONObject(i).toString();
						RequirementRootPackage reqRoot = gson.fromJson(dataStr, RequirementRootPackage.class);
						//populate hashmap as quest_id, Quest Obj>
						reqRoots.add(reqRoot);
					}
					mGamePlayAct.mDispatch.services_requirement_root_packages_received(reqRoots);
				}
			}
			/* parsePlayerScene */
			else if (callingReq.equals(Calls.HTTP_GET_SCENE_4_PLAYER)) {
				if (jsonReturn.has("data")) {
					JSONObject jsonData = jsonReturn.getJSONObject("data");
					Gson gson = new Gson();
					Scene s;
					if (jsonData != null)
						s = gson.fromJson(jsonData.toString(), Scene.class);
					else
						s = new Scene(); //[[Scene alloc] init];
					mGamePlayAct.mDispatch.services_player_scene_received(s);  //_ARIS_NOTIF_SEND_(@"SERVICES_PLAYER_SCENE_RECEIVED", nil, @{@"scene":s});
				}
			}
			/* parseScenes */
			else if (callingReq.contentEquals(Calls.HTTP_GET_SCENES_4_GAME)) { // parse array of returns scenes
				// Response looks like this:
				// {"data":[{"scene_id":"98","game_id":"78","name":"James J Hill","description":"","editor_x":"0","editor_y":"0"}],"returnCode":0,"returnCodeDescription":null}
				try {
					// process incoming json data
					if (jsonReturn.has("data")) {
						JSONArray jsonScenes = jsonReturn.getJSONArray("data");
						Gson gson = new Gson();
						List<Scene> scenes = new ArrayList<>();
						for (int i = 0; i < jsonScenes.length(); i++) {
							String jsonSceneStr = jsonScenes.getJSONObject(i).toString();
							Scene scene = gson.fromJson(jsonSceneStr, Scene.class);
							scenes.add(scene);
						}
						mGamePlayAct.mGame.scenesModel.scenesReceived(scenes);
					}
				} catch (JSONException e) {
					Log.e(AppConfig.LOGTAG, getClass().getSimpleName() + "Failed while parsing returning JSON from request:" + callingReq + " Error reported was: " + e.getCause());
					e.printStackTrace();
				}
			}
			/* parseTabs */
			else if (callingReq.equals(Calls.HTTP_GET_TABS_4_GAME)) { // returns array of teh items for the game mode drawer
				if (jsonReturn.has("data")) {
					JSONArray jsonData = jsonReturn.getJSONArray("data");
					Gson gson = new Gson();
					List<Tab> tabs = new LinkedList<>();
					for (int i = 0; i < jsonData.length(); i++) {
						String dataStr = jsonData.getJSONObject(i).toString();
						Tab tab = gson.fromJson(dataStr, Tab.class);
						//populate hashmap as tab_id, Tab Obj>
						tabs.add(tab);
					}
					mGamePlayAct.mDispatch.services_tabs_received(tabs);
				}
			}
			/* parsePlayerTabs */
			else if (callingReq.equals(Calls.HTTP_GET_TABS_4_PLAYER)) { // returns array of teh items for the game mode drawer
				if (jsonReturn.has("data")) {
					JSONArray jsonData = jsonReturn.getJSONArray("data");
					Gson gson = new Gson();
					List<Tab> tabs = new LinkedList<>();
					for (int i = 0; i < jsonData.length(); i++) {
						String dataStr = jsonData.getJSONObject(i).toString();
						Tab tab = gson.fromJson(dataStr, Tab.class);
						//populate hashmap as tab_id, Tab Obj>
						tabs.add(tab);
					}
					mGamePlayAct.mDispatch.services_player_tabs_received(tabs); // SERVICES_PLAYER_TABS_RECEIVED
				}
			}
			/* parseTags */
			else if (callingReq.equals(Calls.HTTP_GET_TAGS_4_GAME)) {
				if (jsonReturn.has("data")) {
					JSONArray jsonData = jsonReturn.getJSONArray("data");
					Gson gson = new Gson();
					List<Tag> tags = new LinkedList<>();
					for (int i = 0; i < jsonData.length(); i++) {
						String dataStr = jsonData.getJSONObject(i).toString();
						Tag tag = gson.fromJson(dataStr, Tag.class);
						//populate hashmap as tag_id, Tag Obj>
						tags.add(tag);
					}
					mGamePlayAct.mDispatch.services_tags_received(tags);
				}
			}
			/* parseTrigger */
			else if (callingReq.equals(Calls.HTTP_GET_TRIGGER)) {
				if (jsonReturn.has("data")) {
					JSONObject jsonData = jsonReturn.getJSONObject("data");
					Gson gson = new Gson();
					Trigger trigger;
					if (jsonData != null)
						trigger = gson.fromJson(jsonData.toString(), Trigger.class);
					else
						trigger = new Trigger(); // to prevent NPE but will still result in bad wrongness behaviourally
					mGamePlayAct.mDispatch.services_trigger_received(trigger);
				}
			}
			/* parseTriggers */
			else if (callingReq.equals(Calls.HTTP_GET_TRIGGERS_4_GAME)) {
				if (jsonReturn.has("data")) {
					JSONArray jsonData = jsonReturn.getJSONArray("data");
					Gson gson = new Gson();
					List<Trigger> triggers = new LinkedList<>();
					for (int i = 0; i < jsonData.length(); i++) {
						String dataStr = jsonData.getJSONObject(i).toString();
						Trigger trigger = gson.fromJson(dataStr, Trigger.class);
						//populate hashmap as trigger_id, Quest Obj>
						trigger.setLocationFromExistingLatLng(); // sets the location member var from lat and long.
						triggers.add(trigger);
					}
					mGamePlayAct.mDispatch.services_triggers_received(triggers);
				}
			}
			/* parsePlayerTriggers */
			else if (callingReq.equals(Calls.HTTP_GET_TRIGGERS_4_PLAYER)) {
//				Log.d(AppConfig.LOGTAG+AppConfig.LOGTAG_D1, getClass().getSimpleName() + " HTTP_GET_TRIGGERS_4_PLAYER " + callingReq + "; data: " + jsonReturn.toString());
				if (jsonReturn.has("data")) {
					JSONArray jsonData = jsonReturn.getJSONArray("data");
					Gson gson = new Gson();
					List<Trigger> triggers = new LinkedList<>();
					for (int i = 0; i < jsonData.length(); i++) {
						String dataStr = jsonData.getJSONObject(i).toString();
						Trigger trigger = gson.fromJson(dataStr, Trigger.class);
						//populate hashmap as trigger_id, Quest Obj>
						triggers.add(trigger);
					}
//					Log.d(AppConfig.LOGTAG + AppConfig.LOGTAG_D1 + AppConfig.LOGTAG_D2, getClass().getSimpleName() + " Sending mDispatch: services_player_triggers_received() Triggers size = " + triggers.size());
					mGamePlayAct.mDispatch.services_player_triggers_received(triggers);
				}
			}
			/* parseUser */
			else if (callingReq.equals(Calls.HTTP_GET_USER)) {
				if (jsonReturn.has("data")) {
					JSONObject jsonData = jsonReturn.getJSONObject("data");
					Gson gson = new Gson();
					User user;
					if (jsonData != null)
						user = gson.fromJson(jsonData.toString(), User.class);
					else
						user = new User(); // to prevent NPE but will still result in bad wrongness behaviourally
					mGamePlayAct.mDispatch.services_user_received(user);
				}
			}
			/* parseUsers */
			else if (callingReq.equals(Calls.HTTP_GET_USERS_4_GAME)) {
				if (jsonReturn.has("data")) {
					JSONArray jsonData = jsonReturn.getJSONArray("data");
					Gson gson = new Gson();
					for (int i = 0; i < jsonData.length(); i++) {
						String dataStr = jsonData.getJSONObject(i).toString();
						User user = gson.fromJson(dataStr, User.class);
						//populate hashmap as user_id, User Obj>
						mGamePlayAct.mGameUsers.put(user.user_id, user);
					}
					mGamePlayAct.mDispatch.services_users_received(mGamePlayAct.mGameUsers); // _ARIS_NOTIF_SEND_(@"SERVICES_USERS_RECEIVED", nil, @{@"users":users});

				}
			}
			/* parseUpdateNoteComment (see parseCreateNoteComment above) */

			/* parseWebPages */
			else if (callingReq.equals(Calls.HTTP_GET_WEB_PAGES_4_GAME)) {
				if (jsonReturn.has("data")) {
					JSONArray jsonData = jsonReturn.getJSONArray("data");
					Gson gson = new Gson();
					List<WebPage> webPages = new LinkedList<>();
					for (int i = 0; i < jsonData.length(); i++) {
						String dataStr = jsonData.getJSONObject(i).toString();
						WebPage webpage = gson.fromJson(dataStr, WebPage.class);
						//populate hashmap as dialog_id, Dialog Obj>
						webPages.add(webpage);
					}
					mGamePlayAct.mDispatch.services_web_pages_received(webPages);
				}
			}

			/******
			* TOUCH Calls
			*******/
			/* parseGameItemTouch */
			else if (callingReq.equals(Calls.HTTP_TOUCH_ITEMS_4_GAME)) {
				mGamePlayAct.mDispatch.services_game_instances_touched(); //_ARIS_NOTIF_SEND_(@"SERVICES_GAME_INSTANCES_TOUCHED", nil, nil);
			}
			/* parseGroupItemTouch */
			else if (callingReq.equals(Calls.HTTP_TOUCH_ITEMS_4_GROUPS)) {
				mGamePlayAct.mDispatch.services_group_instances_touched(); //_ARIS_NOTIF_SEND_(@"SERVICES_GROUP_INSTANCES_TOUCHED", nil, nil);
			}
			/* parsePlayerItemTouch */
			else if (callingReq.equals(Calls.HTTP_TOUCH_ITEMS_4_PLAYER)) {
				mGamePlayAct.mDispatch.services_player_instances_touched(); //_ARIS_NOTIF_SEND_(@"SERVICES_PLAYER_INSTANCES_TOUCHED", nil, nil);
			}
			/* parseGroupTouch */
			else if (callingReq.equals(Calls.HTTP_TOUCH_GROUP_4_PLAYER)) {
				mGamePlayAct.mDispatch.services_group_touched(); //_ARIS_NOTIF_SEND_(@"SERVICES_GROUP_TOUCHED", nil, nil);
			}
			/* parseSceneTouch */
			else if (callingReq.equals(Calls.HTTP_TOUCH_SCENE_4_PLAYER)) {
				mGamePlayAct.mDispatch.services_scene_touched(); //_ARIS_NOTIF_SEND_(@"SERVICES_SCENE_TOUCHED", nil, nil);
			}

			else if (callingReq.equals("")) { // stub
				if (jsonReturn.has("data")) {
					JSONObject jsonData = jsonReturn.getJSONObject("data");
				}
			}
			else { // unhandled calling Request; Many calls require no response handler. (eg, log... set...)
				Log.e(AppConfig.LOGTAG, getClass().getSimpleName() + "AsyncHttpClient returned successfully but with unhandled server callingReq: " + callingReq);
//				Toast t = Toast.makeText(mGamePlayAct.getApplicationContext(), "There was a problem receiving data from the server. Please try again, later.",
//						Toast.LENGTH_SHORT);
//				t.setGravity(Gravity.CENTER, 0, 0);
//				t.show();

			}
		}
		else { // server denial. Game probably incomplete, possibly unplayable. If the game/player/main counting logic works, the game shouldn't start anyway.
			// todo: Probably need to alert user;
			int returnCode = 0;
			String faultString = "Fault Not Specified by ARIS server.";
			String faultDetail = "Details Not Specified by ARIS server.";
			String faultCode = "Code Not Specified by ARIS server.";
			// why did we fail?
			if (jsonReturn.has("returnCode") && !jsonReturn.isNull("returnCode")) { // General HTTP failure.
				returnCode = jsonReturn.getInt("returnCode");
				Log.e(AppConfig.LOGTAG, getClass().getSimpleName() + " - Server request " + callingReq + " failed; server returned code: " + returnCode);
			}
			else { // ARIS server took issue with the API requested.
				if (jsonReturn.has("faultString") && !jsonReturn.isNull("faultString")) {
					faultString = jsonReturn.getString("faultString");
				}
				if (jsonReturn.has("faultDetail") && !jsonReturn.isNull("faultDetail")) {
					faultDetail = jsonReturn.getString("faultDetail");
				}
				if (jsonReturn.has("faultCode") && !jsonReturn.isNull("faultCode")) {
					faultCode = jsonReturn.getString("faultCode");
				}

				Log.e(AppConfig.LOGTAG, getClass().getSimpleName() + " - Server request " + callingReq + " failed; "
						+ "\nFault Message: " + faultString
						+ "\nFault Detail: " + faultDetail
						+ "\nFault Code: " + faultCode
						+ "\nPlayer Id: " + mGamePlayAct.mPlayer.user_id
						+ "\nGame Id: " + mGamePlayAct.mGame.game_id);
			}
		}
	}

}
