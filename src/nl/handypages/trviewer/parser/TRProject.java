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

import nl.handypages.trviewer.database.ProjectsDbAdapter;

/**
 * @author bhavers
 * Object that represents Projects from Thinking Rock xml file.
 * Partial implementation of the projects in xml file. More fields could be added.
 *
 */
public class TRProject implements Comparable<TRProject> {
	public static final String LIST_ALL_ID = "-999";
	
	
	private String id;
	private String parentId;
	private Boolean done;
	private String description;
	private String type; // can be TYPE_*, see final declaration above. Will only be set for top-level folders.
	

	/**
	 * @param done
	 */
	public TRProject() {
		this.done = false;
		this.type = ProjectsDbAdapter.TYPE_UNDEFINED;
	}

	public String toString() {
		// Could easily be used by ArrayAdapters (for example by a Spinner)
		return description;
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
	public String getParentId() {
		return parentId;
	}
	public void setParentId(String parentId) {
		this.parentId = parentId;
	}
	public Boolean getDone() {
		return done;
	}
	public void setDone(Boolean done) {
		this.done = done;
	}
	public void setDoneAsStr(String done) {
		if (done.equalsIgnoreCase(ProjectsDbAdapter.DONE_TRUE)) {
			this.done = true;
		} else {
			this.done = false;
		}
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	@Override
	public int compareTo(TRProject trp) {
		return this.description.compareTo(trp.description);
	}
	

}
