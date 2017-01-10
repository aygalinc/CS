package org.example.follow.me.regulator;

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
import org.example.follow.me.api.FollowMeConfiguration;

import fr.liglab.adele.icasa.device.DeviceListener;
import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.device.light.BinaryLight;
import fr.liglab.adele.icasa.device.light.DimmerLight;
import fr.liglab.adele.icasa.device.presence.PresenceSensor;

/**
 * Created by aygalinc on 28/10/16.
 */
@Component
@Instantiate(name="FollowMeRegulator1")
@Provides(specifications = FollowMeConfiguration.class)
public class LightFollowMeRegulatorImpl implements DeviceListener, FollowMeConfiguration {

	/** Field for presenceSensors dependency */
	@Requires(id="presenceSensor", optional=true )	
	private PresenceSensor[] presenceSensors;
	/** Field for binaryLights dependency */
	@Requires(id="binaryLight", optional=true )
	private BinaryLight[] binaryLights;
	/** Field for DimmerLights dependency */
	@Requires(id="dimmerLight", optional=true )
	private DimmerLight[] dimmerLights;

	/**
	 * The maximum number of lights to turn on when a user enters the room :
	 **/
	private int maxLightsToTurnOnPerRoom = 10;
	
    /**
    * The maximum energy consumption allowed in a room in Watt:
    **/
    private double maximumEnergyConsumptionAllowedInARoom = 180.0d;

	/**
	 * The name of the LOCATION property
	 */
	public static final String LOCATION_PROPERTY_NAME = "Location";

	/**
	 * The name of the location for unknown value
	 */
	public static final String LOCATION_UNKNOWN = "unknown";
	

	/**
	 * Bind Method for binaryLights dependency. This method is not mandatory and
	 * implemented for debug purpose only.
	 */
	@Bind(id ="binaryLight")
	public void bindBinaryLight(BinaryLight binaryLight, Map<Object, Object> properties) {
		System.out.println("bind binary light " + binaryLight.getSerialNumber());
	}

	/**
	 * Unbind Method for binaryLights dependency. This method is not mandatory
	 * and implemented for debug purpose only.
	 */
	@Unbind(id="binaryLight")
	public void unbindBinaryLight(BinaryLight binaryLight, Map<Object, Object> properties) {
		System.out.println("unbind binary light " + binaryLight.getSerialNumber());
	}

	/**
	 * Bind Method for PresenceSensors dependency. This method is used to manage
	 * device listener.
	 */
	@Bind(id="presenceSensor")
	public synchronized void bindPresenceSensor(PresenceSensor presenceSensor, Map properties) {
		// Add the listener to the presence sensor
		presenceSensor.addListener(this); // ..
	}

	/**
	 * Unbind Method for PresenceSensors dependency. This method will be used to
	 * manage device listener.
	 */
	@Unbind(id="presenceSensor")
	public synchronized void unbindPresenceSensor(PresenceSensor presenceSensor, Map properties) {
		// Remove the listener from the presence sensor
		presenceSensor.removeListener(this); // ..
	}

	/** Component Lifecycle Method */
	@Invalidate
	public synchronized void stop() {
		for (PresenceSensor sensor : presenceSensors) {
			sensor.removeListener(this);
		}
	}

	/** Component Lifecycle Method */
	@Validate
	public void start() {
		System.out.println("Component is starting...");
	}

	@Override
	public void deviceAdded(GenericDevice arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deviceEvent(GenericDevice arg0, Object arg1) {
		// TODO Auto-generated method stub
		

	}

	@Override
	public void devicePropertyAdded(GenericDevice arg0, String arg1) {
		// TODO Auto-generated method stub

	}

	/**
	 * This method is part of the DeviceListener interface and is called when a
	 * subscribed device property is modified.
	 * 				maximumEnergyConsumptionAllowedInARoom
	 * @param device
	 *            is the device whose property has been modified.
	 * @param propertyName
	 *            is the name of the modified property.
	 * @param oldValue
	 *            is the old value of the property
	 * @param newValue
	 *            is the new value of the property
	 */
	@Override
	public void devicePropertyModified(GenericDevice device, String propertyName, Object oldValue, Object newValue) {
		PresenceSensor changingSensor = (PresenceSensor) device;
		int icpt = 0;
		double EnergyConsumptionAllowedInARoom = 0;
		
		// check the change is related to presence sensing
		if (propertyName.equals(PresenceSensor.PRESENCE_SENSOR_SENSED_PRESENCE)) {
			// get the location where the sensor is:
			String detectorLocation = (String) changingSensor.getPropertyValue(LOCATION_PROPERTY_NAME);
			// if the location is known :
			if (!detectorLocation.equals(LOCATION_UNKNOWN)) {
				// get the related binary lights
				List<BinaryLight> sameLocationLigths = getBinaryLightFromLocation(detectorLocation);
				List<DimmerLight> sameDimmerLocationLights = getDimmerLightFromLocation(detectorLocation);
				

				for (DimmerLight dimmerLight : sameDimmerLocationLights) {
					// and switch them on/off depending on the sensed presence
					if (changingSensor.getSensedPresence() && (icpt < maxLightsToTurnOnPerRoom) && ( (100*dimmerLight.getPowerLevel() )+EnergyConsumptionAllowedInARoom <= maximumEnergyConsumptionAllowedInARoom) ) {
						dimmerLight.setPowerLevel(.5);
						EnergyConsumptionAllowedInARoom += 100*dimmerLight.getPowerLevel();								
								
						icpt++;
					} else {
						dimmerLight.setPowerLevel(0);
					}
				}
				
				for (BinaryLight binaryLight : sameLocationLigths) {
					// and switch them on/off depending on the sensed presence
					if (changingSensor.getSensedPresence() && (icpt < maxLightsToTurnOnPerRoom) && (EnergyConsumptionAllowedInARoom+100 <= maximumEnergyConsumptionAllowedInARoom)) {
						binaryLight.turnOn();
						EnergyConsumptionAllowedInARoom += 100;
						icpt++;
					} else {
						binaryLight.turnOff();
					}
				}
			}
		}
	}

	@Override
	public void devicePropertyRemoved(GenericDevice arg0, String arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deviceRemoved(GenericDevice arg0) {
		// TODO Auto-generated method stub

	}

	/**
	 * Return all BinaryLight from the given location
	 * 
	 * @param location
	 *            : the given location
	 * @return the list of matching BinaryLights
	 */
	private synchronized List<BinaryLight> getBinaryLightFromLocation(String location) {
		List<BinaryLight> binaryLightsLocation = new ArrayList<BinaryLight>();

		for (BinaryLight binLight : binaryLights) {
			if (binLight.getPropertyValue(LOCATION_PROPERTY_NAME).equals(location)) {
				binaryLightsLocation.add(binLight);

			}
		}
		return binaryLightsLocation;
	}
	
	/**
	 * Return all DimmerLight from the given location
	 * 
	 * @param location
	 *            : the given location
	 * @return the list of matching DimmerLight
	 */
	private synchronized List<DimmerLight> getDimmerLightFromLocation(String location) {
		List<DimmerLight> dimmerLightsLocation = new ArrayList<DimmerLight>();

		for (DimmerLight dinLight : dimmerLights) {
			if (dinLight.getPropertyValue(LOCATION_PROPERTY_NAME).equals(location)) {
				dimmerLightsLocation.add(dinLight);

			}
		}
		return dimmerLightsLocation;
	}

	/** Bind Method for DimmerLights dependency */
	@Bind(id="dimmerLight")
	public void bindDimmerLight(DimmerLight dimmerLight, Map properties) {
		// TODO: Add your implementation code here
	}

	/** Unbind Method for DimmerLights dependency */
	@Unbind(id="dimmerLight")
	public void unbindDimmerLight(DimmerLight dimmerLight, Map properties) {
		// TODO: Add your implementation code here
	}

	@Override
	public int getMaximumNumberOfLightsToTurnOn() {
		// TODO Auto-generated method stub
		return this.maxLightsToTurnOnPerRoom;
	}

	@Override
	public void setMaximumNumberOfLightsToTurnOn(int maximumNumberOfLightsToTurnOn) {
		// TODO Auto-generated method stub
		this.maxLightsToTurnOnPerRoom = maximumNumberOfLightsToTurnOn;
	}

	@Override
	public double getMaximumAllowedEnergyInRoom() {
		// TODO Auto-generated method stub
		return maximumEnergyConsumptionAllowedInARoom;
	}

	@Override
	public void setMaximumAllowedEnergyInRoom(double maximumEnergy) {
		// TODO Auto-generated method stub
		maximumEnergyConsumptionAllowedInARoom = maximumEnergy;
		
	}
}
