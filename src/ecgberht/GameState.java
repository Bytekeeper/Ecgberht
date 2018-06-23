package ecgberht;

import bwem.BWEM;
import bwem.Base;
import bwem.ChokePoint;
import bwem.area.Area;
import bwem.unit.Geyser;
import bwem.unit.Mineral;
import bwta.BWTA;
import com.google.gson.Gson;
import ecgberht.Agents.Agent;
import ecgberht.Agents.VultureAgent;
import ecgberht.Agents.WraithAgent;
import ecgberht.Simulation.SimManager;
import ecgberht.Strategies.*;
import jfap.JFAP;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.*;
import org.openbw.bwapi4j.type.*;
import org.openbw.bwapi4j.unit.*;
import org.openbw.bwapi4j.util.Pair;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;

public class GameState extends GameHandler {

    public Base enemyBase = null;
    public boolean defense = false;
    public boolean enemyIsRandom = true;
    public boolean expanding = false;
    public boolean firstProxyBBS = false;
    public boolean movingToExpand = false;
    public boolean siegeResearched = false;
    public BuildingMap map;
    public BuildingMap testMap;
    public ChokePoint mainChoke = null;
    public EnemyInfo EI = new EnemyInfo(ih.enemy().getName());
    public Gson enemyInfoJSON = new Gson();
    public InfluenceMap inMap;
    public InfluenceMap inMapUnits;
    public int builtBuildings;
    public int builtCC;
    public int builtRefinery;
    public int frameCount;
    public int mapSize = 2;
    public int mining;
    public int startCount;
    public int trainedCombatUnits;
    public int trainedWorkers;
    public int vulturesTrained = 0;
    public int workerCountToSustain = 0;
    public JFAP simulator;
    public List<Base> blockedBLs = new ArrayList<>();
    public List<Base> BLs = new ArrayList<>();
    public List<Base> EnemyBLs = new ArrayList<>();
    public Map<VespeneGeyser, Boolean> vespeneGeysers = new TreeMap<>();
    public Map<GasMiningFacility, Integer> refineriesAssigned = new TreeMap<>();
    public Map<SCV, Pair<UnitType, TilePosition>> workerBuild = new TreeMap<>();
    public Map<Worker, Position> workerDefenders = new TreeMap<>();
    public Map<SCV, Building> repairerTask = new TreeMap<>();
    public Map<SCV, Building> workerTask = new TreeMap<>();
    public Map<Worker, GasMiningFacility> workerGas = new TreeMap<>();
    public Map<Position, MineralPatch> blockingMinerals = new HashMap<>();
    public Map<Base, CommandCenter> CCs = new HashMap<>();
    public Map<String, Squad> squads = new TreeMap<>();
    public Map<Bunker, Set<Unit>> DBs = new TreeMap<>();
    public Map<Unit, String> TTMs = new TreeMap<>();
    public Map<Unit, EnemyBuilding> enemyBuildingMemory = new TreeMap<>();
    public Map<MineralPatch, Integer> mineralsAssigned = new TreeMap<>();
    public Map<Unit, Agent> agents = new TreeMap<>();
    public Map<Worker, MineralPatch> workerMining = new TreeMap<>();
    public Map<Player, Integer> players = new HashMap<>();
    public Pair<Integer, Integer> deltaCash = new Pair<>(0, 0);
    public Pair<String, Unit> chosenMarine = null;
    public Player neutral;
    public Position attackPosition = null;
    public Race enemyRace = Race.Unknown;
    public Area naturalRegion = null;
    public Set<Base> ScoutSLs = new HashSet<>();
    public Set<Base> SLs = new HashSet<>();
    public Set<String> teamNames = new TreeSet<>(Arrays.asList("Alpha", "Bravo", "Charlie", "Delta",
            "Echo", "Foxtrot", "Golf", "Hotel", "India", "Juliet", "Kilo", "Lima", "Mike", "November", "Oscar", "Papa",
            "Quebec", "Romeo", "Sierra", "Tango", "Uniform", "Victor", "Whiskey", "X-Ray", "Yankee", "Zulu"));
    public Set<String> shipNames = new TreeSet<>(Arrays.asList("Adriatic", "Aegis Fate", "Agincourt", "Allegiance",
            "Apocalypso", "Athens", "Beatrice", "Bloodied Spirit", "Callisto", "Clarity of Faith", "Dawn Under Heaven",
            "Forward Unto Dawn", "Gettysburg", "Grafton", "Halcyon", "Hannibal", "Harbinger of Piety", "High Charity",
            "In Amber Clad", "Infinity", "Jericho", "Las Vegas", "Lawgiver", "Leviathan", "Long Night of Solace",
            "Matador", "Penance", "Persephone", "Pillar of Autumn", "Pitiless", "Pompadour", "Providence", "Revenant",
            "Savannah", "Shadow of Intent", "Spirit of Fire", "Tharsis", "Thermopylae"));
    public Set<Building> buildingLot = new TreeSet<>();
    public Set<ComsatStation> CSs = new TreeSet<>();
    public Set<Unit> enemyCombatUnitMemory = new TreeSet<>();
    public Set<Unit> enemyInBase = new TreeSet<>();
    public Set<Factory> Fs = new TreeSet<>();
    public Set<Barracks> MBs = new TreeSet<>();
    public Set<Starport> Ps = new TreeSet<>();
    public Set<SupplyDepot> SBs = new TreeSet<>();
    public Set<MissileTurret> Ts = new TreeSet<>();
    public Set<ResearchingFacility> UBs = new TreeSet<>();
    public Set<Worker> workerIdle = new TreeSet<>();
    public SupplyMan supplyMan;
    public Strategy strat;
    public TechType chosenResearch = null;
    public TilePosition checkScan = null;
    public TilePosition chosenBaseLocation = null;
    public TilePosition chosenPosition = null;
    public TilePosition initAttackPosition = null;
    public TilePosition initDefensePosition = null;
    public Worker chosenBuilderBL = null;
    public TrainingFacility chosenBuilding = null;
    public ExtendibleByAddon chosenBuildingAddon = null;
    public Building chosenBuildingLot = null;
    public Building chosenBuildingRepair = null;
    public Unit chosenBunker = null;
    public Worker chosenHarasser = null;
    public SCV chosenRepairer = null;
    public Worker chosenScout = null;
    public Unit chosenUnitToHarass = null;
    public ResearchingFacility chosenUnitUpgrader = null;
    public Worker chosenWorker = null;
    public Pair<Base, Unit> MainCC = null;
    public UnitType chosenAddon = null;
    public UnitType chosenToBuild = null;
    public UnitType chosenUnit = null;
    public UpgradeType chosenUpgrade = null;
    public boolean iReallyWantToExpand = false;
    public int directionScoutMain;
    public int maxWraiths = 5;
    public SimManager sim;
    public ChokePoint naturalChoke;
    public Position defendPosition = null;

    public GameState(BW bw, BWTA bwta, BWEM bwem) {
        super(bw, bwta, bwem);
        initPlayers();
        map = new BuildingMap(bw, ih.self(), bwem);
        map.initMap();
        testMap = map.clone();
        inMap = new InfluenceMap(bw, ih.self(), bw.getBWMap().mapHeight(), bw.getBWMap().mapWidth());
        mapSize = bwta.getStartLocations().size();
        simulator = new JFAP(bw);
        supplyMan = new SupplyMan(self.getRace());
        sim = new SimManager(bw);
    }

    public void initPlayers() {
        for (Player p : bw.getAllPlayers()) {
            //if(p.isObserver()) continue;
            if (p.isNeutral()) {
                players.put(p, 0);
                neutral = p;
            } else if (ih.allies().contains(p) || p.equals(self)) players.put(p, 1);
            else if (ih.enemies().contains(p)) players.put(p, -1);
        }
    }

    public Strategy initStrat() {
        try {
            BioBuild b = new BioBuild();
            ProxyBBS bbs = new ProxyBBS();
            BioMechBuild bM = new BioMechBuild();
            BioBuildFE bFE = new BioBuildFE();
            BioMechBuildFE bMFE = new BioMechBuildFE();
            FullMech FM = new FullMech();
            BioGreedyFE bGFE = new BioGreedyFE();
            //if (true) return bbs;
            String map = bw.getBWMap().mapFileName();
            if (enemyRace == Race.Zerg && EI.naughty) return b;
            if (EI.history.isEmpty()) {
                if (enemyRace == Race.Protoss) {
                    double random = Math.random();
                    if (random > 0.5) return b;
                    else return bM;
                }
                if (mapSize == 2 && !map.contains("Heartbreak Ridge")) {
                    double random = Math.random();
                    if (random > 0.5) return b;
                    else return bM;
                }
                if (map.contains("HeartbreakRidge")) {
                    double random = Math.random();
                    if (random > 0.75) return bFE;
                    else return b;
                } else {
                    double random = Math.random();
                    if (random > 0.5) return b;
                    else return bM;
                }
            } else {
                Map<String, Pair<Integer, Integer>> strategies = new TreeMap<>();
                Map<String, Strategy> nameStrat = new TreeMap<>();

                strategies.put(bbs.name, new Pair<>(0, 0));
                nameStrat.put(bbs.name, bbs);

                strategies.put(bFE.name, new Pair<>(0, 0));
                nameStrat.put(bFE.name, bFE);

                strategies.put(bMFE.name, new Pair<>(0, 0));
                nameStrat.put(bMFE.name, bMFE);

                strategies.put(FM.name, new Pair<>(0, 0));
                nameStrat.put(FM.name, FM);

                strategies.put(bGFE.name, new Pair<>(0, 0));
                nameStrat.put(bGFE.name, bGFE);

                strategies.put(bM.name, new Pair<>(0, 0));
                nameStrat.put(bM.name, bM);

                strategies.put(b.name, new Pair<>(0, 0));
                nameStrat.put(b.name, b);

                for (StrategyOpponentHistory r : EI.history) {
                    if (strategies.containsKey(r.strategyName)) {
                        strategies.get(r.strategyName).first += r.wins;
                        strategies.get(r.strategyName).second += r.losses;
                    }
                }
                int totalGamesPlayed = EI.wins + EI.losses;
                int DefaultStrategyWins = strategies.get(b.name).first;
                int DefaultStrategyLosses = strategies.get(b.name).second;
                int strategyGamesPlayed = DefaultStrategyWins + DefaultStrategyLosses;
                double winRate = strategyGamesPlayed > 0 ? DefaultStrategyWins / (double) (strategyGamesPlayed) : 0;
                if (strategyGamesPlayed < 2) {
                    ih.sendText("I dont know you that well yet, lets pick the standard strategy");
                    return b;
                }
                if (strategyGamesPlayed > 0 && winRate > 0.74) {
                    ih.sendText("Using default Strategy with winrate " + winRate * 100 + "%");
                    return b;
                }
                double C = 0.5;
                String bestUCBStrategy = null;
                double bestUCBStrategyVal = Double.MIN_VALUE;
                for (String strat : strategies.keySet()) {
                    if (map.contains("HeartbreakRidge") && (strat == "BioMechFE" || strat == "BioMech" || strat == "FullMech")) {
                        continue;
                    }
                    int sGamesPlayed = strategies.get(strat).first + strategies.get(strat).second;
                    double sWinRate = sGamesPlayed > 0 ? (strategies.get(strat).first / (double) (strategyGamesPlayed)) : 0;
                    double ucbVal = sGamesPlayed == 0 ? C : C * Math.sqrt(Math.log((double) (totalGamesPlayed / sGamesPlayed)));
                    double val = sWinRate + ucbVal;
                    if (val >= bestUCBStrategyVal) {
                        bestUCBStrategy = strat;
                        bestUCBStrategyVal = val;
                    }
                }
                ih.sendText("Chose: " + bestUCBStrategy + " with UCB: " + bestUCBStrategyVal);
                return nameStrat.get(bestUCBStrategy);
            }
        } catch (Exception e) {
            System.err.println("Error initStrat, loading default Strat");
            System.err.println(e);
            BioBuild b = new BioBuild();
            return b;

        }

    }

    public void initEnemyRace() {
        if (ih.enemy().getRace() != Race.Unknown) {
            enemyRace = ih.enemy().getRace();
            enemyIsRandom = false;
        }
    }

    public void initBlockingMinerals() {
        for (MineralPatch u : bw.getMineralPatches()) {
            if (u.getResources() == 0) blockingMinerals.put(u.getPosition(), u);
        }
    }

    public void checkBasesWithBLockingMinerals() {
        if (blockingMinerals.isEmpty()) return;
        for (bwem.Base b : BLs) {
            if (b.isStartingLocation()) continue;
            for (ChokePoint c : b.getArea().getChokePoints()) {
                for (Position m : blockingMinerals.keySet()) {
                    if (broodWarDistance(m, c.getCenter().toPosition()) < 40) {
                        blockedBLs.add(b);
                        break;
                    }
                }
            }
        }
    }

    public void playSound(String soundFile) {
        try {
            if (!ConfigManager.getConfig().ecgConfig.sounds) return;
            String run = getClass().getResource("GameState.class").toString();
            if (run.startsWith("jar:") || run.startsWith("rsrc:")) {
                InputStream fis = getClass().getClassLoader().getResourceAsStream(soundFile);
                javazoom.jl.player.Player playMP3 = new javazoom.jl.player.Player(fis);
                new Thread(() -> {
                    try {
                        playMP3.play();
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }).start();
            } else {
                soundFile = "src\\" + soundFile;
                FileInputStream fis = new FileInputStream(soundFile);
                javazoom.jl.player.Player playMP3 = new javazoom.jl.player.Player(fis);
                new Thread(() -> {
                    try {
                        playMP3.play();
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }).start();
            }

        } catch (Exception e) {
            System.err.println("playSound");
            System.err.println(e);
        }
    }

    public BW getGame() {
        return bw;
    }

    public InteractionHandler getIH() {
        return ih;
    }

    public Player getPlayer() {
        return self;
    }

    public void addNewResources(Unit unit) {
        List<Mineral> minerals = Util.getClosestBaseLocation(unit.getPosition()).getMinerals();
        List<Geyser> gas = Util.getClosestBaseLocation(unit.getPosition()).getGeysers();
        for (Mineral m : minerals) mineralsAssigned.put((MineralPatch) m.getUnit(), 0);
        for (Geyser g : gas) vespeneGeysers.put((VespeneGeyser) g.getUnit(), false);
        if (strat.name.equals("ProxyBBS")) {
            workerCountToSustain = (int) mineralGatherRateNeeded(Arrays.asList(UnitType.Terran_Marine, UnitType.Terran_Marine));
        }
    }

    public void removeResources(Unit unit) {
        List<Mineral> minerals = Util.getClosestBaseLocation(unit.getPosition()).getMinerals();
        List<Geyser> gas = Util.getClosestBaseLocation(unit.getPosition()).getGeysers();
        for (Mineral m : minerals) {
            if (mineralsAssigned.containsKey(m.getUnit())) {
                List<Unit> aux = new ArrayList<>();
                for (Entry<Worker, MineralPatch> w : workerMining.entrySet()) {
                    if (m.getUnit().equals(w.getValue())) {
                        aux.add(w.getKey());
                        workerIdle.add(w.getKey());
                    }
                }
                for (Unit u : aux) workerMining.remove(u);
                mineralsAssigned.remove(m.getUnit());
            }

        }
        for (Geyser g : gas) {
            VespeneGeyser geyser = (VespeneGeyser) g.getUnit(); // TODO improve
            if (vespeneGeysers.containsKey(geyser)) vespeneGeysers.remove(geyser);
        }
        List<Unit> auxGas = new ArrayList<>();
        for (Entry<GasMiningFacility, Integer> pm : refineriesAssigned.entrySet()) { // TODO test
            for (Geyser g : gas) {
                if (pm.getKey().equals(g.getUnit())) {
                    List<Unit> aux = new ArrayList<>();
                    for (Entry<Worker, GasMiningFacility> w : workerGas.entrySet()) {
                        if (pm.getKey().equals(w.getValue())) {
                            aux.add(w.getKey());
                            workerIdle.add(w.getKey());
                        }
                    }
                    for (Unit u : aux) workerGas.remove(u);
                    auxGas.add(pm.getKey());
                }
            }
        }
        for (Unit u : auxGas) refineriesAssigned.remove(u);
        if (strat.name.equals("ProxyBBS")) {
            workerCountToSustain = (int) mineralGatherRateNeeded(Arrays.asList(UnitType.Terran_Marine, UnitType.Terran_Marine));
        }
    }

    public Pair<Integer, Integer> getCash() {
        return new Pair<>(self.minerals(), self.gas());
    }

    public int getSupply() {
        return (self.supplyTotal() - self.supplyUsed());
    }

    public void debugText() {
        try {
            if (!ConfigManager.getConfig().ecgConfig.debugText) return;
            bw.getMapDrawer().drawTextScreen(320, 5, ColorUtil.formatText(supplyMan.getSupplyUsed() + "/" + supplyMan.getSupplyTotal(), ColorUtil.White));
            bw.getMapDrawer().drawTextScreen(320, 20, ColorUtil.formatText(getArmySize() + "/" + strat.armyForAttack, ColorUtil.White));
            String defending = defense ? ColorUtil.formatText("Defense", ColorUtil.Green) : ColorUtil.formatText("Defense", ColorUtil.Red);
            bw.getMapDrawer().drawTextScreen(320, 35, defending);

            if (ih.allies().size() + ih.enemies().size() == 1) {
                bw.getMapDrawer().drawTextScreen(10, 5,
                        ColorUtil.formatText(ih.self().getName(), ColorUtil.getColor(ih.self().getColor())) +
                                ColorUtil.formatText(" vs ", ColorUtil.White) +
                                ColorUtil.formatText(ih.enemy().getName(), ColorUtil.getColor(ih.enemy().getColor())));
            }
            if (chosenScout != null) {
                bw.getMapDrawer().drawTextScreen(10, 20, ColorUtil.formatText("Scouting: ", ColorUtil.White) + ColorUtil.formatText("Yes", ColorUtil.Green));
            } else {
                bw.getMapDrawer().drawTextScreen(10, 20, ColorUtil.formatText("Scouting: ", ColorUtil.White) + ColorUtil.formatText("No", ColorUtil.Red));
            }
            if (enemyBase != null) {
                bw.getMapDrawer().drawTextScreen(10, 35, ColorUtil.formatText("Enemy Base Found: ", ColorUtil.White) + ColorUtil.formatText("Yes", ColorUtil.Green));
            } else {
                bw.getMapDrawer().drawTextScreen(10, 35, ColorUtil.formatText("Enemy Base Found: ", ColorUtil.White) + ColorUtil.formatText("No", ColorUtil.Red));
            }
            bw.getMapDrawer().drawTextScreen(10, 50, ColorUtil.formatText("Strategy: ", ColorUtil.White) + ColorUtil.formatText(strat.name, ColorUtil.Yellow));
            bw.getMapDrawer().drawTextScreen(10, 65, ColorUtil.formatText("EnemyStrategy: ", ColorUtil.White) + ColorUtil.formatText(IntelligenceAgency.getEnemyStrat().toString(), ColorUtil.Yellow));
            bw.getMapDrawer().drawTextScreen(10, 80, ColorUtil.formatText("SimTime(ms): ", ColorUtil.White) + ColorUtil.formatText(String.valueOf(sim.time), ColorUtil.Teal));
            if (enemyRace == Race.Zerg && EI.naughty) {
                bw.getMapDrawer().drawTextScreen(10, 95, ColorUtil.formatText("Naughty Zerg: ", ColorUtil.White) + ColorUtil.formatText("yes", ColorUtil.Green));
            }
        } catch (Exception e) {
            System.err.println("debugText Exception");
            e.printStackTrace();
        }
    }

    public void debugScreen() {
        if (!ConfigManager.getConfig().ecgConfig.debugScreen) return;
        if (naturalRegion != null) print(naturalRegion.getTop().toTilePosition(), Color.RED);
        Integer counter = 0;
        for (bwem.Base b : BLs) {
            bw.getMapDrawer().drawTextMap(b.getLocation().toPosition(), counter.toString());
            counter++;
        }
        for (Agent ag : agents.values()) {
            if (ag instanceof VultureAgent) {
                VultureAgent vulture = (VultureAgent) ag;
                bw.getMapDrawer().drawTextMap(vulture.unit.getPosition(), ag.statusToString());
            }
            if (ag instanceof WraithAgent) {
                WraithAgent wraith = (WraithAgent) ag;
                bw.getMapDrawer().drawTextMap(wraith.unit.getPosition(), ag.statusToString());
                bw.getMapDrawer().drawTextMap(wraith.unit.getPosition().add(new Position(0,
                        UnitType.Terran_Wraith.dimensionUp())), wraith.name);
            }
        }
        if (mainChoke != null) bw.getMapDrawer().drawTextMap(mainChoke.getCenter().toPosition(), "MainChoke");
        if (naturalChoke != null) bw.getMapDrawer().drawTextMap(naturalChoke.getCenter().toPosition(), "NatChoke");
        if (chosenBuilderBL != null) {
            bw.getMapDrawer().drawTextMap(chosenBuilderBL.getPosition(), "BuilderBL");
            print(chosenBuilderBL, Color.BLUE);
        }
        if (chosenHarasser != null) {
            bw.getMapDrawer().drawTextMap(chosenHarasser.getPosition(), "Harasser");
            print(chosenHarasser, Color.BLUE);
        }
        if (chosenBaseLocation != null) {
            print(chosenBaseLocation, UnitType.Terran_Command_Center, Color.CYAN);
        }
        for (Entry<SCV, Pair<UnitType, TilePosition>> u : workerBuild.entrySet()) {
            print(u.getKey(), Color.TEAL);
            bw.getMapDrawer().drawTextMap(u.getKey().getPosition(), "Building " + u.getValue().first.toString());
            print(u.getValue().second, u.getValue().first, Color.TEAL);
            bw.getMapDrawer().drawLineMap(u.getKey().getPosition(), getCenterFromBuilding(u.getValue().second.toPosition(), u.getValue().first), Color.RED);
        }
        if (chosenUnitToHarass != null) {
            print(chosenUnitToHarass, Color.RED);
            bw.getMapDrawer().drawTextMap(chosenUnitToHarass.getPosition(), "UnitToHarass");
        }
        for (SCV r : repairerTask.keySet()) {
            print(r, Color.YELLOW);
            bw.getMapDrawer().drawTextMap(r.getPosition(), "Repairer");
        }
        if (chosenScout != null) {
            bw.getMapDrawer().drawTextMap(chosenScout.getPosition(), "Scouter");
            print(chosenScout, Color.PURPLE);
        }
        if (chosenRepairer != null) bw.getMapDrawer().drawTextMap(chosenRepairer.getPosition(), "ChosenRepairer");
        for (ChokePoint c : bwem.getMap().getChokePoints()) {
            List<WalkPosition> sides = c.getGeometry();
            if (sides.size() == 3) {
                bw.getMapDrawer().drawLineMap(sides.get(1).toPosition(), sides.get(2).toPosition(), Color.GREEN);
            }
        }
        for (Unit u : CCs.values()) {
            print(u, Color.YELLOW);
            bw.getMapDrawer().drawCircleMap(u.getPosition(), 500, Color.ORANGE);
        }
        for (Unit u : DBs.keySet()) {
            bw.getMapDrawer().drawCircleMap(u.getPosition(), 300, Color.ORANGE);
        }
        for (Unit u : workerIdle) print(u, Color.ORANGE);
        for (Entry<SCV, Building> u : workerTask.entrySet()) {
            print(u.getKey(), Color.TEAL);
            bw.getMapDrawer().drawTextMap(u.getKey().getPosition(), "Tasked: " + u.getValue().getInitialType().toString());
            print(u.getValue(), Color.TEAL);
            bw.getMapDrawer().drawLineMap(u.getKey().getPosition(), u.getValue().getPosition(), Color.RED);
        }

        for (Worker u : workerDefenders.keySet()) {
            print(u, Color.PURPLE);
            bw.getMapDrawer().drawTextMap(u.getPosition(), "Spartan");
        }
        for (Entry<Worker, MineralPatch> u : workerMining.entrySet()) {
            print(u.getKey(), Color.CYAN);
            bw.getMapDrawer().drawLineMap(u.getKey().getPosition(), u.getValue().getPosition(), Color.CYAN);
        }
        for (Entry<Worker, GasMiningFacility> u : workerGas.entrySet()) {
            if (u.getKey().getOrder() == Order.HarvestGas) continue;
            print(u.getKey(), Color.GREEN);
            bw.getMapDrawer().drawLineMap(u.getKey().getPosition(), u.getValue().getPosition(), Color.GREEN);
        }
        for (Entry<VespeneGeyser, Boolean> u : vespeneGeysers.entrySet()) {
            print(u.getKey(), Color.GREEN);
            if (refineriesAssigned.containsKey(u.getKey())) {
                int gas = refineriesAssigned.get(u.getKey());
                bw.getMapDrawer().drawTextMap(u.getKey().getPosition(), ColorUtil.formatText(Integer.toString(gas), ColorUtil.White));
            }
        }
        for (Squad s : squads.values()) {
            if (s.members.isEmpty()) continue;
            Position center = getSquadCenter(s);
            //if(s.attack != null) bw.getMapDrawer().drawLineMap(s.attack,center, Color.GREEN);
            bw.getMapDrawer().drawCircleMap(center, 80, Color.GREEN);
            bw.getMapDrawer().drawTextMap(center, ColorUtil.formatText(s.name, ColorUtil.White));
            bw.getMapDrawer().drawTextMap(center.add(new Position(0, UnitType.Terran_Marine.dimensionUp())), ColorUtil.formatText(s.status.toString(), ColorUtil.White));
        }
        for (Entry<MineralPatch, Integer> m : mineralsAssigned.entrySet()) {
            print(m.getKey(), Color.CYAN);
            if (m.getValue() == 0) continue;
            bw.getMapDrawer().drawTextMap(m.getKey().getPosition(), ColorUtil.formatText(m.getValue().toString(), ColorUtil.White));
        }
    }

    public void print(Unit u, Color color) {
        bw.getMapDrawer().drawBoxMap(u.getLeft(), u.getTop(), u.getRight(), u.getBottom(), color);
    }

    public void print(TilePosition u, UnitType type, Color color) {
        Position leftTop = new Position(u.getX() * TilePosition.SIZE_IN_PIXELS, u.getY() * TilePosition.SIZE_IN_PIXELS);
        Position rightBottom = new Position(leftTop.getX() + type.tileWidth() * TilePosition.SIZE_IN_PIXELS, leftTop.getY() + type.tileHeight() * TilePosition.SIZE_IN_PIXELS);
        bw.getMapDrawer().drawBoxMap(leftTop, rightBottom, color);
    }

    public void print(TilePosition u, Color color) {
        Position leftTop = new Position(u.getX() * TilePosition.SIZE_IN_PIXELS, u.getY() * TilePosition.SIZE_IN_PIXELS);
        Position rightBottom = new Position(leftTop.getX() + TilePosition.SIZE_IN_PIXELS, leftTop.getY() + TilePosition.SIZE_IN_PIXELS);
        bw.getMapDrawer().drawBoxMap(leftTop, rightBottom, color);
    }

    public void initStartLocations() {
        Base startBot = Util.getClosestBaseLocation(self.getStartLocation().toPosition());
        for (bwem.Base b : bwem.getMap().getBases()) {
            if (b.isStartingLocation() && !b.getLocation().equals(startBot.getLocation())) {
                SLs.add(b);
                ScoutSLs.add(b);
            }
        }
    }

    public void initBaseLocations() {
        BLs.addAll(bwem.getMap().getBases());
        Collections.sort(BLs, new BaseLocationComparator(Util.getClosestBaseLocation(self.getStartLocation().toPosition())));
    }

    /*public void moveUnitFromChokeWhenExpand() {
        try {
            if (!squads.isEmpty() && chosenBaseLocation != null) {
                Area chosenRegion = bwem.getMap().getArea(chosenBaseLocation);
                if (chosenRegion != null) {
                    if (chosenRegion.equals(naturalRegion)) {
                        TilePosition mapCenter = new TilePosition(bw.getBWMap().mapWidth(), bw.getBWMap().mapHeight());
                        List<ChokePoint> cs = chosenRegion.getChokePoints();
                        ChokePoint closestChoke = null;
                        for (ChokePoint c : cs) {
                            if (!c.getCenter().toTilePosition().equals(this.mainChoke.getCenter().toTilePosition())) {
                                double aux = broodWarDistance(c.getCenter().toPosition(), chosenBaseLocation.toPosition());
                                if (aux > 0.0) {
                                    if (closestChoke == null || aux < broodWarDistance(closestChoke.getCenter().toPosition(), mapCenter.toPosition())) {
                                        closestChoke = c;
                                    }
                                }
                            }
                        }
                        if (closestChoke != null) {
                            for (Squad s : squads.values()) {
                                if (s.status == Status.IDLE) {
                                    s.giveAttackOrder(closestChoke.getCenter().toPosition());
                                    s.status = Status.ATTACK;
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("MoveUnitFromChokeWhenExpand");
            System.err.println(e);
        }
    }*/

    public void fix() {
        if (defense && enemyInBase.isEmpty()) {
            defense = false;
        }
        if (frameCount < 5) {
            for (Entry<Worker, MineralPatch> w : workerMining.entrySet()) {
                if (mineralsAssigned.get(w.getValue()) == 0) {
                    mineralsAssigned.put(w.getValue(), 1);
                    w.getKey().gather(w.getValue());
                }
            }
        }
        List<String> squadsToClean = new ArrayList<>();
        for (Squad s : squads.values()) {
            List<Unit> aux = new ArrayList<>();
            for (Unit u : s.members) {
                if (!u.exists()) aux.add(u);
            }

            if (s.members.isEmpty() || aux.size() == s.members.size()) {
                squadsToClean.add(s.name);
                continue;
            } else s.members.removeAll(aux);

        }
        List<Unit> bunkers = new ArrayList<>();
        for (Entry<Bunker, Set<Unit>> u : DBs.entrySet()) {
            if (u.getKey().exists()) continue;
            for (Unit m : u.getValue()) {
                if (m.exists()) addToSquad(m);
            }
            bunkers.add(u.getKey());
        }
        for (Unit c : bunkers) DBs.remove(c);

        for (String name : squadsToClean) squads.remove(name);

        if (chosenScout != null && chosenScout.isIdle()) {
            workerIdle.add(chosenScout);
            chosenScout = null;
        }
        if (chosenBuilderBL != null && (chosenBuilderBL.isIdle() || chosenBuilderBL.isGatheringGas() || chosenBuilderBL.isGatheringMinerals())) {
            workerIdle.add(chosenBuilderBL);
            chosenBuilderBL = null;
            movingToExpand = false;
            expanding = false;
            chosenBaseLocation = null;
        }
        if (chosenBuilderBL != null && workerIdle.contains(chosenBuilderBL)) workerIdle.remove(chosenBuilderBL);

        List<Unit> aux3 = new ArrayList<>();
        for (Entry<SCV, Pair<UnitType, TilePosition>> u : workerBuild.entrySet()) {
            if ((u.getKey().isIdle() || u.getKey().isGatheringGas() || u.getKey().isGatheringMinerals()) &&
                    broodWarDistance(u.getKey().getPosition(), u.getValue().second.toPosition()) > 100) {
                aux3.add(u.getKey());
                deltaCash.first -= u.getValue().first.mineralPrice();
                deltaCash.second -= u.getValue().first.gasPrice();
                workerIdle.add(u.getKey());
            }
        }
        for (Unit u : aux3) workerBuild.remove(u);

        List<Unit> aux4 = new ArrayList<>();
        for (SCV r : repairerTask.keySet()) {
            if (r.equals(chosenScout)) chosenScout = null;
            if (!r.isRepairing() || r.isIdle()) {
                if (chosenRepairer != null) {
                    if (r.equals(chosenRepairer)) chosenRepairer = null;
                }
                workerIdle.add(r);
                aux4.add(r);
            }
        }
        for (Unit u : aux4) repairerTask.remove(u);

        List<Unit> aux5 = new ArrayList<>();
        for (Worker r : workerDefenders.keySet()) {
            if (r.isIdle() || r.isGatheringMinerals()) {
                workerIdle.add(r);
                aux5.add(r);
            }
        }
        for (Unit u : aux5) workerDefenders.remove(u);

        List<String> aux6 = new ArrayList<>();
        for (Squad u : squads.values()) {
            if (u.members.isEmpty()) aux6.add(u.name);
        }
        for (String s : aux6) squads.remove(s);
    }

    public void checkMainEnemyBase() {
        if (enemyBuildingMemory.isEmpty() && ScoutSLs.isEmpty()) {
            enemyBase = null;
            chosenScout = null;
            ScoutSLs.clear();
            for (bwem.Base b : BLs) {
                if (!CCs.containsKey(b) && bwta.isConnected(self.getStartLocation(), b.getLocation())) {
                    ScoutSLs.add(b);
                }
            }
        }
    }

    // Based on BWEB, thanks @Fawx
    public void initChokes() {
        // Main choke
        naturalRegion = BLs.get(1).getArea();
        double distBest = Double.MAX_VALUE;
        for (ChokePoint choke : naturalRegion.getChokePoints()) {
            double dist = bwta.getGroundDistance(choke.getCenter().toTilePosition(), getPlayer().getStartLocation());
            if (dist < distBest && dist > 0.0) {
                mainChoke = choke;
                distBest = dist;
            }
        }
        if (mainChoke != null) {
            initAttackPosition = mainChoke.getCenter().toTilePosition();
            initDefensePosition = mainChoke.getCenter().toTilePosition();
        } else {
            initAttackPosition = self.getStartLocation();
            initDefensePosition = self.getStartLocation();
        }
        // Natural choke
        // Exception for maps with a natural behind the main such as Crossing Fields
        if (bwta.getGroundDistance(self.getStartLocation(), bwem.getMap().getData().getMapData().getCenter().toTilePosition()) < bwta.getGroundDistance(BLs.get(1).getLocation(), bwem.getMap().getData().getMapData().getCenter().toTilePosition())) {
            naturalChoke = mainChoke;
            return;
        }
        // Find area that shares the choke we need to defend
        distBest = Double.MAX_VALUE;
        Area second = null;
        for (Area a : naturalRegion.getAccessibleNeighbors()) {
            WalkPosition center = a.getTop();
            double dist = center.toPosition().getDistance(bwem.getMap().getData().getMapData().getCenter());
            if (bw.getBWMap().isValidPosition(center) && dist < distBest) {
                second = a;
                distBest = dist;
            }
        }
        // Find second choke based on the connected area
        distBest = Double.MAX_VALUE;
        for (ChokePoint choke : naturalRegion.getChokePoints()) {
            if (choke.getCenter() == mainChoke.getCenter()) continue;
            if (choke.isBlocked() || choke.getGeometry().size() <= 3) continue;
            if (choke.getAreas().first != second && choke.getAreas().second != second) continue;
            double dist = choke.getCenter().toPosition().getDistance(self.getStartLocation().toPosition());
            if (dist < distBest) {
                naturalChoke = choke;
                distBest = dist;
            }
        }
    }

    public void checkUnitsBL(TilePosition BL, Unit chosen) {
        UnitType type = UnitType.Terran_Command_Center;
        Position topLeft = new Position(BL.getX() * TilePosition.SIZE_IN_PIXELS, BL.getY() * TilePosition.SIZE_IN_PIXELS);
        Position bottomRight = new Position(topLeft.getX() + type.tileWidth() * TilePosition.SIZE_IN_PIXELS, topLeft.getY() + type.tileHeight() * TilePosition.SIZE_IN_PIXELS);
        List<Unit> blockers = Util.getUnitsInRectangle(topLeft, bottomRight);
        if (!blockers.isEmpty()) {
            for (Unit u : blockers) {
                if (((PlayerUnit) u).getPlayer().getId() == self.getId() && !u.equals(chosen) && !(u instanceof Worker)) {
                    ((MobileUnit) u).move(Util.getClosestChokepoint(BL.toPosition()).getCenter().toPosition());
                }
            }
        }
    }

    public String getSquadName() {
        if (teamNames.size() == squads.size()) {
            String gg = null;
            while (gg == null || squads.containsKey(gg)) gg = "RandomSquad" + new Random().toString();
            return gg;
        }
        String name = null;
        while (name == null || squads.containsKey(name)) {
            int index = new Random().nextInt(teamNames.size());
            Iterator<String> iter = teamNames.iterator();
            for (int i = 0; i < index; i++) name = iter.next();
        }
        return name;
    }

    public String addToSquad(Unit unit) {
        String name;
        if (squads.size() == 0) {
            Squad aux = new Squad(getSquadName());
            aux.addToSquad(unit);
            squads.put(aux.name, aux);
            name = aux.name;
        } else {
            String chosen = null;
            for (Entry<String, Squad> s : squads.entrySet()) {
                if (s.getValue().members.size() < 12 && broodWarDistance(getSquadCenter(s.getValue()),
                        unit.getPosition()) < 1000 && (chosen == null || broodWarDistance(unit.getPosition(),
                        getSquadCenter(s.getValue())) < broodWarDistance(unit.getPosition(),
                        getSquadCenter(squads.get(chosen))))) {
                    chosen = s.getKey();
                }
            }
            if (chosen != null) {
                squads.get(chosen).addToSquad(unit);
                name = chosen;
            } else {
                Squad newSquad = new Squad(getSquadName());
                newSquad.addToSquad(unit);
                squads.put(newSquad.name, newSquad);
                name = newSquad.name;
            }
        }
        return name;
    }

    public Position getSquadCenter(Squad s) {
        Position point = new Position(0, 0);
        for (Unit u : s.members) {
            if (s.members.size() == 1) return u.getPosition();
            point = new Position(point.getX() + u.getPosition().getX(), point.getY() + u.getPosition().getY());
        }
        return new Position(point.getX() / s.members.size(), point.getY() / s.members.size());

    }

    public void removeFromSquad(Unit unit) {
        for (Entry<String, Squad> s : squads.entrySet()) {
            if (s.getValue().members.contains(unit)) {
                if (s.getValue().members.size() == 1) squads.remove(s.getKey());
                else s.getValue().members.remove(unit);
                break;
            }
        }
    }

    public int getArmySize() {
        int count = 0;
        if (squads.isEmpty()) return count;
        else {
            for (Entry<String, Squad> s : squads.entrySet()) count += s.getValue().getArmyCount();
        }
        return count + agents.size() * 2;
    }

    public void siegeTanks() {
        if (!squads.isEmpty()) {
            Set<SiegeTank> tanks = new TreeSet<>();
            for (Entry<String, Squad> s : squads.entrySet()) tanks.addAll(s.getValue().getTanks());
            if (!tanks.isEmpty()) {
                for (SiegeTank t : tanks) {
                    boolean far = false;
                    boolean close = false;
                    for (Unit e : enemyCombatUnitMemory) {
                        double distance = broodWarDistance(e.getPosition(), t.getPosition());
                        if (distance > UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().maxRange()) continue;
                        if (distance <= UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().minRange()) {
                            close = true;
                            break;
                        }
                        UnitType eType = Util.getType((PlayerUnit) e);
                        if (Util.isEnemy(((PlayerUnit) e).getPlayer()) && !(e instanceof Worker) && !eType.isFlyer() && (eType.canAttack() || eType == UnitType.Terran_Bunker)) {
                            far = true;
                            break;
                        }
                    }
                    if (close && !far) {
                        if (t.isSieged() && t.getOrder() != Order.Unsieging) t.unsiege();
                        continue;
                    }
                    if (far) {
                        if (!t.isSieged() && t.getOrder() != Order.Sieging) t.siege();
                        continue;
                    }
                    if (t.isSieged() && t.getOrder() != Order.Unsieging) t.unsiege();
                }
            }
        }
    }

    public boolean checkSupply() {
        for (Pair<UnitType, TilePosition> w : workerBuild.values()) {
            if (w.first == UnitType.Terran_Supply_Depot) return true;
        }
        for (Building w : workerTask.values()) {
            if (w instanceof SupplyDepot) return true;
        }
        return false;
    }

    public int getCombatUnitsBuildings() {
        int count;
        count = MBs.size() + Fs.size();
        if (count == 0) return 1;
        return count;
    }

    public double getMineralRate() {
        double rate = 0.0;
        if (frameCount > 0) rate = ((double) self.gatheredMinerals() - 50) / frameCount;
        return rate;
    }

    public Position getCenterFromBuilding(Position leftTop, UnitType type) {
        Position rightBottom = new Position(leftTop.getX() + type.tileWidth() * TilePosition.SIZE_IN_PIXELS, leftTop.getY() + type.tileHeight() * TilePosition.SIZE_IN_PIXELS);
        Position center = new Position((leftTop.getX() + rightBottom.getX()) / 2, (leftTop.getY() + rightBottom.getY()) / 2);
        return center;

    }

    //TODO Real maths
    public int getMineralsWhenReaching(TilePosition start, TilePosition end) {
        double rate = getMineralRate();
        double distance = bwta.getGroundDistance(start, end);
        double frames = distance / 2.55;
        return (int) (rate * frames);
    }

    public void mineralLocking() {
        for (Entry<Worker, MineralPatch> u : workerMining.entrySet()) {
            if (u.getKey().isIdle() || (u.getKey().getTargetUnit() == null && !Order.MoveToMinerals.equals(u.getKey().getOrder())))
                u.getKey().gather(u.getValue());
            else if (u.getKey().getTargetUnit() != null) {
                if (!u.getKey().getTargetUnit().equals(u.getValue()) && u.getKey().getOrder() == Order.MoveToMinerals && !u.getKey().isCarryingMinerals()) {
                    u.getKey().gather(u.getValue());
                }
            }
        }
    }

    public Position getNearestCC(Position position) {
        Unit chosen = null;
        double distance = Double.MAX_VALUE;
        for (Unit u : CCs.values()) {
            double distance_aux = broodWarDistance(u.getPosition(), position);
            if (distance_aux > 0.0 && (chosen == null || distance_aux < distance)) {
                chosen = u;
                distance = distance_aux;
            }
        }
        if (chosen != null) return chosen.getPosition();
        return null;
    }

    public void readOpponentInfo() {
        String name = ih.enemy().getName();
        String path = "bwapi-data/read/" + name + ".json";
        try {
            if (Files.exists(Paths.get(path))) {
                EI = enemyInfoJSON.fromJson(new FileReader(path), EnemyInfo.class);
                return;
            }
            path = "bwapi-data/write/" + name + ".json";
            if (Files.exists(Paths.get(path))) {
                EI = enemyInfoJSON.fromJson(new FileReader(path), EnemyInfo.class);
                return;
            }
            path = "bwapi-data/AI/" + name + ".json";
            if (Files.exists(Paths.get(path))) {
                EI = enemyInfoJSON.fromJson(new FileReader(path), EnemyInfo.class);
                return;
            }
        } catch (Exception e) {
            System.err.println("readOpponentInfo");
            System.err.println(e);
        }
    }

    public void writeOpponentInfo(String name) {
        String dir = "bwapi-data/write/";
        String path = dir + name + ".json";
        ih.sendText("Writing result to: " + path);
        Gson aux = new Gson();
        if (enemyIsRandom && EI.naughty) EI.naughty = false;
        String print = aux.toJson(EI);
        File directory = new File(dir);
        if (!directory.exists()) directory.mkdir();
        try (PrintWriter out = new PrintWriter(path)) {
            out.println(print);
        } catch (FileNotFoundException e) {
            System.err.println("writeOpponentInfo");
            System.err.println(e);
        }
    }

    public TilePosition getBunkerPositionAntiPool() {
        try {
            TilePosition rax = MBs.iterator().next().getTilePosition();
            UnitType bunker = UnitType.Terran_Bunker;
            int dist = 0;
            TilePosition chosen = null;
            while (chosen == null) {
                List<TilePosition> sides = new ArrayList<>();
                if (rax.getY() - bunker.tileHeight() - dist >= 0) {
                    TilePosition up = new TilePosition(rax.getX(), rax.getY() - bunker.tileHeight() - dist);
                    sides.add(up);
                }
                if (rax.getY() + UnitType.Terran_Barracks.tileHeight() + dist < bw.getBWMap().mapHeight()) {
                    TilePosition down = new TilePosition(rax.getX(), rax.getY() + UnitType.Terran_Barracks.tileHeight() + dist);
                    sides.add(down);
                }
                if (rax.getX() - bunker.tileWidth() - dist >= 0) {
                    TilePosition left = new TilePosition(rax.getX() - bunker.tileWidth() - dist, rax.getY());
                    sides.add(left);
                }
                if (rax.getX() + UnitType.Terran_Barracks.tileWidth() + dist < bw.getBWMap().mapWidth()) {
                    TilePosition right = new TilePosition(rax.getX() + UnitType.Terran_Barracks.tileWidth() + dist, rax.getY());
                    sides.add(right);
                }
                for (TilePosition tile : sides) {
                    if ((chosen == null && bw.canBuildHere(tile, bunker)) || (mainChoke.getCenter().toTilePosition().getDistance(tile) < mainChoke.getCenter().toTilePosition().getDistance(chosen) && bw.canBuildHere(tile, bunker))) {
                        chosen = tile;
                    }
                }
                dist++;
            }
            return chosen;
        } catch (Exception e) {
            System.err.println(e);
            return null;
        }

    }

    public void updateEnemyBuildingsMemory() {
        List<Unit> aux = new ArrayList<>();
        for (EnemyBuilding u : enemyBuildingMemory.values()) {
            if (bw.getBWMap().isVisible(u.pos)) {
                if (!Util.getUnitsOnTile(u.pos).contains(u.unit)) aux.add(u.unit); // TODO test
                else if (u.unit.isVisible()) u.pos = u.unit.getTilePosition();
                u.type = Util.getType(u.unit);
            }
        }
        for (Unit u : aux) enemyBuildingMemory.remove(u);
    }

    public void mergeSquads() {
        try {
            if (squads.isEmpty()) return;
            if (squads.size() < 2) return;
            for (Squad u1 : squads.values()) {
                int u1_size = u1.members.size();
                if (u1_size < 12) {
                    for (Squad u2 : squads.values()) {
                        if (u2.name.equals(u1.name) || u2.members.size() > 11) continue;
                        if (broodWarDistance(getSquadCenter(u1), getSquadCenter(u2)) < 200) {
                            if (u1_size + u2.members.size() > 12) continue;
                            else {
                                u1.members.addAll(u2.members);
                                u2.members.clear();
                            }
                            break;
                        }
                    }
                    break;
                }
            }
            Set<Squad> aux = new TreeSet<>();
            for (Squad u : squads.values()) {
                if (u.members.isEmpty()) aux.add(u);
            }
            squads.values().removeAll(aux);
        } catch (Exception e) {
            System.err.println("mergeSquads");
            System.err.println(e);
        }
    }

    public void updateSquadOrderAndMicro() {
        for (Squad u : squads.values()) u.microUpdateOrder();
    }

    public int countUnit(UnitType type) {
        int count = 0;
        for (Pair<UnitType, TilePosition> w : workerBuild.values()) {
            if (w.first == type) count++;
        }
        count += Util.countUnitTypeSelf(type);
        return count;
    }

    /**
     * Credits and Thanks to Yegers for the method
     *
     * @param units List of units that are to be sustained.
     * @return Number of workers required.
     * @author Yegers
     * Number of workers needed to sustain a number of units.
     * This method assumes that the required buildings are available.
     * Example usage: to sustain building 2 marines at the same time from 2 barracks.
     */
    public double mineralGatherRateNeeded(final List<UnitType> units) {
        double mineralsRequired = 0.0;
        double m2f = (4.53 / 100.0) / 65.0;
        double SaturationX2_Slope = -1.5;
        double SaturationX1 = m2f * 65.0;
        double SaturationX2_B = m2f * 77.5;
        for (UnitType unit : units) mineralsRequired += (((double) unit.mineralPrice()) / unit.buildTime()) / 1.0;
        double workersRequired = mineralsRequired / SaturationX1;
        if (workersRequired > mineralsAssigned.size())
            return Math.ceil((mineralsRequired - SaturationX2_B / 1.0) / SaturationX2_Slope);
        return Math.ceil(workersRequired);
    }

    public void checkWorkerMilitia() {
        if (countUnit(UnitType.Terran_Barracks) == 2) {
            List<Unit> aux = new ArrayList<>();
            int count = workerMining.size();
            for (Entry<Worker, MineralPatch> scv : workerMining.entrySet()) {
                if (count <= workerCountToSustain) break;
                if (!scv.getKey().isCarryingMinerals()) {
                    scv.getKey().move(new TilePosition(bw.getBWMap().mapWidth() / 2, bw.getBWMap().mapHeight() / 2).toPosition());
                    addToSquad(scv.getKey());
                    if (mineralsAssigned.containsKey(scv.getValue())) {
                        mining--;
                        mineralsAssigned.put(scv.getValue(), mineralsAssigned.get(scv.getValue()) - 1);
                    }
                    aux.add(scv.getKey());
                    count--;
                }
            }
            for (Unit u : aux) workerMining.remove(u);
        }

    }

    //Credits to @PurpleWaveJadien
    public double broodWarDistance(Position a, Position b) {
        double dx = Math.abs(a.getX() - b.getX());
        double dy = Math.abs(a.getY() - b.getY());
        double d = Math.min(dx, dy);
        double D = Math.max(dx, dy);
        if (d < D / 4) return D;
        return D - D / 16 + d * 3 / 8 - D / 64 + d * 3 / 256;
    }

    public double broodWarDistance(double[] a, double[] b) {
        double dx = Math.abs(a[0] - b[0]);
        double dy = Math.abs(a[1] - b[1]);
        double d = Math.min(dx, dy);
        double D = Math.max(dx, dy);
        if (d < D / 4) return D;
        return D - D / 16 + d * 3 / 8 - D / 64 + d * 3 / 256;
    }

    public double getGroundDistance(TilePosition start, TilePosition end) {
        double dist = 0.0;
        if (bwem.getMap().getArea(start) == null || bwem.getMap().getArea(end) == null) return Integer.MAX_VALUE;
        for (TilePosition cpp : bwta.getShortestPath(start, end)) {
            Position center = cpp.toPosition();
            dist += broodWarDistance(start.toPosition(), center);
            start = center.toTilePosition();
        }
        return dist + broodWarDistance(start.toPosition(), end.toPosition());
    }

    public Unit getUnitToAttack(Unit myUnit, Set<Unit> closeSim) {
        Unit chosen = null;
        Set<Unit> workers = new TreeSet<>();
        Set<Unit> combatUnits = new TreeSet<>();
        Unit worker = null;
        for (Unit u : closeSim) {
            if (u.getInitialType().isWorker()) workers.add(u);
            if (!(u instanceof Worker) && (u instanceof Attacker)) combatUnits.add(u);
        }
        if (combatUnits.isEmpty() && workers.isEmpty()) return null;
        if (!workers.isEmpty()) {
            double distB = Double.MAX_VALUE;
            for (Unit u : workers) {
                double distA = broodWarDistance(myUnit.getPosition(), u.getPosition());
                if (worker == null || distA < distB) {
                    worker = u;
                    distB = distA;
                }
            }

        }
        if (!combatUnits.isEmpty()) {
            double distB = Double.MAX_VALUE;
            for (Unit u : combatUnits) {
                double distA = broodWarDistance(myUnit.getPosition(), u.getPosition());
                if (chosen == null || distA < distB) {
                    chosen = u;
                    distB = distA;
                }
            }
        }
        if (chosen != null) return chosen;
        if (worker != null) return worker;
        return null;
    }

    // Credits to @Yegers for a better kite method
    public Position kiteAway(final Unit unit, final Set<Unit> enemies) {
        if (enemies.isEmpty()) return null;
        Position ownPosition = unit.getPosition();
        List<Pair<Double, Double>> vectors = new ArrayList<>();
        double minDistance = Double.MAX_VALUE;
        for (Unit enemy : enemies) {
            Position enemyPosition = enemy.getPosition();
            Pair<Double, Double> unitV = new Pair<>((double) (ownPosition.getX() - enemyPosition.getX()), (double) (ownPosition.getY() - enemyPosition.getY()));
            double distance = ownPosition.getDistance(enemyPosition);
            if (distance < minDistance) minDistance = distance;
            unitV.first = (1 / distance) * unitV.first;
            unitV.second = (1 / distance) * unitV.second;
            vectors.add(unitV);
        }
        minDistance = 2 * minDistance * minDistance;
        for (final Pair<Double, Double> vector : vectors) {
            vector.first *= minDistance;
            vector.second *= minDistance;
        }
        Pair<Double, Double> sumAll = Util.sumPosition(vectors);
        return Util.sumPosition(ownPosition, new Position((int) (sumAll.first / vectors.size()), (int) (sumAll.second / vectors.size())));
    }

    public void runAgents() {
        List<Agent> rem = new ArrayList<>();
        for (Agent ag : agents.values()) {
            boolean remove = ag.runAgent();
            if (remove) rem.add(ag);
        }
        for (Agent ag : rem) agents.remove(ag);
    }

    public void sendCustomMessage() {
        String name = EI.opponent.toLowerCase();
        if (name.equals("krasi0".toLowerCase())) ih.sendText("Please be nice to me!");
        else if (name.equals("hannes bredberg".toLowerCase()) || name.equals("hannesbredberg".toLowerCase())) {
            ih.sendText("Don't you dare nuke me!");
        } else if (name.equals("zercgberht")) {
            ih.sendText("Hello there!, brother");
        } else ih.sendText("BEEEEP BOOOOP!, This king salutes you, " + EI.opponent);
    }

    public String pickShipName() {
        if (shipNames.isEmpty()) return "Pepe";
        String name;
        int index = new Random().nextInt(shipNames.size());
        Iterator<String> iter = shipNames.iterator();
        do {
            name = iter.next();
            index--;
        }
        while (index >= 0);
        if (name == null) return "Pepe";
        shipNames.remove(name);
        return name;
    }

    public boolean canAfford(UnitType type) {
        return (self.minerals() >= type.mineralPrice() && self.gas() >= type.gasPrice());
    }

    public void resetInMap() {
        inMap.clear();
        List<Unit> rem = new ArrayList<>();
        for (EnemyBuilding u : enemyBuildingMemory.values()) {
            if (bw.getBWMap().isVisible(u.pos) && !u.unit.isVisible()) {
                rem.add(u.unit);
                continue;
            } else inMap.updateMap(u.unit, false);
        }
        for (Unit u : rem) enemyBuildingMemory.remove(u);
        for (Unit u : bw.getUnits(self)) {
            if (u instanceof Building && u.exists()) inMap.updateMap(u, false);
        }
    }
}