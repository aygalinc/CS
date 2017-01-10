package org.example.follow.me.manager.command;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Requires;
import org.example.follow.me.api.FollowMeAdministration;
import org.example.follow.me.api.IlluminanceGoal;

import fr.liglab.adele.icasa.command.handler.Command;
import fr.liglab.adele.icasa.command.handler.CommandProvider;

//Define this class as an implementation of a component :
@Component
//Create an instance of the component
@Instantiate
//Use the handler command and declare the command as a command provider. The
//namespace is used to prevent name collision.
@CommandProvider(namespace = "followme")
public class FollowMeManagerCommandImpl {

    // Declare a dependency to a FollowMeAdministration service
    @Requires
    private FollowMeAdministration m_administrationService;


    /**
     * Felix shell command implementation to sets the illuminance preference.
     *
     * @param goal the new illuminance preference ("SOFT", "MEDIUM", "FULL")
     */

    // Each command should start with a @Command annotation
    @Command
    public void setIlluminancePreference(String goal) {
        // The targeted goal
        // Fix the init
        IlluminanceGoal illuminanceGoal = IlluminanceGoal.MEDIUM;
        // Here you have to convert the goal string into an illuminance
        // goal and fail if the entry is not "SOFT", "MEDIUM" or "HIGH"
        
        if( goal.compareToIgnoreCase("SOFT") == 0 ){
        	illuminanceGoal = IlluminanceGoal.SOFT;
        }else if ( goal.compareToIgnoreCase("MEDIUM") == 0  ){
        	illuminanceGoal = IlluminanceGoal.MEDIUM;
        }else if ( goal.compareToIgnoreCase("FULL") == 0  ){
        	illuminanceGoal = IlluminanceGoal.FULL;
        }else{
        	// error
        	System.out.println("Only SOFT, MEDIUM or FULL is tolerated");
        	return;
        }

        //call the administration service to configure it :
        m_administrationService.setIlluminancePreference(illuminanceGoal);
    }

    @Command
    public void getIlluminancePreference(){
        //implement the command that print the current value of the goal
        System.out.print("The illuminance goal is "); //...
        switch (m_administrationService.getIlluminancePreference()) {
		case FULL:
			System.out.println("FULL.");
			break;
		case SOFT:
			System.out.println("SOFT.");
			break;
		case MEDIUM:
			System.out.println("MEDIUM.");
			break;
		default:
			break;
		}
    }

}