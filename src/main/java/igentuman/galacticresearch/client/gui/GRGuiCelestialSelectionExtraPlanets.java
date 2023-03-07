package igentuman.galacticresearch.client.gui;

import com.google.common.collect.Lists;
import com.mjr.extraplanets.client.gui.screen.CustomCelestialSelection;
import com.mjr.mjrlegendslib.util.MessageUtilities;
import igentuman.galacticresearch.GalacticResearch;
import igentuman.galacticresearch.ModConfig;
import igentuman.galacticresearch.client.capability.PlayerClientSpaceData;
import igentuman.galacticresearch.client.capability.SpaceClientCapabilityHandler;
import micdoodle8.mods.galacticraft.api.galaxies.*;
import micdoodle8.mods.galacticraft.core.GalacticraftCore;
import micdoodle8.mods.galacticraft.core.client.gui.screen.GuiCelestialSelection;
import micdoodle8.mods.galacticraft.core.util.ColorUtil;
import micdoodle8.mods.galacticraft.core.util.PlayerUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.apache.logging.log4j.Level;

import java.util.*;

public class GRGuiCelestialSelectionExtraPlanets extends CustomCelestialSelection {
	private List<String> galaxies = new ArrayList();
	private String currentGalaxyName;
	private SolarSystem currentGalaxyMainSystem;

	public GRGuiCelestialSelectionExtraPlanets(boolean mapMode, List<CelestialBody> possibleBodies, boolean canCreateStations) {
		super(mapMode, possibleBodies, canCreateStations);
		Iterator var4 = GalaxyRegistry.getRegisteredSolarSystems().values().iterator();

		while(var4.hasNext()) {
			SolarSystem system = (SolarSystem)var4.next();
			String name = system.getUnlocalizedParentGalaxyName();
			if (!this.galaxies.contains(name)) {
				this.galaxies.add(name);
			}
		}

		this.currentGalaxyName = "galaxy.milky_way";
		this.currentGalaxyMainSystem = GalacticraftCore.solarSystemSol;
	}

	private boolean isUnlocked(String name, PlayerClientSpaceData stats)
	{
		return stats.getUnlockedMissions().contains(name.toLowerCase()) ||
				Arrays.asList(ModConfig.researchSystem.default_researched_bodies).contains(name.toLowerCase());
	}

	@Override
	public void func_73866_w_() {
		Minecraft minecraft = FMLClientHandler.instance().getClient();
		EntityPlayerSP player = minecraft.player;
		EntityPlayerSP playerBaseClient = PlayerUtil.getPlayerBaseClientFromPlayer(player, false);
		PlayerClientSpaceData stats = null;

		if (player != null) {
			stats = playerBaseClient.getCapability(SpaceClientCapabilityHandler.PLAYER_SPACE_CLIENT_DATA, null);
		}
		this.bodiesToRender.clear();

		for(SolarSystem solarSystem: GalaxyRegistry.getRegisteredSolarSystems().values()) {
			if (solarSystem.getUnlocalizedParentGalaxyName().equalsIgnoreCase(this.currentGalaxyName)) {
				this.bodiesToRender.add(solarSystem.getMainStar());
			}
		}

		for(Planet planet: GalaxyRegistry.getRegisteredPlanets().values()) {
			if (planet.getParentSolarSystem().getUnlocalizedParentGalaxyName().equalsIgnoreCase(this.currentGalaxyName)) {
				GalacticResearch.instance.logger.log(Level.INFO, planet.getName());
				if (isUnlocked(planet.getName(), stats)) {
					this.bodiesToRender.add(planet);
				}
			}
		}

		for(Moon moon: GalaxyRegistry.getRegisteredMoons().values()) {
			if (moon.getParentPlanet() != null && moon.getParentPlanet().getParentSolarSystem().getUnlocalizedParentGalaxyName().equalsIgnoreCase(this.currentGalaxyName)) {
				GalacticResearch.instance.logger.log(Level.INFO, moon.getName());
				GalacticResearch.instance.logger.log(Level.INFO, moon.getParentPlanet().getName());
				if (isUnlocked(moon.getParentPlanet().getName(), stats) && isUnlocked(moon.getName(), stats)) {
					this.bodiesToRender.add(moon);
				}
			} else if (moon.getParentPlanet() == null) {
				MessageUtilities.fatalErrorMessageToLog("extraplanets", "The moon " + moon.getUnlocalizedName() + " seems to have a null parent planet. Please check the log for other errors!");
			}
		}

		for(Satellite sat: GalaxyRegistry.getRegisteredSatellites().values()) {
			if (sat.getParentPlanet().getParentSolarSystem().getUnlocalizedParentGalaxyName().equalsIgnoreCase(this.currentGalaxyName)) {
				this.bodiesToRender.add(sat);
			}
		}

		GuiCelestialSelection.BORDER_SIZE = this.width / 65;
		GuiCelestialSelection.BORDER_EDGE_SIZE = GuiCelestialSelection.BORDER_SIZE / 4;
	}

	protected List<CelestialBody> getChildren(Object object) {
		List<CelestialBody> bodyList = Lists.newArrayList();
		Minecraft minecraft = FMLClientHandler.instance().getClient();
		EntityPlayerSP player = minecraft.player;
		EntityPlayerSP playerBaseClient = PlayerUtil.getPlayerBaseClientFromPlayer(player, false);

		PlayerClientSpaceData stats = null;

		if (player != null) {
			stats = playerBaseClient.getCapability(SpaceClientCapabilityHandler.PLAYER_SPACE_CLIENT_DATA, null);
		}

		if (object instanceof Planet) {
			for (Planet planet : GalaxyRegistry.getRegisteredPlanets().values()) {
				if (!planet.equals(object)) {
					continue;
				}
				if (!planet.getParentSolarSystem().getUnlocalizedParentGalaxyName().equalsIgnoreCase(this.currentGalaxyName)) {
					continue;
				}
				for (Moon moon : GalaxyRegistry.getMoonsForPlanet((Planet) object)) {
					if (isUnlocked(moon.getName(), stats)) {
						bodyList.add(moon);
					}
				}
				break;
			}

		} else if (object instanceof SolarSystem) {
			for (SolarSystem solarSystem : GalaxyRegistry.getRegisteredSolarSystems().values()) {
				if (!solarSystem.equals(object)) {
					continue;
				}
				if (!solarSystem.getUnlocalizedParentGalaxyName().equalsIgnoreCase(this.currentGalaxyName)) {
					continue;
				}
				for (Planet planet : GalaxyRegistry.getPlanetsForSolarSystem((SolarSystem) object)) {
					if (isUnlocked(planet.getName(), stats)) {
						bodyList.add(planet);
					}
				}
				break;
			}
		}

		Collections.sort(bodyList);
		return bodyList;
	}

	@Override
	public void drawButtons(int mousePosX, int mousePosY) {
		super.drawButtons(mousePosX, mousePosY);
		int LHS = CustomCelestialSelection.BORDER_SIZE + CustomCelestialSelection.BORDER_EDGE_SIZE;
		drawRect(LHS + 1, (height - LHS) - 5, LHS + 500, (height - LHS) - 20, ColorUtil.to32BitColor(255, 0, 0, 0));
		this.fontRenderer.drawString(I18n.format("gui.celestial_selection.advise"), LHS + 5, (height - LHS) - 15, RED);
	}
}
