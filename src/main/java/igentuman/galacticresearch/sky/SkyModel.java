package igentuman.galacticresearch.sky;

import igentuman.galacticresearch.sky.body.Asteroid;
import igentuman.galacticresearch.sky.body.Researchable;
import igentuman.galacticresearch.sky.body.Star;
import micdoodle8.mods.galacticraft.api.galaxies.*;

import java.util.ArrayList;
import java.util.List;

public class SkyModel {
    public static int width = 1200;
    public static int height = 1200;
    private static int starsDensity = 5000;
    public long seed;
    private Star[] stars;
    private List<Asteroid> asteroids = new ArrayList<>();

    private static SkyModel instance;

    private SkyModel()
    {
    }

    public void addAsteroid(String name)
    {
        asteroids.add(new Asteroid(name));
    }

    public void removeAsteroid(String name)
    {
        int i = 0;
        for(Asteroid a: asteroids) {
            if(a.getName().equals(name)) {
                asteroids.remove(i);
                return;
            }
            i++;
        }
    }

    public void setSeed(long seed)
    {
        this.seed = seed;
    }

    public static SkyModel get()
    {
        if(instance == null) {
            instance = new SkyModel();
        }
        return instance;
    }

    public SolarSystem getSolarSystemByCelestialBody(CelestialBody body)
    {
        if(body instanceof Planet) {
            return ((Planet) body).getParentSolarSystem();
        }
        if(body instanceof Moon) {
            return ((Moon) body).getParentPlanet().getParentSolarSystem();
        }
        if(body instanceof Satellite) {
            return ((Satellite) body).getParentPlanet().getParentSolarSystem();
        }
        return null;
    }

    public List<Researchable> getCurrentSystemBodies(int dim)
    {
        List<Researchable> res = new ArrayList<>();
        CelestialBody body = GalaxyRegistry.getCelestialBodyFromDimensionID(dim);
        SolarSystem solar = getSolarSystemByCelestialBody(body);
        if(solar == null) {
            return res;
        }
        for(Planet pl: GalaxyRegistry.getPlanetsForSolarSystem(solar)) {
            if(!pl.equals(body)) {
                res.add(Researchable.get(pl.getName()));
            } else {
                for(Moon moon: GalaxyRegistry.getMoonsForPlanet(pl)) {
                    res.add(Researchable.get(moon.getName()));
                }
            }
        }
        res.addAll(asteroids);

        return res;
    }

    public Star[] getStars()
    {
        if(stars == null) {
            stars = new Star[starsDensity];
            for (int i = 0; i < starsDensity; i++) {
                stars[i] = Star.generate();
            }
        }
        return stars;
    }

}
