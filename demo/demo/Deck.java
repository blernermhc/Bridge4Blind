package demo;

import lerner.blindBridge.model.Card;
import lerner.blindBridge.model.Rank;
import lerner.blindBridge.model.Suit;

/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */ 


public class Deck {
    private static Card[] cards = new Card[52];
    public Deck() {
        int i = 0;
        for (int j = 0; j < 4; j++) {
            for (Rank rank : Rank.values()) {
            	switch(j){
            		
            		case 0:
            			cards[i++] = new Card(rank, Suit.DIAMONDS);
            			break;
            		case 1:
            			cards[i++] = new Card(rank, Suit.SPADES);
            			break;
            		case 2:
            			cards[i++] = new Card(rank, Suit.HEARTS);
            			break;
            		case 3:
            			cards[i++] = new Card(rank, Suit.CLUBS);
            			break;
            			
            	}
            		
            }
        }
    }
    
    public Card draw(){
    	int card = -1;
    	while (card < 0 || cards[card] == null){
    		card = (int) Math.floor(Math.random() * 52);
    	}
    	Card c = cards[card];
    	cards[card] = null;
    	return c;
    	
    }
}