package com.a51integrated.sfs2x;

import redis.clients.jedis.Jedis;
import sfs2x.client.SmartFox;
import sfs2x.client.core.BaseEvent;
import sfs2x.client.core.SFSEvent;
import sfs2x.client.entities.Room;
import sfs2x.client.entities.User;
import sfs2x.client.requests.JoinRoomRequest;
import sfs2x.client.requests.LoginRequest;
import sfs2x.client.requests.PublicMessageRequest;
import sfs2x.client.util.ConfigData;
import java.util.Scanner;


public class Redis2
{
    private static String msg;
	private static String username;
	SmartFox sfs = new SmartFox();
    ConfigData cfg;

    private void startUserJoin()
    {
        // Configure client connection settings
        cfg = new ConfigData();
        cfg.setHost("localhost");
        cfg.setPort(4041);
        cfg.setZone("MyExt");
        cfg.setDebug(false);

        // Set up event handlers
        sfs = new SmartFox();
        sfs.addEventListener(SFSEvent.CONNECTION, this::OnConnection);
        sfs.addEventListener(SFSEvent.CONNECTION_LOST, this::onConnectionLost);
        sfs.addEventListener(SFSEvent.LOGIN, this::onLogin);
        sfs.addEventListener(SFSEvent.LOGIN_ERROR, this::onLoginError);
        sfs.addEventListener(SFSEvent.ROOM_JOIN, this::onRoomJoin);
        sfs.addEventListener(SFSEvent.PUBLIC_MESSAGE, this::onPublicMessage);
        sfs.addEventListener(SFSEvent.PRIVATE_MESSAGE, this::onPrivateMessage);

        // Connect to server
        sfs.connect(cfg);
    }

    // ----------------------------------------------------------------------
    // Event Handlers
    // ----------------------------------------------------------------------
    private void fusion()
    {
    	sfs.send(new PublicMessageRequest(msg));
    }
    private void OnConnection(BaseEvent evt)
    {
        boolean success = (boolean) evt.getArguments().get("success");

        if (success)
        {
        	Jedis jedis = new Jedis("localhost"); 
		      System.out.println("Connection to Redis server sucessfully");
            System.out.println("Connection to SFS success");

            jedis.set(username, username);
            sfs.send(new LoginRequest(username));
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

    
    private void onPublicMessage(BaseEvent evt)
    {
    	User sender = (User) evt.getArguments().get("room");
        Room room = (Room) evt.getArguments().get("room");
        System.out.println("Joined Room: " + room.getName());
        if(sender == sfs.getMySelf())
        	System.out.println("public Messaage : i said: "+ evt.getArguments().get("message"));
        else
        	System.out.println("Public Message: user" + sender.getName()+ "said:"+ evt.getArguments().get("message"));
    }
    
    private void onPrivateMessage(BaseEvent evt)
    {
        User sender = (User)evt.getArguments().get("sender");
        
        if (sender.isItMe() == false)
        	System.out.println("User " + sender.getName() + "sent Me this " + evt.getArguments().get("message").toString());
        
    }
    
    // ----------------------------------------------------------------------

    
    public static void main(String[] args)
    {
    	 
    	
    	Scanner sc = new Scanner(System.in);
    	username = sc.nextLine();
    	Redis2 mj = new Redis2();
    	mj.startUserJoin();


    	msg = "sagar";
        while(true)
        {
        	msg = sc.nextLine();
        	mj.fusion();
        }
        
        
    }

 }