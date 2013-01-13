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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import nl.handypages.trviewer.MainActivity;
import nl.handypages.trviewer.R;
import nl.handypages.trviewer.database.ProjectsDbAdapter;
import nl.handypages.trviewer.helpers.ActionHelper;
import nl.handypages.trviewer.helpers.ActionListHelper;
import nl.handypages.trviewer.helpers.ActorHelper;
import nl.handypages.trviewer.helpers.ContextHelper;
import nl.handypages.trviewer.helpers.ProjectHelper;
import nl.handypages.trviewer.helpers.TopicHelper;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

/**
 * Contains all the imported data of the Thinking Rock Xstreams (.trx) file.
 * @author bhavers
 * @version 1.0
 */

public class TRParser extends Thread {
	private static final String REVIEWACTIONS_FILTER_TOPIC_ALL = "all"; // how All is defined in TR's ReviewActions.xml file.
	private static final String REVIEWACTIONS_FILTER_CONTEXT_ALL = "all";
	
	Document docActions = null;
	Document docActionLists = null;
	Context ctx = null;
	DateFormat dfm;
	String fileActions = null;
	String fileActionLists = null;
	
	Handler mHandler; // used to handle message exchange with the calling activity
	private ArrayList<TRActionList> listActionLists;
	private ArrayList<TRContext> listContexts;
	private ArrayList<TRTopic> listTopics;
	private ArrayList<TRProject> listProjects;
	private ArrayList<TRActor> listActors;
	private ArrayList<TRAction> listActions;
	
	/**
	 * Description: this class will parse the Thinking Rock .trx file and generates objects for
	 * the various parts of this file. It will populate listActions, listActors, listContexts and .listTopics
	 * that will be written to the database.
	 * Note: R can not be read outside an Activity or Service, so you have to pass the Context to this object.
	 */
	public TRParser(Context ctx, String fileTRX, String fileXMLActionLists, Handler h) {
		super();
		Log.i(MainActivity.TAG, "TRParser thread initialized...");
		this.ctx = ctx; // set the context for use throughout this class.
		mHandler = h;
		/*
		 * Document needs local path to file to start with file://
		 */
		if (fileTRX.startsWith("file://")) {
			fileActions = fileTRX;
		} else {
			fileActions = "file://" + fileTRX; 
		}
		if (fileXMLActionLists.startsWith("file://")) {
			fileActionLists = fileXMLActionLists;
		} else {
			fileActionLists = "file://" + fileXMLActionLists; 
		}
		Log.i(MainActivity.TAG,"fileActionLists = " + fileActionLists);
  
	}
	
	@Override
	public void run() {
		try {
			//Log.i("TRV_Main", "TRParser thread run() started.");
			updateProgress(MainActivity.PARSING_PROGRESS_START);
			
			if (fileActions != "") {
				docActions = this.getDocumentHandle(fileActions);
				dfm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
				/*
				 * SimpleDateFormat.parse() in Android 2.3 will work 
				 * with TimeZones like GMT+xxxx etc, but it doesn't recognize UTC for 
				 * example as a valid TimeZone for parsing. 
				 * See: http://stackoverflow.com/questions/4379261/timezone-broken-in-simpledateformat-in-android-2-3?rq=1
				 * 
				 * This will cause wrong time offsets (because the time is parsed in the local
				 * timezone instead of UTC. The solution is to set the Timezone manually.
				 * See: http://stackoverflow.com/questions/6088778/converting-utc-dates-to-other-timezones
				 */
				dfm.setTimeZone(TimeZone.getTimeZone("UTC"));
				
				if (docActions != null) {
					this.setActions();
				} else {
					Log.e(MainActivity.TAG, "Could not create XML document handle from file: " + fileActions);
					Toast.makeText(ctx, ctx.getResources().getString(R.string.error_file_not_correct), Toast.LENGTH_LONG).show();
				}
					
			} else {
				Log.e(MainActivity.TAG, ctx.getResources().getString(R.string.error_file_not_supplied));
				Toast.makeText(ctx, ctx.getResources().getString(R.string.error_file_not_supplied), Toast.LENGTH_LONG).show();
			}
			if (fileActionLists != "") {
				docActionLists = this.getDocumentHandle(fileActionLists);
				if (docActionLists != null) {
					this.setActionLists();
				} else {
					Log.e(MainActivity.TAG, "Could not create XML document handle from file: " + fileActionLists);
				}
					
			} else {
				Log.e(MainActivity.TAG, ctx.getResources().getString(R.string.error_file_not_supplied));
			}
			
			updateProgress(MainActivity.PARSING_PROGRESS_FINISH);
		} catch (Exception e) {
			Log.e("TRV_Main", "Exception in TRParser thread");
			e.printStackTrace();
		}
	}
    

    private void updateProgress(int progressPercentage) {
    	// Sends update message to calling Activity via Handler with the total update progress in percentage
    	Message msg = mHandler.obtainMessage();
    	msg.arg1 = progressPercentage;
    	mHandler.sendMessage(msg);
    }

	private Document getDocumentHandle(String filename) {
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder builder = null;
		try {
			builder = builderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			Log.e(MainActivity.TAG, "ParserConfigurationException: " + e.getMessage());
			e.printStackTrace();
		}
	    Document document = null;
		try {
			//document = builder.parse("file:///mnt/sdcard/test2.trx"); //test.xml
			document = builder.parse(filename); 
		} catch (SAXException e) {
			Log.e(MainActivity.TAG, "SAXException: " + e.getMessage());
			e.printStackTrace();
		} catch (FileNotFoundException fnfe) {
			Log.e(MainActivity.TAG, ctx.getResources().getString(R.string.error_file_not_found) + fnfe.getMessage());
			Toast.makeText(ctx, ctx.getResources().getString(R.string.error_file_not_found), Toast.LENGTH_LONG).show();
		} catch (IOException e) {
			Log.e(MainActivity.TAG, "IOException: " + e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			Log.e(MainActivity.TAG, "Exception: " + e.getMessage());
			e.printStackTrace();
		}
		return document;
	}
	
	private void setActionLists() {
		/**
		 * This method parses the ReviewActions.xml file that contains the action lists. 
		 */
		/**ActionListsDbAdapter actionListAdapter = new ActionListsDbAdapter(ctx);
		*  actionListAdapter.open();
		*  actionListAdapter.deleteAllCustom(false); // deletes all non-custom (TR-app) lists
		*/
		ActionListHelper actionlistHelper = new ActionListHelper(this.ctx);
		actionlistHelper.deleteAllCustom(false); 
		try {
			XPath xpath = XPathFactory.newInstance().newXPath();
			/**
			 * First set Contexts
			 */
			String expression = "/screens/screen"; 
		    NodeList nodes = null;
		    listActionLists = new ArrayList<TRActionList>();
		    nodes = (NodeList) xpath.evaluate(expression, docActionLists, XPathConstants.NODESET);
		    for (int i =0; i < nodes.getLength(); i++) {    
		    	TRActionList list = new TRActionList();
		    	list.setCustom(false);
		    	list.setWeight(i);
  	
		    	//Log.i(MainActivity.TAG,"Action list: " + nodes.item(i).getAttributes().getNamedItem("name").getTextContent());
	    		list.setName(nodes.item(i).getAttributes().getNamedItem("name").getTextContent());
	    		NodeList filters = nodes.item(i).getChildNodes(); 
	    		for (int j = 0; j < filters.getLength(); j++) {  
	    			if (filters.item(j).getNodeName().equalsIgnoreCase("filters")) { // filters element selected
		    			NodeList filter = filters.item(j).getChildNodes(); 
			    		for (int k = 0; k < filter.getLength(); k++) { 
			    			if (filter.item(k).getNodeName().equalsIgnoreCase("filter")) { // filter element selected
				    			//Log.i(MainActivity.TAG,"filter: " + filter.item(k).getAttributes().getNamedItem("filterID").getTextContent());
				    			if (filter.item(k).getAttributes().getNamedItem("filterID").getTextContent().equalsIgnoreCase("done")) {
				    				NodeList value = filter.item(k).getChildNodes();
				    				for (int m = 0; m < value.getLength(); m++) {
				    					if (value.item(m).getNodeName().equalsIgnoreCase("value")) { // value element selected
				    						//Log.i(MainActivity.TAG,"Value: " + value.item(m).getTextContent());
				    						list.setFilterDone(value.item(m).getTextContent());
				    					}
				    				}
								}
				    			if (filter.item(k).getAttributes().getNamedItem("filterID").getTextContent().equalsIgnoreCase("status")) {
				    				NodeList value = filter.item(k).getChildNodes();
				    				for (int m = 0; m < value.getLength(); m++) {
				    					if (value.item(m).getNodeName().equalsIgnoreCase("value")) { // value element selected
				    						//Log.i(MainActivity.TAG,"Value: " + value.item(m).getTextContent());
				    						list.addFilterStatusByIndex(value.item(m).getTextContent());
				    					}
				    				}
								}
				    			if (filter.item(k).getAttributes().getNamedItem("filterID").getTextContent().equalsIgnoreCase("context")) {
				    				NodeList value = filter.item(k).getChildNodes();
				    				for (int m = 0; m < value.getLength(); m++) {
				    					if (value.item(m).getNodeName().equalsIgnoreCase("value")) { // value element selected
				    						//Log.i(MainActivity.TAG,"Value: " + value.item(m).getTextContent());
				    						if (value.item(m).getTextContent().equalsIgnoreCase(REVIEWACTIONS_FILTER_CONTEXT_ALL)) {
				    							list.addFilterContextId(TRContext.LIST_ALL_ID);
				    						} else {
				    							list.addFilterContextId(getContextIdFromName(value.item(m).getTextContent()));
				    						}
				    					}
				    				}
								}
				    			if (filter.item(k).getAttributes().getNamedItem("filterID").getTextContent().equalsIgnoreCase("topic")) {
				    				NodeList value = filter.item(k).getChildNodes();
				    				for (int m = 0; m < value.getLength(); m++) {
				    					if (value.item(m).getNodeName().equalsIgnoreCase("value")) { // value element selected
				    						//Log.i(MainActivity.TAG,"Value: " + value.item(m).getTextContent());
				    						
				    						if (value.item(m).getTextContent().equalsIgnoreCase(REVIEWACTIONS_FILTER_TOPIC_ALL)) {
				    							list.addFilterTopicId(TRTopic.LIST_ALL_ID);
				    						} else {
				    							list.addFilterTopicId(getTopicIdFromName(value.item(m).getTextContent()));
				    						}
				    					}
				    				}
								}
				    			if (filter.item(k).getAttributes().getNamedItem("filterID").getTextContent().equalsIgnoreCase("action-from")) {
				    				NodeList value = filter.item(k).getChildNodes();
				    				for (int m = 0; m < value.getLength(); m++) {
				    					if (value.item(m).getNodeName().equalsIgnoreCase("value")) { // value element selected
				    						//Log.i(MainActivity.TAG,"Value: " + value.item(m).getTextContent());
				    						list.addFilterActionFrom(value.item(m).getTextContent());
				    					}
				    				}
								}
				    			if (filter.item(k).getAttributes().getNamedItem("filterID").getTextContent().equalsIgnoreCase("action-to")) {
				    				NodeList value = filter.item(k).getChildNodes();
				    				for (int m = 0; m < value.getLength(); m++) {
				    					if (value.item(m).getNodeName().equalsIgnoreCase("value")) { // value element selected
				    						//Log.i(MainActivity.TAG,"Value: " + value.item(m).getTextContent());
				    						list.addFilterActionTo(value.item(m).getTextContent());
				    					}
				    				}
								}
			    			}
			    		}
			    	}
	    		}
	    		listActionLists.add(list);
	    		//actionListAdapter.create(list);
	    		//actionlistHelper.create(list);
		    }
		    if (listActionLists.size() > 0) {
		    	actionlistHelper.create(listActionLists);
		    }
		    updateProgress(MainActivity.PARSING_PROGRESS_ACTIONLISTS);
		    //actionListAdapter.close();
		} catch (XPathExpressionException e) {
			Log.e(MainActivity.TAG,"XPathExpressionException: " + e.getMessage());
			e.printStackTrace(); 
		} catch (DOMException e) {
			Log.e(MainActivity.TAG,"DOMException: " + e.getMessage());
			e.printStackTrace();
		/*} catch (ParseException e) {
			Log.e(MainActivity.TAG,"ParseException: " + e.getMessage());
			e.printStackTrace();*/
		} catch (Exception e) {
			Log.e(MainActivity.TAG,"Exception: " + e.getMessage());
			e.printStackTrace();
		}
	}
	private void setActions() {
		/**
		 * This method parses the TRX file and creates objects (Action, Context, Topic) from it.
		 * It might be interesting to see if the original TR classes can be reused 
		 * (tr.model.action (tr-model.jar)).
		 */
		ActionHelper actionHelper = new ActionHelper(this.ctx);
		actionHelper.deleteAll();
		ProjectHelper projectHelper = new ProjectHelper(this.ctx);
		projectHelper.deleteAll();
		ActorHelper actorHelper = new ActorHelper(this.ctx);
		actorHelper.deleteAll();
		ContextHelper contextHelper = new ContextHelper(this.ctx);
		contextHelper.deleteAll();
		TopicHelper topicHelper = new TopicHelper(this.ctx);
		topicHelper.deleteAll();
		
		try {
			XPath xpath = XPathFactory.newInstance().newXPath();
			/**
			 * First set Contexts
			 */
			String expression = "//contexts/items/context"; 
		    NodeList nodes = null;
		    listContexts = new ArrayList<TRContext>();
		    nodes = (NodeList) xpath.evaluate(expression, docActions, XPathConstants.NODESET);
		    for (int i =0; i < nodes.getLength(); i++) {    
	    		TRContext context = new TRContext();
	    		NodeList contexts = nodes.item(i).getChildNodes(); 
	    		for (int j = 0; j < contexts.getLength(); j++) {
	    			if (contexts.item(j).getTextContent() != null) {
		    			if (contexts.item(j).getNodeName().equals("name") ) {
		    				context.setName(contexts.item(j).getTextContent());
		    			}
		    			if (contexts.item(j).getNodeName().equals("description") ) {
		    				context.setDescription(contexts.item(j).getTextContent());
		    			}
		    			if (contexts.item(j).getNodeName().equals("id") ) {
		    				context.setId(contexts.item(j).getTextContent());
		    				context.setIndex(i);
		    			}
	    			}
	    		}
	    		listContexts.add(context);
		    }
		    updateProgress(MainActivity.PARSING_PROGRESS_CONTEXTS);
		    
		    /**
		     * set Topic
		     */
		    
		    expression = "//topics/items/topic"; 
		    nodes = null;
		    listTopics = new ArrayList<TRTopic>();
		    nodes = (NodeList) xpath.evaluate(expression, docActions, XPathConstants.NODESET);
		    for (int i =0; i < nodes.getLength(); i++) {    
	    		TRTopic topic = new TRTopic();
	    		NodeList topics = nodes.item(i).getChildNodes(); 
	    		for (int j = 0; j < topics.getLength(); j++) {
	    			if (topics.item(j).getTextContent() != null) {
		    			if (topics.item(j).getNodeName().equals("name") ) {
		    				topic.setName(topics.item(j).getTextContent());
		    			}
		    			if (topics.item(j).getNodeName().equals("description") ) {
		    				topic.setDescription(topics.item(j).getTextContent());
		    			}
		    			if (topics.item(j).getNodeName().equals("id") ) {
		    				topic.setId(topics.item(j).getTextContent());
		    				topic.setIndex(i);
		    			}
	    			}
	    		}
	    		listTopics.add(topic);
		    }
		    updateProgress(MainActivity.PARSING_PROGRESS_TOPICS);
			
		    /**
		     * set Topic
		     */
		    
		    expression = "//children/project"; 
		    nodes = null;
		    listProjects = new ArrayList<TRProject>();
		    nodes = (NodeList) xpath.evaluate(expression, docActions, XPathConstants.NODESET);
		    for (int i =0; i < nodes.getLength(); i++) {    
	    		TRProject project = new TRProject();
	    		NodeList projects = nodes.item(i).getChildNodes(); 
	    		for (int j = 0; j < projects.getLength(); j++) {
	    			if (projects.item(j).getTextContent() != null) {
	    				/*
	    				 * Set the id for the project
	    				 */
		    			if (projects.item(j).getNodeName().equals("id") ) {
		    				project.setId(projects.item(j).getTextContent());
		    				//Log.i(MainActivity.TAG,"Project id: " + project.getId());
		    			}
		    			if (projects.item(j).getNodeName().equals("parent") ) {
		    				/*
		    				 * Set the type for the top-level projects.
		    				 * The Projects that are directly below the root folder have a class attribute associated with it that sets the type of 
		    				 * the folder (current or future project). This is only set for the top-level folders.
		    				 */
		    				if (projects.item(j).getAttributes().getNamedItem("class") != null ) {
			    				if (projects.item(j).getAttributes().getNamedItem("class").getTextContent().equals("projects") ) {
			    					project.setType(ProjectsDbAdapter.TYPE_CURRENT);
			    					//Log.i(MainActivity.TAG,"Current project" );
			    				}
			    				if (projects.item(j).getAttributes().getNamedItem("class").getTextContent().equals("tr.model.project.ProjectFutures") ) {
			    					project.setType(ProjectsDbAdapter.TYPE_FUTURE);
			    					//Log.i(MainActivity.TAG,"Future project" );
			    				}
		    				} 
	    					/*
	    					 * Set the parent id for the project.
	    					 * Retrieve the id of the parent folder from parent node: ../../../id. 
	    					 */
	    					NodeList parentProject = projects.item(j).getParentNode().getParentNode().getParentNode().getChildNodes();
	    					for (int k = 0; k < parentProject.getLength(); k++) {
	    						if (parentProject.item(k).getNodeName().equals("id")) {
	    							project.setParentId(parentProject.item(k).getTextContent());
		    						//Log.i(MainActivity.TAG,"ParentID = " + parentProject.item(k).getTextContent());
		    					}	
	    					}

		    			}
		    			/*
		    			 * Set the description of the project
		    			 */
		    			if (projects.item(j).getNodeName().equals("description") ) {
		    				project.setDescription(projects.item(j).getTextContent());
		    				//Log.i(MainActivity.TAG,"Project description: " + project.getDescription());
		    			}
		    			/*
		    			 * Set status (done) of this project.
		    			 */
		    			if (projects.item(j).getNodeName().equals("done") ) {
		    				if (projects.item(j).getTextContent().equalsIgnoreCase("true")) {
		    					project.setDone(true);
		    					//Log.i(MainActivity.TAG,"Project Done = true.");
		    				} 
		    			}
	    			}
	    		}
	    		/**
	    		 * Some projects are not populated, do not include these.
	    		 */
	    		if (project.getId() != null) {
	    			listProjects.add(project);
	    		}
		    }
		    updateProgress(MainActivity.PARSING_PROGRESS_PROJECTS);
		    
		    
		    /**
			 * Set Actors (delegates)
			 */
			expression = "//tr.model.actor.Actor"; 
			nodes = null;
			listActors = new ArrayList<TRActor>();
		    nodes = (NodeList) xpath.evaluate(expression, docActions, XPathConstants.NODESET);
		    for (int i =0; i < nodes.getLength(); i++) {    
	    		TRActor actor = new TRActor();
	    		NodeList actors = nodes.item(i).getChildNodes(); 
	    		for (int j = 0; j < actors.getLength(); j++) {
	    			if (actors.item(j).getTextContent() != null) {
	    				if (actors.item(j).getNodeName().equals("id") ) {
		    				actor.setId(actors.item(j).getTextContent());
		    			}
	    				if (actors.item(j).getNodeName().equals("name") ) {
		    				actor.setName(actors.item(j).getTextContent());
		    			}
	    				if (actors.item(j).getNodeName().equals("email") ) {
		    				actor.setEmail(actors.item(j).getTextContent());
		    			}
		    			if (actors.item(j).getNodeName().equals("inactive") ) {
		    				if (actors.item(j).getTextContent().equalsIgnoreCase("true")) {
		    					actor.setInactive(true);
		    				} else {
		    					actor.setInactive(false);
		    				}
		    			}
		    			
	    			}
	    		}
	    		listActors.add(actor);
		    }
		    updateProgress(MainActivity.PARSING_PROGRESS_ACTORS);
		    /**
			 * set Actions
			 */
			
		    // Retrieve the current actions. Needed later when setting the scheduled date. 
		    expression = "//action/description[../state/recurrence/genToDate !=\"\" and ../done= \"true\"]";
		    NodeList recurringActionSpecification = null;
		    recurringActionSpecification = (NodeList) xpath.evaluate(expression, docActions, XPathConstants.NODESET);
		    
		    // Retrieve all the actions
		    expression = "//action[done = \"false\"]"; 
		    NodeList projects = null;
		    listActions = new ArrayList<TRAction>();
		    String tempContextStr = null; // need it at parsing context, but no need to create it again everytime;
	    
	    	projects = (NodeList) xpath.evaluate(expression, docActions, XPathConstants.NODESET);
	    	for (int i =0; i < projects.getLength(); i++) {    
	    		TRAction action = new TRAction();
	    		NodeList actions = projects.item(i).getChildNodes();
	    		Date dateTemp; // temp date that can be reused in loop below 
	    		
	    		/**
				 * Every 'action' element that has a 'thought' element as parent is either:
				 * - a Someday-maybe /data/futures/items/future/thought/action
				 * - an entry in the Futures list of TR  /data/thoughts/items/thought/action
				 * Maybe implement these in a future version of this app. For now they will be discarded.
				 */
	    		if (!actions.item(0).getParentNode().getParentNode().getNodeName().equals("thought") ) {
					
	    			/**
					 * Set the project id in which this action lives.
					 * Retrieve the id of the parent folder from parent node: ../../../id. 
					 */
					NodeList project = actions.item(0).getParentNode().getParentNode().getParentNode().getChildNodes();
					for (int k = 0; k < project.getLength(); k++) {
						if (project.item(k).getNodeName().equals("id")) {
							action.setProjectId(project.item(k).getTextContent());
    						//Log.i(MainActivity.TAG,"Action parentID = " + parentProject.item(k).getTextContent());
    					}	
					}
	    			
		    		for (int j = 0; j < actions.getLength(); j++) {
		    			//Log.i("TRV_Main", actions.item(j).getNodeName() + " - " + actions.item(j).getTextContent());
		    			if (actions.item(j).getTextContent() != null) {
		    				
			    			if (actions.item(j).getNodeName().equals("id") ) {
			    				action.setId(actions.item(j).getTextContent());
			    			}
			    			if (actions.item(j).getNodeName().equals("created")) {
			    				dateTemp = null;
								dateTemp = dfm.parse(actions.item(j).getTextContent());
			    				action.setCreated(dateTemp);
			    				//Log.i("TRV_Main", "Created date: " + action.getCreated().toString());
			    			} 	    			
			    			if (actions.item(j).getNodeName().equals("description")) {
			    				action.setDescription(actions.item(j).getTextContent());
			    				//Log.i(MainActivity.TAG,"Description: " + action.getDescription());
			    			}
			    			if (actions.item(j).getNodeName().equals("topic")) {
			    				/**
			    				 * Action elements in thoughts (reprocessed actions to someday-maybes) contain 
			    				 * no reference attribute. /data/futures/items/future/thought/action/topic
			    				 */
			    				if (actions.item(j).getAttributes().getNamedItem("reference") != null) {			    					
			    					String tempTopicPath = stripArrayIndex(actions.item(j).getAttributes().getNamedItem("reference").getTextContent());
			    					/**
			    					 * Actions that have no topic associated do not have an index, just /topic instead of /topic[x]
			    					 * If there is no topic than default to the first defined topic, which is None.
			    					 */
			    					if (tempTopicPath != null) {
			    						//Log.i(MainActivity.TAG, "tempTopicPath = " + tempTopicPath);
				    					action.setTopicIndex(tempTopicPath);
				    					action.setTopicId(getTopicIdFromIndex(tempTopicPath));
				    					//Log.i(MainActivity.TAG, "Topic number is: " + action.getTopicId() + "   -- Index = " + action.getTopicIndex()); 
				    				} else {
				    					action.setTopicIndex("1");
				    					action.setTopicId(getTopicIdFromIndex("1"));
				    					//Log.i(MainActivity.TAG,"Has no topic associated, Index set to 1, Id = " + getTopicIdFromIndex("1"));
				    				}
			    				}
			    			}
			    				
			    			if (actions.item(j).getNodeName().equals("context")) {
			    				/**
			    				 * Some 'context' elements have other child elements: name (eg None), 
			    				 * description (No context), id (eg 9). The id references the //context/id element
			    				 * Other context only have a 'reference' attribute. The attribute references
			    				 * the array index of the //context entry (seems to be a tricky approach;
			    				 * what if an entry is deleted, will the whole TRX file be rewritten?)
			    				 * Not clear what makes this difference. This parser saves whatever is supplied (id element 
			    				 * and reference attribute if supplied).
			    				 * For now, this app will also use the (tricky) array index to refer to the context.
			    				 * 
			    				 */
			    				if (actions.item(j).getAttributes().getNamedItem("reference") != null) {
			    					// parsing the reference attribute
			    					//Log.i("TRV_Main",actions.item(j).getAttributes().getNamedItem("reference").getTextContent()); 
			    					tempContextStr = stripArrayIndex(actions.item(j).getAttributes().getNamedItem("reference").getTextContent());
			    					if (tempContextStr != null) {
			    						action.setContextIndex(stripArrayIndex(actions.item(j).getAttributes().getNamedItem("reference").getTextContent()));
			    						action.setContextId(getContextIdFromIndex(stripArrayIndex(actions.item(j).getAttributes().getNamedItem("reference").getTextContent())));
			    						tempContextStr = null;
			    					} else {
			    						/**
				    					 * Expect the context to be None.
				    					 * Same as else below, but a different condition that triggers it.
				    					 */
				    					action.setContextIndex("1");
				    					action.setContextId(getContextIdFromIndex("1"));
				    					tempContextStr = null;
				    					//Log.i("TRV_Main","Has no context associated: " + action.getDescription() + " " + action.getTopicId());
			    					}
			    					
			    				} else {
			    					/**
			    					 * Expect the context to be None.
			    					 */
			    					action.setContextIndex("1");
			    					action.setContextId(getContextIdFromIndex("1"));
			    					// otherwise expect the //context/id element.
			    					//Log.i("TRV_Main","Has no context associated: " + action.getDescription());
			    				}
			    			}
				    		if (actions.item(j).getNodeName().equals("state")) {
				    			/**
				    			 * Possible states of an action:
				    			 * action/state@class=actionStateASAP --> nothing further to include
				    			 * action/state@class=actionStateScheduled --> quite complex, only use the following elements:
				    			 *- 'recurrence' --> make boolean so that a special icon can be shown.
				    			 *- 'date' --> contains scheduled date (but can also not contain date element).
				    			 * action/state@class=actionStateDelegated --> use the following:
				    			 * - contains 'to' element and 'chase' (date) element.
				    			 * action/state@class=actionStateInactive --> nothing to do.
				    			 * 
				    			 * Additionally:
				    			 * action/dueDate should also always be saved.
				    			 */

				    			
				    			if (actions.item(j).getAttributes().getNamedItem("class") != null) {
				    				action.setState(actions.item(j).getAttributes().getNamedItem("class").getTextContent());
				    				
				    				if (action.getState().equals("actionStateScheduled") || action.getState().equals("actionStateDelegated")) {

				    					NodeList scheduled = actions.item(j).getChildNodes();
				    					for (int k = 0; k < scheduled.getLength(); k++) {
					    					if (scheduled.item(k).getNodeName().equals("recurrence")) {
					    						/*
					    						 * Each action that is created from a recurring action has a element action/recurrence that refers to the action that specified this
					    						 * recurrence, for example: ../../../action[4]/state/recurrence
					    						 * The 'master' action is often already marked as done (action/done=true), so not available in the actions array.
					    						 * The last generated actions does not have a date, but refers to the master by its @reference attribute. See
					    						 * if statement that checks for attribute "date/@reference". 
					    						 */
					    						action.setScheduledRecurring(true);
					    						//Log.i(MainActivity.TAG,"Descr: " + action.getDescription() + " :-> recurring scheduled action.");
					    					} 
					    					if (scheduled.item(k).getNodeName().equals("date") && scheduled.item(k).getTextContent() != "") {
					    						dateTemp = null;
												dateTemp = dfm.parse(scheduled.item(k).getTextContent());
					    						
					    						action.setScheduledDate(dateTemp);
					    						//Log.i(MainActivity.TAG,"Descr: " + action.getDescription() + " :-> scheduled date= " + scheduled.item(k).getTextContent());
					    					}
					    					if (scheduled.item(k).getNodeName().equals("date") && scheduled.item(k).getTextContent() == "") {
					    						/**
					    						 * Some actions do not have a date. As far as i can see right now this is only the case with the latest
					    						 * recurring scheduled action. These entries do have a reference to the 'master' action that defines the recurrence
					    						 * via date/@reference (for example contains: ../../../action[4]/state/recurrence/genToDate)
					    						 * This is a reference to the original scheduled recurring action. Most often this action is already done (action/done=true), so 
					    						 * it will not be available in scheduled.item(k). 
					    						 * Above, a specific XPath evaluation is done for these actions (setting recurringActionSpecification). These
					    						 * are just regular actions, although they also contain the specification for the recurrence (the 'master'). 
					    						 * 
					    						 * The first action in this piece of code is to find the same action in scheduled.item(k) and 
					    						 * recurringActionSpecification (by comparing descriptions).
					    						 * The second action is to drill down the tree structure, to find the date in /genToDate.
					    						 * This date will be set as the scheduled date.
					    						 * 
					    						 * It seems to work well, but is a error prone approach. Things i think can go wrong and that i haven't tested:
					    						 * - can recurring actions occur in recurringActionSpecification when done=false; while not being available in scheduled.item(k)?
					    						 * (than it will not be included in the actionlist)
					    						 * - i only use Subsequent action in TR, not Regular actions. No clue what happens to those actions.
					    						 */
					    						//Log.i(MainActivity.TAG,action.getDescription() + "  - Reference attribute = " + scheduled.item(k).getAttributes().getNamedItem("reference").getTextContent());
					    						for (int m =0; m < recurringActionSpecification.getLength(); m++) {   
					    							if (recurringActionSpecification.item(m).getTextContent().equals(action.getDescription())) {
					    								NodeList nl1 = recurringActionSpecification.item(m).getParentNode().getChildNodes();
					    								for (int l = 0; l < nl1.getLength(); l++) {
					    									if (nl1.item(l).getNodeName().equalsIgnoreCase("state")) {
					    										NodeList nl2 = nl1.item(l).getChildNodes();
					    										for (int n = 0; n < nl2.getLength(); n++) {
					    											if (nl2.item(n).getNodeName().equalsIgnoreCase("recurrence")) {
					    												NodeList nl3 = nl2.item(n).getChildNodes();
					    												for (int o = 0; o < nl3.getLength(); o++) {
					    													if (nl3.item(o).getNodeName().equalsIgnoreCase("genToDate")) {
							    												//Log.i(MainActivity.TAG,"Node date = " + nl3.item(o).getTextContent());
							    												dateTemp = null;
							    												dateTemp = dfm.parse(nl3.item(o).getTextContent());
							    					    						action.setScheduledDate(dateTemp);
					    													}
																		}
					    												
					    											}	
																}
					    									}
														}
					    							} 
					    						}
					    					}
					    					if (scheduled.item(k).getNodeName().equals("to") && scheduled.item(k).getTextContent() != "") {
				    							// Delegate has been entered using Free-text delegate mode (TR Application: Tools menu --> Options --> Actions tab)
				    							action.setDelegatedTo(scheduled.item(k).getTextContent());
					    						//Log.i(MainActivity.TAG,"Descr: " + action.getDescription() + " :-> delegatedTo: " + scheduled.item(k).getTextContent());
					    					}
					    					if (scheduled.item(k).getNodeName().equals("actorID") && !scheduled.item(k).getTextContent().equalsIgnoreCase("0")) {
					    						// Delegate has been entered using Dropdown delegate mode (TR Application: Tools menu --> Options --> Actions tab)
					    						// Log.i(MainActivity.TAG,"Delegate id: " + scheduled.item(k).getTextContent());
					    						// Code below should be improved, not copying actor in delegateTo field, but referencing.
					    						for (int k2 = 0; k2 < listActors.size(); k2++) {
													if (scheduled.item(k).getTextContent().equalsIgnoreCase(listActors.get(k2).getId())) {
														action.setDelegatedTo(listActors.get(k2).getName());
													}
												}
					    					}
					    					if (scheduled.item(k).getNodeName().equals("chase") && scheduled.item(k).getTextContent() != "") {
					    						//Log.i("TRV_Main", "ChaseDate: " + scheduled.item(k).getTextContent());
					    						dateTemp = null;
												dateTemp = dfm.parse(scheduled.item(k).getTextContent());
					    						action.setChaseDate(dateTemp);
					    					}
				    					}
				    				}	
				    			}
				    		}
			    			
			    			if (actions.item(j).getNodeName().equals("notes")) 
				    			action.setNotes(actions.item(j).getTextContent()); //TBD
			    			if (actions.item(j).getNodeName().equals("done")) {
			    				if (actions.item(j).getTextContent().equals("true"))
			    					action.setDone(true);
			    				else
			    					action.setDone(false);
			    			}
			    			if (actions.item(j).getNodeName().equals("modified")) {
			    				dateTemp = null;
								dateTemp = dfm.parse(actions.item(j).getTextContent());
			    				action.setModified(dateTemp);
			    				//Log.i("TRV_Main", "Modified date: " + action.getModified().toString());
			    			}
			    			if (actions.item(j).getNodeName().equals("dueDate")) {
			    				dateTemp = null;
								dateTemp = dfm.parse(actions.item(j).getTextContent());
			    				action.setDueDate(dateTemp);
			    				//Log.i(MainActivity.TAG,"Descr: " + action.getDescription() + " :-> Due date: " + action.getDueDate().toString());
			    				//Log.i("TRV_Main", "Due date: " + action.getDueDate().toString());
			    			}
		    			}
		    		}
		    		listActions.add(action);
		    		updateProgress(MainActivity.PARSING_PROGRESS_ACTIONS);
	    		}
	    	}
    	if (listActions.size() > 0) {
	    	actionHelper.create(listActions);
	    }
    	if (listProjects.size() > 0) {
	    	projectHelper.create(listProjects);
	    }
    	if (listActors.size() > 0) {
	    	actorHelper.create(listActors);
	    }
    	if (listContexts.size() > 0) {
	    	contextHelper.create(listContexts);
	    }
    	if (listTopics.size() > 0) {
	    	topicHelper.create(listTopics);
	    }
    	
		} catch (XPathExpressionException e) {
			Log.e(MainActivity.TAG,"XPathExpressionException: " + e.getMessage());
			e.printStackTrace(); 
		} catch (DOMException e) {
			Log.e(MainActivity.TAG,"DOMException: " + e.getMessage());
			e.printStackTrace();
		} catch (ParseException e) {
			Log.e(MainActivity.TAG,"ParseException: " + e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			Log.e(MainActivity.TAG,"Exception: " + e.getMessage());
			e.printStackTrace();
		}
		updateProgress(MainActivity.PARSING_PROGRESS_DB_WRITE);
	}
	/*
	 * Convert from the context index to the id.
	 * If there is no id found for the index returns null (should never happen).
	 */
	private String getContextIdFromIndex(String contextIndex) {
		if (contextIndex != null) {
			int index = Integer.parseInt(contextIndex) - 1; // index starts at 1, listContexts at 0;
			for (int contextCounter = 0; contextCounter < listContexts.size(); contextCounter++) {
				if (Integer.toString(index).equalsIgnoreCase(listContexts.get(contextCounter).getIndexAsString())) {
					return listContexts.get(contextCounter).getId();
				}
			}
		}
		return null;
	}
	/*
	 * Convert from the context name to the id.
	 * If there is no id found for the name returns null (should never happen).
	 */
	private String getContextIdFromName(String contextName) {
		if (contextName != null) {
			for (int contextCounter = 0; contextCounter < listContexts.size(); contextCounter++) {
				if (contextName.equalsIgnoreCase(listContexts.get(contextCounter).getName())) {
					return listContexts.get(contextCounter).getId();
				}
			}
		}
		return null;
	}
	
	/*
	 * Convert from the topic index to the id.
	 * If there is no id found for the index returns null (should never happen).
	 */
	private String getTopicIdFromIndex(String topicIndex) {
		if (topicIndex != null) {
			int index = Integer.parseInt(topicIndex) - 1; // index starts at 1, listContexts at 0;
			for (int topicCounter = 0; topicCounter < listTopics.size(); topicCounter++) {
				if (Integer.toString(index).equalsIgnoreCase(listTopics.get(topicCounter).getIndexAsString())) {
					return listTopics.get(topicCounter).getId();
				}
			}
		}
		return null;
	}
	/*
	 * Convert from the topic name to the id.
	 * If there is no id found for the index returns null (should never happen).
	 */
	private String getTopicIdFromName(String topicName) {
		if (topicName != null) {
			for (int topicCounter = 0; topicCounter < listTopics.size(); topicCounter++) {
				if (topicName.equalsIgnoreCase(listTopics.get(topicCounter).getName())) {
					return listTopics.get(topicCounter).getId();
				}
			}
		}
		return null;
	}
	
	private String stripArrayIndex(String xpathStr) {
		/**
		 * xpathStr contains a line like ../../../thoughts/context[2] --> only need the 2
		 * Only if posCloseBracket is on last position, than this is the right reference.
		 * eg.
		 * Not ok: ../../../../future[8]/thought/action/context
		 * Ok: ../../../../../contexts/items/context[4]
		 */
		int posOpenBracket = xpathStr.lastIndexOf('[')+1;
		int posCloseBracket = xpathStr.lastIndexOf(']');
		
		if (posCloseBracket == xpathStr.length() -1) {
			return xpathStr.substring(posOpenBracket,posCloseBracket);
		} else {
			return null;
		}
	}
}