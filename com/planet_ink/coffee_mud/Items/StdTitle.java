package com.planet_ink.coffee_mud.Items;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class StdTitle extends StdItem implements LandTitle
{
	public String ID(){	return "StdTitle";}
	public String displayText() {return "an official looking document sits here";}
	public int baseGoldValue() {return landPrice();}
	public int value()
	{
		if(name().indexOf("(Copy)")>=0)
			baseGoldValue=10;
		else
			baseGoldValue=landPrice();
		return baseGoldValue;
	}
	public void setBaseGoldValue(int newValue) {setLandPrice(newValue);}

	public StdTitle()
	{
		super();
		setName("a standard title");
		setDescription("Give or Sell this title to transfer ownership. **DON'T LOSE THIS!**");
		baseGoldValue=10000;
		isReadable=true;
		setMaterial(EnvResource.RESOURCE_PAPER);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new StdTitle();
	}

	public int landPrice()
	{
		LandTitle A=fetchLandTitle(null);
		if(A==null)	return 0;
		return A.landPrice();
	}
	public void setLandPrice(int price)
	{
		LandTitle A=fetchLandTitle(null);
		if(A==null)	return;
		A.setLandPrice(price);
		A.updateTitle();
	}
	public String landOwner()
	{
		LandTitle A=fetchLandTitle(null);
		if(A==null)	return "";
		return A.landOwner();
	}
	public void setLandOwner(String owner)
	{
		LandTitle A=fetchLandTitle(null);
		if(A==null)	return;
		A.setLandOwner(owner);
		A.updateTitle();
	}
	public String landRoomID()
	{
		return text();
	}

	public void updateTitleName()
	{
		if(!name.startsWith("the title to"))
		{
			Vector V=getRooms();
			if(V.size()<2)
				setName("the title to "+landRoomID());
			else
				setName("the title to rooms around "+((Room)V.firstElement()).ID());
		}
	}

	public void setLandRoomID(String landID)
	{
		setMiscText(landID);
		updateTitleName();
	}

	public void updateLot(Room R, LandTitle T)
	{
		if(T==null)
			T=fetchLandTitle(R);
		if(T==null) return;
		T.updateLot(R,T);
	}

	public void updateTitle()
	{
		Room R=CMMap.getRoom(landRoomID());
		if(R!=null)
		{
			LandTitle A=(LandTitle)fetchLandTitle(R);
			if(A!=null) A.updateTitle();
		}
	}

	public Vector getRooms()
	{
		Room R=CMMap.getRoom(landRoomID());
		if(R!=null)
		{
			LandTitle A=(LandTitle)fetchLandTitle(R);
			if(A!=null) return A.getRooms();
		}
		return new Vector();
	}

	private LandTitle fetchLandTitle(Room R)
	{
		if(R==null)
		{
			if(landRoomID().length()==0) return null;
			R=CMMap.getRoom(landRoomID());
			if(R==null) return null;
		}
		LandTitle A=null;
		for(int a=0;a<R.numAffects();a++)
			if(R.fetchAffect(a) instanceof LandTitle)
			{ A=(LandTitle)R.fetchAffect(a); break;}
		return A;
	}

	public boolean isReadable(){return true;}
	
	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if((affect.targetMinor()==Affect.TYP_WRITE)
		&&(affect.amITarget(this)))
		{
			MOB mob=affect.source();
			mob.tell("You shouldn't write on "+name()+".");
			return false;
		}
		return super.okAffect(myHost,affect);
	}

	public void affect(Environmental myHost, Affect msg)
	{
		super.affect(myHost,msg);
		if((msg.targetMinor()==Affect.TYP_SELL)
		&&(msg.tool()==this)
		&&(msg.target()!=null)
		&&(msg.target() instanceof ShopKeeper))
		{
			Room R=CMMap.getRoom(landRoomID());
			if(R==null)
			{
				destroy();
				Log.errOut("StdTitle","Unknown room: "+landRoomID());
				return;
			}
			LandTitle A=fetchLandTitle(R);
			if(A==null)
			{
				destroy();
				Log.errOut("StdTitle","Unsellable room: "+landRoomID());
				return;
			}
			A.setLandOwner("");
			A.updateTitle();
			updateLot(R,A);
			recoverEnvStats();
		}
		else
		if((msg.targetMinor()==Affect.TYP_GIVE)
		&&(msg.tool()==this)
		&&(msg.source()!=null)
		&&(landOwner().length()>0)
		&&((msg.source().Name().equals(landOwner()))
			||((msg.source().getClanID().equals(landOwner()))))
		&&(msg.target()!=null)
		&&(msg.target() instanceof MOB)
		&&(!(msg.target() instanceof Banker)))
		{
			Room R=CMMap.getRoom(landRoomID());
			if(R==null)
			{
				destroy();
				Log.errOut("StdTitle","Unknown room: "+landRoomID());
				return;
			}
			LandTitle A=fetchLandTitle(R);
			if(A==null)
			{
				destroy();
				Log.errOut("StdTitle","Ungiveable room: "+landRoomID());
				return;
			}
			A.setLandOwner(msg.target().Name());
			A.updateTitle();
			updateLot(R,A);
			recoverEnvStats();
		}
		else
		if((msg.targetMinor()==Affect.TYP_GET)
		&&(msg.amITarget(this))
		&&(msg.tool()!=null)
		&&(msg.tool() instanceof ShopKeeper))
		{
			Room R=CMMap.getRoom(landRoomID());
			if(R==null)
			{
				destroy();
				Log.errOut("StdTitle","Unknown room: "+landRoomID());
				return;
			}
			LandTitle A=fetchLandTitle(R);
			if(A==null)
			{
				destroy();
				Log.errOut("StdTitle","Unbuyable room: "+landRoomID());
				return;
			}
			if((((ShopKeeper)msg.tool()).whatIsSold()==ShopKeeper.DEAL_CLANDSELLER)
			&&(msg.source().getClanID().length()>0))
				A.setLandOwner(msg.source().getClanID());
			else
				A.setLandOwner(msg.source().Name());
			A.updateTitle();
			updateLot(R,A);
			recoverEnvStats();
		}
		else
		if((msg.amITarget(this))
		&&(msg.targetMinor()==Affect.TYP_READSOMETHING))
		{
			if(Sense.canBeSeenBy(this,msg.source()))
			{
				if((landRoomID()==null)||(landRoomID().length()==0))
					msg.source().tell("It appears to be a blank property title.");
				else
				if((landOwner()==null)||(landOwner().length()==0))
					msg.source().tell("It states that the property herein known as '"+landRoomID()+"' is available for ownership.");
				else
					msg.source().tell("It states that the property herein known as '"+landRoomID()+"' is deeded to "+landOwner()+".");
			}
			else
				msg.source().tell("You can't see that!");
			return;
		}
	}
}
