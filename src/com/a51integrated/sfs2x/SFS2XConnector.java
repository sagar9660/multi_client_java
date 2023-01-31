package com.a51integrated.sfs2x;

import sfs2x.client.SmartFox;
import sfs2x.client.core.BaseEvent;
import sfs2x.client.core.SFSEvent;
import sfs2x.client.entities.Room;
import sfs2x.client.entities.User;
import sfs2x.client.requests.JoinRoomRequest;
import sfs2x.client.requests.LoginRequest;
import sfs2x.client.util.ConfigData;
import java.util.Scanner;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import redis.clients.jedis.Jedis;


public class SFS2XConnector
{
    SmartFox sfs;
    ConfigData cfg;

    public SFS2XConnector()
    {
        // Configure client connection settings
        cfg = new ConfigData();
        cfg.setHost("localhost");
        cfg.setPort(4041);
        cfg.setZone("MyExt");
        cfg.setDebug(false);

        // Set up event handlers
        sfs = new SmartFox();
        sfs.addEventListener(SFSEvent.CONNECTION, this::onConnection);
        sfs.addEventListener(SFSEvent.CONNECTION_LOST, this::onConnectionLost);
        sfs.addEventListener(SFSEvent.LOGIN, this::onLogin);
        sfs.addEventListener(SFSEvent.LOGIN_ERROR, this::onLoginError);
        sfs.addEventListener(SFSEvent.ROOM_JOIN, this::onRoomJoin);
        
//        sfs.addEventListener(SFSEvent.USER_ENTER_ROOM, this::onUserEnterRoom);
        sfs.addEventListener(SFSEvent.PUBLIC_MESSAGE, this::onPublicMessage);
        sfs.addEventListener(SFSEvent.PRIVATE_MESSAGE, this::onPrivateMessage);

        // Connect to server
        sfs.connect(cfg);
    }

    // ----------------------------------------------------------------------
    // Event Handlers
    // ----------------------------------------------------------------------

    private void onConnection(BaseEvent evt)
    {
        boolean success = (boolean) evt.getArguments().get("success");

        if (success)
        {
            System.out.println("Connection success");
            Scanner sc = new Scanner(System.in);
        	String userName;
        	userName = sc.nextLine();
            sfs.send(new LoginRequest(userName));
//            sfs.send(new LoginRequest(""));
        }
        else
            System.out.println("Connection Failed. Is the server running?");

    }

    private void onConnectionLost(BaseEvent evt)
    {
        System.out.println("-- Connection lost --");
    }

    private void onLogin(BaseEvent evt)
    {
        System.out.println("Logged in as: " + sfs.getMySelf().getName());

        sfs.send(new JoinRoomRequest("The Lobby"));
        
        try {
 		
		Jedis object = new Jedis("localhost");
		System.out.println("Connection Estblished Successful");

		Scanner inputfromuser = new Scanner(System.in);
		Boolean usr = object.exists(inputfromuser.nextLine());

		System.out.println("data is found or not:" + usr);
		
	
		if(usr == false)
		{
			MongoClient mongoClient = new MongoClient( "localhost", 27017); 
    		System.out.println("Created Mongo Connection successfully"); 
    		
    		MongoDatabase db = mongoClient.getDatabase("sagar");
        	
    		 MongoCollection<Document> collection = db.getCollection("player");
             FindIterable<Document> iterDoc = collection.find();
        	Document findMember = collection.find(new Document("player_name","ravi")).first();
        	System.out.println("here is data"+findMember); 
        	
        
        	
        	try {
        		
        		String player_name, Email, Mobileno;
        		player_name = findMember.getString("player_name");
        		Email = findMember.getString("email");
        		Mobileno = findMember.getString("mobile");
        		
        		

        		System.out.println(player_name);
        		System.out.println(Mobileno);
        		System.out.println(Email);

        		
        		String key1 = "player_name";
        		String key2 = "Email";
        		String key3 = "Mobile";
        		
        		object.set(key1, player_name);

        		object.setex(key1, 60, player_name);
        		
        		
        		object.set(key2, Email);
        		object.set(key3, Mobileno);

	        	}
	        
	        	

	        	catch(Exception e)
	        	{
	        		System.out.println("error");
	        		}
    	        
    		}
    		else
    		{
    			System.out.println("user is found");
    		}
    		
        
     	}
        
		catch (Exception e) {
			e.getStackTrace();
		}
    }

    private void onLoginError(BaseEvent evt)
    {
        String message = (String) evt.getArguments().get("errorMessage");
        System.out.println("Login failed. Cause: " + message);
    }

    private void onRoomJoin(BaseEvent evt)
    {
        Room room = (Room) evt.getArguments().get("room");
        System.out.println("Joined Room: " + room.getName());
    }

    // ----------------------------------------------------------------------

//    private void onUserEnterRoom(BaseEvent evt)
//    {
//
//    }
    
    private void onPublicMessage(BaseEvent evt)
    {
        Room room = (Room) evt.getArguments().get("room");
        System.out.println("Joined Room: " + room.getName());
    }
    
    private void onPrivateMessage(BaseEvent evt)
    {
        User sender = (User)evt.getArguments().get("sender");
        
        if (sender.isItMe() == false)
        	System.out.println("User " + sender.getName() + "sent Me this " + evt.getArguments().get("message"));
    }
    
    // ----------------------------------------------------------------------

    
    public static void main(String[] args)
    {
        new SFS2XConnector();
    }
}