package nl.theepicblock.immersive_cursedness;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@SuppressWarnings("FieldMayBeFinal")
@me.shedaniel.autoconfig.annotation.Config(name = "immersive-cursedness")
public class IC_Config implements ConfigData {
	public int horizontalSendLimit = 70;
	@Comment("Should usually be atmosphereRadius+2")
	public int portalDepth = 30;
	@Comment("The radius where the outer block of the atmosphere should be")
	private int atmosphereRadius = 28;
	@Comment("Measured in chunks")
	public int renderDistance = 3;
	public boolean debugParticles = false;
	@Comment("Whether Immersive Cursedness is on by default")
	public boolean defaultEnabled = true;

	public transient double squaredAtmosphereRadius;
	public transient double squaredAtmosphereRadiusPlusOne;
	public transient double squaredAtmosphereRadiusMinusOne;

	@Override
	public void validatePostLoad() throws ValidationException {
		if (atmosphereRadius >= portalDepth) {
			throw new ValidationException("atmosphereRadius should be smaller then portalDepth");
		}

		squaredAtmosphereRadius = Math.pow(atmosphereRadius, 2);
		squaredAtmosphereRadiusPlusOne = Math.pow(atmosphereRadius+1, 2);
		squaredAtmosphereRadiusMinusOne = Math.pow(atmosphereRadius-1, 2);
	}
}
