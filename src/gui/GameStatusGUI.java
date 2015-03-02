package gui;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import model.Card;
import model.Contract;
import model.Direction;
import model.Game;
import model.GameListener;

public class GameStatusGUI extends JPanel implements GameListener {
	
	public static Font STATUS_FONT = new Font("Helvetica", Font.BOLD, 60);
	private PlayerStatusGUI[] playerGUIs = new PlayerStatusGUI[4];
	private BidStatusGUI bidGUI = new BidStatusGUI();

	public GameStatusGUI (Game game) {
		game.addListener(this);
		
		setBorder(BorderFactory.createLineBorder(Color.GREEN)) ;
		
		setLayout(new GridBagLayout());
		GridBagConstraints northConstraints = new GridBagConstraints();
		northConstraints.gridx = 1;
		//northConstraints.gridy = 0;
		northConstraints.gridy = 1;
		northConstraints.anchor = GridBagConstraints.NORTH;
		//northConstraints.weightx = 1;
		northConstraints.weighty = 1;
		playerGUIs[Direction.NORTH.ordinal()] = new PlayerStatusGUI(Direction.NORTH);
		add(playerGUIs[Direction.NORTH.ordinal()], northConstraints);
		addDirLabels(Direction.NORTH, 1, 0, GridBagConstraints.PAGE_START);		

		GridBagConstraints eastConstraints = new GridBagConstraints();
		eastConstraints.gridx = 2;
		eastConstraints.gridy = 2;
		eastConstraints.anchor = GridBagConstraints.LINE_START;
		eastConstraints.weightx = 1;
		playerGUIs[Direction.EAST.ordinal()] = new PlayerStatusGUI(Direction.EAST);
		add(playerGUIs[Direction.EAST.ordinal()], eastConstraints);
		addDirLabels(Direction.EAST, 2, 2, GridBagConstraints.LINE_END);

		GridBagConstraints southConstraints = new GridBagConstraints();
		southConstraints.gridx = 1;
		//southConstraints.gridy = 2;
		southConstraints.gridy = 3 ;
		southConstraints.anchor = GridBagConstraints.SOUTH;
		//southConstraints.weightx = 1;
		southConstraints.weighty = 1;
		playerGUIs[Direction.SOUTH.ordinal()] = new PlayerStatusGUI(Direction.SOUTH);
		add(playerGUIs[Direction.SOUTH.ordinal()], southConstraints);
		addDirLabels(Direction.SOUTH, 1, 4, GridBagConstraints.PAGE_END);

		GridBagConstraints westConstraints = new GridBagConstraints();
		westConstraints.gridx = 0;
		westConstraints.gridy = 2;
		westConstraints.anchor = GridBagConstraints.LINE_END;
		westConstraints.weightx = 1;
		playerGUIs[Direction.WEST.ordinal()] = new PlayerStatusGUI(Direction.WEST);
		add(playerGUIs[Direction.WEST.ordinal()], westConstraints);
		addDirLabels(Direction.WEST, 0, 2, GridBagConstraints.LINE_START);
		
		GridBagConstraints bidConstraints = new GridBagConstraints();
		bidConstraints.gridx = 1;
		bidConstraints.gridy = 2;
		bidConstraints.fill = GridBagConstraints.BOTH;
		add (bidGUI, bidConstraints);
	}

	@Override
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
		
	}
	
	public static void main (String[] args) {
        JFrame f = new JFrame();
        //f.add(new GameStatusGUI(new Game()), BorderLayout.CENTER);
        f.setPreferredSize ( new Dimension (1000 , 700));
        f.setMinimumSize ( new Dimension (1000 , 700));
        
        JPanel panel1 = new JPanel ();
        panel1.setBorder (BorderFactory.createLineBorder(Color.RED));
        final JPanel cardPanel = new JPanel();
        final CardLayout layout = new CardLayout();
        cardPanel.setLayout(layout);

        cardPanel.add (panel1, "Screen 1");
        cardPanel.add(new GameStatusGUI(new Game()), "Game status");
        layout.show(cardPanel, "Screen 1");
        
        panel1.addMouseListener (new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent arg0) {
                layout.show(cardPanel, "Game status");
            }

            @Override
            public void mouseEntered(MouseEvent arg0) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void mouseExited(MouseEvent arg0) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void mousePressed(MouseEvent arg0) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void mouseReleased(MouseEvent arg0) {
                // TODO Auto-generated method stub
                
            }
            
        });
        f.add(cardPanel);

        f.setVisible(true);
        
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
	
	private void addDirLabels(Direction dir, int gridX, int gridY, int anchor){
		
		GridBagConstraints dirLabelConstraint = new GridBagConstraints() ;
		dirLabelConstraint.gridx = gridX ;
		dirLabelConstraint.gridy = gridY ;
		dirLabelConstraint.anchor = anchor ;
		
		add(new JLabel(dir.toString()), dirLabelConstraint) ;
	}
}
