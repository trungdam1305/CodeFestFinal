
import io.socket.emitter.Emitter;
import jsclub.codefest.sdk.Hero;
import jsclub.codefest.sdk.algorithm.PathUtils;
import jsclub.codefest.sdk.base.Node;
import jsclub.codefest.sdk.model.GameMap;
import jsclub.codefest.sdk.model.Inventory;
import jsclub.codefest.sdk.model.npcs.Enemy;
import jsclub.codefest.sdk.model.equipments.Armor;
import jsclub.codefest.sdk.model.equipments.HealingItem;
import jsclub.codefest.sdk.model.obstacles.Obstacle;
import jsclub.codefest.sdk.model.players.Player;
import jsclub.codefest.sdk.model.weapon.Bullet;
import jsclub.codefest.sdk.model.weapon.Weapon;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static final String SERVER_URL = "https://cf25-server.jsclub.dev";
    private static final String GAME_ID = "136761";
    private static final String PLAYER_NAME = "WeakEntity";
    private static final String PLAYER_KEY = "sk-5VTDWaBiRSqa2fTy2ZExNw:yj02fPcOBJV30UkGtIdRqmHuvbmpHdrQ-JTsXLyh_QuUZEcvh1OmXjccpXyq-qPUIFMOb8de4mLjt9-S9GH8Fh2tA";
    private static int currentStep = 0;

    private static int countBo = 0;
    private static boolean hyperDodge = false;
    private static boolean hasGun = false;
    private static boolean hasMelee = false;
    private static String IDCurrentMelee= "";
    private static boolean hasThrow = false;
    private static int NumBullet = 0;
    private static boolean hasHead = false;
    private static boolean hasBody = false;
    private static String[] enemyDirection = new String[100];
    private static int[] toado = new int[100];
    private static Boolean[][][] enemyMap = new Boolean[121][121][100];
    private static int enemyMinEdge[] = new int[100];
    private static int enemyMaxEdge[] = new int[100];

    private static Player savedTarget = null;
    private static String savedID = null;
    static Node tron = null;
    static boolean goc1=true;
    static boolean goc2=false;
    static boolean goc3=false;
    static boolean goc4=false;

    private static int NumHeal = 0;
    private static String[] IDHealItem = new String[4];
    //_________________________________________________________________
    static List<Enemy> listNodeEnemySave = new ArrayList<>();
    static List<Node> listNodeEnemy = new ArrayList<>();
    static int countStep = 0;

    static boolean checkListNodeEnemy(int x, int y) {
        boolean check = false;
        for (Node node : listNodeEnemy) {
            if (node.getX() == x && node.getY() == y) {
                check = true;
                break;
            }
        }
        return check;
    }

    static void removeNodeEnemy(int x, int y) {
        for (Node node : listNodeEnemy) {
            if (node.getX() == x && node.getY() == y) {
                listNodeEnemy.remove(node);
                return;
            }
        }
    }
    //_________________________________________________________________
    static int[][] map = new int[121][121];

    private static boolean canAttack = true;
    private static int AttackcountDown = 0;

    private static boolean canHeal = false;
    private static int HealcountDown = 0;

    private static boolean canShoot = false;
    private static int ShootcountDown = 0;
    static int darksize = 0;
    static int prePriority = 0;


    public static void main(String[] args) throws IOException {
        Hero hero = new Hero(GAME_ID, PLAYER_NAME,PLAYER_KEY);

        Emitter.Listener onMapUpdate = new Emitter.Listener() {
            @Override
            public void call(Object... args) {

                try {
                    currentStep++;
                    System.out.println("********************************************************************");
                    System.out.println("Current step: "+currentStep);
                    System.out.println("List healItem : ");
                    System.out.println(IDHealItem[0]);
                    System.out.println(IDHealItem[1]);
                    System.out.println(IDHealItem[2]);
                    System.out.println(IDHealItem[3]);
                    System.out.println("End list heal");
                    System.out.println("=====================================================================");
                    if (NumBullet == 0) {
                        hasGun = false;
                    }
                    if(HealcountDown != 0){
                        HealcountDown --;
                    }
                    if(HealcountDown == 0  && IDHealItem[0] != null){
                        canHeal = true;
                    }else{
                        canHeal = false;
                    }

                    if(ShootcountDown != 0){
                        ShootcountDown--;
                    }
                    if(ShootcountDown == 0 && hasGun){
                        canShoot = true;
                    }
                    if(AttackcountDown != 0){
                        AttackcountDown --;
                    }
                    if(AttackcountDown == 0){
                        hyperDodge = false;
                        canAttack = true;
                    }
                    if (!hasMelee&&hasGun&&canShoot){
                        hyperDodge=false;
                    }
                    GameMap gameMap = hero.getGameMap();
                    gameMap.updateOnUpdateMap(args[0]);
                    Player player = gameMap.getCurrentPlayer();
                    List<Player> otherPlayers = gameMap.getOtherPlayerInfo();
                    List<Obstacle> restricedList = gameMap.getListIndestructibles();
                    restricedList.addAll(gameMap.getListTraps());
                    Node currentNode = new Node(player.getX(), player.getY());

                    List<Node> restrictedNodes = new ArrayList<>();
                    Inventory inventory = hero.getInventory();
                    List<Node> otherPlayesNode = new ArrayList<>();
                    List<Enemy> listEnemies = gameMap.getListEnemies();

                    if(player.getHealth() <= 0){
                        canHeal = false;
                        HealcountDown = 0;
                        canAttack = true;
                        AttackcountDown = 0;
                        canShoot = false;
                        ShootcountDown = 0;
                        hasGun = false;
                        hasMelee = false;
                        hasThrow = false;
                        NumBullet = 0;
                        hasHead = false;
                        hasBody = false;
                        NumHeal = 0;
                        IDHealItem[0]= null;
                        IDHealItem[1]= null;
                        IDHealItem[2]= null;
                        IDHealItem[3]= null;
                    }

                    for(Obstacle chest : gameMap.getListChests()) {
                        if(chest.getHp() != 0){
                            restrictedNodes.add(new Node(chest.getX(),chest.getY()));
                        }
                    }
                    for (Player p : otherPlayers) {
                        if (p.getHealth() > 0) {
                            otherPlayesNode.add(new Node(p.getX(), p.getY()));
                        }
                    }
                    restrictedNodes.addAll(otherPlayers);
                    countStep++;


                    //_Cho enemy vao retriced__________________________
                    if (listNodeEnemySave.isEmpty())
                        for (Enemy enemy : listEnemies) {
                            listNodeEnemySave.add(enemy);
                        }
                    else {
                        if (countStep == 12) {
                            for (int i = 0; i < listEnemies.size(); i++) {
                                if (enemyDirection[i].equals("doc")) {
                                    int xMax = Integer.MIN_VALUE;
                                    int xMin = Integer.MAX_VALUE;
                                    for (int j = 0; j < 121; j++) {
                                        if (enemyMap[j][toado[i]][i] != null && enemyMap[j][toado[i]][i]) {
                                            if (j > xMax) xMax = j;
                                            if (j < xMin) xMin = j;
                                        }
                                    }
                                    enemyMinEdge[i] = xMin;
                                    enemyMaxEdge[i] = xMax;
                                }
                            }
                            for (int i = 0; i < listEnemies.size(); i++) {
                                if (enemyDirection[i].equals("ngang")) {
                                    int yMax = Integer.MIN_VALUE;
                                    int yMin = Integer.MAX_VALUE;
                                    for (int j = 0; j < 121; j++) {
                                        if (enemyMap[toado[i]][j][i] != null && enemyMap[toado[i]][j][i]) {
                                            if (j > yMax) yMax = j;
                                            if (j < yMin) yMin = j;
                                        }
                                    }
                                    enemyMinEdge[i] = yMin;
                                    enemyMaxEdge[i] = yMax;
                                }
                            }
                            System.out.println("Gan xong");
                        }
                        if (countStep >= 12) {
                            List<Node> temp = new ArrayList<>();

                            for (int i = 0; i < gameMap.getListEnemies().size(); i++) {
                                if (enemyDirection[i].equals("ngang")) {
                                    int count = 0;
                                    for (int j = enemyMinEdge[i] - 1; j <= enemyMaxEdge[i] + 1; j++) {
                                        if (count % 2 == 1) {
                                            temp.add(new Node(toado[i], j));
                                            temp.add(new Node(toado[i] - 1, j));
                                            temp.add(new Node(toado[i] + 1, j));
                                        }
                                        count++;
                                    }
                                }
                                if (enemyDirection[i].equals("doc")) {
                                    int count = 0;
                                    for (int j = enemyMinEdge[i] - 1; j <= enemyMaxEdge[i] + 1; j++) {
                                        if (count % 2 == 1) {
                                            temp.add(new Node(j, toado[i]));
                                            temp.add(new Node(j, toado[i] - 1));
                                            temp.add(new Node(j, toado[i] + 1));
                                        }
                                        count++;
                                    }
                                }
                            }
                            restrictedNodes.addAll(temp);

                        }
                        if (countStep < 12) {
                            for (int i = 0; i < gameMap.getListEnemies().size(); i++) {
                                List<Node> temp = new ArrayList<>();
                                enemyMap[listEnemies.get(i).x][listEnemies.get(i).y][i] = true;
                                if (listEnemies.get(i).x > listNodeEnemySave.get(i).x && listEnemies.get(i).y == listNodeEnemySave.get(i).y) {
                                    enemyDirection[i] = "doc";
                                    toado[i] = listEnemies.get(i).y;
                                    for (int j = listEnemies.get(i).x - 1; j < listEnemies.get(i).x + 5; j++) {
                                        for (int k = listEnemies.get(i).y - 1; k < listEnemies.get(i).y + 2; k++) {
                                            temp.add(new Node(j, k));
                                        }
                                    }
                                }
                                if (listEnemies.get(i).x < listNodeEnemySave.get(i).x && listEnemies.get(i).y == listNodeEnemySave.get(i).y) {
                                    enemyDirection[i] = "doc";
                                    toado[i] = listEnemies.get(i).y;
                                    for (int j = listEnemies.get(i).x - 4; j < listEnemies.get(i).x + 2; j++) {
                                        for (int k = listEnemies.get(i).y - 1; k < listEnemies.get(i).y + 2; k++) {
                                            temp.add(new Node(j, k));
                                        }
                                    }
                                }
                                if (listEnemies.get(i).x == listNodeEnemySave.get(i).x && listEnemies.get(i).y > listNodeEnemySave.get(i).y) {
                                    enemyDirection[i] = "ngang";
                                    toado[i] = listEnemies.get(i).x;
                                    for (int j = listEnemies.get(i).y - 1; j < listEnemies.get(i).y + 5; j++) {
                                        for (int k = listEnemies.get(i).x - 1; k < listEnemies.get(i).x + 2; k++) {
                                            temp.add(new Node(k, j));
                                        }
                                    }
                                }
                                if (listEnemies.get(i).x == listNodeEnemySave.get(i).x && listEnemies.get(i).y < listNodeEnemySave.get(i).y) {
                                    enemyDirection[i] = "ngang";
                                    toado[i] = listEnemies.get(i).x;
                                    for (int j = listEnemies.get(i).y - 4; j < listEnemies.get(i).y + 2; j++) {
                                        for (int k = listEnemies.get(i).x - 1; k < listEnemies.get(i).x + 2; k++) {
                                            temp.add(new Node(k, j));
                                        }
                                    }
                                }
                                restrictedNodes.addAll(temp);
                                listNodeEnemySave.remove(i);
                                listNodeEnemySave.add(i, listEnemies.get(i));
                            }
                        }
                    }
                    //===================================================================================================================

                    for (Obstacle o : restricedList) {
                        if((o.getX()!= gameMap.getMapSize()/2) && (o.getY()!= gameMap.getMapSize()/2)) {
                            restrictedNodes.add(new Node(o.getX(), o.getY()));
                        }
                    }

                    List<Obstacle> listChest = gameMap.getListChests();
                    List<Node> nodes = new ArrayList<>();

                    int x = currentNode.getX();
                    int y = currentNode.getY();

                    int realSize = gameMap.getSafeZone();
                    int mapSize = gameMap.getMapSize();

                    boolean shouldHunting = false;
                    boolean shouldRunBo = false;
                    boolean shouldLoot = false;
                    boolean shouldHeal = false;
                    boolean shouldShoot = false;
                    boolean shouldCloseCombat = false;
                    boolean shouldDodge = false;
                    boolean shouldThrow = false;


                    //condition of loot
                    if(NumHeal < 4 ||
                            !hasMelee ||
                            !hasGun ||
                            !(hasHead || hasBody) ){
                        shouldLoot = true;

                    }

                    //end condition of loot

                    //ne dan
                    boolean dodge = false;
                    boolean mustDodge = false;
//                    int dodgeL = 0; //(x-1,y)
//                    int dodgeR = 0; //(x+1,y)
//                    int dodgeU = 0; //(x,y+1)
//                    int dodgeD = 0; //(x,y-1)
//
//                    List<Bullet> bullets = gameMap.getListBullets();
                    //System.out.println("Bullet ne:" + bullets);
//                    if (!bullets.isEmpty()) {
//                        for (int i = 1; i <= 4; i++) {
//                            if (gameMap.getElementByIndex(x, y + i).getType().name().equalsIgnoreCase("bullet")) {
//                                mustDodge = true;
//                                dodgeU += 2;
//                                dodgeD++;
//                            }
//                            if (gameMap.getElementByIndex(x, y - i).getType().name().equalsIgnoreCase("bullet")) {
//                                mustDodge = true;
//                                dodgeD += 2;
//                                dodgeU++;
//                            }
//                            if (gameMap.getElementByIndex(x + i, y).getType().name().equalsIgnoreCase("bullet")) {
//                                System.out.println(gameMap.getElementByIndex(x + i, y));
//                                System.out.println(x + "|" + y);
//                                mustDodge = true;
//                                dodgeR += 2;
//                                dodgeL++;
//                            }
//                            if (gameMap.getElementByIndex(x - i, y).getType().name().equalsIgnoreCase("bullet")) {
//                                mustDodge = true;
//                                dodgeL += 2;
//                                dodgeR++;
//                            }
//                        }
//                    }
//                    if (!bullets.isEmpty()) {
//                        for (int dX = x - 3; dX <= x + 3; dX++) {
//                            if (dodge) {
//                                break;
//                            }
//                            if (dX == x) {
//                                continue;
//                            }
//                            for (int dY = y - 3; dY <= y + 3; dY++) {
//                                if (dY == y) {
//                                    continue;
//                                }
//                                if (gameMap.getElementByIndex(dX, dY).getType().name().equalsIgnoreCase("bullet")) {
//                                    dodge = true;
//                                    break;
//                                }
//                            }
//                        }
//                    }
//                    if (dodge) {
//                        for (int dX = x - 3; dX <= x + 3; dX++) {
//                            if (dX == x) {
//                                continue;
//                            }
//                            for (int dY = y - 3; dY <= y + 3; dY++) {
//                                if (dY == y) {
//                                    continue;
//                                }
//                                if (gameMap.getElementByIndex(dX, dY).getType().name().equalsIgnoreCase("bullet")) {
//                                    if (dX == x - 1) {
//                                        dodgeL++;
//                                    } else if (dX == x + 1) {
//                                        dodgeR++;
//                                    }
//                                    if (dY == y + 1) {
//                                        dodgeU++;
//                                    } else if (dY == y - 1) {
//                                        dodgeD++;
//                                    }
//                                }
//                            }
//                        }
//                        if (dodgeR > 0) {
//                            restrictedNodes.add(new Node(x + 1, y));
//                        }
//                        if (dodgeL > 0) {
//                            restrictedNodes.add(new Node(x - 1, y));
//                        }
//                        if (dodgeU > 0) {
//                            restrictedNodes.add(new Node(x, y + 1));
//                        }
//                        if (dodgeD > 0) {
//                            restrictedNodes.add(new Node(x, y - 1));
//                        }
//                    }
                    //ne dan


                    //condition of hunting
                    Player targetPlayer = null;
                    Player nearPlayer = null;
                    double lowestDistance1 = Integer.MAX_VALUE;
                    double lowestDistance2 = Integer.MAX_VALUE;
                    for(Player p : otherPlayers){
                        double distanceMeAndYou = getDistanceBetweenTwoNode(currentNode,new Node(p.getX(),p.getY()));
                        if( p.getHealth() >= 0 && distanceMeAndYou < lowestDistance1 ){
                            lowestDistance1 = distanceMeAndYou;
                            targetPlayer = p;
                        }
                        if( p.getHealth() >= 0 && distanceMeAndYou < lowestDistance2){
                            lowestDistance2 = distanceMeAndYou;
                            nearPlayer = p;
                        }
                    }
//                    System.out.println("=======TargetPlayer && nearPlayer: ==========");
//                    if (targetPlayer!=null) System.out.println(targetPlayer.getPlayerName());
//                    if (nearPlayer!=null) System.out.println(nearPlayer.getPlayerName());
//                    System.out.println("================================");

                    if(!shouldLoot ){
                        if(lowestDistance2 <=15 ) {

                            System.out.println("Hunting timeeee !");
                            shouldHunting = true;

                        }else if(IDHealItem[3] != null &&
                                hasMelee &&
                                hasGun &&
                                hasHead &&
                                hasBody){
                            System.out.println("I am hunggryyyyy");
                            shouldHunting = true;

                        }else{
                            System.out.println("just loot");
                            shouldLoot = true;
                        }

                    }

                    //end condition of hunting
                    // con
                    //condition dodge
//                    if (gameMap.getElementByIndex(x, y + 3).getType().name().equalsIgnoreCase("bullet") ||
//                            gameMap.getElementByIndex(x, y - 3).getType().name().equalsIgnoreCase("bullet") ||
//                            gameMap.getElementByIndex(x + 3, y).getType().name().equalsIgnoreCase("bullet") ||
//                            gameMap.getElementByIndex(x - 3, y).getType().name().equalsIgnoreCase("bullet")) {
//                        System.out.println("run for bullet");
//                        shouldDodge = true;
//                    }
//                    for (int dX = x - 2; dX <= x + 2; dX++) {
//                        if (shouldDodge) {
//                            break;
//                        }
//                        if (dX == x) {
//                            continue;
//                        }
//                        for (int dY = y - 2; dY <= y + 2; dY++) {
//                            if (dY == y) {
//                                continue;
//                            }
//                            if (gameMap.getElementByIndex(dX, dY).getType().name().equalsIgnoreCase("bullet")) {
//                                shouldDodge = true;
//                                break;
//                            }
//                        }
//                    }
                    //end condition dodge
                    //condition of throw
                    //dieu kien throw
                    List<Player> listPlayerInRangeThrow = new ArrayList<>();
                    for (Player p : otherPlayers) {
                        if (getDistanceBetweenTwoNode(currentNode, new Node(p.getX(),p.getY())) <=10 && p.getHealth() >= 0) {
                            listPlayerInRangeThrow.add(p);
                        }
                    }
                    if(!listPlayerInRangeThrow.isEmpty() && hasThrow){
                        System.out.println("In range throw");
                        shouldThrow = true;
                    }
                    //end condition of throw

                    //condition of close combat
                    List<Player> listPlayerInRangeCloseCombat = new ArrayList<>();
                    for (Player p : otherPlayers) {
                        if (getDistanceBetweenTwoNode(currentNode,new Node(p.getX(),p.getY())) <=6 && p.getHealth() >= 0) {
                            listPlayerInRangeCloseCombat.add(p);
                            restrictedNodes.add(new Node(p.x,p.y+2));
                            restrictedNodes.add(new Node(p.x,p.y+3));
                            //restrictedNodes.add(new Node(p.x,p.y+4));
                            restrictedNodes.add(new Node(p.x,p.y-2));
                            restrictedNodes.add(new Node(p.x,p.y-3));
                            //restrictedNodes.add(new Node(p.x,p.y-4));
                            restrictedNodes.add(new Node(p.x+2,p.y));
                            restrictedNodes.add(new Node(p.x+3,p.y));
                            //restrictedNodes.add(new Node(p.x+4,p.y));
                            restrictedNodes.add(new Node(p.x-2,p.y));
                            restrictedNodes.add(new Node(p.x-3,p.y));
                            //restrictedNodes.add(new Node(p.x-4,p.y));
                        }
                        if (!hasMelee&&(currentStep<40)&&getDistanceBetweenTwoNode(currentNode,new Node(p.getX(),p.getY())) <=3 && p.getHealth() >= 0) {
                            listPlayerInRangeCloseCombat.add(p);
                        }
                        if (!hasMelee&&(currentStep>290)&&getDistanceBetweenTwoNode(currentNode,new Node(p.getX(),p.getY())) <=8 && p.getHealth() >= 0) {
                            listPlayerInRangeCloseCombat.add(p);
                        }
                    }
                    if (!listPlayerInRangeCloseCombat.isEmpty()
                    ) {
                        shouldCloseCombat = true;
                        System.out.println("close combat");
                    }
                    // end condition of close combat
                    // condition of run bo

                    if (realSize != darksize) {
                        if ((y > (darksize / 8 + 1) * 8 && y < (mapSize - (darksize / 8 + 1) * 8) && x > (darksize / 8 + 1) * 8 && x < (mapSize - (darksize / 8 + 1) * 8)) || (x > realSize + 3 && x < mapSize - realSize - 3 && y > realSize + 3 && y < mapSize - realSize - 3)) {
                            //safe
                        } else {
                            shouldRunBo = true;
                            System.out.println("run now");
                            countBo = 8;
                            canHeal = false;
                            HealcountDown = 8;
                        }
                        darksize = realSize;
                    }
                    // end condition of run bo

                    // condition of healing
                    if(player.getHealth() < 100 && canHeal && IDHealItem[0]!=null){
                        shouldHeal = true;
                    }
                    // end condition of healing
                    //conditon of shoot
                    List<Player> listPlayerInRangeShoot = new ArrayList<>();
                    for (Player p : otherPlayers) {
                        if (getDistanceBetweenTwoNode(currentNode,new Node(p.getX(),p.getY())) <=2 && p.getHealth() >= 0) {
                            listPlayerInRangeShoot.add(p);
                        }
                    }
                    if (!listPlayerInRangeShoot.isEmpty() && hasGun) {
                        shouldShoot = true;
                        System.out.println("In range shoot ");
                    }
                    //end condition of shoot




                    // take priority
                    int currentPriority = 6;
                    while (true) {
                        // chay bo
                        if(shouldRunBo){
                            currentPriority = 0;
                            break;
                        }
                        //hyperDodge
                        if (hyperDodge) {
                            currentPriority=-1;
                            break;
                        }
                        // can chien
                        if (shouldCloseCombat && canAttack) {
                            currentPriority = 1;
                            break;
                        }

                        // ne dan : ))
//                        if (shouldDodge ) {
//                            currentPriority = 2;
//                            break;
//                        }
                        // hoi mau
                        if (shouldHeal) {
                            // neu dang can chien thi ban mot phat trong thoi gian cooldown roi moi hoi mau
                            if(shouldCloseCombat && canShoot){
                                currentPriority = 5;
                                break;
                            }else if(canHeal){
                                currentPriority = 3;
                                break;
                            }

                        }
                        // nem bom
                        if (shouldThrow && !shouldShoot) {
                            currentPriority = 4;
                            break;
                        }
                        // ban
                        if (shouldShoot) {
                            currentPriority = 5;
                            break;
                        }
                        // loot do
                        if (shouldLoot ) {
                            currentPriority = 6;
                            break;
                        }

                        if(shouldHunting){
                            currentPriority = 7;
                            break;
                        }

                    }
                    //end take priority
                    //throw - priority 4
                    if (currentPriority==4) {
                        System.out.println("Vao cau lenh throw");
                        boolean deploy = false;
                        for (Player target : listPlayerInRangeThrow) {
                            if (!getThrowDirection(currentNode, new Node(target.x, target.y)).equalsIgnoreCase("planB")) {
                                hero.throwItem(getThrowDirection(currentNode, new Node(target.x, target.y)), PathUtils.distance(currentNode, new Node(target.x, target.y)));
                                hasThrow=false;
                                deploy = true;
                            }
                        }
                        if (!deploy) {
                            Node food = null;
                            float lowestHp = Float.MAX_VALUE;
                            System.out.println("--------------");
                            System.out.println("list Player in range throw");
                            for (Player p : listPlayerInRangeThrow) {
//                                System.out.println("Name: " + p.getPlayerName());
                                System.out.println("HP: " + p.getHealth());
                                if (p.getHealth() < lowestHp) {
                                    lowestHp = p.getHealth();
                                    food = p;
                                }
                            }
                            System.out.println("Khong nem duoc, tiep tuc thuc hien hanh dong truoc:");
                            currentPriority = prePriority;
                        }
                    }
                    //hyper dodge - priority -1
                    if (currentPriority == -1){
                        String path = null;
                        for (Player p : otherPlayers) {
                            if (p.getID().equalsIgnoreCase(savedID)) {
                                savedTarget=p;
                                break;
                            }
                        }
                        while (true) {
                            System.out.println("hyper lap:"+savedTarget);
//                            if (AttackcountDown==1) {
//                                if (PathUtils.getShortestPath(gameMap,restrictedNodes,currentNode,savedTarget,false)!=null)
//                                {
//                                    hero.move(PathUtils.getShortestPath(gameMap,restrictedNodes,currentNode,savedTarget,false));
//                                    break;
//                                }
//                            }
                            if (player.getHealth()<100) {
                                currentPriority=3;
                                break;
                            }
                            Node tranhgiaotranh = null;
                            int x1, y1;
                            if(x>= savedTarget.x&&y>=savedTarget.y){
                                x1=savedTarget.x+1;
                                y1=savedTarget.y+2;
                                tranhgiaotranh= new Node(x1, y1);
                                for (int i = x1; i <mapSize-realSize ; i++) {
                                    for (int j = y1; j <mapSize-realSize ; j++) {
                                        tranhgiaotranh=new Node(i, j);
                                        if (PathUtils.getShortestPath(gameMap,restrictedNodes,currentNode,tranhgiaotranh,false)!=null
                                                && !PathUtils.getShortestPath(gameMap,restrictedNodes,currentNode,tranhgiaotranh,false).isEmpty()){
                                            path=PathUtils.getShortestPath(gameMap,restrictedNodes,currentNode,tranhgiaotranh,false);
                                            break;
                                        }
                                    }
                                    if (path!=null ){
                                        break;
                                    }
                                }
                            }
                            if(x<= savedTarget.x&&y>=savedTarget.y){
                                x1=savedTarget.x-1;
                                y1=savedTarget.y+2;
                                tranhgiaotranh= new Node(x1, y1);
                                for (int i = x1; i >0 ; i--) {
                                    for (int j = y1; j <mapSize-realSize ; j++) {
                                        tranhgiaotranh=new Node(i, j);
                                        if (PathUtils.getShortestPath(gameMap,restrictedNodes,currentNode,tranhgiaotranh,false)!=null){
                                            path=PathUtils.getShortestPath(gameMap,restrictedNodes,currentNode,tranhgiaotranh,false);
                                            break;
                                        }
                                    }
                                    if (path!=null){
                                        break;
                                    }
                                }
                            }
                            if(x>= savedTarget.x&&y<=savedTarget.y){
                                x1=savedTarget.x+2;
                                y1=savedTarget.y-1;
                                tranhgiaotranh= new Node(x1, y1);
                                for (int i = x1; i <mapSize-realSize ; i++) {
                                    for (int j = y1; j >0 ; j--) {
                                        tranhgiaotranh=new Node(i, j);
                                        if (PathUtils.getShortestPath(gameMap,restrictedNodes,currentNode,tranhgiaotranh,false)!=null){
                                            path=PathUtils.getShortestPath(gameMap,restrictedNodes,currentNode,tranhgiaotranh,false);
                                            break;
                                        }
                                    }
                                    if (path!=null){
                                        break;
                                    }
                                }
                            }
                            if(x<= savedTarget.x&&y<=savedTarget.y){
                                x1=savedTarget.x-1;
                                y1=savedTarget.y-2;
                                tranhgiaotranh= new Node(x1, y1);
                                for (int i = x1; i >0 ; i--) {
                                    for (int j = y1; j >0 ; j--) {
                                        tranhgiaotranh=new Node(i, j);
                                        if (PathUtils.getShortestPath(gameMap,restrictedNodes,currentNode,tranhgiaotranh,false)!=null){
                                            path=PathUtils.getShortestPath(gameMap,restrictedNodes,currentNode,tranhgiaotranh,false);
                                            break;
                                        }
                                    }
                                    if (path!=null){
                                        break;
                                    }
                                }
                            }
                            break;
                        }
                        System.out.println("hyper dodge:"+path);
                        hero.move(path);
                    }
                    //heal - priority 3
                    if(currentPriority == 3){
                        System.out.println("heal heal heal");
                        if(shouldCloseCombat){
                            System.out.println("in combat use heal has usageTime <=1");
                            for (int i = 0; i < NumHeal; i++) {
                                if(IDHealItem[i].equalsIgnoreCase("SNACK") ||
                                        IDHealItem[i].equalsIgnoreCase("INSECTICIDE")
                                ){
                                    hero.useItem(IDHealItem[i]);
                                    for (int j = i; j < IDHealItem.length -1; j++) {
                                        IDHealItem[j] = IDHealItem[j + 1];
                                    }
                                    IDHealItem[IDHealItem.length - 1] = null;
                                    canHeal = false;
                                    HealcountDown = 2;
                                    NumHeal--;
                                    return;


                                }
                            }
                        }else{
                            System.out.println("not in combat use heal depend on currentHP");
                            boolean safePlace = true;

                            for (int i = 0; i < listEnemies.size(); i++) {
                                if (enemyDirection[i].equalsIgnoreCase("doc")) {
                                    if (toado[i] - 1 <= currentNode.y && currentNode.y <= toado[i] + 1 && enemyMinEdge[i] - 1 <= currentNode.x && currentNode.x <= 1 + enemyMaxEdge[i]) {
                                        safePlace = false;
                                        break;
                                    }
                                }
                                if (enemyDirection[i].equalsIgnoreCase("ngang")) {
                                    if (toado[i] - 1 <= currentNode.x && currentNode.x <= toado[i] + 1 && enemyMinEdge[i] - 1 <= currentNode.y && currentNode.y <= enemyMaxEdge[i] + 1) {
                                        safePlace = false;
                                        break;
                                    }
                                }
                            }

                            if(safePlace ){
                                for (int i = 0; i < NumHeal; i++) {
                                    if(IDHealItem[i].equalsIgnoreCase("LUNCH_BOX") ){

                                        hero.useItem(IDHealItem[i]);
                                        for (int j = i; j < IDHealItem.length -1; j++) {
                                            IDHealItem[j] = IDHealItem[j + 1];
                                        }
                                        IDHealItem[IDHealItem.length - 1] = null;
                                        canHeal = false;
                                        HealcountDown = 4;
                                        NumHeal--;
                                        return;


                                    }
                                }
                                for (int i = 0; i < NumHeal; i++) {
                                    if(IDHealItem[i].equalsIgnoreCase("BANDAGES") ){

                                        hero.useItem(IDHealItem[i]);

                                        for (int j = i; j < IDHealItem.length -1; j++) {
                                            IDHealItem[j] = IDHealItem[j + 1];
                                        }
                                        IDHealItem[IDHealItem.length - 1] = null;
                                        canHeal = false;
                                        HealcountDown = 2;
                                        NumHeal--;
                                        return;


                                    }
                                }
                                for (int i = 0; i < NumHeal; i++) {
                                    if(IDHealItem[i].equalsIgnoreCase("DRINK") ){

                                        hero.useItem(IDHealItem[i]);
                                        for (int j = i; j < IDHealItem.length -1; j++) {
                                            IDHealItem[j] = IDHealItem[j + 1];
                                        }
                                        IDHealItem[IDHealItem.length - 1] = null;
                                        canHeal = false;
                                        HealcountDown = 2;
                                        NumHeal--;
                                        return;


                                    }
                                }
                                for (int i = 0; i < NumHeal; i++) {
                                    if(IDHealItem[i].equalsIgnoreCase("INSECTICIDE") ){

                                        hero.useItem(IDHealItem[i]);
                                        for (int j = i; j < IDHealItem.length -1; j++) {
                                            IDHealItem[j] = IDHealItem[j + 1];
                                        }
                                        IDHealItem[IDHealItem.length - 1] = null;
                                        canHeal = false;
                                        HealcountDown = 2;
                                        NumHeal--;
                                        return;

                                    }
                                }for (int i = 0; i < NumHeal; i++) {
                                    if(IDHealItem[i].equalsIgnoreCase("SNACK") ){

                                        hero.useItem(IDHealItem[i]);
                                        for (int j = i; j < IDHealItem.length -1; j++) {
                                            IDHealItem[j] = IDHealItem[j + 1];
                                        }
                                        IDHealItem[IDHealItem.length - 1] = null;
                                        canHeal = false;
                                        HealcountDown = 2;
                                        NumHeal--;
                                        return;
                                    }
                                }
                            }
                            else  currentPriority=prePriority;

                        }
                    }

                    //run bo - priority 0
                    if (currentPriority == 0 && countBo > 0) {
                        countBo --;
                        restrictedNodes.addAll(otherPlayesNode);
                        String path = null;
                        int i = 0;
                        if (y < realSize + 5) {
                            while (path == null) {
                                path = PathUtils.getShortestPath(gameMap, restrictedNodes, currentNode, new Node(x + i, y + 8), false);
                                i++;
                            }
                        }
                        if (x < realSize + 5) {
                            while (path == null) {
                                path = PathUtils.getShortestPath(gameMap, restrictedNodes, currentNode, new Node(x + 8, y + i), false);
                                i++;
                            }
                        }
                        if (y > mapSize - realSize - 5) {
                            while (path == null) {
                                path = PathUtils.getShortestPath(gameMap, restrictedNodes, currentNode, new Node(x + i, y - 8), false);
                                i++;
                            }
                        }
                        if (x > mapSize - realSize - 5) {
                            while (path == null) {
                                path = PathUtils.getShortestPath(gameMap, restrictedNodes, currentNode, new Node(x - 8, y + i), false);
                                i++;
                            }
                        }
                        System.out.println("path chay bo: " + path);
                        hero.move(path);
                    }


                    //close combat - priority 1
                    if (currentPriority == 1) {
                        System.out.println("Vao cau lenh close combat");

                        //lay node Of TargetPlayer
                        Node nodeOfTargetPlayer = null;
                        if (listPlayerInRangeCloseCombat.size() < 2) {// danh 1 vs 1
                            savedID = listPlayerInRangeCloseCombat.get(0).getID();
                            savedTarget = listPlayerInRangeCloseCombat.get(0);
                            nodeOfTargetPlayer = new Node(listPlayerInRangeCloseCombat.get(0).getX(),listPlayerInRangeCloseCombat.get(0).getY());
                        } else {// combat nhieu nguoi
                            //danh thang thap mau nhat
                            float lowestHp = Float.MAX_VALUE;
                            System.out.println("--------------");
                            System.out.println("list Player in range ");
                            for (Player p : listPlayerInRangeCloseCombat ) {
                                System.out.println("Name: " + p.getID());
                                System.out.println("HP: "+ p.getHealth());
                                if(p.getHealth() < lowestHp){
                                    lowestHp = p.getHealth();
                                    nodeOfTargetPlayer = new Node(p.getX(),p.getY());
                                    savedID = p.getID();
                                    savedTarget = p;
                                }
                            }
                            System.out.println("--------------");
                        }
                        restrictedNodes.remove(savedTarget);

                        String direction = getCloseCombatDirection(currentNode, nodeOfTargetPlayer);
                        if (direction.equalsIgnoreCase("planB")) {
                            System.out.println("Plan B in close combat is implementing");
                            System.out.println("Path to enemy: " +PathUtils.getShortestPath(gameMap, restrictedNodes, currentNode, nodeOfTargetPlayer, false));
                            hero.move(PathUtils.getShortestPath(gameMap, restrictedNodes, currentNode, nodeOfTargetPlayer, false));
                            if(PathUtils.getShortestPath(gameMap, restrictedNodes, currentNode, nodeOfTargetPlayer, false) == null){
                                System.out.println("Tam thoi di chuyen trong 3 step toi tranh loi");
                                canAttack = false;
                                AttackcountDown = 3;
                            }
                        } else {
                            System.out.println("hero attack at: "+ currentStep);
                            System.out.println("current melee is: "+ inventory.getMelee());
                            hero.attack(direction);

                            if(hasGun && inventory.getMelee().getCooldown() >1){
                                if(inventory.getMelee().getId().equalsIgnoreCase("LIGHT_SABER")){
                                    AttackcountDown = 2;
                                }else{
                                    AttackcountDown = 2;
                                }
                                canAttack = false;
                            }
                            if(hasGun && inventory.getMelee().getCooldown() <=1){
                                System.out.println("danh 1 cai xong ban");
                                AttackcountDown = 2;
                                canAttack = false;
                            }
                            if(!hasGun && (IDCurrentMelee.equalsIgnoreCase("BROOM")
                                    ||IDCurrentMelee.equalsIgnoreCase("SANDAL") )){
                                AttackcountDown = 6;
                                hyperDodge = true;
                                canAttack = false;
                            }
                            if(!hasGun && (IDCurrentMelee.equalsIgnoreCase("LIGHT_SABER"))){
                                AttackcountDown = 8;
                                hyperDodge = true;
                                canAttack = false;
                            }


                        }

                    }
                    //dodge bullet - priority 2
//                    if(currentPriority == 2){
//                        System.out.println("ne ne");
//                        if (!gameMap.getElementByIndex(x, y + 1).getType().name().equalsIgnoreCase("road")) {
//                            dodgeU += 10;
//                        }
//                        if (!gameMap.getElementByIndex(x, y - 1).getType().name().equalsIgnoreCase("road")) {
//                            dodgeD += 10;
//                        }
//                        if (!gameMap.getElementByIndex(x + 1, y).getType().name().equalsIgnoreCase("road")) {
//                            dodgeR += 10;
//                        }
//                        if (!gameMap.getElementByIndex(x - 1, y).getType().name().equalsIgnoreCase("road")) {
//                            dodgeL += 10;
//                        }
//                        String side = "l";
//                        int min = dodgeL;
//                        if (dodgeD < min) {
//                            min = dodgeD;
//                            side = "d";
//                        }
//                        if (dodgeR < min) {
//                            min = dodgeR;
//                            side = "r";
//                        }
//                        if (dodgeU < min) {
//                            min = dodgeU;
//                            side = "u";
//                        }
//                        hero.move(side);
//                    }


                    // shoot - priority 5
                    if (currentPriority == 5) {
                        System.out.println("Vao cau lenh shoot");

                        //lay node Of TargetPlayer
                        Node nodeOfTargetPlayer = null;
                        if (listPlayerInRangeShoot.size() < 2) {// danh 1 vs 1
                            savedID = listPlayerInRangeCloseCombat.get(0).getID();
                            savedTarget = listPlayerInRangeCloseCombat.get(0);
                            nodeOfTargetPlayer = new Node(listPlayerInRangeShoot.get(0).getX(),listPlayerInRangeShoot.get(0).getY());
                        } else {// combat nhieu nguoi
                            //danh thang thap mau nhat
                            float lowestHp = Float.MAX_VALUE;
                            System.out.println("--------------");
                            System.out.println("list Player in range shoot");
                            for (Player p : listPlayerInRangeShoot ) {
                                System.out.println("Name: " + p.getID());
                                System.out.println("HP: "+ p.getHealth());
                                if(p.getHealth() < lowestHp){
                                    savedID=p.getID();
                                    savedTarget=p;
                                    lowestHp = p.getHealth();
                                    nodeOfTargetPlayer = new Node(p.getX(),p.getY());
                                }
                            }
                            System.out.println("--------------");
                        }
                        restrictedNodes.remove(savedTarget);
                        String direction = getCloseCombatDirection(currentNode, nodeOfTargetPlayer);
                        if (direction.equalsIgnoreCase("planB")) {
                            hero.move(PathUtils.getShortestPath(gameMap, restrictedNodes, currentNode, nodeOfTargetPlayer, false));
                        } else {
                            hero.shoot(direction);
                            NumBullet--;
                            System.out.println("hero shoot at: "+ currentStep);
                            ShootcountDown = 2;
                            canShoot = false;
                        }

                    }
                    // loot do - priority 6
                    if (currentPriority == 6) {

                        boolean headvip=false;
                        System.out.println("loot");
                        List<Armor> listArmor = gameMap.getListArmors();
                        List<HealingItem> listHealingItem = gameMap.getListHealingItems();
//                        List<Weapon> listWeapon = gameMap.getListWeapons();
                        // --------------------------------------------------
                        List<Weapon> listThrow = gameMap.getAllThrowable();
                        List<Weapon> listGun = gameMap.getAllGun();
                        List<Weapon> listMele = gameMap.getAllMelee();
                        boolean nearGun = false;
                        //-------------------------------------------------

                        for (Weapon gun :listGun) {
                            if(getDistanceBetweenTwoNode(currentNode,gun)<=10) {
                                nearGun=true;
                                break;
                            }
                        }
                        if (hasMelee&&!hasGun&&nearGun&&(hasBody||hasHead)) {
                            System.out.println("not gun but near, let pick it up");
                            for(Weapon gun: listGun){
                                if(PathUtils.checkInsideSafeArea(new Node(gun.getX(),gun.getY()), gameMap.getSafeZone(), gameMap.getMapSize())){
                                    nodes.add(new Node(gun.getX(), gun.getY()));
                                }
                            }
                        }
                        else {
                            if(IDHealItem[3 ] == null ){
                                System.out.println("not total healing item, just find");
                                for (HealingItem healingItem : listHealingItem) {
                                    if(PathUtils.checkInsideSafeArea(new Node(healingItem.getX(),healingItem.getY()), gameMap.getSafeZone(), gameMap.getMapSize()))
                                        nodes.add(new Node(healingItem.getX(), healingItem.getY()));
                                }
                            }

//
                            if(!hasHead){
                                for(Armor head: listArmor){
                                    if(head.getId().equals("POT") || head.getId().equals("HELMET")){
                                        nodes.add(new Node(head.getX(), head.getY()));
                                    }
                                }
                            }
                            if(!hasBody){
                                for(Armor body: listArmor){
                                    if(body.getId().equals("VEST")){
                                        nodes.add(new Node(body.getX(), body.getY()));
                                    }

                                }
                            }

                            if(!hasGun){
                                System.out.println("not gun, let find one");
                                for(Weapon gun: listGun){
                                    if(PathUtils.checkInsideSafeArea(new Node(gun.getX(),gun.getY()), gameMap.getSafeZone(), gameMap.getMapSize()
                                    )){
                                        nodes.add(new Node(gun.getX(), gun.getY()));
                                    }
                                }
                            }
                            if(!hasMelee){
                                System.out.println("not melee, let find one");
                                for(Weapon melee: listMele){
                                    if(PathUtils.checkInsideSafeArea(new Node(melee.getX(),melee.getY()), gameMap.getSafeZone(), gameMap.getMapSize()
                                    )){
                                        nodes.add(new Node(melee.getX(), melee.getY()));
                                    }
                                }
                            }
                            if(!hasThrow){
                                System.out.println("not throwable, let find one");
                                for(Weapon throwable: listThrow){
                                    if(PathUtils.checkInsideSafeArea(new Node(throwable.getX(),throwable.getY()), gameMap.getSafeZone(), gameMap.getMapSize()
                                    )){
                                        nodes.add(new Node(throwable.getX(), throwable.getY()));
                                    }
                                }
                            }
                            if(!hasMelee || !hasThrow || !hasHead || !hasBody || IDHealItem[3]== null){
                                for (int i = 1; i < gameMap.getMapSize() ; i++) {
                                    for (Obstacle chest : listChest) {
                                        if(PathUtils.checkInsideSafeArea(chest, gameMap.getSafeZone(), gameMap.getMapSize())){
                                            int chestX = chest.getX();
                                            int chestY = chest.getY();
                                            if ((chestX >= x - 10 * i && chestX <= x) && (chestY >= y - 10 * i && chestY <= y + 10 * i)) {

                                                if (chestX == x && chestY > y) {
                                                    nodes.add(new Node(chestX, chestY - 1));
                                                } else if (chestX == x && chestY < y) {
                                                    nodes.add(new Node(chestX, chestY + 1));
                                                } else {
                                                    nodes.add(new Node(chestX + 1, chestY));
                                                }
                                            }
                                            if ((chestX > x && chestX <= x + 10 * i) && (chestY >= y - 10 * i && chestY <= y + 10 * i)) {
                                                nodes.add(new Node(chestX - 1, chestY));
                                            }
                                        }


                                    }
                                    if (!nodes.isEmpty()) {
                                        break;
                                    }
                                }
                            }

//                            for (Obstacle chest : listChest) {
//                                int chestX = chest.getX();
//                                int chestY = chest.getY();
//                                if (chest.getHp() != 0) {
//                                    if (chestX == x && chestY > y) {
//                                        listNodeChest.add(new Node(chestX, chestY - 1));
//                                    } else if (chestX == x && chestY < y) {
//                                        listNodeChest.add(new Node(chestX, chestY + 1));
//                                    } else {
//                                        listNodeChest.add(new Node(chestX + 1, chestY));
//                                    }
//                                    if ((chestX > x  &&  chestY <= y )) {
//                                        listNodeChest.add(new Node(chestX - 1, chestY));
//                                    }
////                                    nodes.add(new Node(chestX, chestY ));
//
//                                }
//
//
//                            }
                        }
                        String path = null;
                        System.out.println("check nodes:");
                        for (Node node : nodes) {
                            System.out.println("Node"+node.x+","+node.y);
                            if (PathUtils.getShortestPath(gameMap,restrictedNodes,currentNode,node,false)!=null) {
                                path = PathUtils.getShortestPath(gameMap,restrictedNodes,currentNode,node,false);
                                break;
                            }
                        }
                        System.out.println("_________________");

                        if (path==null)  {
                            System.out.println("Chay tron");
                            if(targetPlayer!=null&& hasMelee) {
                                System.out.println("cao");
                                restrictedNodes.remove(targetPlayer);
                                hero.move(PathUtils.getShortestPath(gameMap, restrictedNodes, currentNode, targetPlayer, false));
                                prePriority=currentPriority;
                                return;
                            }
                            else {
                                for (Player p : otherPlayers) {
                                    restrictedNodes.add(new Node(p.getX()+1, p.getY()));
                                    restrictedNodes.add(new Node(p.getX()-1, p.getY()));
                                    restrictedNodes.add(new Node(p.getX(), p.getY()+1));
                                    restrictedNodes.add(new Node(p.getX(), p.getY()-1));
                                }
                                List<Node> temp = new ArrayList<>();
                                for (int i = 0; i < gameMap.getListEnemies().size(); i++) {
                                    if (enemyDirection[i].equals("ngang")) {
                                        for (int j = enemyMinEdge[i] - 1; j <= enemyMaxEdge[i] + 1; j++) {
                                            temp.add(new Node(toado[i], j));
                                            temp.add(new Node(toado[i] - 1, j));
                                            temp.add(new Node(toado[i] + 1, j));

                                        }
                                    }
                                    if (enemyDirection[i].equals("doc")) {
                                        int count = 0;
                                        for (int j = enemyMinEdge[i] - 1; j <= enemyMaxEdge[i] + 1; j++) {
                                            temp.add(new Node(j, toado[i]));
                                            temp.add(new Node(j, toado[i] - 1));
                                            temp.add(new Node(j, toado[i] + 1));

                                        }
                                    }
                                }
                                restrictedNodes.addAll(temp);
                                String pathTron=null;
                                Node place =null;
                                if(goc1) {
                                    //di den goc duoi ben trai
                                    for (int i = realSize+1; i < gameMap.getMapSize()/2 ; i++) {
                                        for (int j = realSize+1; j < gameMap.getMapSize()/2 ; j++) {
                                            if (PathUtils.getShortestPath(gameMap,restrictedNodes,currentNode,new Node(i,j),false)!=null){
                                                pathTron=PathUtils.getShortestPath(gameMap,restrictedNodes,currentNode,new Node(i,j),false);
                                                place = new Node(i,j);
                                                break;
                                            }
                                        }
                                        if (pathTron!=null) break;
                                    }
                                    if (pathTron==null || pathTron.isEmpty()) {
                                        goc1=false;goc2=true;
                                    }
                                    System.out.println("goc1:"+ place.x+ ", "+place.y);

                                }
                                if(goc2) {
                                    //di den goc duoi ben phai
                                    for (int i = realSize+1; i < gameMap.getMapSize()/2 ; i++) {
                                        for (int j = gameMap.getMapSize()-realSize-1; j > gameMap.getMapSize()/2 ; j--) {
                                            if (PathUtils.getShortestPath(gameMap,restrictedNodes,currentNode,new Node(j,i),false)!=null){
                                                pathTron=PathUtils.getShortestPath(gameMap,restrictedNodes,currentNode,new Node(j,i),false);
                                                place = new Node(j,i);
                                                break;
                                            }
                                        }
                                        if (pathTron!=null) break;

                                    }
                                    if (pathTron==null || pathTron.isEmpty()) {
                                        goc2=false;goc3=true;
                                    }
                                    System.out.println("goc2:"+ place.x+ ", "+place.y);

                                }
                                if(goc3) {
                                    //goc tren ben phai
                                    for (int i =gameMap.getMapSize()-realSize; i >=  gameMap.getMapSize()/2  ; i--) {
                                        for (int j = gameMap.getMapSize()-realSize; j >=  gameMap.getMapSize()/2  ; j--) {
                                            if (PathUtils.getShortestPath(gameMap,restrictedNodes,currentNode,new Node(i,j),false)!=null){
                                                pathTron=PathUtils.getShortestPath(gameMap,restrictedNodes,currentNode,new Node(i,j),false);
                                                place = new Node(i,j);
                                                break;
                                            }
                                        }
                                        if (pathTron!=null) break;
                                    }
                                    if (pathTron==null || pathTron.isEmpty()) {
                                        goc3=false;goc4=true;
                                    }
                                    System.out.println("goc3:"+ place.x+ ", "+place.y);
                                }
                                if(goc4) {
                                    //goc tren ben trai
                                    for (int i = realSize+1; i < gameMap.getMapSize()/2 ; i++) {
                                        for (int j = gameMap.getMapSize()-realSize; j >=  gameMap.getMapSize()/2  ; j--) {
                                            if (PathUtils.getShortestPath(gameMap,restrictedNodes,currentNode,new Node(i,j),false)!=null){
                                                pathTron=PathUtils.getShortestPath(gameMap,restrictedNodes,currentNode,new Node(i,j),false);
                                                place = new Node(i,j);
                                                break;
                                            }
                                        }
                                        if (pathTron!=null) break;
                                    }
                                    if (pathTron==null || pathTron.isEmpty()){
                                        goc4=false;goc1=true;
                                    }
                                    System.out.println("goc4:"+ place.x+ ", "+place.y);
                                }
                                System.out.println("tron:"+pathTron);
                                if (pathTron!=null){
                                    hero.move(pathTron);
                                    prePriority=currentPriority;
                                    return;
                                }                            }
                        }
                        path = "99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999";
                        Node currentNodeTarget = null;
                        for (Node node : nodes) {
                            String path1 = PathUtils.getShortestPath(gameMap, restrictedNodes, currentNode, node, false);
                            if (path1 != null && path.length() >= path1.length()) {
                                path = path1;
                                currentNodeTarget = node;
                            }
                        }
                        if (path!=null&&!path.isEmpty()) {
                            System.out.println("path: " + path);
                            if(path.equalsIgnoreCase("99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999")){
                                System.out.println("khong tim thay duong, chay vao trung tam");
                                String pathCenter = PathUtils.getShortestPath(gameMap,restrictedNodes,currentNode,
                                        new Node(gameMap.getMapSize()/2, gameMap.getMapSize()/2),false);
                                System.out.println("node trung tam: "+ pathCenter );
                                if(pathCenter!= null){
                                    hero.move(pathCenter.charAt(0)+"");
                                }else{
                                    System.out.println("Node trung tam cung null thi chiu");
                                }
                            }else{
                                hero.move(path.charAt(0)+"");

                            }
                        } else {

                            if (gameMap.getElementByIndex(x - 1, y).getId().equalsIgnoreCase("chest")) {
                                hero.attack("l");
                                System.out.println("attack l");
                            }
                            if (gameMap.getElementByIndex(x + 1, y).getId().equalsIgnoreCase("chest")) {
                                hero.attack("r");
                                System.out.println("attack r");
                            }
                            if (gameMap.getElementByIndex(x, y + 1).getId().equalsIgnoreCase("chest")) {
                                hero.attack("u");
                                System.out.println("attack u");
                            }
                            if (gameMap.getElementByIndex(x, y - 1).getId().equalsIgnoreCase("chest")) {
                                hero.attack("d");
                                System.out.println("attack d");
                            }

//                                if(gameMap.getElementByIndex(currentNodeTarget.getX(), currentNodeTarget.getY()).getId().equals("CHEST") ){
//                                    String direction = getAttackChessDirection(currentNode,currentNodeTarget);
//                                    if(direction.equalsIgnoreCase("planB")){
//                                        hero.move(PathUtils.getShortestPath(gameMap, restrictedNodes, currentNode, currentNodeTarget, false));
//                                    }
//                                    hero.attack(direction);
//                                }


//-------------------------------------------------------------------------------------------------------------------------

                            if(gameMap.getElementByIndex(x,y).getId().equalsIgnoreCase("WATER_GUN")
                                    || gameMap.getElementByIndex(x,y).getId().equalsIgnoreCase("LEGO_GUN")){
                                System.out.println("gun bullet 5");
                                if (!hasGun) {
                                    hero.pickupItem();
                                    hasGun = true;
                                    canShoot = true;
                                    NumBullet = 5;
                                }
                            }
                            //-------------------------------------
                            if(gameMap.getElementByIndex(x,y).getId().equalsIgnoreCase("RUBBER_GUN")
                                    || gameMap.getElementByIndex(x,y).getId().equalsIgnoreCase("BADMINTON")){
                                System.out.println("gun bullet 10");
                                if (!hasGun) {
                                    hero.pickupItem();
                                    hasGun = true;
                                    canShoot = true;
                                    NumBullet = 10;
                                }
                            }
                            //-------------------------------------
                            if(gameMap.getElementByIndex(x,y).getType().name().equalsIgnoreCase("throwable")){
                                System.out.println("throwable");
                                if(!hasThrow){
                                    hero.pickupItem();
                                    hasThrow = true;
                                }
                            }
                            if (gameMap.getElementByIndex(x, y).getType().name().equalsIgnoreCase("melee")) {

                                System.out.println("mel");
                                if (!hasMelee) {
                                    hero.pickupItem();
                                    if(gameMap.getElementByIndex(x, y).getId().equalsIgnoreCase("BROOM")){
                                        IDCurrentMelee = "BROOM";
                                    }
                                    if(gameMap.getElementByIndex(x, y).getId().equalsIgnoreCase("SANDAL")){
                                        IDCurrentMelee = "SANDAL";
                                    }
                                    if(gameMap.getElementByIndex(x, y).getId().equalsIgnoreCase("LIGHT_SABER")){
                                        IDCurrentMelee = "LIGHT_SABER";
                                    }
                                    hasMelee = true;
                                }
                            }
                            if (gameMap.getElementByIndex(x, y).getType().name().equalsIgnoreCase("armor")) {
                                System.out.println("head");
                                if (!hasBody && gameMap.getElementByIndex(x,y).getId().equalsIgnoreCase("vest")){
                                    hero.pickupItem();
                                    hasBody = true;

                                }

                                if (!hasHead && (gameMap.getElementByIndex(x, y).getId().equalsIgnoreCase("pot")||gameMap.getElementByIndex(x, y).getId().equalsIgnoreCase("helmet"))) {
                                    hero.pickupItem();
                                    hasHead = true;
                                } else {
//                                    if (gameMap.getElementByIndex(x, y).getId().equalsIgnoreCase("pot")) {
//                                        for (Armor armor : inventory.getListArmor()) {
//                                            if (armor.getId().equalsIgnoreCase("helmet") || armor.getId().equalsIgnoreCase("pot")) {
//                                                hero.revokeItem(armor.getId());
//                                            }
//                                        }
//                                    }
//                                    if (gameMap.getElementByIndex(x, y).getId().equalsIgnoreCase("helmet")) {
//                                        for (Armor armor : inventory.getListArmor()) {
//                                            if (armor.getId().equalsIgnoreCase("helmet")) {
//                                                hero.revokeItem(armor.getId());
//                                            }
//                                        }
//                                    }
                                }
                            }

                            if (gameMap.getElementByIndex(x, y).getType().name().equalsIgnoreCase("HEALING_ITEM")) {
                                System.out.println("heal Item");


                                if (IDHealItem[3]== null ) {
                                    if(gameMap.getElementByIndex(x, y).getId().equalsIgnoreCase("INSECTICIDE")){
                                        hero.pickupItem();
                                        IDHealItem[NumHeal] = "INSECTICIDE";
                                        NumHeal++;
                                        canHeal = true;
                                        return;
                                    }
                                    if(gameMap.getElementByIndex(x, y).getId().equalsIgnoreCase("DRINK")){
                                        hero.pickupItem();
                                        IDHealItem[NumHeal] = "DRINK";
                                        NumHeal++;
                                        canHeal = true;
                                        return;
                                    }
                                    if(gameMap.getElementByIndex(x, y).getId().equalsIgnoreCase("BANDAGES")){
                                        hero.pickupItem();
                                        IDHealItem[NumHeal] = "BANDAGES";
                                        NumHeal++;
                                        canHeal = true;
                                        return;
                                    }
                                    if(gameMap.getElementByIndex(x, y).getId().equalsIgnoreCase("LUNCH_BOX")){
                                        hero.pickupItem();
                                        IDHealItem[NumHeal] = "LUNCH_BOX";
                                        NumHeal++;
                                        canHeal = true;
                                        return;
                                    }
                                    if(gameMap.getElementByIndex(x, y).getId().equalsIgnoreCase("SNACK")){
                                        hero.pickupItem();
                                        IDHealItem[NumHeal] = "SNACK";
                                        NumHeal++;
                                        canHeal = true;
                                        return;
                                    }

                                }

                            }
                        }
                    }
                    // danh nhau - priority == 7
                    if(currentPriority == 7 && nearPlayer != null){
                        System.out.println("Vao cau lenh Hunting");

                        if(targetPlayer!=null){
                            restrictedNodes.remove(targetPlayer);
                            System.out.println("Player target is: " + targetPlayer.getID());
                            hero.move(PathUtils.getShortestPath(gameMap,restrictedNodes,currentNode,new Node(targetPlayer.getX(), targetPlayer.getY()),false));
                            System.out.println("the path is: " + PathUtils.getShortestPath(gameMap,restrictedNodes,currentNode,new Node(targetPlayer.getX(), targetPlayer.getY()),false));
                        }
                        if(nearPlayer != null) {
                            restrictedNodes.remove(nearPlayer);
                            System.out.println("Player target is: " + nearPlayer.getID());
                            hero.move(PathUtils.getShortestPath(gameMap,restrictedNodes,currentNode,new Node(nearPlayer.getX(), nearPlayer.getY()),false));
                            System.out.println("the path is: " + PathUtils.getShortestPath(gameMap,restrictedNodes,currentNode,new Node(nearPlayer.getX(), nearPlayer.getY()),false));
                        }
                    }
                    if (currentPriority!=-1) prePriority=currentPriority;

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        hero.setOnMapUpdate(onMapUpdate);
        hero.start(SERVER_URL);
    }
    private static String getCloseCombatDirection(Node playerNode, Node enemyNode) {
        int x = playerNode.getX();
        int y = playerNode.getY();
        int x1 = enemyNode.getX();
        int y1 = enemyNode.getY();
        if (Math.abs(x - x1) > 1 || Math.abs(y - y1) > 1) {
            return "planB";
        }
        if (x - x1 == 0 && y - y1 < 0) {
            return "u";
        }
        if (x - x1 == 0 && y - y1 > 0) {
            return "d";
        }
        if (x - x1 > 0 && y - y1 == 0) {
            return "l";
        }
        if (x - x1 < 0 && y - y1 == 0) {
            return "r";
        }
        return "planB";
    }
    private static String getAttackChessDirection(Node playerNode, Node chestNode) {
        int x = playerNode.getX();
        int y = playerNode.getY();
        int x1 = chestNode.getX();
        int y1 = chestNode.getY();
        if (Math.abs(x - x1) > 1 || Math.abs(y - y1) > 1) {
            return "planB";
        }
        if (x - x1 == 0 && y - y1 < 0) {
            return "u";
        }
        if (x - x1 == 0 && y - y1 > 0) {
            return "d";
        }
        if (x - x1 > 0 && y - y1 == 0) {
            return "l";
        }
        if (x - x1 < 0 && y - y1 == 0) {
            return "r";
        }
        return "planB";
    }

    private static Double getDistanceBetweenTwoNode(Node node1, Node node2) {
        return Math.sqrt(Math.pow(node1.getX() - node2.getX(), 2) +
                Math.pow(node1.getY() - node2.getY(), 2));
    }

    private static String getThrowDirection(Node playerNode, Node enemyNode) {
        int x = playerNode.getX();
        int y = playerNode.getY();
        int x1 = enemyNode.getX();
        int y1 = enemyNode.getY();
        if (Math.abs(x - x1) >= 10 || Math.abs(y - y1) >= 10) {
            return "planB";
        }
        if (Math.abs(x - x1) <= 1 && (-5 > y - y1 && y - y1 >= -10)) {
            return "u";
        }
        if (Math.abs(x - x1) <= 1 && (10 >= y - y1 && y - y1 > 5)) {
            return "d";
        }
        if (Math.abs(y - y1) <= 1 && (10 >= x - x1 && x - x1 > 5)) {
            return "l";
        }
        if (Math.abs(y - y1) <= 1 && (-5 > x - x1 && x - x1 >= -10)) {
            return "r";
        }
        return "planB";
    }
    public void getDamageReduction(Player p ){

    }
}
