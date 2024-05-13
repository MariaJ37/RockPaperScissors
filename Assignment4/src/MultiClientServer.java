

import java.awt.Color;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;
import javax.swing.JFrame;
import javax.swing.JLabel;


public class MultiClientServer {

    //vector of connected clients
    public static Vector<ClientThread> clients = new Vector<ClientThread>();

    public static void main(String[] args) throws Exception {

        //waiting for client actions
        ServerSocket welcomeSocket = new ServerSocket(6789);

        JFrame frame = new JFrame();
        frame.setBounds(500, 200, 500, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("RPS Game Server");
        frame.setLayout(null);
        frame.setVisible(true);

        //making a label of connection state
        JLabel numClientsLabel = new JLabel();
        numClientsLabel.setBounds(200, 40, 500, 20);
        numClientsLabel.setFont(new Font("Times", Font.BOLD, 12));
        frame.getContentPane().add(numClientsLabel);

        //display number of clients connected.
        Thread countClients = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {

                    if(clients.size() == 0) {
                        numClientsLabel.setText("No Clients Connected");
                        numClientsLabel.setForeground(Color.red);
                    }
                    else {
                        numClientsLabel.setText(clients.size() + " Clients Connected");
                        numClientsLabel.setForeground(Color.blue);
                    }
                }
            }
        });
        countClients.start(); //start countClients thread

        while (true) {

            //accept client action
            Socket connectionSocket = welcomeSocket.accept();

            //read data from Client and output data to client
            BufferedReader inputFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            DataOutputStream outputToClient = new DataOutputStream(connectionSocket.getOutputStream());

            //get the user input client name
            String name = inputFromClient.readLine();


            outputToClient.writeBytes("You are connected\n");

            Thread sendAvailablePlayers = new Thread(new Runnable() {
                @Override
                public void run() {

                    //store in the string of the list of available players connected in order
                    String previousAvailablePlayers = "";

                    while(true) {
                        try {

                            String availablePlayers = "available_players";

                            //get the list of players that are available
                            for(ClientThread client : MultiClientServer.clients) {
                                if(client.getPlayingWith() == null && !client.getClientName().equals(name)) {
                                    availablePlayers += "," + client.getClientName();
                                }
                            }

                            //update list of available player if change occur
                            if(!availablePlayers.equals(previousAvailablePlayers)) {

                                previousAvailablePlayers = availablePlayers;
                                outputToClient.writeBytes(availablePlayers + "\n");

                            }

                        }
                        catch(Exception ex) {

                        }
                    }
                }
            });
            sendAvailablePlayers.start(); //start thread

            //Displays all the connected clients to the server
            for(ClientThread client : MultiClientServer.clients) {
                if(!client.getClientName().equals(name)) {
                    client.getOutputToClient().writeBytes(name + " is connected" + '\n');
                }
            }
            //Define new Client of type ClientThread
            ClientThread newClient = new ClientThread(name, connectionSocket, inputFromClient, outputToClient);

            Thread t = new Thread(newClient);
            clients.add(newClient); //add to the string of the list of connected clients
            t.start(); //start t thread
        }

    }

}
