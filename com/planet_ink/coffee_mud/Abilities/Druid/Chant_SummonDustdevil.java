package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_SummonDustdevil extends Chant
{
	public String ID() { return "Chant_SummonDustdevil"; }
	public String name(){ return "Summon Dustdevil";}
	public String displayText(){return "(Summon Dustdevil)";}
	public int quality(){return Ability.BENEFICIAL_SELF;}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public Environmental newInstance(){	return new Chant_SummonDustdevil();}
	public long flags(){return Ability.FLAG_SUMMONING;}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID==MudHost.TICK_MOB)
		{
			if((affected!=null)
			&&(affected instanceof MOB)
			&&(invoker!=null))
			{
				MOB mob=(MOB)affected;
				if(((mob.amFollowing()==null)
				||(mob.amDead())
				||(mob.location()!=invoker.location())))
					unInvoke();
				else
				{
					Vector V=new Vector();
					for(int i=0;i<mob.location().numItems();i++)
					{
						Item I=mob.location().fetchItem(i);
						if((I!=null)&&(I.container()==null))
							V.addElement(I);
					}
					boolean giveUp=false;
					for(int i=0;i<V.size();i++)
					{
						Item I=(Item)V.elementAt(i);
						if(mob.maxCarry()>=mob.envStats().weight()+I.envStats().weight())
							CommonMsgs.get(mob,null,I,false);
						else
							giveUp=true;
					}
					if(giveUp)
					{
						V=new Vector();
						for(int i=0;i<mob.inventorySize();i++)
						{
							Item I=mob.fetchInventory(i);
							if((I!=null)&&(I.container()==null))
								V.addElement(I);
						}
						for(int i=0;i<V.size();i++)
						{
							FullMsg msg=new FullMsg(mob,invoker,(Item)V.elementAt(i),CMMsg.MSG_GIVE,"<S-NAME> whirl(s) <O-NAME> to <T-NAMESELF>.");
							if(mob.location().okMessage(mob,msg))
								mob.location().send(mob,msg);
							else
								break;
						}
					}
				}
			}
		}
		return super.tick(ticking,tickID);
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(msg.amISource((MOB)affected)))
		{
			if(msg.sourceMinor()==CMMsg.TYP_DEATH)
			{
				unInvoke();
				return false;
			}
			if(msg.sourceMinor()==CMMsg.TYP_WEAPONATTACK)
			{
				msg.source().tell("You can't fight!");
				msg.source().setVictim(null);
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

	public void unInvoke()
	{
		MOB mob=(MOB)affected;
		if((canBeUninvoked())&&(mob!=null))
		if(mob.location()!=null)
		{
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> dissipate(s).");
			Vector V=new Vector();
			for(int i=0;i<mob.inventorySize();i++)
				V.addElement(mob.fetchInventory(i));
			for(int i=0;i<V.size();i++)
			{
				Item I=(Item)V.elementAt(i);
				mob.delInventory(I);
				mob.location().addItemRefuse(I,Item.REFUSE_MONSTER_EQ);
			}
		}
		super.unInvoke();
		if((canBeUninvoked())&&(mob!=null))
		{
			if(mob.amDead()) mob.setLocation(null);
			mob.destroy();
		}
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(msg.amISource((MOB)affected)||msg.amISource(((MOB)affected).amFollowing()))
		&&(msg.sourceMinor()==CMMsg.TYP_QUIT))
			unInvoke();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if((!auto)&&(mob.location().domainType()&Room.INDOORS)>0)
		{
			mob.tell("You must be outdoors for this chant to work.");
			return false;
		}
		if((mob.location().domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE))
		{
			mob.tell("This magic will not work here.");
			return false;
		}

		int material=EnvResource.RESOURCE_HEMP;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			invoker=mob;
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"":"^S<S-NAME> chant(s) and summon(s) help from the air.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				MOB target = determineMonster(mob, material);
				if(target!=null)
				{
					if(target.isInCombat()) target.makePeace();
					beneficialAffect(mob,target,0);
					CommonMsgs.follow(target,mob,true);
					if(target.amFollowing()!=mob)
						mob.tell(target.name()+" seems unwilling to follow you.");
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> chant(s), but nothing happens.");

		// return whether it worked
		return success;
	}
	public MOB determineMonster(MOB caster, int material)
	{
		MOB newMOB=(MOB)CMClass.getMOB("GenMOB");
		int level=3;
		newMOB.baseEnvStats().setLevel(level);
		newMOB.baseCharStats().setMyRace(CMClass.getRace("AirElemental"));
		String name="a dustdevil";
		newMOB.setName(name);
		newMOB.setDisplayText(name+" whirls around here");
		newMOB.setDescription("");
		newMOB.setAlignment(500);
		newMOB.baseEnvStats().setAbility(25);
		newMOB.baseEnvStats().setWeight(caster.envStats().level()*caster.envStats().level());
		newMOB.baseCharStats().setStat(CharStats.STRENGTH,caster.envStats().level());
		newMOB.baseEnvStats().setSensesMask(newMOB.baseEnvStats().sensesMask()|EnvStats.CAN_SEE_DARK);
		newMOB.baseEnvStats().setSensesMask(newMOB.baseEnvStats().sensesMask()|EnvStats.CAN_SEE_INVISIBLE);
		newMOB.baseEnvStats().setSensesMask(newMOB.baseEnvStats().sensesMask()|EnvStats.CAN_SEE_HIDDEN);
		newMOB.setLocation(caster.location());
		newMOB.baseEnvStats().setRejuv(Integer.MAX_VALUE);
		newMOB.baseEnvStats().setDamage(1);
		newMOB.baseEnvStats().setAttackAdjustment(0);
		newMOB.baseEnvStats().setArmor(100);
		newMOB.baseCharStats().setStat(CharStats.GENDER,(int)'N');
		newMOB.addNonUninvokableEffect(CMClass.getAbility("Prop_ModExperience"));
		newMOB.setMiscText(newMOB.text());
		newMOB.recoverCharStats();
		newMOB.recoverEnvStats();
		newMOB.recoverMaxState();
		newMOB.resetToMaxState();
		newMOB.bringToLife(caster.location(),true);
		newMOB.setMoney(0);
		newMOB.location().showOthers(newMOB,null,CMMsg.MSG_OK_ACTION,"<S-NAME> appear(s)!");
		newMOB.setStartRoom(null);
		return(newMOB);
	}
}