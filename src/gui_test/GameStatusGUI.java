package gui_test;


import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;


public class GameStatusGUI extends JPanel {
	
	public static Font STATUS_FONT = new Font("Helvetica", Font.BOLD, 60);
	private PlayerStatusGUI[] playerGUIs = new PlayerStatusGUI[4];
	private BidStatusGUI bidGUI = new BidStatusGUI();

	public GameStatusGUI () {
		
		
		setLayout(new GridBagLayout());
		
		GridBagConstraints northConstraints = new GridBagConstraints();
		northConstraints.gridx = 1;
		northConstraints.gridy = 0;
		northConstraints.anchor = GridBagConstraints.PAGE_START;
		//northConstraints.anchor = GridBagConstraints.NORTH;
		//northConstraints.weightx = 1;
		northConstraints.weighty = 1;
		
		//playerGUIs[Direction.NORTH.ordinal()] = new PlayerStatusGUI(Direction.NORTH, northConstraints.gridx, northConstraints.gridy);
		playerGUIs[Direction.NORTH.ordinal()] = new PlayerStatusGUI(Direction.NORTH);
		add(playerGUIs[Direction.NORTH.ordinal()], northConstraints);
		
				

		GridBagConstraints eastConstraints = new GridBagConstraints();
		eastConstraints.gridx = 2;
		eastConstraints.gridy = 1;
		eastConstraints.anchor = GridBagConstraints.LINE_START;
		//eastConstraints.anchor = GridBagConstraints.EAST;
		eastConstraints.weightx = 1;
		//playerGUIs[Direction.EAST.ordinal()] = new PlayerStatusGUI(Direction.EAST, eastConstraints.gridx, eastConstraints.gridy);
		playerGUIs[Direction.EAST.ordinal()] = new PlayerStatusGUI(Direction.EAST);
		add(playerGUIs[Direction.EAST.ordinal()], eastConstraints);
		
		

		GridBagConstraints southConstraints = new GridBagConstraints();
		southConstraints.gridx = 1;
		southConstraints.gridy = 2;
		southConstraints.anchor = GridBagConstraints.PAGE_END;
		//southConstraints.anchor = GridBagConstraints.SOUTH;
		//southConstraints.weightx = 1;
		southConstraints.weighty = 1;
		//playerGUIs[Direction.SOUTH.ordinal()] = new PlayerStatusGUI(Direction.SOUTH, southConstraints.gridx, southConstraints.gridy);
		playerGUIs[Direction.SOUTH.ordinal()] = new PlayerStatusGUI(Direction.SOUTH);
		add(playerGUIs[Direction.SOUTH.ordinal()], southConstraints);

		
		GridBagConstraints westConstraints = new GridBagConstraints();
		westConstraints.gridx = 0;
		westConstraints.gridy = 1;
		westConstraints.anchor = GridBagConstraints.LINE_END;
		//westConstraints.anchor = GridBagConstraints.WEST;
		westConstraints.weightx = 1;
		//playerGUIs[Direction.WEST.ordinal()] = new PlayerStatusGUI(Direction.WEST, westConstraints.gridx, westConstraints.gridy);
		playerGUIs[Direction.WEST.ordinal()] = new PlayerStatusGUI(Direction.WEST);
		add(playerGUIs[Direction.WEST.ordinal()], westConstraints);
		
		
		
		GridBagConstraints bidConstraints = new GridBagConstraints();
		bidConstraints.gridx = 1;
		bidConstraints.gridy = 1;
		//bidConstraints.fill = GridBagConstraints.BOTH;
		bidConstraints.anchor = GridBagConstraints.CENTER ;
		add (bidGUI, bidConstraints);
		
		
		/*this.setLayout(new GridLayout(2,2)) ;
		
		playerGUIs[Direction.NORTH.ordinal()] = new PlayerStatusGUI(Direction.NORTH);
		add(playerGUIs[Direction.NORTH.ordinal()]);
		
		playerGUIs[Direction.EAST.ordinal()] = new PlayerStatusGUI(Direction.EAST);
		add(playerGUIs[Direction.EAST.ordinal()]);
		
		playerGUIs[Direction.SOUTH.ordinal()] = new PlayerStatusGUI(Direction.SOUTH);
		add(playerGUIs[Direction.SOUTH.ordinal()]);
		
		playerGUIs[Direction.WEST.ordinal()] = new PlayerStatusGUI(Direction.WEST);
		add(playerGUIs[Direction.WEST.ordinal()]);*/
	}
	
	
	/*public void paint(Graphics g){
		
		super.paint(g);
		
		System.out.println("In paint");
		
		for(int i = 0 ; i < playerGUIs.length ; i++){
			
			System.out.println( "i " + i);
			//playerGUIs[i].paint(g);
			
			playerGUIs[i].repaint();
		}
		
		bidGUI.repaint();
	}*/
	
/*	public GameStatusGUI(){
		
		setLayout(new BorderLayout());
		
		add(bidGUI, BorderLayout.CENTER) ;
		
		playerGUIs[0] = new PlayerStatusGUI(Direction.NORTH) ;
		
		add(playerGUIs[0], BorderLayout.NORTH) ;
				
		
		playerGUIs[1] = new PlayerStatusGUI(Direction.EAST) ;
		
		add(playerGUIs[1], BorderLayout.EAST) ;
		
		
		playerGUIs[2] = new PlayerStatusGUI(Direction.WEST) ;
		
		add(playerGUIs[2], BorderLayout.WEST) ;
		
		
		playerGUIs[3] = new PlayerStatusGUI(Direction.SOUTH) ;
		
		add(playerGUIs[3], BorderLayout.SOUTH) ;
		
	}*/
	


	/*@Override
	public void debugMsg(String string) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void gameReset() {
		bidGUI.clear();
	}

	@Override
	public void cardPlayed(Direction turn, Card card) {
		playerGUIs[turn.ordinal()].cardPlayed(card);
		playerGUIs[(turn.ordinal()+1)%4].nextPlayer();
	}

	@Override
	public void cardScanned(Card card) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void trickWon(Direction winner) {
		for (PlayerStatusGUI playerGUI : playerGUIs) {
			playerGUI.trickOver();
		}
		playerGUIs[winner.ordinal()].nextPlayer();
	}

	@Override
	public void contractSet(Contract contract) {
		bidGUI.setBid(contract);
	}

	@Override
	public void blindHandScanned() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dummyHandScanned() {
		// TODO Auto-generated method stub
		
	}*/
	
	public static void main(String[] args){
		
		JFrame f = new JFrame() ;
		
		f.setSize(900, 600);
		
		GameStatusGUI g = new GameStatusGUI() ;
				
		f.add(g) ;
		
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		f.setVisible(true);
	}
}
