package com.example.tcpserverclient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
        clientMsgEditText = (EditText) findViewById(R.id.server_ip);
        connectPhones = (Button) findViewById(R.id.connect_phones);
        connectPhones.setOnClickListener(connectListener);
        
		serverStatus = (TextView) findViewById(R.id.server_status);
        //SERVERIP = getLocalIpAddress();
        Thread fst = new Thread(new ServerThread());
        fst.start();
	}
	
    public class ServerThread implements Runnable 
    {
        public void run() 
        {
            try 
            {
                if (SERVERIP != null) 
                {
                    handler.post(new Runnable() 
                    {
                        @Override
                        public void run() 
                        {
                            serverStatus.setText("Listening on IP: " + SERVERIP);
                        }
                    });
                    
                    serverSocket = new ServerSocket(SERVERPORT);
                    while (true)
                    {
                        // listen for incoming clients
                        Socket client = serverSocket.accept();
                        handler.post(new Runnable() 
                        {
                            @Override
                            public void run()
                            {
                                serverStatus.setText("Connected.");
                            }
                        });

                        try
                        {
                            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                            String line = null;
                            while ((line = in.readLine()) != null) 
                            {
                            	final String str = line;
                                Log.d("ServerActivity", line);
                                handler.post(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                    	TextView clientMsgTextView = (TextView) findViewById(R.id.client_message);
                                    	clientMsgTextView.setText(str);
                                        // do whatever you want to the front end
                                        // this is where you can be creative
                                    }
                                });
                            }
                            break;
                        } 
                        catch (Exception e) 
                        {
                            handler.post(new Runnable() 
                            {
                                @Override
                                public void run()
                                {
                                    serverStatus.setText("Oops. Connection interrupted. Please reconnect your phones.");
                                }
                            });
                            e.printStackTrace();
                        }
                    }
                }
                else 
                {
                    handler.post(new Runnable()
                    {
                        @Override
                        public void run() 
                        {
                            serverStatus.setText("Couldn't detect internet connection.");
                        }
                    });
                }
            } 
            catch (SocketException e) 
            {
            	Log.d("ServerActivity", e.getMessage());

            	handler.post(new Runnable() 
                {
                    @Override
                    public void run() 
                    {
                        serverStatus.setText("Error");
                    }
                });
                e.printStackTrace();
            } 
            catch (IOException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        }
    }
    
    // gets the ip address of your phone's network
    private String getLocalIpAddress()
    {
        try 
        {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) 
            {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();)
                {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) 
                    { return inetAddress.getHostAddress().toString(); }
                }
            }
        } 
        catch (SocketException ex)
        {
            Log.e("ServerActivity", ex.toString());
        }
        return null;
    }
	
    @Override
    protected void onStop() 
    {
        super.onStop();
        try 
        {
             // make sure you close the socket upon exiting
        	if(serverSocket != null)
        		serverSocket.close();
        }
        catch (IOException e) 
        {
             e.printStackTrace();
        }
    }
    
    // TCP CLIENT CODE
    
    private OnClickListener connectListener = new OnClickListener()
    {
        @Override
        public void onClick(View v) 
        {
            if (!connected) 
            {
                serverIpAddress = SERVERIP;//serverIp.getText().toString();
                if (!serverIpAddress.equals("")) 
                {
                    Thread cThread = new Thread(new ClientThread());
                    cThread.start();
                }
            }
        }
    };
    
    public class ClientThread implements Runnable
    {
        public void run() 
        {
            try
            {
                InetAddress serverAddr = InetAddress.getByName(serverIpAddress);
                Log.d("ClientActivity", "C: Connecting to " + serverAddr.getHostName());
                Socket socket = new Socket(serverAddr, 8080);//ServerActivity.SERVERPORT);
                connected = true;
                
                while (connected)
                {
                    try 
                    {
                        Log.d("ClientActivity", "C: Sending command.");
                        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket
                                    .getOutputStream())), true);
                            // where you issue the commands
                            out.println(clientMsgEditText.getText().toString());//"Hey Server!");
                            Log.d("ClientActivity", "C: Sent.");
                    } 
                    catch (Exception e) 
                    {
                        Log.e("ClientActivity", "S: Error", e);
                    }
                }
                socket.close();
                Log.d("ClientActivity", "C: Closed.");
            } 
            catch (Exception e) 
            {
                Log.e("ClientActivity", "C: Error", e);
                connected = false;
            }
        }
    }
    
    // TCP SERVER
	 private TextView serverStatus;
	 public static String SERVERIP = "localhost";//"128.105.35.199";//"10.0.2.15";"127.0.0.1";// // DEFAULT IP
     public static final int SERVERPORT = 8080; // DESIGNATE A PORT
     private Handler handler = new Handler();
     private ServerSocket serverSocket;
     
     // TCP CLIENT
     private EditText clientMsgEditText;
     private Button connectPhones;
     private String serverIpAddress = "";
     private boolean connected = false;
     private Handler clientHandler = new Handler();
	  


}
