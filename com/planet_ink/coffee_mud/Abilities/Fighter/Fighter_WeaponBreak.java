package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Fighter_WeaponBreak extends StdAbility
{

	public Fighter_WeaponBreak()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Weapon Break";
		displayText="(the great power of the warrior)";
		miscText="";

		triggerStrings.addElement("BREAK");

		canBeUninvoked=true;
		isAutoinvoked=false;

		quality=Ability.MALICIOUS;

		baseEnvStats().setLevel(14);

		minRange=0;
		maxRange=1;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Fighter_WeaponBreak();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!mob.isInCombat())
		{
			mob.tell("You must be in combat to do this!");
			return false;
		}
		if((!auto)&&(mob.fetchWieldedItem()==null))
		{
			mob.tell("You need a weapon to break someone elses!");
			return false;
		}
		if((mob.getVictim().fetchWieldedItem()==null)
		||(!(mob.getVictim().fetchWieldedItem() instanceof Weapon))
		||(((Weapon)mob.getVictim().fetchWieldedItem()).weaponClassification()==Weapon.CLASS_NATURAL))
		{
			mob.tell("He is not wielding a weapon!");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		int levelDiff=mob.getVictim().envStats().level()-mob.envStats().level();
		if(levelDiff>0) 
			levelDiff=levelDiff*5;
		else 
			levelDiff=0;
		Item hisWeapon=mob.getVictim().fetchWieldedItem();
		boolean success=profficiencyCheck((-levelDiff)+(-(mob.getVictim().charStats().getDexterity()*2)),auto)&&(auto||ExternalPlay.isHit(mob,mob.getVictim()));
		if((success)
		   &&(hisWeapon!=null)
		   &&(hisWeapon.envStats().ability()==0)
		   &&(!Sense.isABonusItems(hisWeapon))
		   &&((mob.rangeToTarget()<0)||(mob.rangeToTarget()==0))
		&&((hisWeapon.rawProperLocationBitmap()==Item.WIELD)
		   ||(hisWeapon.rawProperLocationBitmap()==Item.WIELD+Item.HELD)))
		{
			String str=auto?hisWeapon.name()+" break(s) in <T-HIS-HER> hands!":"<S-NAME> disarm(s) <T-NAMESELF> and destroy(s) "+hisWeapon.name()+"!";
			hisWeapon.remove();
			FullMsg msg=new FullMsg(mob,mob.getVictim(),null,Affect.MSG_NOISYMOVEMENT,str);
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				hisWeapon.destroyThis();
				mob.location().recoverRoomStats();
			}
		}
		else
		{
			String str=auto?"":"<S-NAME> attempt(s) to destroy "+hisWeapon.name()+" and fail(s)!";
			FullMsg msg=new FullMsg(mob,mob.getVictim(),null,Affect.MSG_NOISYMOVEMENT,str);
			if(mob.location().okAffect(msg))
				mob.location().send(mob,msg);
		}
		return success;
	}

}