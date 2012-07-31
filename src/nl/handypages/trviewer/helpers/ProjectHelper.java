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
import nl.handypages.trviewer.MainActivity;
import nl.handypages.trviewer.database.ProjectsDbAdapter;
import nl.handypages.trviewer.parser.TRProject;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

public class ProjectHelper {

	
	private Context context;
	private ProjectsDbAdapter projectDbAdapter;

	/**
	 * This is a helper class to read and write Actors to the database. 
	 */
	
	public ProjectHelper(Context context) {
		super();
		this.context = context;
		projectDbAdapter = new ProjectsDbAdapter(this.context);
	}
	public boolean deleteAll() {
		projectDbAdapter.open();
		boolean result = projectDbAdapter.deleteAll();
		projectDbAdapter.close();
		return result;
	}
	
	public void create(ArrayList<TRProject> projects) {
		projectDbAdapter.open();
		for (int i = 0; i < projects.size(); i++) {
			TRProject project = projects.get(i);
			projectDbAdapter.create(project);
		}
		projectDbAdapter.close();
	}

	public TRProject getProject(String id) {
		
		projectDbAdapter.open();
		TRProject project = new TRProject(); 
		
		Cursor cur = projectDbAdapter.fetchProject(id);

		if (cur != null) {
			project.setId(cur.getString(cur.getColumnIndexOrThrow(ProjectsDbAdapter.KEY_ID)));
			project.setParentId(cur.getString(cur.getColumnIndexOrThrow(ProjectsDbAdapter.KEY_PARENTID)));
			project.setDoneAsStr(cur.getString(cur.getColumnIndexOrThrow(ProjectsDbAdapter.KEY_DONE)));
			project.setDescription(cur.getString(cur.getColumnIndexOrThrow(ProjectsDbAdapter.KEY_DESCRIPTION)));
			project.setType(cur.getString(cur.getColumnIndexOrThrow(ProjectsDbAdapter.KEY_TYPE)));
			cur.close();
		} else {
			Log.i(MainActivity.TAG,"No project found for id: " + id);
		}
		
		projectDbAdapter.close();
		return project;
	}
	
	public ArrayList<TRProject> getProjects() {
		
		projectDbAdapter.open();
		ArrayList<TRProject> projects = new ArrayList<TRProject>(); 
		
		Cursor cur = projectDbAdapter.fetchAll();
		//startManagingCursor(cur); 
		cur.moveToFirst();
		while (!cur.isAfterLast()) {
			TRProject project = new TRProject();
			project.setId(cur.getString(cur.getColumnIndexOrThrow(ProjectsDbAdapter.KEY_ID)));
			project.setParentId(cur.getString(cur.getColumnIndexOrThrow(ProjectsDbAdapter.KEY_PARENTID)));
			project.setDoneAsStr(cur.getString(cur.getColumnIndexOrThrow(ProjectsDbAdapter.KEY_DONE)));
			project.setDescription(cur.getString(cur.getColumnIndexOrThrow(ProjectsDbAdapter.KEY_DESCRIPTION)));
			project.setType(cur.getString(cur.getColumnIndexOrThrow(ProjectsDbAdapter.KEY_TYPE)));
			
			projects.add(project);
			cur.moveToNext();
		}
		//actions.trimToSize(); // not really needed.
		cur.close();
		projectDbAdapter.close();
		return projects;
	}
}
