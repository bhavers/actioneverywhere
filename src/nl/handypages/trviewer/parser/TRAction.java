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
package nl.handypages.trviewer.parser;

import java.util.Calendar;
import java.util.Date;

import nl.handypages.trviewer.database.ActionsDbAdapter;

public class TRAction implements Comparable <TRAction>{
	
	public static String stateActionASAP = "actionStateASAP";
	public static String stateActionScheduled = "actionStateScheduled";
	public static String stateActionStateInactive = "actionStateInactive";
	public static String stateActionDelegated = "actionStateDelegated";
	
	
	
	private String id;
	private Date created;
	private String description;
	private String topicIndex;
	private String topicId;
	private String contextIndex;
	private String contextId;
	private String state; // Possible values: actionStateASAP, actionStateScheduled, actionStateInactive, actionStateDelegated
	private String notes;
	private Boolean done;
	private Date modified;
	private Date dueDate;
	private Date scheduledDate;
	private Boolean scheduledRecurring;
	private String delegatedTo;
	private Date chaseDate;
	private String projectId;
	
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Date getCreated() {
		return created;
	}
	public Long getCreatedAsLong() {
		if (created == null) {
			return null;
		}
		return created.getTime();
	}
	public void setCreated(Date created) {
		this.created = created;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getTopicIndex() {
		return topicIndex;
	}
	public void setTopicIndex(String topicIndex) {
		this.topicIndex = topicIndex;
	}
	public String getTopicId() {
		return topicId;
	}
	public void setTopicId(String topicId) {
		this.topicId = topicId;
	}
	/*
	 * This will return the index of the Context, not the id.
	 * See TRContext.getIndex() and .getId() for more information on the difference.
	 */
	public String getContextIndex() {
		return contextIndex;
	}
	public void setContextIndex(String contextIndex) {
		this.contextIndex = contextIndex;
	}
	public String getContextId() {
		return contextId;
	}
	public void setContextId(String contextId) {
		this.contextId = contextId;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}
	public Boolean getDone() {
		return done;
	}
	public String getDoneAsStr() {
		if (done == true) {
			return ActionsDbAdapter.ACTION_TRUE;
		}
		return ActionsDbAdapter.ACTION_FALSE;
	}
	public void setDone(Boolean done) {
		this.done = done;
	}
	public void setDoneAsStr(String done) {
		if (done.equalsIgnoreCase(ActionsDbAdapter.ACTION_TRUE)) {
			this.done = true;
		} else {
			this.done = false;
		}
	}
	public Date getModified() {
		return modified;
	}
	public Long getModifiedAsLong() {
		if (modified == null) {
			return null;
		}
		return modified.getTime();
	}
	public void setModified(Date modified) {
		this.modified = modified;
	}
	public Date getDueDate() {
		return dueDate;
	}
	public Long getDueDateAsLong() {
		if (dueDate == null) {
			return null;
		}
		return dueDate.getTime();
	}
	public String getDueDateStr() {
		if (dueDate == null)
			return "";
		return dueDate.toLocaleString();
	}
	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}
	public Date getChaseDate() {
		return chaseDate;
	}
	public Long getChaseDateAsLong() {
		if (chaseDate == null) {
			return null;
		}
		return chaseDate.getTime();
	}
	public String getChaseDateStr() {
		if (chaseDate == null)
			return "";
		return chaseDate.toLocaleString();
	}
	
	public void setChaseDate(Date chaseDate) {
		this.chaseDate = chaseDate;
	}
	public Date getScheduledDate() {
		return scheduledDate;
	}
	public Long getScheduledDateAsLong() {
		if (scheduledDate == null) {
			return null;
		}
		return scheduledDate.getTime();
	}
	public String getScheduledDateStr() {
		if (scheduledDate == null)
			return "";
		return scheduledDate.toLocaleString();
	}
	public void setScheduledDate(Date scheduledDate) {
		this.scheduledDate = scheduledDate;
	}
	public Boolean getScheduledRecurring() {
		return scheduledRecurring;
	}
	public String getScheduledRecurringAsStr() {
		if (scheduledRecurring != null && scheduledRecurring == true) {
			return ActionsDbAdapter.ACTION_TRUE;
		} else {
			return ActionsDbAdapter.ACTION_FALSE;
		}
	}
	public void setScheduledRecurring(Boolean scheduledRecurring) {
		this.scheduledRecurring = scheduledRecurring;
	}
	public void setScheduledRecurringAsStr(String scheduledRecurring) {
		if (scheduledRecurring.equalsIgnoreCase(ActionsDbAdapter.ACTION_TRUE)) {
			this.scheduledRecurring = true;
		} else {
			this.scheduledRecurring = false;
		}
	}
	public String getDelegatedTo() {
		return delegatedTo;
	}
	public void setDelegatedTo(String delegatedTo) {
		this.delegatedTo = delegatedTo;
	}
	public String getProjectId() {
		return projectId;
	}
	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}
	
	/* Compares TRActions to sort them in the following order
	 * First: from earliest to latest
	 * Second: All non-scheduled actions (asap, inactive) go after the actions schedule for today, but before all the later actions
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(TRAction tra) {
		
		int result = 0;
		
		// Each actions has three attributes that can contain a detail, put them in an array for easy comparison.
		Date sourceActionDate[] = new Date[3];
		sourceActionDate[0] = this.scheduledDate;
		sourceActionDate[1] = this.dueDate;
		sourceActionDate[2] = this.chaseDate;
		
		Date targetActionDate[] = new Date[3];
		targetActionDate[0] = tra.getScheduledDate();
		targetActionDate[1] = tra.getDueDate();
		targetActionDate[2] = tra.getChaseDate();

		// Get todays date, at 23.59.59. All none scheduled actions (asap, inactive) will go after actions that have this date/time.
		// This actually doesn't work because getTimeInMilles doesn't use the 'set' time (you get the Calendar.getInstance() time in Millis.
		// Have to figure this out later. Consequence is that sorting of scheduled action on today might not be happen correctly.
		Calendar todayDate = Calendar.getInstance();
		todayDate.set(Calendar.HOUR_OF_DAY, 23);
		todayDate.set(Calendar.MINUTE, 59);
		todayDate.set(Calendar.SECOND, 59);
		todayDate.get(Calendar.YEAR); // tried with a .get() call to trigger the internal computation, but it doesn't work.
		long today = todayDate.getTimeInMillis();
		//Log.i(MainActivity.TAG, "TodayDate = " + todayDate.getTime().toLocaleString() + "    inMillis = " + Long.toString(today));
		//long today = Calendar.getInstance().getTimeInMillis();
		
		long sourceEarliestDate = 0;
		long targetEarliestDate = 0;
		
		//Log.i(MainActivity.TAG, "Action description: Source = " + this.getDescription() + " --------- Target = " + tra.getDescription());

		// First get the earliest dates from the source (this) and target (tra) TRAction objects.
		for (int i = 0; i < targetActionDate.length; i++) {
			if (sourceActionDate[i] != null) {
				
				if (sourceEarliestDate == 0 || sourceEarliestDate < sourceActionDate[i].getTime()) {
					sourceEarliestDate = sourceActionDate[i].getTime();
				}

			}
			if (targetActionDate[i] != null) {
				
				if (targetEarliestDate == 0 || targetEarliestDate < targetActionDate[i].getTime()) {
					targetEarliestDate = targetActionDate[i].getTime();
				}
			}
		}
		// Log.i(MainActivity.TAG, "Today = " + Long.toString(today) + "   source = " + Long.toString(sourceEarliestDate) + "    target = " + Long.toString(targetEarliestDate));

		// Now determine what to return by comparing source and target.
		if (sourceEarliestDate == 0) {
			sourceEarliestDate = today;
		}
		if (targetEarliestDate == 0) {
			targetEarliestDate = today;
		}
		
		if (targetEarliestDate == sourceEarliestDate) {
			result = 0;
		}
		if (sourceEarliestDate > targetEarliestDate) {
			result = 1;
		} 
		if (sourceEarliestDate < targetEarliestDate) {
			result = -1;
		}
		return result;
	}
}
