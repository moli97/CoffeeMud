package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Skill_Convert extends StdAbility
{
	public String ID() { return "Skill_Convert"; }
	public String name(){ return "Convert";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.INDIFFERENT;}
	private static final String[] triggerStrings = {"CONVERT"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.SKILL;}
	public Environmental newInstance(){	return new Skill_Convert();}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;
		
		if(target.isMonster())
		{
			mob.tell("You can't convert "+target.name()+".");
			return false;
		}
		
		Diety D=mob.getMyDiety();
		if(D==null)
		{
			mob.tell("A faithless one cannot convert anyone.");
			return false;
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			if(target!=mob)
			{
				if(target.getMyDiety()!=null)
				{
					mob.tell(target.name()+" is worshiping "+target.getMyDiety().name()+".  "+target.charStats().HeShe()+" must REBUKE "+target.getMyDiety().charStats().himher()+" first.");
					return false;
				}
				if(target.getMyDiety()==D)
				{
					mob.tell(target.name()+" already worships "+D.name()+".");
					return false;
				}
				try
				{
					if(!target.session().confirm(mob.name()+" is trying to convert you to the worship of "+D.name()+".  Is this what you want (N/y)?","N"))
					{
						mob.location().show(mob,target,Affect.MSG_SPEAK,"<S-NAMEPOSS> attempt to convert <T-NAME> to the worship of "+D.name()+" is rejected.");
						return false;
					}
				}
				catch(Exception e)
				{
					return false;
				}
			}
			Room dRoom=D.location();
			if(dRoom==mob.location()) dRoom=null;
			FullMsg msg=new FullMsg(mob,target,this,Affect.MSG_SPEAK,auto?"<S-NAME> <S-IS-ARE> converted!":"<S-NAME> convert(s) <T-NAME> to the worship of "+D.name()+".");
			FullMsg msg2=new FullMsg(target,D,this,Affect.MSG_SERVE,null);
			if((mob.location().okAffect(msg))
			   &&(mob.location().okAffect(msg2))
			   &&((dRoom==null)||(dRoom.okAffect(msg2))))
			{
				mob.location().send(mob,msg);
				mob.location().send(target,msg2);
				if(dRoom!=null)
					dRoom.send(target,msg2);
				if(mob!=target)
				{
					mob.tell("You gain 200 experience points.");
					mob.charStats().getCurrentClass().gainExperience(mob,null,null,200);
				}
			}
		}
		else
			beneficialWordsFizzle(mob,null,"<S-NAME> attempt(s) to convert <T-NAME>, but <S-IS-ARE> unconvincing.");

		// return whether it worked
		return success;
	}

}