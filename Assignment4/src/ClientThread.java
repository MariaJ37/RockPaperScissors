
import java.io.*;
import java.net.*;



public class ClientThread extends Thread {

	private final String name; 
	private final Socket connectionSocket; 
	private BufferedReader inputFromClient;
	private DataOutputStream outputToClient;
	private String playingWith = null; 
	private String gameChoice = null; 
	
	 
	public ClientThread(String name, Socket connectionSocket, BufferedReader inputFromClient, 
			DataOutputStream outputTpClient) {
		
		this.name = name;
		this.connectionSocket = connectionSocket;
		this.inputFromClient = inputFromClient;
		this.outputToClient = outputTpClient;
	}
	
	//The getters will help the communication between the server and the client thread, 
	public void setplayingWith( String playingWith) {
		this.playingWith = playingWith;
	}
	
	public String getPlayingWith() {
		return playingWith;
	}
	
	public String getClientName() {
		return name;
	}
	
	public DataOutputStream getOutputToClient() {
		return outputToClient;
	}
	
	public BufferedReader getInputFromClient() {
		return inputFromClient;
	}
	
	//Making the choices and who is the winner depending on what each player choose (p1 and p2)
	public String decideWinner(String p1, String p2) {
		if((p1.equals("paper") && p2.equals("rock")) || (p1.equals("rock") && p2.equals("paper"))) {
			return "paper";
		}
		else if((p1.equals("paper") && p2.equals("scissors")) || (p1.equals("scissors") && p2.equals("paper"))) {
			return "scissors";
		}
		else if((p1.equals("rock") && p2.equals("scissors")) || (p1.equals("scissors") && p2.equals("rock"))) {
			return "rock";
		}
		else {
			return "tie";
		}
	}

	@Override
	public void run() {
		
		try {
			while(true) {
				
				inputFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
				//reading the input from the client
				String recSentence = inputFromClient.readLine(); 
		
				
				if(recSentence.equals("_logout_")) {
					
					
					//This client disconnected from the server. No longer able to play with them.
					for(ClientThread client : MultiClientServer.clients) {
						if(!client.name.equals(this.name)) {
							client.outputToClient.writeBytes(this.name + " is disconnected" + '\n');
						}
					}
					//player left "close the window"
					for(ClientThread client : MultiClientServer.clients) {
						if(client.name.equals(this.playingWith)) {
							client.gameChoice = null;
							client.playingWith = null;
							client.outputToClient.writeBytes("_playerLeft_" + '\n');
							break;
						}
						
					}
					
					//Find and remove this client from the drop down menu
					for(ClientThread client : MultiClientServer.clients) {
						if(client.name.equals(this.name)) {
							MultiClientServer.clients.removeElement(client);
							break; 
						}
					}
					//initial while loop still running so break it
					break;
				}
				
				//The player stopped the game
				else if(recSentence.equals("_stop_")) {
					for(ClientThread client : MultiClientServer.clients) {
						if(client.name.equals(this.playingWith)) {
							this.playingWith = null; 
							client.gameChoice = null; 
							client.playingWith = null; 
							client.outputToClient.writeBytes("_playerLeft_" + '\n');
							break;
						}
						
					}
				}
				
				// Games begins each player has to chose between rock paper and scissors, 
				else if(recSentence.startsWith("_response_")) {
					recSentence = recSentence.replaceFirst("_response_", "");
					this.gameChoice = recSentence;
					
					for(ClientThread client : MultiClientServer.clients) {
						if(client.name.equals(this.playingWith) && client.gameChoice != null) {
							String p1choice = client.gameChoice;
							String p2choice = this.gameChoice;
							String winner = decideWinner(p1choice, p2choice);
							//Players choose the same "item"
							if(winner.equals("tie")) {
								client.outputToClient.writeBytes("_winner_" + p1choice + " x " + p2choice + "...Draw" + '\n');
								this.outputToClient.writeBytes("_winner_" + p1choice + " x " + p2choice + "...Draw" + '\n');
							}
							//Player one choose the winning "move"
							else if (winner.equals(p1choice)) {
								client.outputToClient.writeBytes("_winner_" + p1choice + " x " + p2choice + "...You win" + '\n');
								this.outputToClient.writeBytes("_winner_" + p1choice + " x " + p2choice + "..." + this.playingWith + " win" + '\n');
							}
							//Player two choose the winning "move"
							else if(winner.equals(p2choice)) {
								
								this.outputToClient.writeBytes("_winner_" + p1choice + " x " + p2choice + "...You win" + '\n');
								client.outputToClient.writeBytes("_winner_" + p1choice + " x " + p2choice + "..." + client.playingWith + " win" + '\n');
							}
							
							client.gameChoice = null;
							this.gameChoice = null;
							
							client.outputToClient.writeBytes("_waitingForResponse_" + '\n');
							this.outputToClient.writeBytes("_waitingForResponse_" + '\n');
							
							break;
						}
					}
				}
				
				//There's no one to play with, only one person connected to the server
				else if(recSentence.startsWith("_playWith_")) {
					recSentence = recSentence.replaceFirst("_playWith_", "");
					
					//player 1 and player 2
					String[] p1_p2 = recSentence.split(",");
					this.playingWith = p1_p2[0];
					DataOutputStream p1outputToClient = null; 
					DataOutputStream p2outputToClient = this.outputToClient; 
					
					
					for(ClientThread client : MultiClientServer.clients) {
						if(client.name.equals(p1_p2[0])) {
							
							client.setplayingWith(p1_p2[1]);
							
							p1outputToClient = client.getOutputToClient();
							
							client.outputToClient.writeBytes("_chosenToPlay_" + p1_p2[1] + '\n');
							break; //found person, so get out of for loop
						}
					}
					//Waiting reply to from other player, they haven't click on play. 
					p1outputToClient.writeBytes("_waitingForResponse_" + '\n');
					p2outputToClient.writeBytes("_waitingForResponse_" + '\n');
					
				}
				
			}
			
		}
		catch(Exception ex) {
			
		}
		
	}
	
}