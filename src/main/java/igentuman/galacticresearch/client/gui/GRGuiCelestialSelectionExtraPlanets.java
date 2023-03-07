package igentuman.galacticresearch.client.gui;

import com.google.common.collect.Lists;
import com.mjr.extraplanets.Config;
import com.mjr.extraplanets.api.celestialBody.CelestialBodyMaterialRegistry;
import com.mjr.extraplanets.api.prefabs.world.WorldProviderRealisticSpace;
import com.mjr.extraplanets.client.gui.screen.CustomCelestialSelection;
import com.mjr.mjrlegendslib.util.MCUtilities;
import com.mjr.mjrlegendslib.util.MessageUtilities;
import com.mjr.mjrlegendslib.util.TranslateUtilities;
import igentuman.galacticresearch.ModConfig;
import igentuman.galacticresearch.client.capability.PlayerClientSpaceData;
import igentuman.galacticresearch.client.capability.SpaceClientCapabilityHandler;
import micdoodle8.mods.galacticraft.api.galaxies.*;
import micdoodle8.mods.galacticraft.api.prefab.world.gen.WorldProviderSpace;
import micdoodle8.mods.galacticraft.core.GalacticraftCore;
import micdoodle8.mods.galacticraft.core.client.gui.screen.GuiCelestialSelection;
import micdoodle8.mods.galacticraft.core.dimension.WorldProviderMoon;
import micdoodle8.mods.galacticraft.core.dimension.WorldProviderSpaceStation;
import micdoodle8.mods.galacticraft.core.util.ColorUtil;
import micdoodle8.mods.galacticraft.core.util.PlayerUtil;
import micdoodle8.mods.galacticraft.core.util.WorldUtil;
import micdoodle8.mods.galacticraft.planets.asteroids.dimension.WorldProviderAsteroids;
import micdoodle8.mods.galacticraft.planets.mars.dimension.WorldProviderMars;
import micdoodle8.mods.galacticraft.planets.venus.dimension.WorldProviderVenus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.WorldProvider;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import static igentuman.galacticresearch.GalacticResearch.MODID;

public class GRGuiCelestialSelectionExtraPlanets extends CustomCelestialSelection {
	private List<String> galaxies = new ArrayList();
	private String currentGalaxyName;
	private SolarSystem currentGalaxyMainSystem;
	private boolean showGalaxies = false;
	private int mousePosX = 0;
	private int mousePosY = 0;
	private float partialTicks = 0.0F;

	public GRGuiCelestialSelectionExtraPlanets(boolean mapMode, List<CelestialBody> possibleBodies, boolean canCreateStations) {
		super(mapMode, possibleBodies, canCreateStations);
		Iterator var4 = GalaxyRegistry.getRegisteredSolarSystems().values().iterator();

		Minecraft minecraft = FMLClientHandler.instance().getClient();
		EntityPlayerSP player = minecraft.player;
		EntityPlayerSP playerBaseClient = PlayerUtil.getPlayerBaseClientFromPlayer(player, false);
		PlayerClientSpaceData stats = null;

		if (player != null) {
			stats = playerBaseClient.getCapability(SpaceClientCapabilityHandler.PLAYER_SPACE_CLIENT_DATA, null);
		}

		while(var4.hasNext()) {
			SolarSystem system = (SolarSystem)var4.next();
			String name = system.getUnlocalizedParentGalaxyName();
			if (!this.galaxies.contains(name) && isUnlocked(system.getName(), stats)) {
				this.galaxies.add(name);
			}
		}

		this.currentGalaxyName = "galaxy.milky_way";
		this.currentGalaxyMainSystem = GalacticraftCore.solarSystemSol;
	}

	private boolean isUnlocked(String name, PlayerClientSpaceData stats)
	{
		return stats.getUnlockedMissions().contains(name.toLowerCase()) ||
				Arrays.asList(ModConfig.researchSystem.default_researched_objects).contains(name.toLowerCase());
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
				if(isUnlocked(solarSystem.getName(), stats)) {
					this.bodiesToRender.add(solarSystem.getMainStar());
				}
			}
		}

		for(Planet planet: GalaxyRegistry.getRegisteredPlanets().values()) {
			if (planet.getParentSolarSystem().getUnlocalizedParentGalaxyName().equalsIgnoreCase(this.currentGalaxyName)) {
				if (isUnlocked(planet.getName(), stats) && isUnlocked(planet.getParentSolarSystem().getName(), stats)) {
					this.bodiesToRender.add(planet);
				}
			}
		}

		for(Moon moon: GalaxyRegistry.getRegisteredMoons().values()) {
			if (moon.getParentPlanet() != null && moon.getParentPlanet().getParentSolarSystem().getUnlocalizedParentGalaxyName().equalsIgnoreCase(this.currentGalaxyName)) {
				if (isUnlocked(moon.getParentPlanet().getName(), stats) && isUnlocked(moon.getName(), stats) && isUnlocked(moon.getParentPlanet().getParentSolarSystem().getName(), stats)) {
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

	public void drawCustomButtons(int mousePosX, int mousePosY) {
		try {
			if (this.viewState != EnumView.PROFILE) {
				final int LHS = GuiCelestialSelection.BORDER_SIZE + GuiCelestialSelection.BORDER_EDGE_SIZE;
				if (this.selectedBody != null) {
					GL11.glColor4f(0.0F, 0.6F, 1.0F, 1);
					this.mc.renderEngine.bindTexture(GuiCelestialSelection.guiMain1);

					int yOffset = 27;
					int widthSizeOffset = 75;
					int xOffset = (this.width - 270) - LHS;
					boolean showHoveredMats = false;
					int showHoveredMatsX = 0;
					int showHoveredMatsY = 0;
					this.drawTexturedModalRect(xOffset, yOffset, 93 + widthSizeOffset, 4, 159, 102, 93, 4, false, false);
					for (int barY = 0; barY < 25; ++barY) {
						this.drawTexturedModalRect(xOffset, yOffset + barY * this.fontRenderer.FONT_HEIGHT + 4, 93 + widthSizeOffset, this.fontRenderer.FONT_HEIGHT + 10, 159, 106, 93, this.fontRenderer.FONT_HEIGHT, false, false);
					}
					for (int barx = 0; barx < 1; ++barx) {
						this.drawTexturedModalRect(xOffset + barx, yOffset * this.fontRenderer.FONT_HEIGHT + 20, 93 + widthSizeOffset, this.fontRenderer.FONT_HEIGHT / 2, 159, 106, 1, this.fontRenderer.FONT_HEIGHT, false, false);
					}
					if (!(this.selectedBody instanceof Star)) {
						WorldProvider temp = null;
						if (this.selectedBody.getReachable() && !this.selectedBody.getUnlocalizedName().contains("overworld") && !(this.selectedBody instanceof Satellite))
							if (MCUtilities.isClient())
								temp = WorldUtil.getProviderForDimensionClient(this.selectedBody.getDimensionID());
							else
								temp = WorldUtil.getProviderForDimensionServer(this.selectedBody.getDimensionID());
						this.drawString(this.fontRenderer, "------------------------", xOffset + 10, yOffset + 2, BLUE);
						this.drawString(this.fontRenderer, TranslateUtilities.translate("gui.general_details.name") + ": ", xOffset + 10, yOffset + 8, BLUE);
						this.drawString(this.fontRenderer, "------------------------", xOffset + 10, yOffset + 14, BLUE);
						String type;
						if (GalaxyRegistry.getRegisteredPlanets().containsValue(this.selectedBody))
							type = TranslateUtilities.translate("gui.type_planet.name");
						else if (GalaxyRegistry.getRegisteredMoons().containsValue(this.selectedBody))
							type = TranslateUtilities.translate("gui.type_moon.name");
						else if (GalaxyRegistry.getRegisteredSatellites().containsValue(this.selectedBody))
							type = TranslateUtilities.translate("gui.type_satellite.name");
						else
							type = TranslateUtilities.translate("gui.type_unknown.name");

						this.drawString(this.fontRenderer, TranslateUtilities.translate("gui.celestial_body_type.name") + ": " + type, xOffset + 10, yOffset + 23, 14737632);
						if ((this.selectedBody instanceof Planet))
							this.drawString(this.fontRenderer, TranslateUtilities.translate("gui.celestial_body_amount_of_moons.name") + ": " + this.getChildren(this.selectedBody).size(), xOffset + 10, yOffset + 33, 14737632);
						this.drawString(this.fontRenderer, "Materials: " + TextFormatting.GOLD + "Hover to Show", xOffset + 10, yOffset + 43, 14737632);
						if (mousePosX >= xOffset + 60 && mousePosY >= yOffset + 41) {
							if (mousePosX <= xOffset + 140 && mousePosY <= yOffset + 50) {
								showHoveredMats = true;
								showHoveredMatsX = xOffset + 10;
								showHoveredMatsY = yOffset + 43;
							}
						}
						yOffset = yOffset + 50;
						this.drawString(this.fontRenderer, "------------------------", xOffset + 10, yOffset + 2, BLUE);
						this.drawString(this.fontRenderer, TranslateUtilities.translate("gui.celestial_details.name") + ": ", xOffset + 10, yOffset + 8, BLUE);
						this.drawString(this.fontRenderer, "------------------------", xOffset + 10, yOffset + 14, BLUE);
						this.drawString(this.fontRenderer, TranslateUtilities.translate("gui.celestial_body_orbit_time.name") + ": " + this.selectedBody.getRelativeOrbitTime() * 24 + " " + TranslateUtilities.translate("gui.type_days.name"),
								xOffset + 10, yOffset + 23, 14737632);
						float gravity = 0;
						long dayLength = 0;
						if (temp instanceof WorldProviderSpace) {
							if (this.selectedBody.getReachable() && !(this.selectedBody instanceof Satellite) && !this.selectedBody.getUnlocalizedName().toLowerCase().contains("overworld")) {
								gravity = ((WorldProviderSpace) temp).getGravity();
								dayLength = ((WorldProviderSpace) temp).getDayLength() / 1000;
							} else if (this.selectedBody.getUnlocalizedName().toLowerCase().contains("overworld")) {
								gravity = 1;
								dayLength = 24;
							}
							this.drawString(this.fontRenderer, TranslateUtilities.translate("gui.celestial_body_gravity.name") + ": " + (this.selectedBody.getReachable() ? gravity : TranslateUtilities.translate("gui.type_unknown.name")),
									xOffset + 10, yOffset + 33, 14737632);
							this.drawString(this.fontRenderer, TranslateUtilities.translate("gui.celestial_body_day_lengh.name") + ": "
									+ (this.selectedBody.getReachable() ? dayLength + " " + TranslateUtilities.translate("gui.type_hours.name") : TranslateUtilities.translate("gui.type_unknown.name")), xOffset + 10, yOffset + 43, 14737632);
							yOffset = yOffset + 55;
							this.drawString(this.fontRenderer, "------------------------", xOffset + 10, yOffset + 2, BLUE);
							this.drawString(this.fontRenderer, TranslateUtilities.translate("gui.general_features_details.name") + ": ", xOffset + 10, yOffset + 8, BLUE);
							this.drawString(this.fontRenderer, "------------------------", xOffset + 10, yOffset + 14, BLUE);
							double meteorFrequency = 0;
							if (temp != null && !(this.selectedBody instanceof Satellite) && !this.selectedBody.getUnlocalizedName().contains("overworld")) {
								double number = ((WorldProviderSpace) temp).getMeteorFrequency();
								BigDecimal bd = new BigDecimal(number).setScale(7, RoundingMode.DOWN);
								meteorFrequency = bd.doubleValue();
							} else {
								meteorFrequency = 0;
							}
							this.drawString(this.fontRenderer,
									TranslateUtilities.translate("gui.celestial_body_meteor_frequency.name") + ": " + (this.selectedBody.getReachable() ? meteorFrequency : TranslateUtilities.translate("gui.type_unknown.name")), xOffset + 10,
									yOffset + 23, 14737632);

							String name = this.selectedBody.getUnlocalizedName().toLowerCase();
							String hasDungeon = TranslateUtilities.translate("gui.type_unknown.name");
							if (name.contains("moon") || name.contains("venus") || name.contains("mars") || name.contains("mercury") || name.contains("jupiter") || name.contains("saturn") || name.contains("uranus") || name.contains("neptune")
									|| name.contains("pluto") || name.contains("eris"))
								hasDungeon = "true";
							if (name.contains("overworld") || name.contains("ceres") || name.contains("kepler22b") || name.contains("asteroids") || name.contains("phobos") || name.contains("deimos") || name.contains("io") || name.contains("europa")
									|| name.contains("ganymede") || name.contains("callisto") || name.contains("rhea") || name.contains("titan") || name.contains("iapetus") || name.contains("titania") || name.contains("oberon")
									|| name.contains("triton"))
								hasDungeon = "false";
							this.drawString(this.fontRenderer, TranslateUtilities.translate("gui.celestial_body_has_dungeon.name") + ": " + hasDungeon, xOffset + 10, yOffset + 32, 14737632);
							yOffset = yOffset + 5;
							this.drawString(this.fontRenderer, "------------------------", xOffset + 10, yOffset + 39, BLUE);
							this.drawString(this.fontRenderer, TranslateUtilities.translate("gui.atmosphere_details.name") + ": ", xOffset + 10, yOffset + 45, BLUE);
							this.drawString(this.fontRenderer, "------------------------", xOffset + 10, yOffset + 51, BLUE);
							this.drawString(this.fontRenderer, TranslateUtilities.translate("gui.celestial_body_wind_level.name") + ": "
									+ (this.selectedBody.getReachable() ? this.selectedBody.atmosphere.windLevel() * 10 + "%" : TranslateUtilities.translate("gui.type_unknown.name")), xOffset + 10, yOffset + 60, 14737632);
							float temperature = 0;
							if (this.selectedBody.getReachable() && !this.selectedBody.getUnlocalizedName().toLowerCase().contains("overworld") && !(this.selectedBody instanceof Satellite))
								try {
									temperature = ((WorldProviderSpace) temp).getThermalLevelModifier();
								} catch (Exception e) {
								}
							this.drawString(this.fontRenderer,
									TranslateUtilities.translate("gui.celestial_body_temperature.name") + ": " + (this.selectedBody.getReachable() ? temperature + "C" : TranslateUtilities.translate("gui.type_unknown.name")), xOffset + 10,
									yOffset + 70, 14737632);
							boolean breathable = false;
							if (temp != null && !(this.selectedBody instanceof Satellite))
								breathable = ((WorldProviderSpace) temp).hasBreathableAtmosphere();
							if (this.selectedBody.getUnlocalizedName().contains("overworld"))
								breathable = true;
							this.drawString(this.fontRenderer, TranslateUtilities.translate("gui.celestial_body_breathable.name") + ": " + (this.selectedBody.getReachable() ? breathable : TranslateUtilities.translate("gui.type_unknown.name")),
									xOffset + 10, yOffset + 80, 14737632);
							this.drawString(this.fontRenderer, TranslateUtilities.translate("gui.celestial_body_corrosive_atmosphere.name") + ": "
									+ (this.selectedBody.getReachable() ? this.selectedBody.atmosphere.isCorrosive() : TranslateUtilities.translate("gui.type_unknown.name")), xOffset + 10, yOffset + 90, 14737632);
							int radiationLevel = 0;
							int pressureLevel = 0;
							try {
								radiationLevel = ((WorldProviderRealisticSpace) temp).getSolarRadiationLevel();
								pressureLevel = ((WorldProviderRealisticSpace) temp).getPressureLevel();
							} catch (Exception ex) {
								if (temp instanceof WorldProviderMoon) {
									if (Config.GC_PRESSURE)
										pressureLevel = 80;
									if (Config.GC_RADIATION)
										radiationLevel = Config.MOON_RADIATION_AMOUNT;
								} else if (temp instanceof WorldProviderMars) {
									if (Config.GC_PRESSURE)
										pressureLevel = 90;
									if (Config.GC_RADIATION)
										radiationLevel = Config.MARS_RADIATION_AMOUNT;
								} else if (temp instanceof WorldProviderVenus) {
									if (Config.GC_PRESSURE)
										pressureLevel = 100;
									if (Config.GC_RADIATION)
										radiationLevel = Config.VENUS_RADIATION_AMOUNT;
								} else if (temp instanceof WorldProviderAsteroids) {
									if (Config.GC_PRESSURE)
										pressureLevel = 100;
									if (Config.GC_RADIATION)
										radiationLevel = Config.ASTEROIDS_RADIATION_AMOUNT;
								} else if (temp instanceof WorldProviderSpaceStation || this.selectedBody instanceof Satellite) {
									if (Config.GC_PRESSURE || Config.PRESSURE)
										pressureLevel = 100;
									if (Config.GC_RADIATION || Config.RADIATION)
										radiationLevel = Config.SPACE_STATION_RADIATION_AMOUNT;
								} else {
									radiationLevel = 0;
									pressureLevel = 0;
								}
							}

							this.drawString(this.fontRenderer,
									TranslateUtilities.translate("gui.celestial_body_radiation_level.name") + ": " + (this.selectedBody.getReachable() ? radiationLevel + "%" : TranslateUtilities.translate("gui.type_unknown.name")), xOffset + 10,
									yOffset + 100, 14737632);
							this.drawString(this.fontRenderer,
									TranslateUtilities.translate("gui.celestial_body_pressure_level.name") + ": " + (this.selectedBody.getReachable() ? pressureLevel + "%" : TranslateUtilities.translate("gui.type_unknown.name")), xOffset + 10,
									yOffset + 110, 14737632);
						}
						if (showHoveredMats) {
							List<String> materials = CelestialBodyMaterialRegistry.getTextOutputByCelestialBody(selectedBody);
							if (materials.size() != 0)
								this.drawHoveringText(materials, showHoveredMatsX, showHoveredMatsY);
							else
								this.drawHoveringText("Unknown Materials", showHoveredMatsX, showHoveredMatsY);
						}
					} else if (this.selectedBody instanceof Star) {
						this.drawString(this.fontRenderer, "------------------------", xOffset + 10, yOffset + 2, BLUE);
						this.drawString(this.fontRenderer, TranslateUtilities.translate("gui.general_details.name") + ": ", xOffset + 10, yOffset + 8, BLUE);
						this.drawString(this.fontRenderer, "------------------------", xOffset + 10, yOffset + 14, BLUE);
						this.drawString(this.fontRenderer, TranslateUtilities.translate("gui.celestial_body_type.name") + ": " + "Star", xOffset + 10, yOffset + 23, 14737632);
						List<CelestialBody> planets = this.getChildren(((Star) this.selectedBody).getParentSolarSystem());
						int amountofPlanets = planets.size();
						this.drawString(this.fontRenderer, TranslateUtilities.translate("gui.celestial_body_amount_of_planets.name") + ": " + amountofPlanets, xOffset + 10, yOffset + 33, 14737632);

						yOffset = yOffset + 50;

						this.drawString(this.fontRenderer, "------------------------", xOffset + 10, yOffset + 2, BLUE);
						this.drawString(this.fontRenderer, TranslateUtilities.translate("gui.parent_solar_system_details.name") + ": ", xOffset + 10, yOffset + 8, BLUE);
						this.drawString(this.fontRenderer, "------------------------", xOffset + 10, yOffset + 14, BLUE);

						int amountofMoons = 0;
						for (int i = 0; i < amountofPlanets; i++) {
							amountofMoons += this.getChildren(planets.get(i)).size();
						}
						this.drawString(this.fontRenderer, TranslateUtilities.translate("gui.celestial_body_amount_of_moons.name") + ": " + amountofMoons, xOffset + 10, yOffset + 23, 14737632);
					}
				}

				int scale = (int) Math.min(95, this.ticksSinceSelectionF * 12.0F);
				String str;

				if (this.showGalaxies) {
					this.drawString(this.fontRenderer, "-", LHS + 80, LHS + 16, 8087790);
					for (int i = 0; i < this.galaxies.size(); i++) {
						String child = TranslateUtilities.translate(this.galaxies.get(i));
						int xOffset = 0;
						int yOffset = 45;

						scale = (int) Math.min(95.0F, Math.max(0.0F, (this.ticksSinceMenuOpenF * 25.0F) - 95 * i));

						this.mc.renderEngine.bindTexture(GuiCelestialSelection.guiMain0);
						GL11.glColor4f(0.0F, 0.6F, 1.0F, scale / 95.0F);
						this.drawTexturedModalRect(LHS + 3 + xOffset, LHS + 6 + i * 14 + yOffset, 86, 10, 0, 489, 86, 10, false, false);
						GL11.glColor4f(1.0F, 1.0F, 1.0F, scale / 95.0F);
						this.drawTexturedModalRect(LHS + 2 + xOffset, LHS + 5 + i * 14 + yOffset, 93, 12, 95, 464, 93, 12, false, false);

						if (scale > 0) {
							GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
							str = child;
							int color = 14737632;
							this.fontRenderer.drawString(str, LHS + 7 + xOffset, LHS + 8 + i * 14 + yOffset, color);
						}
					}
				} else if (!this.showGalaxies && Config.CUSTOM_GALAXIES && galaxies.size() > 1) {
					this.drawString(this.fontRenderer, "+", LHS + 80, LHS + 16, 8087790);
					this.drawString(this.fontRenderer, "< " + TranslateUtilities.translate("gui.new_galaxies.name") + "!", LHS + 100, LHS + 16, 8087790);
				}
			}
		} catch (Exception e) {
			MessageUtilities.fatalErrorMessageToLog(MODID, "An error has occurred while rendering custom features on the Celestial Selection screen, Please check log for errors!");
			e.printStackTrace();
		}
	}

	@Override
	protected void mouseClicked(int x, int y, int button) throws IOException {
		if (Config.CUSTOM_GALAXIES) {
			int xPos;
			int yPos;

			final int LHS = GuiCelestialSelection.BORDER_SIZE + GuiCelestialSelection.BORDER_EDGE_SIZE;

			xPos = LHS + 0;
			yPos = LHS + 10;
			if (this.showGalaxies == false && x >= xPos && x <= xPos + 100 && y >= yPos && y <= yPos + 15)
				this.showGalaxies = true;
			else if (this.showGalaxies && x >= xPos && x <= xPos + 100 && y >= yPos && y <= yPos + 15)
				this.showGalaxies = false;

			if (this.showGalaxies == false) {
				super.mouseClicked(x, y, button);
				if (!this.currentGalaxyName.equalsIgnoreCase("galaxy.milky_way"))
					this.selectedParent = this.currentGalaxyMainSystem;
			}

			if (this.showGalaxies) {
				for (int i = 0; i < this.galaxies.size(); i++) {
					int xOffset = 0;
					int yOffset = 45;

					xPos = LHS + 2 + xOffset;
					yPos = LHS + 5 + i * 14 + yOffset;

					if (x >= xPos && x <= xPos + 93 && y >= yPos && y <= yPos + 12) {
						boolean clicked = false;
						if (i == 0) {
							this.currentGalaxyName = this.galaxies.get(i);
							this.currentGalaxyMainSystem = GalacticraftCore.solarSystemSol;
							clicked = true;
						} else {
							this.currentGalaxyName = this.galaxies.get(i);
							for (SolarSystem system : GalaxyRegistry.getRegisteredSolarSystems().values())
								if (system.getUnlocalizedParentGalaxyName().equalsIgnoreCase(this.currentGalaxyName))
									this.currentGalaxyMainSystem = system;
							clicked = true;
						}
						if (clicked) {
							this.drawScreen(this.mousePosX, this.mousePosY, this.partialTicks);
							this.selectedParent = this.currentGalaxyMainSystem;
							this.showGalaxies = false;

							// Used to make sure nothing is selected/zoomed & resets it all like the screen was just opened
							this.unselectCelestialBody();
							this.planetZoom = 0.0F;
							this.zoom = 0.0F;
							this.translation = new Vector2f(0.0F, 0.0F);
							this.position = new Vector2f(0, 0);
							initGui(); // Used to reload the bodies to render
						}
					}
				}
			}
		} else
			super.mouseClicked(x, y, button);
	}
}
