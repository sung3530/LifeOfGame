package com.holub.life;

import java.awt.*;
import javax.swing.*;
import com.holub.ui.Colors;	// Contains constants specifying various
							// colors not defined in java.awt.Color.
import com.holub.life.Cell;
import com.holub.life.Storable;
import com.holub.life.Direction;
import com.holub.life.Neighborhood;
import com.holub.life.Universe;

/*** ****************************************************************
 * The Resident class implements a single cell---a "resident" of a
 * block.
 *
 * @include /etc/license.txt
 */

public final class Resident implements Cell
{
	private static final Color BORDER_COLOR = Colors.DARK_YELLOW;
	private static final Color LIVE_COLOR 	= Color.RED;
	private static final Color DEAD_COLOR   = Colors.LIGHT_YELLOW;

	//private boolean amAlive 	= false;
	//private boolean willBeAlive	= false;
	
	State AliveState;
	State DeadState;
	State WillBeAlive;
	State WillBeDead;
	
	State state;
	State willBeState;
	
	
	public Resident () {
		AliveState = new AliveState(this);
		DeadState = new DeadState(this);
		WillBeAlive = new AliveState(this);
		WillBeDead = new DeadState(this);
		
		state= DeadState;
		willBeState = WillBeDead;
	}
	
	
	public void setState(State state) {
		this.state = state;
	}
	
	public void setWillBeState(State willBeState) {
		this.willBeState = willBeState;
	}

	private boolean isStable(){
		return state.getState() == willBeState.getState();
		}

	/** figure the next state.
	 *  @return true if the cell is not stable (will change state on the
	 *  next transition().
	 */
	public boolean figureNextState(
							Cell north, 	Cell south,
							Cell east, 		Cell west,
							Cell northeast, Cell northwest,
							Cell southeast, Cell southwest )
	{
		verify( north, 		"north"		);
		verify( south, 		"south"		);
		verify( east, 		"east"		);
		verify( west, 		"west"		);
		verify( northeast,	"northeast"	);
		verify( northwest,	"northwest" );
		verify( southeast,	"southeast" );
		verify( southwest,	"southwest" );

		int neighbors = 0;

		if( north.	  isAlive()) ++neighbors;
		if( south.	  isAlive()) ++neighbors;
		if( east. 	  isAlive()) ++neighbors;
		if( west. 	  isAlive()) ++neighbors;
		if( northeast.isAlive()) ++neighbors;
		if( northwest.isAlive()) ++neighbors;
		if( southeast.isAlive()) ++neighbors;
		if( southwest.isAlive()) ++neighbors;

		if( (neighbors==3 || (state.getState() && neighbors==2)) == true ) setWillBeState(WillBeAlive);
		else setWillBeState(WillBeDead);
		
		return !isStable();
	}

	private void verify( Cell c, String direction )
	{	assert (c instanceof Resident) || (c == Cell.DUMMY)
				: "incorrect type for " + direction +  ": " +
				   c.getClass().getName();
	}

	/** This cell is monetary, so it's at every edge of itself. It's
	 *  an internal error for any position except for (0,0) to be
	 *  requsted since the width is 1.
	 */
	public Cell	edge(int row, int column)
	{	assert row==0 && column==0;
		return this;
	}

	public boolean transition()
	{	boolean changed = isStable();
	
	
	if(willBeState.getState() == true) setState(AliveState);
	else if(willBeState.getState() == false) setState(DeadState);
		return changed;
	}

	public void redraw(Graphics g, Rectangle here, boolean drawAll)
    {   g = g.create();
    		g.setColor(state.getState() ? LIVE_COLOR : DEAD_COLOR );
		g.fillRect(here.x+1, here.y+1, here.width-1, here.height-1);

		g.setColor( BORDER_COLOR );
		g.drawLine( here.x, here.y, here.x, here.y + here.height );
		g.drawLine( here.x, here.y, here.x + here.width, here.y  );
		g.dispose();
	}

	public void userClicked(Point here, Rectangle surface)
	{	
		if(state.getState() == true) setState(DeadState);
		else if(state.getState() == false) setState(AliveState);
	}

	public void	   clear()			{
		setWillBeState(WillBeDead);
		setState(DeadState); 
		}
	public boolean isAlive()		{
		return state.getState();
		}
	public Cell    create()			{return new Resident();			}
	public int 	   widthInCells()	{return 1;}

	public Direction isDisruptiveTo()
	{	return isStable() ? Direction.NONE : Direction.ALL ;
	}

	public boolean transfer(Storable blob,Point upperLeft,boolean doLoad)
	{
		Memento memento = (Memento)blob;
		if( doLoad )
		{	
			if(   memento.isAlive(upperLeft)  ) {
				setWillBeState(WillBeAlive);
				setState(AliveState);
			} 
			else {
				setState(DeadState);
				setWillBeState(WillBeDead);
			} 
			

			if(state.getState())
				return true;
		}
		 					// store only live cells
		else if(state.getState())	
		memento.markAsAlive( upperLeft );

		return false;
	}

	/** Mementos must be created by Neighborhood objects. Throw an
	 *  exception if anybody tries to do it here.
	 */
	public Storable createMemento()
	{	throw new UnsupportedOperationException(
					"May not create memento of a unitary cell");
	}
}
