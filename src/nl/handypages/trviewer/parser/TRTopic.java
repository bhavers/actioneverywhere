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

public class TRTopic implements Comparable<TRTopic> {
	public static final String LIST_ALL_ID = "-999";
	
	private String name;
	private String description;
	//private Color color;
	//private Color background; 
	private String id;
	private Integer index; // Array index of topic in trx file.
	
	public String getName() {
		return name;
	}
	public String toString() {
		// Used by ArrayAdapters (for example by Spinner in Thought activity)
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public int getIndex() {
		return index;
	}
	public String getIndexAsString() {
		return Integer.toString(index);
	}
	public void setIndex(Integer index) {
		this.index = index;
	}
	@Override
	public int compareTo(TRTopic trp) {
		return this.name.compareTo(trp.name);
	}
	

}