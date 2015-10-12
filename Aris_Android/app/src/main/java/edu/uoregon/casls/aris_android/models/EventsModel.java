package edu.uoregon.casls.aris_android.models;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.uoregon.casls.aris_android.GamePlayActivity;
import edu.uoregon.casls.aris_android.data_objects.Event;
import edu.uoregon.casls.aris_android.data_objects.Item;

/**
 * Created by smorison on 8/20/15.
 */
public class EventsModel extends ARISModel {

	public Collection<Event> events = new LinkedList<>();
	public GamePlayActivity mGamePlayAct;

	public void initContext(GamePlayActivity gamePlayAct) {
		mGamePlayAct = gamePlayAct; // todo: may need leak checking is activity gets recreated.
	}

	public void clearGameData() {
		events.clear();
		n_game_data_received = 0;
	}

	public void requestEvents() {

	}

	public long nGameDataToReceive () {
		return 1;
	}

	public Event eventPackageForId(long object_id) {
		return events.get(object_id);
	}

	public void runEventPackageId(long active_event_package_id) {

	}


	public void requestGameData
	{
		this.requestEvents();
	}
	
	public void eventsReceived(List<Event> newEvents)
	{
		this.updateEvents(newEvents);
	}

	public void updateEvents(List<Event> newEvents)
	{
		Event *newEvent;
		NSNumber *newEventId;
		for(long i = 0; i < newEvents.count; i++)
		{
			newEvent = [newEvents objectAtIndex:i];
			newEventId = [NSNumber numberWithLong:newEvent.event_id];
			if(![events objectForKey:newEventId]) [events setObject:newEvent forKey:newEventId];
		}
		n_game_data_received++;
		_ARIS_NOTIF_SEND_(@"MODEL_EVENTS_AVAILABLE",nil,nil);
		_ARIS_NOTIF_SEND_(@"MODEL_GAME_PIECE_AVAILABLE",nil,nil);
	}

	public void runEventPackageId:(long)event_package_id
	{
		NSArray *es = this.eventsForEventPackageId:event_package_id];
		Event *e;
		for(long i = 0; i < es.count; i++)
		{
			e = es[i];
			//legacy
			if([e.event isEqualToString:@"TAKE_ITEM"])
			[_MODEL_PLAYER_INSTANCES_ takeItemFromPlayer:e.content_id qtyToRemove:e.qty];
			if([e.event isEqualToString:@"GIVE_ITEM"])
			[_MODEL_PLAYER_INSTANCES_ giveItemToPlayer:e.content_id qtyToAdd:e.qty];

			if(!e.event || [e.event isEqualToString:@"NONE"])
			return;

			if([e.event isEqualToString:@"TAKE_ITEM_PLAYER"])
			[_MODEL_PLAYER_INSTANCES_ takeItemFromPlayer:e.content_id qtyToRemove:e.qty];
			if([e.event isEqualToString:@"GIVE_ITEM_PLAYER"])
			[_MODEL_PLAYER_INSTANCES_ giveItemToPlayer:e.content_id qtyToAdd:e.qty];
			if([e.event isEqualToString:@"SET_ITEM_PLAYER"])
			[_MODEL_PLAYER_INSTANCES_ setItemsForPlayer:e.content_id qtyToSet:e.qty];

			if([e.event isEqualToString:@"TAKE_ITEM_GAME"])
			[_MODEL_GAME_INSTANCES_ takeItemFromGame:e.content_id qtyToRemove:e.qty];
			if([e.event isEqualToString:@"GIVE_ITEM_GAME"])
			[_MODEL_GAME_INSTANCES_ giveItemToGame:e.content_id qtyToAdd:e.qty];
			if([e.event isEqualToString:@"SET_ITEM_GAME"])
			[_MODEL_GAME_INSTANCES_ setItemsForGame:e.content_id qtyToSet:e.qty];

			if([e.event isEqualToString:@"TAKE_ITEM_GROuP"])
			[_MODEL_GROUP_INSTANCES_ takeItemFromGroup:e.content_id qtyToRemove:e.qty];
			if([e.event isEqualToString:@"GIVE_ITEM_GROUP"])
			[_MODEL_GROUP_INSTANCES_ giveItemToGroup:e.content_id qtyToAdd:e.qty];
			if([e.event isEqualToString:@"SET_ITEM_GROUP"])
			[_MODEL_GROUP_INSTANCES_ setItemsForGroup:e.content_id qtyToSet:e.qty];

			if([e.event isEqualToString:@"SET_SCENE"])
			[_MODEL_SCENES_ setPlayerScene:[_MODEL_SCENES_ sceneForId:e.content_id]];

			if([e.event isEqualToString:@"SET_GROUP"])
			[_MODEL_GROUPS_ setPlayerGroup:[_MODEL_GROUPS_ groupForId:e.content_id]];

			if([e.event isEqualToString:@"RUN_SCRIPT"])
			{
				runner = [[ARISWebView alloc] initWithDelegate:self];
				runner.userInteractionEnabled = NO;
				[runner loadHTMLString:[NSString stringWithFormat:[ARISTemplate ARISHtmlTemplate], e.script] baseURL:nil];
			}
		}
		[_MODEL_LOGS_ playerRanEventPackageId:event_package_id];
	}

	public void requestEvents()
	{
		[_SERVICES_ fetchEvents();
	}

	public List<Event> eventsForEventPackageId(long event_package_id)
	{
		Event e;
		NSMutableArray *package_events = [[NSMutableArray alloc] init];
		NSArray *allEvents = [events allValues];
		for(long i = 0; i < allEvents.count; i++)
		{
			e = allEvents[i];
			if(e.event_package_id == event_package_id)
			[package_events addObject:e];
		}
		return package_events;
	}

	public List<Event> events()
	{
		return events.values; // allValues];
	}

// null event (id == 0) NOT flyweight!!! (to allow for temporary customization safety)
	public Event eventForId(long event_id)
	{
		if(!event_id) return [[Event alloc] init];
		return [events objectForKey:[NSNumber numberWithLong:event_id]];
	}

// NOT flyweight!!! (because joke objects)
	public EventPackage eventPackageForId(long event_package_id)
	{
		EventPackage ep = new EventPackage();
		ep.event_package_id = event_package_id;
		return ep;
	}

}
