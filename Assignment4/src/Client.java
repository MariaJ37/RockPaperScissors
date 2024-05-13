
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class Client {
	
	public static void main(String[] args) {		
		JFrame frame = new JFrame();
		frame.setBounds(600, 200, 600, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle("RPS Game Client");
		frame.setLayout(null);
		
		
		//ALL LABELS
		//Title of the client name text fiels
		JLabel clientLabel = new JLabel("Client Name:");
		clientLabel.setBounds(20, 20, 200, 30);
		clientLabel.setFont(new Font("Times", Font.BOLD, 14));
		frame.getContentPane().add(clientLabel);
		
		//Title of the player selection
		JLabel ChoseWhoToPlayWith = new JLabel("Play with:");
		ChoseWhoToPlayWith.setBounds(20, 60, 200, 30);
		ChoseWhoToPlayWith.setFont(new Font("Times", Font.BOLD, 14));
		frame.getContentPane().add(ChoseWhoToPlayWith);
		
		//Indicates if you won or lost
		JLabel winnerLabel = new JLabel();
		winnerLabel.setBounds(20, 500, 500, 30);
		winnerLabel.setFont(new Font("Times", Font.BOLD, 14));
		frame.getContentPane().add(winnerLabel);
		winnerLabel.setForeground(Color.blue);
		
		//ALL BUTTONS
		//Connection button to server
		JButton connectButton = new JButton("Connect");
		connectButton.setBounds(380, 20, 150, 30);
		connectButton.setFont(new Font("Times", Font.BOLD, 14));
		frame.getContentPane().add(connectButton);
		
		//Disconnect button to server
		JButton disconnectButton = new JButton("Disconnect");
		disconnectButton.setBounds(380, 20, 150, 30);
		disconnectButton.setFont(new Font("Times", Font.BOLD, 14));
		frame.getContentPane().add(disconnectButton);
		disconnectButton.setVisible(false);
		
		//Button to start playing
		JButton playButton = new JButton("Play");
		playButton.setBounds(380, 60, 150, 30);
		playButton.setFont(new Font("Times", Font.BOLD, 14));
		frame.getContentPane().add(playButton);
		playButton.setEnabled(false);
		
		//Button to stop playing
		JButton stopButton = new JButton("Stop");
		stopButton.setBounds(380, 60, 150, 30);
		stopButton.setFont(new Font("Times", Font.BOLD, 14));
		frame.getContentPane().add(stopButton);
		stopButton.setVisible(false);
			
		//Player chooses rock
		JButton rockButton = new JButton("Rock");
		rockButton.setBounds(200, 150, 150, 30);
		rockButton.setFont(new Font("Times", Font.BOLD, 14));
		frame.getContentPane().add(rockButton);
		rockButton.setEnabled(false);
				
		//Player chooses paper
		JButton paperButton = new JButton("Paper");
		paperButton.setBounds(200, 190, 150, 30);
		paperButton.setFont(new Font("Times", Font.BOLD, 14));
		frame.getContentPane().add(paperButton);
		paperButton.setEnabled(false);
				
		//Player chooses scissors
		JButton scissorsButton = new JButton("Scissors");
		scissorsButton.setBounds(200, 230, 150, 30);
		scissorsButton.setFont(new Font("Times", Font.BOLD, 14));
		frame.getContentPane().add(scissorsButton);
		scissorsButton.setEnabled(false);
		
		
		//Text field for client to input their name
		JTextField clientTextfield = new JTextField();
		clientTextfield.setBounds(150, 20, 200, 30);
		clientTextfield.setFont(new Font("Times", Font.BOLD, 14));
		frame.getContentPane().add(clientTextfield);
		
		
		//Drop Menu for all available players connected to the server
		JComboBox<String> dropDownMenu = new JComboBox<String>();
		dropDownMenu.setBounds(150, 60, 200, 30);
		dropDownMenu.setFont(new Font("Times", Font.BOLD, 14));
		frame.getContentPane().add(dropDownMenu);
		dropDownMenu.setEnabled(false);
		
		//VERY IMPORTANT: This adds visibility to all that was declared previously. The setVisible needs to be declared after creating all the button and text box fields.
		
		frame.setVisible(true);
		
		//Connect to the server
		connectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				//Client inputed a Name 
				if(!clientTextfield.getText().isEmpty()) {
					
					//Inputed name is saved in name
					String name = clientTextfield.getText();
					clientTextfield.setEnabled(false);
					connectButton.setVisible(false);
					disconnectButton.setVisible(true);
					
					//Calls function
					connect(name,connectButton, clientTextfield, disconnectButton, dropDownMenu, playButton, 
							stopButton, rockButton, paperButton, scissorsButton, winnerLabel);
					
					dropDownMenu.setEnabled(true);
					playButton.setEnabled(true);
					
				}
				else {
					System.out.println("Try again. Provide a valid input name");
				}
				
				
			}
		});		
	}//END MAIN
	
	
	
	public static void connect(String name, JButton connectButton, JTextField clientTextfield
			,JButton disconnectButton, JComboBox<String> dropDownMenu, JButton playButton, JButton stopButton
			,JButton rockButton, JButton paperButton, JButton scissorsButton, JLabel winnerLabel) {
		
		try {
			
			
			Socket clientSocket = new Socket("localhost", 6789);
			DataOutputStream outputToServer = new DataOutputStream(clientSocket.getOutputStream());
			String sentence = name + '\n'; 
			outputToServer.writeBytes(sentence);
			BufferedReader inputFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

			Thread readMessage = new Thread(new Runnable() {
				
				@Override
				public void run() {
					
					while(true) {
						
						try {
							String recSentence = inputFromServer.readLine();
							//outputToServer.writeBytes("_inputCheck_"+'\n');
							
							//Determines if there are available players if "available players" is at the start
							if(recSentence.startsWith("available_players")) {
								if(recSentence.length() == 17) {
									recSentence += ",";
								}
								
								recSentence = recSentence.replaceFirst("available_players,", "");
								String[] availablePlayers = recSentence.split(",");
								
								dropDownMenu.removeAllItems();
								
								for(String ap : availablePlayers) {
									dropDownMenu.addItem(ap);
								}
							}
							
							//Determines if the current user has selected player if "chosenToPlay" is at the start

							else if(recSentence.startsWith("_chosenToPlay_")) {
								recSentence = recSentence.replaceFirst("_chosenToPlay_", "");
								dropDownMenu.setSelectedItem(recSentence);
								
								dropDownMenu.setEnabled(false);
								playButton.setVisible(false);
								stopButton.setVisible(true);
								
							}
							//Waiting response from the other player, Second player has to click on play to be able to play with player one
							else if(recSentence.startsWith("_waitingForResponse_")) {
								enableRockPaperScissors(rockButton,paperButton,scissorsButton);
							}
							//Broadcasting the winner to all current players
							else if(recSentence.startsWith("_winner_")) {
								recSentence = recSentence.replaceFirst("_winner_", "");
								winnerLabel.setText(recSentence);
								winnerLabel.setForeground(Color.blue);
							}
							
							//Determines if the player currently selected is no longer available
							else if(recSentence.equals("_playerLeft_")) {
								String leftmsg = "You can not play with this player. Please choose another player";
								winnerLabel.setText(leftmsg);
								winnerLabel.setForeground(Color.red);
								
								stopButton.setVisible(false);
								playButton.setVisible(true);
								dropDownMenu.setEnabled(true);
								disableRockPaperScissors(rockButton,paperButton,scissorsButton);
							}
							
						}
						catch(Exception ex) {
							
						}
					}
				}
			});
			
			readMessage.start(); 
			playButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					
					//Choose the player once you hit "play"
					String chosenPlayer = dropDownMenu.getSelectedItem().toString();
					
					try {
						//If the chosen player doesn't have an empty name or doesn't not exist, then set "playWith" at the start of the string

						if(!chosenPlayer.equals("") || chosenPlayer != null) {
							outputToServer.writeBytes("_playWith_" + chosenPlayer + "," + name + '\n');
							
							dropDownMenu.setEnabled(false);
							playButton.setVisible(false);
							stopButton.setVisible(true);
						}
					}
					catch(Exception ex) {
						
					}
				}
			});
			
			rockButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					
					try {
						//Sending rock to the server 
						outputToServer.writeBytes("_response_rock\n");
						disableRockPaperScissors(rockButton,paperButton,scissorsButton);
					}
					catch(Exception ex) {			
					}
				}
			});
			
			paperButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					
					try {
						//Sending paper to the server
						outputToServer.writeBytes("_response_paper\n");
						disableRockPaperScissors(rockButton,paperButton,scissorsButton);
					}
					catch(Exception ex) {
						
					}
					
				}
			});
			
			scissorsButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					
					try {
						//Sending scissors to the server
						outputToServer.writeBytes("_response_scissors\n");
						disableRockPaperScissors(rockButton,paperButton,scissorsButton);			
					}
					catch(Exception ex) {
	
					}

				}
			});
			//Disconnect from the server. The number of clients connected should be reduce or "no clients connected"
			disconnectButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					
					try {
						clientTextfield.setEnabled(true);
						disconnectButton.setVisible(false);
						connectButton.setVisible(true);
						disableRockPaperScissors(rockButton,paperButton,scissorsButton);						
						stopButton.setVisible(false);
						playButton.setVisible(true);
						dropDownMenu.setEnabled(false);
						//The client disconnected from the server
						outputToServer.writeBytes("_logout_" + '\n');
						clientSocket.close(); 
					}
					catch(Exception ex) {
						
					}
				}
			});	
						
			stopButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					
					try {				
						stopButton.setVisible(false);
						playButton.setVisible(true);
						dropDownMenu.setEnabled(true);
						disableRockPaperScissors(rockButton,paperButton,scissorsButton);
					
						//Client stopped playing
						outputToServer.writeBytes("_stop_" + '\n');
					}
					catch(Exception ex) {
						
					}
				}
			});	
			
		}
		catch(Exception ex) {
			
		}
		
	}
	public static void disableRockPaperScissors(JButton rockButton, JButton paperButton, JButton scissorsButton) {
		
		rockButton.setEnabled(false);
		paperButton.setEnabled(false);
		scissorsButton.setEnabled(false);
		
		
	}
	public static void enableRockPaperScissors(JButton rockButton, JButton paperButton, JButton scissorsButton) {
		rockButton.setEnabled(true);
		paperButton.setEnabled(true);
		scissorsButton.setEnabled(true);
	}
	
	/*  public static void checkAvailability(JTextField clientTextfield) { 
		  Socket clientSocket = new Socket("localhost", 6789);
		  try {
		  DataOutputStream outputToServer = new DataOutputStream(clientSocket.getOutputStream());
			String sentence = name + '\n'; 
			outputToServer.writeBytes(sentence)
			});*/
	 }
	 




