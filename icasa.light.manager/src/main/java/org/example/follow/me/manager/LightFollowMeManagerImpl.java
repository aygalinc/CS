package org.example.follow.me.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Unbind;
import org.apache.felix.ipojo.annotations.Validate;
import org.example.follow.me.api.FollowMeAdministration;
import org.example.follow.me.api.FollowMeConfiguration;
import org.example.follow.me.api.IlluminanceGoal;

import fr.liglab.adele.icasa.device.DeviceListener;
import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.device.light.BinaryLight;
import fr.liglab.adele.icasa.device.light.DimmerLight;
import fr.liglab.adele.icasa.device.presence.PresenceSensor;


/**
 * Created by aygalinc on 28/10/16.
 */
@Component
@Instantiate(name="FollowMeManager1")
@Provides(specifications = FollowMeAdministration.class)
public class LightFollowMeManagerImpl implements  FollowMeAdministration
{
	
	@Requires
	private FollowMeConfiguration followMeConfiguration;

	@Override
	public void setIlluminancePreference(IlluminanceGoal illuminanceGoal) {
		// TODO Auto-generated method stub
		followMeConfiguration.setMaximumNumberOfLightsToTurnOn(illuminanceGoal.getNumberOfLightsToTurnOn());
		followMeConfiguration.setMaximumAllowedEnergyInRoom(100*illuminanceGoal.getNumberOfLightsToTurnOn());		
	}

	@Override
	public IlluminanceGoal getIlluminancePreference() {
		// TODO Auto-generated method stub
		IlluminanceGoal illuminanceGoal;
		
		switch (followMeConfiguration.getMaximumNumberOfLightsToTurnOn() ) {			
		case 1:
			illuminanceGoal = IlluminanceGoal.SOFT;
			break;
		case 2:
			illuminanceGoal = IlluminanceGoal.MEDIUM;
			break;
		case 3:
			illuminanceGoal = IlluminanceGoal.FULL;
			break;

		default:
			illuminanceGoal = IlluminanceGoal.MEDIUM;
			setIlluminancePreference(illuminanceGoal);
			
			break;
		}
		
		return illuminanceGoal;
	}
	
	
}
