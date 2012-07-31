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

import nl.handypages.trviewer.database.TopicsDbAdapter;
import nl.handypages.trviewer.parser.TRTopic;
import android.content.Context;
import android.database.Cursor;

public class TopicHelper {

	
	private Context context;
	private TopicsDbAdapter topicDbAdapter;

	/**
	 * This is a helper class to read and write Actors to the database. 
	 */
	
	public TopicHelper(Context context) {
		super();
		this.context = context;
		topicDbAdapter = new TopicsDbAdapter(this.context);
	}
	public boolean deleteAll() {
		topicDbAdapter.open();
		boolean result = topicDbAdapter.deleteAll();
		topicDbAdapter.close();
		return result;
	}
	
	public void create(ArrayList<TRTopic> topics) {
		topicDbAdapter.open();
		for (int i = 0; i < topics.size(); i++) {
			TRTopic topic = topics.get(i);
			topicDbAdapter.create(topic);
		}
		topicDbAdapter.close();
	}

	public ArrayList<TRTopic> getTopics() {
		
		topicDbAdapter.open();
		ArrayList<TRTopic> topics = new ArrayList<TRTopic>(); 
		
		Cursor cur = topicDbAdapter.fetchAll();
		//startManagingCursor(cur); 
		cur.moveToFirst();
		while (!cur.isAfterLast()) {
			TRTopic topic = new TRTopic();
			topic.setId(cur.getString(cur.getColumnIndexOrThrow(TopicsDbAdapter.KEY_ID)));
			topic.setName(cur.getString(cur.getColumnIndexOrThrow(TopicsDbAdapter.KEY_NAME)));
			if (!cur.getString(cur.getColumnIndexOrThrow(TopicsDbAdapter.KEY_TOPICINDEX)).equalsIgnoreCase("")) {
				topic.setIndex(cur.getInt(cur.getColumnIndexOrThrow(TopicsDbAdapter.KEY_TOPICINDEX)));
			}
			topic.setDescription(cur.getString(cur.getColumnIndexOrThrow(TopicsDbAdapter.KEY_DESCRIPTION)));
			
			topics.add(topic);
			cur.moveToNext();
		}
		//actions.trimToSize(); // not really needed.
		cur.close();
		topicDbAdapter.close();
		return topics;
	}
}
