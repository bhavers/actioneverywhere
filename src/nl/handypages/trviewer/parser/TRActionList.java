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

import java.util.ArrayList;

import nl.handypages.trviewer.database.FilterActionDateDbAdapter;

public class TRActionList implements Comparable<TRActionList> {
	
	public static final int CUSTOM_TRUE = 1;
	public static final int CUSTOM_FALSE = 0;
	public static final int FILTER_DONE_ALL = 0;
	public static final int FILTER_DONE_DONE = 1;
	public static final int FILTER_DONE_TODO = 2;
	
	// FILTER_FROMTO maps on values in field 5 of filterActionFrom and field 6 filterActionTo
	public static final int FILTER_FROMTO_NOT_SPECIFIED = -11;
	public static final int FILTER_FROMTO_DAYS = 1;
	public static final int FILTER_FROMTO_DATE = 2;
	public static final int FILTER_FROMTO_WEEKS_FROM_NOW = 5;
	public static final int FILTER_FROMTO_WEEKS_TO_NOW = 6;
	
	// FILTER_FROM maps on values in field 1 of filterActionFrom
	public static final int FILTER_FROM_START_OF_NEXT_WEEK = 1;
	public static final int FILTER_FROM_START_OF_THIS_WEEK = 0;
	public static final int FILTER_FROM_START_OF_LAST_WEEK = -1;
	
	// FILTER_TO maps on values in field 1 of filterActionTo
	public static final int FILTER_TO_END_OF_LAST_WEEK = -1;
	public static final int FILTER_TO_END_OF_THIS_WEEK = 0;
	public static final int FILTER_TO_END_OF_NEXT_WEEK = 1;
	
	private String name;
	
	private Boolean custom; 
	
	private int weight;
	/* Mapping for weight (the word 'order' can not be used in sqlite as it is a reserved word).
	 * The index of the action list /screens/screen, starts at 1.
	 */

	private String filterDone = "0";
	/* Mapping for Done.
	 * Can be one of the following:
	 * All = 0 (default; set in constructor)
	 * Done = 1
	 * To do = 2
	 */
	
	private ArrayList<String> filterTopicIds;
	/* Mapping for topics
	 * All = all (TRTopic.LIST_ALL_ID = TRParser.REVIEWACTIONS_FILTER_TOPIC_ALL)
	 * or
	 * contains list of topics 
	 */
	
	private ArrayList<String>  filterContextIds;
	/* Mapping for contexts
	 * All = all (TRContext.LIST_ALL_ID = TRParser.REVIEWACTIONS_FILTER_CONTEXT_ALL)
	 * or
	 * contains list of contexts 
	 */
	
	private ArrayList<String> filterStates;
	/* Mapping for Status
	 * all = 0
	 * delegated = 2
	 * do asap = 3
	 * inactive = 4
	 * Scheduled = 5
	 * 
	 * See mapping of index (number above) to id (in TRAction)
	 */
	
	private ArrayList<String> filterActionFrom;
	/*
	 * Mapping from action-from:
	 * Three fields to map:
	 * 1: dependent on state of field 3, this field contains number of day or weeks from now, or a specific date.
	 * 2: not used, always set to false. map false = 0, true = 1
	 * 3: the type of date:
	 * 		0 = not used
	 * 		1 = field 1 contains number of days from now.
	 * 		2 = field 1 contains a specific date
	 * 		3 = not used
	 * 		4 = not used
	 * 		5 = field 1 contains the number of weeks from now (only used in action-from)
	 * 			Only possible values are: start of next week (field 1=1), start of this week (field 1=0, start of last week (field 1=-1)
	 * 		6 = field 1 contains the number of week to now (only used in action-to)
	 * 			Only possible values are: end of last week (field 1=-1), end of this week (field 1=0), end of next week (field 1=1)
	 * 
	 * Note field 3 has been added in version 3.3 of Thinking Rock. Version 3.1.2 only has field 1 and 2. 
	 * In version 3.1.2 field 1 is always 'number of days' or a 'specific date'. There is no indicator to distinguish between the two.
	 * Probably there is a rule that if the value is > 1000 it has to be a specific date, because there is no reason to have that number
	 * of days as a filter.
	 * field 2 in version 3.1.2 is used to indicate if the filter is used. In version 3.3 it seems not to be used anymore (always false). 
	 * 
	 */
	private ArrayList<String> filterActionTo;
	/*
	 * For mapping see action-from[]
	 */

	public TRActionList() {
		super();
		filterStates = new ArrayList<String>();
		filterContextIds = new ArrayList<String>();
		filterTopicIds = new ArrayList<String>();
		filterActionFrom = new ArrayList<String>();
		filterActionTo = new ArrayList<String>();
	}
	
	/*
	 * Returns True if list is defined by user in AE, and false if list comes from ReviewActions.xml
	 */
	public Boolean getCustom() {
		return custom;
	}
	
	/*
	 * Returns 1 for CUSTOM_TRUE and 0 on CUSTOM_FALSE
	 */
	public int getCustomInt() {
		if (custom == true) return CUSTOM_TRUE;
		else return CUSTOM_FALSE;
	}
	
	/*
	 * Returns true or false wether this list was taken over from ReviewActions.xml or custom created.
	 * See getCustom for values.
	 */
	public void setCustom(Boolean custom) {
		this.custom = custom;
	}
	public void setCustomAsInt(int custom) {
		if (custom == TRActionList.CUSTOM_TRUE) {
			this.custom = true;
		} else {
			this.custom = false;
		}
	}
	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}
	public void setWeightAsStr(String weight) {
		this.weight = Integer.parseInt(weight);
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}

	public String getFilterDone() {
		return filterDone;
	}

	public void setFilterDone(String filterDone) {
		this.filterDone = filterDone;
	}

	public ArrayList<String>  getFilterTopicIds() {
		return filterTopicIds;
	}

	
	public void addFilterTopicId(String filterTopicId) {
		this.filterTopicIds.add(filterTopicId);
	}
	
	/*
	 * Returns all status filters for this list with the index as assigned by TR
	 */
	public ArrayList<String> getFilterStatesIndex() {
		return filterStates;
	}
	/*
	 * Returns all status filter for this list with the id as specified in TRActions.
	 */
	public ArrayList<String> getFilterStatesIds() {
		ArrayList<String> filterStatesIds = new ArrayList<String>();
		for (int i = 0; i < filterStates.size(); i++) {
			if (filterStates.get(i).equalsIgnoreCase("0")) filterStatesIds.add(FilterActionDateDbAdapter.LIST_ALL_ID);
			if (filterStates.get(i).equalsIgnoreCase("2")) filterStatesIds.add(TRAction.stateActionDelegated);
			if (filterStates.get(i).equalsIgnoreCase("3")) filterStatesIds.add(TRAction.stateActionASAP);
			if (filterStates.get(i).equalsIgnoreCase("4")) filterStatesIds.add(TRAction.stateActionStateInactive);
			if (filterStates.get(i).equalsIgnoreCase("5")) filterStatesIds.add(TRAction.stateActionScheduled);
		}
		return filterStatesIds;
	}

	/*
	 * Index is the number by which the status is known in the TR app.
	 * See definition in the declaration of filterStatus.
	 */
	public void addFilterStatusByIndex(String filterStatusIndex) {
		this.filterStates.add(filterStatusIndex);
	}

	public ArrayList<String> getFilterContextIds() {
		return filterContextIds;
	}

	public void addFilterContextId(String filterContextId) {
		this.filterContextIds.add(filterContextId);
	}

	public ArrayList<String>  getFilterActionFrom() {
		return filterActionFrom;
	}
	/*
	 * Returns the value (as Long) of field 1 from filterActionFrom. 
	 * The meaning of this value depends on the value of field 5 (getFilterActionFromType), 
	 * e.g. number of days from now; a specific date. 
	 */
	public long getFilterActionFromValueLong() {
		return Long.parseLong(filterActionFrom.get(0));
	}
	/*
	 * Returns the value (as String) of field 1 from filterActionFrom. 
	 * The meaning of this value depends on the value of field 5 (getFilterActionFromType), 
	 * e.g. number of days from now; a specific date. 
	 */
	public String getFilterActionFromValueStr() {
		return filterActionFrom.get(0);
	}
	/*
	 * Returns the meaning of the ActionFrom value field (getFilterActionFromValue()).
	 * See specification of actionFrom for the values that can be returned.
	 */
	public int getFilterActionFromType() {
		//Log.i(MainActivity.TAG,"listname:  " + name);
		if (filterActionFrom.size() == 0) {
			return FILTER_FROMTO_NOT_SPECIFIED;
		}
		if (filterActionFrom.size() == 2) {
			// this is using a TR version 3.1.2 file
			if (Long.parseLong(filterActionFrom.get(0)) > 1000) {
				return FILTER_FROMTO_DATE;
			} else {
				return FILTER_FROMTO_DAYS;
			}
		}
		int value = Integer.parseInt(filterActionFrom.get(2)); 
		switch (value) {
		case 1:
			return FILTER_FROMTO_DAYS;
		case 2:
			return FILTER_FROMTO_DATE;
		case 5:
			return FILTER_FROMTO_WEEKS_FROM_NOW;
		default:
			return FILTER_FROMTO_NOT_SPECIFIED;
		}

		
	}
	/*
	 * Returns the meaning of the ActionTo value field (getFilterActionToValue()).
	 * See specification of actionFrom for the values that can be returned.
	 */
	public int getFilterActionToType() {
		if (filterActionTo.size() == 0) {
			return FILTER_FROMTO_NOT_SPECIFIED;
		}
		if (filterActionTo.size() == 2) {
			// this is using a TR version 3.1.2 file
			if (Long.parseLong(filterActionTo.get(0)) > 1000) {
				return FILTER_FROMTO_DATE;
			} else {
				return FILTER_FROMTO_DAYS;
			}
		}
		int value = Integer.parseInt(filterActionTo.get(2)); 
		switch (value) {
		case 1:
			return FILTER_FROMTO_DAYS;
		case 2:
			return FILTER_FROMTO_DATE;
		case 6:
			return FILTER_FROMTO_WEEKS_TO_NOW;
		default:
			return FILTER_FROMTO_NOT_SPECIFIED;
		}
	}
	/*
	 * Returns the value (as long) of field 1 from filterActionTo. 
	 * The meaning of this value depends on the value of field 6 (getFilterActionToType), 
	 * e.g. number of days from now; a specific date. 
	 */
	public long getFilterActionToValueLong() {
		return Long.parseLong(filterActionTo.get(0));
	}
	/*
	 * Returns the value (as String) of field 1 from filterActionTo. 
	 * The meaning of this value depends on the value of field 6 (getFilterActionToType), 
	 * e.g. number of days from now; a specific date. 
	 */
	public String getFilterActionToValueStr() {
		return filterActionTo.get(0);
	}
	
	/*
	 * Returns the number of days from now.
	 */
	
	public void addFilterActionFrom(String actionFrom) {
		this.filterActionFrom.add(actionFrom);
	}

	public ArrayList<String> getFilterActionTo() {
		return this.filterActionTo;
	}

	public void addFilterActionTo(String actionTo) {
		this.filterActionTo.add(actionTo);
	}

	@Override
	public int compareTo(TRActionList trp) {
		return this.name.compareTo(trp.name);
	}
	
/* Test results of filters for Action From and Action To filter.
ActionReview.xml xpath: /screens/screen/filters/filter/value 
filter: action-to
Yesterday:
1: -1
2: false
3: 1

Today
1: 0
2: false
3: 1

Tomorrow
1: 1
2: false
3: 1

End of last week
1: -1
2: false
3: 6

End of this week
1: 0
2: false
3: 6

End of next week
1: 1
2: false
3: 6

Next week, two, three week
1: 7, 14, 21
2: false
3: 1

27 maart 2012
1: 1332799200000
2: false
3: 2

niets
1: 0
2: false
3: 0


filter: action-from

Yesterday:
1: -1
2: false
3: 1

Today
1: 0
2: false
3: 1

Tomorrow
1: 1
2: false
3: 1

start of next week
1: 1
2: false
3: 5

start of this week
1: 0
2: false
3: 5

start of last week:
1: -1
2: false
3: 5

One week ago, two, three
1: -7
2: false
3: 1

27 maart 2012
1: 1332799200000
2: false
3: 2
 */
}
