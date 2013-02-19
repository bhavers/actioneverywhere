package nl.handypages.trviewer.test;

import com.jayway.android.robotium.solo.Solo;

import nl.handypages.trviewer.MainActivity;
import nl.handypages.trviewer.R;
import android.preference.CheckBoxPreference;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Functional test script prereqs: 
 * - Device should be linked with Dropbox manually (as Robotium does not access Dropbox app)
 * - This script is dependent on two test files ReviewActions.xml and TestData.trx, available in tests\assets\
 * - The test files must be copied on Dropbox in: <dropbox>\Apps\Action Everywhere\Test
 * - Run as Android JUnit Test (for just one test: use @SmallTest annotation on method and select this in Run Configuration).
 * - Test validated with Android API level 16 on Intel Atom image, using Robotium 3.6
 * 
 * Import in Eclipse
 * Import the tests folder as separate project in Eclipse, eg called Action Everywhere Tests.
 * Eclipse 4.2 Juno has problems with the import process, see: http://stackoverflow.com/questions/12286234/android-test-project-root-directory-issue-in-eclipse 
 */

public class MainActivityFunctionalTest extends ActivityInstrumentationTestCase2<MainActivity> {
	private Solo solo;
	
	public MainActivityFunctionalTest() {
		super(MainActivity.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		solo = new Solo(getInstrumentation(), getActivity());
		
		boolean actual = solo.searchText("Apache License"); // EULA popup, app has not been set up, do this first.
		assertEquals(false, actual);
	}

	protected void tearDown() throws Exception {
		solo.finishOpenedActivities();
	}

	public void testEditActionLists() {
		ListView lvMainList = (ListView)solo.getView(R.id.listViewMain);
		int originalMainListCount = lvMainList.getCount();
		
		solo.setActivityOrientation(Solo.LANDSCAPE);
		solo.sleep(1000);
		solo.setActivityOrientation(Solo.PORTRAIT);
		
		solo.sendKey(Solo.MENU);
		solo.clickOnText("Edit Lists");
		
		ListView lvEditLists = (ListView) solo.getView(R.id.listEditLists);
		int originalEditListCount = lvEditLists.getCount(); //
		assertTrue(originalEditListCount == 0); // for clean testing purposes, assert an empty list.
		Log.i(MainActivity.TAG,"list count = " + Integer.toString(originalEditListCount));
		EditText editListEdit = (EditText) solo.getView(R.id.editListEditTextAdd);
		solo.enterText(editListEdit, "Test List 1");
		solo.clickOnButton("Add");
		solo.sleep(1000); // getCount is not refreshed in time, add a pause.
		Log.i(MainActivity.TAG,"new list count = " + Integer.toString(lvEditLists.getCount()));
		assertTrue(lvEditLists.getCount() == originalEditListCount+1);
		solo.enterText(editListEdit, "Test List 2");
		solo.clickOnButton("Add");
		solo.enterText(editListEdit, "Test List 3");
		solo.clickOnButton("Add");
		solo.sleep(1000); // getCount is not refreshed in time, add a pause.
		assertTrue(lvEditLists.getCount() == originalEditListCount+3);
		solo.clickOnText("Test List 3");
		solo.clickOnButton("Move down");  // Test moving list to impossible positions
		solo.clickOnButton("Move up");
		solo.clickOnText("Test List 3");
		solo.clickOnButton("Move up");
		solo.clickOnText("Test List 3");
		solo.clickOnButton("Move up");
		Log.i(MainActivity.TAG,"position of list 3 = " + lvEditLists.getCheckedItemPosition());
		assertTrue(lvEditLists.getCheckedItemPosition() == 0); // expect the selected list to be on the first position.
		
		solo.goBack();
		//solo.sleep(5000); // getCount is not refreshed in time, add a pause.
		lvMainList = (ListView)solo.getView(R.id.listViewMain); // After switching activities, you need to reinstatiate, otherwise getCount() does not get refreshed.
		Log.i(MainActivity.TAG,"main list count = " + Integer.toString(lvMainList.getCount()) + " - dit was "+ Integer.toString(originalMainListCount));
		assertTrue(lvMainList.getCount() == originalMainListCount + 3);
		
		solo.sendKey(Solo.MENU);
		solo.clickOnText("Edit Lists");
		
		solo.clickOnText("Test List 1");
		solo.clickOnButton("Remove");
		solo.clickOnText("Test List 2");
		solo.clickOnButton("Remove");
		solo.clickOnText("Test List 3");
		solo.clickOnButton("Remove");
		solo.sleep(1000); // getCount is not refreshed in time, add a pause.
		lvEditLists = (ListView) solo.getView(R.id.listEditLists); // After switching activities, you need to reinstatiate, otherwise getCount() does not get refreshed.
		assertTrue(lvEditLists.getCount() == originalEditListCount);
		
		solo.goBack();
	}
	
	public void testPreferences() {
		solo.sendKey(Solo.MENU);
        solo.clickOnText("Preferences");
        boolean actual = solo.searchText("Login to Dropbox"); 
		assertEquals(false, actual); // device is not yet linked to Dropbox, do this manually (Robotium can not extend outside app to control Dropbox app).
		actual = solo.searchText("Unlink");
		assertEquals(true, actual);
		solo.setActivityOrientation(Solo.LANDSCAPE);
		solo.sleep(1000);
		solo.setActivityOrientation(Solo.PORTRAIT);
		solo.clickOnText("Select Dropbox File");
        solo.clickOnMenuItem("Test");
        solo.clickOnMenuItem("TestData.trx");
        solo.clickOnText("Synchronize");
        solo.clickOnText("15 minutes");
        solo.goBack();
        
        actual = solo.searchText("minutes ago"); // label at top.
		assertEquals(true, actual); // Download has been successful.
	}
	
	public void testAction() {
		solo.clickOnText("This Week");
		ListView actionList = (ListView)solo.getView(android.R.id.list);
		assertTrue(actionList.getCount() > 0);
		solo.setActivityOrientation(Solo.LANDSCAPE);
		solo.sleep(1000);
		solo.setActivityOrientation(Solo.PORTRAIT);
		assertTrue(actionList.getCount() > 0);
		solo.clickInList(0);
		solo.setActivityOrientation(Solo.LANDSCAPE);
		solo.sleep(1000);
		solo.setActivityOrientation(Solo.PORTRAIT);
		solo.goBack();
		solo.goBack();
		//getInstrumentation().callActivityOnDestroy(solo.getCurrentActivity());
		//solo.sleep(3000);
		//getInstrumentation().callActivityOnResume(solo.getCurrentActivity());
		//assertTrue(actionList.getCount() > 0);
	}
	
	@SmallTest
	public void testFilters() {
		
		// Create a new custom list
		solo.sendKey(Solo.MENU);
		solo.clickOnText("Edit Lists");
		EditText editListEdit = (EditText) solo.getView(R.id.editListEditTextAdd);
		solo.enterText(editListEdit, "Custom List 1");
		solo.clickOnButton("Add");
		solo.goBack();
		
		// Enter custom list and check number of actions.
		solo.clickOnText("Custom List 1");
		assertTrue(solo.searchText("17 actions"));

		// Create filters and check number of actions after applying filters.
		solo.sendKey(Solo.MENU);
		solo.clickOnText("Filter List");
		
		solo.clickOnText("Context");
		solo.clickOnText("Phone");
		solo.clickOnText("Computer");
		solo.goBack();

		solo.clickOnText("Topic");
		solo.clickOnText("Personal");
		solo.clickOnText("Work");
		//solo.clickOnText("Work - projects");
		/**
		 * clickOnText("Work - projects") doesn't work because it is not visibly on the 
		 * screen, and in some emulators the scroll function for AlertDialogs doesn't work.
		 * Also solo.clickInList(8) doesn't seem to work.
		 * So now using None.
		 */
		solo.clickOnText("None");
		
		solo.goBack();
		solo.goBack();
		assertTrue(solo.searchText("4 actions"));
		
		solo.sendKey(Solo.MENU);
		solo.clickOnText("Filter List");
		
		solo.clickOnText("Status");
		solo.clickOnText("Include all");
		solo.goBack();
		
		solo.clickOnText("to or after");
		solo.clickOnText("Today");
		solo.goBack();
		solo.goBack();
		assertTrue(solo.searchText("2 actions"));
		
		// Remove custom list.
		solo.goBack();
		solo.sendKey(Solo.MENU);
		solo.clickOnText("Edit Lists");
		solo.clickOnText("Custom List 1");
		solo.clickOnButton("Remove");
		solo.goBack();
	}
}
