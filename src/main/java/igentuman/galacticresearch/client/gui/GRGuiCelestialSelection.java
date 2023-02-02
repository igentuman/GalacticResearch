package igentuman.galacticresearch.client.gui;

import com.google.common.collect.Lists;
import igentuman.galacticresearch.common.capability.PlayerSpaceData;
import igentuman.galacticresearch.common.capability.SpaceCapabilityHandler;
import micdoodle8.mods.galacticraft.api.galaxies.*;
import micdoodle8.mods.galacticraft.core.client.gui.screen.GuiCelestialSelection;
import micdoodle8.mods.galacticraft.core.util.ColorUtil;
import micdoodle8.mods.galacticraft.core.util.PlayerUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.client.FMLClientHandler;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GRGuiCelestialSelection extends GuiCelestialSelection {

	public GRGuiCelestialSelection(boolean mapMode, List<CelestialBody> possibleBodies, boolean canCreateStations) {
		super(mapMode, possibleBodies, canCreateStations);
	}

	@Override
	public void initGui() {
		final Minecraft minecraft = FMLClientHandler.instance().getClient();
		final EntityPlayerSP player = minecraft.player;
		final EntityPlayerSP playerBaseClient = PlayerUtil.getPlayerBaseClientFromPlayer(player, false);
		PlayerSpaceData stats = null;

		if (player != null) {
			stats = (PlayerSpaceData) playerBaseClient.getCapability(SpaceCapabilityHandler.PLAYER_SPACE_DATA, null);
		}
		this.bodiesToRender.clear();

		for (Planet planet : GalaxyRegistry.getRegisteredPlanets().values()) {
			if (stats.getUnlockedMissions().contains(planet.getName())) {
				this.bodiesToRender.add(planet);
			}
		}

		for (Moon moon : GalaxyRegistry.getRegisteredMoons().values()) {
			if (stats.getUnlockedMissions().contains(moon.getParentPlanet().getName()) && stats.getUnlockedMissions().contains(moon.getName())) {
				this.bodiesToRender.add(moon);
			}
		}

		for (Satellite satellite : GalaxyRegistry.getRegisteredSatellites().values()) {
			if (stats.getUnlockedMissions().contains(satellite.getParentPlanet().getName())) {
				this.bodiesToRender.add(satellite);
			}
		}

		GuiCelestialSelection.BORDER_SIZE = this.width / 65;
		GuiCelestialSelection.BORDER_EDGE_SIZE = GuiCelestialSelection.BORDER_SIZE / 4;

		for (SolarSystem solarSystem : GalaxyRegistry.getRegisteredSolarSystems().values()) {
			this.bodiesToRender.add(solarSystem.getMainStar());
		}
	}

	@Override
	protected List<CelestialBody> getChildren(Object object) {
		List<CelestialBody> bodyList = Lists.newArrayList();
		final Minecraft minecraft = FMLClientHandler.instance().getClient();
		final EntityPlayerSP player = minecraft.player;
		final EntityPlayerSP playerBaseClient = PlayerUtil.getPlayerBaseClientFromPlayer(player, false);

		PlayerSpaceData stats = null;

		if (player != null) {
			stats = (PlayerSpaceData) playerBaseClient.getCapability(SpaceCapabilityHandler.PLAYER_SPACE_DATA, null);
		}

		if (object instanceof Planet) {
			List<Moon> moons = GalaxyRegistry.getMoonsForPlanet((Planet) object);
			for (Moon moon : moons)
				if (stats.getUnlockedMissions().contains(moon.getName()))
					bodyList.add(moon);
		} else if (object instanceof SolarSystem) {
			List<Planet> planets = GalaxyRegistry.getPlanetsForSolarSystem((SolarSystem) object);
			for (Planet planet : planets)
				if (stats.getUnlockedMissions().contains(planet.getName()))
					bodyList.add(planet);
		}

		Collections.sort(bodyList);

		return bodyList;
	}

	@Override
	public void drawButtons(int mousePosX, int mousePosY) {
		super.drawButtons(mousePosX, mousePosY);
		final int LHS = GuiCelestialSelection.BORDER_SIZE + GuiCelestialSelection.BORDER_EDGE_SIZE;
		drawRect(LHS + 1, (height - LHS) - 5, LHS + 500, (height - LHS) - 20, ColorUtil.to32BitColor(255, 0, 0, 0));
		this.fontRenderer.drawString(I18n.format("gui.celestial_selection.advise"), LHS + 5, (height - LHS) - 15, RED);
	}
}
