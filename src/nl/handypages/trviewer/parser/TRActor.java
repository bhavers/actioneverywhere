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

import nl.handypages.trviewer.database.ActorsDbAdapter;

public class TRActor implements Comparable<TRActor> {
	private String id;
	private String name;
	private String email;
	private Boolean inactive;
	
	public String getId() {
		return id;
	}


	public void setId(String id) {
		this.id = id;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getEmail() {
		return email;
	}


	public void setEmail(String email) {
		this.email = email;
	}


	public Boolean getInactive() {
		return inactive;
	}


	public void setInactive(Boolean inactive) {
		this.inactive = inactive;
	}
	public void setInactiveAsStr(String inactive) {
		if (inactive.equalsIgnoreCase(ActorsDbAdapter.INACTIVE_TRUE)) {
			this.inactive = true;
		} else {
			this.inactive = false;
		}
	}

	@Override
	public int compareTo(TRActor trp) {
		return this.name.compareTo(trp.name);
	}
	

}
