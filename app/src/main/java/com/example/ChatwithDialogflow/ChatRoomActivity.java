package com.example.ChatwithDialogflow;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ChatwithDialogflow.Adapter.MessageAdapter;
import com.example.ChatwithDialogflow.Model.Chat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class ChatRoomActivity extends AppCompatActivity {

    private static final String TAG = "ChatRoomActivity";
    static final int CUSTOM_POST_REQUEST = 1;  // The request code

    ImageButton btn_input;
    EditText ed_input;
    TextView response;
    Button report, question, hope;

    MessageAdapter messageAdapter;
    ArrayList<Chat> mchat = new ArrayList<>();
    public static int action = 0;

    RecyclerView recyclerView;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        //define view
        btn_input = (ImageButton)findViewById(R.id.btn_input);
        ed_input = (EditText) findViewById(R.id.ed_input);
        response = (TextView) findViewById(R.id.tv_response);
        report = (Button)findViewById(R.id.report);
        question = (Button)findViewById(R.id.question);
        hope = (Button)findViewById(R.id.hope);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);

        //set the layout of recyclerview
        recyclerView.setLayoutManager(linearLayoutManager);

        btn_input.setOnClickListener(doClick);
        report.setOnClickListener(doClick);
        hope.setOnClickListener(doClick);
    }

    private Button.OnClickListener doClick = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {

            switch (view.getId()){
                case R.id.btn_input:
                    action = 0;
                    //User Input and run POST Request
                    sendMessage();
                    break;
                case R.id.report:
                    action = 1;
                    sendMessage();
                    break;
                case R.id.hope:
                    action = 2;
                    sendMessage();
                    break;

            }

        }
    };

    private void sendMessage() {
//        Intent intent = new Intent();
//        intent.putExtra("test", CUSTOM_POST_REQUEST);
        String userQuery = ed_input.getText().toString();

        try {
            if(action == 1){
                userQuery = "立即回報";
                mchat.add(new Chat("sender", userQuery));
            }else if(action == 2) {
                userQuery = "許願池";
                mchat.add(new Chat("sender", userQuery));
            }else{
                ed_input.setText("");
                mchat.add(new Chat("sender", userQuery));
            }
            Log.d(TAG, "User Query: " + userQuery);

            messageAdapter = new MessageAdapter(ChatRoomActivity.this, mchat);
            recyclerView.setAdapter(messageAdapter);

            //task running
            RetrieveFeedTask task=new RetrieveFeedTask();
            task.execute(userQuery);
            Log.d(TAG, "AsyncTask invoked");

        }catch (ActivityNotFoundException e){
            Toast.makeText(this, "POST Request error", Toast.LENGTH_SHORT).show();
        }
    }


    // function connect with API.api and get json response
    public String GetResponse(String query) throws UnsupportedEncodingException{

        String text = "";
        BufferedReader reader;

        try{
            URL url = new URL("https://api.dialogflow.com/v1/query?v=20150910");
            URLConnection conn = url.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);

            // Parameter, including Access token
            conn.setRequestProperty("Authorization", "Bearer " + "60e9b458dc2147ef94437dfdb0c62f2a");
            conn.setRequestProperty("Content-Type", "application/json");

            JSONObject jsonParam = new JSONObject();
            JSONArray queryArray = new JSONArray();
            queryArray.put(query);
            jsonParam.put("query", queryArray);

            jsonParam.put("lang", "en");
            //sessionId: random value
            jsonParam.put("sessionId", "1234567890");

            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(jsonParam.toString());
            Log.d(TAG, "Write jsonParam");

            wr.flush();

            //Get sever response
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line = null;

//            Read server Response
            while ((line = reader.readLine()) != null){
                sb.append(line + "\n");
            }

            text = sb.toString();
            Log.d(TAG, "response is " + text);
            JSONObject object = new JSONObject(text);
            JSONObject object1 = object.getJSONObject("result");
            JSONObject fulfillment = null;
            String speech = null;
            fulfillment = object1.getJSONObject("fulfillment");
            speech = fulfillment.getString("speech");

            return speech;

        }catch (Exception e){
            Log.d(TAG, "Exception occur: " + e);

        }
        finally {
            try {

            }catch (Exception ex){

            }
        }
        return null;
    }

    // Asynctask to run POST Request
    class RetrieveFeedTask extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... voids) {
            String s = null;
            try{
                Log.d(TAG, "doInBackground. Param: " + voids[0]);
                s = GetResponse(voids[0]);

            }catch (UnsupportedEncodingException e){
                e.printStackTrace();
            }
            return s;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
//            response.setText(s);
            Linkify.addLinks(response, Linkify.EMAIL_ADDRESSES);

            mchat.add(new Chat("receiver", s));
            messageAdapter = new MessageAdapter(ChatRoomActivity.this, mchat);
            recyclerView.setAdapter(messageAdapter);
        }
    }

}
