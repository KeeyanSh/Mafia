package com.company;

import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.io.*;
import java.net.SocketException;

/**
 * The type God.
 */
public class God {
    /**
     * The constant allMembers in game.
     */
    public static ArrayList<ThreadServer> allMembers = new ArrayList<>();
    /**
     * The constant playerNum.
     */
    public static int playerNum,
    /**
     * The number of ready players.
     */
    readyNum = 0;

    /**
     * The constant playersByRole holding characters of each player.
     */
    public static HashMap<Character, ThreadServer> playersByRole = new HashMap<>();
    private static ArrayList<ThreadServer> mafias = new ArrayList<>();
    private static ArrayList<ThreadServer> citizens = new ArrayList<>();
    /**
     * The constant observers those who are dead but have not left the game.
     */
    public static ArrayList<ThreadServer> observers = new ArrayList<>();
    /**
     * The constant dead players.
     */
    public static ArrayList<ThreadServer> dead = new ArrayList<>();
    /**
     * The constant saved players.
     */
    public static ArrayList<ThreadServer> saved = new ArrayList<>();
    /**
     * The constant votes.
     */
    public static HashMap<ThreadServer, ArrayList<String>> votes = new HashMap<>();
    /**
     * the player made quiet by the psychologist
     */
    private static ThreadServer quiet;

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.print("Enter the number of players : ");
            int num = sc.nextInt();
            if (num < 1)
                System.out.println("\t\t> Minimum number is 5; try again... <");
            else {
                playerNum = num;
                break;
            }
        }
        sc.close();

        try (ServerSocket serversocket = new ServerSocket(5000)) {
            System.out.println("Waiting for players to join...");
            int count = 0;
            while (count < playerNum) {
                Socket socket = serversocket.accept();
                count++;
                ThreadServer threadServer = new ThreadServer(socket);
                threadServer.start();
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        while (allMembers.size() != playerNum) System.out.print("");
        ArrayList<ThreadServer> cli = new ArrayList<>(allMembers);
        Collections.copy(cli, allMembers);
        generateCharacters();
        allMembers = cli;

        System.out.println("*Roles :");
        for (Character c : playersByRole.keySet())
            System.out.println(c + " --> " + playersByRole.get(c).getMemberName());
        System.out.println("\nmafias :");
        for (ThreadServer t : mafias)
            System.out.println(t.getMemberName());
        System.out.println("\ncitizens :");

        for (ThreadServer t : citizens)
            System.out.println(t.getMemberName());

        gameProcess();
    }

    /**
     * Game process loop.
     */
    public static void gameProcess() {
        while (!isFinished()) {
            int awareOrNot = 0;

            //Night starts
            ThreadServer.sendToGroup("\n\n\u001B[40m# Night Starts #\u001B[0m", allMembers);
            mafia();
            cityDr();
            detective();
            if (sniper() == -1 && saved.contains(playersByRole.get(Character.SNIPER)))
                saved.remove(playersByRole.get(Character.SNIPER));
            psychologist();
            awareOrNot = dieHard();
            ArrayList<Character> deadChars = checkDead();
            if (awareOrNot == 1)
                ThreadServer.sendToGroup("\n*Last night we lost these characters :\n" + deadChars, allMembers);

            //Day starts
            ThreadServer.sendToGroup("\n\n\u001B[47m\u001B[30m# Day Starts #\u001B[0m", allMembers);
            showAlive();
            if (quiet != null)
                ThreadServer.sendToGroup("\t\t* " + quiet.getMemberName() + "\t* is quiet today...", allMembers);
            letChat(allMembers, "Day Discussion", 300);
            quiet = null;
            letVote(allMembers, allMembers, "X Who are you Suspicious of ? X");
            mayor();
        }
    }

    /**
     * Mafias action.
     */
    public static void mafia() {
        if (playersByRole.get(Character.GODFATHER) == null)
            changeGodFather();
        mafiasIntroduction();
        ThreadServer.sendToGroup("Mafias Discussing", allMembers);
        letChat(mafias, "Mafias Discussion", 180);
        ArrayList<ThreadServer> mafiaTmp = new ArrayList<>(mafias);
        ThreadServer godFa = playersByRole.get(Character.GODFATHER);
        mafiaTmp.remove(godFa);
        godFa.sendToClient("Mafias voting...");
        letVote(mafiaTmp, citizens, "X Who must die ? X");
        mafiaTmp.clear();
        mafiaTmp.add(godFa);
        voteResults(mafiaTmp);
        ThreadServer.sendToGroup("waiting for GODFATHER to play role...", allMembers);
        int chosen = playersByRole.get(Character.GODFATHER).act(citizens, "");
        if (chosen == -1)
            return;
        dead.add(citizens.get(chosen));
        ThreadServer.sendToGroup("GODFATHER chose " + citizens.get(chosen).getMemberName(), mafias);
        drLecter();

    }

    /**
     * Mafias introduction to each other.
     */
    public static void mafiasIntroduction() {
        ThreadServer.sendToGroup("\n\t\txX Mafias Group Xx", mafias);
        ThreadServer.sendToGroup("\u001B[47m\u001B[30m" + playersByRole.get(Character.GODFATHER).getMemberName()
                + " --> GODFATHER", mafias);
        if (playersByRole.get(Character.DRLECTER) != null)
            ThreadServer.sendToGroup("\u001B[47m\u001B[30m" + playersByRole.get(Character.DRLECTER).getMemberName()
                    + " --> DR.LECTER\n", mafias);

        for (ThreadServer mafia : mafias)
            if (!(mafia.equals(playersByRole.get(Character.DRLECTER))
                    || mafia.equals(playersByRole.get(Character.GODFATHER))))
                ThreadServer.sendToGroup("\u001B[47m\u001B[30m" + mafia.getMemberName()
                        + " --> MAFIA", mafias);
        ThreadServer.sendToGroup("\u001B[0m", mafias);
    }

    /**
     * Changes god father if dead.
     */
    public static void changeGodFather() {
        Random rand = new Random();
        ThreadServer newGodFather = mafias.get(rand.nextInt(mafias.size()));
        if (newGodFather.getCharacter().equals(Character.DRLECTER))
            playersByRole.remove(newGodFather.getCharacter());
        playersByRole.put(Character.GODFATHER, newGodFather);
        newGodFather.sendToClient("role");
        newGodFather.sendToClient(Character.GODFATHER.toString());
        newGodFather.setCharacter(Character.GODFATHER);
        ThreadServer.sendToGroup("\n*New GODFATHER is : \u001B[47m\u001B[30m"
                + playersByRole.get(Character.GODFATHER).getMemberName(), mafias);

    }

    /**
     * Dr lecter's action.
     */
    public static void drLecter() {
        if (playersByRole.get(Character.DRLECTER) == null)
            return;
        ThreadServer.sendToGroup("waiting for DR.LECTER to play role...", allMembers);
        ThreadServer drLecter = playersByRole.get(Character.DRLECTER);
        DrLecter drPlayer = (DrLecter) drLecter.getPlayer();
        if (drPlayer.getSaves() == 0) {
            drLecter.sendToClient("\t\tYou have used all your saves...");
            return;
        }
        ArrayList<ThreadServer> mafiaTmp = new ArrayList<>(mafias);
        if (drPlayer.getSaveYourself() == 0)
            mafiaTmp.remove(playersByRole.get(Character.DRLECTER));

        String alert = "(Total saves = " + drPlayer.getSaves() + " & You can save your self " + drPlayer.getSaveYourself() + " more times.)";
        if (drPlayer.getSaves() == 0) {
            drPlayer.saveYourself();
            return;
        }
        int chosen = playersByRole.get(Character.DRLECTER).act(mafiaTmp, alert);
        if (chosen == -1)
            return;
        if (mafiaTmp.get(chosen).equals(drLecter))
            drPlayer.saveYourself();
        else {
            drPlayer.save();
            saved.add(mafias.get(chosen));
        }
    }

    /**
     * City dr's action.
     */
    public static void cityDr() {
        if (playersByRole.get(Character.CITYDOCTOR) == null)
            return;
        ThreadServer.sendToGroup("waiting for CITY.DR to play role...", allMembers);
        ThreadServer dr = playersByRole.get(Character.CITYDOCTOR);
        CityDoctor drPlayer = (CityDoctor) dr.getPlayer();
        if (drPlayer.getSaves() == 0) {
            dr.sendToClient("\t\tYou have used all your saves...");
            return;
        }
        ArrayList<ThreadServer> tmp = new ArrayList<>(allMembers);
        if (drPlayer.getSaveYourself() == 0)
            tmp.remove(playersByRole.get(Character.CITYDOCTOR));
        String alert = "(Total saves = " + drPlayer.getSaves() + " & You can save your self " + drPlayer.getSaveYourself() + " more times.)";

        int chosen = playersByRole.get(Character.CITYDOCTOR).act(tmp, alert);
        if (chosen == -1)
            return;
        if (tmp.get(chosen).equals(dr))
            drPlayer.saveYourself();
        else {
            drPlayer.save();
            if (allMembers.get(chosen).getPlayer() instanceof Citizen)
                saved.add(allMembers.get(chosen));
        }
    }

    /**
     * Mayor's action.
     */
    public static void mayor() {
        ThreadServer mayor = playersByRole.get(Character.MAYOR);
        if (mayor == null)
            return;
        if (playersByRole.get(Character.CITYDOCTOR) == null)
            mayor.sendToClient("\u001B[30mCITY.DR is dead\u001B[0m");
        else
            mayor.sendToClient("\u001B[47m\u001B[30mCITY.DR is "
                    + playersByRole.get(Character.CITYDOCTOR).getMemberName() + "\u001B[0m");


        ThreadServer.sendToGroup("waiting for MAYOR to accept/decline the vote results...", allMembers);
        int choice = mayor.act(null, null);
        if (choice == 1) {
            ThreadServer.sendToGroup("The MAYOR accepted the results...", allMembers);
            kick();
        } else
            ThreadServer.sendToGroup("The MAYOR declined the results...", allMembers);

    }

    /**
     * Die hard's action.
     *
     * @return the int
     */
    public static int dieHard() {
        int awareOrNot = -1;
        if (playersByRole.get(Character.DIEHARD) == null)
            return awareOrNot;
        ThreadServer.sendToGroup("waiting for DIEHARD to play role...", allMembers);
        ThreadServer dieHard = playersByRole.get(Character.DIEHARD);
        DieHard role = (DieHard) dieHard.getPlayer();
        if (role.getBeAware() == 0 && role.getLives() == 0) {
            dieHard.sendToClient("\t\tYou have used all your chances to act...");
            return awareOrNot;
        }
        String alert = "(You have " + role.getLives() + " lives left.)\n(You can know dead characters "
                + role.getBeAware() + " times more.)";
        dieHard.sendToClient("act");
        int chosen = dieHard.readChoice(alert);
        if (chosen == 1 && role.getBeAware() > 0) {
            awareOrNot = 1;
            role.beAware();
        }
        if (dead.contains(dieHard) && !(saved.contains(dieHard)) && role.getLives() > 0) {
            role.beShot();
            dead.remove(dieHard);
        }

        return awareOrNot;
    }

    /**
     * Psychologist's action.
     */
    public static void psychologist() {
        if (playersByRole.get(Character.PSYCHOLOGIST) == null)
            return;
        ThreadServer.sendToGroup("waiting for PSYCHOLOGIST to play role...", allMembers);
        ArrayList<ThreadServer> allTmp = new ArrayList<>(allMembers);
        allTmp.remove(playersByRole.get(Character.PSYCHOLOGIST));
        int chosen = playersByRole.get(Character.PSYCHOLOGIST).act(allTmp, "");
        if (chosen == -1)
            return;
        quiet = allTmp.get(chosen);
    }

    /**
     * Sniper's action.
     *
     * @return the int
     */
    public static int sniper() {
        if (playersByRole.get(Character.SNIPER) == null)
            return 0;
        ThreadServer.sendToGroup("waiting for Sniper to play role...", allMembers);
        ArrayList<ThreadServer> allTmp = new ArrayList<>(allMembers);
        ThreadServer sniper = playersByRole.get(Character.SNIPER);
        Sniper role = (Sniper) sniper.getPlayer();
        if (role.getShots() == 0) {
            sniper.sendToClient("\t\tYou have used all your shots...");
            return 0;
        }
        allTmp.remove(sniper);
        String alert = "(Left shots = " + role.getShots() + ")";
        int chosen = sniper.act(allTmp, alert);
        if (chosen == -1)
            return 0;
        role.shot();
        if (allTmp.get(chosen).getPlayer() instanceof Mafia) {
            dead.add(allTmp.get(chosen));
            return 0;
        } else {
            dead.add(playersByRole.get(Character.SNIPER));
            return -1;
        }
    }

    /**
     * Detective's action.
     */
    public static void detective() {
        if (playersByRole.get(Character.DETECTIVE) == null)
            return;
        ThreadServer.sendToGroup("waiting for detective to play role...", allMembers);
        ArrayList<ThreadServer> allTmp = new ArrayList<>(allMembers);
        allTmp.remove(playersByRole.get(Character.DETECTIVE));
        int chosen = playersByRole.get(Character.DETECTIVE).act(allTmp, "");
        if (chosen == -1)
            return;
        Player role = allTmp.get(chosen).getPlayer();
        String state = "Citizen";
        if (role instanceof Mafia)
            if (!(role instanceof GodFather))
                state = "Mafia";

        playersByRole.get(Character.DETECTIVE).sendToClient("\u001B[40m\u001B[31m"
                + allTmp.get(chosen).getMemberName() + " " + state + "\u001B[0m");
    }

    /**
     * Shows alive players.
     */
    public static void showAlive() {
        ThreadServer.sendToGroup("*Alive players :", allMembers);
        for (ThreadServer alive : allMembers)
            ThreadServer.sendToGroup("\t+ " + alive.getMemberName(), allMembers);
    }

    /**
     * checks if the game is finished.
     *
     * @return the boolean
     */
    public static boolean isFinished() {
        if (mafias.size() >= citizens.size()) {
            ThreadServer.sendToGroup("\u001B[41m### MAFIAs Won ###\u001B[0m", allMembers);
            return true;
        } else if (mafias.size() == 0) {
            ThreadServer.sendToGroup("\u001B[41m### CITIZENs Won ###\u001B[0m", allMembers);
            return true;
        }
        return false;
    }

    /**
     * Check if a dead player if saved.
     *
     * @return the array list
     */
    public static ArrayList<Character> checkDead() {
        ArrayList<Character> deadChars = new ArrayList<>();
        for (ThreadServer shot : dead)
            if (!(saved.contains(shot))) {
                deadChars.add(shot.getCharacter());
                removeFromLists(shot, 1);
            }
        saved.clear();
        dead.clear();
        return deadChars;
    }

    /**
     * Lets a group to chat.
     *
     * @param members the members
     * @param title   the title
     * @param seconds the seconds
     */
    public static void letChat(ArrayList<ThreadServer> members, String title, int seconds) {
        readyNum = 0;
        ThreadServer.setChatTo(members);
        int count = members.size();
        for (ThreadServer cli : members) {
            if (quiet != cli)
                cli.setChatStatus("begin");
            else
                count--;
        }
        ThreadServer.sendToGroup("\u001B[33m\t\t** " + title + " **\nDiscuss ends in : "
                + seconds + " seconds.\n\t\tIf you were ready to vote, enter \"ready\" \u001B[0m", members);
        long beginTime = System.currentTimeMillis();
        while ((readyNum != count) && (System.currentTimeMillis() - beginTime < seconds * 1000))
            System.out.print("");

        ThreadServer.sendToGroup("\u001B[33mDiscussion ended.\u001B[0m", members);
        for (ThreadServer cli : members)
            cli.setChatStatus("end");
        ThreadServer.chatTo.clear();
        readyNum = 0;

    }

    /**
     * Lets a group to vote.
     *
     * @param choosers   the choosers
     * @param totChoices the tot choices
     * @param title      the title
     */
    public static void letVote(ArrayList<ThreadServer> choosers, ArrayList<ThreadServer> totChoices, String
            title) {
        readyNum = 0;
        votes.clear();
        for (ThreadServer choice : totChoices)
            votes.put(choice, new ArrayList<>());

        for (ThreadServer chooser : choosers) {
            ArrayList<ThreadServer> choices = new ArrayList<>(totChoices);
            Collections.copy(choices, totChoices);
            if (choices.contains(chooser))
                choices.remove(chooser);
            int index = 1;
            chooser.sendToClient("\n\n\n\n\u001B[33m" + "\t\t* " + title + " *");
            for (ThreadServer choice : choices)
                chooser.sendToClient(index++ + ". " + '\u200e' + choice.getMemberName());

            chooser.sendToClient("\t* You can only vote for one player\n\t" +
                    "* You can change your vote till end of the time.\n\t* Once you are done, type \"done\"\u001B[0m");
            Thread t = new Thread() {
                public void run() {
                    chooser.readVote(choices);
                }
            };
            t.start();
        }
        while (readyNum != choosers.size())
            System.out.print("");
        voteResults(choosers);
    }

    /**
     * shows vote results.
     *
     * @param choosers the choosers
     */
    public static void voteResults(ArrayList<ThreadServer> choosers) {
        ThreadServer.sendToGroup("\u001B[33m\n\t\t** Vote Results **", choosers);
        for (ThreadServer threadServer : votes.keySet()) {
            String message = '\u200e' + threadServer.getMemberName() + "    voted by:  " + '\u200e';
            for (String s : votes.get(threadServer))
                message += s + " ";
            ThreadServer.sendToGroup(message, choosers);
        }
        ThreadServer.sendToGroup("\u001B[0m", choosers);
        kick();
    }

    /**
     * Kicks a player.
     */
    public static void kick() {
        ThreadServer voted = null;
        int voteMax = 0;
        for (ThreadServer player : votes.keySet()) {
            int voteNum = votes.get(player).size();
            if (voteNum > voteMax) {
                voteMax = voteNum;
                voted = player;
            }
        }
        int check = 0;
        for (ArrayList<String> list : votes.values())
            if (list.size() == voteMax) {
                check++;
                if (check == 2) {
                    return;
                }
            }
        removeFromLists(voted, 1);
    }

    /**
     * Removes a player from lists.
     *
     * @param player the player
     * @param num    the num
     */
    public static void removeFromLists(ThreadServer player, int num) {
        if (player == null)
            return;
        allMembers.remove(player);
        playersByRole.remove(player.getCharacter());
        if (citizens.contains(player))
            citizens.remove(player);
        else
            mafias.remove(player);

        if (num == 1) {
            ThreadServer.sendToGroup("\u001B[31m\t\t*We lost " + player.getMemberName() + "*\u001B[0m", allMembers);
            Thread t = new Thread() {
                public void run() {
                    String alert = "\t\tX You are dead X :(\n Do you want to observe the rest of game or leave ?" +
                            "\n  1. observe\n  Other nums . leave";
                    if (player.readChoice(alert) == 1) {
                        player.sendToClient("You continue as observer...");
                        observers.add(player);
                    } else
                        player.setChatStatus("exit");
                }
            };
            t.start();
        }
    }

    /**
     * Generates characters.
     */
    public static void generateCharacters() {
        int mafiasNum = (int) Math.ceil(playerNum / 4.0);
        int citizensNum = playerNum - mafiasNum;
        chooseCharacters(mafiasNum, 0, 2);
        chooseCharacters(citizensNum, 3, 9);
    }

    /**
     * Chooses the characters.
     *
     * @param groupNum the group num
     * @param floor    the floor
     * @param ceil     the ceil
     */
    public static void chooseCharacters(int groupNum, int floor, int ceil) {
        int count = 0, num = floor;
        Random rand = new Random();
        while (groupNum > count) {
            ThreadServer client = allMembers.get(rand.nextInt(allMembers.size()));
            setCharacters(client, num++);
            allMembers.remove(client);
            count++;
            if (num >= ceil)
                num = ceil;
        }
    }

    /**
     * Sets characters to players.
     *
     * @param client the client
     * @param num    the num
     */
    public static void setCharacters(ThreadServer client, int num) {
        client.sendToClient("role");
        Character ch = Character.values()[num];
        client.sendToClient(ch.toString());
        client.setCharacter(ch);
        if (ch != Character.CITIZEN && ch != Character.MAFIA)
            playersByRole.put(ch, client);
        if (num >= 0 && num <= 2)
            mafias.add(client);
        else
            citizens.add(client);
    }
}

/**
 * The type Thread server.
 */
class ThreadServer extends Thread {

    private String memberName, chatStatus;
    private final Socket socket;
    private Character character;
    private Player player;
    private BufferedReader input;
    private InputStream inputStream;
    private static HashMap<Socket, String> clientNameList = new HashMap<>();
    /**
     * The players to chat to.
     */
    public static ArrayList<ThreadServer> chatTo;
    private int noResponse;

    /**
     * Instantiates a new Thread server.
     *
     * @param socket the socket
     */
    public ThreadServer(Socket socket) {
        this.socket = socket;
        chatStatus = "";
        noResponse = 0;
    }

    @Override
    public void run() {
        try {
            String outputString = null;
            inputStream = socket.getInputStream();
            input = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            register();
            outputString = input.readLine();
            God.allMembers.add(this);
            String ready = "\t\t\u001B[34m" + outputString + "\n\t\t> " + God.allMembers.size() + "/" + God.playerNum + " ready. <" + "\u001B[0m";
            sendToGroup(ready, God.allMembers);


            while (true) {
                if (God.allMembers.size() == God.playerNum)
                    break;
                System.out.print("");
            }

            while (true) {
                if (chatStatus.equals("begin"))
                    chat();
                else if (chatStatus.equals("exit")) {
                    throw new SocketException();
                }
                System.out.print("");
            }
        } catch (Exception e) {
            try {
                socket.close();
            } catch (IOException io) {
                io.printStackTrace();
            }
            String printMessage = "\t\t" + '\u200e' + "\u001B[33m" +
                    clientNameList.get(socket) + " left the chat room" + "\u001B[0m";
            God.removeFromLists(this, 0);
            if (God.allMembers.size() == 0)
                System.exit(0);
            clientNameList.remove(socket);
            System.out.println(printMessage);
            sendToGroup(printMessage, God.allMembers);
        }
    }

    /**
     * Act int.
     *
     * @param choices the choices to pck
     * @param alert   the alert message
     * @return the chosen num
     */
    public int act(ArrayList<ThreadServer> choices, String alert) {
        sendToClient("act");
        int index = 1;
        if (choices != null)
            for (ThreadServer choice : choices)
                sendToClient(index++ + ". " + choice.getMemberName());
        int chosen = readChoice(alert);
        chosen--;
        if (choices != null)
            if (!(chosen >= 0 && chosen < choices.size()))
                chosen = -1;
        return chosen;

    }

    /**
     * start chat.
     *
     * @throws IOException the io exception
     */
    public void chat() throws IOException {
        while (true) {
            if (inputStream.available() > 0) {
                String message = input.readLine();
                if (message.equals("exit"))
                    throw new SocketException();
                else if (message.equals("ready")) {
                    God.readyNum++;
                    chatStatus = "end";
                    message = '\u200e' + "\t\t\u001B[36m" + memberName + " is ready to vote \u001B[0m";
                } else
                    message = '\u200e' + "\u001B[36m" + memberName + " : \u001B[0m" + '\u200e' + message;
                sendToGroup(message, chatTo);
            }
            if (chatStatus.equals("end"))
                break;
        }
    }

    /**
     * Checks if the player sent his final choice.
     *
     * @param time the time
     * @return the string
     * @throws IOException the io exception
     */
    public String checkIfDone(int time) throws IOException {
        String choice = "";
        long beginMillis = System.currentTimeMillis();
        sendToClient("\n\t\t\u001B[35mYou have " + time + " seconds to choose.\u001B[0m");
        while (System.currentTimeMillis() - beginMillis < (time * 1000))
            if (inputStream.available() > 0) {
                String message = input.readLine();
                if (message.equals("done")) {
                    sendToClient("*Registered..\n");
                    noResponse = 0;
                    return choice;
                } else if (message.equals("exit")) {
                    chatStatus = "exit";
                    return message;
                }
                choice = message;
            }
        noResponse++;
        if (noResponse == 3) {
            sendToClient("\n\t\t > You didnt response three times <\n\t\tYou are kicked...");
        }
        sendToClient("\t\t\u001B[35mTime is finished :(\u001B[36m");
        return "time finished";
    }

    /**
     * Reads vote.
     *
     * @param choices the choices
     */
    public void readVote(ArrayList<ThreadServer> choices) {
        try {
            String chosen = checkIfDone(30);
            God.readyNum++;
            int num = Integer.parseInt(chosen);
            num--;
            if (num >= 0 && num < choices.size()) {
                ArrayList<String> tmp = God.votes.get(choices.get(num));
                tmp.add(getMemberName());
                God.votes.put(choices.get(num), tmp);
            }
        } catch (IOException io) {
            io.printStackTrace();
        } catch (NumberFormatException nf) {
            return;
        }
    }

    /**
     * Reads choice.
     *
     * @param alert the alert message
     * @return the chosen num
     */
    public int readChoice(String alert) {
        if (alert != null)
            sendToClient(alert);
        try {
            if (chatStatus.equals("exit")) {
                System.out.println(9990);
                return 9999;
            }
            int num = Integer.parseInt(checkIfDone(30));
            return num;
        } catch (IOException io) {
            io.printStackTrace();
        } catch (NumberFormatException nf) {
            return -1;
        }
        return -1;
    }

    /**
     * Sends a message to a group.
     *
     * @param outputString the message
     * @param letSee       the group to send to
     */
    public static void sendToGroup(String outputString, ArrayList<ThreadServer> letSee) {
        ArrayList<ThreadServer> chatAllowed = new ArrayList<>(letSee);
        chatAllowed.addAll(God.observers);
        PrintWriter printWriter;
        System.out.println(outputString);
        try {
            for (ThreadServer member : chatAllowed) {
                printWriter = new PrintWriter(new OutputStreamWriter(
                        member.getSocket().getOutputStream(), StandardCharsets.UTF_8), true);
                printWriter.println(outputString);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Sends a message to the player.
     *
     * @param message the message
     */
    public void sendToClient(String message) {
        PrintWriter printWriter;
        try {
            printWriter = new PrintWriter(socket.getOutputStream(), true);
            printWriter.println(message);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Registers the player's name.
     */
    public void register() {
        if (clientNameList.containsKey(socket))
            return;
        try {
            while (true) {
                String name = input.readLine();
                if (!clientNameList.containsValue(name)) {
                    sendToClient("Welcome...");
                    clientNameList.put(socket, name);
                    memberName = name;
                    name = "\t\t" + '\u200e' + "\u001B[33m" + name + " joined. \u001B[0m";
                    sendToGroup(name, God.allMembers);
                    break;
                }
                sendToClient("\t> Name already exists; Try again. <");
            }
        } catch (IOException io) {
            System.out.println(io);
        }
    }

    /**
     * Sets chat status.
     *
     * @param chatStatus the new chat status
     */
    public void setChatStatus(String chatStatus) {
        this.chatStatus = chatStatus;
    }

    /**
     * Gets player's name.
     *
     * @return the name
     */
    public String getMemberName() {
        return memberName;
    }

    /**
     * Gets socket.
     *
     * @return the socket
     */
    public Socket getSocket() {
        return socket;
    }

    /**
     * Sets character & player instance.
     *
     * @param character the character
     */
    public void setCharacter(Character character) {
        this.character = character;
        player = character.getPlayer();
        player.setName(memberName);
    }

    /**
     * Gets character.
     *
     * @return the character
     */
    public Character getCharacter() {
        return character;
    }

    /**
     * Gets player object.
     *
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Sets who to chat to.
     *
     * @param members the members to chat
     */
    public static void setChatTo(ArrayList<ThreadServer> members) {
        chatTo = new ArrayList<>(members);
    }
}
