package igentuman.galacticresearch.client.gui;

import com.google.common.collect.Lists;
import com.mjr.extraplanets.client.gui.screen.CustomCelestialSelection;
import com.mjr.mjrlegendslib.util.MessageUtilities;
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
		return stats.getUnlockedMissions().contains(name) ||
				Arrays.asList(ModConfig.researchSystem.default_researched_bodies).contains(name);
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
		Iterator var1 = GalaxyRegistry.getRegisteredSolarSystems().values().iterator();

		while(var1.hasNext()) {
			SolarSystem solarSystem = (SolarSystem)var1.next();
			if (solarSystem.getUnlocalizedParentGalaxyName().equalsIgnoreCase(this.currentGalaxyName)) {
				this.bodiesToRender.add(solarSystem.getMainStar());
			}
		}

		var1 = GalaxyRegistry.getRegisteredPlanets().values().iterator();

		while(var1.hasNext()) {
			Planet planet = (Planet)var1.next();
			if (planet.getParentSolarSystem().getUnlocalizedParentGalaxyName().equalsIgnoreCase(this.currentGalaxyName)) {
				if (isUnlocked(planet.getName(), stats)) {
					this.bodiesToRender.add(planet);
				}
			}
		}

		var1 = GalaxyRegistry.getRegisteredMoons().values().iterator();

		while(true) {
			while(var1.hasNext()) {
				Moon moon = (Moon)var1.next();
				if (moon.getParentPlanet() != null && moon.getParentPlanet().getParentSolarSystem().getUnlocalizedParentGalaxyName().equalsIgnoreCase(this.currentGalaxyName)) {
					if (isUnlocked(moon.getParentPlanet().getName(), stats) && isUnlocked(moon.getName(), stats)) {
						this.bodiesToRender.add(moon);
					}
				} else if (moon.getParentPlanet() == null) {
					MessageUtilities.fatalErrorMessageToLog("extraplanets", "The moon " + moon.getUnlocalizedName() + " seems to have a null parent planet. Please check the log for other errors!");
				}
			}

			var1 = GalaxyRegistry.getRegisteredSatellites().values().iterator();

			while(var1.hasNext()) {
				Satellite satellite = (Satellite)var1.next();
				if (satellite.getParentPlanet().getParentSolarSystem().getUnlocalizedParentGalaxyName().equalsIgnoreCase(this.currentGalaxyName)) {
					this.bodiesToRender.add(satellite);
				}
			}

			GuiCelestialSelection.BORDER_SIZE = this.width / 65;
			GuiCelestialSelection.BORDER_EDGE_SIZE = GuiCelestialSelection.BORDER_SIZE / 4;
			return;
		}
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
		Iterator var3;
		List planets;
		Iterator var6;
		if (object instanceof Planet) {
			var3 = GalaxyRegistry.getRegisteredPlanets().values().iterator();

			label81:
			while(true) {
				while(true) {
					Planet planet;
					do {
						do {
							if (!var3.hasNext()) {
								break label81;
							}

							planet = (Planet)var3.next();
						} while(!planet.equals(object));
					} while(!planet.getParentSolarSystem().getUnlocalizedParentGalaxyName().equalsIgnoreCase(this.currentGalaxyName));

					planets = GalaxyRegistry.getMoonsForPlanet((Planet)object);
					var6 = planets.iterator();

					while(var6.hasNext()) {
						Moon moon = (Moon)var6.next();
						if (stats.getUnlockedMissions().contains(moon.getName())) {
							bodyList.add(moon);
						}
					}
				}
			}
		} else if (object instanceof SolarSystem) {
			var3 = GalaxyRegistry.getRegisteredSolarSystems().values().iterator();

			label57:
			while(true) {
				while(true) {
					SolarSystem solarSystems;
					do {
						do {
							if (!var3.hasNext()) {
								break label57;
							}

							solarSystems = (SolarSystem)var3.next();
						} while(!solarSystems.equals(object));
					} while(!solarSystems.getUnlocalizedParentGalaxyName().equalsIgnoreCase(this.currentGalaxyName));

					planets = GalaxyRegistry.getPlanetsForSolarSystem((SolarSystem)object);
					var6 = planets.iterator();

					while(var6.hasNext()) {
						Planet planet = (Planet)var6.next();
						if (stats.getUnlockedMissions().contains(planet.getName())) {
							bodyList.add(planet);
						}
					}
				}
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
