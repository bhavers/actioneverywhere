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
package nl.handypages.trviewer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

/**
 * @author bhavers
 *
 */
public class ActionDetailActivity extends Activity {
	

	private WebView webviewDetails;
	private TextView textviewDescription;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.action_detail);
	    webviewDetails = (WebView) findViewById(R.id.webViewActionDetails);
	    webviewDetails.setBackgroundColor(Color.LTGRAY);
	    textviewDescription = (TextView) findViewById(R.id.textViewActionDetails);
	    Bundle bundle = getIntent().getExtras();
	    textviewDescription.setText(bundle.getString("actionDescription"));
	    String body = bundle.getString("actionDetails");
	    body = body.replaceAll("\\n\\r|\\r\\n|\\r|\\n", "<br />"); // Replace new line (/n, and other combinations) by html <br />
	    
	    // Maybe for future improvements, find a TR defined url and convert it into HTML.
	    // body = body.replaceAll("[http://", "<br />"); // Replace url to html <a> eg. [http://www.testing.com/reviews/|http://www.testing.com/reviews/]
	    // \[http://*\] to match the first part.
	    
	    if (!body.trim().equals("")) {
	    	webviewDetails.loadData(body, "text/html", null);
	    }
	}
	
	private String findPhoneNumber(String str) {
		String result = null;
		
		String[] aWords = str.split(" ");
		
		for (int i = 0; i < aWords.length; i++) {
			Pattern pattern = Pattern.compile("^[+]?([0-9]*[\\.\\s\\-\\(\\)]|[0-9]+){3,24}$");
			Matcher matcher = pattern.matcher(aWords[i]);
			if(matcher.matches())
			{
				result = aWords[i];
			}
		}

		return result;
	}
	
	/**
	 * Start dialing a number when Dial button is pressed.
	 */
	public void startDialer(View view) {
		String phoneNumber = findPhoneNumber((String)textviewDescription.getText());
        

		if (phoneNumber == null) {
			Intent intent = new Intent(Intent.ACTION_DIAL); 
	        startActivity(intent);
		} else {
			phoneNumber = "tel:" + phoneNumber.trim();
			Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(phoneNumber)); 
	        startActivity(intent);
		}

	}
	
}
