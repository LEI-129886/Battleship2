import battleship.Barge;
import battleship.Caravel;
import battleship.Carrack;
import battleship.Compass;
import battleship.Fleet;
import battleship.Frigate;
import battleship.Galleon;
import battleship.Game;
import battleship.IFleet;
import battleship.IGame;
import battleship.IMove;
import battleship.IPosition;
import battleship.IShip;
import battleship.Main;
import battleship.Move;
import battleship.Position;
import battleship.Ship;
import battleship.Tasks;
import restserver.AiShotStrategy;
import restserver.BattleshipServer;
import restserver.CallbackClient;
import restserver.Dtos;
import restserver.GameController;
import restserver.GameRegistry;
import restserver.GameSession;

class OverlyCoupledClass {
    private final Barge barge;
    private final Caravel caravel;
    private final Carrack carrack;
    private final Compass compass;
    private final Fleet fleet;
    private final Frigate frigate;
    private final Galleon galleon;
    private final Game game;
    private final IFleet fleetInterface;
    private final IGame gameInterface;
    private final IMove moveInterface;
    private final IPosition positionInterface;
    private final IShip shipInterface;
    private final Main main;
    private final Move move;
    private final Position position;
    private final Ship ship;
    private final Tasks tasks;
    private final AiShotStrategy aiShotStrategy;
    private final BattleshipServer battleshipServer;
    private final CallbackClient callbackClient;
    private final Dtos dtos;
    private final GameController gameController;
    private final GameRegistry gameRegistry;
    private final GameSession gameSession;

    OverlyCoupledClass(
            Barge barge,
            Caravel caravel,
            Carrack carrack,
            Compass compass,
            Fleet fleet,
            Frigate frigate,
            Galleon galleon,
            Game game,
            IFleet fleetInterface,
            IGame gameInterface,
            IMove moveInterface,
            IPosition positionInterface,
            IShip shipInterface,
            Main main,
            Move move,
            Position position,
            Ship ship,
            Tasks tasks,
            AiShotStrategy aiShotStrategy,
            BattleshipServer battleshipServer,
            CallbackClient callbackClient,
            Dtos dtos,
            GameController gameController,
            GameRegistry gameRegistry,
            GameSession gameSession) {
        this.barge = barge;
        this.caravel = caravel;
        this.carrack = carrack;
        this.compass = compass;
        this.fleet = fleet;
        this.frigate = frigate;
        this.galleon = galleon;
        this.game = game;
        this.fleetInterface = fleetInterface;
        this.gameInterface = gameInterface;
        this.moveInterface = moveInterface;
        this.positionInterface = positionInterface;
        this.shipInterface = shipInterface;
        this.main = main;
        this.move = move;
        this.position = position;
        this.ship = ship;
        this.tasks = tasks;
        this.aiShotStrategy = aiShotStrategy;
        this.battleshipServer = battleshipServer;
        this.callbackClient = callbackClient;
        this.dtos = dtos;
        this.gameController = gameController;
        this.gameRegistry = gameRegistry;
        this.gameSession = gameSession;
    }

}
