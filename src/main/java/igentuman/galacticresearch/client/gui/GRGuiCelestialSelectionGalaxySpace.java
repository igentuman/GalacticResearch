package igentuman.galacticresearch.client.gui;

import asmodeuscore.AsmodeusCore;
import asmodeuscore.api.dimension.IAdvancedSpace.StarClass;
import asmodeuscore.api.dimension.IAdvancedSpace.TypeBody;
import asmodeuscore.api.space.ICelestialRegistry;
import asmodeuscore.api.space.IExBody;
import asmodeuscore.core.astronomy.BodiesData;
import asmodeuscore.core.astronomy.BodiesRegistry;
import asmodeuscore.core.astronomy.SpaceData;
import asmodeuscore.core.astronomy.SpaceData.Engine_Type;
import asmodeuscore.core.astronomy.gui.screen.GuiCustomTeleporting;
import asmodeuscore.core.astronomy.gui.screen.NewGuiCelestialSelection;
import asmodeuscore.core.configs.AsmodeusConfig;
import asmodeuscore.core.network.packet.ACPacketSimple;
import asmodeuscore.core.network.packet.ACPacketSimple.ACEnumSimplePacket;
import asmodeuscore.core.proxy.ClientProxy;
import asmodeuscore.core.utils.Utils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.*;
import java.util.Map.Entry;

import igentuman.galacticresearch.ModConfig;
import igentuman.galacticresearch.client.capability.PlayerClientSpaceData;
import igentuman.galacticresearch.client.capability.SpaceClientCapabilityHandler;
import micdoodle8.mods.galacticraft.api.GalacticraftRegistry;
import micdoodle8.mods.galacticraft.api.event.client.CelestialBodyRenderEvent.Post;
import micdoodle8.mods.galacticraft.api.event.client.CelestialBodyRenderEvent.Pre;
import micdoodle8.mods.galacticraft.api.galaxies.CelestialBody;
import micdoodle8.mods.galacticraft.api.galaxies.GalaxyRegistry;
import micdoodle8.mods.galacticraft.api.galaxies.IChildBody;
import micdoodle8.mods.galacticraft.api.galaxies.Moon;
import micdoodle8.mods.galacticraft.api.galaxies.Planet;
import micdoodle8.mods.galacticraft.api.galaxies.Satellite;
import micdoodle8.mods.galacticraft.api.galaxies.SolarSystem;
import micdoodle8.mods.galacticraft.api.galaxies.Star;
import micdoodle8.mods.galacticraft.api.world.IGalacticraftWorldProvider;
import micdoodle8.mods.galacticraft.api.world.SpaceStationType;
import micdoodle8.mods.galacticraft.core.GalacticraftCore;
import micdoodle8.mods.galacticraft.core.client.gui.screen.GuiCelestialSelection;
import micdoodle8.mods.galacticraft.core.proxy.ClientProxyCore;
import micdoodle8.mods.galacticraft.core.tick.KeyHandlerClient;
import micdoodle8.mods.galacticraft.core.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldProviderSurface;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public class GRGuiCelestialSelectionGalaxySpace extends NewGuiCelestialSelection {
	protected NewGuiCelestialSelection.EnumView viewState;
	private String galaxy;
	private List<String> galaxylist;
	private List<SolarSystem> starlist;
	private boolean showStarList;
	private boolean showGalaxyList;
	private boolean enableNewTierSystem;
	private double isometx;
	private double isometz;
	private double mindistance;
	private int currenttier;
	private int tierneed;
	private double timer;
	private double traveltime;
	private boolean enableTraveltime;
	private int coef;
	private boolean canTravel;
	private int small_page;
	private Engine_Type engine;
	public SpaceData data;
	private int fuelSet;
	private int fuelRocketLevel;
	protected static final int LIGHTBLUE = ColorUtil.to32BitColor(255, 0, 255, 255);
	protected static final int YELLOW = ColorUtil.to32BitColor(255, 255, 255, 0);
	public static ResourceLocation guiMain2 = new ResourceLocation("asmodeuscore", "textures/gui/celestialselection2.png");
	public static ResourceLocation guiImg = new ResourceLocation("asmodeuscore", "textures/gui/galaxymap_1.png");
	public static ResourceLocation vortexTexture = new ResourceLocation("asmodeuscore", "textures/gui/celestialbodies/vortex.png");
	private double xImgOffset;
	private double yImgOffset;
	private int scroll;
	private int max_scroll;
	private Vec3d nebula_color;
	private int nebula_img;
	private boolean isSecondPassenger;

	public GRGuiCelestialSelectionGalaxySpace(boolean mapMode, List<CelestialBody> possibleBodies, boolean canCreateStations) {
		super(mapMode, possibleBodies, canCreateStations, null);
		this.viewState = NewGuiCelestialSelection.EnumView.GS;
		this.galaxy = GalacticraftCore.planetOverworld.getParentSolarSystem().getUnlocalizedParentGalaxyName();
		this.galaxylist = new ArrayList();
		this.starlist = new ArrayList();
		this.showStarList = false;
		this.showGalaxyList = false;
		this.enableNewTierSystem = AsmodeusConfig.enableNewTierSystem;
		this.isometx = AsmodeusConfig.enable2DGalaxyMap ? 0.0D : 55.0D;
		this.isometz = 45.0D;
		this.mindistance = 900.0D;
		this.currenttier = 0;
		this.tierneed = -1;
		this.enableTraveltime = false;
		this.canTravel = true;
		this.small_page = 0;
		this.engine = Engine_Type.FUEL_ENGINE;
		this.fuelSet = 0;
		this.fuelRocketLevel = 0;
		this.xImgOffset = 0.0D;
		this.yImgOffset = 0.0D;
		this.scroll = 0;
		this.max_scroll = 8;
		this.nebula_color = new Vec3d(1.0D, 1.0D, 1.0D);
		this.nebula_img = 0;
		int tier = 0;

		this.currenttier = tier;
		this.coef = AsmodeusConfig.speedTimeTravel - 1 + tier;
		if (this.engine != null) {
			switch(this.engine) {
				case FUEL_ENGINE:
				default:
					break;
				case ION_ENGINE:
					this.coef += 4;
					break;
				case PLASMA_ENGINE:
					this.coef += 8;
					break;
				case SUBLIGHT_ENGINE:
					this.coef += 100;
					break;
				case BLACKHOLE_ENGINE:
					this.coef += 1000;
			}
		}

		Random rand = new Random();
		this.nebula_img = rand.nextInt(2);
		this.nebula_color = new Vec3d((double)rand.nextFloat(), (double)rand.nextFloat(), (double)rand.nextFloat());
	}

	public void initGui() {
		GuiCelestialSelection.BORDER_SIZE = this.width / 65;
		GuiCelestialSelection.BORDER_EDGE_SIZE = GuiCelestialSelection.BORDER_SIZE / 4;
		this.refreshBodies();
	}

	public void updateScreen() {
		++this.ticksSinceMenuOpen;
		++this.ticksTotal;
		if (this.ticksSinceMenuOpen < 20) {
			Mouse.setGrabbed(false);
		}

		if (this.selectedBody != null) {
			++this.ticksSinceSelection;
		}

		if (this.selectedBody == null && this.ticksSinceUnselection >= 0) {
			++this.ticksSinceUnselection;
		}

		if (!this.renamingSpaceStation && (this.selectedBody == null || !this.isZoomed())) {
			Vector2f var10000;
			if (GameSettings.isKeyDown(KeyHandlerClient.leftKey)) {
				var10000 = this.translation;
				var10000.x += -2.0F;
				var10000 = this.translation;
				var10000.y += -2.0F;
				if (AsmodeusConfig.enableDynamicImgOnGalaxyMap) {
					this.xImgOffset += -1.0D;
				}
			}

			if (GameSettings.isKeyDown(KeyHandlerClient.rightKey)) {
				var10000 = this.translation;
				var10000.x += 2.0F;
				var10000 = this.translation;
				var10000.y += 2.0F;
				if (AsmodeusConfig.enableDynamicImgOnGalaxyMap) {
					++this.xImgOffset;
				}
			}

			if (GameSettings.isKeyDown(KeyHandlerClient.upKey)) {
				var10000 = this.translation;
				var10000.x += 2.0F;
				var10000 = this.translation;
				var10000.y += -2.0F;
				if (AsmodeusConfig.enableDynamicImgOnGalaxyMap) {
					this.yImgOffset += -1.0D;
				}
			}

			if (GameSettings.isKeyDown(KeyHandlerClient.downKey)) {
				var10000 = this.translation;
				var10000.x += -2.0F;
				var10000 = this.translation;
				var10000.y += 2.0F;
				if (AsmodeusConfig.enableDynamicImgOnGalaxyMap) {
					++this.yImgOffset;
				}
			}
		}

		if (this.xImgOffset > 20.0D) {
			this.xImgOffset = 20.0D;
		}

		if (this.xImgOffset < -150.0D) {
			this.xImgOffset = -150.0D;
		}

		if (this.yImgOffset > 20.0D) {
			this.yImgOffset = 20.0D;
		}

		if (this.yImgOffset < -80.0D) {
			this.yImgOffset = -80.0D;
		}

	}

	private boolean isUnlocked(String name, PlayerClientSpaceData stats)
	{
		return stats.getUnlockedMissions().contains(name.toLowerCase()) ||
				Arrays.asList(ModConfig.researchSystem.default_researched_objects).contains(name.toLowerCase());
	}

	private void refreshBodies() {
		EntityPlayerSP player = Minecraft.getMinecraft().player;
		this.bodiesToRender.clear();
		this.galaxylist.clear();
		this.starlist.clear();
		EntityPlayerSP playerBaseClient = PlayerUtil.getPlayerBaseClientFromPlayer(player, false);
		PlayerClientSpaceData stats = null;

		if (player != null) {
			stats = playerBaseClient.getCapability(SpaceClientCapabilityHandler.PLAYER_SPACE_CLIENT_DATA, null);
		}
		Iterator var2 = GalaxyRegistry.getRegisteredSolarSystems().values().iterator();

		while(var2.hasNext()) {
			SolarSystem solarSystem = (SolarSystem)var2.next();
			if (!this.galaxylist.contains(solarSystem.getUnlocalizedParentGalaxyName())) {
				this.galaxylist.add(solarSystem.getUnlocalizedParentGalaxyName());
			}

			if (solarSystem.getUnlocalizedParentGalaxyName().equals(this.galaxy)) {
				if(isUnlocked(solarSystem.getName(), stats)) {
					if (solarSystem.getUnlocalizedParentGalaxyName().equals(this.galaxy)) {
						this.starlist.add(solarSystem);
					}
					this.bodiesToRender.add(solarSystem.getMainStar());
				}
			}
		}


		var2 = GalaxyRegistry.getRegisteredPlanets().values().iterator();

		while(var2.hasNext()) {
			Planet planet = (Planet)var2.next();
			if (planet.getParentSolarSystem().getUnlocalizedParentGalaxyName().equals(this.galaxy)) {
				if(isUnlocked(planet.getName(), stats) && isUnlocked(planet.getParentSolarSystem().getName(), stats)) {
					this.bodiesToRender.add(planet);
				}
			}
		}

		var2 = GalaxyRegistry.getRegisteredMoons().values().iterator();

		while(var2.hasNext()) {
			Moon moon = (Moon)var2.next();
			if (moon.getParentPlanet().getParentSolarSystem().getUnlocalizedParentGalaxyName().equals(this.galaxy)) {
				if(isUnlocked(moon.getName(), stats) && isUnlocked(moon.getParentPlanet().getParentSolarSystem().getName(), stats)) {
					this.bodiesToRender.add(moon);
				}
			}
		}

		var2 = GalaxyRegistry.getRegisteredSatellites().values().iterator();

		while(var2.hasNext()) {
			Satellite satellite = (Satellite)var2.next();
			if (satellite.getParentPlanet().getParentSolarSystem().getUnlocalizedParentGalaxyName().equals(this.galaxy)) {
				this.bodiesToRender.add(satellite);
			}
		}

		var2 = BodiesRegistry.getBodies().iterator();

		while(var2.hasNext()) {
			Map<String, ? extends CelestialBody> bodies = (Map)var2.next();
			Iterator var4 = bodies.values().iterator();

			while(var4.hasNext()) {
				CelestialBody body = (CelestialBody)var4.next();
				if (body instanceof ICelestialRegistry) {
					if (body instanceof IChildBody) {
						if (((IChildBody)body).getParentPlanet().getParentSolarSystem().getUnlocalizedParentGalaxyName().equals(this.galaxy) && ((ICelestialRegistry)body).canRegistry()) {
							this.bodiesToRender.add(body);
						}
					} else if (((ICelestialRegistry)body).getParentSolarSystem().getUnlocalizedParentGalaxyName().equals(this.galaxy) && ((ICelestialRegistry)body).canRegistry()) {
						this.bodiesToRender.add(body);
					}
				}
			}
		}

		Collections.sort(this.starlist, new Comparator<SolarSystem>() {
			public int compare(SolarSystem lhs, SolarSystem rhs) {
				return lhs.getName().compareTo(rhs.getName());
			}
		});
	}

	protected int drawChildren(List<CelestialBody> children, int xOffsetBase, int yOffsetPrior, boolean recursive) {
		xOffsetBase += GuiCelestialSelection.BORDER_SIZE + GuiCelestialSelection.BORDER_EDGE_SIZE;
		int yOffsetBase = GuiCelestialSelection.BORDER_SIZE + GuiCelestialSelection.BORDER_EDGE_SIZE + 50 + yOffsetPrior;
		int yOffset = 0;
		int size = children.size();
		EntityPlayer player = Minecraft.getMinecraft().player;
		EntityPlayerSP playerBaseClient = PlayerUtil.getPlayerBaseClientFromPlayer(player, false);
		PlayerClientSpaceData stats = null;

		if (player != null) {
			stats = playerBaseClient.getCapability(SpaceClientCapabilityHandler.PLAYER_SPACE_CLIENT_DATA, null);
		}
		for(int i = 0; i < size; ++i) {
			CelestialBody child = (CelestialBody)children.get(i);
			int xOffset = xOffsetBase + (child.equals(this.selectedBody) ? 5 : 0);
			int scale = (int)Math.min(95.0F, Math.max(0.0F, this.ticksSinceMenuOpenF * 25.0F - (float)(95 * i)));
			BodiesData data = BodiesRegistry.getData(child);
			this.mc.renderEngine.bindTexture(GuiCelestialSelection.guiMain0);
			float brightness = child.equals(this.selectedBody) ? 0.2F : 0.0F;
			if (child.getReachable()) {
				if (child.equals(this.selectedBody)) {
					GL11.glColor4f(0.0F, 1.0F, 1.0F, (float)scale / 95.0F);
				} else {
					GL11.glColor4f(0.0F, 0.6F + brightness, 0.0F, (float)scale / 95.0F);
				}
			} else {
				boolean checked = false;
				if (child instanceof Planet) {
					Iterator var15 = GalaxyRegistry.getMoonsForPlanet((Planet)child).iterator();

					while(var15.hasNext()) {
						Moon moon = (Moon)var15.next();
						if (moon.getReachable()) {
							checked = true;
							break;
						}
					}
				}

				if (data != null && data.getType() == TypeBody.STAR) {
					GL11.glColor4f(0.0F, 0.4F, 0.6F + brightness, (float)scale / 95.0F);
				} else if (child instanceof Planet && checked) {
					GL11.glColor4f(0.6F + brightness, 0.6F, 0.0F, (float)scale / 95.0F);
				} else {
					GL11.glColor4f(0.6F + brightness, 0.0F, 0.0F, (float)scale / 95.0F);
				}
			}

			this.drawTexturedModalRect(3 + xOffset, yOffsetBase + yOffset + 1, 86, 10, 0, 489, 86, 10, false, false);
			GL11.glColor4f(3.0F * brightness, 0.6F + 2.0F * brightness, 1.0F, (float)scale / 95.0F);
			this.drawTexturedModalRect(2 + xOffset, yOffsetBase + yOffset, 93, 12, 95, 464, 93, 12, false, false);
			if (scale > 0) {
				GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
				int color = 14737632;
				GlStateManager.pushMatrix();
				GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
				this.mc.renderEngine.bindTexture(child.getBodyIcon());
				this.drawTexturedModalRect((float)(xOffset + 7), (float)(yOffsetBase + yOffset + 2), 8.0F, 8.0F, 0.0F, 0.0F, 32.0F, 32.0F, false, false, 32.0F, 32.0F);
				GlStateManager.popMatrix();
				this.fontRenderer.drawString(child.getLocalizedName(), 17 + xOffset, yOffsetBase + yOffset + 2, color);
			}

			yOffset += 14;
			if (recursive && child.equals(this.selectedBody)) {
				List<CelestialBody> grandchildren = this.getChildren(child, 0, this.max_scroll);
				if (grandchildren.size() > 0) {
					if (this.animateGrandchildren == 14 * grandchildren.size()) {
						yOffset += this.drawChildren(grandchildren, 10, yOffset, false);
					} else {
						if (this.animateGrandchildren >= 14) {
							List<CelestialBody> partial = new LinkedList();

							for(int j = 0; j < this.animateGrandchildren / 14; ++j) {
								partial.add(grandchildren.get(j));
							}

							this.drawChildren(partial, 10, yOffset, false);
						}

						yOffset += this.animateGrandchildren;
						this.animateGrandchildren += 2;
					}
				}
			}
		}

		if (ClientProxy.smallInfoOnMap && recursive) {
			GL11.glColor4f(0.0F, 0.6F, 1.0F, 1.0F);
			this.mc.renderEngine.bindTexture(guiMain2);
			this.drawTexturedModalRect(BORDER_SIZE + 18, yOffsetBase + yOffset - 1, 62, 4, 280, 74, 62, 4, false, true);
			GL11.glColor4f(1.0F, 1.0F, 0.0F, 1.0F);
			boolean planetZoomedNotMoon = this.isZoomed() && !(this.selectedParent instanceof Planet);
			if (this.scroll != 0) {
				this.drawTexturedModalRect(BORDER_SIZE + 47, yOffsetBase - 4, 7, 3, 344, 74, 7, 3, false, false);
			}

			if (this.scroll != this.getChildren(planetZoomedNotMoon ? this.selectedBody : this.selectedParent).size() - this.max_scroll) {
				this.drawTexturedModalRect(BORDER_SIZE + 47, yOffsetBase + yOffset, 7, 3, 344, 74, 7, 3, false, true);
			}
		}

		return yOffset;
	}

	@Override
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
			List<Moon> moons = GalaxyRegistry.getMoonsForPlanet((Planet) object);
			for (Moon moon : moons)
				if (isUnlocked(moon.getName(), stats))
					bodyList.add(moon);
		} else if (object instanceof SolarSystem) {
			List<Planet> planets = GalaxyRegistry.getPlanetsForSolarSystem((SolarSystem) object);
			for (Planet planet : planets)
				if (isUnlocked(planet.getName(), stats))
					bodyList.add(planet);
		}

		Collections.sort(bodyList);

		return bodyList;
	}

	public void drawScreen(int mousePosX, int mousePosY, float partialTicks) {
		this.ticksSinceMenuOpenF += partialTicks;
		this.ticksTotalF += partialTicks;
		if (this.selectedBody != null) {
			this.ticksSinceSelectionF += partialTicks;
		}

		if (this.selectedBody == null && this.ticksSinceUnselectionF >= 0.0F) {
			this.ticksSinceUnselectionF += partialTicks;
		}

		if (Mouse.hasWheel()) {
			float wheel = (float)Mouse.getDWheel() / (this.selectedBody == null ? 500.0F : 250.0F);
			if (wheel != 0.0F) {
				if (mousePosX < 105 && ClientProxy.smallInfoOnMap) {
					boolean planetZoomedNotMoon = this.isZoomed() && !(this.selectedParent instanceof Planet);
					if (wheel > 0.0F && this.scroll > 0) {
						--this.scroll;
					} else if (wheel < 0.0F && this.scroll < this.getChildren(planetZoomedNotMoon ? this.selectedBody : this.selectedParent).size() - this.max_scroll) {
						++this.scroll;
					}
				} else if (this.selectedBody != null && this.isZoomed()) {
					this.planetZoom = Math.min(Math.max(this.planetZoom + wheel, -4.9F), 5.0F);
				} else {
					this.zoom = Math.min(Math.max(this.zoom + wheel * (this.zoom + 2.0F) / 10.0F, -1.095F), 3.0F);
				}
			}
		}

		GL11.glPushMatrix();
		GL11.glEnable(3042);
		Matrix4f camMatrix = new Matrix4f();
		Matrix4f.translate(new Vector3f(0.0F, 0.0F, -9000.0F), camMatrix, camMatrix);
		Matrix4f viewMatrix = new Matrix4f();
		viewMatrix.m00 = 2.0F / (float)this.width;
		viewMatrix.m11 = 2.0F / (float)(-this.height);
		viewMatrix.m22 = -2.2222222E-4F;
		viewMatrix.m30 = -1.0F;
		viewMatrix.m31 = 1.0F;
		viewMatrix.m32 = -2.0F;
		GL11.glMatrixMode(5889);
		GL11.glLoadIdentity();
		FloatBuffer fb = BufferUtils.createFloatBuffer(512);
		fb.rewind();
		viewMatrix.store(fb);
		fb.flip();
		GL11.glMultMatrix(fb);
		fb.clear();
		GL11.glMatrixMode(5888);
		GL11.glLoadIdentity();
		fb.rewind();
		camMatrix.store(fb);
		fb.flip();
		fb.clear();
		GL11.glMultMatrix(fb);
		this.setBlackBackground();
		boolean drawGrid = true;
		if (AsmodeusConfig.enableImgOnGalaxyMap) {
			GL11.glPushMatrix();
			this.setImgBackground(guiImg);
			drawGrid = false;
			GL11.glPopMatrix();
		}

		GL11.glPushMatrix();
		Matrix4f worldMatrix = this.setIsometric(partialTicks);
		float gridSize = 14000.0F;
		if (drawGrid) {
			this.drawGrid(gridSize, (float)(this.height / 3) / 3.5F);
		}

		if (this.selectedBody instanceof Star) {
			this.drawAgeCircle(worldMatrix);
		}

		this.drawCircles();
		this.drawVortex();
		GL11.glPopMatrix();
		HashMap<CelestialBody, Matrix4f> matrixMap = this.drawCelestialBodies(worldMatrix);
		this.planetPosMap.clear();
		Iterator var11 = matrixMap.entrySet().iterator();

		while(var11.hasNext()) {
			Entry<CelestialBody, Matrix4f> e = (Entry)var11.next();
			Matrix4f planetMatrix = (Matrix4f)e.getValue();
			Matrix4f matrix0 = Matrix4f.mul(viewMatrix, planetMatrix, planetMatrix);
			int x = (int)Math.floor(((double)matrix0.m30 * 0.5D + 0.5D) * (double)Minecraft.getMinecraft().displayWidth);
			int y = (int)Math.floor((double)Minecraft.getMinecraft().displayHeight - ((double)matrix0.m31 * 0.5D + 0.5D) * (double)Minecraft.getMinecraft().displayHeight);
			Vector2f vec = new Vector2f((float)x, (float)y);
			Matrix4f scaleVec = new Matrix4f();
			scaleVec.m00 = matrix0.m00;
			scaleVec.m11 = matrix0.m11;
			scaleVec.m22 = matrix0.m22;
			Vector4f newVec = Matrix4f.transform(scaleVec, new Vector4f(2.0F, -2.0F, 0.0F, 0.0F), (Vector4f)null);
			float iconSize = newVec.y * ((float)Minecraft.getMinecraft().displayHeight / 2.0F) * (float)(e.getKey() instanceof Star ? 2 : 1) * (e.getKey() == this.selectedBody ? 1.5F : 1.0F);
			this.planetPosMap.put(e.getKey(), new Vector3f(vec.x, vec.y, iconSize));
		}

		this.drawSelectionCursor(fb, worldMatrix);

		try {
			this.drawButtons(mousePosX, mousePosY);
		} catch (Exception var21) {
			if (!this.errorLogged) {
				this.errorLogged = true;
				GCLog.severe("Problem identifying planet or dimension in an add on for Galacticraft!");
				GCLog.severe("(The problem is likely caused by a dimension ID conflict.  Check configs for dimension clashes.  You can also try disabling Mars space station in configs.)");
				var21.printStackTrace();
			}
		}

		this.drawBorder();
		GL11.glPopMatrix();
		GL11.glMatrixMode(5889);
		GL11.glLoadIdentity();
		GL11.glMatrixMode(5888);
		GL11.glLoadIdentity();
	}

	public void drawButtons(int mousePosX, int mousePosY) {
		super.drawButtons(mousePosX, mousePosY);
		int LHS = GuiCelestialSelection.BORDER_SIZE + GuiCelestialSelection.BORDER_EDGE_SIZE;
		String str = EnumColor.DARK_RED + I18n.format("gui.celestial_selection.advise");
		this.fontRenderer.drawString(str, LHS + 5, this.height - GuiCelestialSelection.BORDER_SIZE - GuiCelestialSelection.BORDER_EDGE_SIZE - 20, ColorUtil.to32BitColor(255, 255, 255, 255));

	}

	protected void drawSelectionCursor(FloatBuffer fb, Matrix4f worldMatrix) {
		GL11.glPushMatrix();
		float div;
		float colMod;
		switch(this.selectionState) {
			case SELECTED:
				if (this.selectedBody != null) {
					this.setupMatrix(this.selectedBody, worldMatrix, fb);
					fb.clear();
					GL11.glScalef(0.06666667F, 0.06666667F, 1.0F);
					this.mc.renderEngine.bindTexture(GuiCelestialSelection.guiMain0);
					div = this.getZoomAdvanced() < 4.9F ? (float)(Math.sin((double)(this.ticksSinceSelectionF / 2.0F)) * 0.5D + 0.5D) : 1.0F;
					GL11.glColor4f(1.0F, 1.0F, 0.0F, 1.0F * div);
					int width = (int)Math.floor((double)this.getWidthForCelestialBody(this.selectedBody) / 2.0D * (this.selectedBody instanceof IChildBody ? 9.0D : 30.0D));
					this.drawTexturedModalRect(-width, -width, width * 2, width * 2, 266, 29, 100, 100, false, false);
					GL11.glPushMatrix();
					colMod = this.selectedBody instanceof IChildBody ? 2.0F : 12.0F;
					GL11.glScalef(colMod, colMod, colMod);
					String name = this.selectedBody.getLocalizedName();
					int white = Utils.getIntColor(255, 255, 255, 255);
					int yellow = Utils.getIntColor(255, 255, 0, 255);
					this.drawString(this.fontRenderer, name, 4 + this.getWidthForCelestialBody(this.selectedBody), -5, this.selectedBody instanceof Star ? yellow : white);
					GL11.glPopMatrix();
				}
				break;
			case ZOOMED:
				if (this.selectedBody != null) {
					this.setupMatrix(this.selectedBody, worldMatrix, fb);
					fb.clear();
					div = this.zoom + 1.0F - this.planetZoom;
					float scale = Math.max(0.3F, 1.5F / (this.ticksSinceSelectionF / 5.0F)) * 2.0F / div;
					GL11.glScalef(scale, scale, 1.0F);
					this.mc.renderEngine.bindTexture(GuiCelestialSelection.guiMain0);
					colMod = this.getZoomAdvanced() < 4.9F ? (float)(Math.sin((double)(this.ticksSinceSelectionF / 1.0F)) * 0.5D + 0.5D) : 1.0F;
					GL11.glColor4f(0.4F, 0.8F, 1.0F, 1.0F * colMod);
					int width = this.getWidthForCelestialBody(this.selectedBody) * 13;
					this.drawTexturedModalRect(-width, -width, width * 2, width * 2, 266, 29, 100, 100, false, false);
				}
		}

		GL11.glPopMatrix();
	}

	public int getWidthForCelestialBody(CelestialBody celestialBody) {
		boolean zoomed = celestialBody == this.selectedBody && this.selectionState == EnumSelection.SELECTED;
		float size = celestialBody.getRelativeSize();
		if ((double)size < 0.9D && celestialBody instanceof IChildBody) {
			size = 1.0F;
		}

		if (size > 5.0F) {
			size = 5.0F;
		}

		if (size < 0.3F) {
			size = 0.3F;
		}

		return (int)(celestialBody instanceof Star ? (zoomed ? 12.0F * size : 8.0F * size) : (celestialBody instanceof Planet ? (zoomed ? 6.0F * size : 4.0F * size) : (celestialBody instanceof IChildBody ? (zoomed ? 6.0F * size : 4.0F * size) : 2.0F)));
	}

	public HashMap<CelestialBody, Matrix4f> drawCelestialBodies(Matrix4f worldMatrix) {
		GL11.glColor3f(1.0F, 1.0F, 1.0F);
		FloatBuffer fb = BufferUtils.createFloatBuffer(512);
		HashMap<CelestialBody, Matrix4f> matrixMap = Maps.newHashMap();
		Iterator var4 = this.bodiesToRender.iterator();

		while(true) {
			CelestialBody body;
			boolean hasParent;
			float alpha;
			int size;
			do {
				if (!var4.hasNext()) {
					var4 = this.bodiesToRender.iterator();

					while(true) {
						do {
							if (!var4.hasNext()) {
								return matrixMap;
							}

							body = (CelestialBody)var4.next();
							hasParent = body instanceof IChildBody;
							alpha = this.getAlpha(body);
						} while((!(body instanceof Satellite) || !(alpha > 0.0F)) && (body instanceof Satellite || !(alpha >= 0.0F)));

						GlStateManager.pushMatrix();
						this.setupMatrix(body, worldMatrix, fb, hasParent ? 0.25F : 1.0F);
						if (this.selectionState == EnumSelection.UNSELECTED && !hasParent || this.selectionState == EnumSelection.ZOOMED && hasParent) {
							GL11.glScalef(0.5F, 0.5F, 0.5F);
							String name = body.getLocalizedName();
							size = Utils.getIntColor(255, 255, 255, 255);
							int yellow = Utils.getIntColor(255, 255, 0, 255);
							this.drawString(this.fontRenderer, name, 4 + this.getWidthForCelestialBody(body), -5, body instanceof Star ? yellow : size);
							if (ClientProxyCore.playerHead != null) {
								if (this.selectionState == EnumSelection.UNSELECTED && this.mc.world.provider instanceof IGalacticraftWorldProvider && ((IGalacticraftWorldProvider)this.mc.world.provider).getCelestialBody() instanceof IChildBody) {
									if (body == ((IChildBody)((IGalacticraftWorldProvider)this.mc.world.provider).getCelestialBody()).getParentPlanet()) {
										GlStateManager.enableBlend();
										GlStateManager.blendFunc(770, 771);
										GlStateManager.scale(0.25F, 0.25F, 0.25F);
										this.mc.renderEngine.bindTexture(ClientProxyCore.playerHead);
										this.drawTexturedModalRect(20 + this.fontRenderer.getStringWidth(name) * 5, -18, 32, 32, 32, 32);
									}
								} else if (this.mc.world.provider instanceof IGalacticraftWorldProvider && body != GalacticraftCore.planetOverworld && ((IGalacticraftWorldProvider)this.mc.world.provider).getCelestialBody() == body) {
									GlStateManager.enableBlend();
									GlStateManager.blendFunc(770, 771);
									GlStateManager.scale(0.25F, 0.25F, 0.25F);
									this.mc.renderEngine.bindTexture(ClientProxyCore.playerHead);
									this.drawTexturedModalRect(20 + this.fontRenderer.getStringWidth(name) * 5, -18, 32, 32, 32, 32);
								} else if (this.mc.world.provider instanceof WorldProviderSurface && body == GalacticraftCore.planetOverworld) {
									GlStateManager.enableBlend();
									GlStateManager.blendFunc(770, 771);
									GlStateManager.scale(0.25F, 0.25F, 0.25F);
									this.mc.renderEngine.bindTexture(ClientProxyCore.playerHead);
									this.drawTexturedModalRect(20 + this.fontRenderer.getStringWidth(name) * 5, -18, 32, 32, 32, 32);
								}
							}
						}

						GlStateManager.popMatrix();
					}
				}

				body = (CelestialBody)var4.next();
				hasParent = body instanceof IChildBody;
				alpha = this.getAlpha(body);
			} while((!(body instanceof Satellite) || !(alpha > 0.0F)) && (body instanceof Satellite || !(alpha >= 0.0F)));

			GlStateManager.pushMatrix();
			Matrix4f worldMatrixLocal = this.setupMatrix(body, worldMatrix, fb, hasParent ? 0.25F : 1.0F);
			Pre preEvent = new Pre(body, body.getBodyIcon(), 16);
			MinecraftForge.EVENT_BUS.post(preEvent);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			if (preEvent.celestialBodyTexture != null) {
				this.mc.renderEngine.bindTexture(preEvent.celestialBodyTexture);
			}

			if (!preEvent.isCanceled()) {
				size = this.getWidthForCelestialBody(body);
				float xOffset = 0.0F;
				float yOffset = 0.0F;
				if (size % 2 != 0) {
					xOffset = body.getRelativeSize() < 1.0F ? -0.5F : 0.0F;
					yOffset = body.getRelativeSize() < 1.0F ? -0.5F : 0.0F;
				}

				BodiesData data = BodiesRegistry.getData(body);
				if (body instanceof Star || data != null && data.getType() == TypeBody.STAR && data.getStarColor() != null) {
					GL11.glPushMatrix();
					GL11.glShadeModel(7425);
					GL11.glDisable(3553);
					GL11.glEnable(3008);
					GL11.glAlphaFunc(516, 0.0F);
					float r = 1.0F;
					float g = 1.0F;
					float b = 0.8F;
					float a = 0.7F;
					float f10 = (float)(size * 4);
					if (data != null && data.getType() == TypeBody.STAR) {
						if (data.getStarColor() != null) {
							r = data.getStarColor().getColor().floatX() / 255.0F;
							g = data.getStarColor().getColor().floatY() / 255.0F;
							b = data.getStarColor().getColor().floatZ() / 255.0F;
							a = 0.7F;
						}

						if (data.getStarClass() == StarClass.BLACKHOLE) {
							a = 0.0F;
							b = 0.0F;
							g = 0.0F;
							r = 0.0F;
						}
					}

					float xSize = 0.0F;
					float ySize = 0.0F;
					xSize /= 4.0F;
					ySize /= 4.0F;
					GL11.glColor4f(r, g, b, a);
					GL11.glDisable(2884);
					GL11.glBegin(6);
					GL11.glVertex2d((double)xSize, (double)ySize);

					for(int angle = 0; angle <= 360; angle += 60) {
						if (angle % 120 == 0) {
							GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.0F);
						}

						double x = (double)f10 * Math.cos((double)angle * 3.141592653589793D / 180.0D);
						double y = (double)f10 * Math.sin((double)angle * 3.141592653589793D / 180.0D);
						GL11.glVertex2d(x + (double)xSize, y + (double)ySize);
					}

					GL11.glEnd();
					GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
					GL11.glDisable(3008);
					GL11.glShadeModel(7424);
					GL11.glPopMatrix();
				}

				this.drawTexturedModalRect(xOffset + (float)(-size / 2), yOffset + (float)(-size / 2), (float)size, (float)size, 0.0F, 0.0F, (float)preEvent.textureSize, (float)preEvent.textureSize, false, false, (float)preEvent.textureSize, (float)preEvent.textureSize);
				matrixMap.put(body, worldMatrixLocal);
			}

			Post postEvent = new Post(body);
			MinecraftForge.EVENT_BUS.post(postEvent);
			GlStateManager.popMatrix();
		}
	}

	protected Matrix4f setupMatrix(CelestialBody body, Matrix4f worldMatrix, FloatBuffer fb, float scaleXZ) {
		Matrix4f worldMatrix0 = new Matrix4f(worldMatrix);
		Matrix4f.translate(this.getCelestialBodyPosition(body), worldMatrix0, worldMatrix0);
		Matrix4f worldMatrix1 = new Matrix4f();
		Matrix4f.rotate((float)Math.toRadians(this.isometz), new Vector3f(0.0F, 0.0F, 1.0F), worldMatrix1, worldMatrix1);
		Matrix4f.rotate((float)Math.toRadians(-this.isometx), new Vector3f(1.0F, 0.0F, 0.0F), worldMatrix1, worldMatrix1);
		if (scaleXZ != 1.0F) {
			Matrix4f.scale(new Vector3f(scaleXZ, scaleXZ, 1.0F), worldMatrix1, worldMatrix1);
		}

		worldMatrix1 = Matrix4f.mul(worldMatrix0, worldMatrix1, worldMatrix1);
		fb.rewind();
		worldMatrix1.store(fb);
		fb.flip();
		GL11.glMultMatrix(fb);
		return worldMatrix1;
	}

	protected void mouseClickMove(int x, int y, int lastButtonClicked, long timeSinceMouseClick) {
		if (this.mouseDragging && this.lastMovePosX != -1 && lastButtonClicked == 0) {
			int deltaX = x - this.lastMovePosX;
			int deltaY = y - this.lastMovePosY;
			float scollMultiplier = -Math.abs(this.zoom);
			if (this.zoom == -1.0F) {
				scollMultiplier = -10.5F;
			} else if (this.zoom >= -0.25F && this.zoom <= 0.15F) {
				scollMultiplier = -0.2F;
			}

			if (this.zoom >= 0.15F) {
				scollMultiplier = -0.25F;
			}

			Vector2f var10000 = this.translation;
			var10000.x += (float)(deltaX - deltaY) * scollMultiplier * (ConfigManagerCore.invertMapMouseScroll ? -1.0F : 1.0F) * ConfigManagerCore.mapMouseScrollSensitivity * 0.2F;
			var10000 = this.translation;
			var10000.y += (float)(deltaY + deltaX) * scollMultiplier * (ConfigManagerCore.invertMapMouseScroll ? -1.0F : 1.0F) * ConfigManagerCore.mapMouseScrollSensitivity * 0.2F;
			if (AsmodeusConfig.enableDynamicImgOnGalaxyMap) {
				this.xImgOffset += (double)((float)deltaX * (scollMultiplier / 4.0F));
				this.yImgOffset += (double)((float)deltaY * (scollMultiplier / 4.0F));
			}
		}

		this.lastMovePosX = x;
		this.lastMovePosY = y;
	}

	protected void mouseReleased(int x, int y, int button) {
		super.mouseReleased(x, y, button);
		this.mouseDragging = false;
		this.lastMovePosX = -1;
		this.lastMovePosY = -1;
	}

	public void setImgBackground(ResourceLocation galaxy) {
		GL11.glEnable(2929);
		GL11.glDepthMask(false);
		GL11.glBlendFunc(770, 771);
		GL11.glEnable(3008);
		GL11.glEnable(3553);
		GL11.glTranslated(-40.0D, -40.0D, 0.0D);
		GL11.glTranslated(this.xImgOffset, this.yImgOffset, 0.0D);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.renderEngine.bindTexture(galaxy);
		int width = Display.getWidth();
		int height = Display.getHeight();
		this.drawTexturedModalRect(0.0F, 0.0F, (float)width, (float)height, 0.0F, 0.0F, (float)(width * 2), (float)(height * 2), false, false, 1024.0F, 1024.0F);
		ResourceLocation guiImg_2 = new ResourceLocation("asmodeuscore", "textures/gui/galaxymap_nebula_" + this.nebula_img + ".png");
		this.mc.renderEngine.bindTexture(guiImg_2);
		GL11.glTranslated(this.xImgOffset * 1.5D, this.yImgOffset * 1.5D, 0.0D);
		GL11.glColor4f(0.5F, 0.4F, (float)this.nebula_color.z, 0.5F);
		this.drawTexturedModalRect(0.0F, 0.0F, (float)width, (float)height, 0.0F, 0.0F, 1024.0F, 1024.0F, false, false, 1024.0F, 1024.0F);
		GL11.glDepthMask(true);
		GL11.glDisable(3553);
		GL11.glDisable(2929);
		GL11.glDisable(3008);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	}

	public Matrix4f setIsometric(float partialTicks) {
		Matrix4f mat0 = new Matrix4f();
		Matrix4f.translate(new Vector3f((float)this.width / 2.0F, (float)(this.height / 2), 0.0F), mat0, mat0);
		Matrix4f.rotate((float)Math.toRadians(this.isometx), new Vector3f(1.0F, 0.0F, 0.0F), mat0, mat0);
		Matrix4f.rotate((float)Math.toRadians(-this.isometz), new Vector3f(0.0F, 0.0F, 1.0F), mat0, mat0);
		float zoomLocal = this.getZoomAdvanced();
		this.zoom = zoomLocal;
		Matrix4f.scale(new Vector3f(1.1F + zoomLocal, 1.1F + zoomLocal, 1.1F + zoomLocal), mat0, mat0);
		Vector2f cBodyPos = this.getTranslationAdvanced(partialTicks);
		this.position = this.getTranslationAdvanced(partialTicks);
		Matrix4f.translate(new Vector3f(-cBodyPos.x, -cBodyPos.y, 0.0F), mat0, mat0);
		FloatBuffer fb = BufferUtils.createFloatBuffer(16);
		fb.rewind();
		mat0.store(fb);
		fb.flip();
		GL11.glMultMatrix(fb);
		return mat0;
	}

	public float getAlpha(CelestialBody body) {
		float alpha = 1.0F;
		boolean selected;
		boolean ready;
		boolean isSibling;
		if (body instanceof IChildBody) {
			selected = body == this.selectedBody || ((IChildBody)body).getParentPlanet() == this.selectedBody && this.selectionState != EnumSelection.SELECTED;
			ready = this.lastSelectedBody != null || this.ticksSinceSelection > 35;
			isSibling = this.getSiblings(this.selectedBody).contains(body);
			boolean isPossible = !(body instanceof Satellite) || this.possibleBodies != null && this.possibleBodies.contains(body);
			if ((selected || isSibling) && isPossible) {
				if (this.isZoomed() && (!selected || !ready) && !isSibling) {
					alpha = Math.min(Math.max((float)(this.ticksSinceSelection - 30) / 15.0F, 0.0F), 1.0F);
				}
			} else {
				alpha = 0.0F;
			}
		} else {
			selected = this.selectedBody == body;
			ready = this.selectedBody instanceof IChildBody;
			isSibling = ready && ((IChildBody)this.selectedBody).getParentPlanet() == body;
			if (!selected && !isSibling && (this.isZoomed() || ready)) {
				if (this.lastSelectedBody == null && !(this.selectedBody instanceof IChildBody)) {
					alpha = 0.4F - Math.min((float)this.ticksSinceSelection / 25.0F, 0.4F);
				} else {
					alpha = 0.0F;
				}
			}
		}

		return alpha;
	}

	public void drawCircles() {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glLineWidth(2.0F);
		int count = 0;
		float theta = 0.06981317F;
		float cos = (float)Math.cos(0.06981316953897476D);
		float sin = (float)Math.sin(0.06981316953897476D);
		Iterator var5 = this.bodiesToRender.iterator();

		while(true) {
			CelestialBody body;
			Vector3f systemOffset;
			float orbit_ext_x;
			float orbit_ext_y;
			float orbit_offset_x;
			float orbit_offset_y;
			float x;
			float y;
			float alpha;
			do {
				if (!var5.hasNext()) {
					GL11.glLineWidth(1.0F);
					return;
				}

				body = (CelestialBody)var5.next();
				systemOffset = new Vector3f(0.0F, 0.0F, 0.0F);
				if (body instanceof IChildBody) {
					systemOffset = this.getCelestialBodyPosition(((IChildBody)body).getParentPlanet());
				} else if (body instanceof Planet) {
					systemOffset = this.getCelestialBodyPosition(((Planet)body).getParentSolarSystem().getMainStar());
				} else if (body instanceof Star) {
					systemOffset = this.getCelestialBodyPosition((Star)body);
				} else if (body instanceof ICelestialRegistry && !(body instanceof IChildBody)) {
					systemOffset = this.getCelestialBodyPosition(((ICelestialRegistry)body).getParentSolarSystem().getMainStar());
				}

				orbit_ext_x = body instanceof IExBody ? ((IExBody)body).getXOrbitEccentricity() : 1.0F;
				orbit_ext_y = body instanceof IExBody ? ((IExBody)body).getYOrbitEccentricity() : 1.0F;
				orbit_offset_x = body instanceof IExBody ? ((IExBody)body).getXOrbitOffset() : 0.0F;
				orbit_offset_y = body instanceof IExBody ? ((IExBody)body).getYOrbitOffset() : 0.0F;
				x = this.getScale(body);
				y = 0.0F;
				alpha = this.getAlpha(body);
			} while(!(alpha > 0.0F));

			switch(count++ % 2) {
				case 0:
					GL11.glColor4f(0.0F, 0.2857143F, 0.64285713F, alpha / 1.4F);
					break;
				case 1:
					GL11.glColor4f(0.0F, 0.4F, 0.9F, alpha / 2.4F);
			}

			if (body.equals(this.selectedBody)) {
				GL11.glColor4f(0.0F, 0.4F, 0.9F, 1.0F);
			}

			micdoodle8.mods.galacticraft.api.event.client.CelestialBodyRenderEvent.CelestialRingRenderEvent.Pre preEvent = new micdoodle8.mods.galacticraft.api.event.client.CelestialBodyRenderEvent.CelestialRingRenderEvent.Pre(body, systemOffset);
			MinecraftForge.EVENT_BUS.post(preEvent);
			if (!preEvent.isCanceled()) {
				GL11.glTranslatef(systemOffset.x, systemOffset.y, systemOffset.z);
				GL11.glBegin(2);

				for(int i = 0; i < 90; ++i) {
					GL11.glVertex2f((x + orbit_offset_x) * (orbit_ext_x > 0.0F ? orbit_ext_x : 1.0F), (y + orbit_offset_y) * (orbit_ext_y > 0.0F ? orbit_ext_y : 1.0F));
					float temp = x;
					x = cos * x - sin * y;
					y = sin * temp + cos * y;
				}

				GL11.glEnd();
				GL11.glTranslatef(-systemOffset.x, -systemOffset.y, -systemOffset.z);
			}

			micdoodle8.mods.galacticraft.api.event.client.CelestialBodyRenderEvent.CelestialRingRenderEvent.Post postEvent = new micdoodle8.mods.galacticraft.api.event.client.CelestialBodyRenderEvent.CelestialRingRenderEvent.Post(body);
			MinecraftForge.EVENT_BUS.post(postEvent);
		}
	}

	private void drawAgeCircle(Matrix4f worldMatrix) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glLineWidth(2.0F);
		float theta = 0.06981317F;
		float cos = (float)Math.cos(0.06981316953897476D);
		float sin = (float)Math.sin(0.06981316953897476D);
		CelestialBody body = this.selectedBody;
		Vector3f systemOffset = new Vector3f(0.0F, 0.0F, 0.0F);
		if (body instanceof Star) {
			systemOffset = this.getCelestialBodyPosition((Star)body);
		}

		float x = 1000.0F;
		float y = 0.0F;
		GL11.glTranslatef(systemOffset.x, systemOffset.y, systemOffset.z);

		for(int count_rings = 1; count_rings <= 8; ++count_rings) {
			x = (float)(1000 * count_rings);
			GL11.glColor4f(0.2F, 0.2F, 0.2F, 0.5F);
			GL11.glBegin(2);

			for(int i = 0; i < 90; ++i) {
				GL11.glVertex2f(x * 1.0F, y * 1.0F);
				float temp = x;
				x = cos * x - sin * y;
				y = sin * temp + cos * y;
			}

			GL11.glEnd();
			GlStateManager.pushMatrix();
			GL11.glEnable(3553);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			this.fontRenderer.drawString((int)(x / 1000.0F) * 5 + " l.y.", (int)x, (int)y, 16777215);
			GL11.glDisable(3553);
			GlStateManager.popMatrix();
		}

		GL11.glTranslatef(-systemOffset.x, -systemOffset.y, -systemOffset.z);
		GL11.glLineWidth(1.0F);
	}

	private void drawVortex() {
		for(Iterator var1 = this.bodiesToRender.iterator(); var1.hasNext(); GlStateManager.popMatrix()) {
			CelestialBody body = (CelestialBody)var1.next();
			GlStateManager.pushMatrix();
			new Vector3f(0.0F, 0.0F, 0.0F);
			BodiesData data = BodiesRegistry.getData(body);
			if (body instanceof Star && data != null && data.getStarClass() == StarClass.BLACKHOLE) {
				Vector3f systemOffset = this.getCelestialBodyPosition((Star)body);
				float size = (float)this.getWidthForCelestialBody(body) * 4.0F;
				float xOffset = systemOffset.x;
				float yOffset = systemOffset.y;
				GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
				float n = size / 2.0F;
				GlStateManager.translate(xOffset + -size / 2.0F + n, yOffset + -size / 2.0F + n, 0.0F);
				GlStateManager.rotate(this.ticksTotalF % 360.0F * 2.0F, 0.0F, 0.0F, 1.0F);
				GlStateManager.translate(-(xOffset + -size / 2.0F) - n, -(yOffset + -size / 2.0F) - n, 0.0F);
				this.mc.renderEngine.bindTexture(vortexTexture);
				this.drawTexturedModalRect(xOffset + -size / 2.0F, yOffset + -size / 2.0F, size, size, 0.0F, 0.0F, 16.0F, 16.0F, false, false, 16.0F, 16.0F);
			}
		}

	}

	protected Vector3f getCelestialBodyPosition(CelestialBody cBody) {
		if (cBody == null) {
			return new Vector3f(0.0F, 0.0F, 0.0F);
		} else if (cBody instanceof Star) {
			return cBody.getUnlocalizedName().equalsIgnoreCase("star.sol") ? new Vector3f() : ((Star)cBody).getParentSolarSystem().getMapPosition().toVector3f();
		} else {
			float timeScale = cBody instanceof Planet ? 200.0F : 2.0F;
			float orbit_ext_x = cBody instanceof IExBody ? ((IExBody)cBody).getXOrbitEccentricity() : 1.0F;
			float orbit_ext_y = cBody instanceof IExBody ? ((IExBody)cBody).getYOrbitEccentricity() : 1.0F;
			float orbit_offset_x = cBody instanceof IExBody ? ((IExBody)cBody).getXOrbitOffset() : 0.0F;
			float orbit_offset_y = cBody instanceof IExBody ? ((IExBody)cBody).getYOrbitOffset() : 0.0F;
			float distanceFromCenter = this.getScale(cBody);
			float x = (float)Math.sin((double)(this.ticksTotalF / (timeScale * cBody.getRelativeOrbitTime()) + cBody.getPhaseShift())) * distanceFromCenter;
			float y = (float)Math.cos((double)(this.ticksTotalF / (timeScale * cBody.getRelativeOrbitTime()) + cBody.getPhaseShift())) * distanceFromCenter;
			Vector3f cBodyPos = new Vector3f((x + orbit_offset_x) * (orbit_ext_x > 0.0F ? orbit_ext_x : 1.0F), (y + orbit_offset_y) * (orbit_ext_y > 0.0F ? orbit_ext_y : 1.0F), 0.0F);
			Vector3f parentVec;
			if (cBody instanceof Planet) {
				parentVec = this.getCelestialBodyPosition(((Planet)cBody).getParentSolarSystem().getMainStar());
				return Vector3f.add(cBodyPos, parentVec, (Vector3f)null);
			} else if (cBody instanceof IChildBody) {
				parentVec = this.getCelestialBodyPosition(((IChildBody)cBody).getParentPlanet());
				return Vector3f.add(cBodyPos, parentVec, (Vector3f)null);
			} else {
				if (cBody instanceof ICelestialRegistry) {
					ICelestialRegistry body = (ICelestialRegistry)cBody;
					if (body.getParentSolarSystem() != null) {
						parentVec = this.getCelestialBodyPosition(body.getParentSolarSystem().getMainStar());
						return Vector3f.add(cBodyPos, parentVec, (Vector3f)null);
					}
				}

				return cBodyPos;
			}
		}
	}

	protected float getScale(CelestialBody celestialBody) {
		float scale = 0.2F;
		if (celestialBody instanceof Planet) {
			scale = 25.0F;
		}

		if (celestialBody instanceof ICelestialRegistry && !((ICelestialRegistry)celestialBody instanceof IChildBody)) {
			scale = 25.0F;
		}

		return 3.0F * celestialBody.getRelativeDistanceFromCenter().unScaledDistance * scale;
	}

	protected void keyTyped(char keyChar, int keyID) throws IOException {
		if (this.mapMode) {
			super.keyTyped(keyChar, keyID);
		}

		if (keyID == 1 && !this.enableTraveltime && this.selectedBody != null) {
			this.unselectCelestialBody();
		}

		if (this.renamingSpaceStation) {
			String pastestring;
			if (keyID == 14) {
				if (this.renamingString != null && this.renamingString.length() > 0) {
					pastestring = this.renamingString.substring(0, this.renamingString.length() - 1);
					if (this.isValid(pastestring)) {
						this.renamingString = pastestring;
					} else {
						this.renamingString = "";
					}
				}
			} else if (keyChar == 22) {
				pastestring = GuiScreen.getClipboardString();
				if (pastestring == null) {
					pastestring = "";
				}

				if (this.isValid(this.renamingString + pastestring)) {
					this.renamingString = this.renamingString + pastestring;
					this.renamingString = this.renamingString.substring(0, Math.min(String.valueOf(this.renamingString).length(), 32));
				}
			} else if (this.isValid(this.renamingString + keyChar)) {
				this.renamingString = this.renamingString + keyChar;
				this.renamingString = this.renamingString.substring(0, Math.min(this.renamingString.length(), 32));
			}

		} else {
			if (keyID == 28 && !this.mc.player.capabilities.isCreativeMode && this.currenttier >= this.tierneed && this.enableNewTierSystem) {
				if (this.canTravel) {
					this.enableTraveltime = true;
				} else {
					this.teleportToSelectedBody();
				}
			}

		}
	}

	protected void teleportToSelectedBody() {
		if (this.selectedBody != null && this.selectedBody.getReachable()) {
			if (this.enableNewTierSystem) {
				if (this.currenttier < this.tierneed) {
					return;
				}
			} else if (this.possibleBodies == null || !this.possibleBodies.contains(this.selectedBody)) {
				return;
			}

			try {
				String dimension;
				int dimensionID;
				if (this.selectedBody instanceof Satellite) {
					if (this.spaceStationMap == null) {
						GCLog.severe("Please report as a BUG: spaceStationIDs was null.");
						return;
					}

					Satellite selectedSatellite = (Satellite)this.selectedBody;
					Integer mapping = ((StationDataGUI)((Map)this.spaceStationMap.get(this.getSatelliteParentID(selectedSatellite))).get(this.selectedStationOwner)).getStationDimensionID();
					if (mapping == null) {
						GCLog.severe("Problem matching player name in space station check: " + this.selectedStationOwner);
						return;
					}

					dimensionID = mapping;
					WorldProvider spacestation = WorldUtil.getProviderForDimensionClient(dimensionID);
					if (spacestation == null) {
						GCLog.severe("Failed to find a spacestation with dimension " + dimensionID);
						return;
					}

					dimension = "Space Station " + mapping;
				} else {
					dimensionID = this.selectedBody.getDimensionID();
					dimension = WorldUtil.getDimensionName(WorldUtil.getProviderForDimensionClient(dimensionID));
				}

				if (dimension.contains("$")) {
					this.mc.gameSettings.thirdPersonView = 0;
				}

				AsmodeusCore.packetPipeline.sendToServer(new ACPacketSimple(ACEnumSimplePacket.S_TELEPORT_ENTITY, GCCoreUtil.getDimensionID(this.mc.world), new Object[]{dimensionID, this.fuelSet}));
				this.mc.displayGuiScreen(new GuiCustomTeleporting(dimensionID));
			} catch (Exception var6) {
				var6.printStackTrace();
			}
		}

	}

	protected void drawTransitBar(int length) {
		int menuTopLeft = GuiCelestialSelection.BORDER_SIZE + GuiCelestialSelection.BORDER_EDGE_SIZE;
		this.mc.renderEngine.bindTexture(guiMain2);
		GL11.glColor4f(0.0F, 0.6F, 1.0F, 1.0F);
		this.drawTexturedModalRect(this.width / 2 - 113, menuTopLeft + 15, 225, 67, 0, 0, 225, 67, false, false);
		GL11.glColor4f(0.0F, 0.6F, 1.0F, 1.0F);
		this.drawTexturedModalRect(this.width / 2 - 50, menuTopLeft + 57, 0 + length, 10, 269, 0, 138, 10, false, false);
		String str;
		if (!(this.mc.player.world.provider instanceof WorldProviderSurface) && this.mc.player.world.provider instanceof IGalacticraftWorldProvider) {
			CelestialBody body = ((IGalacticraftWorldProvider)this.mc.player.world.provider).getCelestialBody();
			this.drawBodyOnGUI(body, this.width / 2 - 76, menuTopLeft + 54, 16, 16);
			str = GCCoreUtil.translate(body.getUnlocalizedName());
		} else {
			this.drawBodyOnGUI(GalacticraftCore.planetOverworld, this.width / 2 - 76, menuTopLeft + 54, 16, 16);
			str = GCCoreUtil.translate(GalacticraftCore.planetOverworld.getUnlocalizedName());
		}

		this.fontRenderer.drawString(str, this.width / 2 - 105, GuiCelestialSelection.BORDER_SIZE + GuiCelestialSelection.BORDER_EDGE_SIZE + 35, ColorUtil.to32BitColor(255, 255, 255, 255));
		this.drawBodyOnGUI(this.selectedBody, this.width / 2 + 59, menuTopLeft + 54, 16, 16);
		str = GCCoreUtil.translate(this.selectedBody.getUnlocalizedName());
		this.fontRenderer.drawString(str, this.width / 2 + 50, GuiCelestialSelection.BORDER_SIZE + GuiCelestialSelection.BORDER_EDGE_SIZE + 35, ColorUtil.to32BitColor(255, 255, 255, 255));
		str = "Boost: x" + this.coef;
		this.fontRenderer.drawString(str, this.width / 2 - (this.fontRenderer.getStringWidth(str) - 25), GuiCelestialSelection.BORDER_SIZE + GuiCelestialSelection.BORDER_EDGE_SIZE + 48, ColorUtil.to32BitColor(255, 0, 255, 255));
		double time = this.traveltime - this.timer;
		str = (int)(time / 100.0D) + "h " + (int)(time % 59.0D) + "m";
		this.fontRenderer.drawString(str, this.width / 2 - (this.fontRenderer.getStringWidth(str) - 25), GuiCelestialSelection.BORDER_SIZE + GuiCelestialSelection.BORDER_EDGE_SIZE + 59, ColorUtil.to32BitColor(255, 0, 255, 255));
		if (this.timer >= this.traveltime) {
			this.teleportToSelectedBody();
			this.enableTraveltime = false;
		}

	}

	protected int getScaledTravelTime(int barLength) {
		double relative = this.timer / this.traveltime;
		return (int)(relative * (double)barLength);
	}

	protected void drawBodyOnGUI(CelestialBody body, int x, int y, int w, int h) {
		if (body != null) {
			this.mc.renderEngine.bindTexture(body.getBodyIcon());
			this.drawFullSizedTexturedRect(x, y, w, h);
		}
	}

	public void drawFullSizedTexturedRect(int x, int y, int width, int height) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder worldRenderer = tessellator.getBuffer();
		worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
		worldRenderer.pos((double)x, (double)(y + height), (double)this.zLevel).tex(0.0D, 0.0D).endVertex();
		worldRenderer.pos((double)(x + width), (double)(y + height), (double)this.zLevel).tex(1.0D, 0.0D).endVertex();
		worldRenderer.pos((double)(x + width), (double)y, (double)this.zLevel).tex(1.0D, 1.0D).endVertex();
		worldRenderer.pos((double)x, (double)y, (double)this.zLevel).tex(0.0D, 1.0D).endVertex();
		tessellator.draw();
	}

	protected boolean canCreateSpaceStation(CelestialBody atBody) {
		if (!this.mapMode && !ConfigManagerCore.disableSpaceStationCreation && this.canCreateStations) {
			if (this.enableNewTierSystem) {
				if (this.currenttier < this.tierneed) {
					return false;
				}
			} else if (this.possibleBodies != null && !this.possibleBodies.contains(atBody)) {
				return false;
			}

			boolean foundRecipe = false;
			Iterator var3 = GalacticraftRegistry.getSpaceStationData().iterator();

			while(var3.hasNext()) {
				SpaceStationType type = (SpaceStationType)var3.next();
				if (type.getWorldToOrbitID() == atBody.getDimensionID()) {
					foundRecipe = true;
				}
			}

			if (!foundRecipe) {
				return false;
			} else if (!ClientProxyCore.clientSpaceStationID.containsKey(atBody.getDimensionID())) {
				return true;
			} else {
				int resultID = (Integer)ClientProxyCore.clientSpaceStationID.get(atBody.getDimensionID());
				return resultID == 0 || resultID == -1;
			}
		} else {
			return false;
		}
	}

	protected List<CelestialBody> getChildren(Object object, int start, int size) {
		List<CelestialBody> bodyList = Lists.newArrayList();Minecraft minecraft = FMLClientHandler.instance().getClient();
		EntityPlayerSP player = minecraft.player;
		EntityPlayerSP playerBaseClient = PlayerUtil.getPlayerBaseClientFromPlayer(player, false);

		PlayerClientSpaceData stats = null;

		if (player != null) {
			stats = playerBaseClient.getCapability(SpaceClientCapabilityHandler.PLAYER_SPACE_CLIENT_DATA, null);
		}
		SpaceData.refreshGalaxies();
		List planets;
		Iterator var6;
		CelestialBody body;
		List bodies;
		Iterator var11;
		if (object instanceof Planet) {
			planets = GalaxyRegistry.getMoonsForPlanet((Planet)object);

			var6 = planets.iterator();

			while(var6.hasNext()) {
				Moon moon = (Moon)var6.next();
				if (isUnlocked(moon.getName(), stats)) {
					bodyList.add(moon);
				}
			}


			bodies = SpaceData.getMoonsForPlanet((Planet)object);
			var11 = bodies.iterator();

			while(var11.hasNext()) {
				body = (CelestialBody)var11.next();
				if (body instanceof ICelestialRegistry && body instanceof IChildBody) {
					bodyList.add(body);
				}
			}
		} else if (object instanceof SolarSystem) {
			planets = GalaxyRegistry.getPlanetsForSolarSystem((SolarSystem)object);

				var6 = planets.iterator();

				while(var6.hasNext()) {
					Planet planet = (Planet)var6.next();
					if (isUnlocked(planet.getName(), stats)) {
						bodyList.add(planet);
					}
				}


			bodies = SpaceData.getBodiesForSolarSystem((SolarSystem)object);
			var11 = bodies.iterator();

			while(var11.hasNext()) {
				body = (CelestialBody)var11.next();
				if (body instanceof ICelestialRegistry && !(body instanceof IChildBody)) {
					bodyList.add(body);
				}
			}
		}

		Collections.sort(bodyList);
		if (!ClientProxy.smallInfoOnMap) {
			return bodyList;
		} else {
			List<CelestialBody> doneList = Lists.newArrayList();
			int startPos = start;
			int getSize = size;
			if (start >= bodyList.size()) {
				startPos = 0;
			}

			if (size >= bodyList.size()) {
				getSize = bodyList.size();
			}

			for(int i = 0; i < getSize; ++i) {
				doneList.add(i, bodyList.get(i + startPos));
			}

			return doneList;
		}
	}

	protected static enum EnumView {
		PREVIEW,
		PROFILE,
		GS;

		private EnumView() {
		}
	}
}
