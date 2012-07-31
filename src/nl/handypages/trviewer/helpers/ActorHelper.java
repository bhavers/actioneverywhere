/*******************************************************************************
 * Copyright (c) 2012 Handypages.nl
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package nl.handypages.trviewer.helpers;
import java.util.ArrayList;
import nl.handypages.trviewer.database.ActorsDbAdapter;
import nl.handypages.trviewer.parser.TRActor;
import android.content.Context;
import android.database.Cursor;

public class ActorHelper {

	
	private Context context;
	private ActorsDbAdapter actorDbAdapter;

	/**
	 * This is a helper class to read and write Actors to the database. 
	 */
	
	public ActorHelper(Context context) {
		super();
		this.context = context;
		actorDbAdapter = new ActorsDbAdapter(this.context);
	}
	public boolean deleteAll() {
		actorDbAdapter.open();
		boolean result = actorDbAdapter.deleteAll();
		actorDbAdapter.close();
		return result;
	}
	
	public void create(ArrayList<TRActor> actors) {
		actorDbAdapter.open();
		for (int i = 0; i < actors.size(); i++) {
			TRActor actor = actors.get(i);
			actorDbAdapter.create(actor);
		}
		actorDbAdapter.close();
	}

	public ArrayList<TRActor> getActors() {
		
		actorDbAdapter.open();
		ArrayList<TRActor> actors = new ArrayList<TRActor>(); 
		
		Cursor cur = actorDbAdapter.fetchAll();
		//startManagingCursor(cur); 
		cur.moveToFirst();
		while (!cur.isAfterLast()) {
			TRActor actor = new TRActor();
			actor.setId(cur.getString(cur.getColumnIndexOrThrow(ActorsDbAdapter.KEY_ID)));
			actor.setName(cur.getString(cur.getColumnIndexOrThrow(ActorsDbAdapter.KEY_NAME)));
			actor.setEmail(cur.getString(cur.getColumnIndexOrThrow(ActorsDbAdapter.KEY_EMAIL)));
			actor.setInactiveAsStr(cur.getString(cur.getColumnIndexOrThrow(ActorsDbAdapter.KEY_INACTIVE)));
			
			actors.add(actor);
			cur.moveToNext();
		}
		//actions.trimToSize(); // not really needed.
		cur.close();
		actorDbAdapter.close();
		return actors;
	}
}
